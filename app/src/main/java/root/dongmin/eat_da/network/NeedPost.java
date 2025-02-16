package root.dongmin.eat_da.network;

import com.google.gson.annotations.SerializedName;

public class NeedPost {

    @SerializedName("postID")
    private String postID; // 게시글 ID

    @SerializedName("contents")
    private String contents; // 내용

    @SerializedName("ingredients")
    private String ingredients; // 재료

    @SerializedName("nickname")
    private String nickname; // 닉네임

    @SerializedName("latitude")
    private String latitude; // 위도 (String으로 받기)

    @SerializedName("longitude")
    private String longitude; // 경도 (String으로 받기)

    public NeedPost() {}

    // Getter & Setter 메소드들
    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}

