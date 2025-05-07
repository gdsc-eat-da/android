package root.dongmin.eat_da.adapter;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import root.dongmin.eat_da.R;
import root.dongmin.eat_da.TestChatActivity;
import root.dongmin.eat_da.data.ChatData;

/**
 * RecyclerView의 어댑터 클래스 - 채팅 데이터를 표시하는 역할
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private List<ChatData> mDataset; // 채팅 메시지를 저장하는 리스트
    private String myNickName; // 현재 로그인한 사용자의 닉네임
    private Context context; // 현재 컨텍스트 (UI 관련 작업에 사용)

    public String profileUrl;

    /**
     * ViewHolder 클래스 - 하나의 채팅 메시지에 해당하는 뷰를 저장
     */
    // MyViewHolder 클래스 정의
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView TextView_nickname; // 닉네임을 표시할 TextView
        public TextView TextView_msg; // 메시지를 표시할 TextView
        public View tailView; // 꼬리표를 표시할 View
        public TextView TextView_date; // 날짜 구분선용 TextView
        public ImageView profile;

        public MyViewHolder(View v) {
            super(v);
            // XML에서 정의한 TextView와 View를 찾아 연결
            TextView_nickname = v.findViewById(R.id.TextView_nickname);//사실 닉네임이 아니라 읽음/안읽음 처리하는거임
            TextView_msg = v.findViewById(R.id.TextView_msg);
            tailView = v.findViewById(R.id.tailView); // 꼬리표 View
            profile = v.findViewById(R.id.profile);

            // 날짜 구분선용 TextView
            TextView_date = v.findViewById(R.id.TextView_date);
        }
    }


    /**
     * 생성자 - 어댑터가 사용할 데이터 및 컨텍스트를 받아 초기화
     */
    public ChatAdapter(List<ChatData> myDataset, Context context, String myNickName, String profileUrl) {
        this.mDataset = myDataset;
        this.context = context;
        this.myNickName = myNickName;
        this.profileUrl = profileUrl;
    }

    /**
     * 새로운 뷰 홀더를 생성하는 메서드 (ViewHolder 패턴 적용) start!
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        if (viewType == 1) { // 날짜 구분선
            view = inflater.inflate(R.layout.row_date_divider, parent, false);
        } else if (viewType == 2) { // 내 채팅
            view = inflater.inflate(R.layout.row_chat, parent, false);
        } else { // 상대방 채팅
            view = inflater.inflate(R.layout.row_chat_other, parent, false);
        }

        return new MyViewHolder(view);
    }


    @Override
    public int getItemViewType(int position) {// RecyclerView가 아이템을 그리기 전에 호출
        ChatData chat = mDataset.get(position);
        if (chat.isDateDivider()) {
            return 1; // 날짜 구분선
        } else if (chat.getNickname().equals(myNickName)) {
            return 2; // 내 채팅
        } else {
            return 3; // 상대방 채팅
        }
    }

    /**
     * 데이터와 뷰를 바인딩하는 메서드 (화면에 채팅 메시지 출력) update!
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatData chat = mDataset.get(position); // 현재 위치의 채팅 데이터 가져오기

        if (getItemViewType(position) == 1) { // 날짜 구분선인 경우!
            holder.TextView_date.setText(chat.getTime());
        } else {
            // 메시지 설정
            holder.TextView_msg.setText(chat.getMsg());

            Log.d("ChatAdapter", "내 닉네임: [" + myNickName + "]");
            Log.d("ChatAdapter", "메시지 닉네임: [" + chat.getNickname() + "]");
            Log.d("ChatAdapter", "프로필 URL: [" + profileUrl + "]"); // 프로필 URL 로그 추가
            Log.d("ChatAdapter", "읽었냐: " + chat.getIsnotread());

            // 현재 로그인한 사용자의 닉네임과 비교하여 정렬 방향 변경
            if (chat.getNickname().equals(myNickName)) {
                Log.d("ChatAdapter", "일치성공!!!!!!: [" + chat.getNickname() + "]");

                // 본인이 보낸 메시지는 오른쪽 정렬
                holder.TextView_msg.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);



                String[] parts = chat.getIsnotread().split("_");

                if(parts[1].equals("X"))
                {
                    holder.TextView_nickname.setText("읽음");
                }
                else {
                    holder.TextView_nickname.setText("안읽음");
                }


                // LinearLayout의 gravity를 'end'로 설정하여 오른쪽 정렬
                ((LinearLayout) holder.itemView).setGravity(Gravity.END);

            } else {
                // 상대방이 보낸 메시지인 경우
                if (position > 0) { // 이전 메시지가 있는 경우
                    ChatData prevChat = mDataset.get(position - 1); // 이전 메시지 데이터 가져오기

                    // prevChat과 chat이 null이 아닌지 확인
                    if (prevChat != null && chat != null) {
                        // prevChat.getNickname()과 chat.getNickname()이 null이 아닌지 확인
                        String prevNickname = prevChat.getNickname();
                        String currentNickname = chat.getNickname();
                        profileUrl = chat.getUrl();

                        Log.d("ChatAdapter", "프로필aaaaaaa:" + profileUrl);


                        if (prevNickname != null && currentNickname != null && prevNickname.equals(currentNickname)) {
                            // 닉네임이 같으면 프로필 이미지와 꼬리표를 숨김
                            if (holder.profile != null) {
                                holder.profile.setVisibility(View.GONE); // 프로필 이미지 비활성화
                            }
                            if (holder.tailView != null) {
                                holder.tailView.setVisibility(View.GONE); // 꼬리표 비활성화
                            }
                            // 아이템을 오른쪽으로 20dp 띄우기
                            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.TextView_msg.getLayoutParams();
                            if (layoutParams != null) {
                                // 오른쪽 마진을 20dp로 설정 (px로 변환 필요)
                                int marginRightInPx = (int) TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        20,
                                        holder.TextView_msg.getResources().getDisplayMetrics()
                                );
                                layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, marginRightInPx, layoutParams.bottomMargin);
                                holder.TextView_msg.setLayoutParams(layoutParams);
                            }



                        } else {
                            // 닉네임이 다르면 프로필 이미지와 꼬리표를 보이게 함
                            if (holder.profile != null) {
                                holder.profile.setVisibility(View.VISIBLE); // 프로필 이미지 활성화
                            }
                            if (holder.tailView != null) {
                                holder.tailView.setVisibility(View.VISIBLE); // 꼬리표 활성화
                            }

                            // Glide를 사용하여 프로필 이미지 로드
                            if (profileUrl != null && !profileUrl.isEmpty()) {
                                Glide.with(context)
                                        .load(profileUrl)
                                        .into(holder.profile);
                                        //.onLoadFailed(context.getDrawable(R.drawable.chatclicked)); // 로드 실패 시 기본 이미지 설정
                            } else {
                                Log.d("ChatAdapter", "프로필없나...:" + profileUrl);
                                holder.profile.setImageResource(R.drawable.chatclicked); // 기본 이미지 설정
                            }
                        }
                    }
                } else {
                    // 첫 번째 메시지인 경우 프로필 이미지와 꼬리표를 보이게 함
                    if (holder.profile != null) {
                        holder.profile.setVisibility(View.VISIBLE); // 프로필 이미지 활성화
                    }
                    if (holder.tailView != null) {
                        holder.tailView.setVisibility(View.VISIBLE); // 꼬리표 활성화
                    }

                    // Glide를 사용하여 프로필 이미지 로드
                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        Glide.with(context)
                                .load(profileUrl)
                                .into(holder.profile);
                                //.onLoadFailed(context.getDrawable(R.drawable.chatclicked)); // 로드 실패 시 기본 이미지 설정
                    } else {
                        Log.d("ChatAdapter", "프로필없나...:" + profileUrl);
                        holder.profile.setImageResource(R.drawable.chatclicked); // 기본 이미지 설정
                    }
                }

                // 상대방이 보낸 메시지는 왼쪽 정렬
                holder.TextView_msg.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

                // LinearLayout의 gravity를 'start'로 설정하여 왼쪽 정렬
                ((LinearLayout) holder.itemView).setGravity(Gravity.START);
            }
        }
    }


    /**
     * 새로운 채팅 메시지를 리스트에 추가하고 RecyclerView에 반영하는 메서드
     */
    public void addChat(ChatData chat) {
        if (mDataset.size() > 0) {
            // 이전 메시지의 요일 추출
            String prevDay = mDataset.get(mDataset.size() - 1).getTime().substring(0, 1);
            // 현재 메시지의 요일 추출
            String currentDay = chat.getTime().substring(0, 1);

            // 요일이 다르면 날짜 구분선을 mdataset을 통해 추가함(파이어베이스가 아니라 뷰어에 보여줄 리스트에 추가함)
            if (!prevDay.equals(currentDay)) {
                ChatData dateDivider = new ChatData();
                dateDivider.setTime(chat.getTime()); // 날짜 구분선에 시간 설정
                dateDivider.setDateDivider(true); // 날짜 구분선임을 표시
                mDataset.add(dateDivider);
            }
        }
        else {
            ChatData dateDivider = new ChatData();
            dateDivider.setTime(chat.getTime()); // 날짜 구분선에 시간 설정
            dateDivider.setDateDivider(true); // 날짜 구분선임을 표시
            mDataset.add(dateDivider);
        }

        // 새로운 채팅 메시지 추가(원래 기본적으로 추가되는것.)
        if(chat.getUrl() != null)
        {
            profileUrl = chat.getUrl();
        }
        mDataset.add(chat);
        notifyItemInserted(mDataset.size() - 1);
    }


    public void addprofile(String profileUrl)
    {
        this.profileUrl = profileUrl;
    }









//원래 있는 row chat 밑에 읽음/안읽음으로 바꾸기 그리고 Firebase 쿼리 사용해서 들어갈때마다? 아니면 1초?마다 x로 바꾸기
    //







    /**
     * 데이터셋의 크기를 반환하는 메서드
     */
    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }
    /**
     * 특정 위치의 채팅 데이터를 가져오는 메서드
     */
    public ChatData getChat(int position) {
        return mDataset != null ? mDataset.get(position) : null;
    }
}
