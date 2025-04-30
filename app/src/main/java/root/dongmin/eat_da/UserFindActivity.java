package root.dongmin.eat_da;



import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import root.dongmin.eat_da.adapter.ChatRoomAdapter;
import root.dongmin.eat_da.data.PostLocation;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.NeedPost;
import root.dongmin.eat_da.network.Post;
import root.dongmin.eat_da.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import root.dongmin.eat_da.data.User;
import android.view.MenuItem;

public class UserFindActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;
    private RecyclerView chatRoomRecyclerView;
    private ArrayList<String> chatRoomList;
    private ChatRoomAdapter adapter;
    private List<Post> posts;

    private String nickname;
    private List<String> chatList;

    private List<NeedPost> needPosts = new ArrayList<>(); // 거리 리스트!


    ArrayList<PostLocation> postLocationList;// 거리 리스트!

    // API 서비스 객체
    private ApiService apiService;

    private Button leftButton;
    private Button rightButton;
    private TextView readD;
    private TextView notReadD;
    private int isnotMine = 3; // 기본값 0
    private int ifnotRead = 0;
    private boolean isRunning = true; // Runnable 실행 여부를 제어하는 플래그

    public String lastMessage = null; //마지막에 한 채팅내용 가져오는것

    int ischanged = 1;//좌우가 바뀌었는지?

    int aa = 0;
    private boolean isLoading = true; // 데이터 로드 상태를 나타내는 플래그




    private Handler handler = new Handler(); // Handler 객체 생성
    private Runnable runnable; // Runnable 객체 생성


    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double currentLatitude = 0.0; // 현재 위도
    private double currentLongitude = 0.0; // 현재 경도
    private static final double EARTH_RADIUS = 6371.0; // 지구 반지름 (단위: km)

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // 위치 권한 요청 코드
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_find);

        // BottomNavigationView 초기화 및 설정
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(R.id.chat);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            private int previousItemId = R.id.chat; // 초기 선택된 아이콘

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (previousItemId == item.getItemId()) {
                    return false; // 동일한 아이템 클릭 방지
                }

                if (item.getItemId() == R.id.chat) {
                    Toast.makeText(UserFindActivity.this, "Chat", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    Intent intent = new Intent(UserFindActivity.this, MyPageActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.nav_home) {
                    // MainActivity로 이동
                    isRunning = false; // 실행 중지 플래그 설정
                    if (handler != null) {
                        handler.removeCallbacks(runnable); // Runnable 제거
                    }
                    Intent intent = new Intent(UserFindActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.work_load) {
                    Intent intent = new Intent(UserFindActivity.this, MapActivity.class);
                    intent.putParcelableArrayListExtra("needPostList", new ArrayList<>(needPosts)); // 리스트 전달
                    startActivity(intent);
                    finish();
                    return true;
                }else if (item.getItemId() == R.id.recipe){
                    Intent intent = new Intent(UserFindActivity.this,RecipeActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });



        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 위치 권한 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 이미 허용된 경우 위치 정보 가져오기
            initializeLocation();
        }

        // Retrofit API 초기화
        apiService = RetrofitClient.getApiService(this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        // 뷰 초기화
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        readD = findViewById(R.id.readD);
        notReadD = findViewById(R.id.notReadD);

        // "전체" TextView 클릭 리스너 설정
        readD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TextViewClick", "전체를 클릭했습니다.");
                ifnotRead = 0;

                if (isnotMine == 0) {
                    adapter.setIsNotMine(isnotMine);
                    selectLeftButton();
                } else if (isnotMine == 1) {
                    adapter.setIsNotMine(isnotMine);
                    selectLeftButton();
                }
            }
        });

        // "읽지않음" TextView 클릭 리스너 설정
        notReadD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TextViewClick", "읽지않음을 클릭했습니다.");
                ifnotRead = 1;

                if (isnotMine == 0) {
                    adapter.setIsNotMine(isnotMine);
                    selectLeftButton();
                } else if (isnotMine == 1) {
                    adapter.setIsNotMine(isnotMine);
                    selectLeftButton();
                }
            }
        });

        // 왼쪽 버튼 클릭 리스너 설정
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isnotMine = 0; // 왼쪽 버튼 클릭 시 0으로 설정
                Log.d("UserFind", "isnotMine value: " + isnotMine);
                adapter.setIsNotMine(isnotMine);
                selectLeftButton();
            }
        });

        // 오른쪽 버튼 클릭 리스너 설정
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isnotMine = 1; // 오른쪽 버튼 클릭 시 1로 설정
                Log.d("UserFind", "isnotMine value: " + isnotMine);
                adapter.setIsNotMine(isnotMine);
                selectRightButton();
            }
        });

        // Intent로 받은 채팅 리스트 가져오기
        Intent intent = getIntent();
        nickname = getIntent().getStringExtra("nickname");
        String chatRoom = getIntent().getStringExtra("chatRoom");
        int isnotMinea = getIntent().getIntExtra("isnotMinea", 2);


        if(nickname == null)
        {
            Log.e("MAP_DEBUG", "아니 닉네임 못받음 뭐임??????????");
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            nickname = sharedPreferences.getString("Nickname", null);
            if (nickname != null) {
                Log.e("UserFindActivity", "Nickname 다시 불러오기 성공: " + nickname);
                // 불러온 Nickname 사용
            } else {
                Log.e("UserFindActivity", "Nickname이 여전히 NULL이얌");
            }
        }
        if (chatRoom != null) {
            // 전달된 채팅방 정보를 사용하여 명령어 처리
            handleChatRoomAction(chatRoom, isnotMinea);
        }
        else {
            Log.e("MAP_DEBUG", "아니 chatRoom 못받음 뭐임??????????");
            mainloadChatList();
            handleChatRoomAction(chatRoom, isnotMinea);
        }

        // 채팅방의 경도, 위도 정보 가져오기
        postLocationList = getIntent().getParcelableArrayListExtra("needPostList2");
        if (postLocationList != null) {
            for (PostLocation postLocation : postLocationList) {
                Log.d("MAP_DEBUG", "Post ID_userfind: " + postLocation.getPostID());
                Log.d("MAP_DEBUG", "Latitude_userfind: " + postLocation.getLatitude());
                Log.d("MAP_DEBUG", "Longitude_userfind: " + postLocation.getLongitude());
            }
        } else {
            Log.e("MAP_DEBUG", "PostLocation 리스트 노노....");
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            Gson gson = new Gson();
            String postLocationsJson = sharedPreferences.getString("post_locations", null);
            if (postLocationsJson != null) {
                // JSON 문자열을 List<PostLocation>으로 변환
                Type postLocationsType = new TypeToken<List<PostLocation>>() {}.getType();
                postLocationList = gson.fromJson(postLocationsJson, postLocationsType);

                if (postLocationList != null) {
                    Log.d("loadData", "postLocations 불러오기 성공: " + postLocationList.size() + "개의 항목");
                }
                else
                {
                    Log.e("loadData", "postLocations이 NULL즉 안가져와짐 ㅅㄱ");
                }
            }
            else
            {
                Log.e("loadData", "post_locations 제이슨이 안가져와짐 ㅅㄱ");
            }






        }

        // RecyclerView 초기화
        chatRoomRecyclerView = findViewById(R.id.chatRoomRecyclerView);
        chatRoomRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 채팅방 리스트 및 어댑터 설정
        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(chatRoomList);
        chatRoomRecyclerView.setAdapter(adapter);

        // 채팅 리스트 및 게시글 로드
        loadChatList(new OnChatListLoadedListener() {
            @Override
            public void onChatListLoaded(List<String> chatList) {
                loadPosts(new OnPostsLoadedListener() {
                    @Override
                    public void onPostsLoaded(List<Post> posts) {
                        initializeLocation();
                        getUserLocation();
                        aa = 0;
                        updateRecyclerView(chatList, nickname, posts, aa); // 기본값으로 0번으로 로드
                    }
                });
            }
        });

        // 버튼 초기 상태 설정
        defaultButton();

        // Runnable 초기화 (주기적으로 데이터 로드)
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning || isDestroyed() || isFinishing()) {
                    return; // 액티비티가 종료됐거나 실행 중이 아니면 중단
                }

                if (!isLoading) {
                    isLoading = true; // 로드 상태를 true로 설정
                    loadChatList(new OnChatListLoadedListener() {
                        @Override
                        public void onChatListLoaded(List<String> chatList) {
                            loadPosts(new OnPostsLoadedListener() {
                                @Override
                                public void onPostsLoaded(List<Post> posts) {
                                    if (chatList != null) {
                                        updateRecyclerView(chatList, nickname, posts, aa);
                                        Log.d("MAP_DEBUG", "0.5초마다 데이터 로드 및 업데이트");
                                    }
                                    isLoading = false; // 로드 상태를 false로 설정
                                }
                            });
                        }
                    });
                }

                // 0.5초 후에 다시 실행 (isRunning 체크 추가)
                if (isRunning) {
                    handler.postDelayed(this, 500);
                }
            }
        };

