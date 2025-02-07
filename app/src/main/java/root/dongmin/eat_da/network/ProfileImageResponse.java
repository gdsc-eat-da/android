package root.dongmin.eat_da.network;

public class ProfileImageResponse {
    private boolean success; // 성공 여부
    private String message; // 메시지
    private String image_url; // 이미지 URL
    private int image_id; // 이미지 ID

    // getter와 setter 추가
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }

    public int getImageId() {
        return image_id;
    }

    public void setImageId(int image_id) {
        this.image_id = image_id;
    }
}
