package root.dongmin.eat_da;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.adapter.AllergyAdapter;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.RetrofitClient;

public class NeedActivity extends AppCompatActivity implements View.OnClickListener {

    // UI 요소
    private Button btnComplete;
    private EditText eText, inText;
    private RadioGroup radioGroup;
    private RadioButton radioNeed, radioDistribute;
    private ImageView back;

    //문고리
    private RadioGroup radioNeedFaceGroup;
    private RadioButton radioNeedFace, radioNeedNoFace;

    // 위치 서비스
    private FusedLocationProviderClient fusedLocationClient;
//알레르기 이걸또 넣어?!
    private RecyclerView allergyRecyclerView;
    private AllergyAdapter allergyAdapter;
    private ArrayList<String> selectedItems, finalselectedItems;

    // API 서비스
    private ApiService apiService;

    // face 값 저장할 변수 (0: face 선택, 1: noface 선택)
    private int isFaceSelected = 0; // 기본값은 face(0)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_need);

        // API 서비스 초기화
        apiService = RetrofitClient.getApiService(this);

        // UI 요소 연결
        btnComplete = findViewById(R.id.needphotoupload);
        eText = findViewById(R.id.needcontext);
        inText = findViewById(R.id.needingredient);
        radioGroup = findViewById(R.id.needradioGroup);
        radioNeed = findViewById(R.id.needfoodNeed);
        radioDistribute = findViewById(R.id.needfoodDistribute);

        radioNeedFaceGroup = findViewById(R.id.radioNeedFace);
        radioNeedFace = findViewById(R.id.needface);
        radioNeedNoFace = findViewById(R.id.neednoface);

        allergyRecyclerView = findViewById(R.id.allergyRecyclerView); // 알레르기 리사이클러뷰
        allergyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedItems = new ArrayList<>();
        finalselectedItems = new ArrayList<>();




