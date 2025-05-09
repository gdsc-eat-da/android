package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotdoneActivity extends AppCompatActivity {

    //로그인 창으로 버튼
    private Button btntoLongin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgotpw_done);

        btntoLongin = findViewById(R.id.btn_reset_tologin);

        btntoLongin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotdoneActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(ForgotdoneActivity.this, "Click the button below to go to the login page.", Toast.LENGTH_SHORT).show();
            } //뒤로 가기 꼬임 방지
        });

    }
}