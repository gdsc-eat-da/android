package root.dongmin.eat_da;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import root.dongmin.eat_da.adapter.ChatAdapter;
import root.dongmin.eat_da.data.ChatData;

public class TestChatActivity extends AppCompatActivity {

    // 리사이클러뷰 및 관련 변수 선언
    private RecyclerView mRecycleView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<ChatData> chatList;

    // Firebase 인증 관련 변수
    private FirebaseAuth mAuth;
    private String userEmail, receivedId, postID; // 현재 로그인한 사용자의 이메일 저장            receivedId    postID        yourNick

    // 채팅 입력 필드 및 전송 버튼
    private EditText EditText_chat;
    private ImageView Button_send,Button_back;


    private ImageView profile;
    private TextView yourNickView,Button_tradeIsDone;

    // Firebase Realtime Database 참조
    private DatabaseReference myRef;

    // 클래스 멤버 변수로 yourNick 선언
    private String yourNick = "";
    private int isnotMine = 0; // 기본값 0
    public String profileUrl;

    private Handler handler = new Handler(); // Handler 객체 생성
    private Runnable runnable; // Runnable 객체 생성

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_chat);

        Toast.makeText(this, "isnotMine: " + isnotMine, Toast.LENGTH_SHORT).show();


        // 1. Intent에서 데이터 가져오기 (상대방 아이디)       (receivedID)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("chatID")) {
            receivedId = intent.getStringExtra("chatID");
            postID = intent.getStringExtra("postID");
            isnotMine = getIntent().getIntExtra("isnotMine", 0);

            Toast.makeText(this, "채팅을 시작할 상대: " + receivedId, Toast.LENGTH_SHORT).show();
        }

        // 2. FirebaseAuth 인스턴스를 가져와 현재 로그인한 사용자 확인 (자기 자신 아이디)
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userEmail = currentUser.getEmail(); // 로그인한 사용자의 이메일 저장
            Log.d("TestChatActivity", "로그인된 이메일: " + userEmail);
            Toast.makeText(this, "로그인된 이메일: " + userEmail, Toast.LENGTH_SHORT).show();
        } else {
            userEmail = "Unknown";
            Log.d("TestChatActivity", "로그인된 사용자가 없습니다.");
        }

        // 3. 현재 사용자의 닉네임 가져오기
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid(); // uid 변수 초기화
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(uid);

            userRef.child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    yourNick = dataSnapshot.getValue(String.class); // 클래스 멤버 변수에 닉네임 저장
                    if (yourNick != null) {
                        Log.d("Nickname", "닉네임: " + yourNick);

                        // 4. 고유 채팅방 아이디 만들기
                        List<String> users = new ArrayList<>();
                        String userEmailSafe = userEmail.replace(".", "_").replace("@", "_");
                        String receivedIdSafe = receivedId.replace(".", "_").replace("@", "_");
                        users.add(receivedIdSafe);
                        users.add(yourNick);
                        Collections.sort(users);

                        String sortedId = users.get(0) + "_" + users.get(1);
                        Log.d("TestChatActivity", "정렬된 채팅방 ID: " + sortedId);

                        // 5. Firebase Realtime Database 참조 가져오기
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        if(isnotMine == 0)
                        {
                            myRef = database.getReference("chat").child(postID + "_" + yourNick + "_" + receivedId);
                        }
                        else
                        {
                            myRef = database.getReference("chat").child(postID + "_" + receivedId + "_" + yourNick);
                        }


                        // Runnable 초기화
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                // 2초마다 실행할 코드
                                updateIsNotReadForAllMessages();

                                // 2초 후에 다시 실행
                                handler.postDelayed(this, 2000);
                            }
                        };

                        // Runnable 시작
                        handler.post(runnable);


                        // 6. UI 요소 초기화 및 이벤트 리스너 설정
                        initUI();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "닉네임 불러오기 실패: " + databaseError.getMessage());
                }
            });






        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티가 종료될 때 Handler 정리
        handler.removeCallbacks(runnable);
    }

    // 모든 메시지의 isnotread 필드를 업데이트하는 메서드
    private void updateIsNotReadForAllMessages() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {//데이터를 한 번만 가져오는 리스너
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    updateIsNotRead(snapshot); // 각 메시지에 대해 isnotread 업데이트
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "데이터 읽기 실패: " + databaseError.getMessage());
            }
        });
    }





    public String getCurrentDateTime() {
        // 현재 시간을 가져옴
        Date now = new Date();

        // 포맷 지정 (요일(한국어) HH:mm 형식)
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE HH:mm", Locale.KOREAN);

        // 포맷된 날짜와 시간 반환
        return formatter.format(now);
    }

    // UI 초기화 및 이벤트 리스너 설정
    private void initUI() {
        Button_send = findViewById(R.id.sendButton);
        Button_back = findViewById(R.id.backButton);
        EditText_chat = findViewById(R.id.EditText_chat);
        profile = findViewById(R.id.profileImage);
        yourNickView = findViewById(R.id.profileName);
        Button_tradeIsDone = findViewById(R.id.tradeIsDone);

        if (receivedId != null && !receivedId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount");

            // receivedId와 일치하는 사용자의 UID 찾기
            userRef.orderByChild("nickname").equalTo(receivedId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String nickname = snapshot.child("nickname").getValue(String.class);
                            String profileImageUrl = snapshot.child("profileImage").getValue(String.class);
                            profileUrl = profileImageUrl;

                            if (nickname != null) {
                                yourNickView.setText(nickname); // 상대방 닉네임 표시
                            }

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                // Glide를 사용하여 프로필 이미지 로드
                                Glide.with(TestChatActivity.this)
                                        .load(profileImageUrl)
                                        .into(profile);
                            }
                            break; // 첫 번째 일치하는 사용자 정보만 사용
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "프로필 정보 불러오기 실패: " + databaseError.getMessage());
                }
            });
        }


        // 전송 버튼 클릭 시 채팅 데이터 Firebase에 저장
        Button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = EditText_chat.getText().toString();

                // receivedId에 해당하는 사용자의 프로필 이미지 URL을 Firebase에서 가져오기
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount");
                userRef.orderByChild("nickname").equalTo(yourNick).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String profileImageUrl = snapshot.child("profileImage").getValue(String.class);

                                // 프로필 이미지 URL이 있는 경우
                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    profileUrl = profileImageUrl; // profileUrl 업데이트
                                } else {
                                    profileUrl = "default_profile_url"; // 기본값 설정
                                }

                                // 채팅 데이터 생성
                                ChatData chat = new ChatData();
                                chat.setNickname(yourNick); // 사용자 닉네임 설정
                                chat.setMsg(msg); // 입력된 메시지 설정
                                chat.setTime(getCurrentDateTime()); // 현재 시간 설정
                                chat.setIsnotread(receivedId + "_O"); // 읽음 상태 설정
                                chat.setUrl(profileUrl); // 프로필 이미지 URL 설정

                                // Firebase에 데이터 저장
                                myRef.push().setValue(chat);

                                // 입력 필드 초기화
                                EditText_chat.setText("");
                                break; // 첫 번째 일치하는 사용자 정보만 사용
                            }
                        } else {
                            Log.d("TestChatActivity", "일치하는 사용자가 없습니다.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", "프로필 정보 불러오기 실패: " + databaseError.getMessage());
                    }
                });
            }
        });
        Button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //거래 완료 시
        Button_tradeIsDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference chatRef;

                if (isnotMine == 0) {
                    chatRef = FirebaseDatabase.getInstance().getReference("chatIsDone").child(postID + "_" + yourNick + "_" + receivedId);
                } else {
                    chatRef = FirebaseDatabase.getInstance().getReference("chatIsDone").child(postID + "_" + receivedId + "_" + yourNick);
                }

                // Firebase Database 참조
                String newYourData = yourNick + "_OK";

                // 고유한 키 생성 및 데이터 추가
                chatRef.push().setValue(newYourData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // 데이터 추가 성공
                                Toast.makeText(TestChatActivity.this, "Data added successfully!", Toast.LENGTH_SHORT).show();

                                // receivedId_OK 데이터가 있는지 확인
                                chatRef.orderByValue().equalTo(receivedId + "_OK").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            // receivedId_OK 데이터가 있는 경우
                                            // chat 테이블의 해당 노드 삭제
                                            DatabaseReference chatTableRef;

                                            if (isnotMine == 0) {
                                                chatTableRef = FirebaseDatabase.getInstance().getReference("chat").child(postID + "_" + yourNick + "_" + receivedId);
                                            } else {
                                                chatTableRef = FirebaseDatabase.getInstance().getReference("chat").child(postID + "_" + receivedId + "_" + yourNick);
                                            }

                                            // chat 테이블 데이터 삭제
                                            chatTableRef.removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(TestChatActivity.this, "chat is sucessfully deleted", Toast.LENGTH_SHORT).show();

                                                            // chatRef의 모든 요소들도 삭제
                                                            chatRef.removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            //Toast.makeText(TestChatActivity.this, "chatRef 데이터 또한 삭제되었습니다...", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(Exception e) {
                                                                            Toast.makeText(TestChatActivity.this, "chatRef 데이터 삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });

                                                            // UserAccount 노드에서 yourNick과 receivedId에 해당하는 사용자의 transactionCount 업데이트
                                                            updateTransactionCount(yourNick);
                                                            updateTransactionCount(receivedId);

                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(Exception e) {
                                                            Toast.makeText(TestChatActivity.this, "채팅 데이터 삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    });
                                        } else {
                                            // receivedId_OK 데이터가 없는 경우
                                            // 팝업창으로 메시지 표시
                                            new AlertDialog.Builder(TestChatActivity.this)
                                                    .setTitle("거래 완료 요청")
                                                    .setMessage("거래 완료 요청을 완료했습니다! 상대방도 거래 완료 버튼을 누를 시 채팅은 종료됩니다!")
                                                    .setPositiveButton("확인", null)
                                                    .show();
                                            Button_tradeIsDone.setBackgroundResource(R.drawable.miniunsel); // 배경 변경
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(TestChatActivity.this, "데이터 확인 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                // 데이터 추가 실패
                                Toast.makeText(TestChatActivity.this, "거래 완료 요청 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });




        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // 비동기 함수 호출
        checkFirebaseDataAsync(postID, receivedId, yourNick, new FirebaseCheckCallback() {
            @Override
            public void onResult(boolean isYourNickOK, boolean isReceivedIdOK) {
                // 결과에 따라 TextView 배경 변경(한명이라도 버튼 누르면 파란색으로 바뀌노 ㅏㅣㄴㅇ리ㅏ머니ㅏㅇ러마인러ㅣㅇㄴㄹ밍ㄹ니ㅓ)
                if (isYourNickOK || isReceivedIdOK) {
                    Button_tradeIsDone.setBackgroundResource(R.drawable.miniunsel); // 배경 변경
                } else {
                    Button_tradeIsDone.setBackgroundResource(R.drawable.minisel); // 기본 배경
                }
            }

            @Override
            public void onError(String errorMessage) {
                // 에러 처리
                Toast.makeText(TestChatActivity.this, "데이터 확인 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        // 리사이클러뷰 초기화 및 레이아웃 설정
        mRecycleView = findViewById(R.id.my_recycler_view);
        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);

        // 채팅 데이터 리스트 및 어댑터 설정
        chatList = new ArrayList<>();
        mAdapter = new ChatAdapter(chatList, TestChatActivity.this, yourNick, profileUrl);
        mRecycleView.setAdapter(mAdapter);

        // Firebase에서 새로운 채팅 데이터가 추가될 때마다 실행되는 리스너 설정
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatData chat = snapshot.getValue(ChatData.class); // 새로 추가된 데이터 가져오기
                ((ChatAdapter) mAdapter).addprofile(profileUrl);
                ((ChatAdapter) mAdapter).addChat(chat); // 어댑터를 통해 리스트에 추가
                // isnotread 필드 업데이트
                updateIsNotRead(snapshot);//DataSnapshot은 Firebase Realtime Database에서 특정 경로에 있는 데이터를 읽어온 결과.
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // 채팅 데이터가 변경되었을 때 처리 (현재 미구현)
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // 채팅 데이터가 삭제되었을 때 처리 (현재 미구현)
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // 채팅 데이터가 이동되었을 때 처리 (현재 미구현)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 데이터 로드 중 에러 발생 시 처리
            }
        });
    }

    // isnotread 필드 업데이트 메서드
    private void updateIsNotRead(DataSnapshot snapshot) {
        ChatData chat = snapshot.getValue(ChatData.class);
        if (chat != null) {
            String isnotread = chat.getIsnotread();
            if (isnotread != null) {
                String[] parts = isnotread.split("_");
                if (parts.length == 2 && parts[0].equals(yourNick) && parts[1].equals("O")) {
                    // isnotread 필드 업데이트
                    String newIsNotRead = yourNick + "_X";
                    snapshot.getRef().child("isnotread").setValue(newIsNotRead);
                }
            }
        }
    }



    public void checkFirebaseDataAsync(String postID, String receivedId, String yourNick, FirebaseCheckCallback callback) {
        // Firebase Database 참조
        DatabaseReference chatRef;

        if(isnotMine == 0)
        {
            chatRef = FirebaseDatabase.getInstance().getReference("chatIsDone").child(postID + "_" + yourNick + "_" + receivedId);
        }
        else
        {
            chatRef = FirebaseDatabase.getInstance().getReference("chatIsDone").child(postID + "_" + receivedId + "_" + yourNick);
        }

        // 데이터 확인
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isYourNickOK = false;
                boolean isReceivedIdOK = false;

                // 데이터 스냅샷 순회
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String value = snapshot.getValue(String.class);
                    if (value != null) {
                        if (value.equals(yourNick + "_OK")) {
                            isYourNickOK = true;
                        }
                        if (value.equals(receivedId + "_OK")) {
                            isReceivedIdOK = true;
                        }
                    }
                }

                // 결과를 콜백으로 전달
                callback.onResult(isYourNickOK, isReceivedIdOK);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 에러 발생 시 콜백으로 전달
                callback.onError(databaseError.getMessage());
            }
        });
    }
    public interface FirebaseCheckCallback {
        void onResult(boolean isYourNickOK, boolean isReceivedIdOK); // 결과를 전달하는 콜백
        void onError(String errorMessage); // 에러 발생 시 호출되는 콜백
    }




    // transactionCount 업데이트 메서드
    private void updateTransactionCount(String nickname) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount");
        userRef.orderByChild("nickname").equalTo(nickname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String uid = userSnapshot.getKey();
                        DatabaseReference userAccountRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(uid);

                        // transactionCount 업데이트
                        userAccountRef.child("transactionCount").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    int currentCount = snapshot.getValue(Integer.class);
                                    userAccountRef.child("transactionCount").setValue(currentCount + 1);
                                } else {
                                    // transactionCount가 없는 경우 기본값 1로 설정
                                    userAccountRef.child("transactionCount").setValue(1);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(TestChatActivity.this, "transactionCount 업데이트 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TestChatActivity.this, "사용자 검색 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }





}