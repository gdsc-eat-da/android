package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Onboarding1 extends AppCompatActivity {

    private Button goNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding1);

        goNext = findViewById(R.id.boarding1);

        goNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Onboarding1.this, Onboarding2.class);
                startActivity(intent);

                // 화면 전환 애니메이션 적용
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                finish();

            }});

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(Onboarding1.this, "\n" + "This is a page that requires membership registration.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}