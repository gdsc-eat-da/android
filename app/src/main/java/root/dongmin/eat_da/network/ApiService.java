package root.dongmin.eat_da.network;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    // 게시글 업로드 (이미지 + 글 + 재료)
    @Multipart
    @POST("post.php")
    Call<ResponseBody> uploadPost(
            @Part MultipartBody.Part photo, //사진
            @Part("contents") RequestBody contents, // 내용
            @Part("ingredients") RequestBody ingredients // 재료(영양 성분)
    );

    // 게시글 목록 가져오기
    @GET("get_post.php")
    Call<List<Post>> getPosts();

    // 위치 저장

    // 오늘의 교훈 phpmyadmin 에서 영문자 대소문자 구별합니다. 이것 때문에 2시간 버림
    @FormUrlEncoded
    @POST("post_location.php")
    Call<ResponseBody> uploadLocation(
            @Field("postID") int postID, // 외래키 참조
            @Field("latitude") double latitude, // 위도
            @Field("longitude") double longitude // 경도
    );
}
