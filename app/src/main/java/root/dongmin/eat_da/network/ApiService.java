package root.dongmin.eat_da.network;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import root.dongmin.eat_da.data.PostLocationResponseWrapper;

// 컨트롤러의 역할
public interface ApiService {
    // 게시글 업로드 (이미지 + 글 + 재료)
    @Multipart
    @POST("post.php")
    Call<ResponseBody> uploadPost(
            @Part MultipartBody.Part photo, //사진
            @Part("contents") RequestBody contents, // 내용
            @Part("ingredients") RequestBody ingredients, // 재료(영양 성분)
            @Part("nickname") RequestBody nickname, //  닉네임 추가
            @Part("selectedJoinedItems")RequestBody selectedJoinedItems,
            @Part("face") RequestBody face,
            @Part("hashtag") RequestBody hashtag
            );

    // 필요 게시글 업로드 (글 내용)
    @Multipart
    @POST("needpost.php")
    Call<ResponseBody> needuploadPost(
            @Part("contents") RequestBody contents, // 내용
            @Part("ingredients") RequestBody ingredients, // 재료(영양 성분)
            @Part("nickname") RequestBody nickname,  //  닉네임 추가
            @Part("latitude") RequestBody latitude,  // 위도 추가
            @Part("longitude") RequestBody longitude, // 경도 추가
            @Part("face") RequestBody face
    );

    // 레시피 업로드 (이미지 + 글 + 재료)
    @Multipart
    @POST("recipe.php")
    Call<ResponseBody> uploadRecipe(
            @Part MultipartBody.Part photo, //사진
            @Part("contents") RequestBody contents, // 내용
            @Part("ingredients") RequestBody ingredients, // 재료(영양 성분)
            @Part("nickname") RequestBody nickname //  닉네임 추가
            // + 추천수 suggestion 컬럼 존재함
    );



    // 게시글 목록 가져오기
    @GET("get_post.php")
    Call<List<Post>> getPosts();


    // 필요한 게시글 목록 가져오기

    @GET("get_needpost.php")
    Call<NeedPostResponseWrapper> getNeedPosts();

    // 레시피 목록 가져오기
    @GET("get_recipe.php")
    Call<List<Recipe>> getRecipes();


    // 위치 저장

    // 오늘의 교훈 phpmyadmin 에서 영문자 대소문자 구별합니다. 이것 때문에 2시간 버림
    @FormUrlEncoded
    @POST("post_location.php")
    Call<ResponseBody> uploadLocation(
            @Field("postID") int postID, // 외래키 참조
            @Field("latitude") double latitude, // 위도
            @Field("longitude") double longitude // 경도
    );


    @GET("getPostLocations.php") // 백엔드에서 실제로 구현된 PHP 파일명으로 변경 필요
    Call<PostLocationResponseWrapper> getPostLocations();

    @GET("getNearbyPosts.php")
    Call<NearbyPostResponse> getNearbyPosts(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("radius") double radius
    );
    // 프로필 이미지 서버로 보내기
    @Multipart
    @POST("post_profile.php")
    Call<ResponseBody> uploadProfile(
            @Part MultipartBody.Part image_url // 프로필 이미지 주소
    );

    @GET("get_profile.php")
    Call<ProfileImageResponse> getProfile(
            @Query("id") int id // 기본키(id)를 쿼리로 받아서 이미지 URL 반환
    );

    @GET("get_my_posts.php")
    Call<ResponseBody> getMyPosts(@Query("nickname") String nickname);

    // ApiService 인터페이스에 DELETE 메서드 추가
    @DELETE("delete_post.php")
    Call<ResponseBody> deletePost(@Query("postID") String postID);

    @GET("get_my_need_posts.php")
    Call<ResponseBody> getMyNeedPosts(@Query("nickname") String nickname);

    @FormUrlEncoded
    @POST("delete_needpost.php")
    Call<ResponseBody> deleteNeedPost(@Field("postID") String postID);


}
