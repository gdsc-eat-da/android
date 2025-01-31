package root.dongmin.eat_da;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener {


    // ActivityResultLauncher 선언
    private ActivityResultLauncher<Intent> cameraLauncher;

    // 글쓰기 url (strings.xml 파일에 가면 주소 있음)
    final private String URL = getString(R.string.api_url);

    private Button btnCamera;
    private ImageView cameraView;
    private EditText eText,inText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);


        // 디자인 정의
        btnCamera = findViewById(R.id.btnPhoto);
        cameraView = findViewById(R.id.carmeraView);
        eText = findViewById(R.id.context);
        inText = findViewById(R.id.ingredient);



        btnCamera.setOnClickListener(this);

        // ActivityResultLauncher 초기화
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // 결과 처리
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        cameraView.setImageBitmap(imageBitmap);
                    }
                }
        );
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnPhoto) {
            // 카메라 기능을 Intent
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(cameraIntent);
            }
        }

    }
}

