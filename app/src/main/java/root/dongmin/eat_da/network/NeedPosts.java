//package root.dongmin.eat_da.network;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//
//public class NeedPosts implements Parcelable {
//
//    private String postID;
//    private String contents;
//    private String nickname;
//    private String latitude;
//    private String longitude;
//
//    // 생성자
//    public NeedPosts(String postID, String contents, String nickname, String latitude, String longitude) {
//        this.postID = postID;
//        this.contents = contents;
//        this.nickname = nickname;
//        this.latitude = latitude;
//        this.longitude = longitude;
//    }
//
//    // Parcelable 구현
//    protected NeedPosts(Parcel in) {
//        postID = in.readString();
//        contents = in.readString();
//        nickname = in.readString();
//        latitude = in.readString();
//        longitude = in.readString();
//    }
//
//    public static final Creator<NeedPosts> CREATOR = new Creator<NeedPosts>() {
//        @Override
//        public NeedPosts createFromParcel(Parcel in) {
//            return new NeedPosts(in);
//        }
//
//        @Override
//        public NeedPosts[] newArray(int size) {
//            return new NeedPosts[size];
//        }
//    };
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(postID);
//        dest.writeString(contents);
//        dest.writeString(nickname);
//        dest.writeString(latitude);
//        dest.writeString(longitude);
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    // Getter 메서드
//    public String getPostID() { return postID; }
//    public String getContents() { return contents; }
//    public String getNickname() { return nickname; }
//    public String getLatitude() { return latitude; }
//    public String getLongitude() { return longitude; }
//}
