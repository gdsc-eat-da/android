package root.dongmin.eat_da.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("post.php") // 서버의 PHP 파일 경로
    Call<ResponseBody> uploadPost(
            @Part MultipartBody.Part photo,           // 이미지
            @Part("contents") RequestBody contents,   // 텍스트 (글)
            @Part("ingredients") RequestBody ingredients  // 텍스트 (재료)
    );
}
