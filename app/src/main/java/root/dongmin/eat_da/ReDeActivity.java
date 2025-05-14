package root.dongmin.eat_da;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.imageview.ShapeableImageView;
import android.content.Intent;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReDeActivity extends AppCompatActivity {

    // 뷰 변수 선언
    private TextView detailTitle;
    private ShapeableImageView profile;
    private TextView nick;
    private TextView detailIngredients;
    private ImageView detailImage;
    private TextView whatisname;
    private TextView heart;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_re_de);

        // 뷰 초기화
        detailTitle = findViewById(R.id.detailTitle);
        profile = findViewById(R.id.profile);
        nick = findViewById(R.id.nick);
        detailIngredients = findViewById(R.id.detailIngredients);
        detailImage = findViewById(R.id.detailImage);
        whatisname = findViewById(R.id.whatisname);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // Intent에서 데이터 받아오기
        Intent intent = getIntent();
        if (intent != null) {
            String recipeID = intent.getStringExtra("recipeID");
            String contents = intent.getStringExtra("contents");
            String ingredients = intent.getStringExtra("ingredients");
            String photo = intent.getStringExtra("photo");
            String nickname = intent.getStringExtra("nickname");
            int suggestion = intent.getIntExtra("suggestion", 0);
            String hashtag = intent.getStringExtra("hashtag");
            int isRecipe = intent.getIntExtra("isrecipe", 0);

            // 뷰에 데이터 설정
            detailTitle.setText(contents); // 제목 (예: 요리 이름)
            nick.setText(nickname); // 작성자 닉네임
            detailIngredients.setText(ingredients); // 재료 목록
            whatisname.setText(hashtag); // 해시태그
            heart.setText(String.valueOf(suggestion)); // 좋아요 수

            // 이미지 로드 (Glide 사용)
            Glide.with(this)
                    .load(photo)
                    .into(detailImage);
        }


        loadImageProfile();

        // 시스템 바 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }




    private void loadImageProfile() {
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();

            // 사용자 정보 가져오기 (프로필 이미지)
            mDatabaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String profileImageUrl = dataSnapshot.child("profileImage").getValue(String.class);

                        // 프로필 이미지 설정
                        if (profileImageUrl != null) {
                            Glide.with(ReDeActivity.this)
                                    .load(profileImageUrl)  // Firebase에서 가져온 URL
                                    .into(profile);  // ImageView에 로드
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ReDeActivity.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "로그인되지 않은 사용자입니다.", Toast.LENGTH_SHORT).show();
        }
    }

}
