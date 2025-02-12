package root.dongmin.eat_da.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import root.dongmin.eat_da.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private List<String> chatRoomList;

    // 생성자
    public ChatRoomAdapter(List<String> chatRoomList) {
        this.chatRoomList = chatRoomList;
    }

    @NonNull
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

        // ":"을 기준으로 채팅방 정보를 분리
        String[] parts = chatRoom.split(":");
        String receivedID = parts[0]; // 받는 사람의 ID

        // Firebase에서 해당 ID에 맞는 프로필 이미지를 불러옴
        loadProfileImage(receivedID, holder.profileImageView);
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    // Firebase에서 프로필 이미지를 로드하는 메서드
    private void loadProfileImage(String receivedID, ShapeableImageView profileImageView) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        userRef.orderByChild("nickname").equalTo(receivedID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String profileImageUrl = snapshot.child("profileImage").getValue(String.class);

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            // Glide를 사용하여 프로필 이미지 로드
                            Glide.with(profileImageView.getContext())
                                    .load(profileImageUrl)
                                    .into(profileImageView);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 처리
            }
        });
    }

    // ViewHolder 정의
    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView chatRoomNameTextView;
        ShapeableImageView profileImageView; // 프로필 이미지를 위한 ImageView

        public ChatRoomViewHolder(View itemView) {
            super(itemView);
            chatRoomNameTextView = itemView.findViewById(R.id.chatRoomNameTextView);
            profileImageView = itemView.findViewById(R.id.profile); // 프로필 이미지
        }
    }
}
