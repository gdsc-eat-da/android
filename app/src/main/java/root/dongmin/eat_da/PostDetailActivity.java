package root.dongmin.eat_da;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PostDetailActivity extends AppCompatActivity {

    private TextView nickNameView, ingredientsTextView, titleView;
    private ImageView postImageView, profileImage;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // 뷰 초기화
        nickNameView = findViewById(R.id.nick);
        ingredientsTextView = findViewById(R.id.detailIngredients);
        postImageView = findViewById(R.id.detailImage);
        profileImage = findViewById(R.id.profile);
        titleView = findViewById(R.id.detailTitle);

        // Firebase 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // MainActivity에서 전달받은 데이터 가져오기
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String ingredients = intent.getStringExtra("ingredients");
        String image = intent.getStringExtra("image");
        String Nick = intent.getStringExtra("nickname");
        String postID = intent.getStringExtra("postID");

        // 데이터 적용
        titleView.setText(title);
        nickNameView.setText(Nick);
        ingredientsTextView.setText(ingredients);
        Glide.with(this).load(image).into(postImageView);

        // 사용자 프로필 이미지 가져오기
        mDatabaseRef.orderByChild("nickname").equalTo(Nick).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 닉네임에 해당하는 사용자를 찾았을 때, profileImage 가져오기
                    String profileImageUrl = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        profileImageUrl = snapshot.child("profileImage").getValue(String.class);
                        break;  // 첫 번째 사용자만 가져오면 되므로 반복문을 종료합니다.
                    }
                    if (profileImageUrl != null) {
                        // 프로필 이미지 URL이 존재하면 Glide로 로드
                        Glide.with(PostDetailActivity.this).load(profileImageUrl).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 오류 처리
            }
        });

        // 채팅 버튼 클릭 시, TestChatActivity로 닉네임 전달
        Button chatbutton = findViewById(R.id.chatButton);
        chatbutton.setOnClickListener(view -> {
            Intent chatIntent = new Intent(PostDetailActivity.this, TestChatActivity.class);
            chatIntent.putExtra("chatID", Nick);  // 닉네임을 "chatID"라는 키로 전달
            chatIntent.putExtra("postID",postID);
            startActivity(chatIntent);


        });

        Button chatbutton2 = findViewById(R.id.alergicButton);
        chatbutton2.setOnClickListener(view -> {
            Intent chatIntent = new Intent(PostDetailActivity.this, alergicActivity.class);
            startActivity(chatIntent);
        });
    }
}
