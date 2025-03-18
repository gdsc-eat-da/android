package root.dongmin.eat_da;

import android.Manifest;
import android.app.Dialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.adapter.AllergyAdapter;
import root.dongmin.eat_da.adapter.HashtagAdapter;
import root.dongmin.eat_da.adapter.PlusHashtagAdapter;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.RetrofitClient;

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener {

    // UI 요소
    private Button btnUpload;
    private ImageView cameraView,back;
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

    //문고리
    private RadioGroup radioFaceGroup;
    private RadioButton radioFace, radioNoFace;

    // face 값 저장할 변수 (0: face 선택, 1: noface 선택)
    private int isFaceSelected = 0; // 기본값은 face(0)


    public List<String> alergicList = new ArrayList<>(Arrays.asList("유제품", "땅콩", "복숭아" ,"밀", "쇠고기", "새우"));
    public List<String> finalAlergicList = new ArrayList<>();

    public List<String> hashList = new ArrayList<>(Arrays.asList("조리", "비조리", "인스턴트" ,"밀키트", "넉넉한유통기한"));
    public List<String> finalHashList = new ArrayList<>();
    public List<String> UpHashList = new ArrayList<>();
    private RecyclerView allergyRecyclerView, plusHashtagRecyclerView, hashtagRecyclerView;
    private AllergyAdapter allergyAdapter;
    private PlusHashtagAdapter plusHashtagAdapter;
    private HashtagAdapter HashtagAdapter;


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
        allergyRecyclerView = findViewById(R.id.allergyRecyclerView);//알레르기 리사이클러뷰
        plusHashtagRecyclerView = findViewById(R.id.plusHashtagRecyclerView);//해시태그 위에 있는 리사이클러뷰
        hashtagRecyclerView = findViewById(R.id.hashtagRecyclerView);//해시태그 밑에 있는 리사이클러뷰
        allergyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));//수직 드래그?
        plusHashtagRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hashtagRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        back = findViewById(R.id.btnback5);
        back.setOnClickListener(v -> finish());

        // 버튼 클릭 리스너 등록
        cameraView.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

        //어댑터 설정
        allergyAdapter = new AllergyAdapter(alergicList, finalAlergicList);
        allergyRecyclerView.setAdapter(allergyAdapter);
        HashtagAdapter = new HashtagAdapter(hashList, finalHashList);
        hashtagRecyclerView.setAdapter(HashtagAdapter);
        plusHashtagAdapter = new PlusHashtagAdapter(UpHashList, this, HashtagAdapter);
        plusHashtagRecyclerView.setAdapter(plusHashtagAdapter);



        plusHashtagRecyclerView.setOnClickListener(v -> showCustomDialog());



        // 라디오 버튼 클릭 리스너 설정
        radioGroup = findViewById(R.id.radioGroup);
        radioNeed = findViewById(R.id.foodNeed);
        radioDistribute = findViewById(R.id.foodDistribute);
        radioNeed.setButtonTintList(ColorStateList.valueOf(Color.BLACK));
        radioDistribute.setButtonTintList(ColorStateList.valueOf(Color.BLACK));

        radioFaceGroup = findViewById(R.id.radioFace);
        radioFace = findViewById(R.id.face);
        radioNoFace = findViewById(R.id.noface);

        //라디오 이미지 토글
        radioFaceGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.face) {
                    radioFace.setBackgroundResource(R.drawable.checkedface);
                    radioNoFace.setBackgroundResource(R.drawable.noface);
                    isFaceSelected = 0; // ✅ face 선택 시 0
                } else if (checkedId == R.id.noface) {
                    radioFace.setBackgroundResource(R.drawable.face);
                    radioNoFace.setBackgroundResource(R.drawable.checkednoface);
                    isFaceSelected = 1; // ✅ noface 선택 시 1
                }
            }
        });


        //클릭했을때 이미지 변하도록 하고 , 서버랑 API 부분 수정해야함





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

        if (requestCode == 101 && resultCode == RESULT_OK) {
            // AlergicActivity에서 보낸 데이터를 받기
            if (data != null) {
                List<String> modifiedItems = (List<String>) data.getSerializableExtra("modifiedItems");
                if (modifiedItems != null) {
                    // 변경된 리스트를 처리
                    selectedItems = modifiedItems; // selectedItems 업데이트
                    Log.d("PhotoActivity", "Modified items: " + selectedItems); // 로그 출력
                    selectedJoinedItems = TextUtils.join("_", selectedItems); // 리스트를 String으로 변환

                    allergyAdapter.updateAllergyList(modifiedItems);

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



        // finalAlergicList와 finalHashList를 문자열로 변환
        String finalAlergicListString = TextUtils.join("_", finalAlergicList);
        String finalHashListString = TextUtils.join("_", finalHashList);
        selectedJoinedItems = finalAlergicListString;

        // ✅ selectedJoinedItems가 null이면 빈 문자열로 설정
        if (selectedJoinedItems == null) {
            selectedJoinedItems = "";
        }
        Log.d("Gallery", "알레르기는 두둥두둥: " + finalAlergicListString);
        Log.d("Gallery", "해시태그는 두둥두둥: " + finalHashListString);


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
            RequestBody selectedJoinedItemsBody = RequestBody.create(MediaType.parse("text/plain"), selectedJoinedItems);
            //RequestBody finalAlergicListBody = RequestBody.create(MediaType.parse("text/plain"), finalAlergicListString); // finalAlergicList 추가
            RequestBody finalHashListBody = RequestBody.create(MediaType.parse("text/plain"), finalHashListString); // finalHashList 추가
            RequestBody faceBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(isFaceSelected)); // ✅ 추가된 부분

            // ✅ API 호출 (닉네임 + selectedJoinedItems + finalAlergicList + finalHashList 포함)
            Call<ResponseBody> call = apiService.uploadPost(
                    filePart,
                    contentsBody,
                    ingredientsBody,
                    nicknameBody,
                    selectedJoinedItemsBody, // finalHashList 추가
                    faceBody,
                    finalHashListBody
            );

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




    private void showCustomDialog() {
        Toast.makeText(getApplicationContext(), "다이얼로그 시작.", Toast.LENGTH_SHORT).show();
        // 1. 다이얼로그 생성
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_item);

        // 2. 다이얼로그 내부 UI 요소 가져오기
        EditText editTextInput = dialog.findViewById(R.id.editTextInput);
        ImageView btnAdd = dialog.findViewById(R.id.btnAdd);

        // 3. 추가 버튼 클릭 시 아이템 리스트에 추가
        btnAdd.setOnClickListener(v -> {
            String newItem = editTextInput.getText().toString().trim();
            if (!newItem.isEmpty() && !UpHashList.contains(newItem)) {
                // UpHashList에 새로운 아이템 추가
                UpHashList.add(newItem);
                // 어댑터에 데이터 변경 알림
                plusHashtagAdapter.notifyDataSetChanged();
                // 다이얼로그 닫기
                dialog.dismiss();
            } else {
                Toast.makeText(getApplicationContext(), "올바른 값을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. 다이얼로그 표시
        dialog.show();
    }



}



//추천 해시테그(위에거) 기본값은 #글자.
//리사이클러뷰 누르면 팝업띄우기 그리고 그 팝업 작성하면 위에 리사이클에 뜸
//터치 상관없이 리사이클러뷰 누르기만 하면 팝업 띄우고 그 결과물을 리사이클에 띄우게 하기
//아래 리사이클에는 리스트로 예시 5개 추가시키고 추가로 위에 리사이클 추가할때마다 아래 리스트에도 추가하게 하기( 해시리스트, 파이널해시리스트 만들기 그리고
//업 해시리스트 만들기. 결론적으로 업 해시리스트는 위에서 추가해주는 리스트. 밑에 추가될때마다 그 요소를 해시리스트(오리지널)에 넣음과 동시에 어댑터 1번더 실
//행시켜주는 코드 ㄱㄱ 그리고 동시에 선택되었으니까 파이널해시리스트에도 넣어줘야함