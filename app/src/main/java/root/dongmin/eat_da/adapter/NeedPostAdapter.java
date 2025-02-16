package root.dongmin.eat_da.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import root.dongmin.eat_da.network.NeedPost;
import root.dongmin.eat_da.R;

public class NeedPostAdapter extends RecyclerView.Adapter<NeedPostAdapter.ViewHolder> {
    private Context context;
    private List<NeedPost> needPostList;

    public NeedPostAdapter(Context context, List<NeedPost> needPostList) {
        this.context = context;
        this.needPostList = needPostList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_needpost, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NeedPost post = needPostList.get(position);
        holder.textNeedContents.setText(post.getContents());
        holder.textNeedIngredients.setText(post.getIngredients());
    }

    @Override
    public int getItemCount() {
        return needPostList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNeedContents, textNeedIngredients;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNeedContents = itemView.findViewById(R.id.textNeedContents);
            textNeedIngredients = itemView.findViewById(R.id.textNeedIngredients);
        }
    }
}

