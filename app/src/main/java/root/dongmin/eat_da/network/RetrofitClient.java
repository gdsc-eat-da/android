package root.dongmin.eat_da.network;

import android.content.Context;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import root.dongmin.eat_da.R;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    // Retrofit 인스턴스를 한 번만 생성하여 반환
    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            String baseUrl = context.getString(R.string.api_url);  // strings.xml에서 URL 가져오기
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1))  // 도메인만 가져와서 baseUrl 설정
                    .addConverterFactory(GsonConverterFactory.create())  // JSON 파싱을 위한 Converter 설정
                    .build();
        }
        return retrofit;
    }

    // ApiService를 반환하는 메서드
    public static ApiService getApiService(Context context) {
        return getRetrofitInstance(context).create(ApiService.class);
    }
}
