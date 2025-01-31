package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotpwActivity extends AppCompatActivity {

    private EditText mEtEmail;
    private Button mBtnResetPassword;
    private FirebaseAuth mFirebaseAuth;
    private TextView ack5,ack6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgotpw);

        // 레이아웃 요소 연결
        mEtEmail = findViewById(R.id.et_email);
        mBtnResetPassword = findViewById(R.id.btn_reset_password);

        // 입력 확인 알림 텍스트
        ack5 = findViewById(R.id.acktext5);
        ack6 = findViewById(R.id.acktext6);

        // 초기에는 전부 안 보이도록 설정
        ack5.setVisibility(View.GONE);
        ack6.setVisibility(View.GONE);



        // Firebase 인증 객체 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();

        // 비밀번호 재설정 버튼 클릭 리스너 설정
        mBtnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEtEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(ForgotpwActivity.this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();

                    return;
                }

                // Firebase를 통해 비밀번호 재설정 이메일 전송
                mFirebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotpwActivity.this, "비밀번호 재설정 이메일을 보냈습니다.", Toast.LENGTH_SHORT).show();
                            ack5.setVisibility(View.VISIBLE);
                            // ForgotdoneActivity로 이동
                            Intent intent = new Intent(ForgotpwActivity.this, ForgotdoneActivity.class);
                            startActivity(intent);
                            finish(); // 액티비티 종료
                        } else {
                            Toast.makeText(ForgotpwActivity.this, "이메일 전송 실패! 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                            ack6.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        // 화면 Insets 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}