package root.dongmin.eat_da;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import root.dongmin.eat_da.adapter.ChatAdapter;
import root.dongmin.eat_da.data.ChatData;

public class TestChatActivity extends AppCompatActivity {


    private RecyclerView mRecycleView;//xml에 있는 리사이클뷰를 끌어온다
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<ChatData> chatList;
    private FirebaseAuth mAuth;
    private String userEmail; // 사용자 이메일 저장 변수
    private EditText EditText_chat;
    private Button Button_send;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_chat);



        // FirebaseAuth 인스턴스 가져오기-----------------------------------------초기 자기자신 식별코드
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userEmail = currentUser.getEmail(); // 현재 로그인한 사용자의 이메일 가져오기
            Log.d("TestChatActivity", "로그인된 이메일: " + userEmail);
            Toast.makeText(this, "로그인된 이메일: " + userEmail, Toast.LENGTH_SHORT).show();
        } else {
            userEmail = "Unknown";
            Log.d("TestChatActivity", "로그인된 사용자가 없습니다.");
        }




//초기설정 리사이클뷰랑 어댑터 연동 코드------------------------------------------------------------
        Button_send = findViewById(R.id.Button_send);
        EditText_chat = findViewById(R.id.EditText_chat);
        Button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = EditText_chat.getText().toString();
                ChatData chat = new ChatData();
                chat.setNickname(userEmail);
                chat.setMsg(msg);
                myRef.push().setValue(chat);
            }
        });

        mRecycleView = findViewById(R.id.my_recycler_view);
        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);


        chatList = new ArrayList<>();
        mAdapter = new ChatAdapter(chatList, TestChatActivity.this, userEmail);
        mRecycleView.setAdapter(mAdapter);








        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

//        ChatData chat = new ChatData();
//        chat.setNickname(userEmail);
//        chat.setMsg("hellpoooo");
//        myRef.setValue(chat);


        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatData chat = snapshot.getValue(ChatData.class);
                ((ChatAdapter) mAdapter).addChat(chat);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });









//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
}