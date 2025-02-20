package root.dongmin.eat_da;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
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

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener {

    // UI 요소
    private Button btnUpload;
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

    // 라디오버튼 요소
    private RadioGroup radioGroup;

    private RadioButton radioNeed, radioDistribute;

    //알레르기 리스트!!!!
    public List<String> selectedItems;


    public String selectedJoinedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        //빈칸 초기화
        selectedJoinedItems = "";

        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);

        // UI 요소 연결
        btnUpload = findViewById(R.id.photoupload);
        cameraView = findViewById(R.id.carmeraView);
        eText = findViewById(R.id.context);
        inText = findViewById(R.id.ingredient);

        // 버튼 클릭 리스너 등록
        cameraView.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

        // 라디오 버튼 클릭 리스너 설정
        radioGroup = findViewById(R.id.radioGroup);
        radioNeed = findViewById(R.id.foodNeed);
        radioDistribute = findViewById(R.id.foodDistribute);
        radioNeed.setButtonTintList(ColorStateList.valueOf(Color.BLACK));
        radioDistribute.setButtonTintList(ColorStateList.valueOf(Color.BLACK));


        // 알레르기 버튼 클릭 리스너 설정                                                 <알레르기!>
        Button alergicButton = findViewById(R.id.alergicButton);
        alergicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 알레르기 액티비티로 이동하면서 데이터 전달
                Intent intent = new Intent(PhotoActivity.this, alergicActivity.class);

                // 예시: "selectedItems"라는 값을 전달
                intent.putExtra("selectedItems", (Serializable) selectedItems);

                startActivityForResult(intent, 100); // 100은 요청 코드, 뒤에서 결과 받기 위해 사용
            }
        });


        // 기본 선택값을 foodDistribute로
        radioGroup.check(R.id.foodDistribute);

        // 음식필요해요 라디오 버튼클릭시
        radioNeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhotoActivity.this,NeedActivity.class);
                startActivity(intent);

            }
        });




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


    // 알레르기 액티비티에서 요소 받아오는 코드                                                                 <알레르기!>
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            // AlergicActivity에서 보낸 데이터를 받기
            if (data != null) {
                List<String> modifiedItems = (List<String>) data.getSerializableExtra("modifiedItems");
                if (modifiedItems != null) {
                    // 변경된 리스트를 처리
                    selectedItems = modifiedItems;
                    // 예: 새로운 리스트를 처리하는 코드 작성
                    Log.d("PhotoActivity", "Modified items: " + selectedItems);
                    selectedJoinedItems = TextUtils.join("_", selectedItems);
                }
            }
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.carmeraView) {
            openCamera();
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

        // ✅ selectedJoinedItems가 null이면 빈 문자열로 설정
        if (selectedJoinedItems == null) {
            selectedJoinedItems = "";
        }

        getNickname(nickname -> {
            if (nickname == null) {
                Toast.makeText(PhotoActivity.this, "닉네임을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("Upload", "닉네임 포함하여 업로드: " + nickname);

            // ✅ 이미지 Multipart 변환
            MultipartBody.Part filePart = createImagePart(imageBitmap);

            // ✅ 다른 데이터 RequestBody로 변환
            RequestBody contentsBody = RequestBody.create(MediaType.parse("text/plain"), contents);
            RequestBody ingredientsBody = RequestBody.create(MediaType.parse("text/plain"), ingredients);
            RequestBody nicknameBody = RequestBody.create(MediaType.parse("text/plain"), nickname);
            RequestBody selectedJoinedItemsBody = RequestBody.create(MediaType.parse("text/plain"), selectedJoinedItems); // ✅ 추가된 부분

            // ✅ API 호출 (닉네임 + selectedJoinedItems 포함)
            Call<ResponseBody> call = apiService.uploadPost(filePart, contentsBody, ingredientsBody, nicknameBody, selectedJoinedItemsBody);
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

                                // ✅ 위치 저장 실행
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
