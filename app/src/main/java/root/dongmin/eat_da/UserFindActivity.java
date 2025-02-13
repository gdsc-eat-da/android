package root.dongmin.eat_da;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import root.dongmin.eat_da.adapter.ChatRoomAdapter;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.Post;
import root.dongmin.eat_da.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFindActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;
    private RecyclerView chatRoomRecyclerView;
    private ArrayList<String> chatRoomList;
    private ChatRoomAdapter adapter;
    private List<Post> posts;

    private String nickname;
    private List<String> chatList;

    // API 서비스 객체
    private ApiService apiService;

    private Button leftButton;
    private Button rightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_find);

        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLeftButton();
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRightButton();
            }
        });

        // Intent로 받은 채팅 리스트 가져오기
        //chatList = getIntent().getStringArrayListExtra("chatList");
        nickname = getIntent().getStringExtra("nickname");
        String chatRoom = getIntent().getStringExtra("chatRoom");
        if (chatRoom != null) {
            // 전달된 채팅방 정보를 사용하여 명령어 처리
            handleChatRoomAction(chatRoom);
        }

        // RecyclerView 초기화
        chatRoomRecyclerView = findViewById(R.id.chatRoomRecyclerView);
        chatRoomRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 채팅방 리스트 및 어댑터 설정
        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(chatRoomList);
        chatRoomRecyclerView.setAdapter(adapter);

        loadChatList(new OnChatListLoadedListener() {
            @Override
            public void onChatListLoaded(List<String> chatList) {
                loadPosts(new OnPostsLoadedListener() {
                    @Override
                    public void onPostsLoaded(List<Post> posts) {
                        updateRecyclerView(chatList, nickname, posts,0);
                    }
                });
            }
        });
    }

    private void loadChatList(OnChatListLoadedListener listener) {
        chatList = new ArrayList<>();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("chat");

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String chatId = snapshot.getKey();
                    if (chatId != null) {
                        chatList.add(chatId);
                    }
                }
                listener.onChatListLoaded(chatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserFindActivity.this, "채팅 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPosts(OnPostsLoadedListener listener) {
        Call<List<Post>> call = apiService.getPosts();
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onPostsLoaded(response.body());
                } else {
                    Log.e("UserFindActivity", "게시글을 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.e("UserFindActivity", "네트워크 오류로 게시글을 불러올 수 없습니다.");
            }
        });
    }

    private void updateRecyclerView(List<String> chatList, String nickname, List<Post> posts,int a) {
        chatRoomList.clear();

        for (String chat : chatList) {
            String[] chatDetails = chat.split("_");
//            //------------------------------------------------------------
//            if (chatDetails.length >= 2) {
//                String receivingPerson = chatDetails[1];
//                String receivingNum = chatDetails[0];
//                String showingPerson = chatDetails[2];
//                String zemok = "";
//
//                if (receivingPerson.equals(nickname)) {
//                    for (Post post : posts) {
//                        if (receivingNum.equals(post.getPostID())) {
//                            zemok += post.getContents();
//                        }
//                    }
//                    String mergechatList = showingPerson + ": " + zemok;
//                    chatRoomList.add(mergechatList);
//                }
//            }
//            //--------------------------------------------------------------
            if(a == 0)
            {
                //------------------------------------------------------------
                if (chatDetails.length >= 2) {
                    String receivingPerson = chatDetails[1];
                    String receivingNum = chatDetails[0];
                    String showingPerson = chatDetails[2];
                    String zemok = "";

                    if (receivingPerson.equals(nickname)) {
                        for (Post post : posts) {
                            if (receivingNum.equals(post.getPostID())) {
                                zemok += post.getContents();
                            }
                        }
                        String mergechatList = showingPerson + ": " + zemok + "_" + receivingNum;
                        chatRoomList.add(mergechatList);
                    }
                }
                //--------------------------------------------------------------
            }
            else
            {
                //------------------------------------------------------------
                if (chatDetails.length >= 2) {
                    String receivingPerson = chatDetails[1];
                    String receivingNum = chatDetails[0];
                    String showingPerson = chatDetails[2];
                    String zemok = "";

                    if (showingPerson.equals(nickname)) {
                        for (Post post : posts) {
                            if (receivingNum.equals(post.getPostID())) {
                                zemok += post.getContents();
                            }
                        }
                        String mergechatList = receivingPerson + ": " + zemok + "_" + receivingNum;
                        chatRoomList.add(mergechatList);
                    }
                }
                //--------------------------------------------------------------
            }
        }
        adapter.notifyDataSetChanged();
    }

    interface OnChatListLoadedListener {
        void onChatListLoaded(List<String> chatList);
    }

    interface OnPostsLoadedListener {
        void onPostsLoaded(List<Post> posts);
    }




    private void selectLeftButton() {
        leftButton.setBackgroundColor(Color.BLACK);
        leftButton.setTextColor(Color.WHITE);

        rightButton.setBackgroundColor(Color.WHITE);
        rightButton.setTextColor(Color.BLACK);
        loadChatList(new OnChatListLoadedListener() {
            @Override
            public void onChatListLoaded(List<String> chatList) {
                loadPosts(new OnPostsLoadedListener() {
                    @Override
                    public void onPostsLoaded(List<Post> posts) {
                        updateRecyclerView(chatList, nickname, posts,0);
                    }
                });
            }
        });
    }

    private void selectRightButton() {
        rightButton.setBackgroundColor(Color.BLACK);
        rightButton.setTextColor(Color.WHITE);

        leftButton.setBackgroundColor(Color.WHITE);
        leftButton.setTextColor(Color.BLACK);
        loadChatList(new OnChatListLoadedListener() {
            @Override
            public void onChatListLoaded(List<String> chatList) {
                loadPosts(new OnPostsLoadedListener() {
                    @Override
                    public void onPostsLoaded(List<Post> posts) {
                        updateRecyclerView(chatList, nickname, posts,1);
                    }
                });
            }
        });
    }


    // 채팅방 정보를 처리하는 메서드
    private void handleChatRoomAction(String chatRoom) {
        // 여기서 chatRoom에 대한 명령어 처리 구현
        // 예를 들어, 채팅방 이름을 로그로 출력하거나, Firebase에서 해당 채팅방의 메시지를 불러오는 등의 작업을 할 수 있습니다.
        Log.d("UserFind", "Selected Chat Room: " + chatRoom);
        Intent chatIntent = new Intent(UserFindActivity.this, TestChatActivity.class);

        String[] parts = chatRoom.split(":");
        String nickname = parts[0];  // 닉네임 (":" 앞부분)

        // "_" 기준으로 문자열을 분리하여 번호 추출
        String[] parts2 = parts[1].split("_");
        String number = parts2[1];  // 번호 ("_" 뒤부분)


        chatIntent.putExtra("chatID", nickname);  // 닉네임을 "chatID"라는 키로 전달
            chatIntent.putExtra("postID",number);
    startActivity(chatIntent);
        // 현재 액티비티 종료
        finish();


        // Firebase 또는 다른 처리 로직을 추가
        // 예: Firebase에서 채팅방 메시지 불러오기, 채팅방에 대한 세부 작업 처리 등
    }

}

