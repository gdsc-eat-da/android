package root.dongmin.eat_da.adapter;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import root.dongmin.eat_da.R;
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


public class HashtagAdapter extends RecyclerView.Adapter<HashtagAdapter.HashtagViewHolder> {


    private List<String> hashList; // 알레르기 아이템 목록 (액티비티의 리스트를 참조)
    private List<String> finalHashList; // 선택된 알레르기 아이템 목록 (액티비티의 리스트를 참조)
    private List<Boolean> isSelectedList; // 아이템의 선택 상태 리스트


    // 생성자
    public HashtagAdapter(List<String> hashList, List<String> finalHashList) {
        this.hashList = hashList;
        this.finalHashList = finalHashList;
        this.isSelectedList = new ArrayList<>();
        syncIsSelectedList(); // isSelectedList 초기화
    }

    // isSelectedList를 allergyList와 동기화
    private void syncIsSelectedList() {
        while (isSelectedList.size() < hashList.size()) {
            isSelectedList.add(false); // 기본값은 true 왜냐면 알레르기 거기에서 이미 선택한 거니까
        }
    }
    private void syncIsSelectedList(int a) {
        int aa = a;
        if(aa == 1)
        {
            while (isSelectedList.size() < hashList.size()) {
                isSelectedList.add(true); // 기본값은 true 왜냐면 알레르기 거기에서 이미 선택한 거니까
            }
        }

    }

    // ViewHolder 생성
    @NonNull
    @Override
    public HashtagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_minialergic, parent, false);
        return new HashtagViewHolder(view);
    }

    // 데이터 바인딩
    @Override
    public void onBindViewHolder(@NonNull HashtagViewHolder holder, int position) {
        String hashItem = hashList.get(position);
        ((HashtagViewHolder) holder).bind(hashItem);

        // 아이템의 선택 상태에 따라 배경 설정
        if (isSelectedList.get(position)) {
            ((HashtagViewHolder) holder).tvMini.setBackgroundResource(R.drawable.miniunsel); // 활성화 상태
        } else {
            ((HashtagViewHolder) holder).tvMini.setBackgroundResource(R.drawable.minisel); // 비활성화 상태
        }

        // 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener(v -> {
            boolean isSelected = isSelectedList.get(position);

            if (!isSelected) {
                // 활성화 상태로 변경
                isSelectedList.set(position, true);
                ((HashtagViewHolder) holder).tvMini.setBackgroundResource(R.drawable.miniunsel);
                finalHashList.add(hashItem); // 리스트에 추가 (액티비티의 리스트를 직접 수정)
                Toast.makeText(v.getContext(), "added: " + hashItem, Toast.LENGTH_SHORT).show();
            } else {
                // 비활성화 상태로 변경
                isSelectedList.set(position, false);
                ((HashtagViewHolder) holder).tvMini.setBackgroundResource(R.drawable.minisel);
                finalHashList.remove(hashItem); // 리스트에서 제거 (액티비티의 리스트를 직접 수정)
                Toast.makeText(v.getContext(), "deleted: " + hashItem, Toast.LENGTH_SHORT).show();
            }
        });



    }

    // 아이템 개수 반환
    @Override
    public int getItemCount() {
        return hashList.size();
    }

    // ViewHolder 클래스(말풍선 말하는거임)
    public static class HashtagViewHolder extends RecyclerView.ViewHolder {
        TextView tvMini;

        public HashtagViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMini = itemView.findViewById(R.id.tv_mini);
        }
        public void bind(String hashitem) {
            tvMini.setText(hashitem);
        }
    }




    public void updateHashList(String hashtag) {


        if(!this.hashList.contains(hashtag)){
            this.hashList.add(hashtag);

            if(!this.finalHashList.contains(hashtag)){
                this.finalHashList.add(hashtag);
            }
        }
        else if(this.hashList.contains(hashtag))
        {
            int index = this.hashList.indexOf(hashtag);
            this.isSelectedList.set(index,true);

            if(!this.finalHashList.contains(hashtag)){
                this.finalHashList.add(hashtag);
            }
        }


        // 로그로 확인
        Log.d("PhotoActivity", "Updated hashList!: " + this.hashList);
        Log.d("PhotoActivity", "Updated finalHashList!: " + this.finalHashList);
        syncIsSelectedList(1); // isSelectedList 동기화
        notifyDataSetChanged(); // 어댑터에 데이터 변경 알림
    }





}