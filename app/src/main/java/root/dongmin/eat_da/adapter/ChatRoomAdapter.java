package root.dongmin.eat_da.adapter;

import android.content.Intent;
import android.util.Log;
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
    public String lastMessage = null; //마지막에 한 채팅내용 가져오는것

    public int isnotread = 0;

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
        Log.d("LAST_DEBUG", "챗룸은::::::" +chatRoom);
        // "_" 기준으로 문자열 분리
        String[] parts1 = chatRoom.split("_");
        String chatRoomName = parts1[0]; // "_" 앞부분만 가져옴
        String chatRoomdis = parts1[2];
        String chatRoomdisA[] = chatRoomdis.split("\\|");
        chatRoomdis = chatRoomdisA[0];
        String []partsForLast = chatRoom.split("\\|");
        String conornot = partsForLast[2];
        Log.d("LAST_DEBUG", "마지막 쓴 댓글--------:" + partsForLast[1]);


        //만약 conornot(안읽은 메세지만 표시)가 1일 경우
        if(conornot.equals("1"))
        {

            checkIsNotRead(partsForLast[1], new IsNotReadCallBack() {
                @Override
                public void onIsNotReadChecked(int isnotreadOne) {
                    if (isnotreadOne == 1) {
                        Log.d("IsNotRead", "isnotread에 'O'가 포함되어 있습니다.");
                        holder.itemView.setVisibility(View.GONE); // 뷰 숨기기
                    } else {
                        Log.d("IsNotRead", "isnotread에 'O'가 포함되어 있지 않습니다.");
                        holder.itemView.setVisibility(View.VISIBLE); // 뷰 보이기
                    }
                }
            });
        }
        else {
            holder.itemView.setVisibility(View.VISIBLE); // 기본적으로 뷰 보이기
        }





        // 콜백을 전달하여 getLastMessage 호출
        getLastMessage(partsForLast[1], new LastMessageCallback() {
            @Override
            public void onLastMessageReceived(String message) {
                if (message != null) {
                    // 마지막 메시지를 UI에 반영
                    holder.chatRoomSubtitleTextView.setText(message);
                } else {
                    holder.chatRoomSubtitleTextView.setText("no message");
                }
            }
        });

        // 채팅방 이름을 TextView에 설정
        holder.chatRoomNameTextView.setText(chatRoomName);
        holder.distanceView.setText(chatRoomdis + "km");

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
        TextView chatRoomSubtitleTextView;//나중에 아래 채팅도 만들자
        TextView distanceView;
        ShapeableImageView profileImageView; // 프로필 이미지를 위한 ImageView

        public ChatRoomViewHolder(View itemView) {
            super(itemView);
            chatRoomNameTextView = itemView.findViewById(R.id.chatRoomNameTextView);
            distanceView = itemView.findViewById(R.id.distanceView);
            profileImageView = itemView.findViewById(R.id.profile); // 프로필 이미지
            chatRoomSubtitleTextView = itemView.findViewById(R.id.chatRoomSubtitleTextView); // 초기화 추가
        }
    }



    public void getLastMessage(String chatRoomId, final LastMessageCallback callback) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chat").child(chatRoomId);

        chatRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lastMessage = null;

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    lastMessage = messageSnapshot.child("msg").getValue(String.class);
                }

                if (callback != null) {
                    callback.onLastMessageReceived(lastMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "채팅방 " + chatRoomId + "의 마지막 메시지 가져오기 실패", error.toException());
                if (callback != null) {
                    callback.onLastMessageReceived(null);
                }
            }
        });
    }

    public interface LastMessageCallback {
        void onLastMessageReceived(String message);
    }




    public void checkIsNotRead(String chatRoomInfo, final IsNotReadCallBack callBack)//한번이라도 읽었나 안읽었나 판단하는 비동기 함수
    {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chat").child(chatRoomInfo);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isnotread = 0;

                for(DataSnapshot messageSnapshot : snapshot.getChildren())
                {
                    String isnotreada = messageSnapshot.child("isnotread").getValue(String.class);

                    if(isnotreada != null && isnotreada.endsWith("_O"))
                    {
                        isnotread = 1;
                        break;
                    }
                }

                if(callBack != null)
                {
                    callBack.onIsNotReadChecked(isnotread);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

                Log.e("FirebaseError", "isnotread 조회 실패: " + error.getMessage());
                if (callBack != null)
                {
                    callBack.onIsNotReadChecked(0); // 오류 시 기본값 0 반환
                }
            }
        });
    }


    public interface IsNotReadCallBack
    {
        void onIsNotReadChecked(int isnotreadOne);
    }






}
