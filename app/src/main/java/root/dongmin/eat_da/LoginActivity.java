package root.dongmin.eat_da;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001; // 구글 로그인 요청 코드

    private FirebaseAuth mFirebaseAuth; // 파이어베이스 인증
    private DatabaseReference mdatabaseRef; // 실시간 데이터베이스

    private GoogleSignInClient mGoogleSignInClient; // 구글 로그인 클라이언트

    private EditText mEtEmail, mEtPwd;
    private ImageView mBtnLogin;
    private TextView mBtnRegister;
    private ImageView googleLoginbtn;
    private TextView btnFind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);



















        // 파이어베이스 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mdatabaseRef = FirebaseDatabase.getInstance().getReference();

        // 버튼 및 입력 필드 초기화
        mEtEmail = findViewById(R.id.et_email);
        mEtPwd = findViewById(R.id.et_pwd);
        mBtnRegister = findViewById(R.id.legistor);
        mBtnLogin = findViewById(R.id.login);
        googleLoginbtn = findViewById(R.id.btn_google_sign_in);
        btnFind = findViewById(R.id.findpasswd);

        // 이메일 로그인 버튼
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();

                mFirebaseAuth.signInWithEmailAndPassword(strEmail, strPwd)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // 로그인 성공
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // 로그인 실패
                                    Toast.makeText(LoginActivity.this, "정보를 확인해 주세요", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // 회원가입 버튼
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 시스템 바 여백 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Google Sign-In 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Firebase 콘솔에서 가져온 클라이언트 ID
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 구글 로그인 버튼
        googleLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        // 비밀번호 찾기 버튼
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,ForgotpwActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료 (필요한 경우)
            }
        });
    }




    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Toast.makeText(this, "구글 로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace(); // 로그캣에 오류 출력
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                String email = user.getEmail();

                                // UserAccount 경로에서 닉네임 조회
                                DatabaseReference userRef = mdatabaseRef.child("UserAccount").child(uid);
                                userRef.child("emailId").setValue(email);
                                userRef.child("idToken").setValue(uid);

                                userRef.child("nickname").get().addOnCompleteListener(nicknameTask -> {
                                    if (nicknameTask.isSuccessful() && nicknameTask.getResult().exists()) {
                                        // 닉네임이 존재하면 MainActivity로 이동
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        // 닉네임이 없으면 NicknameActivity로 이동
                                        Intent intent = new Intent(LoginActivity.this, NicknameActivity.class);
                                        startActivity(intent);
                                    }
                                    finish();
                                });
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "구글 로그인 인증 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
