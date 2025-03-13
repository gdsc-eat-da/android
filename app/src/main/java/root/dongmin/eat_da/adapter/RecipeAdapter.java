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
import root.dongmin.eat_da.R;

import root.dongmin.eat_da.network.Post;
import root.dongmin.eat_da.network.Recipe;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<Recipe> recipeList;

    public RecipeAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }


    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position){
        Recipe recipe = recipeList.get(position);
        holder.contentsTextView.setText(recipe.getContents());
        holder.ingredientsTextView.setText(recipe.getIngredients());

        // 🔥 이미지 로드
        Glide.with(context)
                .load(recipe.getPhoto())
                .into(holder.imageView);

    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView contentsTextView, ingredientsTextView;
        ImageView imageView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            contentsTextView = itemView.findViewById(R.id.textRecipeContents);
            ingredientsTextView = itemView.findViewById(R.id.textRecipeIngredient);
            imageView = itemView.findViewById(R.id.imageRecipe);
        }
    }

}
