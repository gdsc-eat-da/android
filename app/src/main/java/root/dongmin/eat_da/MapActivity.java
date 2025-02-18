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
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.TrackingManager;

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
    private Label userMarker;
    private boolean requestingLocationUpdates = false;

    private final KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull KakaoMap map) {
            progressBar.setVisibility(View.GONE);
            kakaoMap = map;

            LabelLayer labelLayer = kakaoMap.getLabelManager().getLayer();
            userMarker = labelLayer.addLabel(LabelOptions.from("userMarker", userLocation)
                    .setStyles(LabelStyle.from(R.drawable.red_dot_marker).setAnchorPoint(0.5f, 0.5f))
                    .setRank(1));

            TrackingManager trackingManager = kakaoMap.getTrackingManager();
            trackingManager.startTracking(userMarker);

            startLocationUpdates();
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



        mapView = findViewById(R.id.map_view);
        //progressBar = findViewById(R.id.progressBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    userMarker.moveTo(LatLng.from(location.getLatitude(), location.getLongitude()));
                }
            }
        };

        setupBottomNavigationView();

        if (checkLocationPermissions()) {
            getUserLocation();
        } else {
            requestLocationPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
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
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("위치 권한이 필요합니다. 설정에서 권한을 허용해주세요.")
                .setPositiveButton("설정으로 이동", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        startActivity(intent);
                    } finally {
                        finish();
                    }
                })
                .setNegativeButton("종료", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
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
}
