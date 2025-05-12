package root.dongmin.eat_da;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class GeminiActivity extends AppCompatActivity {

    // Gemini API 키
    //private static final String API_KEY = "";

    private EditText inputText;
    private TextView resultText;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inputText = findViewById(R.id.inputText);
        resultText = findViewById(R.id.resultText);
        sendButton = findViewById(R.id.sendButton);

        // 버튼 클릭 시 modelCall 실행
        sendButton.setOnClickListener(view -> {
            String prompt = inputText.getText().toString();
            modelCall(prompt);
        });
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
}





//public void modelCall()
//{
//    // The Gemini 1.5 models are versatile and work with both text-only and multimodal prompts
//    GenerativeModel gm = new GenerativeModel("gemini-1.5-flash","AIzaSyBA8v6TilPcv2_qzLEUdZcZabIG7JAX8Iw"); //하드코딩 해줄거임 (api)
//    GenerativeModelFutures model = GenerativeModelFutures.from(gm);
//
//    Content content = new Content.Builder()
//            .addText("Write a story about a magic backpack.")
//            .build();
//
//
//    ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
//    Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
//        @Override
//        public void onSuccess(GenerateContentResponse result) {
//            String resultText = result.getText();
//            System.out.println(resultText);
//        }
//
//        @Override
//        public void onFailure(Throwable t) {
//            t.printStackTrace();
//        }
//    }, this.getMainExecutor());
//}
//
//


