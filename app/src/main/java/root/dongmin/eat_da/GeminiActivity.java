package root.dongmin.eat_da;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiActivity extends AppCompatActivity {

    private TextView resultText, sum;
    private TextView nick;
    private TextView detailTitle;
    private TextView whatisname;
    private ShapeableImageView profile;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;

    private ImageView imageView; // 필드 선언




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini);

        resultText = findViewById(R.id.textView47);
        profile = findViewById(R.id.profile);
        sum = findViewById(R.id.textView46);
        imageView = findViewById(R.id.detailImage); // 연결
        nick = findViewById(R.id.nick);
        detailTitle = findViewById(R.id.detailTitle);
        whatisname = findViewById(R.id.whatisname);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        Intent intent = getIntent();
        Uri imageUri = null;
        String ingredients = "";
        String nickname = intent.getStringExtra("nickname");
        String hashtag = intent.getStringExtra("hashtag");
        String contents = intent.getStringExtra("contents");

        if (intent != null) {
            String uriStr = intent.getStringExtra("photoUri");
            if (uriStr != null) {
                imageUri = Uri.parse(uriStr);
            }
            ingredients = intent.getStringExtra("ingredients");
        }

        loadImageProfile();
        nick.setText(nickname); // 작성자 닉네임
        whatisname.setText(hashtag); // 해시태그
        detailTitle.setText(contents); // 제목 (예: 요리 이름)


        if (imageUri != null) {
            processImageAndCallGemini(imageUri, ingredients);
        } else {
            resultText.setText("이미지 URI가 전달되지 않았습니다.");
        }
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
                            Glide.with(GeminiActivity.this)
                                    .load(profileImageUrl)  // Firebase에서 가져온 URL
                                    .into(profile);  // ImageView에 로드
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(GeminiActivity.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "로그인되지 않은 사용자입니다.", Toast.LENGTH_SHORT).show();
        }
    }
    private void processImageAndCallGemini(Uri imageUri, String ingredients) {
        try {
            Bitmap bitmap;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            }
            imageView.setImageBitmap(bitmap); // 이미지뷰에 표시

            // Bitmap을 그대로 넘김
            callGemini(bitmap, ingredients);

        } catch (IOException e) {
            e.printStackTrace();
            resultText.setText("이미지 처리 중 오류가 발생했습니다.");
        }
    }


    private void callGemini(Bitmap bitmap, String ingredients) {
        String apiKey = getString(R.string.GEMINI_KEY);

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText("Please divide the content into a summary and a detailed explanation. Put the summary between @@ symbols. For the detailed explanation, organize it into multiple steps and enclose each step's title between !! symbols. From now on, please respond in the same language as the question. Here is the question: " + ingredients)

                .addImage(bitmap)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultStr = result.getText().toString();

                // 요약 텍스트 찾기 (정규표현식으로 @@사이 텍스트 추출)
                Pattern summaryPattern = Pattern.compile("@@(.*?)@@");
                Matcher summaryMatcher = summaryPattern.matcher(resultStr);

                String summaryText;
                if (summaryMatcher.find()) {
                    summaryText = summaryMatcher.group(1).trim();
                } else {
                    summaryText = "";
                }

                // 본문 텍스트에서 @@요약@@ 제거
                String mainText = resultStr.replaceAll("@@(.*?)@@", "").trim();

                // !! 강조 텍스트 Bold 처리
                Pattern boldPattern = Pattern.compile("!!(.*?)!!");
                Matcher boldMatcher = boldPattern.matcher(mainText);

                // !! 제거된 텍스트 만들기
                String cleanedMainText = mainText.replaceAll("!!(.*?)!!", "$1");
                SpannableString spannable = new SpannableString(cleanedMainText);

                int offset = 0;
                while (boldMatcher.find()) {
                    String matchText = boldMatcher.group(1);
                    int start = cleanedMainText.indexOf(matchText, offset);
                    int end = start + matchText.length();
                    offset = end;

                    spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                runOnUiThread(() -> {
                    resultText.setText(spannable);
                    sum.setText(summaryText.isEmpty() ? "요약 없음" : summaryText);
                });
            }


            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> resultText.setText("Gemini 응답 실패: " + t.getMessage()));
            }
        }, getMainExecutor());
    }

}
