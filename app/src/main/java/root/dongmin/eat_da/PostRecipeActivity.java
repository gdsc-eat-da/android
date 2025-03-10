package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PostRecipeActivity extends AppCompatActivity {

    private ImageView btnback;

    // UI 요소
    private Button btnUpload;
    private ImageView cameraView,back;
    private EditText eText, inText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_recipe);

        btnback = findViewById(R.id.btnback7);

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostRecipeActivity.this, RecipeActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}