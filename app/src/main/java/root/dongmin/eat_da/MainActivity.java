package root.dongmin.eat_da;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.adapter.PostAdapter;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.Post;
import root.dongmin.eat_da.network.RetrofitClient;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private TextView greed; // 사용자 환영 메시지
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase 및 UI 요소 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");
        greed = findViewById(R.id.greeding);
        recyclerView = findViewById(R.id.recyclerView);

        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);

        // 사용자 정보 및 게시글 불러오기
        loadUserInfo();
        loadPosts();

        // 버튼 이벤트 처리
        setupButtons();
    }

    // 사용자 닉네임 가져오기
    private void loadUserInfo() {
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            mDatabaseRef.child(userId).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String nickname = dataSnapshot.getValue(String.class);
                    if (nickname != null) {
                        greed.setText("반갑습니다, " + nickname + "님!");
                    } else {
                        greed.setText("닉네임을 설정해주세요.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "닉네임을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // 로그인되지 않은 경우 로그인 화면으로 이동
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // 게시글 목록 불러오기
    private void loadPosts() {
        Call<List<Post>> call = apiService.getPosts();
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new GsonBuilder().setLenient().create();

                    // 🔥 서버 응답 로그 출력
                    try {
                        String jsonResponse = new Gson().toJson(response.body());
                        Log.d("MainActivity", "서버 응답 데이터: " + jsonResponse);
                        Log.d("RetrofitResponse", "Response: " + response.body());
                        Log.d("RetrofitError", "Error Body: " + response.errorBody());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 리스트 변환 및 RecyclerView 연결
                    List<Post> postList = response.body();
                    if (postList != null && !postList.isEmpty()) {
                        postAdapter = new PostAdapter(MainActivity.this, postList);
                        recyclerView.setAdapter(postAdapter);
                    } else {
                        showErrorMessage("게시글이 없습니다.");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("MainActivity", "서버 응답 오류: " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showErrorMessage("서버 응답 오류: 게시글을 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                showErrorMessage("네트워크 오류로 게시글을 불러올 수 없습니다.");
                Log.e("MainActivity", "게시글 불러오기 실패", t);
            }
        });
    }

    // 버튼 클릭 이벤트 처리
    private void setupButtons() {
        Button photobutton = findViewById(R.id.btngotophoto);
        photobutton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
            startActivity(intent);
        });

        Button chatbutton = findViewById(R.id.btnchat);
        chatbutton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        Button findUserButton = findViewById(R.id.btnFindUser);
        findUserButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, TestChatActivity.class);
            startActivity(intent);
        });
    }

    // 오류 메시지를 화면에 표시하는 메서드
    private void showErrorMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