// Runnable 시작
        handler.post(runnable);


        // 뒤로가기 이벤트 처리
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(UserFindActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

//-------------------------------------------------------------위치-----------------------------------------------------------------
//-------------------------------------------------------------위치-----------------------------------------------------------------
//-------------------------------------------------------------위치-----------------------------------------------------------------
    private void initializeLocation() {
        createLocationRequest();
        createLocationCallback();
        getUserLocation(); // 현재 위치 가져오기
        startLocationUpdates(); // 지속적인 위치 업데이트 시작
    }//위치로드 종합 ㄱㄱ

    // 위치 요청 설정
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY); // 높은 정확도
        locationRequest.setInterval(10000); // 10초마다 업데이트
        locationRequest.setFastestInterval(5000); // 최소 5초마다 업데이트
    }


    // 위치 업데이트 콜백 설정
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // 위치가 업데이트될 때마다 위도와 경도 저장
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    Log.d("LOCATION_UPDATE", "업데이트된 위치: " + currentLatitude + ", " + currentLongitude);
                }
            }
        };
    }

    // 현재 위치 가져오기 (단일 요청)
    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // 현재 위치를 변수에 저장
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            Log.d("LOCATION_DEBUG", "현재 위치: " + currentLatitude + ", " + currentLongitude);
                        } else {
                            Log.e("LOCATION_ERROR", "위치 정보를 가져올 수 없습니다.");
                        }
                    }
                });
    }

    // 지속적인 위치 업데이트 시작
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    // 위치 업데이트 중지
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우 위치 정보 가져오기
                initializeLocation();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // 액티비티가 다시 시작될 때 위치 업데이트 시작
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
        isRunning = true; // 실행 상태를 true로 설정
        if (handler != null && runnable != null) {
            handler.post(runnable); // Runnable 다시 실행
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 액티비티가 일시정지되면 위치 업데이트 중지
        stopLocationUpdates();
        isRunning = false; // 실행 중지 플래그 설정
        if (handler != null) {
            handler.removeCallbacks(runnable); // Runnable 제거
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false; // 실행 중지 플래그 설정
        if (handler != null) {
            handler.removeCallbacks(runnable); // Runnable 제거
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
        if (handler != null) {
            handler.removeCallbacks(runnable); // Runnable 제거
        }
    }
//-------------------------------------------------------------위치-----------------------------------------------------------------
//-------------------------------------------------------------위치-----------------------------------------------------------------
//-------------------------------------------------------------위치-----------------------------------------------------------------




    private void loadChatList(OnChatListLoadedListener listener) {
        chatList = new ArrayList<>();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("chat");

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String chatId = snapshot.getKey();
                    if (chatId != null) {
                        chatList.add(chatId);
                    }
                }
                listener.onChatListLoaded(chatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserFindActivity.this, "채팅 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPosts(OnPostsLoadedListener listener) {
        Call<List<Post>> call = apiService.getPosts();
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onPostsLoaded(response.body());
                } else {
                    Log.e("UserFindActivity", "게시글을 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.e("UserFindActivity", "네트워크 오류로 게시글을 불러올 수 없습니다.");
            }
        });
    }

    private void updateRecyclerView(List<String> chatList, String nickname, List<Post> posts,int a) {
        chatRoomList.clear();
        getUserLocation();
        isLoading = true;//혹시나 해서 함 넣어봄

        for (String chat : chatList) {
            String[] chatDetails = chat.split("_");
            //------------------------------------------------------------만약 왼쪽버튼 누르면
            if(a == 0)
            {

                if (chatDetails.length >= 2) {
                    String receivingPerson = chatDetails[1];
                    String receivingNum = chatDetails[0];//이걸 가지고 위도,경도 추측하고 내 위도,경도를 통해 거리를 얻는 함수를 만들기!
                    String showingPerson = chatDetails[2];
                    String zemok = "";

                    double LAa = 0;
                    double LOo = 0;
                    double distance = 0;

                    if (postLocationList != null) {

                        for (PostLocation postLocation : postLocationList) {
                            String postIdStr = String.valueOf(postLocation.getPostID());
                            if(postIdStr.equals(receivingNum))
                            {
                                LAa = postLocation.getLatitude();
                                LOo = postLocation.getLongitude();
                                distance = getDistance(LAa, LOo, currentLatitude, currentLongitude);
                                break;
                            }
                        }


                        if (receivingPerson.equals(nickname)) {
                            for (Post post : posts) {
                                if (receivingNum.equals(post.getPostID())) {
                                    zemok += post.getContents();
                                }
                            }
                            String mergechatList = showingPerson + ": " + zemok + "_" + receivingNum + "_" + distance + "|" + chat + "|" + ifnotRead;
                            chatRoomList.add(mergechatList);
                        }


                    }

                }
                //--------------------------------------------------------------만약 오른쪽버튼 누르면
            }
            else
            {
                //------------------------------------------------------------
                if (chatDetails.length >= 2) {
                    String receivingPerson = chatDetails[1];
                    String receivingNum = chatDetails[0];
                    String showingPerson = chatDetails[2];
                    String zemok = "";

                    double LAa = 0;
                    double LOo = 0;
                    double distance = 0;
                    if (postLocationList != null) {

                        for (PostLocation postLocation : postLocationList) {
                            String postIdStr = String.valueOf(postLocation.getPostID());
                            if(postIdStr.equals(receivingNum))
                            {
                                LAa = postLocation.getLatitude();
                                LOo = postLocation.getLongitude();
                                distance = getDistance(LAa, LOo, currentLatitude, currentLongitude);
                                break;
                            }
                        }

                        if (showingPerson.equals(nickname)) {
                            for (Post post : posts) {
                                if (receivingNum.equals(post.getPostID())) {
                                    zemok += post.getContents();
                                }
                            }
                            String mergechatList = receivingPerson + ": " + zemok + "_" + receivingNum + "_" + distance+ "|" + chat+ "|" + ifnotRead;
                            chatRoomList.add(mergechatList);
                        }
                    }

                }
                //--------------------------------------------------------------
            }
        }
        adapter.notifyDataSetChanged();
        isLoading = false;
    }

    interface OnChatListLoadedListener {
        void onChatListLoaded(List<String> chatList);
    }

    interface OnPostsLoadedListener {
        void onPostsLoaded(List<Post> posts);
    }


    void defaultButton()
    {

        leftButton.setZ(10f); // 왼쪽 버튼이 위로 올라옴
        rightButton.setZ(5f);

        // 왼쪽 버튼 이동 애니메이션
        ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(leftButton, "translationX", 0f, 40f);
        moveAnimator.setDuration(400);

        // 오른쪽 버튼 이동 애니메이션 (왼쪽으로 약간 이동)
        ObjectAnimator moveAnimator2 = ObjectAnimator.ofFloat(rightButton, "translationX", 40f, 0f);
        moveAnimator2.setDuration(400);

        // 왼쪽 버튼 가로 크기 확대
        ValueAnimator leftWidthAnimator = ValueAnimator.ofInt(leftButton.getWidth(), leftButton.getWidth() + 200);
        leftWidthAnimator.setDuration(400);
        leftWidthAnimator.addUpdateListener(animation -> {
            int newWidth = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = leftButton.getLayoutParams();
            params.width = newWidth;
            leftButton.setLayoutParams(params);
        });

        // 오른쪽 버튼 가로 크기 축소
        ValueAnimator rightWidthAnimator = ValueAnimator.ofInt(rightButton.getWidth(), rightButton.getWidth() -0);
        rightWidthAnimator.setDuration(400);
        rightWidthAnimator.addUpdateListener(animation -> {
            int newWidth = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = rightButton.getLayoutParams();
            params.width = newWidth;
            rightButton.setLayoutParams(params);
        });

//        // 왼쪽 버튼 색상 변경 애니메이션 (민트색으로 변경)
//        int mintColor = Color.parseColor("#55eee0"); // 민트색
//        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.WHITE, mintColor);
//        colorAnimator.setDuration(1000);
//        colorAnimator.addUpdateListener(animation ->
//                leftButton.setBackgroundTintList(ColorStateList.valueOf((int) animation.getAnimatedValue()))
//
//        );



        rightButton.setBackgroundResource(R.drawable.rounded_button_no);
        leftButton.setBackgroundResource(R.drawable.rounded_button_yes);


        rightButton.setZ(5f); // 오른쪽 버튼이 아래로 내려감
        leftButton.setZ(10f); // 왼쪽 버튼이 위로 올라옴


        rightButton.setTextColor(Color.parseColor("#babac0")); // 또는 Color.parseColor("#babac0")
        leftButton.setTextColor(Color.parseColor("#FFFFFF")); // 또는 Color.parseColor("#000000")



        // 애니메이션 실행
        moveAnimator2.start();
        moveAnimator.start();


        rightWidthAnimator.start();
        leftWidthAnimator.start();
        //colorAnimator.start();
        ischanged = 0;

        rightButton.setZ(5f); // 오른쪽 버튼이 아래로 내려감
        leftButton.setZ(10f); // 왼쪽 버튼이 위로 올라옴


    }//버튼 기본값

    private void selectLeftButton() {
        if(ischanged == 1)
        {
            // 왼쪽 버튼 이동 애니메이션
            //ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(leftButton, "translationX", 0f, 30f);
            //moveAnimator.setDuration(1000);

            // 오른쪽 버튼 이동 애니메이션 (왼쪽으로 약간 이동)
            //ObjectAnimator moveAnimator2 = ObjectAnimator.ofFloat(rightButton, "translationX", 0f, 0f);
            //moveAnimator2.setDuration(1000);

            // 왼쪽 버튼 가로 크기 확대
            ValueAnimator leftWidthAnimator = ValueAnimator.ofInt(leftButton.getWidth(), leftButton.getWidth() + 200);
            leftWidthAnimator.setDuration(400);
            leftWidthAnimator.addUpdateListener(animation -> {
                int newWidth = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = leftButton.getLayoutParams();
                params.width = newWidth;
                leftButton.setLayoutParams(params);
            });

            // 오른쪽 버튼 가로 크기 축소
            ValueAnimator rightWidthAnimator = ValueAnimator.ofInt(rightButton.getWidth(), rightButton.getWidth() -200);
            rightWidthAnimator.setDuration(400);
            rightWidthAnimator.addUpdateListener(animation -> {
                int newWidth = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = rightButton.getLayoutParams();
                params.width = newWidth;
                rightButton.setLayoutParams(params);
            });

            leftButton.setBackgroundResource(R.drawable.rounded_button_yes);
            rightButton.setBackgroundResource(R.drawable.rounded_button_no);
            leftButton.setZ(10f); // 왼쪽 버튼이 위로 올라옴
            rightButton.setZ(5f); // 오른쪽 버튼이 아래로 내려감

            leftButton.setTextColor(Color.parseColor("#FFFFFF")); // 또는 Color.parseColor("#000000")
            rightButton.setTextColor(Color.parseColor("#babac0")); // 또는 Color.parseColor("#babac0")

            // 애니메이션 실행
            //moveAnimator.start();
            //moveAnimator2.start();
            leftWidthAnimator.start();
            rightWidthAnimator.start();
            //colorAnimator.start();
            ischanged = 0;
        }

        loadChatList(new OnChatListLoadedListener() {
            @Override
            public void onChatListLoaded(List<String> chatList) {
                loadPosts(new OnPostsLoadedListener() {
                    @Override
                    public void onPostsLoaded(List<Post> posts) {
                        aa = 0;
                        updateRecyclerView(chatList, nickname, posts,aa);
                    }
                });
            }
        });
    }//업데이트 리사이클러뷰 0으로

    private void selectRightButton() {
        if(ischanged == 0)
        {
            // 왼쪽 버튼 이동 애니메이션
            //ObjectAnimator moveAnimator = ObjectAnimator.ofFloat(leftButton, "translationX", 0f, -0f);
            //moveAnimator.setDuration(1000);

            // 오른쪽 버튼 이동 애니메이션 (왼쪽으로 약간 이동)
            //ObjectAnimator moveAnimator2 = ObjectAnimator.ofFloat(rightButton, "translationX", 0f, -30f);
            //moveAnimator2.setDuration(1000);

            // 왼쪽 버튼 가로 크기 확대
            ValueAnimator leftWidthAnimator = ValueAnimator.ofInt(leftButton.getWidth(), leftButton.getWidth() - 200);
            leftWidthAnimator.setDuration(400);
            leftWidthAnimator.addUpdateListener(animation -> {
                int newWidth = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = leftButton.getLayoutParams();
                params.width = newWidth;
                leftButton.setLayoutParams(params);
            });

            // 오른쪽 버튼 가로 크기 축소
            ValueAnimator rightWidthAnimator = ValueAnimator.ofInt(rightButton.getWidth(), rightButton.getWidth() + 200);
            rightWidthAnimator.setDuration(400);
            rightWidthAnimator.addUpdateListener(animation -> {
                int newWidth = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = rightButton.getLayoutParams();
                params.width = newWidth;
                rightButton.setLayoutParams(params);
            });

            leftButton.setBackgroundResource(R.drawable.rounded_button_no);
            rightButton.setBackgroundResource(R.drawable.rounded_button_yes);
            leftButton.setZ(5f); // 왼쪽 버튼이 위로 올라옴
            rightButton.setZ(10f); // 오른쪽 버튼이 아래로 내려감

            leftButton.setTextColor(Color.parseColor("#babac0")); // 또는 Color.parseColor("#000000")
            rightButton.setTextColor(Color.parseColor("#FFFFFF")); // 또는 Color.parseColor("#babac0")

            // 애니메이션 실행
            //moveAnimator.start();
            //moveAnimator2.start();
            leftWidthAnimator.start();
            rightWidthAnimator.start();
            //colorAnimator.start();
            ischanged = 1;
        }


        loadChatList(new OnChatListLoadedListener() {
            @Override
            public void onChatListLoaded(List<String> chatList) {
                loadPosts(new OnPostsLoadedListener() {
                    @Override
                    public void onPostsLoaded(List<Post> posts) {
                        aa = 1;
                        updateRecyclerView(chatList, nickname, posts,aa);
                    }
                });
            }
        });
    }//업데이트 리사이클러뷰 1로


    // 채팅방 정보를 처리하는 메서드
    private void handleChatRoomAction(String chatRoom, int isnotMinea) {
        if (chatRoom == null) {
            Log.e("UserFind", "응 챗룸 없어서 안뜰꺼 ㅅㄱ");
            return;} // 방 정보가 없으면 실행 안 함

        int a = 2; // 기본값

        // 현재 선택된 버튼 확인 (배경색 기준)
        if (leftButton.getBackground() instanceof ColorDrawable && ((ColorDrawable) leftButton.getBackground()).getColor() == Color.WHITE) {
            a = 1; // 왼쪽 버튼 선택됨
            Log.d("UserFind", "왼쪽 버튼 선택됨: " + a);
        } else if (rightButton.getBackground() instanceof ColorDrawable && ((ColorDrawable) rightButton.getBackground()).getColor() == Color.WHITE) {
            a = 0; // 오른쪽 버튼 선택됨
            Log.d("UserFind", "오른쪽 버튼 선택됨: " + a);
        }

        Log.d("UserFind", "Selected Chat Room: " + chatRoom);
        Log.d("UserFind", "Detected isnotMine value: " + isnotMinea);

        Intent chatIntent = new Intent(UserFindActivity.this, TestChatActivity.class);

        String[] parts = chatRoom.split(":");
        String nickname = parts[0];  // 닉네임 (":" 앞부분)

        // "_" 기준으로 문자열을 분리하여 번호 추출
        String[] parts2 = parts[1].split("_");
        String number = parts2[1];  // 번호 ("_" 뒤부분)

        chatIntent.putExtra("chatID", nickname);
        chatIntent.putExtra("postID", number);
        chatIntent.putExtra("isnotMine", isnotMinea);  // 버튼 클릭 상태 기반으로 전달
        Log.d("UserFind", "Final isnotMine value sent: " + isnotMinea);

        startActivity(chatIntent);
        finish();
    }




    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        // 위도, 경도를 라디안으로 변환
        double latRad1 = Math.toRadians(lat1);
        double lonRad1 = Math.toRadians(lon1);
        double latRad2 = Math.toRadians(lat2);
        double lonRad2 = Math.toRadians(lon2);

        // 위도, 경도의 차이
        double dLat = latRad2 - latRad1;
        double dLon = lonRad2 - lonRad1;

        // Haversine 공식 적용
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(latRad1) * Math.cos(latRad2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 최종 거리 (km 단위)
        double distance = EARTH_RADIUS * c;
        // 소수점 첫 번째 자리까지 반올림
        distance = Math.round(distance * 10) / 10.0;

        // 디버그 로그: 현재 위치와 상대방 위치 출력
        Log.d("DISTANCE_DEBUG", "---------------현재 위치 - 위도: " + lat1 + ", 경도: " + lon1);
        Log.d("DISTANCE_DEBUG", "상대방 위치 - 위도: " + lat2 + ", 경도: " + lon2);
        Log.d("DISTANCE_DEBUG", "계산된 거리: " + distance + " km");

        return distance;


    }









    // ✅ 일단 전체 채팅 리스트 로드시켜 놓기.
    private void mainloadChatList() {
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
                Toast.makeText(UserFindActivity.this, "채팅 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleChatList(List<String> chatList) {
        for (String chat : chatList) {
            //Log.d("ChatData", chat);
        }
        //그리고 이걸 보내야 한다...!!!!
    }









}



// 지금 해야할거는 바로
