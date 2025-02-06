package root.dongmin.eat_da.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import root.dongmin.eat_da.PostDetailActivity;
import root.dongmin.eat_da.network.Post;
import root.dongmin.eat_da.R;

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

            // ✅ 클릭한 게시글 정보를 새로운 액티비티로 전달
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("title", postTitle);
            intent.putExtra("ingredients", postIngredients);
            intent.putExtra("image", postImage);
            intent.putExtra("nickname", postNickname);
            context.startActivity(intent);
        });
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
}

