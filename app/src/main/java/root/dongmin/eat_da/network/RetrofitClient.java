package root.dongmin.eat_da.network;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import root.dongmin.eat_da.R;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            String baseUrl = context.getString(R.string.post_url).trim(); // 경로를 잘 파악하자..

            // baseUrl이 슬래시('/')로 끝나지 않으면 추가
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }

            // Gson 설정 (setLenient 적용)
            Gson gson = new GsonBuilder()
                    .setLenient()  // JSON 파싱 유연하게 처리
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))  // Gson 변환기 추가
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        return getRetrofitInstance(context).create(ApiService.class);
    }
}
