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

    private List<String> allergyList; // 알레르기 아이템 목록 (액티비티의 리스트를 참조)
    private List<String> finalAlergicList; // 선택된 알레르기 아이템 목록 (액티비티의 리스트를 참조)
    private List<Boolean> isSelectedList; // 아이템의 선택 상태 리스트

    // 생성자에서 액티비티의 리스트를 전달받음
    public AllergyAdapter(List<String> allergyList, List<String> finalAlergicList) {
        this.allergyList = allergyList; // 액티비티의 리스트를 직접 참조
        this.finalAlergicList = finalAlergicList; // 액티비티의 리스트를 직접 참조
        this.isSelectedList = new ArrayList<>();
        syncIsSelectedList(); // isSelectedList 초기화
    }

    // isSelectedList를 allergyList와 동기화
    private void syncIsSelectedList() {
        while (isSelectedList.size() < allergyList.size()) {
            isSelectedList.add(false); // 기본값은 true 왜냐면 알레르기 거기에서 이미 선택한 거니까
        }
    }
    private void syncIsSelectedList(int a) {
        int aa = a;
        if(aa == 1)
        {
            while (isSelectedList.size() < allergyList.size()) {
                isSelectedList.add(true); // 기본값은 true 왜냐면 알레르기 거기에서 이미 선택한 거니까
            }
        }
    }

    // allergyList 업데이트 메서드
    public void updateAllergyList(List<String> newAllergyList) {


        // modifiedItems의 값을 alergicList와 finalAlergicList에 추가 (중복 제외)
        for (String item : newAllergyList) {
            if (!this.allergyList.contains(item)) { // alergicList에 중복되지 않으면 추가
                this.allergyList.add(item);

                if (!this.finalAlergicList.contains(item)) { // finalAlergicList에 중복되지 않으면 추가
                    this.finalAlergicList.add(item);
                }
            }
            else if(this.allergyList.contains(item))//만약 중복이 된다면
            {

                int index = this.allergyList.indexOf(item); // 중복된 아이템의 인덱스 찾기
                this.isSelectedList.set(index, true); // isSelectedList의 해당 인덱스를 true로 설정


                if (!this.finalAlergicList.contains(item)) { // finalAlergicList에 중복되지 않으면 추가
                    this.finalAlergicList.add(item);
                }
            }

        }

        // 로그로 확인
        Log.d("PhotoActivity", "Updated alergicList!: " + this.allergyList);
        Log.d("PhotoActivity", "Updated finalAlergicList!: " + this.finalAlergicList);
        syncIsSelectedList(1); // isSelectedList 동기화
        notifyDataSetChanged(); // 어댑터에 데이터 변경 알림
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
                    finalAlergicList.add(allergyItem); // 리스트에 추가 (액티비티의 리스트를 직접 수정)
                    Toast.makeText(v.getContext(), "추가됨: " + allergyItem, Toast.LENGTH_SHORT).show();
                } else {
                    // 비활성화 상태로 변경
                    isSelectedList.set(position, false);
                    ((AllergyViewHolder) holder).tvMini.setBackgroundResource(R.drawable.minisel);
                    finalAlergicList.remove(allergyItem); // 리스트에서 제거 (액티비티의 리스트를 직접 수정)
                    Toast.makeText(v.getContext(), "제거됨: " + allergyItem, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (holder instanceof PlusViewHolder) {
            // plus 이미지 아이템 처리
            ((PlusViewHolder) holder).itemView.setOnClickListener(v -> {
                // 새로운 액티비티를 띄우고 결과를 받아오기
                Intent intent = new Intent(v.getContext(), alergicActivity.class);

                // 현재 선택된 아이템을 전달 (예: selectedItems)
                intent.putExtra("selectedItems", (Serializable) finalAlergicList);

                // 액티비티를 띄우고 결과를 받기 위해 요청 코드 사용
                ((Activity) v.getContext()).startActivityForResult(intent, 101);
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