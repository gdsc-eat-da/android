package root.dongmin.eat_da;

import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.RetrofitClient;

public class MyPageActivity extends AppCompatActivity {

    private TextView namePage; // 사용자 닉네임 표시
    private TextView transaction; // 총 거래 횟수 표시
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mFirebaseAuth;
    private ImageView profile;
    private Uri imageUri; // 갤러리에서 선택된 이미지 URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        namePage = findViewById(R.id.nickname);
        transaction = findViewById(R.id.donationCount);
        profile = findViewById(R.id.profileImage);

        profile.setOnClickListener(v -> {
            // 갤러리에서 이미지 선택하기 위한 Intent
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");  // 이미지 파일만 선택할 수 있도록 설정
            startActivityForResult(intent, 100);  // 100은 요청 코드, 나중에 onActivityResult에서 사용할 코드
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
                            namePage.setText(nickname + " 님의 페이지");
                        } else {
                            namePage.setText("닉네임이 없습니다.");
                        }

                        if (transactionCount != null) {
                            transaction.setText(String.format("기부횟수 %d 회", transactionCount));
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
    }

    // 갤러리에서 이미지 선택 후 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData(); // 선택된 이미지 URI

            // 실제 파일 경로를 얻기 위한 메서드 호출
            String realFilePath = getRealPathFromURI(imageUri);

            if (realFilePath != null) {
                Log.d("UploadProfile", "실제 파일 경로: " + realFilePath);
                File file = new File(realFilePath);
                if (file.exists()) {
                    // 파일이 존재하면 이미지를 업로드
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
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { android.provider.MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        } else {
            return null;
        }
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
                            // 서버에서 반환된 이미지 URL 가져오기
                            String responseStr = response.body().string();
                            Log.d("UploadProfile", "서버에서 반환된 이미지 URL: " + responseStr);

                            String imageUrl = responseStr; // 실제 URL 파싱 로직이 필요할 수 있음
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (firebaseUser != null) {
                                DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");
                                String uid = firebaseUser.getUid();
                                mDatabaseRef.child(uid).child("profileImage").setValue(imageUrl)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(MyPageActivity.this, "프로필 사진이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                            Log.d("UploadProfile", "Firebase에 프로필 이미지 업데이트 성공");
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(MyPageActivity.this, "프로필 사진 업데이트 실패", Toast.LENGTH_SHORT).show();
                                            Log.e("UploadProfile", "Firebase 업데이트 실패: " + e.getMessage());
                                        });
                            }
                        } catch (IOException e) {
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
}
