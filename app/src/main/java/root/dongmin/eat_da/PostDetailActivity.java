package root.dongmin.eat_da;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        String selectedJoinedItems = intent.getStringExtra("selectedJoinedItems");

        // 데이터 적용
        titleView.setText(title);
        nickNameView.setText(Nick);
        ingredientsTextView.setText(ingredients);
        Glide.with(this).load(image).into(postImageView);

        // ✅ selectedJoinedItems를 리스트로 변환
        if (selectedJoinedItems == null) {
            selectedJoinedItems = "";
        }
        List<String> foodAllergies = Arrays.asList(selectedJoinedItems.split("_"));
        Set<String> foodAllergySet = new HashSet<>(foodAllergies); // 중복 제거

        // 현재 로그인한 사용자 UID 가져오기
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // ✅ Firebase에서 사용자 myalergic 데이터 가져오기
        mDatabaseRef.child(uid).child("myalergic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String myAlergicData = dataSnapshot.getValue(String.class);
                    List<String> userAllergies = Arrays.asList(myAlergicData.split("_"));
                    Set<String> userAllergySet = new HashSet<>(userAllergies);

                    // ✅ 두 리스트 비교 (교집합 찾기)
                    userAllergySet.retainAll(foodAllergySet);
                    if (!userAllergySet.isEmpty()) {
                        // 교집합이 존재하면 팝업 띄우기
                        showAllergyWarningPopup(userAllergySet);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 오류 처리
            }
        });

        // 사용자 프로필 이미지 가져오기
        mDatabaseRef.orderByChild("nickname").equalTo(Nick).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 닉네임에 해당하는 사용자를 찾았을 때, profileImage 가져오기
                    String profileImageUrl = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        profileImageUrl = snapshot.child("profileImage").getValue(String.class);
                        break;  // 첫 번째 사용자만 가져오면 되므로 반복문을 종료
                    }
                    if (profileImageUrl != null) {
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
            chatIntent.putExtra("chatID", Nick);
            chatIntent.putExtra("postID", postID);
            startActivity(chatIntent);
        });
    }

    // ✅ 알레르기 경고 팝업 띄우는 함수
    private void showAllergyWarningPopup(Set<String> allergySet) {
        String message = "⚠️ 해당 음식에 포함된 알레르기 성분: " + String.join(", ", allergySet);

        new AlertDialog.Builder(this)
                .setTitle("알레르기 경고")
                .setMessage(message)
                .setPositiveButton("확인", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
