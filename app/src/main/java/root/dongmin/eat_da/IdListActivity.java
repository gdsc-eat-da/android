package root.dongmin.eat_da;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class IdListActivity extends AppCompatActivity {


    private EditText editTextMessage;
    private Button buttonSend;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_id_list);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            private int previousItemId = R.id.chat; // 초기 선택된 아이콘
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                // 1️⃣ 이전 아이콘을 default로 변경
                updateIcon(previousItemId, false);

                // 2️⃣ 현재 클릭된 아이콘을 clicked 상태로 변경
                updateIcon(item.getItemId(), true);

                // 3️⃣ 현재 클릭된 아이콘을 이전 아이콘으로 설정
                previousItemId = item.getItemId();


                if (item.getItemId() == R.id.chat) {
                    Toast.makeText(IdListActivity.this, "Chat", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    Intent intent = new Intent(IdListActivity.this, MyPageActivity.class);
                    startActivity(intent);
                    return true;
                }else if (item.getItemId() == R.id.nav_home) {
                    Intent intent = new Intent(IdListActivity.this, MainActivity.class );
                    startActivity(intent);
                }else if (item.getItemId() == R.id.work_load){
                    Intent intent = new Intent(IdListActivity.this,MapActivity.class);
                    startActivity(intent);
                }
                return false;
            }
        });

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);


        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();
                Intent intent = new Intent(IdListActivity.this, TestChatActivity.class);
                intent.putExtra("chatID",message);

                startActivity(intent);
            }
        });







        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 아이콘 업데이트 함수
    private void updateIcon(int itemId, boolean isClicked) {
        if (bottomNavigationView == null) return;

        int iconRes;
        if (itemId == R.id.nav_home) {
            iconRes = isClicked ? R.drawable.homeclicked : R.drawable.homedefault;
        } else if (itemId == R.id.chat) {
            iconRes = isClicked ? R.drawable.chatclicked : R.drawable.chatdefault;
        } else if (itemId == R.id.nav_profile) {
            iconRes = isClicked ? R.drawable.mypageclicked : R.drawable.mypagedefault;
        } else if (itemId == R.id.work_load) {
            iconRes = isClicked ? R.drawable.workloadclicked : R.drawable.workloaddefault;
        } else {
            return;
        }
        bottomNavigationView.getMenu().findItem(itemId).setIcon(iconRes);
    }
}