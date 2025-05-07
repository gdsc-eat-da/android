package root.dongmin.eat_da.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import root.dongmin.eat_da.R;

import root.dongmin.eat_da.ReDeActivity;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.LikeResponse;
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
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.contentsTextView.setText(recipe.getContents());
        holder.ingredientsTextView.setText(recipe.getIngredients());
        holder.countHeart.setText(String.valueOf(recipe.getSuggestion()));

        // 🔥 이미지 로드
        Glide.with(context)
                .load(recipe.getPhoto())
                .into(holder.imageView);

        // SharedPreferences에서 이전에 저장된 좋아요 상태 불러오기
        SharedPreferences preferences = context.getSharedPreferences("likes", Context.MODE_PRIVATE);
        final boolean[] isLiked = {preferences.getBoolean("liked_" + recipe.getRecipeID(), false)}; // 기본값은 false

        // 하트 상태 설정
        holder.heart.setImageResource(isLiked[0] ? R.drawable.heartclicked : R.drawable.heartdefault);

        // 하트 클릭 리스너
        holder.heart.setOnClickListener(v -> {
            isLiked[0] = !isLiked[0]; // 상태 반전
            holder.heart.setImageResource(isLiked[0] ? R.drawable.heartclicked : R.drawable.heartdefault);

            // SharedPreferences에 좋아요 상태 저장
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("liked_" + recipe.getRecipeID(), isLiked[0]);
            editor.apply();

            // 서버에 좋아요 상태 업데이트
            updateLikeStatus(recipe.getRecipeID(), isLiked[0]);

            // suggestion 값 바로 업데이트
            int newSuggestion = recipe.getSuggestion() + (isLiked[0] ? 1 : -1); // 좋아요가 눌렸으면 +1, 취소했으면 -1
            recipe.setSuggestion(newSuggestion);

            // 해당 아이템 새로 고침
            holder.countHeart.setText(String.valueOf(newSuggestion));

            // 전체 RecyclerView 새로고침 (특정 아이템만 새로 고치려면 notifyItemChanged(position) 사용)
            notifyItemChanged(position);
        });



        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReDeActivity.class);
            intent.putExtra("recipeID", recipe.getRecipeID());
            intent.putExtra("contents", recipe.getContents());
            intent.putExtra("ingredients", recipe.getIngredients());
            intent.putExtra("photo", recipe.getPhoto());
            intent.putExtra("nickname", recipe.getNickname());
            intent.putExtra("suggestion", recipe.getSuggestion());
            intent.putExtra("hashtag", recipe.getHashtag());
            intent.putExtra("isrecipe", recipe.getIsrecipe());
            context.startActivity(intent);
        });
    }



    // 좋아요 상태 업데이트 API 호출
    private void updateLikeStatus(String recipeID, boolean isLiked) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.post_url)) // 서버 주소
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Log.d("Like", "Base URL: " + context.getString(R.string.post_url));




        ApiService apiService = retrofit.create(ApiService.class);
        Call<LikeResponse> call = apiService.updateLike(recipeID, isLiked ? "true" : "false");
        Log.d("Like", "Request URL: " + call.request().url());

        call.enqueue(new Callback<LikeResponse>() {
            @Override
            public void onResponse(Call<LikeResponse> call, Response<LikeResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("Like", "좋아요 상태 업데이트 성공!");
                }
            }

            @Override
            public void onFailure(Call<LikeResponse> call, Throwable t) {
                Log.e("Like Error", "서버 요청 실패: " + t.getMessage());
            }
        });
    }


    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView contentsTextView, ingredientsTextView;
        ImageView imageView;
        ImageView heart;
        TextView countHeart;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            contentsTextView = itemView.findViewById(R.id.textRecipeContents);
            ingredientsTextView = itemView.findViewById(R.id.textRecipeIngredient);
            imageView = itemView.findViewById(R.id.imageRecipe);
            heart = itemView.findViewById(R.id.heart);
            countHeart = itemView.findViewById(R.id.heartCount);
        }
    }

    public void setItems(List<Recipe> recipeList){
        this.recipeList= recipeList;
        notifyDataSetChanged();
    }

}
