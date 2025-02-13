package root.dongmin.eat_da.network;

import java.util.List;

// 서버 응답을 처리할 모델 클래스
public class ApiResponse {
    private boolean success;
    private String message;
    private List<Post> posts;

    // Getter와 Setter 추가
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

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}
