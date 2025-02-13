package root.dongmin.eat_da;

import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.adapter.PostAdapter;
import root.dongmin.eat_da.network.ApiResponse;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.Post;
import root.dongmin.eat_da.network.RetrofitClient;

public class MyPostActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> allPosts;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);

        recyclerView = findViewById(R.id.myrecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ✅ RetrofitClient를 사용하여 apiService 인스턴스를 생성
        apiService = RetrofitClient.getApiService(MyPostActivity.this);

        // Firebase에서 현재 사용자의 닉네임을 가져와서 게시글 로드
        loadMyPosts();
    }

    private void loadMyPosts() {
        // ✅ Firebase에서 닉네임 가져오기
        getNickname(nickname -> {
            if (nickname == null || nickname.isEmpty()) {
                Toast.makeText(MyPostActivity.this, "닉네임을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("MyPostActivity", "닉네임: " + nickname);

            // ✅ 서버에 내 닉네임으로 게시글 요청
            Call<ResponseBody> call = apiService.getMyPosts(nickname);  // 수정된 부분: ResponseBody로 요청
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            // 서버로부터 받은 응답을 String으로 변환
                            String responseBody = response.body().string();
                            Log.d("MyPostActivity", "응답 본문: " + responseBody);
                            Log.d("MyPostActivity", "응답: " + responseBody);

                            // JSON 파싱하여 응답 처리
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success) {
                                // 게시글 목록 추출
                                JSONArray postsArray = jsonResponse.getJSONArray("posts");
                                List<Post> posts = new ArrayList<>();
                                for (int i = 0; i < postsArray.length(); i++) {
                                    JSONObject postObject = postsArray.getJSONObject(i);
                                    Post post = new Post();
                                    post.setPostID(String.valueOf(postObject.getInt("postID")));
                                    post.setContents(postObject.getString("contents"));
                                    post.setIngredients(postObject.getString("ingredients"));
                                    post.setPhoto(postObject.getString("photo"));
                                    post.setNickname(postObject.getString("nickname"));
                                    // 다른 필드도 필요에 맞게 추출
                                    posts.add(post);
                                }

                                if (!posts.isEmpty()) {
                                    allPosts = posts;
                                    postAdapter = new PostAdapter(MyPostActivity.this, allPosts);
                                    recyclerView.setAdapter(postAdapter);
                                } else {
                                    Toast.makeText(MyPostActivity.this, "게시글이 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(MyPostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("MyPostActivity", "JSON 파싱 오류: " + e.getMessage());
                            Toast.makeText(MyPostActivity.this, "응답 처리 오류", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("MyPostActivity", "Error: " + response.message());
                        Toast.makeText(MyPostActivity.this, "게시글을 불러올 수 없습니다. 서버 오류.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    // 네트워크 오류 처리
                    Log.d("MyPostActivity", "Failure: " + t.getMessage());
                    Toast.makeText(MyPostActivity.this, "네트워크 오류로 게시글을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }



    // Firebase에서 현재 사용자의 닉네임을 가져오는 메서드
    private void getNickname(OnNicknameReceivedListener listener) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid(); // 현재 유저 UID 가져오기
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(uid);

            userRef.child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String nickname = dataSnapshot.getValue(String.class); // 닉네임 가져오기
                    if (nickname != null) {
                        Log.d("Nickname", "닉네임 가져옴: " + nickname);
                        listener.onReceived(nickname); // 콜백으로 전달
                    } else {
                        Log.e("Nickname", "닉네임이 없습니다.");
                        listener.onReceived(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "닉네임 불러오기 실패: " + databaseError.getMessage());
                    listener.onReceived(null);
                }
            });
        } else {
            Log.e("Nickname", "FirebaseUser가 null입니다.");
            listener.onReceived(null);
        }
    }

    // 닉네임 콜백 인터페이스
    interface OnNicknameReceivedListener {
        void onReceived(String nickname);
    }
}
