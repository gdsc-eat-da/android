package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import root.dongmin.eat_da.data.User;

public class RegisterActivity extends AppCompatActivity {



//https://www.youtube.com/watch?v=NJgolOfKcYE 참고



    //https://console.firebase.google.com/project/eat-da-68342/authentication/users?hl=ko
    //가보면 회원가입 완료 시 가입한 아이디,비번,UID 코드가 뜰거임

    //파이어베이스와 연동하기 위한 코드
    private FirebaseAuth mFirebaseAuth;//파이어베이스 인증
    private DatabaseReference mdatabaseRef;//실시간 데이터 베이스

    //-------------------------------------------------------------------------

    private EditText mEtEmail, mEtPwd;
    private Button mBtnRegister;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);


        //파이어베이스 래퍼런스 선언
        mFirebaseAuth = FirebaseAuth.getInstance();
        mdatabaseRef = FirebaseDatabase.getInstance().getReference();

        //버튼 이름 정리
        mEtEmail = findViewById(R.id.et_re_e);
        mEtPwd = findViewById(R.id.et_re_p);
        mBtnRegister = findViewById(R.id.et_re_gogo);




        mBtnRegister.setOnClickListener(new View.OnClickListener() { //ㄱㄱ버튼 눌렀을 때
            @Override
            public void onClick(View v) {

                //회원가입 처리 시작
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();



                //firebase Auth 시작
                mFirebaseAuth.createUserWithEmailAndPassword(strEmail,strPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {// 회원가입 성공 유무 클래스의 함수

                        if(task.isSuccessful()) //성공시
                        {
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            UserAccount account = new UserAccount();
                            account.setIdToken(firebaseUser.getUid());
                            account.setEmailId(firebaseUser.getEmail());
                            account.setPassword(strPwd);

                            // 실시간 데이터베이스에 저장
                            String userId = mFirebaseAuth.getCurrentUser().getUid();
                            addUserToDatabase(strEmail,strPwd, userId);

                            //mdatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);
                            Toast.makeText(RegisterActivity.this, "화원가입 완료",Toast.LENGTH_SHORT).show();


                            //닉네임 액티비티로 이동 ㄱㄱ
                            Intent intent = new Intent(RegisterActivity.this, NicknameActivity.class);
                            startActivity(intent);
                            finish(); // 현재 액티비티 종료
                        }
                        else { //실패시
                            Toast.makeText(RegisterActivity.this, "아이디나 비밀번호가 적절한지 확인하세요",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            private void addUserToDatabase(String name, String email, String uId) {
                mdatabaseRef.child("user").child(uId).setValue(new User(name,email,uId));
            }
        });







        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}