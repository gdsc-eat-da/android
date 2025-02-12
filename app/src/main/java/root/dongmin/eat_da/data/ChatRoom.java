package root.dongmin.eat_da.data;

public class ChatRoom {
    private String profileImageUrl;
    private String postTitle;

    public ChatRoom(String nickname, String profileImageUrl, String postTitle) {
        this.profileImageUrl = profileImageUrl;
        this.postTitle = postTitle;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getPostTitle() {
        return postTitle;
    }
}