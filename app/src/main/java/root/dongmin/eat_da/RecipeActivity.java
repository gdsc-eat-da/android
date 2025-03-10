package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.adapter.RecipeAdapter;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.Recipe;
import root.dongmin.eat_da.network.RetrofitClient;

public class RecipeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private ApiService apiService;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> allRecipes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.recipe);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            private int previousItemId = R.id.recipe; // 초기 선택된 아이콘 (homeclicked 상태)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (previousItemId == item.getItemId()) {
                    return false; // 동일한 아이템 클릭 방지
                }


                if (item.getItemId() == R.id.recipe) {
                    Toast.makeText(RecipeActivity.this, "Mypage", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_home) {
                    Intent intent = new Intent(RecipeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }else if (item.getItemId() == R.id.chat) {
                    Intent intent = new Intent(RecipeActivity.this, UserFindActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }else if (item.getItemId() == R.id.nav_profile){
                    Intent intent = new Intent(RecipeActivity.this, MyPageActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.work_load) {
                    Intent intent = new Intent(RecipeActivity.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(RecipeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        recyclerView = findViewById(R.id.recipeRecyclerView);

        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);
    }

    // 레시피 목록 불러오기
    private void loadRecipe() {
        Call<List<Recipe>> call = apiService.getRecipes();
        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recipe>> call, @NonNull Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRecipes = response.body();
                    recipeAdapter = new RecipeAdapter(RecipeActivity.this, allRecipes);
                    recyclerView.setAdapter(recipeAdapter);
                } else {
                    Toast.makeText(RecipeActivity.this, "레시피 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Recipe>> call, @NonNull Throwable t) {
                Toast.makeText(RecipeActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
