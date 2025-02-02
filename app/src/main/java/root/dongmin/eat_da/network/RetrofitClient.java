package root.dongmin.eat_da.network;

import android.content.Context;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import root.dongmin.eat_da.R;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            String baseUrl = context.getString(R.string.api_url).trim();

            // baseUrl이 슬래시('/')로 끝나지 않으면 추가
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)  // 정리된 baseUrl 설정
                    .addConverterFactory(GsonConverterFactory.create())  // JSON 변환기 추가
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        return getRetrofitInstance(context).create(ApiService.class);
    }
}

