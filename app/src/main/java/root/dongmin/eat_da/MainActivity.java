package root.dongmin.eat_da;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.adapter.PostAdapter;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.NearbyPostResponse;
import root.dongmin.eat_da.network.Post;
import root.dongmin.eat_da.network.RetrofitClient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // 위치 권한
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    public String Nickname;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private TextView greed; // 사용자 환영 메시지
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ApiService apiService;
    private boolean isNearbyActive = false; // "근처 게시물 보기" 상태 여부
    private List<Post> allPosts = new ArrayList<>(); // 원래 전체 게시글 저장용
    private FusedLocationProviderClient fusedLocationClient; // 위치 서비스 객체 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // 위치 서비스 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //  위치 권한 확인 및 요청, 저장소도
        checkLocationPermission();
        checkBoxPermission();

        // Firebase 및 UI 요소 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");
        greed = findViewById(R.id.greeding);
        recyclerView = findViewById(R.id.recyclerView);

        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);

        // 사용자 정보 및 게시글 불러오기
        loadUserInfo();
        loadPosts();

        // 버튼 이벤트 처리
        setupButtons();
    }


    // ✅ 저장소 권한 확인 및 요청
    private void checkBoxPermission()
    {
        // 저장소 권한 체크 및 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        }
    }



    // ✅ 위치 권한 확인 및 요청
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // ✅ 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "위치 권한 허용됨");
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다. 기능이 제한됩니다.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }







    // ✅ 사용자 닉네임 가져오기
    private void loadUserInfo() {
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            mDatabaseRef.child(userId).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String nickname = dataSnapshot.getValue(String.class);
                    if (nickname != null) {
                        greed.setText("반갑습니다, " + nickname + "님!");
                        Nickname = nickname;
                    } else {
                        greed.setText("닉네임을 설정해주세요.");
                        greed.setOnClickListener(v -> {
                            Intent intent = new Intent(MainActivity.this, NicknameActivity.class);
                            startActivity(intent);
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "닉네임을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // 로그인되지 않은 경우 로그인 화면으로 이동
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // ✅ 게시글 목록 불러오기
    private void loadPosts() {
        Call<List<Post>> call = apiService.getPosts();
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allPosts = response.body(); // 기존 게시물 저장
                    postAdapter = new PostAdapter(MainActivity.this, allPosts);
                    recyclerView.setAdapter(postAdapter);
                } else {
                    showErrorMessage("게시글을 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                showErrorMessage("네트워크 오류로 게시글을 불러올 수 없습니다.");
            }
        });
    }

    // ✅ 근처 게시글 불러오기
    private void loadNearbyPosts(double latitude, double longitude) {
        double radius = 5.0; // 반경 5km
        Call<NearbyPostResponse> call = apiService.getNearbyPosts(latitude, longitude, radius);

        Log.d("Upload", "API 요청 URL: " + call.request().url());

        call.enqueue(new Callback<NearbyPostResponse>() {
            @Override
            public void onResponse(@NonNull Call<NearbyPostResponse> call, @NonNull Response<NearbyPostResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Post> postList = response.body().getPosts();
                    Log.d("Upload", "서버 응답 데이터: " + new Gson().toJson(postList));
                    postAdapter = new PostAdapter(MainActivity.this, postList);
                    recyclerView.setAdapter(postAdapter);
                } else {
                    Log.e("Upload", "근처 게시물 불러오기 실패: 위도 :"+ latitude + " 경도:" + longitude + " 거리: " + radius);
                    showErrorMessage("근처 게시글을 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<NearbyPostResponse> call, @NonNull Throwable t) {
                showErrorMessage("네트워크 오류로 게시글을 불러올 수 없습니다.");
            }
        });
    }


    // ✅ 근처 게시글 보기 토글 기능
    private void toggleNearbyPosts(Button nearbyButton) {
        if (isNearbyActive) {
            // 📌 토글 OFF: 원래 게시글 목록 복원
            isNearbyActive = false;
            nearbyButton.setText("근처 게시글 보기");

            postAdapter = new PostAdapter(MainActivity.this, allPosts);
            recyclerView.setAdapter(postAdapter);
        } else {
            // 📌 토글 ON: 현재 위치 가져와서 근처 게시글 불러오기
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                isNearbyActive = true;
                                nearbyButton.setText("근처 게시글 취소");

                                loadNearbyPosts(latitude, longitude);
                            } else {
                                Toast.makeText(MainActivity.this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // 권한이 없는 경우 다시 요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }



    // ✅ 버튼 클릭 이벤트 처리
    private void setupButtons() {
        Button photobutton = findViewById(R.id.btngotophoto);
        photobutton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, PhotoActivity.class)));

        Button chatbutton = findViewById(R.id.btnchat);
        chatbutton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, IdListActivity.class)));

        Button findUserButton = findViewById(R.id.btnFindUser);
        findUserButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, UserFindActivity.class)));

        Button nearbutton = findViewById(R.id.btnNearby);
        nearbutton.setOnClickListener(view -> toggleNearbyPosts(nearbutton));

        Button mypagebutton = findViewById(R.id.btnMyPage);
        mypagebutton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, MyPageActivity.class)));
    }

    // ✅ 오류 메시지 출력
    private void showErrorMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
