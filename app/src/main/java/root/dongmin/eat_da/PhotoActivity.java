package root.dongmin.eat_da;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener {

    // UI 요소
    private Button btnCamera, btnGallery, btnUpload;
    private ImageView cameraView;
    private EditText eText, inText;

    // 사진 저장 변수
    private Bitmap imageBitmap;

    // API 서비스
    private ApiService apiService;

    // 위치 서비스
    private FusedLocationProviderClient fusedLocationClient;

    // 카메라 & 갤러리 실행 결과 처리
    private ActivityResultLauncher<Intent> cameraLauncher, galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);

        // UI 요소 연결
        btnCamera = findViewById(R.id.btnPhoto);
        btnGallery = findViewById(R.id.btnGallery);
        btnUpload = findViewById(R.id.photoupload);
        cameraView = findViewById(R.id.carmeraView);
        eText = findViewById(R.id.context);
        inText = findViewById(R.id.ingredient);

        // 버튼 클릭 리스너 등록
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
                            Uri selectedImageUri = result.getData().getData();
                            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                            cameraView.setImageBitmap(imageBitmap);
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

    // 📸 카메라 실행
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        }
    }

    // 🖼 갤러리 열기
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }


    // 위치 권한이 있는지 확인
    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // 📤 게시글 업로드
    private void uploadPost() {
        if (!isLocationPermissionGranted()) {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

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
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        boolean success = jsonResponse.getBoolean("success");

                        if (success) {
                            int postID = jsonResponse.getInt("postID");
                            Log.d("Upload", "postID: " + postID);

                            // 위지 저장 요청 실행
                            uploadLocation(postID);
                        } else {
                            Log.e("Upload", "게시물 업로드 실패: " + jsonResponse.getString("message"));
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
    }

    // 📍 위치 업로드
    private void uploadLocation(int postID) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d("Location", "현재 위치: " + latitude + ", " + longitude);

                Call<ResponseBody> call = apiService.uploadLocation(postID, latitude, longitude);
                Log.e("PhotoActivity", "🔗 요청 보낸 URL: " + call.request().url());
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d("Upload", "위치 업로드 성공!");
                            Log.d("Upload", "보내는 데이터: postID=" + postID + ", lat=" + latitude + ", lng=" + longitude);
                            Toast.makeText(PhotoActivity.this, "게시물이 업로드되었습니다!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PhotoActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();
                        } else {
                            Log.e("Upload", "위치 업로드 실패: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("Upload", "위치 업로드 실패: " + t.getMessage());
                    }
                });
            } else {
                Log.e("Location", "현재 위치를 가져올 수 없습니다.");
            }
        });
    }

    // 📷 Bitmap -> MultipartBody 변환
    private MultipartBody.Part createImagePart(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), byteArrayOutputStream.toByteArray());
        return MultipartBody.Part.createFormData("photo", "image.jpg", requestBody);
    }
}
