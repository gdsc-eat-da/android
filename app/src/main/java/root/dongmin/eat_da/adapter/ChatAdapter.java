package root.dongmin.eat_da.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import root.dongmin.eat_da.R;
import root.dongmin.eat_da.data.ChatData;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import root.dongmin.eat_da.R;
import root.dongmin.eat_da.data.User;

/**
 * Created by Dongmin on 2025. 02. 03..
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private List<ChatData> mDataset;
    private String myNickName;
    private Context context;

    // ViewHolder 정의
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView TextView_nickname;  // 변경된 ID
        public TextView TextView_msg;       // 변경된 ID

        public MyViewHolder(View v) {
            super(v);
            TextView_nickname = v.findViewById(R.id.TextView_nickname);  // 변경된 ID
            TextView_msg = v.findViewById(R.id.TextView_msg);            // 변경된 ID
        }
    }

    // 생성자
    public ChatAdapter(List<ChatData> myDataset, Context context, String myNickName) {
        this.mDataset = myDataset;
        this.context = context;
        this.myNickName = myNickName;
    }

    // 새로운 뷰 생성 (뷰홀더 반환)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 새로운 뷰 생성
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_chat, parent, false);
        return new MyViewHolder(v);
    }

    // 데이터 바인딩 (뷰와 데이터 연결)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ChatData chat = mDataset.get(position);

        holder.TextView_nickname.setText(chat.getNickname());
        holder.TextView_msg.setText(chat.getMsg());

        // 내 이름과 비교하여 메시지 정렬
        if (chat.getNickname().equals(this.myNickName)) {
            holder.TextView_msg.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            holder.TextView_nickname.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        } else {
            holder.TextView_msg.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.TextView_nickname.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }
    }

    // 데이터셋 크기 반환
    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    // 채팅 데이터 추가
    public void addChat(ChatData chat) {
        mDataset.add(chat);
        notifyItemInserted(mDataset.size() - 1);
    }

    // 특정 채팅 가져오기
    public ChatData getChat(int position) {
        return mDataset != null ? mDataset.get(position) : null;
    }
}
