package root.dongmin.eat_da.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.PostDetailActivity;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.Post;
import root.dongmin.eat_da.R;
import root.dongmin.eat_da.network.RetrofitClient;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.contentsTextView.setText(post.getContents());
        holder.ingredientsTextView.setText(post.getIngredients());

        // 🔥 이미지 로드
        Glide.with(context)
                .load(post.getPhoto())
                .into(holder.imageView);

        // ✅ 아이템 클릭 이벤트 추가
        holder.itemView.setOnClickListener(v -> {
            // 클릭한 게시글의 정보 가져오기
            String postTitle = post.getContents();  // 게시글 제목
            String postIngredients = post.getIngredients();  // 재료 정보
            String postImage = post.getPhoto();  // 이미지 URL
            String postNickname = post.getNickname();
            String postID = post.getPostID();

            // ✅ 클릭한 게시글 정보를 새로운 액티비티로 전달
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("title", postTitle);
            intent.putExtra("ingredients", postIngredients);
            intent.putExtra("image", postImage);
            intent.putExtra("nickname", postNickname);
            intent.putExtra("postID",postID);
            context.startActivity(intent);
        });

        // 길게 클릭 시 삭제 확인 창
        holder.itemView.setOnLongClickListener(v -> {
            // 현재 로그인된 사용자 닉네임 가져오기
            getNickname(nickname -> {
                if (nickname != null && nickname.equals(post.getNickname())) {
                    Log.d("PostAdapter", "로그인한 사용자 닉네임: " + nickname);
                    Log.d("PostAdapter", "게시글 작성자 닉네임: " + post.getNickname());
                    // 자신이 작성한 게시물만 삭제 가능
                    new AlertDialog.Builder(context)
                            .setTitle("삭제하시겠습니까?")
                            .setMessage("이 게시물을 삭제하시겠습니까?")
                            .setPositiveButton("확인", (dialog, which) -> {
                                // 서버에 게시물 삭제 요청
                                deletePost(post.getPostID());
                            })
                            .setNegativeButton("취소", null)
                            .show();
                } else {
                    // 자신이 작성한 게시물이 아니라면 삭제 불가
                    Toast.makeText(context, "자신의 게시물만 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
            });

            return true;  // 길게 클릭 이벤트 처리 후 다른 이벤트 방지
        });
    }

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

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView contentsTextView, ingredientsTextView;
        ImageView imageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            contentsTextView = itemView.findViewById(R.id.textContents);
            ingredientsTextView = itemView.findViewById(R.id.textIngredients);
            imageView = itemView.findViewById(R.id.imagePost);
        }
    }

    // 서버에서 게시물 삭제하는 메서드
    private void deletePost(String postID) {
        ApiService apiService = RetrofitClient.getApiService(context);
        Call<ResponseBody> call = apiService.deletePost(postID);

        Log.d("DeletePost", "삭제할 게시물의 postID: " + postID);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 삭제 후 RecyclerView에서 해당 게시물 제거
                    postList.removeIf(post -> post.getPostID().equals(postID));
                    notifyDataSetChanged();
                    Toast.makeText(context, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(context, "네트워크 오류로 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnNicknameReceivedListener {
        void onReceived(String nickname);
    }
}
