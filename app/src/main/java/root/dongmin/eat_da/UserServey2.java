package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import root.dongmin.eat_da.adapter.AllergyAdapter;

public class UserServey2 extends AppCompatActivity {

    private EditText nicknameEditText;
    private EditText userTypeEditText;
    private String nickname;
    private RecyclerView allergyRecyclerView;
    private AllergyAdapter allergyAdapter;
    private ArrayList<String> selectedItems, finalselectedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_servey2);
        // 뷰 초기화
        nicknameEditText = findViewById(R.id.nicknameEditText);
        userTypeEditText = findViewById(R.id.userTypeEditText);
        allergyRecyclerView = findViewById(R.id.allergyRecyclerView);//알레르기 리사이클러뷰

        allergyAdapter = new AllergyAdapter(selectedItems, finalselectedItems);
        allergyRecyclerView.setAdapter(allergyAdapter);

        // Intent에서 데이터 받아오기
        Intent intent = getIntent();
        if (intent != null) {
            // selectedItems 받아오기
            selectedItems = intent.getStringArrayListExtra("selectedItems");
            finalselectedItems =  selectedItems;
            if (selectedItems != null) {
                Log.d("WarmingupActivity1", "Selected Items: " + selectedItems);

                // 예시: selectedItems를 RecyclerView에 표시
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                AllergyAdapter adapter = new AllergyAdapter(selectedItems, finalselectedItems);
                recyclerView.setAdapter(adapter);
            } else {
                Log.d("WarmingupActivity1", "No selected items received");
            }

            // userType 받아오기
            String userType = intent.getStringExtra("userType");
            if (userType != null) {
                Log.d("WarmingupActivity1", "User Type: " + userType);
                userTypeEditText.setText(userType);
            } else {
                Log.d("WarmingupActivity1", "No user type received");
            }
        }



        // 💄💄💄닉네임 실시간 저장
        nicknameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 텍스트가 변경될 때마다 호출
                String nickname = s.toString();
                //saveNickname(nickname); // 닉네임 저장 로직
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        // 💄💄💄사용자 유형 실시간 저장
        userTypeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 텍스트가 변경될 때마다 호출
                String userType = s.toString();
                //saveUserType(userType); // 사용자 유형 저장 로직
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        //---------------------------------------------------------------파이어베이스 존---------------------------------
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();


            // Firebase에서 nickname 가져오기
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("UserAccount").child(uid);

            userRef.child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        nickname = dataSnapshot.getValue(String.class); // nickname 저장
                        Log.d("Firebase", "Nickname: " + nickname);

                        // EditText에 nickname 표시
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
        }
        else {
            Log.d("Firebase", "User is not logged in");
        }
        //---------------------------------------------------------------파이어베이스 존---------------------------------













        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 알레르기 액티비티에서 요소 받아오는 코드                                                                 <알레르기!>
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            // AlergicActivity에서 보낸 데이터를 받기
            if (data != null) {
                List<String> modifiedItems = (List<String>) data.getSerializableExtra("modifiedItems");
                if (modifiedItems != null) {
                    selectedItems = (ArrayList<String>) modifiedItems;
                    allergyAdapter.updateAllergyList(modifiedItems);

                }
            }
        }
    }












}