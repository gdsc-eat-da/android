package root.dongmin.eat_da;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.RetrofitClient;

public class PostRecipeActivity extends AppCompatActivity implements View.OnClickListener{

    // UI 요소
    private Button btnUpload;
    private ImageView cameraView,back;
    private EditText eText, inText;

    // 사진 저장 변수
    private Bitmap imageBitmap;

    // API 서비스
    private ApiService apiService;

    // 카메라 실행 결과 처리
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_recipe);

        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);

        // UI 요소 연결

        btnUpload = findViewById(R.id.recipeupload);
        cameraView = findViewById(R.id.recipecarmeraView);
        eText = findViewById(R.id.recipecontext);
        inText = findViewById(R.id.recipeingredient);

        // 뒤로가기 이벤트 처리
        back = findViewById(R.id.btnback7);
        back.setOnClickListener(v -> finish());

        // 버튼 클릭 리스너 등록
        cameraView.setOnClickListener(this);
        btnUpload.setOnClickListener(this);


        // 카메라 실행 결과 처리
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        imageBitmap = (Bitmap) extras.get("data");
                        cameraView.setImageBitmap(imageBitmap);
                    }
                }
        );

    }

    // 📸 카메라 실행
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.recipecarmeraView) {
            openCamera();
        } else if (view.getId() == R.id.recipeupload) {
            recipePost();
        }
    }

    // 레시피 업로드
    private void recipePost() {
        if (imageBitmap == null) {
            Log.e("Upload", "이미지가 없습니다.");
            return;
        }

        String contents = eText.getText().toString().trim();
        String ingredients = inText.getText().toString().trim();

        if (contents.isEmpty() || ingredients.isEmpty()) {
            Log.e("Upload", "내용 또는 재료가 비어 있습니다.");
            return;
        }

        getNickname(nickname -> {
            if (nickname == null) {
                Toast.makeText(PostRecipeActivity.this, "닉네임을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("Upload", "닉네임 포함하여 업로드: " + nickname);

            // ✅ 이미지 Multipart 변환
            MultipartBody.Part filePart = createImagePart(imageBitmap);

            // ✅ 다른 데이터 RequestBody로 변환
            RequestBody contentsBody = RequestBody.create(MediaType.parse("text/plain"), contents);
            RequestBody ingredientsBody = RequestBody.create(MediaType.parse("text/plain"), ingredients);
            RequestBody nicknameBody = RequestBody.create(MediaType.parse("text/plain"), nickname);


            // ✅ API 호출
            Call<ResponseBody> call = apiService.uploadRecipe(filePart, contentsBody, ingredientsBody, nicknameBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success) {
                                int recipeID = jsonResponse.getInt("recipeID");
                                Log.d("Upload", "recipeID: " + recipeID);

                            } else {
                                Log.e("Upload", "레시피 업로드 실패: " + jsonResponse.getString("message"));
                            }
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

    }

    // 📷 Bitmap -> MultipartBody 변환
    private MultipartBody.Part createImagePart(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), byteArrayOutputStream.toByteArray());
        return MultipartBody.Part.createFormData("photo", "image.jpg", requestBody);
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
                        Log.d("Nickname", "닉네임 가져옴: " + nickname);
                        listener.onReceived(nickname); // 콜백으로 전달
                    } else {
                        Log.e("Nickname", "닉네임이 없습니다.");
                        listener.onReceived(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "닉네임 불러오기 실패: " + databaseError.getMessage());
                    listener.onReceived(null);
                }
            });
        } else {
            Log.e("Nickname", "FirebaseUser가 null입니다.");
            listener.onReceived(null);
        }
    }

    // 🔥 닉네임을 받아서 처리할 인터페이스 (비동기 처리용)
    interface OnNicknameReceivedListener {
        void onReceived(String nickname);
    }
}