package root.dongmin.eat_da;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexWrap;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

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














        //이거 수정 ㄱㄱ


        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(this);
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW); // 가로 방향으로 아이템 배치
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP); // 여러 줄로 표시
        flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START); // 왼쪽부터 아이템 배치
        selectedRecyclerView.setLayoutManager(flexboxLayoutManager);

        // 아이템 간격 조정
        selectedRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                int spacing = 0; // 간격 (px)
                outRect.left = spacing;
                outRect.right = spacing;
                outRect.top = spacing;
                outRect.bottom = spacing;
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
        miniAlergicAdapter = new MiniAlergicAdapter(this, selectedItems, btnComplete);

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
        private Context context;
        private Button btnComplete; // 버튼 추가
        private List<String> selectedItems = new ArrayList<>(); // 선택된 아이템 목록

        public MiniAlergicAdapter(Context context, List<String> items, Button btnComplete) {
            this.context = context;
            this.items = items;
            this.btnComplete = btnComplete;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_minialergic, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String item = items.get(position);


            //SpannableStringBuilder를 사용하여 텍스트에 이미지 추가
            SpannableStringBuilder builder = new SpannableStringBuilder(item + " ");
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.exex);
            if (drawable != null) {
                drawable.setBounds(0, 0, 30, 30);
                ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                builder.setSpan(imageSpan, builder.length() - 1, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            holder.textView.setText(builder);

            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.itemView.setLayoutParams(layoutParams);


            btnComplete.setText(items.size() + "개 선택완료");

            holder.itemView.setOnClickListener(v -> {
                // 아이템을 클릭했을 때, items에서만 해당 아이템 삭제
                String selectedItem = items.get(position);
                items.remove(position);  // items 리스트에서 삭제
                notifyDataSetChanged();
                btnComplete.setText(items.size() + "개 선택완료");  // 남은 개수로 버튼 텍스트 업데이트
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
