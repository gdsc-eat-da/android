package root.dongmin.eat_da.adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import root.dongmin.eat_da.R;

public class PlusHashtagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> plusHashtags; // 위쪽 RecyclerView에 표시할 데이터
    private Context context; // Context 추가

    public HashtagAdapter hashtagAdapter;


    // 생성자
    public PlusHashtagAdapter(List<String> plusHashtags, Context context, HashtagAdapter hashtagAdapter) {
        this.plusHashtags = plusHashtags;
        this.context = context; // Context 초기화
        this.hashtagAdapter = hashtagAdapter;
    }

    // 뷰 타입 반환
    @Override
    public int getItemViewType(int position) {
        return plusHashtags.isEmpty() ? 0 : 1;
    }

    // ViewHolder 생성
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plus_hashtag, parent, false);
            return new PlusHashtagViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_minialergic, parent, false);
            return new HashViewHolder(view);
        }
    }

    // 데이터 바인딩
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PlusHashtagViewHolder) {
            // PlusHashtagViewHolder용 바인딩
            PlusHashtagViewHolder plusHolder = (PlusHashtagViewHolder) holder;
            plusHolder.plusHashtagText.setVisibility(View.GONE);
            plusHolder.plusHashtagImage.setVisibility(View.VISIBLE);
            plusHolder.plusHashtagImage.setImageResource(R.drawable.youcanplus);
            plusHolder.plusHashtagImage.setOnClickListener(v -> showCustomDialog());
        } else if (holder instanceof HashViewHolder) {
            // HashViewHolder용 바인딩
            HashViewHolder hashHolder = (HashViewHolder) holder;
            String hashtag = plusHashtags.get(position);
            hashHolder.tvMini.setText(hashtag);

            // HashViewHolder에 클릭 리스너 추가
            hashHolder.itemView.setOnClickListener(v -> showCustomDialog());
        }
    }

    // 아이템 개수 반환
    @Override
    public int getItemCount() {
        return plusHashtags.isEmpty() ? 1 : plusHashtags.size();
    }

    // PlusHashtagViewHolder 클래스
    public static class PlusHashtagViewHolder extends RecyclerView.ViewHolder {
        TextView plusHashtagText;
        ImageView plusHashtagImage;

        public PlusHashtagViewHolder(@NonNull View itemView) {
            super(itemView);
            plusHashtagText = itemView.findViewById(R.id.plusHashtagText);
            plusHashtagImage = itemView.findViewById(R.id.plusHashtagImage);
        }
    }

    // HashViewHolder 클래스
    public static class HashViewHolder extends RecyclerView.ViewHolder {
        TextView tvMini;

        public HashViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMini = itemView.findViewById(R.id.tv_mini);
        }
    }

    // 다이얼로그를 보여주는 메서드
    private void showCustomDialog() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_item);

        EditText editTextInput = dialog.findViewById(R.id.editTextInput);
        ImageView btnAdd = dialog.findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(v -> {
            String newItem = editTextInput.getText().toString().trim();
            if (!newItem.isEmpty() && !plusHashtags.contains(newItem)) {


                plusHashtags.add(newItem);
                notifyDataSetChanged();
                hashtagAdapter.updateHashList(newItem);


                dialog.dismiss();
            } else {
                Toast.makeText(context, "올바른 값을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}