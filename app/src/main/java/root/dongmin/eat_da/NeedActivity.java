package root.dongmin.eat_da;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.RetrofitClient;

public class NeedActivity extends AppCompatActivity implements View.OnClickListener {

    // UI 요소
    private Button btnComplete;
    private EditText eText, inText;
    private RadioGroup radioGroup;
    private RadioButton radioNeed, radioDistribute;

    // API 서비스
    private ApiService apiService;

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

    // 📤 게시글 업로드
    private void needUploadPost() {
        String contents = eText.getText().toString().trim();
        String ingredients = inText.getText().toString().trim();

        if (contents.isEmpty() || ingredients.isEmpty()) {
            Log.e("Upload", "내용 또는 재료가 비어 있습니다.");
            return;
        }

        // ✅ 닉네임을 가져온 후 API 요청 실행  완성해야함
//        getNickname(nickname -> {
//            if (nickname == null) {
//                Toast.makeText(NeedActivity.this, "닉네임을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            Log.d("Upload", "닉네임 포함하여 업로드: " + nickname);
//
//            // ✅ 다른 데이터 RequestBody로 변환
//            RequestBody contentsBody = RequestBody.create(MediaType.parse("text/plain"), contents);
//            RequestBody ingredientsBody = RequestBody.create(MediaType.parse("text/plain"), ingredients);
//            RequestBody nicknameBody = RequestBody.create(MediaType.parse("text/plain"), nickname); // ✅ 닉네임 추가
//
//            // ✅ 라디오 버튼 값 (음식 필요 여부)
//            String foodStatus = radioNeed.isChecked() ? "음식이 필요해요" : "음식을 나눠줄래요";
//            RequestBody foodStatusBody = RequestBody.create(MediaType.parse("text/plain"), foodStatus);
//
//            // ✅ API 호출 (닉네임 포함)
//            Call<ResponseBody> call = apiService.uploadPost(contentsBody, ingredientsBody, nicknameBody, foodStatusBody);
//            call.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                    try {
//                        if (response.isSuccessful() && response.body() != null) {
//                            String responseBody = response.body().string();
//                            Log.d("Upload", "Response: " + responseBody);
//                            // 서버 응답을 확인하여 게시물 업로드 성공 여부 처리
//                            Toast.makeText(NeedActivity.this, "게시물이 업로드되었습니다!", Toast.LENGTH_SHORT).show();
//                            finish();
//                        } else {
//                            Log.e("Upload", "게시물 업로드 실패: " + response.code() + " " + response.message());
//                        }
//                    } catch (Exception e) {
//                        Log.e("Upload", "응답 처리 중 오류 발생: " + e.getMessage());
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    Log.e("Upload", "게시물 업로드 실패: " + t.getMessage());
//                }
//            });
//        });
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
