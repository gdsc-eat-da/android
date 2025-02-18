package root.dongmin.eat_da;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import root.dongmin.eat_da.R;

public class alergicActivity extends AppCompatActivity {

    private RecyclerView recyclerView, selectedRecyclerView;
    private AlergicAdapter alergicAdapter;
    private MiniAlergicAdapter miniAlergicAdapter;

    private List<String> alergicItems;
    private List<String> selectedItems;  // 클릭된 아이템 저장 리스트
    private Map<String, List<String>> alergicMap;

    private List<String> modifiedItems; // 수정된 데이터 리스트 (예시)
    public MaterialButton btnComplete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alergic);



        // 기존의 selectedItems를 받기 (PhotoActivity에서 보낸 값)
        Intent intent = getIntent();
        selectedItems = (List<String>) intent.getSerializableExtra("selectedItems");

        recyclerView = findViewById(R.id.recyclerViewBig);
        selectedRecyclerView = findViewById(R.id.recyclerViewMini); // 새로운 리사이클러뷰
        btnComplete = findViewById(R.id.btn_complete);
        btnComplete.setOnClickListener(v -> {
            modifiedItems = selectedItems != null ? new ArrayList<>(selectedItems) : new ArrayList<>();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("modifiedItems", (Serializable) modifiedItems); // 수정된 데이터 전달
            setResult(RESULT_OK, resultIntent); // 결과 전달
            finish(); // AlergicActivity 종료
        });
        ImageButton imageButton = findViewById(R.id.imageButton5);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 클릭 시 showCustomDialog() 호출
                showCustomDialog();
            }
        });


        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);  // 4열로 설정
        selectedRecyclerView.setLayoutManager(layoutManager);
        //selectedRecyclerView.setLayoutManager(layoutManager);

