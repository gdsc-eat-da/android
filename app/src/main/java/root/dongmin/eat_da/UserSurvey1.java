package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserSurvey1 extends AppCompatActivity {

    private Button toSkip, goNext;
    private ImageView back;
    private RadioGroup personal;
    private RadioButton individual, selfEmp, enterprise;
    private String userType  = "미정";
    private List<String> selectedItems;
    private int isIndividual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_survey1);

        back = findViewById(R.id.btnbacktoon1);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSurvey1.this, Onboarding4.class);
                startActivity(intent);
                finish();
            }
        });

        toSkip = findViewById(R.id.toSkip);
        goNext = findViewById(R.id.goNext);

        toSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // WarmingupActivity1로 이동하는 Intent 생성
                Intent intent = new Intent(UserSurvey1.this, WarmingupActivity1.class);

                // selectedItems와 userType을 Intent에 추가
                intent.putStringArrayListExtra("selectedItems", new ArrayList<>(selectedItems)); // List<String>을 전달
                intent.putExtra("userType", userType); // String 변수 전달

                // 액티비티 시작
                startActivity(intent);

                // 현재 액티비티 종료
                finish();
            }
        });

        goNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isIndividual == 1) {
                    Intent intent = new Intent(UserSurvey1.this, alergicActivity.class);
                    startActivityForResult(intent, 101); // 알레르기 정보 입력 후 결과 받기(수정했음)
                    // 화면 전환 애니메이션 적용
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    //finish();
                } else {
                    Intent intent = new Intent(UserSurvey1.this, UserServey2.class);
                    startActivity(intent);

                    // 화면 전환 애니메이션 적용
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                    finish();
                }
            }
        });

        personal = findViewById(R.id.radioGroupPersonal);
        individual = findViewById(R.id.radioIndividual);
        selfEmp = findViewById(R.id.radioSelf_empoloyed);
        enterprise = findViewById(R.id.radioEnterprise);

        personal.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioIndividual) {
                    individual.setBackgroundResource(R.drawable.radioclicked);
                    selfEmp.setBackgroundResource(R.drawable.radiodefault);
                    enterprise.setBackgroundResource(R.drawable.radiodefault);
                    isIndividual = 1;
                    userType = "개인";
                } else if (checkedId == R.id.radioSelf_empoloyed) {
                    selfEmp.setBackgroundResource(R.drawable.radioclicked);
                    enterprise.setBackgroundResource(R.drawable.radiodefault);
                    individual.setBackgroundResource(R.drawable.radiodefault);
                    isIndividual = 0;
                    userType = "기업";
                } else if (checkedId == R.id.radioEnterprise) {
                    enterprise.setBackgroundResource(R.drawable.radioclicked);
                    individual.setBackgroundResource(R.drawable.radiodefault);
                    selfEmp.setBackgroundResource(R.drawable.radiodefault);
                    isIndividual = 0;
                    userType = "자영업자";
                }
            }
        });
    }

    // 알레르기 정보 입력 후 결과 처리
    // 갤러리에서 이미지 선택 후 처리. 또한 알레르기 데이터 가져오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Firebase Database Reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = database.getReference("UserAccount"); // UserAccount 테이블 기준
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 로그인한 사용자 UID 가져오기

        //알레르기 액티비티에서 데이터를 받아오는 경우 (requestCode: 101)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null) {
                List<String> modifiedItems = (List<String>) data.getSerializableExtra("modifiedItems");
                if (modifiedItems != null) {
                    selectedItems = modifiedItems;
                    Log.d("UsersurvayActivity", "Modified items: " + selectedItems);
                    Intent intent = new Intent(UserSurvey1.this, UserServey2.class);
                    // selectedItems와 userType을 Intent에 추가
                    intent.putStringArrayListExtra("selectedItems", new ArrayList<>(modifiedItems)); // List<String>을 전달
                    intent.putExtra("userType", userType); // String 변수 전달
                    // 액티비티 시작
                    startActivity(intent);
                    // 현재 액티비티 종료
                    finish();
                }
            }
            else {
                Intent intent = new Intent(UserSurvey1.this, UserServey2.class);
                // selectedItems와 userType을 Intent에 추가
                intent.putStringArrayListExtra("selectedItems", new ArrayList<>(selectedItems)); // List<String>을 전달
                intent.putExtra("userType", userType); // String 변수 전달
                // 액티비티 시작
                startActivity(intent);
                // 현재 액티비티 종료
                finish();
            }
        }

    }
}
