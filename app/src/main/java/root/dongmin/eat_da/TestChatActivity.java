package root.dongmin.eat_da;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import root.dongmin.eat_da.adapter.ChatAdapter;
import root.dongmin.eat_da.data.ChatData;

public class TestChatActivity extends AppCompatActivity {

    // 리사이클러뷰 및 관련 변수 선언
    private RecyclerView mRecycleView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<ChatData> chatList;

    // Firebase 인증 관련 변수
    private FirebaseAuth mAuth;
    private String userEmail, receivedId; // 현재 로그인한 사용자의 이메일 저장

    // 채팅 입력 필드 및 전송 버튼
    private EditText EditText_chat;
    private ImageView Button_send,Button_back;


    private ImageView profile;
    private TextView yourNickView;

    // Firebase Realtime Database 참조
    private DatabaseReference myRef;

    // 클래스 멤버 변수로 yourNick 선언
    private String yourNick = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_chat);

        // 1. Intent에서 데이터 가져오기 (상대방 아이디)       (receivedID)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("chatID")) {
            receivedId = intent.getStringExtra("chatID");
            Toast.makeText(this, "채팅을 시작할 상대: " + receivedId, Toast.LENGTH_SHORT).show();
        }

        // 2. FirebaseAuth 인스턴스를 가져와 현재 로그인한 사용자 확인 (자기 자신 아이디)
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userEmail = currentUser.getEmail(); // 로그인한 사용자의 이메일 저장
            Log.d("TestChatActivity", "로그인된 이메일: " + userEmail);
            Toast.makeText(this, "로그인된 이메일: " + userEmail, Toast.LENGTH_SHORT).show();
        } else {
            userEmail = "Unknown";
            Log.d("TestChatActivity", "로그인된 사용자가 없습니다.");
        }

        // 3. 현재 사용자의 닉네임 가져오기
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid(); // uid 변수 초기화
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(uid);

            userRef.child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    yourNick = dataSnapshot.getValue(String.class); // 클래스 멤버 변수에 닉네임 저장
                    if (yourNick != null) {
                        Log.d("Nickname", "닉네임: " + yourNick);

                        // 4. 고유 채팅방 아이디 만들기
                        List<String> users = new ArrayList<>();
                        String userEmailSafe = userEmail.replace(".", "_").replace("@", "_");
                        String receivedIdSafe = receivedId.replace(".", "_").replace("@", "_");
                        users.add(receivedIdSafe);
                        users.add(yourNick);
                        Collections.sort(users);

                        String sortedId = users.get(0) + "_" + users.get(1);
                        Log.d("TestChatActivity", "정렬된 채팅방 ID: " + sortedId);

                        // 5. Firebase Realtime Database 참조 가져오기
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        myRef = database.getReference(sortedId);

                        // 6. UI 요소 초기화 및 이벤트 리스너 설정
                        initUI();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "닉네임 불러오기 실패: " + databaseError.getMessage());
                }
            });

        }
    }


    // UI 초기화 및 이벤트 리스너 설정
    private void initUI() {
        Button_send = findViewById(R.id.sendButton);
        Button_back = findViewById(R.id.backButton);
        EditText_chat = findViewById(R.id.EditText_chat);
        profile = findViewById(R.id.profileImage);
        yourNickView = findViewById(R.id.profileName);

        if (receivedId != null && !receivedId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount");

            // receivedId와 일치하는 사용자의 UID 찾기
            userRef.orderByChild("nickname").equalTo(receivedId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String nickname = snapshot.child("nickname").getValue(String.class);
                            String profileImageUrl = snapshot.child("profileImage").getValue(String.class);

                            if (nickname != null) {
                                yourNickView.setText(nickname); // 상대방 닉네임 표시
                            }

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                // Glide를 사용하여 프로필 이미지 로드
                                Glide.with(TestChatActivity.this)
                                        .load(profileImageUrl)
                                        .into(profile);
                            }
                            break; // 첫 번째 일치하는 사용자 정보만 사용
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "프로필 정보 불러오기 실패: " + databaseError.getMessage());
                }
            });
        }

        // 전송 버튼 클릭 시 채팅 데이터 Firebase에 저장
        Button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = EditText_chat.getText().toString();
                ChatData chat = new ChatData();
                chat.setNickname(yourNick); // 사용자 이메일을 닉네임으로 설정
                chat.setMsg(msg); // 입력된 메시지 설정
                myRef.push().setValue(chat); // Firebase에 데이터 저장
            }
        });
        Button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 리사이클러뷰 초기화 및 레이아웃 설정
        mRecycleView = findViewById(R.id.my_recycler_view);
        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);

        // 채팅 데이터 리스트 및 어댑터 설정
        chatList = new ArrayList<>();
        mAdapter = new ChatAdapter(chatList, TestChatActivity.this, yourNick);
        mRecycleView.setAdapter(mAdapter);

        // Firebase에서 새로운 채팅 데이터가 추가될 때마다 실행되는 리스너 설정
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatData chat = snapshot.getValue(ChatData.class); // 새로 추가된 데이터 가져오기
                ((ChatAdapter) mAdapter).addChat(chat); // 어댑터를 통해 리스트에 추가
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // 채팅 데이터가 변경되었을 때 처리 (현재 미구현)
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // 채팅 데이터가 삭제되었을 때 처리 (현재 미구현)
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // 채팅 데이터가 이동되었을 때 처리 (현재 미구현)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 데이터 로드 중 에러 발생 시 처리
            }
        });
    }


}