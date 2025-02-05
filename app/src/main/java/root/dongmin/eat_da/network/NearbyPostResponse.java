package root.dongmin.eat_da.network;

import java.util.List;

// url 에서 JSON 이 반환 하는 값을 확인 하고 구조를 만들어야 함
public class NearbyPostResponse {
    private boolean success;
    private List<Post> posts;

    public boolean isSuccess() {
        return success;
    }

    public List<Post> getPosts() {
        return posts;
    }
}
