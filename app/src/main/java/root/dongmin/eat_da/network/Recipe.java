package root.dongmin.eat_da.network;

import com.google.gson.annotations.SerializedName;

public class Recipe {
    @SerializedName("recipeID")
    private String recipeID;

    @SerializedName("contents")
    private String contents;

    @SerializedName("ingredients")
    private String ingredients;

    @SerializedName("photo")
    private String photo;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("isrecipe")
    private int isrecipe;

    @SerializedName("hashtag")
    private String hashtag;

    @SerializedName("suggestion")
    private int suggestion;

    // 기본 생성자
    public Recipe() {}

    // 매개변수 있는 생성자
    public Recipe(String recipeID, String contents, String ingredients, String photo, String nickname, int suggestion, String hashtag, int isrecipe) {
        this.recipeID = recipeID;
        this.contents = contents;
        this.ingredients = ingredients;
        this.photo = photo;
        this.nickname = nickname;
        this.suggestion = suggestion;
        this.hashtag = hashtag;
        this.isrecipe = isrecipe;
    }

    // Getter 및 Setter 메서드
    public String getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
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

    public String getPhoto() {
        return photo;
    }

    public String getHashtag(){return hashtag;}
    public int getIsrecipe(){return  isrecipe;}

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(int suggestion) {
        this.suggestion = suggestion;
    }
}
