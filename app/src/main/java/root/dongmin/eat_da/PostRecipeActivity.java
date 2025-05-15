package root.dongmin.eat_da;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.adapter.HashtagAdapter;
import root.dongmin.eat_da.adapter.PlusHashtagAdapter;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.RetrofitClient;

public class PostRecipeActivity extends AppCompatActivity implements View.OnClickListener{

    // UI 요소
    private Button btnUpload ,askGemma, askPerson;
    private ImageView cameraView,back;
    private EditText eText, inText;
    private PlusHashtagAdapter plusHashtagAdapter;
    private root.dongmin.eat_da.adapter.HashtagAdapter HashtagAdapter;
    private RecyclerView hashtagRecyclerView, plusHashtagRecyclerView;
    public List<String> hashList = new ArrayList<>(Arrays.asList("Popular", "Recycling", "Q&A" ,"Frugal Knowledge"));
    public List<String> finalHashList = new ArrayList<>();
    public List<String> UpHashList = new ArrayList<>();
    private TextView free, recipe;
    private int isrecipe = 0; // 0 또는 1로 설정
    private int isGemini = 0;









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
        askGemma = findViewById(R.id.askGemma);
        askPerson = findViewById(R.id.askPerson);
        cameraView = findViewById(R.id.recipecarmeraView);
        eText = findViewById(R.id.recipecontext);
        inText = findViewById(R.id.recipeingredient);
        plusHashtagRecyclerView = findViewById(R.id.plusHashtagRecyclerView);//해시태그 위에 있는 리사이클러뷰
        hashtagRecyclerView = findViewById(R.id.hashtagRecyclerView);//해시태그 밑에 있는 리사이클러뷰
        plusHashtagRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hashtagRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // 뷰 초기화


        free = findViewById(R.id.textView30);
        recipe = findViewById(R.id.textView31);
        updateTextColor(isrecipe);
        free.setOnClickListener(v -> {
            isrecipe = 0; // freeorrecipe 값을 0으로 설정

            //updateTextColor(isrecipe); // 텍스트 색상 업데이트
            free.setTextColor(Color.parseColor("#000000")); // textView30: 검은색
            recipe.setTextColor(Color.parseColor("#999AA3")); // textView31: 회색
        });

        recipe.setOnClickListener(v -> {
            isrecipe = 1; // freeorrecipe 값을 1로 설정
            //updateTextColor(isrecipe); // 텍스트 색상 업데이트
            free.setTextColor(Color.parseColor("#999AA3")); // textView30: 회색
            recipe.setTextColor(Color.parseColor("#000000")); // textView31: 검은색
        });


        //어댑터 설정
        HashtagAdapter = new HashtagAdapter(hashList, finalHashList);
        hashtagRecyclerView.setAdapter(HashtagAdapter);
        plusHashtagAdapter = new PlusHashtagAdapter(UpHashList, this, HashtagAdapter);
        plusHashtagRecyclerView.setAdapter(plusHashtagAdapter);
        plusHashtagRecyclerView.setOnClickListener(v -> showCustomDialog());


        // 뒤로가기 이벤트 처리
        back = findViewById(R.id.btnback7);
        back.setOnClickListener(v -> finish());

        // 버튼 클릭 리스너 등록
        cameraView.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        askPerson.setOnClickListener(this);


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
            isGemini = 1;
            recipePost();
        } else if (view.getId() == R.id.askPerson) {
            isGemini = 0;
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

        // isrecipe가 0인 경우 (일반 게시물)
        if (isrecipe == 0 && isGemini == 1) {
            getNickname(nickname -> {
                if (nickname == null) {
                    Toast.makeText(PostRecipeActivity.this, "Failed to load the nickname.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ReDeActivity로 이동하면서 데이터 전달
                Intent intent = new Intent(PostRecipeActivity.this, GeminiActivity.class);

                // 이미지 비트맵을 파일로 저장 후 URI로 전달
                if (imageBitmap != null) {
                    try {
                        // 캐시 디렉토리에 이미지 파일 저장
                        File imageFile = new File(getCacheDir(), "shared_image.jpg");
                        FileOutputStream fos = new FileOutputStream(imageFile);
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        fos.flush();
                        fos.close();

                        // FileProvider를 사용해 안전한 URI 생성
                        Uri imageUri = FileProvider.getUriForFile(
                                PostRecipeActivity.this,
                                getPackageName() + ".fileprovider",
                                imageFile
                        );

                        intent.putExtra("photoUri", imageUri.toString());
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 권한 부여

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(PostRecipeActivity.this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // 나머지 데이터 전달
                intent.putExtra("contents", contents);
                intent.putExtra("ingredients", ingredients);
                intent.putExtra("nickname", nickname);
                intent.putExtra("suggestion", 0); // 기본값 0
                intent.putExtra("hashtag", getFormattedHashtags());
                intent.putExtra("isrecipe", isrecipe);
                intent.putExtra("recipeID", "local_post_" + System.currentTimeMillis());

                startActivity(intent);
                finish();
            });
            return;
        }






        getNickname(nickname -> {
            if (nickname == null) {
                Toast.makeText(PostRecipeActivity.this, "Failed to load the nickname.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("Upload", "닉네임 포함하여 업로드: " + nickname);

            // ✅ finalHashList를 정렬하고 _로 구분된 문자열로 변환
            String hashtags = getFormattedHashtags();

            // ✅ 이미지 Multipart 변환
            MultipartBody.Part filePart = createImagePart(imageBitmap);

            // ✅ 다른 데이터 RequestBody로 변환
            RequestBody contentsBody = RequestBody.create(MediaType.parse("text/plain"), contents);
            RequestBody ingredientsBody = RequestBody.create(MediaType.parse("text/plain"), ingredients);
            RequestBody nicknameBody = RequestBody.create(MediaType.parse("text/plain"), nickname);
            RequestBody hashtagsBody = RequestBody.create(MediaType.parse("text/plain"), hashtags); // hashtag 추가
            RequestBody isrecipeBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(isrecipe)); // isrecipe 추가


            // ✅ API 호출
            Call<ResponseBody> call = apiService.uploadRecipe(filePart, contentsBody, ingredientsBody, nicknameBody, hashtagsBody, isrecipeBody);
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
                                Toast.makeText(PostRecipeActivity.this,"The recipe has been uploaded!" , Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(PostRecipeActivity.this, RecipeActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                finish();

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











    private void showCustomDialog() {
        Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Please enter the correct value.", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. 다이얼로그 표시
        dialog.show();
    }


    // freeorrecipe 값에 따라 텍스트 색상 업데이트
    private void updateTextColor(int freeorrecipe) {
        if (freeorrecipe == 0) {
            // freeorrecipe가 0인 경우
            free.setTextColor(Color.parseColor("#000000")); // textView30: 검은색
            recipe.setTextColor(Color.parseColor("#999AA3")); // textView31: 회색
        } else {
            // freeorrecipe가 1인 경우
            free.setTextColor(Color.parseColor("#999AA3")); // textView30: 회색
            recipe.setTextColor(Color.parseColor("#000000")); // textView31: 검은색
        }
    }



    // finalHashList를 정렬하고 _로 구분된 문자열로 변환
    private String getFormattedHashtags() {
        Collections.sort(finalHashList); // 정렬
        return TextUtils.join("_", finalHashList); // _로 구분된 문자열로 변환
    }







































}