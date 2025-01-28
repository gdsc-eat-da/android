package root.dongmin.eat_da.adapter;

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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final ArrayList<User> userList;

    // 생성자
    public UserAdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    // ViewHolder 클래스
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.name_text);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 레이아웃 초기화
        View view = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // ViewHolder 데이터 바인딩 코드 작성 필요
        // 데이터 바인딩
        User currentUser = userList.get(position);
        holder.nameText.setText(currentUser.getName());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}

