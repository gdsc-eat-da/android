package root.dongmin.eat_da.network;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NeedPost implements Parcelable {

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

    // 기본 생성자
    public NeedPost() {}

    // Parcelable 생성자
    protected NeedPost(Parcel in) {
        postID = in.readString();
        contents = in.readString();
        ingredients = in.readString();
        nickname = in.readString();
        latitude = in.readString();
        longitude = in.readString();
    }

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

    // Parcelable 인터페이스 구현
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postID);
        dest.writeString(contents);
        dest.writeString(ingredients);
        dest.writeString(nickname);
        dest.writeString(latitude);
        dest.writeString(longitude);
    }

    @Override
    public String toString() {
        return "NeedPost{" +
                "postID='" + postID + '\'' +
                ", contents='" + contents + '\'' +
                ", ingredients='" + ingredients + '\'' +
                ", nickname='" + nickname + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }


    // Parcelable.Creator 구현
    public static final Creator<NeedPost> CREATOR = new Creator<NeedPost>() {
        @Override
        public NeedPost createFromParcel(Parcel in) {
            return new NeedPost(in);
        }

        @Override
        public NeedPost[] newArray(int size) {
            return new NeedPost[size];
        }
    };
}
