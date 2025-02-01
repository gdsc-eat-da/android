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

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener {

    // ActivityResultLauncher 선언
    private ActivityResultLauncher<Intent> cameraLauncher;



    private Button btnCamera, btnUpload;
    private ImageView cameraView;
    private EditText eText, inText;
    private Bitmap imageBitmap;  // 사진을 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // 글쓰기 url (strings.xml 파일에 가면 주소 있음)
        final String URL = getString(R.string.api_url);

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
                        // 결과 처리
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
            // 카메라 기능을 Intent
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(cameraIntent);
            }
        } else if (view.getId() == R.id.photoupload) {
            // 업로드 버튼 클릭 시 서버로 데이터 전송
            uploadPost();
        }
    }

    // 서버로 데이터 업로드하는 메서드
    private void uploadPost() {
        if (imageBitmap == null) {
            Log.e("Upload", "이미지가 없습니다.");
            return; // 이미지가 없으면 종료
        }

        // EditText 내용 가져오기
        String contents = eText.getText().toString();
        String ingredients = inText.getText().toString();

        // Bitmap을 MultipartBody.Part로 변환
        MultipartBody.Part filePart = createImagePart(imageBitmap);

        // 텍스트를 RequestBody로 변환
        RequestBody contentsBody = RequestBody.create(MediaType.parse("text/plain"), contents);
        RequestBody ingredientsBody = RequestBody.create(MediaType.parse("text/plain"), ingredients);

        // ApiService 인스턴스 생성
        ApiService apiService = RetrofitClient.getApiService(PhotoActivity.this);

        // API 호출
        Call<ResponseBody> call = apiService.uploadPost(filePart, contentsBody, ingredientsBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("Upload", "Success: " + response.body().toString());
                    // 성공적으로 업로드 처리
                } else {
                    Log.d("Upload", "Failed: " + response.message());
                    // 실패 처리
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Upload", "Error: " + t.getMessage());
                // 네트워크 실패 처리
            }
        });
    }

    // Bitmap을 MultipartBody.Part로 변환하는 메서드
    private MultipartBody.Part createImagePart(Bitmap bitmap) {
        if (bitmap != null) {
            // Bitmap을 ByteArray로 변환
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // ByteArray를 RequestBody로 변환
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
            return MultipartBody.Part.createFormData("photo", "image.jpg", requestBody);
        } else {
            return null;
        }
    }
}
