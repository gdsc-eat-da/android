package root.dongmin.eat_da.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

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

        // 🔥 Base64 → URL로 변경하여 이미지 로드
        Glide.with(context)
                .load(post.getPhoto())  // URL을 바로 로드
                .into(holder.imageView);
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

