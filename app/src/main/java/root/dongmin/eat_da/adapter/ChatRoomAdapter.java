package root.dongmin.eat_da.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import root.dongmin.eat_da.R;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private List<String> chatRoomList;

    // 생성자
    public ChatRoomAdapter(List<String> chatRoomList) {
        this.chatRoomList = chatRoomList;
    }

    @Override
    public ChatRoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // RecyclerView의 각 항목 레이아웃을 설정
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatRoomViewHolder holder, int position) {
        // 각 채팅방의 이름을 해당 항목에 바인딩
        String chatRoom = chatRoomList.get(position);
        holder.chatRoomNameTextView.setText(chatRoom);
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    // ViewHolder 정의
    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView chatRoomNameTextView;

        public ChatRoomViewHolder(View itemView) {
            super(itemView);
            chatRoomNameTextView = itemView.findViewById(R.id.chatRoomNameTextView);
        }
    }
}
