package root.dongmin.eat_da.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NeedPostResponseWrapper {
    @SerializedName("need") // JSON에서 "need" 키를 매핑
    private List<NeedPost> needPosts; // ✅ needPosts로 변경

    public List<NeedPost> getNeedPosts() {
        return needPosts;
    }

    public void setNeedPosts(List<NeedPost> needPosts) {
        this.needPosts = needPosts;
    }
}


