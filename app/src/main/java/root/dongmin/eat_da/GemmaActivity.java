package root.dongmin.eat_da;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.genai.llminference.LlmInference;

public class GemmaActivity extends AppCompatActivity {

    private static final String TAG = "GemmaActivity";
    //private static final String MODEL_PATH = "gemma-2b-it-cpu-int4.bin";


    private LlmInference llmInference;
    private TextView outputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemma);

        outputTextView = findViewById(R.id.output_text_view);
        initializeModel();
    }

    private void initializeModel() {
        try {
            LlmInference.LlmInferenceOptions options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath("gemma-2b-it-gpu-int4.bin")  // assets 폴더 내 모델 파일명
                    .setMaxTokens(512)
                    .setMaxTopK(40)
                    .build();

            llmInference = LlmInference.createFromOptions(this, options);

            askQuestion("안녕하세요! AI에 대해 설명해주세요.");
        } catch (Exception e) {
            Log.e(TAG, "모델 초기화 실패", e);
            outputTextView.setText("초기화 오류: " + e.getMessage());
        }
    }





    private void askQuestion(String prompt) {
        if (llmInference == null) {
            outputTextView.setText("모델이 로드되지 않았습니다.");
            return;
        }

        // 백그라운드 스레드에서 질문을 처리
        new Thread(() -> {
            try {
                String response = llmInference.generateResponse(prompt);
                runOnUiThread(() -> outputTextView.setText(response));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    outputTextView.setText("오류: " + e.getMessage());
                    Log.e(TAG, "질문 처리 실패", e);
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (llmInference != null) {
            llmInference.close();
        }
    }
}
