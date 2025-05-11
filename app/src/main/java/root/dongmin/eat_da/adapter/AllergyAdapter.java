package root.dongmin.eat_da.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import root.dongmin.eat_da.R;
import root.dongmin.eat_da.alergicActivity;

public class AllergyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ALLERGY = 0; // 알레르기 아이템 타입
    private static final int TYPE_PLUS = 1; // plus 이미지 아이템 타입

    private List<String> allergyList; // 알레르기 아이템 목록
    private List<String> finalAlergicList; // 선택된 알레르기 아이템 목록
    private List<Boolean> isSelectedList; // 아이템의 선택 상태 리스트

    // 생성자
    public AllergyAdapter(List<String> allergyList, List<String> finalAlergicList) {
        this.allergyList = allergyList;
        this.finalAlergicList = finalAlergicList;
        this.isSelectedList = new ArrayList<>();
        syncIsSelectedList(); // isSelectedList 초기화
    }

    // isSelectedList를 allergyList와 동기화
    private void syncIsSelectedList() {
        isSelectedList.clear(); // 기존 데이터 초기화
        for (String item : allergyList) {
            // finalAlergicList에 포함된 아이템은 true로 설정
            isSelectedList.add(finalAlergicList.contains(item));
        }
    }

    // allergyList 업데이트 메서드
    public void updateAllergyList(List<String> newAllergyList) {
        for (String item : newAllergyList) {
            if (!allergyList.contains(item)) {
                allergyList.add(item);
                if (!finalAlergicList.contains(item)) {
                    finalAlergicList.add(item);
                }
            } else {
                int index = allergyList.indexOf(item);
                isSelectedList.set(index, true);
                if (!finalAlergicList.contains(item)) {
                    finalAlergicList.add(item);
                }
            }
        }
        syncIsSelectedList(); // isSelectedList 동기화
        notifyDataSetChanged(); // 어댑터에 데이터 변경 알림
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ALLERGY) {
            View view = inflater.inflate(R.layout.item_minialergic, parent, false);
            return new AllergyViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_plus, parent, false);
            return new PlusViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AllergyViewHolder) {
            String allergyItem = allergyList.get(position);
            ((AllergyViewHolder) holder).bind(allergyItem);

            if (isSelectedList.get(position)) {
                ((AllergyViewHolder) holder).tvMini.setBackgroundResource(R.drawable.miniunsel);
            } else {
                ((AllergyViewHolder) holder).tvMini.setBackgroundResource(R.drawable.minisel);
            }

            holder.itemView.setOnClickListener(v -> {
                boolean isSelected = isSelectedList.get(position);

                if (!isSelected) {
                    isSelectedList.set(position, true);
                    ((AllergyViewHolder) holder).tvMini.setBackgroundResource(R.drawable.miniunsel);
                    if (!finalAlergicList.contains(allergyItem)) {
                        finalAlergicList.add(allergyItem);
                        Toast.makeText(v.getContext(), "Added: " + allergyItem, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    isSelectedList.set(position, false);
                    ((AllergyViewHolder) holder).tvMini.setBackgroundResource(R.drawable.minisel);
                    finalAlergicList.remove(allergyItem);
                    Toast.makeText(v.getContext(), "Removed: " + allergyItem, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (holder instanceof PlusViewHolder) {
            ((PlusViewHolder) holder).itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), alergicActivity.class);
                intent.putExtra("selectedItems", (Serializable) finalAlergicList);
                ((Activity) v.getContext()).startActivityForResult(intent, 101);
            });
        }
    }

    @Override
    public int getItemCount() {
        return allergyList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
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