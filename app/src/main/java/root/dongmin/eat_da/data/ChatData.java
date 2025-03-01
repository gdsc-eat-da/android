package root.dongmin.eat_da.data;
import java.util.Objects;

public class ChatData {
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }

    private  String msg;
//-----------------------------------------------------
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    private String nickname;

    //-----------------------------------------------------
    public String getTime() {
        return Time;
    }
    public void setTime(String Time) {
        this.Time = Time;
    }

    private String Time;

    //-----------------------------------------------------
    public String getIsnotread() {
        return Isnotread;
    }
    public void setIsnotread(String Isnotread) {
        this.Isnotread = Isnotread;
    }

    private String Isnotread;


    public String imageUrl;
    public String getUrl() {
        return imageUrl;
    }
    public void setUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }






    private boolean isDateDivider; // 날짜 구분선인지 여부
    public boolean isDateDivider() {
        return isDateDivider;
    }
    public void setDateDivider(boolean dateDivider) {
        isDateDivider = dateDivider;
    }




}
