package root.dongmin.eat_da;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.RetrofitClient;

public class MySetting extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mFirebaseAuth;
    private ImageView profile;
    private Uri imageUri;
    private EditText nicknameChange;
    private ImageView back;
    private ProgressBar progressBar;
    private TextView levelshow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_setting);

        back = findViewById(R.id.btnback);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MySetting.this, MyPageActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 뒤로가기 버튼 이벤트 처리
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 버튼을 눌렀을 때 MyPageActivity로 이동
                Intent intent = new Intent(MySetting.this, MyPageActivity.class);
                startActivity(intent);
                finish();  // 현재 Activity 종료
            }
        });

        profile = findViewById(R.id.profileImage);

        profile.setOnClickListener(v -> {
            // 갤러리에서 이미지 선택하기 위한 Intent
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");  // 이미지 파일만 선택할 수 있도록 설정
            startActivityForResult(intent, 100);  // 100은 요청 코드, 나중에 onActivityResult에서 사용할 코드
        });

        nicknameChange = findViewById(R.id.nicknameChange);

        // Firebase 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // 현재 로그인된 사용자 정보 가져오기
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();

            // 사용자 정보 가져오기 (닉네임과 프로필 이미지)
            mDatabaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String nickname = dataSnapshot.child("nickname").getValue(String.class);
                        String profileImageUrl = dataSnapshot.child("profileImage").getValue(String.class);

                        // 텍스트뷰에 사용자 정보 설정
                        if (nickname != null) {
                            nicknameChange.setText(nickname);
                        } else {
                            nicknameChange.setText("닉네임이 없습니다.");
                        }

                        if (profileImageUrl != null) {
                            Glide.with(MySetting.this)
                                    .load(profileImageUrl)  // Firebase에서 가져온 URL
                                    .into(profile);  // ImageView에 로드
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MySetting.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "로그인되지 않은 사용자입니다.", Toast.LENGTH_SHORT).show();
        }

        // 닉네임 변경 후 저장 버튼 클릭 리스너
        findViewById(R.id.saveNicknameButton).setOnClickListener(v -> {
            String newNickname = nicknameChange.getText().toString().trim();

            if (!newNickname.isEmpty()) {
                // 변경된 닉네임을 Firebase에 저장할지 묻는 다이얼로그 띄우기
                new AlertDialog.Builder(MySetting.this)
                        .setMessage("변경된 닉네임을 저장하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("확인", (dialog, id) -> {
                            // Firebase에 닉네임 저장
                            FirebaseUser firebaseUser1 = mFirebaseAuth.getCurrentUser();
                            if (firebaseUser1 != null) {
                                String uid = firebaseUser1.getUid();
                                mDatabaseRef.child(uid).child("nickname").setValue(newNickname)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(MySetting.this, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MySetting.this, "닉네임 변경 실패", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            } else {
                Toast.makeText(MySetting.this, "닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        progressBar = findViewById(R.id.progressBar);
        levelshow = findViewById(R.id.level);


        loadUserLevel();

    }

    // 갤러리에서 이미지 선택 후 처리. 또한 알레르기 데이터 가져오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Firebase Database Reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = database.getReference("UserAccount"); // UserAccount 테이블 기준
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 로그인한 사용자 UID 가져오기


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
                                                Toast.makeText(MySetting.this, "프로필 사진이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                                Log.d("UploadProfile", "Firebase에 프로필 이미지 업데이트 성공");

                                                // Glide로 프로필 이미지 로드
                                                Glide.with(MySetting.this)
                                                        .load(imageUrl)  // Firebase에서 저장된 이미지 URL
                                                        .into(profile);  // ImageView에 로드
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(MySetting.this, "프로필 사진 업데이트 실패", Toast.LENGTH_SHORT).show();
                                                Log.e("UploadProfile", "Firebase 업데이트 실패: " + e.getMessage());
                                            });
                                }
                            } else {
                                Log.e("UploadProfile", "이미지 URL이 유효하지 않습니다.");
                                Toast.makeText(MySetting.this, "유효한 이미지 URL이 아닙니다.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MySetting.this, "서버 응답 처리 실패", Toast.LENGTH_SHORT).show();
                            Log.e("UploadProfile", "서버 응답 처리 중 오류 발생: " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(MySetting.this, "서버 오류", Toast.LENGTH_SHORT).show();
                        Log.e("UploadProfile", "서버 오류 발생: " + response.code());
                    }
                }


                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(MySetting.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                    Log.e("UploadProfile", "네트워크 오류 발생: " + t.getMessage());
                }
            });
        } else {
            Log.e("UploadProfile", "파일이 존재하지 않습니다.");
            Toast.makeText(this, "파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ 사용자 레벨 가져오기
    private void loadUserLevel() {
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            // transactionCount 값을 가져오기
            mDatabaseRef.child(userId).child("transactionCount").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Integer transactionCount = dataSnapshot.getValue(Integer.class);

                    if (transactionCount != null) {
                        // 레벨 계산
                        int level = transactionCount / 5; // 레벨 (5로 나눈 몫)
                        int remainder = transactionCount % 5; // 남은 거래 횟수 (5로 나눈 나머지)
                        Log.d("API_DEBUG", "레벨 트랜직션카운트(오리지널): " + transactionCount);
                        Log.d("API_DEBUG", "레벨: " + level);
                        Log.d("API_DEBUG", "남은 거래 횟수: " + remainder);


                        progressBar.setProgress(remainder); // 나머지 값을 progress로 설정
                        levelshow.setText(level + "Lv");
                    } else {

                        progressBar.setProgress(0); // 기본값 0으로 설정
                        levelshow.setText("0Lv");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MySetting.this, "거래 횟수를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // 로그인되지 않은 경우 로그인 화면으로 이동
            Intent intent = new Intent(MySetting.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
