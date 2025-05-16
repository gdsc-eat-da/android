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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ai.client.generativeai.java.ChatFutures;
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

// (기존 import 유지)
import com.google.ai.client.generativeai.java.ChatFutures;

public class GeminiActivity extends AppCompatActivity {

    // 기존 필드 그대로
    private TextView resultText, sum;
    private TextView nick, detailTitle, whatisname, detailIngredients, confirm;
    private ShapeableImageView profile;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private EditText searchPost;
    private ImageView imageView;

    // ✅ Chat 객체 전역으로 선언
    private ChatFutures chat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini);

        // UI 초기화
        confirm = findViewById(R.id.confirm);
        resultText = findViewById(R.id.textView47);
        searchPost = findViewById(R.id.searchPost);
        profile = findViewById(R.id.profile);
        sum = findViewById(R.id.textView46);
        imageView = findViewById(R.id.detailImage);
        nick = findViewById(R.id.nick);
        detailTitle = findViewById(R.id.detailTitle);
        detailIngredients = findViewById(R.id.detailIngredients);
        whatisname = findViewById(R.id.whatisname);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // ✅ Gemini 초기화 및 ChatFutures 생성
        String apiKey = getString(R.string.GEMINI_KEY);
        GenerativeModel gm = new GenerativeModel("gemini-2.0-flash", apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        chat = model.startChat();

        // 인텐트 처리
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
        nick.setText(nickname);
        whatisname.setText(hashtag);
        detailTitle.setText(contents);
        detailIngredients.setText(ingredients);

        if (imageUri != null) {
            processImageAndCallGemini(imageUri, ingredients);
        } else {
            resultText.setText("이미지 URI가 전달되지 않았습니다.");
        }

        confirm.setOnClickListener(v -> {
            String userQuestion = searchPost.getText().toString().trim();

            if (userQuestion.isEmpty()) {
                Toast.makeText(this, "ask a question.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 이미지 없이 질문만 보냄
            callGeminiWithTextOnly(userQuestion);
            searchPost.setText("");
        });
    }

    private void loadImageProfile() {
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            mDatabaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String profileImageUrl = dataSnapshot.child("profileImage").getValue(String.class);
                        if (profileImageUrl != null) {
                            Glide.with(GeminiActivity.this)
                                    .load(profileImageUrl)
                                    .into(profile);
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
            imageView.setImageBitmap(bitmap);

            // 이미지와 함께 초기 질문
            callGeminiWithImage(bitmap, ingredients);

        } catch (IOException e) {
            e.printStackTrace();
            resultText.setText("이미지 처리 중 오류가 발생했습니다.");
        }
    }

    // ✅ 이미지와 텍스트 함께 사용 (처음 질문)
    private void callGeminiWithImage(Bitmap bitmap, String ingredients) {
        Content content = new Content.Builder()
                .addText("Please split the response into a summary and the full content. The summary should appear right at the beginning, enclosed between @@ symbols. The full content should be broken down step by step, with light summarization. Each step should be separated by section titles, and the section titles should be wrapped with !!. From now on, please respond in the language that will follow(english recommend) : " + ingredients)
                .addImage(bitmap)
                .build();

        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(content);

        handleResponse(response);
    }

    // ✅ 이후 텍스트 질문만 보낼 때
    private void callGeminiWithTextOnly(String question) {
        Content content = new Content.Builder()
                .addText(question)
                .build();

        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(content);

        handleResponse(response);
    }

    // ✅ 공통 응답 처리 함수
    private void handleResponse(ListenableFuture<GenerateContentResponse> response) {
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultStr = result.getText().toString();

                Pattern summaryPattern = Pattern.compile("@@(.*?)@@");
                Matcher summaryMatcher = summaryPattern.matcher(resultStr);

                String summaryText = summaryMatcher.find() ? summaryMatcher.group(1).trim() : "";
                String mainText = resultStr.replaceAll("@@(.*?)@@", "").trim();

                Pattern boldPattern = Pattern.compile("!!(.*?)!!");
                Matcher boldMatcher = boldPattern.matcher(mainText);

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
                    sum.setText(summaryText.isEmpty() ? "no summary" : summaryText);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> resultText.setText("Gemini failed: " + t.getMessage()));
            }
        }, getMainExecutor());
    }
}
