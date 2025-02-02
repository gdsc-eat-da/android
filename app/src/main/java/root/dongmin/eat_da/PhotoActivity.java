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

    private ActivityResultLauncher<Intent> cameraLauncher;
    private Button btnCamera, btnUpload;
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

        // 디자인 정의
        btnCamera = findViewById(R.id.btnPhoto);
        cameraView = findViewById(R.id.carmeraView);
        eText = findViewById(R.id.context);
        inText = findViewById(R.id.ingredient);
        btnUpload = findViewById(R.id.photoupload);

        btnCamera.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

        // ActivityResultLauncher 초기화
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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnPhoto) {
            openCamera();
        } else if (view.getId() == R.id.photoupload) {
            uploadPost();
        }
    }

    // 카메라 실행 메서드
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        }
    }

    // 서버로 데이터 업로드하는 메서드
    private void uploadPost() {
        if (imageBitmap == null) {
            Log.e("Upload", "이미지가 없습니다.");
            return;
        }

        // EditText 내용 가져오기
        String contents = eText.getText().toString().trim();
        String ingredients = inText.getText().toString().trim();

        // 값이 비어있는지 확인
        if (contents.isEmpty() || ingredients.isEmpty()) {
            Log.e("Upload", "내용 또는 재료가 비어 있습니다.");
            return;
        }

        // Bitmap을 MultipartBody.Part로 변환
        MultipartBody.Part filePart = createImagePart(imageBitmap);
        RequestBody contentsBody = RequestBody.create(MediaType.parse("text/plain"), contents);
        RequestBody ingredientsBody = RequestBody.create(MediaType.parse("text/plain"), ingredients);

        // API 호출
        Call<ResponseBody> call = apiService.uploadPost(filePart, contentsBody, ingredientsBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d("Upload", "Success: " + responseBody);
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

    // Bitmap을 MultipartBody.Part로 변환하는 메서드
    private MultipartBody.Part createImagePart(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
        return MultipartBody.Part.createFormData("photo", "image.jpg", requestBody);
    }
}
