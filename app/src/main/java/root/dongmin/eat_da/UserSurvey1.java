package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class UserSurvey1 extends AppCompatActivity {

    private ImageButton toSkip, goNext;
    private ImageView back;
    private RadioGroup personal;
    private RadioButton individual, selfEmp, enterprise;
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
                Intent intent = new Intent(UserSurvey1.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        goNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isIndividual == 1) {
                    Intent intent = new Intent(UserSurvey1.this, alergicActivity.class);
                    startActivityForResult(intent, 1001); // 알레르기 정보 입력 후 결과 받기
                } else {
                    Intent intent = new Intent(UserSurvey1.this, MainActivity.class);
                    startActivity(intent);
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
                } else if (checkedId == R.id.radioSelf_empoloyed) {
                    selfEmp.setBackgroundResource(R.drawable.radioclicked);
                    enterprise.setBackgroundResource(R.drawable.radiodefault);
                    individual.setBackgroundResource(R.drawable.radiodefault);
                    isIndividual = 0;
                } else if (checkedId == R.id.radioEnterprise) {
                    enterprise.setBackgroundResource(R.drawable.radioclicked);
                    individual.setBackgroundResource(R.drawable.radiodefault);
                    selfEmp.setBackgroundResource(R.drawable.radiodefault);
                    isIndividual = 0;
                }
            }
        });
    }

    // 알레르기 정보 입력 후 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            Intent intent = new Intent(UserSurvey1.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
