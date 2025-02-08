package root.dongmin.eat_da.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import root.dongmin.eat_da.R;
import root.dongmin.eat_da.data.ChatData;

/**
 * RecyclerView의 어댑터 클래스 - 채팅 데이터를 표시하는 역할
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private List<ChatData> mDataset; // 채팅 메시지를 저장하는 리스트
    private String myNickName; // 현재 로그인한 사용자의 닉네임
    private Context context; // 현재 컨텍스트 (UI 관련 작업에 사용)

    /**
     * ViewHolder 클래스 - 하나의 채팅 메시지에 해당하는 뷰를 저장
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView TextView_nickname; // 닉네임을 표시할 TextView
        public TextView TextView_msg; // 메시지를 표시할 TextView

        public MyViewHolder(View v) {
            super(v);
            // XML에서 정의한 TextView를 찾아 연결
            TextView_nickname = v.findViewById(R.id.TextView_nickname);
            TextView_msg = v.findViewById(R.id.TextView_msg);
        }
    }

    /**
     * 생성자 - 어댑터가 사용할 데이터 및 컨텍스트를 받아 초기화
     */
    public ChatAdapter(List<ChatData> myDataset, Context context, String myNickName) {
        this.mDataset = myDataset;
        this.context = context;
        this.myNickName = myNickName;
    }

    /**
     * 새로운 뷰 홀더를 생성하는 메서드 (ViewHolder 패턴 적용) start!
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 레이아웃을 인플레이트하여 하나의 채팅 메시지 아이템을 생성
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_chat, parent, false);
        return new MyViewHolder(v);
    }

    /**
     * 데이터와 뷰를 바인딩하는 메서드 (화면에 채팅 메시지 출력) update!
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatData chat = mDataset.get(position); // 현재 위치의 채팅 데이터 가져오기

        // 닉네임과 메시지 설정
        holder.TextView_nickname.setText(chat.getNickname());
        holder.TextView_msg.setText(chat.getMsg());

        Log.d("ChatAdapter", "내 닉네임: [" + myNickName + "]");
        Log.d("ChatAdapter", "메시지 닉네임: [" + chat.getNickname() + "]");

        // 현재 로그인한 사용자의 닉네임과 비교하여 정렬 방향 변경
        if (chat.getNickname().equals(myNickName)) {
            Log.d("ChatAdapter", "일치성공!!!!!!: [" + chat.getNickname() + "]");
            // 본인이 보낸 메시지는 오른쪽 정렬
            holder.TextView_msg.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            holder.TextView_nickname.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

            // LinearLayout의 gravity를 'end'로 설정하여 오른쪽 정렬
            ((LinearLayout) holder.itemView).setGravity(Gravity.END);

            // 닉네임을 오른쪽 정렬로 변경
            holder.TextView_nickname.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
        } else {
            // 상대방이 보낸 메시지는 왼쪽 정렬
            holder.TextView_msg.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.TextView_nickname.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

            // LinearLayout의 gravity를 'start'로 설정하여 왼쪽 정렬
            ((LinearLayout) holder.itemView).setGravity(Gravity.START);

            // 닉네임을 왼쪽 정렬로 변경
            holder.TextView_nickname.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.START));
        }
    }




    /**
     * 데이터셋의 크기를 반환하는 메서드
     */
    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    /**
     * 새로운 채팅 메시지를 리스트에 추가하고 RecyclerView에 반영하는 메서드
     */
    public void addChat(ChatData chat) {
        mDataset.add(chat); // 리스트에 새로운 채팅 데이터 추가
        notifyItemInserted(mDataset.size() - 1); // 추가된 아이템을 화면에 반영
    }

    /**
     * 특정 위치의 채팅 데이터를 가져오는 메서드
     */
    public ChatData getChat(int position) {
        return mDataset != null ? mDataset.get(position) : null;
    }
}
