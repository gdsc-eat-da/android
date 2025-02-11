package root.dongmin.eat_da;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import root.dongmin.eat_da.adapter.ChatRoomAdapter;

public class UserFindActivity extends AppCompatActivity {

    private RecyclerView chatRoomRecyclerView;
    private ArrayList<String> chatRoomList;
    private ChatRoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_find);

        // Intent로 받은 채팅 리스트 가져오기
        List<String> chatList = getIntent().getStringArrayListExtra("chatList");
        String nickname = getIntent().getStringExtra("nickname");

        // RecyclerView 초기화
        chatRoomRecyclerView = findViewById(R.id.chatRoomRecyclerView);
        chatRoomRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 채팅방 리스트 및 어댑터 설정
        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(chatRoomList);
        chatRoomRecyclerView.setAdapter(adapter);

        // 받은 채팅 리스트를 RecyclerView에 업데이트
        if (chatList != null) {
            updateRecyclerView(chatList, nickname);
        } else {
            Log.d("ChatData", "비었다.....");
        }




    }

    private void updateRecyclerView(List<String> chatList, String nickname) {
        chatRoomList.clear();

        for (String chat : chatList) {
            // "_"로 분리하여 받는사람을 추출
            String[] chatDetails = chat.split("_");
            if (chatDetails.length >= 2) {
                String receivingPerson = chatDetails[1]; // "_받는사람_" 부분을 추출

                // nickname이 받는사람과 일치하는 경우에만 리스트에 추가
                if (receivingPerson.equals(nickname)) {
                    chatRoomList.add(chat);
                }
            }
        }

        adapter.notifyDataSetChanged();
        Log.d("UserFindActivity", "Updated chat rooms: " + chatRoomList);
    }

}
