package root.dongmin.eat_da;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class NicknameActivity extends AppCompatActivity {


    private FirebaseAuth mFirebaseAuth; // Firebase 인증 객체
    private DatabaseReference mDatabaseRef; // Firebase 실시간 데이터베이스 객체
    private EditText mEtNickname; // 닉네임 입력 필드
    private Button mBtnSaveNickname; // 저장 버튼
    private ImageView back;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nickname);



        // Firebase 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // View 초기화
        mEtNickname = findViewById(R.id.et_nickname); // 닉네임 입력 필드 (XML에 정의된 ID)
        mBtnSaveNickname = findViewById(R.id.btn_save_nickname); // 저장 버튼 (XML에 정의된 ID)


        // 저장 버튼 클릭 리스너
        mBtnSaveNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNickname();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        back = findViewById(R.id.btnback2);
        back.setOnClickListener(v -> finish());
    }







    // 닉네임 저장 메서드
    private void saveNickname() {

        // 닉네임 가져오기
        String nickname = mEtNickname.getText().toString().trim();

        // 닉네임이 비어있다면 저장 불가
        if (nickname.isEmpty()) {
            Toast.makeText(NicknameActivity.this, "닉네임을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 현재 로그인된 사용자 정보 가져오기
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();

            // `UserAccount/{uid}/nickname` 경로에 닉네임 저장
            mDatabaseRef.child(uid).child("nickname").setValue(nickname).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(NicknameActivity.this, "닉네임이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    setTransactionCount(uid);
                    setDefaultProfileImage(uid);
                    // 온보딩 1로 이동
                    Intent intent = new Intent(NicknameActivity.this, Onboarding1.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(NicknameActivity.this, "닉네임 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(NicknameActivity.this, "로그인 상태를 확인하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    // 가입시 -> 거래횟수를 초기화
    private void setTransactionCount(String uid) {
        // 거래 횟수를 0으로 설정
        mDatabaseRef.child(uid).child("transactionCount").setValue(0).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 거래 횟수 초기화 성공
                Toast.makeText(NicknameActivity.this, "거래 횟수가 초기화되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 거래 횟수 초기화 실패
                Toast.makeText(NicknameActivity.this, "거래 횟수 초기화에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //가입시 -> 프로필에는 기본 이미지가 저장
    private void setDefaultProfileImage(String uid){
        // 기본이미지 경로를 파이어베이스에 자동 저장
        // 기본 프로필 이미지 URL
        String defaultProfileImageUrl = "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&s=200";  // 기본 이미지 URL

        // Firebase에 프로필 이미지 URL 저장
        mDatabaseRef.child(uid).child("profileImage").setValue(defaultProfileImageUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(NicknameActivity.this, "기본 프로필 이미지가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NicknameActivity.this, "프로필 이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

}