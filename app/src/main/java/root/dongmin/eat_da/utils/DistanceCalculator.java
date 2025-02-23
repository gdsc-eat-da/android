package root.dongmin.eat_da.utils;

import android.util.Log;

import com.kakao.vectormap.LatLng;

public class DistanceCalculator {

    // Haversine 공식을 사용한 거리 계산
    public static float calculateDistance(LatLng userLocation, LatLng postLocation) {
        Log.d("DistanceCalculator", "Invalid location data: userLocation=" + userLocation + ", postLocation=" + postLocation);
        if (userLocation == null || postLocation == null) {
            return -1;  // 위치 정보가 없으면 -1 반환
        }

        double earthRadius = 6371000;  // 지구 반경 (단위: 미터)

        double lat1 = Math.toRadians(userLocation.latitude);
        double lon1 = Math.toRadians(userLocation.longitude);
        double lat2 = Math.toRadians(postLocation.latitude);
        double lon2 = Math.toRadians(postLocation.longitude);

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 거리 계산
        return (float) (earthRadius * c);
    }
}

