package root.dongmin.eat_da;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.RoadViewRequest;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelManager;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.label.TrackingManager;
import com.kakao.vectormap.shape.MapPoints;

import java.util.ArrayList;
import java.util.List;

import root.dongmin.eat_da.adapter.MapDistanceAdapter;
import root.dongmin.eat_da.adapter.NeedPostAdapter;
import root.dongmin.eat_da.network.NeedPost;
import root.dongmin.eat_da.utils.DistanceCalculator;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private final String[] locationPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private MapView mapView;
    private KakaoMap kakaoMap;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private LatLng userLocation = null;
    private Label label;
    private boolean requestingLocationUpdates = false;
    private List<NeedPost> needPosts;
    private RecyclerView mapRecyclerView;
    private MapDistanceAdapter mapDistanceAdapter;


     // 음식 필요 게시물 리스트

    private final KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull KakaoMap map) {
            Log.d("MAP_DEBUG", "✅ onMapReady() 실행됨!"); // 확인용 로그
            progressBar.setVisibility(View.GONE);
            kakaoMap = map;

            if (kakaoMap == null) {
                Log.e("MAP_ERROR", "❌ kakaoMap이 null입니다. 초기화가 실패한 것 같습니다.");
                return;
            }

            LabelStyles styles = kakaoMap.getLabelManager()
                    .addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.mymarker)));

            LabelOptions options = LabelOptions.from(LatLng.from(userLocation))
                    .setStyles(styles);

            LabelLayer layer = kakaoMap.getLabelManager().getLayer();

            label = layer.addLabel(options);

            if (label == null) {
                Log.e("MAP_ERROR", "❌ label 생성 실패!");
            } else {
                Log.d("MAP_SUCCESS", "✅ label 생성 완료!");
            }



            // 유저 위치 추적
            TrackingManager trackingManager = kakaoMap.getTrackingManager();
            trackingManager.startTracking(label);

            // 지도 이동 할 수 있도록
            trackingManager.stopTracking();

            startLocationUpdates();
            needLabel();  // 음식 필요 게시물에 대한 레이블 표시
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return userLocation != null ? userLocation : LatLng.from(37.5665, 126.9780); // 기본 위치: 서울 시청
        }

        @NonNull
        @Override
        public int getZoomLevel() {
            return 17;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        Log.d("MAP_DEBUG", "onCreate 실행됨");

        mapRecyclerView = findViewById(R.id.needMapPosts);
        mapRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        // Intent에서 데이터 받기
        Intent intent = getIntent();
        needPosts = intent.getParcelableArrayListExtra("needPostList");  // 전달받은 음식 필요 게시물 리스트 할당
        if (needPosts != null && !needPosts.isEmpty()) {
            Log.d("MAP_DEBUG", "Received needPostList with size: " + needPosts.size());
            for (NeedPost post : needPosts) {
                Log.d("MAP_DEBUG", "Post ID: " + post.getPostID());
                Log.d("MAP_DEBUG", "Contents: " + post.getContents());
                Log.d("MAP_DEBUG", "Nickname: " + post.getNickname());
                Log.d("MAP_DEBUG", "Latitude: " + post.getLatitude());
                Log.d("MAP_DEBUG", "Longitude: " + post.getLongitude());
            }
        } else {
            Log.d("MAP_DEBUG", "NeedPost List is null or empty");
        }

        mapView = findViewById(R.id.map_view);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        mapDistanceAdapter = new MapDistanceAdapter(this, needPosts);
        mapRecyclerView.setAdapter(mapDistanceAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    label.moveTo(LatLng.from(location.getLatitude(), location.getLongitude()));
                }
            }
        };

        setupBottomNavigationView();

        if (checkLocationPermissions()) {
            getUserLocation(); // 위치 권한 확인 후 초기화
        } else {
            requestLocationPermissions();
        }
    }

    private void needLabel() {
        Log.d("MAP_DEBUG", "needLabel() 함수 호출됨");
        if (kakaoMap == null) {
            Log.e("MAP_ERROR", "kakaoMap이 아직 초기화되지 않았습니다.");
            return;
        }


        if (needPosts == null || needPosts.isEmpty()) {
            Log.d("MAP_DEBUG", "needPosts 리스트가 비어 있습니다.");
            return;
        }

        // LabelManager 확인
        LabelManager labelManager = kakaoMap.getLabelManager();
        if (labelManager == null) {
            Log.e("MAP_ERROR", "LabelManager를 가져올 수 없습니다.");
            return;
        }

        // 라벨 스타일 생성
        LabelStyles styles = labelManager.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.eat_da_logo)));
        if (styles == null) {
            Log.e("MAP_ERROR", "라벨 스타일을 생성할 수 없습니다.");
            return;
        }

        // LabelLayer 가져오기
        LabelLayer layer = labelManager.getLayer();
        if (layer == null) {
            Log.e("MAP_ERROR", "LabelLayer를 가져올 수 없습니다.");
            return;
        }


        for (NeedPost post : needPosts) {
            try {
                Log.d("MAP_DEBUG", "위도: " + post.getLatitude() + ", 경도: " + post.getLongitude());

                double lat = Double.parseDouble(post.getLatitude());
                double lng = Double.parseDouble(post.getLongitude());

                Log.d("MAP_DEBUG", "변환된 위도: " + lat + ", 변환된 경도: " + lng);

                // 현재 사용자 위치와 게시물 위치 간의 거리 계산
                float distance = DistanceCalculator.calculateDistance(userLocation, LatLng.from(lat, lng));
                Log.d("MAP_DEBUG", "현재 위치와 게시물 간의 거리: " + distance + "미터");

                // LabelOptions 생성하기
                LabelOptions options = LabelOptions.from(LatLng.from(lat, lng))
                        .setStyles(styles);

                layer.addLabel(options);

                Log.d("MAP_DEBUG", "라벨 추가됨: " + post.getNickname() + " (" + lat + ", " + lng + ")");
            } catch (NumberFormatException e) {
                Log.e("MAP_ERROR", "위도/경도 변환 오류: " + post.getLatitude() + ", " + post.getLongitude());
            }
        }


    }

    private void setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            private int previousItemId = R.id.work_load;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (previousItemId == item.getItemId()) return false;

                updateIcon(previousItemId, false);
                updateIcon(item.getItemId(), true);
                previousItemId = item.getItemId();

                if (item.getItemId() == R.id.work_load) {
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    startActivity(new Intent(MapActivity.this, MyPageActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.chat) {
                    startActivity(new Intent(MapActivity.this, IdListActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(MapActivity.this, MainActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void updateIcon(int itemId, boolean isClicked) {
        if (bottomNavigationView == null) return;
        int iconRes = isClicked ? R.drawable.workloadclicked : R.drawable.workloaddefault;
        bottomNavigationView.getMenu().findItem(itemId).setIcon(iconRes);
    }

    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        // 위치 권한이 승인되었을 때, 현재 위치 가져오기
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        userLocation = LatLng.from(location.getLatitude(), location.getLongitude());
                        Log.d("MAP_DEBUG", "현재 위치: " + location.getLatitude() + ", " + location.getLongitude());

                        // 위치가 정상적으로 받아왔으므로 userLocation이 null이 아님
                        if (userLocation != null) {
                            Log.d("MAP_DEBUG", "userLocation 업데이트됨: " + userLocation.toString());
                        } else {
                            Log.e("MAP_ERROR", "userLocation이 여전히 null입니다.");
                        }

                        // 위치 정보가 업데이트된 후 MapDistanceAdapter에 전달
                        if (mapDistanceAdapter != null) {
                            mapDistanceAdapter.updateUserLocation(userLocation);
                        }

                        // 카카오맵 SDK 초기화
                        try {
                            // KakaoMap SDK 초기화
                            KakaoMapSdk.init(MapActivity.this, getString(R.string.KAKAO_MAP_KEY));

                            // 맵 뷰 초기화 및 시작
                            mapView = findViewById(R.id.map_view);
                            mapView.start(new com.kakao.vectormap.MapLifeCycleCallback() {
                                @Override
                                public void onMapDestroy() {
                                    Log.d("KakaoMap", "onMapDestroy: 지도 종료됨");
                                }

                                @Override
                                public void onMapError(Exception error) {
                                    Log.e("KakaoMap", "onMapError: ", error);
                                    Toast.makeText(MapActivity.this, "지도 로딩 중 오류 발생", Toast.LENGTH_SHORT).show();
                                }
                            }, readyCallback); // KakaoMapReadyCallback 추가

                        } catch (Exception e) {
                            Log.e("MapActivity", "KakaoMap SDK 초기화 실패", e);
                            Toast.makeText(MapActivity.this, "지도 초기화 중 오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        requestingLocationUpdates = true;
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (requestingLocationUpdates) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                showPermissionExplanation();
            }
        }
    }

    private void showPermissionExplanation() {
        new AlertDialog.Builder(this)
                .setTitle("위치 권한 필요")
                .setMessage("위치 서비스를 사용하기 위해 권한을 허용해주세요.")
                .setPositiveButton("설정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

}
