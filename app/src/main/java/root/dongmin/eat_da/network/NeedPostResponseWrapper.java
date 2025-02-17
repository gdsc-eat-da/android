package root.dongmin.eat_da.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NeedPostResponseWrapper {
    @SerializedName("need")
    private List<NeedPost> need;  // ✅ 기존 needPosts → need로 변경

    public List<NeedPost> getNeedPosts() {  // ✅ 메서드에서 need를 반환하도록 수정
        return need;
    }

    public void setNeedPosts(List<NeedPost> need) {  // ✅ 동일하게 수정
        this.need = need;
    }
}



