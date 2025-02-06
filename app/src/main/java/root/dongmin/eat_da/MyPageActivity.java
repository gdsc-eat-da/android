package root.dongmin.eat_da;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import root.dongmin.eat_da.R;

public class MyPageActivity extends AppCompatActivity {

    private TextView namePage; // 사용자 닉네임 표시
    private TextView transaction; // 총 거래 횟수 표시
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_page);

        namePage = findViewById(R.id.nickname);
        transaction = findViewById(R.id.donationCount);

        // Firebase 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // 현재 로그인된 사용자 정보 가져오기
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();

            // 사용자 정보 가져오기 (닉네임과 거래 횟수)
            mDatabaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String nickname = dataSnapshot.child("nickname").getValue(String.class);
                        Integer transactionCount = dataSnapshot.child("transactionCount").getValue(Integer.class);

                        // 텍스트뷰에 사용자 정보 설정
                        if (nickname != null) {
                            namePage.setText(nickname+" 님의 페이지");
                        } else {
                            namePage.setText("닉네임이 없습니다.");
                        }

                        if (transactionCount != null) {
                            transaction.setText(String.format("기부횟수 %d 회", transactionCount));
                        } else {
                            transaction.setText("0");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MyPageActivity.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "로그인되지 않은 사용자입니다.", Toast.LENGTH_SHORT).show();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
