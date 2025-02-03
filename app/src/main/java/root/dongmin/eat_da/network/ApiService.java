package root.dongmin.eat_da.network;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    //  게시글 업로드 (이미지 + 글 + 재료)
    @Multipart
    @POST("post.php") // 서버의 PHP 파일 경로
    Call<ResponseBody> uploadPost(
            @Part MultipartBody.Part photo,           // 이미지
            @Part("contents") RequestBody contents,   // 텍스트 (글)
            @Part("ingredients") RequestBody ingredients  // 텍스트 (재료)
    );

    //  게시글 목록 가져오기 (GET 요청)
    @GET("get_post.php") // 서버의 게시글 목록을 가져오는 PHP 파일 경로
    Call<List<Post>> getPosts();

    //@GET("get_post.php")
    //Call<String> getPostsAsString();
}

