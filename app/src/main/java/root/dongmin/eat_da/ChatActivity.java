package root.dongmin.eat_da;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import root.dongmin.eat_da.adapter.UserAdapter;
import root.dongmin.eat_da.data.User;
import root.dongmin.eat_da.databinding.ActivityMainBinding;

public class ChatActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserAdapter adapter;
    private FirebaseAuth mAuth;
    private DatabaseReference mDbRef;
    private ArrayList<User> userList;
    private RecyclerView userChatRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View Binding 초기화
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_chat);


        // 인증 초기화
        mAuth = FirebaseAuth.getInstance();

        // Database 초기화
        mDbRef = FirebaseDatabase.getInstance().getReference();

        // 사용자 리스트 초기화
        userList = new ArrayList<>();

        // 어댑터 초기화
        adapter = new UserAdapter(this, userList);


        // RecyclerView를 findViewById로 참조
        userChatRecyclerView = findViewById(R.id.user_chat);

        // RecyclerView에 LinearLayoutManager 설정
        userChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 어댑터 설정을 위해 findViewById로 RecyclerView를 참조하고 adapter를 설정
        RecyclerView userRecyclerView = findViewById(R.id.user_chat);
        userRecyclerView.setAdapter(adapter);

        mDbRef.child("user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    User currentUser = postSnapshot.getValue(User.class);
                    if (currentUser != null && !mAuth.getCurrentUser().getUid().equals(currentUser.getuId())) {
                        userList.add(currentUser);
                    } //나를 제외한 다른 유저를 불러옴 -> 응용해서 나의 채팅방만을 불러옴(시도하기)
                }
                adapter.notifyDataSetChanged();
                // TODO: 구현 내용
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // 데이터 조회가 취소(실패) 되었을 때 호출
                // TODO: 구현 내용
            }
        });


    }
}
