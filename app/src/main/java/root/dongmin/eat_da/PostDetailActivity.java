package root.dongmin.eat_da;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PostDetailActivity extends AppCompatActivity {

    private TextView titleTextView, ingredientsTextView;
    private ImageView postImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        titleTextView = findViewById(R.id.detailTitle);
        ingredientsTextView = findViewById(R.id.detailIngredients);
        postImageView = findViewById(R.id.detailImage);

        // ✅ MainActivity에서 전달받은 데이터 가져오기
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String ingredients = intent.getStringExtra("ingredients");
        String image = intent.getStringExtra("image");
        String Nick = intent.getStringExtra("nickname");

        // ✅ 데이터 적용
        titleTextView.setText(Nick + "님의 " + title);
        ingredientsTextView.setText(ingredients);
        Glide.with(this).load(image).into(postImageView);
    }
}