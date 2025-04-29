package root.dongmin.eat_da;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import root.dongmin.eat_da.data.User;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;  // Firebase 인증
    private DatabaseReference mdatabaseRef;  // Firebase DB

    private EditText mEtEmail, mEtPwd, checkPwd;
    private MaterialButton mBtnRegister;  // MaterialButton 사용
    private TextView ack1, ack2, ack3, ack4;
    private ImageView mBtnTogglePwd1, mBtnTogglePwd2 ,back;

    private boolean isPasswordVisible = false; // 비밀번호 표시 상태 저장 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Firebase 레퍼런스 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mdatabaseRef = FirebaseDatabase.getInstance().getReference();

        // UI 요소 초기화
        mEtEmail = findViewById(R.id.et_re_e);
        mEtPwd = findViewById(R.id.et_re_p);
        checkPwd = findViewById(R.id.check_et_re_p);
        mBtnRegister = findViewById(R.id.et_re_gogo);

        back = findViewById(R.id.btnback1);
        back.setOnClickListener(v -> finish());

        // 입력 확인 알림 텍스트
        ack1 = findViewById(R.id.acktext1);
        ack2 = findViewById(R.id.acktext2);
        ack3 = findViewById(R.id.acktext3);
        ack4 = findViewById(R.id.acktext4);

        // 초기에는 전부 안 보이도록 설정
        ack1.setVisibility(View.GONE);
        ack2.setVisibility(View.GONE);
        ack3.setVisibility(View.GONE);
        ack4.setVisibility(View.GONE);

        // 초기 버튼 비활성화 및 배경색 변경
        setRegisterButtonState(false);

        // 비밀번호 표시/숨김 버튼
        mBtnTogglePwd1 = findViewById(R.id.et_re_visible);
        mBtnTogglePwd2 = findViewById(R.id.et_re_visible2);

        // 비밀번호 기본적으로 숨김 상태로 설정
        mEtPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // 비밀번호 표시/숨김 토글 리스너 등록
        View.OnClickListener togglePasswordListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        };

        // 두 개의 버튼에 같은 리스너 적용
        mBtnTogglePwd1.setOnClickListener(togglePasswordListener);
        mBtnTogglePwd2.setOnClickListener(togglePasswordListener);

        // 비밀번호 입력 감지 리스너
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPasswordMatch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        mEtPwd.addTextChangedListener(passwordWatcher);
        checkPwd.addTextChangedListener(passwordWatcher);

        // 회원가입 버튼 클릭 리스너
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strEmail = mEtEmail.getText().toString().trim();
                String strPwd = mEtPwd.getText().toString().trim();

                if (strEmail.isEmpty() || strPwd.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "이메일과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firebase 회원가입 진행
                mFirebaseAuth.createUserWithEmailAndPassword(strEmail, strPwd)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                                    String userId = firebaseUser.getUid();

                                    // 사용자 데이터베이스에 저장
                                    addUserToDatabase(strEmail, strPwd, userId);

                                    Toast.makeText(RegisterActivity.this, "회원가입 완료", Toast.LENGTH_SHORT).show();

                                    // 닉네임 입력 화면으로 이동
                                    Intent intent = new Intent(RegisterActivity.this, NicknameActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "아이디나 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // 시스템 바 여백 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * 비밀번호 표시/숨김을 토글하는 메서드
     */
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // 비밀번호 숨기기
            mEtPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            checkPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mBtnTogglePwd1.setImageResource(R.drawable.eye);
            mBtnTogglePwd2.setImageResource(R.drawable.eye);
        } else {
            // 비밀번호 보이기
            mEtPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            checkPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            mBtnTogglePwd1.setImageResource(R.drawable.eyeoff);
            mBtnTogglePwd2.setImageResource(R.drawable.eyeoff);
        }

        isPasswordVisible = !isPasswordVisible;

        // 커서 위치 유지
        mEtPwd.setSelection(mEtPwd.getText().length());
    }

    /**
     * 비밀번호가 일치하는지 확인하고 버튼 상태 변경
     */
    private void checkPasswordMatch() {
        String password = mEtPwd.getText().toString().trim();
        String confirmPassword = checkPwd.getText().toString().trim();

        boolean isMatch = !password.isEmpty() && password.equals(confirmPassword);
        setRegisterButtonState(isMatch);

        boolean isValidLength = password.length() >= 6;

        if (isMatch && isValidLength) {
            // 둘 다 만족
            ack1.setVisibility(View.VISIBLE);
            ack2.setVisibility(View.VISIBLE);
            ack3.setVisibility(View.GONE);
            ack4.setVisibility(View.GONE);
        } else if (isMatch && !isValidLength) {
            // 길이만 틀림
            ack1.setVisibility(View.GONE);
            ack2.setVisibility(View.GONE);
            ack3.setVisibility(View.VISIBLE);
            ack4.setVisibility(View.VISIBLE);
        } else if (!isMatch && isValidLength) {
            // 패턴만 틀림
            ack1.setVisibility(View.VISIBLE);
            ack2.setVisibility(View.GONE);
            ack3.setVisibility(View.GONE);
            ack4.setVisibility(View.VISIBLE);
        } else {
            // 둘 다 틀림
            ack1.setVisibility(View.GONE);
            ack2.setVisibility(View.GONE);
            ack3.setVisibility(View.VISIBLE);
            ack4.setVisibility(View.VISIBLE);
        }

    }

    private void setRegisterButtonState(boolean isEnabled) {
        mBtnRegister.setEnabled(isEnabled);
        mBtnRegister.setBackgroundTintList(ColorStateList.valueOf(isEnabled ? Color.BLACK : Color.GRAY));
        mBtnRegister.setTextColor(Color.WHITE);
    }

    private void addUserToDatabase(String email, String password, String uId) {
        mdatabaseRef.child("user").child(uId).setValue(new User(email, password, uId));
    }
}
