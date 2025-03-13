package root.dongmin.eat_da.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int space;

    public SpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.bottom = space / 2; // 아이템 아래 간격 추가
        outRect.top = space / 2;  // 아이템 위 간격 (절반)
        outRect.left = space / 2; // 좌측 간격
        outRect.right = space / 2; // 우측 간격
    }
}

