package root.dongmin.eat_da;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GeminiActivity extends AppCompatActivity {

    private TextView resultText;

    private ImageView imageView; // 필드 선언




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini);

        resultText = findViewById(R.id.resultText);
        imageView = findViewById(R.id.imageView); // 연결

        Intent intent = getIntent();
        Uri imageUri = null;
        String ingredients = "";

        if (intent != null) {
            String uriStr = intent.getStringExtra("photoUri");
            if (uriStr != null) {
                imageUri = Uri.parse(uriStr);
            }
            ingredients = intent.getStringExtra("ingredients");
        }

        if (imageUri != null) {
            processImageAndCallGemini(imageUri, ingredients);
        } else {
            resultText.setText("이미지 URI가 전달되지 않았습니다.");
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
                .addText("그냥내용, 요약본으로 나눠줘 그리고 요약본은 @@ 사이에 끼워줘 그리고 앞으로 질문할 언어로 대답해줘: " + ingredients)
                .addImage(bitmap)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultStr = result.getText();
                runOnUiThread(() -> resultText.setText(resultStr));
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> resultText.setText("Gemini 응답 실패: " + t.getMessage()));
            }
        }, getMainExecutor());
    }

}
