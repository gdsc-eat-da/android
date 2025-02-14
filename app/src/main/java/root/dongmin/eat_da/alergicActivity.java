package root.dongmin.eat_da;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alergic);

        recyclerView = findViewById(R.id.recyclerViewBig);
        selectedRecyclerView = findViewById(R.id.recyclerViewMini); // 새로운 리사이클러뷰

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);  // 4열로 설정
        selectedRecyclerView.setLayoutManager(layoutManager);

        // ItemDecoration을 통해 간격 조정 (아이템이 왼쪽 정렬되도록)
        selectedRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                // 첫 번째 열에서 간격을 줄여서 왼쪽 정렬을 하도록 설정
                int position = parent.getChildAdapterPosition(view);
                int spanCount = 4; // 4열

                if (position % spanCount != 0) {
                    outRect.left = 0;  // 첫 번째 열이 아니면 간격을 없앰
                } else {
                    outRect.left = 0;  // 첫 번째 열이 아닌 경우에만 왼쪽 간격 추가 (없음)
                }

                outRect.right = 0; // 오른쪽 간격 없애기
                outRect.top = 4;  // 상단 간격
                outRect.bottom = 4;  // 하단 간격
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
            Toast.makeText(getApplicationContext(), item + " 선택됨!", Toast.LENGTH_SHORT).show();
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

    private void setClickListener(int textViewId) {
        TextView textView = findViewById(textViewId);
        if (textView != null) {
            textView.setOnClickListener(v -> {
                String viewId = getResources().getResourceEntryName(textViewId);
                List<String> items = alergicMap.get(viewId);

                if (items != null && !items.isEmpty()) {
                    Toast.makeText(getApplicationContext(), String.join(", ", items) + " 클릭됨!", Toast.LENGTH_SHORT).show();
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

    public class AlergicAdapter extends RecyclerView.Adapter<AlergicAdapter.ViewHolder> {

        private List<String> items;
        private OnItemClickListener onItemClickListener;

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

            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvInsideImage;

            public ViewHolder(View itemView) {
                super(itemView);
                tvInsideImage = itemView.findViewById(R.id.tv_inside_image);
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
