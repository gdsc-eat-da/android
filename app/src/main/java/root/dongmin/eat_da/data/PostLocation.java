package root.dongmin.eat_da.data;

import android.os.Parcel;
import android.os.Parcelable;

public class PostLocation implements Parcelable {
    private int postID;
    private double latitude;
    private double longitude;

    // 기본 생성자
    public PostLocation() {}

    // 생성자
    public PostLocation(int postID, double latitude, double longitude) {
        this.postID = postID;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter 메서드
    public int getPostID() {
        return postID;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    // Parcelable 구현
    protected PostLocation(Parcel in) {
        postID = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<PostLocation> CREATOR = new Creator<PostLocation>() {
        @Override
        public PostLocation createFromParcel(Parcel in) {
            return new PostLocation(in);
        }

        @Override
        public PostLocation[] newArray(int size) {
            return new PostLocation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(postID);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
