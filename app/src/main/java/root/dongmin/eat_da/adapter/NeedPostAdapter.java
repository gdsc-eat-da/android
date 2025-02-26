package root.dongmin.eat_da.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import root.dongmin.eat_da.R;
import root.dongmin.eat_da.network.ApiService;
import root.dongmin.eat_da.network.NeedPost;
import root.dongmin.eat_da.network.RetrofitClient;

public class NeedPostAdapter extends RecyclerView.Adapter<NeedPostAdapter.ViewHolder> {
    private Context context;
    private List<NeedPost> needPostList;

    public NeedPostAdapter(Context context, List<NeedPost> needPostList) {
        this.context = context;
        this.needPostList = needPostList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_needpost, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NeedPost post = needPostList.get(position);
        holder.textNeedContents.setText(post.getContents());
        holder.textNeedIngredients.setText(post.getIngredients());

        if (post.isFace()) {
            // face가 0일 때 toface 이미지를 보이게 설정
            holder.tofaceImageView.setVisibility(View.VISIBLE);
            holder.tonofaceImageView.setVisibility(View.GONE);
        } else {
            // face가 1일 때 tonoface 이미지를 보이게 설정
            holder.tofaceImageView.setVisibility(View.GONE);
            holder.tonofaceImageView.setVisibility(View.VISIBLE);
        }

        // 🔥 게시글 길게 클릭 시 삭제 기능 추가
        holder.itemView.setOnLongClickListener(v -> {
            getNickname(nickname -> {
                if (nickname != null && nickname.equals(post.getNickname())) {
                    Log.d("NeedPostAdapter", "로그인한 사용자 닉네임: " + nickname);
                    Log.d("NeedPostAdapter", "게시글 작성자 닉네임: " + post.getNickname());

                    // ✅ 삭제 확인 다이얼로그
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("삭제하시겠습니까?")
                            .setMessage("이 게시물을 삭제하시겠습니까?")
                            .setPositiveButton("확인", (dialogInterface, which) -> {
                                deleteNeedPost(post.getPostID());
                            })
                            .setNegativeButton("취소", null)
                            .show();

                    // "확인" 버튼의 텍스트 색을 검정으로 설정
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

                    // "취소" 버튼의 텍스트 색을 검정으로 설정
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);

                } else {
                    Toast.makeText(context, "자신의 게시물만 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
            });

            return true;  // 이벤트 소비 (다른 이벤트 방지)
        });
        ;
    }

    @Override
    public int getItemCount() {
        return needPostList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNeedContents, textNeedIngredients;
        ImageView tofaceImageView, tonofaceImageView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNeedContents = itemView.findViewById(R.id.textNeedContents);
            textNeedIngredients = itemView.findViewById(R.id.textNeedIngredients);
            tofaceImageView = itemView.findViewById(R.id.needtoface);
            tonofaceImageView = itemView.findViewById(R.id.needtonoface);
        }
    }

    // ✅ Firebase에서 로그인한 사용자의 닉네임 가져오기
    private void getNickname(OnNicknameReceivedListener listener) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(uid);

            userRef.child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String nickname = dataSnapshot.getValue(String.class);
                    if (nickname != null) {
                        Log.d("Nickname", "닉네임 가져옴: " + nickname);
                        listener.onReceived(nickname);
                    } else {
                        Log.e("Nickname", "닉네임이 없습니다.");
                        listener.onReceived(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "닉네임 불러오기 실패: " + databaseError.getMessage());
                    listener.onReceived(null);
                }
            });
        } else {
            Log.e("Nickname", "FirebaseUser가 null입니다.");
            listener.onReceived(null);
        }
    }

    // ✅ 서버에서 게시물 삭제 요청
    private void deleteNeedPost(String postID) {
        Log.d("DeleteNeedPost", "삭제할 게시물의 postID: " + postID);
        ApiService apiService = RetrofitClient.getApiService(context);
        Call<ResponseBody> call = apiService.deleteNeedPost(postID);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 삭제 후 RecyclerView에서 해당 게시물 제거
                    needPostList.removeIf(post -> post.getPostID().equals(postID));
                    notifyDataSetChanged();  // 리스트 갱신
                    Toast.makeText(context, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(context, "네트워크 오류로 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 닉네임을 가져오는 콜백 인터페이스
    interface OnNicknameReceivedListener {
        void onReceived(String nickname);
    }
}
