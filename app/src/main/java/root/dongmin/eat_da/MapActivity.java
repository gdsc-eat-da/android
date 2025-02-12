package root.dongmin.eat_da;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;

public class MapActivity extends AppCompatActivity {
    MapView mapView;
    KakaoMap kakaoMap;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            private int previousItemId = R.id.work_load; // 초기 선택된 아이콘
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (previousItemId == item.getItemId()) {
                    return false; // 동일한 아이템 클릭 방지
                }

                // 1️⃣ 이전 아이콘을 default로 변경
                updateIcon(previousItemId, false);

                // 2️⃣ 현재 클릭된 아이콘을 clicked 상태로 변경
                updateIcon(item.getItemId(), true);

                // 3️⃣ 현재 클릭된 아이콘을 이전 아이콘으로 설정
                previousItemId = item.getItemId();

                // 아이템 선택 해제 (중요)
                item.setCheckable(false);
                item.setChecked(false);


                if (item.getItemId() == R.id.work_load) {
                    Toast.makeText(MapActivity.this, "Map", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    Intent intent = new Intent(MapActivity.this, MyPageActivity.class);
                    startActivity(intent);
                    return true;
                }else if (item.getItemId() == R.id.chat) {
                    Intent intent = new Intent(MapActivity.this, IdListActivity.class );
                    startActivity(intent);
                }else if (item.getItemId() == R.id.nav_home){
                    Intent intent = new Intent(MapActivity.this,MainActivity.class);
                    startActivity(intent);
                }
                return false;
            }
        });

        // KakaoMap SDK 초기화 (앱 키를 여기서 사용)
        KakaoMapSdk.init(this, getString(R.string.KAKAO_MAP_KEY));  

        // MapView 객체를 찾아서 사용
        mapView = findViewById(R.id.map_view);
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                // 지도 API가 정상적으로 종료될 때 호출
                Log.d("KakaoMap", "onMapDestroy: ");
            }

            @Override
            public void onMapError(Exception error) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출
                Log.e("KakaoMap", "onMapError: ", error);
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                // 정상적으로 인증이 완료되었을 때 호출
                // KakaoMap 객체를 얻어 옵니다.
                kakaoMap = map;
            }
        });
    }

    // 아이콘 업데이트 함수
    private void updateIcon(int itemId, boolean isClicked) {
        if (bottomNavigationView == null) return;

        int iconRes;
        if (itemId == R.id.nav_home) {
            iconRes = isClicked ? R.drawable.homeclicked : R.drawable.homedefault;
        } else if (itemId == R.id.chat) {
            iconRes = isClicked ? R.drawable.chatclicked : R.drawable.chatdefault;
        } else if (itemId == R.id.nav_profile) {
            iconRes = isClicked ? R.drawable.mypageclicked : R.drawable.mypagedefault;
        } else if (itemId == R.id.work_load) {
            iconRes = isClicked ? R.drawable.workloadclicked : R.drawable.workloaddefault;
        } else {
            return;
        }
        bottomNavigationView.getMenu().findItem(itemId).setIcon(iconRes);

        bottomNavigationView.getMenu().findItem(itemId).setChecked(true);
    }
}
