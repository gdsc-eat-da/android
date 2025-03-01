package root.dongmin.eat_da;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
    private int isnotMine = 3; // 기본값 0

    int ischanged = 1;//좌우가 바뀌었는지?

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
                isnotMine = 0; // 왼쪽 버튼 클릭 시 0으로 설정
                Log.d("UserFind", "isnotMine value: " + isnotMine);
                adapter.setIsNotMine(isnotMine);
                selectLeftButton();
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isnotMine = 1; // 오른족
                Log.d("UserFind", "isnotMine value: " + isnotMine);
                adapter.setIsNotMine(isnotMine);
                selectRightButton();
            }
        });

        // Intent로 받은 채팅 리스트 가져오기
        //chatList = getIntent().getStringArrayListExtra("chatList");
        nickname = getIntent().getStringExtra("nickname");
        String chatRoom = getIntent().getStringExtra("chatRoom");
        int isnotMinea = getIntent().getIntExtra("isnotMinea", 2);
        if (chatRoom != null) {
            // 전달된 채팅방 정보를 사용하여 명령어 처리
            handleChatRoomAction(chatRoom, isnotMinea);
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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(UserFindActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });







        //leftButton.bringToFront(); // 버튼 1을 가장 앞으로 가져옴
        defaultButton();


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






    void defaultButton()
    {

        leftButton.setZ(10f); // 왼쪽 버튼이 위로 올라옴
        rightButton.setZ(5f);

        // 왼쪽 버튼 이동 애니메이션
        ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(leftButton, "translationX", 0f, 40f);
        moveAnimator.setDuration(400);

        // 오른쪽 버튼 이동 애니메이션 (왼쪽으로 약간 이동)
        ObjectAnimator moveAnimator2 = ObjectAnimator.ofFloat(rightButton, "translationX", 40f, 0f);
        moveAnimator2.setDuration(400);

        // 왼쪽 버튼 가로 크기 확대
        ValueAnimator leftWidthAnimator = ValueAnimator.ofInt(leftButton.getWidth(), leftButton.getWidth() + 200);
        leftWidthAnimator.setDuration(400);
        leftWidthAnimator.addUpdateListener(animation -> {
            int newWidth = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = leftButton.getLayoutParams();
            params.width = newWidth;
            leftButton.setLayoutParams(params);
        });

        // 오른쪽 버튼 가로 크기 축소
        ValueAnimator rightWidthAnimator = ValueAnimator.ofInt(rightButton.getWidth(), rightButton.getWidth() -0);
        rightWidthAnimator.setDuration(400);
        rightWidthAnimator.addUpdateListener(animation -> {
            int newWidth = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = rightButton.getLayoutParams();
            params.width = newWidth;
            rightButton.setLayoutParams(params);
        });

//        // 왼쪽 버튼 색상 변경 애니메이션 (민트색으로 변경)
//        int mintColor = Color.parseColor("#55eee0"); // 민트색
//        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.WHITE, mintColor);
//        colorAnimator.setDuration(1000);
//        colorAnimator.addUpdateListener(animation ->
//                leftButton.setBackgroundTintList(ColorStateList.valueOf((int) animation.getAnimatedValue()))
//
//        );



        rightButton.setBackgroundResource(R.drawable.rounded_button_no);
        leftButton.setBackgroundResource(R.drawable.rounded_button_yes);


        rightButton.setZ(5f); // 오른쪽 버튼이 아래로 내려감
        leftButton.setZ(10f); // 왼쪽 버튼이 위로 올라옴


        rightButton.setTextColor(Color.parseColor("#babac0")); // 또는 Color.parseColor("#babac0")
        leftButton.setTextColor(Color.parseColor("#FFFFFF")); // 또는 Color.parseColor("#000000")



        // 애니메이션 실행
        moveAnimator2.start();
        moveAnimator.start();


        rightWidthAnimator.start();
        leftWidthAnimator.start();
        //colorAnimator.start();
        ischanged = 0;

        rightButton.setZ(5f); // 오른쪽 버튼이 아래로 내려감
        leftButton.setZ(10f); // 왼쪽 버튼이 위로 올라옴


    }

    private void selectLeftButton() {
        if(ischanged == 1)
        {
            // 왼쪽 버튼 이동 애니메이션
            //ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(leftButton, "translationX", 0f, 30f);
            //moveAnimator.setDuration(1000);

            // 오른쪽 버튼 이동 애니메이션 (왼쪽으로 약간 이동)
            //ObjectAnimator moveAnimator2 = ObjectAnimator.ofFloat(rightButton, "translationX", 0f, 0f);
            //moveAnimator2.setDuration(1000);

            // 왼쪽 버튼 가로 크기 확대
            ValueAnimator leftWidthAnimator = ValueAnimator.ofInt(leftButton.getWidth(), leftButton.getWidth() + 200);
            leftWidthAnimator.setDuration(400);
            leftWidthAnimator.addUpdateListener(animation -> {
                int newWidth = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = leftButton.getLayoutParams();
                params.width = newWidth;
                leftButton.setLayoutParams(params);
            });

            // 오른쪽 버튼 가로 크기 축소
            ValueAnimator rightWidthAnimator = ValueAnimator.ofInt(rightButton.getWidth(), rightButton.getWidth() -200);
            rightWidthAnimator.setDuration(400);
            rightWidthAnimator.addUpdateListener(animation -> {
                int newWidth = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = rightButton.getLayoutParams();
                params.width = newWidth;
                rightButton.setLayoutParams(params);
            });

            leftButton.setBackgroundResource(R.drawable.rounded_button_yes);
            rightButton.setBackgroundResource(R.drawable.rounded_button_no);
            leftButton.setZ(10f); // 왼쪽 버튼이 위로 올라옴
            rightButton.setZ(5f); // 오른쪽 버튼이 아래로 내려감

            leftButton.setTextColor(Color.parseColor("#FFFFFF")); // 또는 Color.parseColor("#000000")
            rightButton.setTextColor(Color.parseColor("#babac0")); // 또는 Color.parseColor("#babac0")

            // 애니메이션 실행
            //moveAnimator.start();
            //moveAnimator2.start();
            leftWidthAnimator.start();
            rightWidthAnimator.start();
            //colorAnimator.start();
            ischanged = 0;
        }

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
        if(ischanged == 0)
        {
            // 왼쪽 버튼 이동 애니메이션
            //ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(leftButton, "translationX", 0f, -0f);
            //moveAnimator.setDuration(1000);

            // 오른쪽 버튼 이동 애니메이션 (왼쪽으로 약간 이동)
            //ObjectAnimator moveAnimator2 = ObjectAnimator.ofFloat(rightButton, "translationX", 0f, -30f);
            //moveAnimator2.setDuration(1000);

            // 왼쪽 버튼 가로 크기 확대
            ValueAnimator leftWidthAnimator = ValueAnimator.ofInt(leftButton.getWidth(), leftButton.getWidth() - 200);
            leftWidthAnimator.setDuration(400);
            leftWidthAnimator.addUpdateListener(animation -> {
                int newWidth = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = leftButton.getLayoutParams();
                params.width = newWidth;
                leftButton.setLayoutParams(params);
            });

            // 오른쪽 버튼 가로 크기 축소
            ValueAnimator rightWidthAnimator = ValueAnimator.ofInt(rightButton.getWidth(), rightButton.getWidth() + 200);
            rightWidthAnimator.setDuration(400);
            rightWidthAnimator.addUpdateListener(animation -> {
                int newWidth = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = rightButton.getLayoutParams();
                params.width = newWidth;
                rightButton.setLayoutParams(params);
            });

            leftButton.setBackgroundResource(R.drawable.rounded_button_no);
            rightButton.setBackgroundResource(R.drawable.rounded_button_yes);
            leftButton.setZ(5f); // 왼쪽 버튼이 위로 올라옴
            rightButton.setZ(10f); // 오른쪽 버튼이 아래로 내려감

            leftButton.setTextColor(Color.parseColor("#babac0")); // 또는 Color.parseColor("#000000")
            rightButton.setTextColor(Color.parseColor("#FFFFFF")); // 또는 Color.parseColor("#babac0")

            // 애니메이션 실행
            //moveAnimator.start();
            //moveAnimator2.start();
            leftWidthAnimator.start();
            rightWidthAnimator.start();
            //colorAnimator.start();
            ischanged = 1;
        }












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
    private void handleChatRoomAction(String chatRoom, int isnotMinea) {
        if (chatRoom == null) return; // 방 정보가 없으면 실행 안 함

        int a = 2; // 기본값

        // 현재 선택된 버튼 확인 (배경색 기준)
        if (leftButton.getBackground() instanceof ColorDrawable && ((ColorDrawable) leftButton.getBackground()).getColor() == Color.WHITE) {
            a = 1; // 왼쪽 버튼 선택됨
            Log.d("UserFind", "왼쪽 버튼 선택됨: " + a);
        } else if (rightButton.getBackground() instanceof ColorDrawable && ((ColorDrawable) rightButton.getBackground()).getColor() == Color.WHITE) {
            a = 0; // 오른쪽 버튼 선택됨
            Log.d("UserFind", "오른쪽 버튼 선택됨: " + a);
        }

        Log.d("UserFind", "Selected Chat Room: " + chatRoom);
        Log.d("UserFind", "Detected isnotMine value: " + isnotMinea);

        Intent chatIntent = new Intent(UserFindActivity.this, TestChatActivity.class);

        String[] parts = chatRoom.split(":");
        String nickname = parts[0];  // 닉네임 (":" 앞부분)

        // "_" 기준으로 문자열을 분리하여 번호 추출
        String[] parts2 = parts[1].split("_");
        String number = parts2[1];  // 번호 ("_" 뒤부분)

        chatIntent.putExtra("chatID", nickname);
        chatIntent.putExtra("postID", number);
        chatIntent.putExtra("isnotMine", isnotMinea);  // 버튼 클릭 상태 기반으로 전달
        Log.d("UserFind", "Final isnotMine value sent: " + isnotMinea);

        startActivity(chatIntent);
        finish();
    }



}