// ItemDecoration을 통해 간격 조정 (아이템이 가로로 가득 차면 다음 줄로 넘어가게 설정)
        selectedRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                // 아이템 위치 확인
                int position = parent.getChildAdapterPosition(view);
                int spanCount = 4; // 4열
                int column = position % spanCount; // 현재 아이템이 위치한 열

                // 아이템 간격 설정
                int spacing = 1; // 간격 (px)

                // 왼쪽 간격 설정 (첫 번째 열은 간격을 주지 않음)
                if (column != 0) {
                    outRect.left = spacing;
                } else {
                    outRect.left = 0;
                }

                // 오른쪽 간격 설정
                if (column != spanCount - 1) {
                    outRect.right = spacing;
                } else {
                    outRect.right = 0;
                }

                // 상단과 하단 간격 설정
                outRect.top = spacing;  // 상단 간격
                outRect.bottom = spacing;  // 하단 간격
            }
        });


        alergicItems = new ArrayList<>();
        selectedItems = new ArrayList<>(); // 선택된 항목 저장 리스트

        alergicItems.add("사과");
        alergicItems.add("토마토");
        alergicItems.add("파인애플");

        alergicMap = new HashMap<>();
        alergicMap.put("TextView_msgegg", new ArrayList<String>() {{ add("계란"); }});
        alergicMap.put("textView_msgmilk", new ArrayList<String>() {{ add("우유"); }});
        alergicMap.put("textView_msgfruitvega", new ArrayList<String>() {{ add("복숭아"); }});
        alergicMap.put("textView_msgseed", new ArrayList<String>() {{ add("메밀"); add("대두"); add("밀"); add("잣"); }});
        alergicMap.put("textView_msgnut", new ArrayList<String>() {{ add("땅콩"); add("호두"); }});
        alergicMap.put("textView_msghardseafood", new ArrayList<String>() {{ add("게"); add("새우"); }});
        alergicMap.put("textView_msgseafood", new ArrayList<String>() {{ add("오징어"); add("고등어"); }});
        alergicMap.put("textView_msgjoga", new ArrayList<String>() {{ add("조개"); }});
        alergicMap.put("textView_msgmeat", new ArrayList<String>() {{ add("닭고기"); add("돼지고기"); add("쇠고기"); }});
        alergicMap.put("textView_msgahwang", new ArrayList<String>() {{ add("아황산류"); }});
        alergicMap.put("textView_msgvega", new ArrayList<String>() {{ add("토마토"); }});

        alergicAdapter = new AlergicAdapter(alergicItems);
        miniAlergicAdapter = new MiniAlergicAdapter(selectedItems);

        recyclerView.setAdapter(alergicAdapter);
        selectedRecyclerView.setAdapter(miniAlergicAdapter);

        alergicAdapter.setOnItemClickListener(item -> {
            if (!selectedItems.contains(item)) {
                selectedItems.add(item);
                miniAlergicAdapter.notifyDataSetChanged();
            }
            //Toast.makeText(getApplicationContext(), item + " 선택됨!", Toast.LENGTH_SHORT).show();
        });

        setClickListener(R.id.TextView_msgegg);
        setClickListener(R.id.textView_msgmilk);
        setClickListener(R.id.textView_msgfruitvega);
        setClickListener(R.id.textView_msgseed);
        setClickListener(R.id.textView_msgnut);
        setClickListener(R.id.textView_msghardseafood);
        setClickListener(R.id.textView_msgseafood);
        setClickListener(R.id.textView_msgjoga);
        setClickListener(R.id.textView_msgmeat);
        setClickListener(R.id.textView_msgahwang);
        setClickListener(R.id.textView_msgvega);
    }

    private TextView selectedTextView = null; // 현재 선택된 TextView 저장
    private ImageView selectedImageView = null; // 현재 선택된 ImageView 저장

    private void setClickListener(int textViewId) {
        TextView textView = findViewById(textViewId);
        if (textView != null) {
            textView.setOnClickListener(v -> {
                // 기존에 선택된 TextView가 있다면 배경을 원래대로 변경
                if (selectedTextView != null) {
                    selectedTextView.setBackgroundResource(R.drawable.minisel);
                }

                // 새로 선택된 TextView의 배경을 변경하고 저장
                textView.setBackgroundResource(R.drawable.miniunsel);
                selectedTextView = textView;

                String viewId = getResources().getResourceEntryName(textViewId);
                List<String> items = alergicMap.get(viewId);

                if (items != null && !items.isEmpty()) {
                    //Toast.makeText(getApplicationContext(), String.join(", ", items) + " 클릭됨!", Toast.LENGTH_SHORT).show();
                    updateRecyclerView(items);
                } else {
                    Toast.makeText(getApplicationContext(), "해당 항목이 없습니다!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateRecyclerView(List<String> newItems) {
        alergicItems.clear();
        alergicItems.addAll(newItems);
        alergicAdapter.notifyDataSetChanged();
    }


    private void showCustomDialog() {
        // 1. 다이얼로그 생성
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_item);

        // 2. 다이얼로그 내부 UI 요소 가져오기
        EditText editTextInput = dialog.findViewById(R.id.editTextInput);
        ImageView btnAdd = dialog.findViewById(R.id.btnAdd);
        //Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // 3. 추가 버튼 클릭 시 아이템 리스트에 추가
        btnAdd.setOnClickListener(v -> {
            String newItem = editTextInput.getText().toString().trim();
            if (!newItem.isEmpty() && !selectedItems.contains(newItem)) {
                selectedItems.add(newItem);
                miniAlergicAdapter.notifyDataSetChanged();
                //Toast.makeText(getApplicationContext(), newItem + " 추가됨!", Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // 다이얼로그 닫기
            } else {
                Toast.makeText(getApplicationContext(), "올바른 값을 입력하세요.", Toast.LENGTH_SHORT).show();

            }
        });


        // 5. 다이얼로그 표시
        dialog.show();
    }


    public class AlergicAdapter extends RecyclerView.Adapter<AlergicAdapter.ViewHolder> {

        private List<String> items;
        private OnItemClickListener onItemClickListener;
        private ViewHolder selectedViewHolder = null; // 선택된 아이템 저장

        public AlergicAdapter(List<String> items) {
            this.items = items;
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.onItemClickListener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alergic, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String item = items.get(position);
            holder.tvInsideImage.setText(item);
            holder.imageView.setImageResource(R.drawable.bigunselected);

            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(item);
                }

                // 기존 선택된 항목을 원래 상태로 되돌림
                if (selectedViewHolder != null) {
                    selectedViewHolder.imageView.setImageResource(R.drawable.bigunselected);
                }

                // 새로운 선택 항목 변경
                holder.imageView.setImageResource(R.drawable.bigselected);
                selectedViewHolder = holder;
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvInsideImage;
            ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                tvInsideImage = itemView.findViewById(R.id.tv_inside_image);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }


    public class MiniAlergicAdapter extends RecyclerView.Adapter<MiniAlergicAdapter.ViewHolder> {

        private List<String> items;

        public MiniAlergicAdapter(List<String> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_minialergic, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String item = items.get(position);
            holder.textView.setText(item);
            btnComplete = findViewById(R.id.btn_complete);
            btnComplete.setText(selectedItems.size() + "개 선택완료");

            holder.itemView.setOnClickListener(v -> {
                items.remove(position); // 리스트에서 삭제
                notifyDataSetChanged(); // RecyclerView 업데이트
                //Toast.makeText(v.getContext(), item + " 삭제됨!", Toast.LENGTH_SHORT).show();
                btnComplete = findViewById(R.id.btn_complete);
                btnComplete.setText(selectedItems.size() + "개 선택완료");

            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.tv_mini);
            }
        }
    }


    public interface OnItemClickListener {
        void onItemClick(String item);
    }
}
