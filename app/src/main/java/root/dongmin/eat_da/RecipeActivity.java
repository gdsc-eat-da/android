package root.dongmin.eat_da;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.adapter.NeedPostAdapter;
import root.dongmin.eat_da.adapter.RecipeAdapter;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.NeedPost;
import root.dongmin.eat_da.network.Recipe;
import root.dongmin.eat_da.network.RetrofitClient;
import root.dongmin.eat_da.utils.SpaceItemDecoration;

public class RecipeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView, helprecycleView;
    private ApiService apiService;
    private RecipeAdapter recipeAdapter, helpAdapter;
    private List<Recipe> allRecipes = new ArrayList<>();
    private List<Recipe> recipeList = new ArrayList<>(); // isrecipe가 1인 레시피 목록
    private List<Recipe> helpList = new ArrayList<>(); // isrecipe가 0인 레시피 목록

    private TextView A, B, C, D, E, F, G, H, I; // 해시태그 버튼
    private int space;
    private EditText search;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private ImageView profileImage;
    private TextView greed;
    private String Nickname;
    private List<NeedPost> needPosts = new ArrayList<>(); // 거리 리스트!



    private boolean isAFiltered = false, isBFiltered = false, isCFiltered = false, isDFiltered = false, isEFiltered = false;
    private boolean isFFiltered = false, isGFiltered = false, isHFiltered = false, isIFiltered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // 초기화 및 설정
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.recipe);

        needPosts = getIntent().getParcelableArrayListExtra("needPostList");


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            private int previousItemId = R.id.recipe; // 초기 선택된 아이콘 (recipeclicked 상태)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (previousItemId == item.getItemId()) {
                    return false; // 동일한 아이템 클릭 방지
                }


                if (item.getItemId() == R.id.recipe) {
                    Toast.makeText(RecipeActivity.this, "RecipePage", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_home) {
                    Intent intent = new Intent(RecipeActivity.this, MainActivity.class);
                    startActivity(intent);

                    overridePendingTransition(0, 0); // 전환 애니메이션 제거

                    finish();
                    return true;
                }else if (item.getItemId() == R.id.chat) {
                    Intent intent = new Intent(RecipeActivity.this, UserFindActivity.class);
                    startActivity(intent);

                    overridePendingTransition(0, 0); // 전환 애니메이션 제거

                    return true;
                }else if (item.getItemId() == R.id.work_load){
                    // needPosts가 null이 아닐 때까지 기다림
                    if (needPosts != null && !needPosts.isEmpty()) {
                        Intent intent = new Intent(RecipeActivity.this, MapActivity.class);
                        intent.putParcelableArrayListExtra("needPostList", new ArrayList<>(needPosts)); // 리스트 전달
                        startActivity(intent);

                        overridePendingTransition(0, 0); // 전환 애니메이션 제거

                        finish();
                        return true;
                    } else {
                        // 필요 시 로딩 중 메시지나 대기 화면을 띄울 수도 있습니다
                        Toast.makeText(RecipeActivity.this, "데이터를 로딩 중입니다.", Toast.LENGTH_SHORT).show();
                    }
                }else if (item.getItemId() == R.id.nav_profile){
                    Intent intent = new Intent(RecipeActivity.this,MyPageActivity.class);
                    startActivity(intent);

                    overridePendingTransition(0, 0); // 전환 애니메이션 제거

                    finish();
                    return true;
                }
                return false;
            }
        }); // 이게 날아가 있었음 4/29 수정



        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(RecipeActivity.this, MainActivity.class);
                startActivity(intent);

                overridePendingTransition(0, 0); // 전환 애니메이션 제거

                finish();
            }
        }); // 뒤로가기 처리


        profileImage = findViewById(R.id.profileImage);
        greed = findViewById(R.id.greeding);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recipeRecyclerView);
        helprecycleView = findViewById(R.id.helpRecyclerView);
        space = 30;

        // SpaceItemDecoration 인스턴스 생성
        SpaceItemDecoration itemDecoration = new SpaceItemDecoration(space);
        recyclerView.addItemDecoration(itemDecoration);
        helprecycleView.addItemDecoration(itemDecoration);

        // RecyclerView LayoutManager 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        helprecycleView.setLayoutManager(new LinearLayoutManager(this));

        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);

        // 검색 기능 설정
        search = findViewById(R.id.searchRecipe);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString().toLowerCase(); // 입력된 검색어 가져오기
                filterRecipes(searchText); // 검색 함수 호출
            }
        });

        // 해시태그 버튼 초기화
        A = findViewById(R.id.TextView_msg1);
        B = findViewById(R.id.textView_msg2);
        C = findViewById(R.id.textView_msg3);
        D = findViewById(R.id.textView_msg4);
        E = findViewById(R.id.textView_msg5);
        F = findViewById(R.id.TextView_msg6);
        G = findViewById(R.id.textView_msg7);
        H = findViewById(R.id.textView_msg8);
        I = findViewById(R.id.textView_msg9);

        // 해시태그 버튼 클릭 리스너 설정
        A.setOnClickListener(v -> filterByHashtag("인기메뉴", true));
        B.setOnClickListener(v -> filterByHashtag("자취생", true));
        C.setOnClickListener(v -> filterByHashtag("건강식", true));
        D.setOnClickListener(v -> filterByHashtag("아동", true));
        E.setOnClickListener(v -> filterByHashtag("간식", true));
        F.setOnClickListener(v -> filterByHashtag("인기", false));
        G.setOnClickListener(v -> filterByHashtag("분리배출", false));
        H.setOnClickListener(v -> filterByHashtag("질문상담", false));
        I.setOnClickListener(v -> filterByHashtag("알뜰 지식", false));

        // 버튼 이벤트 처리
        setupButtons();

        // 레시피 데이터 불러오기
        loadRecipe();

        // 사용자 정보 불러오기
        loadUserInfo();
        loadImageProfile();
    }

    // 레시피 목록 불러오기
    private void loadRecipe() {
        Call<List<Recipe>> call = apiService.getRecipes();
        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recipe>> call, @NonNull Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRecipes = response.body();

                    // isrecipe 값에 따라 레시피 분류
                    for (Recipe recipe : allRecipes) {
                        if (recipe.getIsrecipe() == 1) {
                            recipeList.add(recipe); // isrecipe가 1인 경우
                        } else if (recipe.getIsrecipe() == 0) {
                            helpList.add(recipe); // isrecipe가 0인 경우
                        }
                    }

                    // 어댑터 설정
                    recipeAdapter = new RecipeAdapter(RecipeActivity.this, recipeList);
                    helpAdapter = new RecipeAdapter(RecipeActivity.this, helpList);

                    // RecyclerView에 어댑터 설정
                    recyclerView.setAdapter(recipeAdapter);
                    helprecycleView.setAdapter(helpAdapter);
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

    // 해시태그로 필터링
    // 해시태그로 필터링
    private void filterByHashtag(String hashtag, boolean isRecipe) {
        List<Recipe> filteredList = new ArrayList<>();
        List<Recipe> sourceList = isRecipe ? recipeList : helpList;
        TextView targetButton = null;

        boolean isFiltered = false;
        if (isRecipe) {
            switch (hashtag) {
                case "인기메뉴": isFiltered = isAFiltered; targetButton = A; isAFiltered = !isAFiltered; break;
                case "자취생": isFiltered = isBFiltered; targetButton = B; isBFiltered = !isBFiltered; break;
                case "건강식": isFiltered = isCFiltered; targetButton = C; isCFiltered = !isCFiltered; break;
                case "아동": isFiltered = isDFiltered; targetButton = D; isDFiltered = !isDFiltered; break;
                case "간식": isFiltered = isEFiltered; targetButton = E; isEFiltered = !isEFiltered; break;
            }
        } else {
            switch (hashtag) {
                case "인기": isFiltered = isFFiltered; targetButton = F; isFFiltered = !isFFiltered; break;
                case "분리배출": isFiltered = isGFiltered; targetButton = G; isGFiltered = !isGFiltered; break;
                case "질문상담": isFiltered = isHFiltered; targetButton = H; isHFiltered = !isHFiltered; break;
                case "알뜰 지식": isFiltered = isIFiltered; targetButton = I; isIFiltered = !isIFiltered; break;
            }
        }

        // 버튼의 배경 변경
        if (targetButton != null) {
            if (isFiltered) {
                targetButton.setBackgroundResource(R.drawable.minisel); // 비활성화 스타일
            } else {
                targetButton.setBackgroundResource(R.drawable.miniunsel); // 활성화 스타일
            }
        }

        // 필터링 적용
        if (!isFiltered) {
            for (Recipe recipe : sourceList) {
                if (recipe.getHashtag().contains(hashtag)) {
                    filteredList.add(recipe);
                }
            }
        } else {
            filteredList.addAll(sourceList); // 필터링 해제 시 원래 리스트 복원
        }

        // 어댑터에 필터링된 리스트 적용
        if (isRecipe) {
            recipeAdapter.setItems(filteredList);
        } else {
            helpAdapter.setItems(filteredList);
        }
    }


    // 레시피 검색 기능
    private void filterRecipes(String searchText) {
        List<Recipe> filteredRecipeList = new ArrayList<>();
        List<Recipe> filteredHelpList = new ArrayList<>();

        // isrecipe가 1인 레시피 검색
        for (Recipe recipe : recipeList) {
            if (recipe.getContents().toLowerCase().contains(searchText)) {
                filteredRecipeList.add(recipe);
            }
        }

        // isrecipe가 0인 레시피 검색
        for (Recipe recipe : helpList) {
            if (recipe.getContents().toLowerCase().contains(searchText)) {
                filteredHelpList.add(recipe);
            }
        }

        // 어댑터에 검색 결과 적용
        if (recipeAdapter != null) {
            recipeAdapter.setItems(filteredRecipeList);
        }
        if (helpAdapter != null) {
            helpAdapter.setItems(filteredHelpList);
        }
    }

    // 버튼 클릭 이벤트 처리
    private void setupButtons() {
        ImageButton recipeButton = findViewById(R.id.btngotophoto2);
        recipeButton.setOnClickListener(v -> {
            CustomDialog customDialog = new CustomDialog(RecipeActivity.this);
            customDialog.show();
        });
    }

    // 프로필 이미지 불러오기
    private void loadImageProfile() {
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();

            // 사용자 정보 가져오기 (프로필 이미지)
            mDatabaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String profileImageUrl = dataSnapshot.child("profileImage").getValue(String.class);

                        // 프로필 이미지 설정
                        if (profileImageUrl != null) {
                            Glide.with(RecipeActivity.this)
                                    .load(profileImageUrl)  // Firebase에서 가져온 URL
                                    .into(profileImage);  // ImageView에 로드
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(RecipeActivity.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "로그인되지 않은 사용자입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 사용자 정보 불러오기
    private void loadUserInfo() {
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            mDatabaseRef.child(userId).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String nickname = dataSnapshot.getValue(String.class);
                    if (nickname != null) {
                        greed.setText(nickname + "님");
                        Nickname = nickname;
                    } else {
                        greed.setText("닉네임을 설정해주세요.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(RecipeActivity.this, "닉네임을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // 로그인되지 않은 경우 로그인 화면으로 이동
            Intent intent = new Intent(RecipeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}