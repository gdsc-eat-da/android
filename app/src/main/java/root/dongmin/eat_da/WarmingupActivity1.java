package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WarmingupActivity1 extends AppCompatActivity {

    private ImageButton jumpButton;
    private ImageButton checkAnswerButton;
    private ImageButton startEatDaButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_warmingup1);

        jumpButton = findViewById(R.id.toSkip2);

        jumpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(WarmingupActivity1.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}