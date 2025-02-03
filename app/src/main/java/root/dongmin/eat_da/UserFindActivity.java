package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class UserFindActivity extends AppCompatActivity {

    private EditText nicknameEditText;
    private Button startChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_find);

        nicknameEditText = findViewById(R.id.nicknameEditText);
        startChatButton = findViewById(R.id.startChatButton);

        startChatButton.setOnClickListener(v -> {
            String nickname = nicknameEditText.getText().toString().trim();
            if (!nickname.isEmpty()) {
                Intent intent = new Intent(UserFindActivity.this, TestChatActivity.class);
                intent.putExtra("nickname", nickname);  // 닉네임을 ChatingActivity로 전달
                startActivity(intent);
            }
        });
    }
}
