package root.dongmin.eat_da.adapter;

import android.content.Intent;
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
import root.dongmin.eat_da.UserFindActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private List<String> chatRoomList;
    private int isnotMine; // isnotMine 변수 추가

    // 생성자
    public ChatRoomAdapter(List<String> chatRoomList) {
        this.chatRoomList = chatRoomList;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // RecyclerView의 각 항목 레이아웃을 설정
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);

        // 클릭 리스너 설정
        view.setOnClickListener(v -> {
            // View가 속한 RecyclerView에서 아이템의 포지션을 가져옵니다.
            RecyclerView recyclerView = (RecyclerView) parent;
            int position = recyclerView.getChildAdapterPosition(v);  // RecyclerView에서 직접 호출

            if (position != RecyclerView.NO_POSITION) {
                // 클릭된 항목의 chatRoom 정보 가져오기
                String selectedChatRoom = chatRoomList.get(position);

                // UserFind 액티비티로 채팅방 정보 전달
                Intent intent = new Intent(v.getContext(), UserFindActivity.class);
                intent.putExtra("chatRoom", selectedChatRoom); // 클릭된 채팅방 정보 전달
                intent.putExtra("isnotMinea", isnotMine); // isnotMine 값 추가
                v.getContext().startActivity(intent);
            }
        });

        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatRoomViewHolder holder, int position) {
        // 각 채팅방의 이름을 해당 항목에 바인딩
        String chatRoom = chatRoomList.get(position);
        // "_" 기준으로 문자열 분리
        String[] parts1 = chatRoom.split("_");
        String chatRoomName = parts1[0]; // "_" 앞부분만 가져옴

        // 채팅방 이름을 TextView에 설정
        holder.chatRoomNameTextView.setText(chatRoomName);

        // ":"을 기준으로 채팅방 정보를 분리
        String[] parts2 = chatRoom.split(":");
        String receivedID = parts2[0]; // 받는 사람의 ID

        // Firebase에서 해당 ID에 맞는 프로필 이미지를 불러옴
        loadProfileImage(receivedID, holder.profileImageView);
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    // isnotMine 값을 외부에서 업데이트 할 수 있게 하는 메서드
    public void setIsNotMine(int isnotMine) {
        this.isnotMine = isnotMine;
        notifyDataSetChanged(); // 값 변경 시 RecyclerView 갱신
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
