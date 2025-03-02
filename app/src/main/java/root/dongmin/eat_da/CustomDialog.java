package root.dongmin.eat_da;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.widget.LinearLayout;

public class CustomDialog extends Dialog {

    public CustomDialog(Context context){
        super(context);
        setContentView(R.layout.dialog_custom); //커스텀 다이얼 로그 설정

        getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 배경을 투명하게 설정

        LinearLayout btnOption1 = findViewById(R.id.btnCommunity);
        LinearLayout btnOption2 = findViewById(R.id.btnFoodRequest);

        // 첫 번째 선택지를 눌렀을 때 Activity1으로 이동
        btnOption1.setOnClickListener(v -> {
            context.startActivity(new Intent(context, PhotoActivity.class));
            dismiss();
        });

        // 두 번째 선택지를 눌렀을 때 Activity2으로 이동
        btnOption2.setOnClickListener(v -> {
            context.startActivity(new Intent(context, NeedActivity.class));
            dismiss();
        });
    }
}
