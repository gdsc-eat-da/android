package root.dongmin.eat_da;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;

import root.dongmin.eat_da.adapter.ChatRoomAdapter;

public class UserFindActivity extends AppCompatActivity {

    private RecyclerView chatRoomRecyclerView;
    private ArrayList<String> chatRoomList;
    private ChatRoomAdapter adapter;

    private DatabaseReference mDatabaseRef;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_find);

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
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");
        userRef = FirebaseDatabase.getInstance().getReference("chat");

        // "chat" 노드 아래의 데이터 이름을 불러와서 로그로 출력
        getChatRoomNames();
    }

    private void getChatRoomNames() {
        userRef.child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // "chat" 노드가 존재하는지 확인
                if (dataSnapshot.exists()) {
                    Log.d("UserFindActivity", "chat data exists.");

                    // "chat" 노드 하위의 모든 데이터를 순회
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String chatRoomName = snapshot.child("chat").getKey(); // 채팅방 이름 가져오기
                        Log.d("UserFindActivity", "Chat room name: " + chatRoomName);

                        // 필요하다면 여기서 추가 작업 수행
                        // 예: 특정 조건에 맞는 채팅방만 필터링
                        String[] parts = chatRoomName.split("_");
                        Log.d("UserFindActivity", "parts:::: " + Arrays.toString(parts));

                        if (parts.length > 1 && "chop".equals(parts[parts.length - 1])) {
                            chatRoomList.add(chatRoomName); // 리스트에 추가
                        }
                    }

                    // 리스트에 추가된 채팅방 내용 출력
                    Log.d("UserFindActivity", "Filtered chat rooms with 'chop': " + Arrays.toString(chatRoomList.toArray()));

                    // 어댑터에 데이터 변경을 알림
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d("UserFindActivity", "chat data does not exist.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 오류 처리
                Log.e("UserFindActivity", "Error fetching chat rooms: " + databaseError.getMessage());
            }
        });
    }
}

