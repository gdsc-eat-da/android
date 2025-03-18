package root.dongmin.eat_da.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kakao.vectormap.LatLng;

import java.util.List;


import root.dongmin.eat_da.R;
import root.dongmin.eat_da.network.NeedPost;
import root.dongmin.eat_da.utils.DistanceCalculator;

public class MapDistanceAdapter extends RecyclerView.Adapter<MapDistanceAdapter.ViewHolder> {
    private Context context;
    private List<NeedPost> needPostList;
    private LatLng userLocation;
    private boolean isLocationReady =false;




    public MapDistanceAdapter(Context context, List<NeedPost> needPostList) {
        this.context = context;
        this.needPostList = needPostList;

    }

    public void updateUserLocation(LatLng newUserLocation) {
        this.userLocation = newUserLocation;
        this.isLocationReady = true;  // 위치 정보 준비 완료
        notifyDataSetChanged();  // 데이터 갱신
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mapdistance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NeedPost needPost = needPostList.get(position);
        holder.textNeedContents.setText(needPost.getContents());

        try {
            holder.textNickName.setText(needPost.getNickname()); // 닉네임을 UI에 표시

            // 위치 정보 준비 여부 확인
            if (!isLocationReady) {
                Log.e("MapDistanceAdapter", "위치 정보가 아직 준비되지 않았습니다.");
                holder.textDistance.setText("위치 정보 불가");
                return;
            }


            // 위도와 경도를 String에서 double로 변환
            double lat = Double.parseDouble(needPost.getLatitude());
            double lng = Double.parseDouble(needPost.getLongitude());

            Log.d("MapDistanceAdapter", "위도: " + lat + ", 경도: " + lng); // 위도와 경도 로그

            // LatLng 객체 생성
            LatLng postLocation = LatLng.from(lat, lng); // 수정된 부분
            Log.d("MapDistanceAdapter", "LatLng 객체: " + postLocation.toString()); // LatLng 객체 로그

            // 거리 계산
            float distance = DistanceCalculator.calculateDistance(userLocation, postLocation);

            // 계산된 거리 값 확인
            Log.d("MapDistanceAdapter", "계산된 거리: " + distance + "m");

            // 거리 값을 m 단위로 표시하거나 km 단위로 변환하여 표시
            if (distance == -1) {
                holder.textDistance.setText("거리 계산 불가");
            } else {
                if (distance < 1000) {
                    holder.textDistance.setText(String.format("%.1f m", distance));
                } else {
                    float km = distance / 1000;
                    holder.textDistance.setText(String.format("%.1f km", km));
                }
            }

        } catch (NumberFormatException e) {
            Log.e("MapDistanceAdapter", "위도/경도 변환 오류: " + needPost.getLatitude() + ", " + needPost.getLongitude(), e);
            holder.textDistance.setText("위치 정보 오류");
        }

        // SharedPreferences에서 이전에 저장된 좋아요 상태 불러오기
        SharedPreferences preferences = context.getSharedPreferences("likes", Context.MODE_PRIVATE);
        final boolean[] isLiked = {preferences.getBoolean("liked_" + needPost.getPostID(), false)}; // 기본값은 false

        // 하트 상태 설정
        holder.heart.setImageResource(isLiked[0] ? R.drawable.heartclicked : R.drawable.heartdefault);

        // 하트 클릭 리스너
        holder.heart.setOnClickListener(v -> {
            isLiked[0] = !isLiked[0]; // 상태 반전
            holder.heart.setImageResource(isLiked[0] ? R.drawable.heartclicked : R.drawable.heartdefault);

            // SharedPreferences에 좋아요 상태 저장
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("liked_" + needPost.getPostID(), isLiked[0]);
            editor.apply();

        });

    }



    @Override
    public int getItemCount() {
        return needPostList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDistance;
        TextView textNickName;
        TextView textNeedContents;
        ImageView heart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDistance = itemView.findViewById(R.id.textMeter);
            textNickName = itemView.findViewById(R.id.textNickName);
            textNeedContents = itemView.findViewById(R.id.textNeedContents);
            heart = itemView.findViewById(R.id.heart);
            // mapdistance.xml에 맞춰서 ID 설정
        }
    }
}