        //파이어베이스에서 알레르기 가져와서 리사이클뷰어에 표시
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(uid);
            userRef.child("myalergic").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists())
                    {
                        String myalergic = snapshot.getValue(String.class);
                        Log.d("Firebase", "myalergic: " + myalergic);


                        if (myalergic != null && !myalergic.isEmpty()) {
                            String[] allergies = myalergic.split("_"); // _로 분리
                            for (String allergy : allergies) {
                                if (!selectedItems.contains(allergy)) {
                                    selectedItems.add(allergy); // selectedItems에 추가
                                }
                                if (!finalselectedItems.contains(allergy)) {
                                    finalselectedItems.add(allergy); // finalselectedItems에 추가
                                }
                            }
                        }

                        // RecyclerView에 selectedItems 표시
                        allergyAdapter = new AllergyAdapter(selectedItems, finalselectedItems);
                        allergyRecyclerView.setAdapter(allergyAdapter);





                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    Log.d("Wㅅㅂ", "아니 내가 뭘 잘못했다고");
                }
            });


        }

        //라디오 이미지 토글
        radioNeedFaceGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.needface) {
                    radioNeedFace.setBackgroundResource(R.drawable.checkedface);
                    radioNeedNoFace.setBackgroundResource(R.drawable.noface);
                    isFaceSelected = 0; // ✅ face 선택 시 0
                } else if (checkedId == R.id.neednoface) {
                    radioNeedFace.setBackgroundResource(R.drawable.face);
                    radioNeedNoFace.setBackgroundResource(R.drawable.checkednoface);
                    isFaceSelected = 1; // ✅ noface 선택 시 1
                }
            }
        });

        back = findViewById(R.id.btnback6);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NeedActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
        });


        // 버튼 클릭 리스너 등록
        btnComplete.setOnClickListener(this);

        // 라디오 버튼 초기화
        radioNeed.setButtonTintList(ColorStateList.valueOf(Color.BLACK));
        radioDistribute.setButtonTintList(ColorStateList.valueOf(Color.BLACK));

        // 기본 선택값 설정
        radioGroup.check(R.id.needfoodNeed);

        //다시 음식기부 페이지로
        radioDistribute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NeedActivity.this, PhotoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.needphotoupload) {
            needUploadPost();
        }
    }

    // 위치 권한이 있는지 확인
    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }


    // 📤 게시글 업로드
    private void needUploadPost() {
        if (!isLocationPermissionGranted()) {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String contents = eText.getText().toString().trim();
        String ingredients = inText.getText().toString().trim();

        if (contents.isEmpty() || ingredients.isEmpty()) {
            Log.e("Upload", "내용 또는 재료가 비어 있습니다.");
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        // ✅ 현재 위치 가져오기
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d("Location", "현재 위치: " + latitude + ", " + longitude);

                // ✅ 닉네임 가져와서 API 호출
                getNickname(nickname -> {
                    if (nickname == null) {
                        Toast.makeText(NeedActivity.this, "닉네임을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d("Upload", "닉네임 포함하여 업로드: " + nickname);

                    // ✅ 로그 추가: API 요청 전에 데이터 확인
                    Log.d("Upload", "전송 데이터: contents=" + contents +
                            ", ingredients=" + ingredients +
                            ", nickname=" + nickname +
                            ", latitude=" + latitude +
                            ", longitude=" + longitude);

                    // ✅ RequestBody 변환
                    RequestBody contentsBody = RequestBody.create(MediaType.parse("text/plain"), contents);
                    RequestBody ingredientsBody = RequestBody.create(MediaType.parse("text/plain"), ingredients);
                    RequestBody nicknameBody = RequestBody.create(MediaType.parse("text/plain"), nickname);
                    RequestBody latitudeBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(latitude));
                    RequestBody longitudeBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(longitude));

                    RequestBody faceBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(isFaceSelected));// ✅ 추가된 부분

                    // ✅ API 호출 (게시물과 위치 함께 업로드)
                    Call<ResponseBody> call = apiService.needuploadPost(contentsBody, ingredientsBody, nicknameBody, latitudeBody, longitudeBody, faceBody);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.isSuccessful() && response.body() != null) {

                                    String myalergic = TextUtils.join("_", finalselectedItems);

                                    Map<String, Object> userUpdates = new HashMap<>();
                                    userUpdates.put("myalergic", myalergic);

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    DatabaseReference userRef = database.getReference("UserAccount").child(uid);

                                    userRef.updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("Upload", "알레르기 성공!");

                                            } else {
                                                Log.d("Upload", "알레르기 실패 앙 기모띠");
                                            }
                                        }
                                    });





                                    Log.d("Upload", "게시물 업로드 성공!");
                                    Toast.makeText(NeedActivity.this, "게시물이 업로드되었습니다!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(NeedActivity.this, MainActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                    finish();
                                } else {
                                    Log.e("Upload", "Failed: " + response.code() + " " + response.message());
                                }
                            } catch (Exception e) {
                                Log.e("Upload", "응답 처리 중 오류 발생: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e("Upload", "게시물 업로드 실패: " + t.getMessage());
                        }
                    });
                });
            } else {
                Log.e("Location", "현재 위치를 가져올 수 없습니다.");
            }
        });
    }



    // 🔥 Firebase에서 현재 사용자의 닉네임 가져오는 메서드
    private void getNickname(OnNicknameReceivedListener listener) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid(); // 현재 유저 UID 가져오기
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(uid);

            userRef.child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String nickname = dataSnapshot.getValue(String.class); // 닉네임 가져오기
                    if (nickname != null) {
                        listener.onReceived(nickname); // 콜백으로 전달
                    } else {
                        listener.onReceived(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    listener.onReceived(null);
                }
            });
        } else {
            listener.onReceived(null);
        }
    }

    // 🔥 닉네임을 받아서 처리할 인터페이스 (비동기 처리용)
    interface OnNicknameReceivedListener {
        void onReceived(String nickname);
    }
}
