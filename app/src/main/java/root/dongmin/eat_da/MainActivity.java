package root.dongmin.eat_da;

import android.Manifest;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.adapter.NeedPostAdapter;
import root.dongmin.eat_da.adapter.PostAdapter;
import root.dongmin.eat_da.data.PostLocation;
import root.dongmin.eat_da.data.PostLocationResponseWrapper;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.NearbyPostResponse;
import root.dongmin.eat_da.network.NeedPost;
import root.dongmin.eat_da.network.NeedPostResponseWrapper;
import root.dongmin.eat_da.network.Post;
import root.dongmin.eat_da.network.RetrofitClient;
import root.dongmin.eat_da.utils.SpaceItemDecoration;

import android.os.Build;
import android.os.Handler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // 위치 권한
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    public String Nickname;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private TextView greed, greed2, levelBig, levelSmall, tradeNum, zacksungGesigul, zori, bezori, instant; // 사용자 환영 메시지
    private ProgressBar progressBar;
    private RecyclerView recyclerView, needrecyclerView;
    private PostAdapter postAdapter;
    private NeedPostAdapter needPostAdapter;
    private ApiService apiService;
    private boolean isNearbyActive = false; // "근처 게시물 보기" 상태 여부
    private List<Post> allPosts = new ArrayList<>(); // 원래 전체 게시글 저장용
    private List<String> chatList = new ArrayList<>();
    private List<NeedPost> needPosts;
    private EditText search;
    private ImageView profileImage;
    private boolean zorifilter, bezorifilter, instantfilter = false;

    private int space;
    public int myTradeCount = 0;

    private FusedLocationProviderClient fusedLocationClient; // 위치 서비스 객체 추가

    private BottomNavigationView bottomNavigationView;

    public List<PostLocation> postLocations;



    // 1초에 한 번씩 loadChatList()를 호출
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            loadChatList();
            handler.postDelayed(this, 1000); // 1초 후에 다시 실행
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this) // ✅ 다이얼로그 띄우기
                        .setTitle("앱 종료")
                        .setMessage("정말 종료하시겠습니까?")
                        .setPositiveButton("확인", (dialogInterface, which) -> finish()) // 🔴 앱 종료
                        .setNegativeButton("취소", null) // 취소 버튼 클릭 시 아무 동작 없음
                        .show();

                // "확인" 버튼의 텍스트 색을 검정으로 설정
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

                // "취소" 버튼의 텍스트 색을 검정으로 설정
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
            }
        });




        loadChatList();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            private int previousItemId = R.id.nav_home; // 초기 선택된 아이콘 (homeclicked 상태)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (previousItemId == item.getItemId()) {
                    return false; // 동일한 아이템 클릭 방지
                }

                if (item.getItemId() == R.id.nav_home) {
                    shareData();
                    Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    shareData();
                    Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
                    intent.putParcelableArrayListExtra("needPostList", new ArrayList<>(needPosts));

                    startActivity(intent);

                    overridePendingTransition(0, 0); // 전환 애니메이션 제거

                    finish();
                    return true;
                }else if (item.getItemId() == R.id.chat) {
                    shareData();
                    Intent intent = new Intent(MainActivity.this, UserFindActivity.class);
                    intent.putStringArrayListExtra("chatList", new ArrayList<>(chatList)); // 리스트 전달
                    intent.putParcelableArrayListExtra("needPostList2", new ArrayList<>(postLocations)); // 이거 혹시
                    intent.putParcelableArrayListExtra("needPostList", new ArrayList<>(needPosts));
                    intent.putExtra("nickname", Nickname);

                    startActivity(intent);

                    overridePendingTransition(0, 0); // 전환 애니메이션 제거

                    finish();
                    return true;
                }else if (item.getItemId() == R.id.work_load){
                    shareData();
                    Intent intent = new Intent(MainActivity.this,MapActivity.class);
                    intent.putParcelableArrayListExtra("needPostList", new ArrayList<>(needPosts)); // 리스트 전달
                    setIntent(intent);

                    startActivity(intent);

                    overridePendingTransition(0, 0); // 전환 애니메이션 제거

                    finish();
                    return true;
                }else if (item.getItemId() == R.id.recipe){
                    Intent intent = new Intent(MainActivity.this,RecipeActivity.class);
                    intent.putParcelableArrayListExtra("needPostList", new ArrayList<>(needPosts));

                    startActivity(intent);

                    overridePendingTransition(0, 0); // 전환 애니메이션 제거

                    finish();
                    return true;
                }
                return false;
            }
        });




        // 위치 서비스 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //  위치 권한 확인 및 요청, 저장소도
        checkLocationPermission();
        checkBoxPermission();

        // Firebase 및 UI 요소 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");
        greed = findViewById(R.id.greeding);
        greed2 = findViewById(R.id.whatisname);
        levelBig = findViewById(R.id.levelhowmuch);
        levelSmall = findViewById(R.id.levelhowmuch2);
        tradeNum = findViewById(R.id.gureCount);
        progressBar = findViewById(R.id.progressBar);
        profileImage = findViewById(R.id.profileImage);
        zacksungGesigul = findViewById(R.id.bookCount);
        zori = findViewById(R.id.TextView_msgegg);
        bezori = findViewById(R.id.textView_msgfruitvega);
        instant = findViewById(R.id.textView_msgseed);
        search = findViewById(R.id.searchPost);


        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView);

        space = 25;

        // SpaceItemDecoration 인스턴스 생성
        SpaceItemDecoration itemDecoration = new SpaceItemDecoration(space);

        // RecyclerView에 itemDecoration 적용
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        needrecyclerView = findViewById(R.id.recyclerNeedView);

        // RecyclerView에 itemDecoration 적용
        needrecyclerView.addItemDecoration(itemDecoration);

        needrecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        zori.setOnClickListener(v -> {

            if(zorifilter)
            {
                zori.setBackgroundResource(R.drawable.minisel);
                zorifilter = false;
                filterPosts2(zorifilter,bezorifilter,instantfilter);
            }
            else {
                zori.setBackgroundResource(R.drawable.miniunsel);
                zorifilter = true;
                filterPosts2(zorifilter,bezorifilter,instantfilter);
            }
        });
        bezori.setOnClickListener(v -> {

            if(bezorifilter)
            {
                bezori.setBackgroundResource(R.drawable.minisel);
                bezorifilter = false;
                filterPosts2(zorifilter,bezorifilter,instantfilter);
            }
            else {
                bezori.setBackgroundResource(R.drawable.miniunsel);
                bezorifilter = true;
                filterPosts2(zorifilter,bezorifilter,instantfilter);
            }
        });
        instant.setOnClickListener(v -> {

            if(instantfilter)
            {
                instant.setBackgroundResource(R.drawable.minisel);
                instantfilter = false;
                filterPosts2(zorifilter,bezorifilter,instantfilter);
            }
            else {
                instant.setBackgroundResource(R.drawable.miniunsel);
                instantfilter = true;
                filterPosts2(zorifilter,bezorifilter,instantfilter);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                //String searchText = s.toString().toLowerCase(); // 입력된 검색어 가져오기
                //filterPosts(searchText, zorifilter, bezorifilter, instantfilter); // 검색 함수 호출
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString().toLowerCase(); // 입력된 검색어 가져오기
                filterPosts(searchText, zorifilter, bezorifilter, instantfilter); // 검색 함수 호출
            }
        });




        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);

        // 사용자 정보 및 게시글 불러오기
        loadUserInfo();
        loadUserLevel();
        handler.post(runnable);
        loadPosts();
        loadNeedPosts();
        loadImageProfile();
        loadPostLocations();

        // 버튼 이벤트 처리
        setupButtons();
        //데이터 공유하기
        shareData();
    }

    // ✅ 저장소 권한 확인 및 요청
    private void checkBoxPermission()
    {
        // 저장소 권한 체크 및 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        }
    }



    // ✅ 위치 권한 확인 및 요청
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // ✅ 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "위치 권한 허용됨");
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다. 기능이 제한됩니다.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // ✅ 일단 전체 채팅 리스트 로드시켜 놓기.
    private void loadChatList() {
        //chatList = new ArrayList<>();
        // chatList가 null이 아니면 비워주기
        if (chatList != null) {
            chatList.clear(); // 기존 항목 모두 제거
        } else {
            chatList = new ArrayList<>(); // 만약 null이라면 새로 초기화
        }

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("chat");

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Toast.makeText(MainActivity.this, "채팅접근중.", Toast.LENGTH_SHORT).show();
                //Log.d("ChatData", "Children count: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String chatId = snapshot.getKey(); // 최상위 키(채팅 ID) 가져오기
                    if (chatId != null) {
                        chatList.add(chatId);
                    }
                }

                handleChatList(chatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "채팅 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleChatList(List<String> chatList) {
        for (String chat : chatList) {
            //Log.d("ChatData", chat);
        }
        //그리고 이걸 보내야 한다...!!!!
    }



    // ✅ 사용자 닉네임 가져오기
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
                        greed2.setText(nickname);
                        Nickname = nickname;
                    } else {
                        greed.setText("닉네임을 설정해주세요.");
                        greed2.setText("_");
                        greed.setOnClickListener(v -> {
                            Intent intent = new Intent(MainActivity.this, NicknameActivity.class);
                            startActivity(intent);
                        });
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
    // ✅ 사용자 레벨 가져오기
    private void loadUserLevel() {
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            // transactionCount 값을 가져오기
            mDatabaseRef.child(userId).child("transactionCount").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Integer transactionCount = dataSnapshot.getValue(Integer.class);

                    if (transactionCount != null) {
                        // 레벨 계산
                        int level = transactionCount / 5; // 레벨 (5로 나눈 몫)
                        int remainder = transactionCount % 5; // 남은 거래 횟수 (5로 나눈 나머지)
                        Log.d("API_DEBUG", "레벨 트랜직션카운트(오리지널): " + transactionCount);
                        Log.d("API_DEBUG", "레벨: " + level);
                        Log.d("API_DEBUG", "남은 거래 횟수: " + remainder);




                        // levelBig에 텍스트 설정
                        //levelBig = findViewById(R.id.levelhowmuch);
                        levelBig.setText("거래 " + (5-remainder) + "번 더 하면 레벨 업이에요!");

                        // levelSmall에 텍스트 설정
                        //levelSmall = findViewById(R.id.levelhowmuch2);
                        levelSmall.setText(level + "Lv");

                        // tradeNum에 거래 횟수 설정
                        tradeNum.setText(String.valueOf(transactionCount));

                        // ProgressBar에 진행 상태 설정
                        //progressBar = findViewById(R.id.progressBar);
                        progressBar.setProgress(remainder); // 나머지 값을 progress로 설정
                    } else {
                        // transactionCount가 null인 경우 기본값 설정
                        //levelBig = findViewById(R.id.levelhowmuch);
                        levelBig.setText("거래 0번 더 하면 레벨 업이에요..");

                        //levelSmall = findViewById(R.id.levelhowmuch2);
                        levelSmall.setText("0Lv..");

                        tradeNum.setText("0..");

                        // ProgressBar 초기화
                        //progressBar = findViewById(R.id.progressBar);
                        progressBar.setProgress(0); // 기본값 0으로 설정
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "거래 횟수를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // 로그인되지 않은 경우 로그인 화면으로 이동
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // ✅ 게시글 목록 불러오기
    private void loadPosts() {
        Call<List<Post>> call = apiService.getPosts();
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allPosts = response.body(); // 기존 게시물 저장
                    //postList = allPosts;
                    postAdapter = new PostAdapter(MainActivity.this, allPosts);
                    recyclerView.setAdapter(postAdapter);
                    //✅ 추가로 거래횟수 로드 여기서 할게요
                    loadtradeCount(allPosts);

                } else {
                    showErrorMessage("게시글을 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                showErrorMessage("네트워크 오류로 게시글을 불러올 수 없습니다.");
            }
        });
    }

    // 필요 게시물 목록 불러오기
    private void loadNeedPosts() {
        Call<NeedPostResponseWrapper> call = apiService.getNeedPosts();
        call.enqueue(new Callback<NeedPostResponseWrapper>() {
            @Override
            public void onResponse(@NonNull Call<NeedPostResponseWrapper> call, @NonNull Response<NeedPostResponseWrapper> response) {
                Log.d("API_RESPONSE", "HTTP Status Code: " + response.code());
                Log.d("API_RESPONSE", "Response Message: " + response.message());

                // ✅ 추가한 디버깅 코드
                if (response.body() == null) {
                    Log.e("API_DEBUG", "response.body()가 null입니다.");
                } else {
                    String jsonResponse = new Gson().toJson(response.body());
                    Log.d("API_DEBUG", "Response Body JSON: " + jsonResponse);

                    if (response.body().getNeedPosts() == null) {
                        Log.e("API_DEBUG", "getNeedPosts()가 null입니다.");
                    } else {
                        Log.d("API_DEBUG", "Need Posts List Size: " + response.body().getNeedPosts().size());
                    }
                }

                if (response.body() != null && response.body().getNeedPosts() != null) {

                    needPosts = response.body().getNeedPosts();
                    needPostAdapter = new NeedPostAdapter(MainActivity.this, needPosts);
                    Log.d("API_RESPONSE", "Sending NeedPost List: " + needPosts.toString());
                    needrecyclerView.setAdapter(needPostAdapter);


                    Log.d("MAP_DEBUG", "🚀 needPostList를 전달하기 직전: " + needPosts.toString());


                    if (needPosts.isEmpty()) {
                        Log.d("API_RESPONSE", "NeedPost List is empty.");

                    } else {
                        Log.d("API_RESPONSE", "NeedPost List: " + needPosts.toString());
                        for (NeedPost post : needPosts) {
                            Log.d("API_RESPONSE", "Post ID: " + post.getPostID());
                            Log.d("API_RESPONSE", "Contents: " + post.getContents());
                            Log.d("API_RESPONSE", "Nickname: " + post.getNickname());
                            Log.d("API_RESPONSE", "Latitude: " + post.getLatitude());
                            Log.d("API_RESPONSE", "Longitude: " + post.getLongitude());
                        }
                    }

                } else {
                    Log.d("API_RESPONSE", "Response Body or NeedPost List is null");
                    showErrorMessage("필요 게시글을 불러올 수 없습니다.");
                    needPostAdapter = new NeedPostAdapter(MainActivity.this, new ArrayList<>()); // 빈 리스트로 초기화
                    needrecyclerView.setAdapter(needPostAdapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<NeedPostResponseWrapper> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "네트워크 오류: " + t.getMessage());
                showErrorMessage("네트워크 오류로 필요 게시글을 불러올 수 없습니다.");

                // 네트워크 오류 발생 시 RecyclerView에 빈 리스트 적용
                needPostAdapter = new NeedPostAdapter(MainActivity.this, new ArrayList<>());
                needrecyclerView.setAdapter(needPostAdapter);
            }
        });
    }

    // 지금까지 포스트된 위치 받기(나눔하는사람 포스트임)
    private void loadPostLocations() {
        Call<PostLocationResponseWrapper> call = apiService.getPostLocations();
        call.enqueue(new Callback<PostLocationResponseWrapper>() {
            @Override
            public void onResponse(@NonNull Call<PostLocationResponseWrapper> call, @NonNull Response<PostLocationResponseWrapper> response) {
                Log.d("API_RESPONSE", "HTTP Status Code: " + response.code());
                Log.d("API_RESPONSE", "Response Message: " + response.message());

                if (response.body() == null) {
                    Log.e("API_DEBUG", "response.body()가 null입니다.");
                } else {
                    String jsonResponse = new Gson().toJson(response.body());
                    Log.d("API_DEBUG", "Response Body JSON: " + jsonResponse);

                    if (response.body().getPostLocations() == null) {
                        Log.e("API_DEBUG", "getPostLocations()가 null입니다.");
                    } else {
                        Log.d("API_DEBUG", "Post Locations List Size: " + response.body().getPostLocations().size());
                    }
                }

                if (response.body() != null && response.body().getPostLocations() != null) {
                    postLocations = response.body().getPostLocations();
                    Log.d("API_RESPONSE", "PostLocation List: " + postLocations.toString());

                    for (PostLocation post : postLocations) {
                        Log.d("API_RESPONSE", "Post ID: " + post.getPostID());
                        Log.d("API_RESPONSE", "Latitude: " + post.getLatitude());
                        Log.d("API_RESPONSE", "Longitude: " + post.getLongitude());
                    }
                } else {
                    Log.d("API_RESPONSE", "Response Body or PostLocation List is null");
                    showErrorMessage("위치 데이터를 불러올 수 없어.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostLocationResponseWrapper> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "네트워크 오류: " + t.getMessage());
                showErrorMessage("네트워크 오류로 데이터를 불러올 수 없어 ㅅ뷰ㅠㅠㅠㅠ.");
            }
        });
    }



    // ✅ 근처 게시글 불러오기
    private void loadNearbyPosts(double latitude, double longitude) {
        double radius = 5.0; // 반경 5km
        Call<NearbyPostResponse> call = apiService.getNearbyPosts(latitude, longitude, radius);

        Log.d("Upload", "API 요청 URL: " + call.request().url());

        call.enqueue(new Callback<NearbyPostResponse>() {
            @Override
            public void onResponse(@NonNull Call<NearbyPostResponse> call, @NonNull Response<NearbyPostResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Post> postList = response.body().getPosts();
                    Log.d("Upload", "서버 응답 데이터: " + new Gson().toJson(postList));
                    postAdapter = new PostAdapter(MainActivity.this, postList);
                    recyclerView.setAdapter(postAdapter);
                } else {
                    Log.e("Upload", "근처 게시물 불러오기 실패: 위도 :"+ latitude + " 경도:" + longitude + " 거리: " + radius);
                    showErrorMessage("근처 게시글을 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<NearbyPostResponse> call, @NonNull Throwable t) {
                showErrorMessage("네트워크 오류로 게시글을 불러올 수 없습니다.");
            }
        });
    }


    // ✅ 근처 게시글 보기 토글 기능
    private void toggleNearbyPosts(SwitchCompat nearbySwitch) {
        if (isNearbyActive) {
            // 📌 토글 OFF: 원래 게시글 목록 복원
            isNearbyActive = false;
            nearbySwitch.setText("근처 게시글 보기");

            postAdapter = new PostAdapter(MainActivity.this, allPosts);
            recyclerView.setAdapter(postAdapter);
        } else {
            // 📌 토글 ON: 현재 위치 가져와서 근처 게시글 불러오기
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                isNearbyActive = true;
                                nearbySwitch.setText("근처 게시글 취소");

                                loadNearbyPosts(latitude, longitude);
                            } else {
                                Toast.makeText(MainActivity.this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // 권한이 없는 경우 다시 요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }




    // ✅ 버튼 클릭 이벤트 처리
    private void setupButtons() {
        ImageButton photobutton = findViewById(R.id.btngotophoto);
        photobutton.setOnClickListener(v -> {
            CustomDialog customDialog = new CustomDialog(MainActivity.this);
            customDialog.show();
        });



//        Button findUserButton = findViewById(R.id.btnFindUser);
//        findUserButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, UserFindActivity.class)));

        SwitchCompat nearButton = findViewById(R.id.btnNearby);
        Drawable trackDrawable = ContextCompat.getDrawable(this, R.drawable.track);
        // nearButton에 적용
        nearButton.setTrackDrawable(trackDrawable);

        Drawable thumbDrawable = ContextCompat.getDrawable(this,R.drawable.thumb);
        // nearButton에 적용
        nearButton.setThumbDrawable(thumbDrawable);


        nearButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleNearbyPosts(nearButton);
        });



//        findUserButton.setOnClickListener(view -> {
//            Intent intent = new Intent(MainActivity.this, UserFindActivity.class);
//            intent.putStringArrayListExtra("chatList", new ArrayList<>(chatList)); // 리스트 전달
//            intent.putExtra("nickname", Nickname);
//            startActivity(intent);
//        });

    }

    // ✅ 오류 메시지 출력
    private void showErrorMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }


    // ✅ 데이터 공유하는 함수니까 이건 잠시 보류해도 될듯
    public void shareData() {
        // SharedPreferences에 데이터 저장
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        // chatList가 null이 아닌지 확인
        if (chatList != null) {
            String json = gson.toJson(chatList);
            editor.putString("chatListJson", json);
        } else {
            Log.e("shareData", "잠깐 chatListJson이 NULL이얌");
        }

        // Nickname이 null이 아닌지 확인
        if (Nickname != null) {
            editor.putString("Nickname", Nickname);
        } else {
            Log.e("shareData", "잠깐 Nickname이 NULL이얌");
        }

        // postLocations가 null이 아닌지 확인
        if (postLocations != null) {
            String jsonL = gson.toJson(postLocations);
            editor.putString("post_locations", jsonL);
        } else {
            Log.e("shareData", "잠깐 post_locations이 NULL이얌");
        }

        // 변경사항 저장
        editor.apply();
    }

    // ✅ 프로필 들고오는 코드
    public void loadImageProfile()
    {

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
                            //ImageView profileImage = findViewById(R.id.profileImage);  // XML에서 정의한 ImageView
                            Glide.with(MainActivity.this)
                                    .load(profileImageUrl)  // Firebase에서 가져온 URL
                                    .into(profileImage);  // ImageView에 로드
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "로그인되지 않은 사용자입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ 거래횟수 로드(계시글 목록 불러오는 함수랑 연동됨)
    public void loadtradeCount(List<Post> posta)
    {
        for(Post pp:posta)
        {
            if(pp.getNickname().equals(Nickname) )
            {
                myTradeCount+=1;
            }

        }
        zacksungGesigul.setText(String.valueOf(myTradeCount));
    }
    // 게시물 검색기능
    private void filterPosts(String searchText, boolean zorifilter, boolean bezorifilter, boolean instantfilter) {
        List<Post> filteredList = new ArrayList<>();

        if(allPosts == null || allPosts.isEmpty()) return; // null 체크

        for (Post post : allPosts) {
            if (!(post.getContents().toLowerCase().contains(searchText) ||
                    post.getIngredients().toLowerCase().contains(searchText)))
            {
                continue;
            }

            List<String> hashtags = Arrays.asList(post.getHashtag().split("_"));

            if(zorifilter == true)
            {
                if (!(zorifilter && hashtags.contains("조리"))) {
                    continue;
                }
            }
            if(bezorifilter == true)
            {
                if (!(bezorifilter && hashtags.contains("비조리"))) {
                    continue;
                }
            }
            if(instantfilter == true)
            {
                if (!(instantfilter && hashtags.contains("인스턴트"))) {
                    continue;
                }
            }


            filteredList.add(post);
        }

        if (postAdapter != null) {
            postAdapter.setItems(filteredList); // 검색된 리스트 적용
        }
    }
    private void filterPosts2(boolean zorifilter, boolean bezorifilter, boolean instantfilter) {
        List<Post> filteredList = new ArrayList<>();

        if (allPosts == null || allPosts.isEmpty()) return; // null 체크

        for (Post post : allPosts) {

            List<String> hashtags = Arrays.asList(post.getHashtag().split("_"));

            if(zorifilter == true)
            {
                if (!(zorifilter && hashtags.contains("조리"))) {
                    continue;
                }
            }
            if(bezorifilter == true)
            {
                if (!(bezorifilter && hashtags.contains("비조리"))) {
                    continue;
                }
            }
            if(instantfilter == true)
            {
                if (!(instantfilter && hashtags.contains("인스턴트"))) {
                    continue;
                }
            }
            filteredList.add(post);
        }

        if (postAdapter != null) {
            postAdapter.setItems(filteredList); // 검색된 리스트 적용
        }
    }

    // 게시물 검색기능이랑 양대산맥을 이루는 조리,비조리,인스턴트 검색기능(태그)

}
