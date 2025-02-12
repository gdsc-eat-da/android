package root.dongmin.eat_da;

import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_find);

        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // Intent로 받은 채팅 리스트 가져오기
        chatList = getIntent().getStringArrayListExtra("chatList");
        nickname = getIntent().getStringExtra("nickname");

        // RecyclerView 초기화
        chatRoomRecyclerView = findViewById(R.id.chatRoomRecyclerView);
        chatRoomRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 채팅방 리스트 및 어댑터 설정
        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(chatRoomList);
        chatRoomRecyclerView.setAdapter(adapter);

        // 게시글 데이터 가져오기
        loadPosts();

        // 받은 채팅 리스트를 RecyclerView에 업데이트
        if (chatList != null) {
            updateRecyclerView(chatList, nickname);
        } else {
            Log.d("ChatData", "비었다.....");
        }


    }

    // 채팅방 리스트 업데이트
    private void updateRecyclerView(List<String> chatList, String nickname) {
        chatRoomList.clear();

        for (String chat : chatList) {
            // "_"로 분리하여 받는사람을 추출
            String[] chatDetails = chat.split("_");
            if (chatDetails.length >= 2) {
                String receivingPerson = chatDetails[1]; // "_받는사람_" 부분을 추출
                String receivingNum = chatDetails[0];
                String showingPerson = chatDetails[2];
                String zemok = ".";

                // nickname이 받는사람과 일치하는 경우에만 리스트에 추가
                if (receivingPerson.equals(nickname)) {


                    if(posts  == null)
                    {
                        Log.e("UserFindActivity", "포스트가 null입니다.");
                        break;

                    }
                    for (Post post : posts) {
                        // 예시로 게시글 제목만 가져와 리스트에 추가
                        String postTitle = post.getContents();  // 게시글 제목
                        String postIngredients = post.getIngredients();  // 재료 정보
                        String postImage = post.getPhoto();  // 이미지 URL
                        String postNickname = post.getNickname();
                        String postID = post.getPostID();

                        if(receivingNum.equals(postID))
                        {
                            zemok += postTitle;
                        }
                    }


                    String mergechatList = showingPerson + ": " + zemok;


                    chatRoomList.add(mergechatList);
                }
            }
        }

        adapter.notifyDataSetChanged();
        Log.d("UserFindActivity", "Updated chat rooms: " + chatRoomList);
    }

    // 게시글 목록 불러오기
    private void loadPosts() {
        Call<List<Post>> call = apiService.getPosts(); // API 호출 (게시글 목록)
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    posts = response.body(); // 서버에서 받은 게시글 목록

                    // posts가 업데이트된 후, RecyclerView를 업데이트
                    // 받은 채팅 리스트를 RecyclerView에 업데이트
                    if (chatList != null) {
                        updateRecyclerView(chatList, nickname);
                    } else {
                        Log.d("ChatData", "비었다.....");
                    }
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



    // 게시글 RecyclerView 업데이트
    private void updatePosts(List<Post> posts) {
        // 게시글을 chatRoomList에 추가 (필요에 따라 필터링 가능)
        for (Post post : posts) {
            // 예시로 게시글 제목만 가져와 리스트에 추가
            String postTitle = post.getContents();  // 게시글 제목
            String postIngredients = post.getIngredients();  // 재료 정보
            String postImage = post.getPhoto();  // 이미지 URL
            String postNickname = post.getNickname();
            String postID = post.getPostID();
        }

        adapter.notifyDataSetChanged();
        Log.d("UserFindActivity", "게시글 목록 업데이트: " + chatRoomList);
    }



    private void loadChatList() {
        //chatList = new ArrayList<>();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("chat");

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Toast.makeText(MainActivity.this, "채팅접근중.", Toast.LENGTH_SHORT).show();
                Log.d("ChatData", "Children count: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String chatId = snapshot.getKey(); // 최상위 키(채팅 ID) 가져오기
                    if (chatId != null) {
                        chatList.add(chatId);
                    }
                }

                handleChatList(chatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserFindActivity.this, "채팅 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleChatList(List<String> chatList) {
        for (String chat : chatList) {
            Log.d("ChatData", chat);
        }
        //그리고 이걸 보내야 한다...!!!!
    }


}
