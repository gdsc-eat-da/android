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

    // ✅ 기본 생성자
    public Post() {}

    // ✅ 모든 필드를 포함한 생성자
    public Post(String postID, String contents, String ingredients, String photo) {
        this.postID = postID;
        this.contents = contents;
        this.ingredients = ingredients;
        this.photo = photo;
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
}
