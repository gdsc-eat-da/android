package root.dongmin.eat_da;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.imageview.ShapeableImageView;
import android.content.Intent;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;

public class ReDeActivity extends AppCompatActivity {

    // 뷰 변수 선언
    private TextView detailTitle;
    private ShapeableImageView profile;
    private TextView nick;
    private TextView detailIngredients;
    private ImageView detailImage;
    private TextView whatisname;
    private TextView heart;

    private TextView resultText;
    private EditText inputText;
    private TextView sendButton;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;

    // 레시피 정보 저장 변수
    private String recipeContents;
    private String recipeIngredients;
    private String recipePhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_re_de);

        // 뷰 초기화
        detailTitle = findViewById(R.id.detailTitle);
        profile = findViewById(R.id.profile);
        nick = findViewById(R.id.nick);
        detailIngredients = findViewById(R.id.detailIngredients);
        detailImage = findViewById(R.id.detailImage);
        whatisname = findViewById(R.id.whatisname);
        resultText = findViewById(R.id.textView46);
        inputText = findViewById(R.id.searchPost);
        sendButton = findViewById(R.id.confirm);

        // 버튼 클릭 시 modelCall 실행
        sendButton.setOnClickListener(view -> {
            String prompt = inputText.getText().toString();
            modelCall(prompt);
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // Intent에서 데이터 받아오기
        Intent intent = getIntent();
        if (intent != null) {
            String recipeID = intent.getStringExtra("recipeID");
            recipeContents = intent.getStringExtra("contents");
            recipeIngredients = intent.getStringExtra("ingredients");
            //recipePhotoUrl = intent.getStringExtra("photoUri");
//            if (recipePhotoUrl != null) {
//                try {
//                    Uri photoUri = Uri.parse(recipePhotoUrl);
//                    InputStream inputStream = getContentResolver().openInputStream(photoUri);
//                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                    inputStream.close();
//
//                    // 이 비트맵을 Gemini에 넘기거나 이미지뷰에 설정 가능
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
//                }
//            }
            String nickname = intent.getStringExtra("nickname");
            int suggestion = intent.getIntExtra("suggestion", 0);
            String hashtag = intent.getStringExtra("hashtag");
            int isRecipe = intent.getIntExtra("isrecipe", 0);

            // 뷰에 데이터 설정
            detailTitle.setText(recipeContents);
            nick.setText(nickname);
            detailIngredients.setText(recipeIngredients);
            whatisname.setText(hashtag);
            heart.setText(String.valueOf(suggestion));

            // 이미지 로드
//            Glide.with(this)
//                    .load(recipePhotoUrl)
//                    .into(detailImage);

            // 레시피 정보를 바탕으로 Gemini에게 자동 질문
            //askGeminiAboutRecipe();
        }

        loadImageProfile();

        // 시스템 바 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 레시피 정보를 바탕으로 Gemini에게 자동으로 질문하는 메서드
    private void askGeminiAboutRecipe() {
        String prompt = "요청 사항:\n" +
                "제목 : " + recipeContents + "\n" +
                "내용: " + recipeIngredients + "\n" +
                //"이미지 URL: " + recipePhotoUrl + "\n\n" +
                "여기서 내용 언어와 같은 언어로 대답을 해주세요. " +
                "일반 내용도 적어주고 요약본도 적어주세요 요약은 되도록 짧게 해주시고 요약본은 @@로 감싸주세요.";

        modelCall(prompt);
    }

    // 사용자의 입력을 받아 Gemini 모델에 전송하는 함수
    public void modelCall(String userPrompt) {
        String apiKey = getString(R.string.GEMINI_KEY);
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText(userPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        // 비동기 처리 콜백
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultStr = result.getText();
                runOnUiThread(() -> resultText.setText(resultStr));
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> resultText.setText("오류 발생: " + t.getMessage()));
            }
        }, this.getMainExecutor());
    }

    private void loadImageProfile() {
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();

            // 사용자 정보 가져오기 (프로필 이미지)
            mDatabaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String profileImageUrl = dataSnapshot.child("profileImage").getValue(String.class);

                        // 프로필 이미지 설정
                        if (profileImageUrl != null) {
                            Glide.with(ReDeActivity.this)
                                    .load(profileImageUrl)
                                    .into(profile);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ReDeActivity.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "로그인되지 않은 사용자입니다.", Toast.LENGTH_SHORT).show();
        }
    }
}