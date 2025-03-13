package root.dongmin.eat_da.network;


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

    @SerializedName("selectedJoinedItems")
    private String selectedJoinedItems; // ✅ 알레르기 추가

    @SerializedName("nickname")
    private String nickname; // ✅ 닉네임 추가

    @SerializedName("face") // 추가된 face 컬럼
    private int face; // 추가된 face 필드

    @SerializedName("hashtag") // 해시태그 추가함( 알레르기는 selectedJoinedItems )
    private String hashtag;

    // ✅ 기본 생성자
    public Post() {}

    // ✅ 모든 필드를 포함한 생성자
    public Post(String postID, String contents, String ingredients, String photo, double distance, String nickname, String selectedJoinedItems, int face, String hashtag) {
        this.postID = postID;
        this.contents = contents;
        this.ingredients = ingredients;
        this.photo = photo;
        this.distance = distance;
        this.nickname = nickname;
        this.selectedJoinedItems = selectedJoinedItems;
        this.face = face;
        this.hashtag = hashtag;

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

    public String getselectedJoinedItems(){return selectedJoinedItems;}
    public void setselectedJoinedItems(String selectedJoinedItems){this.selectedJoinedItems = selectedJoinedItems;}

    public String getHashtag(){return hashtag;}
    public void setHashtag(String hashtag){this.hashtag = hashtag;}

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    // 추가된 face getter & setter
    public boolean isFace() { return face == 0; }  // boolean 타입의 getter는 isFace()로 작성
    public void setFace(int face) { this.face = face; }  // face 값 setter
}
