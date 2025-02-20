package root.dongmin.eat_da;

import android.Manifest;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.adapter.NeedPostAdapter;
import root.dongmin.eat_da.adapter.PostAdapter;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.NearbyPostResponse;
import root.dongmin.eat_da.network.NeedPost;
import root.dongmin.eat_da.network.NeedPostResponseWrapper;
import root.dongmin.eat_da.network.Post;
import root.dongmin.eat_da.network.RetrofitClient;

import android.os.Build;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // 위치 권한
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    public String Nickname;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private TextView greed; // 사용자 환영 메시지
    private RecyclerView recyclerView, needrecyclerView;
    private PostAdapter postAdapter;
    private NeedPostAdapter needPostAdapter;
    private ApiService apiService;
    private boolean isNearbyActive = false; // "근처 게시물 보기" 상태 여부
    private List<Post> allPosts = new ArrayList<>(); // 원래 전체 게시글 저장용
    private List<String> chatList = new ArrayList<>();
    private List<NeedPost> needPosts;

    private FusedLocationProviderClient fusedLocationClient; // 위치 서비스 객체 추가

    private BottomNavigationView bottomNavigationView;



    // 1초에 한 번씩 loadChatList()를 호출
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            loadChatList();
            handler.postDelayed(this, 1000); // 1초 후에 다시 실행
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        loadChatList();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            private int previousItemId = R.id.nav_home; // 초기 선택된 아이콘 (homeclicked 상태)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (previousItemId == item.getItemId()) {
                    return false; // 동일한 아이템 클릭 방지
                }

                // 1️⃣ 이전 아이콘을 default로 변경
                updateIcon(previousItemId, false);

                // 2️⃣ 현재 클릭된 아이콘을 clicked 상태로 변경
                updateIcon(item.getItemId(), true);

                // 3️⃣ 현재 클릭된 아이콘을 이전 아이콘으로 설정
                previousItemId = item.getItemId();

                // 아이템 선택 해제 (중요)
                item.setCheckable(false);
                item.setChecked(false);


                if (item.getItemId() == R.id.nav_home) {
                    Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
                    startActivity(intent);
                    return true;
                }else if (item.getItemId() == R.id.chat) {
                    Intent intent = new Intent(MainActivity.this, IdListActivity.class );
                    startActivity(intent);
                }else if (item.getItemId() == R.id.work_load){
                    Intent intent = new Intent(MainActivity.this,MapActivity.class);
                    intent.putParcelableArrayListExtra("needPostList", new ArrayList<>(needPosts)); // 리스트 전달
                    setIntent(intent);
                    startActivity(intent);



                }
                return false;
            }
        });




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

        needrecyclerView = findViewById(R.id.recyclerNeedView);
        needrecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);

        // 사용자 정보 및 게시글 불러오기
        loadUserInfo();
        handler.post(runnable);
        loadPosts();
        loadNeedPosts();

        // 버튼 이벤트 처리
        setupButtons();
    }

    // 아이콘 업데이트 함수
    private void updateIcon(int itemId, boolean isClicked) {
        if (bottomNavigationView == null) return;

        int iconRes;
        if (itemId == R.id.nav_home) {
            Log.d("하단바 동작","하단바 클릭됨");
            iconRes = isClicked ? R.drawable.homeclicked : R.drawable.homedefault;
        } else if (itemId == R.id.chat) {
            Log.d("하단바 동작","하단바 클릭됨");
            iconRes = isClicked ? R.drawable.chatclicked : R.drawable.chatdefault;
        } else if (itemId == R.id.nav_profile) {
            Log.d("하단바 동작","하단바 클릭됨");
            iconRes = isClicked ? R.drawable.mypageclicked : R.drawable.mypagedefault;
        } else if (itemId == R.id.work_load) {
            Log.d("하단바 동작","하단바 클릭됨");
            iconRes = isClicked ? R.drawable.workloadclicked : R.drawable.workloaddefault;
        } else {
            return;
        }
        bottomNavigationView.getMenu().findItem(itemId).setIcon(iconRes);

        bottomNavigationView.getMenu().findItem(itemId).setChecked(true);
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

    // ✅ 일단 전체 채팅 리스트 로드시켜 놓기.
    private void loadChatList() {
        //chatList = new ArrayList<>();
        // chatList가 null이 아니면 비워주기
        if (chatList != null) {
            chatList.clear(); // 기존 항목 모두 제거
        } else {
            chatList = new ArrayList<>(); // 만약 null이라면 새로 초기화
        }

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("chat");

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Toast.makeText(MainActivity.this, "채팅접근중.", Toast.LENGTH_SHORT).show();
                //Log.d("ChatData", "Children count: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String chatId = snapshot.getKey(); // 최상위 키(채팅 ID) 가져오기
                    if (chatId != null) {
                        chatList.add(chatId);
                    }
                }

                handleChatList(chatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "채팅 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleChatList(List<String> chatList) {
        for (String chat : chatList) {
            //Log.d("ChatData", chat);
        }
        //그리고 이걸 보내야 한다...!!!!
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

    // 필요 게시물 목록 불러오기
    private void loadNeedPosts() {
        Call<NeedPostResponseWrapper> call = apiService.getNeedPosts();
        call.enqueue(new Callback<NeedPostResponseWrapper>() {
            @Override
            public void onResponse(@NonNull Call<NeedPostResponseWrapper> call, @NonNull Response<NeedPostResponseWrapper> response) {
                Log.d("API_RESPONSE", "HTTP Status Code: " + response.code());
                Log.d("API_RESPONSE", "Response Message: " + response.message());

                // ✅ 추가한 디버깅 코드
                if (response.body() == null) {
                    Log.e("API_DEBUG", "response.body()가 null입니다.");
                } else {
                    String jsonResponse = new Gson().toJson(response.body());
                    Log.d("API_DEBUG", "Response Body JSON: " + jsonResponse);

                    if (response.body().getNeedPosts() == null) {
                        Log.e("API_DEBUG", "getNeedPosts()가 null입니다.");
                    } else {
                        Log.d("API_DEBUG", "Need Posts List Size: " + response.body().getNeedPosts().size());
                    }
                }

                if (response.body() != null && response.body().getNeedPosts() != null) {

                    needPosts = response.body().getNeedPosts();
                    needPostAdapter = new NeedPostAdapter(MainActivity.this, needPosts);
                    Log.d("API_RESPONSE", "Sending NeedPost List: " + needPosts.toString());
                    needrecyclerView.setAdapter(needPostAdapter);


                    Log.d("MAP_DEBUG", "🚀 needPostList를 전달하기 직전: " + needPosts.toString());


                    if (needPosts.isEmpty()) {
                        Log.d("API_RESPONSE", "NeedPost List is empty.");

                    } else {
                        Log.d("API_RESPONSE", "NeedPost List: " + needPosts.toString());
                        for (NeedPost post : needPosts) {
                            Log.d("API_RESPONSE", "Post ID: " + post.getPostID());
                            Log.d("API_RESPONSE", "Contents: " + post.getContents());
                            Log.d("API_RESPONSE", "Nickname: " + post.getNickname());
                            Log.d("API_RESPONSE", "Latitude: " + post.getLatitude());
                            Log.d("API_RESPONSE", "Longitude: " + post.getLongitude());
                        }
                    }

                } else {
                    Log.d("API_RESPONSE", "Response Body or NeedPost List is null");
                    showErrorMessage("필요 게시글을 불러올 수 없습니다.");
                    needPostAdapter = new NeedPostAdapter(MainActivity.this, new ArrayList<>()); // 빈 리스트로 초기화
                    needrecyclerView.setAdapter(needPostAdapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<NeedPostResponseWrapper> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "네트워크 오류: " + t.getMessage());
                showErrorMessage("네트워크 오류로 필요 게시글을 불러올 수 없습니다.");

                // 네트워크 오류 발생 시 RecyclerView에 빈 리스트 적용
                needPostAdapter = new NeedPostAdapter(MainActivity.this, new ArrayList<>());
                needrecyclerView.setAdapter(needPostAdapter);
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

//        Button chatbutton = findViewById(R.id.btnchat);
//        chatbutton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, IdListActivity.class)));

        Button findUserButton = findViewById(R.id.btnFindUser);
        findUserButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, UserFindActivity.class)));

        Button nearbutton = findViewById(R.id.btnNearby);
        nearbutton.setOnClickListener(view -> toggleNearbyPosts(nearbutton));

//        Button mypagebutton = findViewById(R.id.btnMyPage);
//        mypagebutton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, MyPageActivity.class)));
        findUserButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UserFindActivity.class);
            intent.putStringArrayListExtra("chatList", new ArrayList<>(chatList)); // 리스트 전달
            intent.putExtra("nickname", Nickname);
            startActivity(intent);
        });

    }

    // ✅ 오류 메시지 출력
    private void showErrorMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
