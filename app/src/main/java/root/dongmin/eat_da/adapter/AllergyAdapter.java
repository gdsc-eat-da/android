package root.dongmin.eat_da.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import root.dongmin.eat_da.R;

public class AllergyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ALLERGY = 0; // 알레르기 아이템 타입
    private static final int TYPE_PLUS = 1; // plus 이미지 아이템 타입

    private List<String> allergyList;
    private List<String> finalAlergicList; // finalAlergicList 추가
    private List<Boolean> isSelectedList; // 아이템의 선택 상태를 저장하는 리스트

    public AllergyAdapter(List<String> allergyList, List<String> finalAlergicList) {
        this.allergyList = allergyList;
        this.finalAlergicList = finalAlergicList; // finalAlergicList 초기화
        this.isSelectedList = new ArrayList<>(); // 선택 상태 리스트 초기화

        // 모든 아이템의 선택 상태를 false로 초기화
        for (int i = 0; i < allergyList.size(); i++) {
            isSelectedList.add(false);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ALLERGY) {
            // 알레르기 아이템 뷰 홀더 생성
            View view = inflater.inflate(R.layout.item_minialergic, parent, false);
            return new AllergyViewHolder(view);
        } else {
            // plus 이미지 아이템 뷰 홀더 생성
            View view = inflater.inflate(R.layout.item_plus, parent, false);
            return new PlusViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AllergyViewHolder) {
            // 알레르기 아이템 처리
            String allergyItem = allergyList.get(position);
            ((AllergyViewHolder) holder).bind(allergyItem);

            // 아이템의 선택 상태에 따라 배경 설정
            if (isSelectedList.get(position)) {
                ((AllergyViewHolder) holder).tvMini.setBackgroundResource(R.drawable.miniunsel); // 활성화 상태
            } else {
                ((AllergyViewHolder) holder).tvMini.setBackgroundResource(R.drawable.minisel); // 비활성화 상태
            }

            // 아이템 클릭 이벤트 처리
            holder.itemView.setOnClickListener(v -> {
                boolean isSelected = isSelectedList.get(position);

                if (!isSelected) {
                    // 활성화 상태로 변경
                    isSelectedList.set(position, true);
                    ((AllergyViewHolder) holder).tvMini.setBackgroundResource(R.drawable.miniunsel);
                    finalAlergicList.add(allergyItem); // 리스트에 추가
                    Toast.makeText(v.getContext(), "추가됨: " + allergyItem, Toast.LENGTH_SHORT).show();
                } else {
                    // 비활성화 상태로 변경
                    isSelectedList.set(position, false);
                    ((AllergyViewHolder) holder).tvMini.setBackgroundResource(R.drawable.minisel);
                    finalAlergicList.remove(allergyItem); // 리스트에서 제거
                    Toast.makeText(v.getContext(), "제거됨: " + allergyItem, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (holder instanceof PlusViewHolder) {
            // plus 이미지 아이템 처리
            ((PlusViewHolder) holder).itemView.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "plus클릭!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        // 알레르기 아이템 수 + plus 이미지 1개
        return allergyList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        // 마지막 아이템은 plus 이미지 타입으로 설정
        if (position == allergyList.size()) {
            return TYPE_PLUS;
        }
        return TYPE_ALLERGY;
    }

    // 알레르기 아이템 뷰 홀더
    static class AllergyViewHolder extends RecyclerView.ViewHolder {
        TextView tvMini;

        public AllergyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMini = itemView.findViewById(R.id.tv_mini);
        }

        public void bind(String allergyItem) {
            tvMini.setText(allergyItem);
        }
    }

    // plus 이미지 아이템 뷰 홀더
    static class PlusViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlus;

        public PlusViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlus = itemView.findViewById(R.id.iv_plus);
        }
    }
}
//리스트들 가져오는게 아니라 퍼블릭으로 만들어서 그냥 들고와서 쓰기