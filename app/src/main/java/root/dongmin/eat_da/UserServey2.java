package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import root.dongmin.eat_da.adapter.AllergyAdapter;

public class UserServey2 extends AppCompatActivity {

    private EditText nicknameEditText;
    private EditText userTypeEditText;
    private String nickname, userType;
    private RecyclerView allergyRecyclerView;
    private AllergyAdapter allergyAdapter;
    private ArrayList<String> selectedItems, finalselectedItems;
    private Button getStartImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_servey2);

        // 뷰 초기화
        nicknameEditText = findViewById(R.id.nicknameEditText);
        userTypeEditText = findViewById(R.id.userTypeEditText);
        allergyRecyclerView = findViewById(R.id.allergyRecyclerView); // 알레르기 리사이클러뷰
        getStartImageView = findViewById(R.id.getstart); // 다음 버튼

        allergyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // selectedItems와 finalselectedItems 초기화
        selectedItems = new ArrayList<>();
        finalselectedItems = new ArrayList<>();

        // Intent에서 데이터 받아오기
        Intent intent = getIntent();
        if (intent != null) {
            // selectedItems 받아오기
            selectedItems = intent.getStringArrayListExtra("selectedItems");
            if (selectedItems != null) {
                finalselectedItems = new ArrayList<>(selectedItems); // 깊은 복사
                Log.d("WarmingupActivity1", "Selected Items: " + selectedItems);

                // RecyclerView에 selectedItems 표시
                allergyAdapter = new AllergyAdapter(selectedItems, finalselectedItems);
                allergyRecyclerView.setAdapter(allergyAdapter);
            } else {
                Log.d("WarmingupActivity1", "No selected items received");
            }

            // userType 받아오기
            userType = intent.getStringExtra("userType");
            if (userType != null) {
                Log.d("WarmingupActivity1", "User Type: " + userType);
                userTypeEditText.setText(userType);
            } else {
                Log.d("WarmingupActivity1", "No user type received");
            }
        }

        // 닉네임 실시간 저장
        nicknameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nickname = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 사용자 유형 실시간 저장
        userTypeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userType = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Firebase에서 nickname 가져오기
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("UserAccount").child(uid);

            userRef.child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        nickname = dataSnapshot.getValue(String.class);
                        if (nickname == null) {
                            nickname = ""; // 빈 문자열로 초기화
                        }
                        Log.d("Firebase", "Nickname: " + nickname);
                        nicknameEditText.setText(nickname);
                    } else {
                        Log.d("Firebase", "Nickname does not exist");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase", "Failed to read nickname", databaseError.toException());
                }
            });
        } else {
            Log.d("Firebase", "User is not logged in");
        }

        // 넘어가기 버튼 클릭 이벤트
        getStartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalselectedItems == null) {
                    finalselectedItems = new ArrayList<>(); // 빈 리스트로 초기화
                }

                Collections.sort(finalselectedItems);
                String myalergic = TextUtils.join("_", finalselectedItems);

                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("myalergic", myalergic);
                userUpdates.put("nickname", nickname);
                userUpdates.put("userType", userType);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference userRef = database.getReference("UserAccount").child(uid);

                userRef.updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "The information was successfully saved.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UserServey2.this, WarmingupActivity1.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to save information.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 알레르기 액티비티에서 요소 받아오는 코드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null) {
                List<String> modifiedItems = (List<String>) data.getSerializableExtra("modifiedItems");
                if (modifiedItems != null) {
                    selectedItems = new ArrayList<>(modifiedItems);
                    if (allergyAdapter != null) {
                        allergyAdapter.updateAllergyList(selectedItems);
                    }
                }
            }
        }
    }
}