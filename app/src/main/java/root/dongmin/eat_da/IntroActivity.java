package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);

        // 3초 후에 LoginActivity로 이동
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // LoginActivity로 이동
                Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(intent);
                // 현재 IntroActivity 종료

                overridePendingTransition(0, 0); // 전환 애니메이션 제거

                finish();
            }
        }, 3000); // 3000ms = 3초
    }
}
