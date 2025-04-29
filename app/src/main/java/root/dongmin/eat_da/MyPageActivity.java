package root.dongmin.eat_da;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.NeedPost;
import root.dongmin.eat_da.network.RetrofitClient;

public class MyPageActivity extends AppCompatActivity {

    private TextView namePage; // 사용자 닉네임 표시
    private TextView transaction; // 총 거래 횟수 표시
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mFirebaseAuth;
    private ImageView profile;
    private Uri imageUri; // 갤러리에서 선택된 이미지 URI
    private BottomNavigationView bottomNavigationView;
    private Button myPost;
    private ImageButton logout;
    public List<String> selectedItems;
    public String selectedJoinedItems;
    private boolean alarmSetting = true;
    private SwitchCompat alarmSwitch;
    private ImageView goSetting;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            private int previousItemId = R.id.nav_profile; // 초기 선택된 아이콘 (homeclicked 상태)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (previousItemId == item.getItemId()) {
                    return false; // 동일한 아이템 클릭 방지
                }


                if (item.getItemId() == R.id.nav_profile) {
                    Toast.makeText(MyPageActivity.this, "Mypage", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_home) {
                    Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }else if (item.getItemId() == R.id.chat) {
                    Intent intent = new Intent(MyPageActivity.this, UserFindActivity.class);
                    startActivity(intent);
                    return true;
                }else if (item.getItemId() == R.id.work_load){
                    Intent intent = new Intent(MyPageActivity.this,MapActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }else if (item.getItemId() == R.id.recipe){
                    Intent intent = new Intent(MyPageActivity.this,RecipeActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });

        namePage = findViewById(R.id.nickname);
        transaction = findViewById(R.id.donationCount);
        profile = findViewById(R.id.profileImage);

        profile.setOnClickListener(v -> {
            // 갤러리에서 이미지 선택하기 위한 Intent
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");  // 이미지 파일만 선택할 수 있도록 설정
            startActivityForResult(intent, 100);  // 100은 요청 코드, 나중에 onActivityResult에서 사용할 코드
        });

        // 알레르기 버튼 클릭 리스너 설정                                                 <알레르기!>
        Button alergicButton = findViewById(R.id.alergicButton);
        alergicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 알레르기 액티비티로 이동하면서 데이터 전달
                Intent intent = new Intent(MyPageActivity.this, alergicActivity.class);

                // 예시: "selectedItems"라는 값을 전달
                intent.putExtra("selectedItems", (Serializable) selectedItems);

                startActivityForResult(intent, 101); // 100은 요청 코드, 뒤에서 결과 받기 위해 사용
            }
        });

        // Firebase 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // 현재 로그인된 사용자 정보 가져오기
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();



        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();

            // 사용자 정보 가져오기 (닉네임과 거래 횟수)
            mDatabaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String nickname = dataSnapshot.child("nickname").getValue(String.class);

                        Integer transactionCount = dataSnapshot.child("transactionCount").getValue(Integer.class);
                        String profileImageUrl = dataSnapshot.child("profileImage").getValue(String.class);

                        // 텍스트뷰에 사용자 정보 설정
                        if (nickname != null) {
                            namePage.setText(nickname);
                        } else {
                            namePage.setText("닉네임이 없습니다.");
                        }

                        if (transactionCount != null) {
                            transaction.setText(String.format("%d", transactionCount));
                        } else {
                            transaction.setText("0");
                        }

                        if (profileImageUrl != null) {
                            Glide.with(MyPageActivity.this)
                                    .load(profileImageUrl)  // Firebase에서 가져온 URL
                                    .into(profile);  // ImageView에 로드
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MyPageActivity.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "로그인되지 않은 사용자입니다.", Toast.LENGTH_SHORT).show();
        }


        // 내 게시물들 보기
        myPost = findViewById(R.id.btnMyPost);
        myPost.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPageActivity.this, MyPostActivity.class);
                startActivity(intent);
            }
        });

        // 로그아웃 구현
        logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(v.getContext()) // context 대신 v.getContext() 사용 가능
                        .setTitle("로그아웃하시겠습니까?")
                        .setMessage("로그인 창으로 돌아가시겠습니까?")
                        .setPositiveButton("확인", (dialogInterface, which) -> {
                            // Firebase 로그아웃 처리
                            FirebaseAuth.getInstance().signOut();

                            // 로그인 화면으로 이동
                            Intent intent = new Intent(v.getContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // 현재 액티비티 종료
                        })
                        .setNegativeButton("취소", null)
                        .show();

                // "확인" 버튼의 텍스트 색을 검정으로 설정
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

                // "취소" 버튼의 텍스트 색을 검정으로 설정
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        Drawable trackDrawable = ContextCompat.getDrawable(this, R.drawable.track);
        Drawable thumbDrawable = ContextCompat.getDrawable(this, R.drawable.thumb);

        alarmSwitch = findViewById(R.id.btnAlarm); // 찾고

        alarmSwitch.setChecked(alarmSetting);

        alarmSwitch.setTrackDrawable(trackDrawable); // 적용
        alarmSwitch.setThumbDrawable(thumbDrawable);


        alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleSetAlarm(alarmSwitch);
        });

        goSetting = findViewById(R.id.btnGoMySetting);
        goSetting.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MySetting.class);
            v.getContext().startActivity(intent);
        });


    }



    // 갤러리에서 이미지 선택 후 처리. 또한 알레르기 데이터 가져오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Firebase Database Reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = database.getReference("UserAccount"); // UserAccount 테이블 기준
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 로그인한 사용자 UID 가져오기

        // 1️⃣ 알레르기 액티비티에서 데이터를 받아오는 경우 (requestCode: 101)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null) {
                List<String> modifiedItems = (List<String>) data.getSerializableExtra("modifiedItems");
                if (modifiedItems != null) {
                    selectedItems = modifiedItems;
                    Log.d("PhotoActivity", "Modified items: " + selectedItems);
                    selectedJoinedItems = TextUtils.join("_", selectedItems);

                    // Firebase Realtime Database에 저장
                    if (uid != null) {
                        mDatabaseRef.child(uid).child("myalergic").setValue(selectedJoinedItems)
                                .addOnSuccessListener(aVoid -> Log.d("Firebase", "myalergic 데이터 저장 성공"))
                                .addOnFailureListener(e -> Log.e("Firebase", "myalergic 데이터 저장 실패", e));
                    }
                }
            }
        }

        // 2️⃣ 갤러리에서 이미지 선택 후 처리하는 경우 (requestCode: 100)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData(); // 선택된 이미지 URI

            // 실제 파일 경로를 얻기 위한 메서드 호출
            String realFilePath = getRealPathFromURI(imageUri);

            if (realFilePath != null) {
                Log.d("UploadProfile", "실제 파일 경로: " + realFilePath);
                File file = new File(realFilePath);
                if (file.exists()) {
                    uploadProfileImage(file);
                } else {
                    Log.e("UploadProfile", "파일이 존재하지 않습니다.");
                    Toast.makeText(this, "파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("UploadProfile", "파일 경로를 가져올 수 없습니다.");
                Toast.makeText(this, "파일 경로를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }

            // Glide로 미리보기
            Glide.with(this)
                    .load(imageUri)
                    .into(profile);
        }
    }



    // *URI에서 실제 파일 경로를 가져오는 메서드*
    private String getRealPathFromURI(Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePath;
    }



    // imageUri.getPath()로는 실제 경로를 얻지 못했음
    private void uploadProfileImage(File file) {
        if (file != null && file.exists()) {
            Log.d("UploadProfile", "파일 경로: " + file.getAbsolutePath());

            // 나머지 업로드 처리 로직
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("photo", file.getName(), requestBody);

            ApiService apiService = RetrofitClient.getApiService(this);
            Call<ResponseBody> call = apiService.uploadProfile(part);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("UploadProfile", "서버 응답 상태: " + response.code());

                    if (response.isSuccessful()) {
                        try {
                            // 서버에서 반환된 JSON 응답을 문자열로 받아옴
                            String responseStr = response.body().string();
                            Log.d("UploadProfile", "서버에서 반환된 응답: " + responseStr);

                            // 서버 응답에서 image_url 추출 (예: JSON 형태의 응답에서 "image_url" 값을 파싱)
                            JSONObject jsonResponse = new JSONObject(responseStr);
                            String imageUrl = jsonResponse.getString("image_url");

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                // Firebase에 이미지 URL 저장
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (firebaseUser != null) {
                                    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");
                                    String uid = firebaseUser.getUid();

                                    // Firebase에 이미지 URL 업데이트
                                    mDatabaseRef.child(uid).child("profileImage").setValue(imageUrl)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(MyPageActivity.this, "프로필 사진이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                                Log.d("UploadProfile", "Firebase에 프로필 이미지 업데이트 성공");

                                                // Glide로 프로필 이미지 로드
                                                Glide.with(MyPageActivity.this)
                                                        .load(imageUrl)  // Firebase에서 저장된 이미지 URL
                                                        .into(profile);  // ImageView에 로드
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(MyPageActivity.this, "프로필 사진 업데이트 실패", Toast.LENGTH_SHORT).show();
                                                Log.e("UploadProfile", "Firebase 업데이트 실패: " + e.getMessage());
                                            });
                                }
                            } else {
                                Log.e("UploadProfile", "이미지 URL이 유효하지 않습니다.");
                                Toast.makeText(MyPageActivity.this, "유효한 이미지 URL이 아닙니다.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MyPageActivity.this, "서버 응답 처리 실패", Toast.LENGTH_SHORT).show();
                            Log.e("UploadProfile", "서버 응답 처리 중 오류 발생: " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(MyPageActivity.this, "서버 오류", Toast.LENGTH_SHORT).show();
                        Log.e("UploadProfile", "서버 오류 발생: " + response.code());
                    }
                }


                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(MyPageActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                    Log.e("UploadProfile", "네트워크 오류 발생: " + t.getMessage());
                }
            });
        } else {
            Log.e("UploadProfile", "파일이 존재하지 않습니다.");
            Toast.makeText(this, "파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }



    // 알람 설정 기능
    private void toggleSetAlarm(SwitchCompat alarmSwitch) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE); //이걸로 앱이 재시작 되어도 상태 유지
        SharedPreferences.Editor editor = sharedPreferences.edit();

        alarmSetting = !alarmSetting; // 상태 변경
        editor.putBoolean("alarmSetting", alarmSetting); // 변경된 상태 저장
        editor.apply();

        if (alarmSetting) {
            setAlarm(); // 알람 활성화
            Toast.makeText(this, "알람이 활성화되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            cancelAlarm(); // 알람 비활성화
            Toast.makeText(this, "알람이 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
        }

        alarmSwitch.setChecked(alarmSetting); // UI 업데이트
    }

    // 알람을 설정하는 메서드
    private void setAlarm() {
        // 여기에 알람을 설정하는 로직 추가 (예: AlarmManager 활용)
        Log.d("Alarm", "알람이 활성화되었습니다.");
    }

    // 알람을 취소하는 메서드
    private void cancelAlarm() {
        // 여기에 알람을 해제하는 로직 추가
        Log.d("Alarm", "알람이 비활성화되었습니다.");
    }


}
