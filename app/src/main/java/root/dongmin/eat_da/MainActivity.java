package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private TextView greed; // 사용자에게 보여줄 텍스트뷰 (반갑습니다 [닉네임]님!)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // 텍스트뷰 초기화
        greed = findViewById(R.id.greeding);

        // 현재 로그인한 사용자 정보 가져오기
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            // Firebase에서 사용자 닉네임 가져오기
            String userId = firebaseUser.getUid();

            // 데이터베이스에서 해당 사용자 닉네임을 가져오기
            mDatabaseRef.child(userId).child("nickname").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // 닉네임 가져오기
                            String nickname = dataSnapshot.getValue(String.class);
                            if (nickname != null) {
                                // 닉네임을 "반갑습니다, [닉네임]님!" 형태로 표시
                                greed.setText("반갑습니다, " + nickname + "님!");
                            } else {
                                // 닉네임이 없다면 기본 텍스트 설정
                                greed.setText("닉네임을 설정해주세요.");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // 에러 처리
                            Toast.makeText(MainActivity.this, "닉네임을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // 로그인이 되어 있지 않으면 로그인 화면으로 이동
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // 버튼 초기화 및 클릭 이벤트 처리
        Button photobutton = findViewById(R.id.btngotophoto);
        photobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // PhotoActivity로 이동
                Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
                startActivity(intent);
            }
        });

        Button chatbutton = findViewById(R.id.btnchat);
        chatbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ChatActivity로 이동
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        // 사용자 찾기 버튼 클릭 시 UserFindActivity로 이동
        Button findUserButton = findViewById(R.id.btnFindUser);
        findUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // UserFindActivity로 이동
                Intent intent = new Intent(MainActivity.this, TestChatActivity.class);
                startActivity(intent);
            }
        });
    }
}

