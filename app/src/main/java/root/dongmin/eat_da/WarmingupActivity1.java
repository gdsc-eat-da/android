package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class WarmingupActivity1 extends AppCompatActivity {

    private Button jumpButton;
    private Button checkAnswerButton;
    private Button btnstart;
    private CheckBox question1, question2, question3, question4, question5, question6;
    private ImageView bubble1, bubble2, bubble3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_warmingup1);

        jumpButton = findViewById(R.id.toSkip2);
        checkAnswerButton = findViewById(R.id.seeAnswer);
        btnstart = findViewById(R.id.btnstarteatda);

        question1 = findViewById(R.id.question1);
        question2 = findViewById(R.id.question2);
        question3 = findViewById(R.id.question3);
        question4 = findViewById(R.id.question4);
        question5 = findViewById(R.id.question5);
        question6 = findViewById(R.id.question6);

        bubble1 = findViewById(R.id.bubble1);
        bubble2 = findViewById(R.id.bubble2);
        bubble3 = findViewById(R.id.bubble3);

        jumpButton.setOnClickListener(v -> {
            Intent intent = new Intent(WarmingupActivity1.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 각각의 체크박스에 대해 개별 리스너 설정
        setCheckBoxListeners();

        // checkAnswerButton 클릭 시 UI 변경
        checkAnswerButton.setOnClickListener(v -> {

            // 체크박스 상태 확인
            boolean allCorrect = question2.isChecked() && question4.isChecked() && question6.isChecked();
            boolean includesIncorrect = question1.isChecked() || question3.isChecked() || question5.isChecked();

            if(allCorrect && !includesIncorrect){
                // 모두 정답일 경우
                Toast.makeText(WarmingupActivity1.this, "All are correct!", Toast.LENGTH_SHORT).show();
            } else {
                // bubble 이미지를 보여주고, 잘못된 항목의 배경을 변경
                if (question1.isChecked()) {
                    bubble1.setVisibility(View.VISIBLE); // question1이 체크되었을 때 bubble1 표시
                    question1.setBackgroundResource(R.drawable.foodnowrongup); // question1 배경 변경
                }
                if (question3.isChecked()) {
                    bubble3.setVisibility(View.VISIBLE); // question3이 체크되었을 때 bubble2 표시
                    question3.setBackgroundResource(R.drawable.foodfakewrongup); // question3 배경 변경
                }
                if (question5.isChecked()) {
                    bubble2.setVisibility(View.VISIBLE); // question5가 체크되었을 때 bubble3 표시
                    question5.setBackgroundResource(R.drawable.cashwrongup); // question5 배경 변경
                }


                Toast.makeText(WarmingupActivity1.this, "Please check your answer.", Toast.LENGTH_SHORT).show();
            }

            // 체크박스를 클릭할 수 없도록 비활성화
            question1.setEnabled(false);
            question2.setEnabled(false);
            question3.setEnabled(false);
            question4.setEnabled(false);
            question5.setEnabled(false);
            question6.setEnabled(false);


            jumpButton.setVisibility(View.GONE); // jumpButton 숨기기
            checkAnswerButton.setVisibility(View.GONE); // checkAnswerButton 숨기기
            btnstart.setVisibility(View.VISIBLE); // btnstart 보이게 하기
        });


        btnstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WarmingupActivity1.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setCheckBoxListeners() {
        question1.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateCheckBoxBackground(question1, isChecked, R.drawable.foodnoclickup, R.drawable.foodnodefaultup));
        question2.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateCheckBoxBackground(question2, isChecked, R.drawable.recipeshareclickup, R.drawable.recipesharedefaultup));
        question3.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateCheckBoxBackground(question3, isChecked, R.drawable.foodfakeclickup, R.drawable.foodfakedefaultup));
        question4.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateCheckBoxBackground(question4, isChecked, R.drawable.crimeclickedup, R.drawable.crimedefaultup));
        question5.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateCheckBoxBackground(question5, isChecked, R.drawable.cashclickedup, R.drawable.cashdefaultup));
        question6.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateCheckBoxBackground(question6, isChecked, R.drawable.timeatteckclickedup, R.drawable.timeattackdefaultup));
    }

    private void updateCheckBoxBackground(CheckBox checkBox, boolean isChecked, int checkedImage, int defaultImage) {
        if (checkBox != null) {
            if (isChecked) {
                checkBox.setBackgroundResource(checkedImage);
            } else {
                checkBox.setBackgroundResource(defaultImage);
            }
        }
    }


}
