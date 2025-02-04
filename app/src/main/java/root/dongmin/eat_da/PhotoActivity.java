package root.dongmin.eat_da;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityResultLauncher<Intent> cameraLauncher, galleryLauncher;
    private Button btnCamera, btnGallery, btnUpload;
    private ImageView cameraView;
    private EditText eText, inText;
    private Bitmap imageBitmap; // 사진을 저장할 변수
    private ApiService apiService; // Retrofit API 서비스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // Retrofit API 서비스 초기화
        apiService = RetrofitClient.getApiService(this);

        // UI 요소 연결
        btnCamera = findViewById(R.id.btnPhoto);
        btnGallery = findViewById(R.id.btnGallery);  // 추가된 버튼
        btnUpload = findViewById(R.id.photoupload);
        cameraView = findViewById(R.id.carmeraView);
        eText = findViewById(R.id.context);
        inText = findViewById(R.id.ingredient);

        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
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

        // 갤러리에서 이미지 선택 결과 처리
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            // 갤러리에서 선택한 이미지의 URI 가져오기
                            Uri selectedImageUri = result.getData().getData();
                            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                            cameraView.setImageBitmap(imageBitmap);  // ImageView에 표시
                        } catch (IOException e) {
                            Log.e("Gallery", "이미지 불러오기 오류: " + e.getMessage());
                        }
                    }
                }
        );
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnPhoto) {
            openCamera();
        } else if (view.getId() == R.id.btnGallery) {
            openGallery();
        } else if (view.getId() == R.id.photoupload) {
            uploadPost();
        }
    }

    // 📸 카메라 실행 메서드
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        }
    }

    // 🖼 갤러리에서 이미지 선택 메서드
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }

    // 📤 서버 업로드
    private void uploadPost() {
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

        MultipartBody.Part filePart = createImagePart(imageBitmap);
        RequestBody contentsBody = RequestBody.create(MediaType.parse("text/plain"), contents);
        RequestBody ingredientsBody = RequestBody.create(MediaType.parse("text/plain"), ingredients);

        Call<ResponseBody> call = apiService.uploadPost(filePart, contentsBody, ingredientsBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        Toast.makeText(PhotoActivity.this, "게시물이 업로드되었습니다!", Toast.LENGTH_SHORT).show();
                        Log.d("Upload", "Success: " + responseBody);

                        // ✅ 업로드 완료 후 메인 화면으로 이동
                        Intent intent = new Intent(PhotoActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // 현재 액티비티 종료
                    } else {
                        Log.e("Upload", "Failed: " + response.code() + " " + response.message());
                        if (response.errorBody() != null) {
                            Log.e("Upload", "Error body: " + response.errorBody().string());
                        }
                    }
                } catch (IOException e) {
                    Log.e("Upload", "응답 처리 중 오류 발생: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload", "Error: " + t.getMessage());
            }
        });
    }

    // Bitmap을 MultipartBody.Part로 변환
    private MultipartBody.Part createImagePart(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // compress 숫자 높일수록 화질 업 max:100
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
        return MultipartBody.Part.createFormData("photo", "image.jpg", requestBody);
    }
}
