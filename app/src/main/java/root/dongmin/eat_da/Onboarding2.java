package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Onboarding2 extends AppCompatActivity {

    private Button goNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding2);

        goNext = findViewById(R.id.boarding2);

        goNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Onboarding2.this, Onboarding3.class);
                startActivity(intent);

                // 화면 전환 애니메이션 적용
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                finish();

            }});


    }
}