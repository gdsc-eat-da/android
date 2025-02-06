package root.dongmin.eat_da.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Post {
    @SerializedName("postID")
    private String postID;

    @SerializedName("contents")
    private String contents;

    @SerializedName("ingredients")
    private String ingredients;

    @SerializedName("photo")
    private String photo;

    @SerializedName("distance") // 실제 DB에 distance 컬럼은 없지만 php 의 sql 쿼리의 계산 값으로 존재함
    private double distance; // *즉 JSON 은 distance 컬럼 까지 반환 하는데 distance 가 없었기 때문에 오류가 났음*


    @SerializedName("nickname")
    private String nickname; // ✅ 닉네임 추가

    // ✅ 기본 생성자
    public Post() {}

    // ✅ 모든 필드를 포함한 생성자
    public Post(String postID, String contents, String ingredients, String photo, double distance, String nickname) {
        this.postID = postID;
        this.contents = contents;
        this.ingredients = ingredients;
        this.photo = photo;
        this.distance = distance;
        this.nickname = nickname;
    }

    // ✅ Getter & Setter (JSON 매핑에 필요)
    public String getPostID() { return postID; }
    public void setPostID(String postID) { this.postID = postID; }

    public String getContents() { return contents; }
    public void setContents(String contents) { this.contents = contents; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getNickname(){return nickname;}

    public void setNickname(String nickname){this.nickname = nickname;}

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
}
