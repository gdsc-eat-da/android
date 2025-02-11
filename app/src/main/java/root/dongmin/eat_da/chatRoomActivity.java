package root.dongmin.eat_da;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import root.dongmin.eat_da.adapter.ChatRoomAdapter;

public class chatRoomActivity extends AppCompatActivity {

    private RecyclerView chatRoomRecyclerView;
    private ArrayList<String> chatRoomList;
    private ChatRoomAdapter adapter;

    private FirebaseAuth mFirebaseAuth;

    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_room);

        // RecyclerView 초기화
        chatRoomRecyclerView = findViewById(R.id.chatRoomRecyclerView);

        // LinearLayoutManager 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRoomRecyclerView.setLayoutManager(layoutManager);

        // 채팅방 리스트 및 어댑터 설정
        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(chatRoomList);
        chatRoomRecyclerView.setAdapter(adapter);

        // Firebase Database reference 초기화
        // Firebase 및 UI 요소 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // "chop"이 포함된 채팅방 리스트 가져오기
        getChatRoomsWithChop();

        // 시스템 바 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Firebase에서 "chop"이 포함된 채팅방을 가져오는 메소드
    private void getChatRoomsWithChop() {
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // "chatting" 노드가 존재하는지 체크
                if (dataSnapshot.exists()) {
                    Log.d("chatRoomActivity", "chatting data exists.");
                } else {
                    Log.d("chatRoomActivity", "chatting data does not exist.");
                }

                // "chatting" 노드 하위의 모든 데이터를 순회
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String chatRoom = snapshot.getKey(); // 채팅방 이름 가져오기
                    Log.d("chatRoomActivity", "chatRoom name: " + chatRoom);

                    // "chop"이 두 번째 "_" 이후에 오는 채팅방만 필터링
                    String[] parts = chatRoom.split("_");

                    // 배열을 Log로 출력
                    Log.d("chatRoomActivity", "parts:::: " + Arrays.toString(parts));

                    if (parts.length > 1 && "chop".equals(parts[parts.length - 1])) {
                        chatRoomList.add(chatRoom); // 리스트에 추가
                    }
                }

                // 리스트에 추가된 채팅방 내용 출력
                Log.d("chatRoomActivity", "Filtered chat rooms with 'chop': " + Arrays.toString(chatRoomList.toArray()));

                // 어댑터에 데이터 변경을 알림
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 오류 처리
                Log.e("chatRoomActivity", "Error fetching chat rooms: " + databaseError.getMessage());
            }
        });
    }
}
