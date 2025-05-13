//package root.dongmin.eat_da;
//
//import java.util.List;
//
//import root.dongmin.eat_da.network.Post;
//
//public class DataHolder {
//    private static final DataHolder instance = new DataHolder();
//
//    private List<Post> needPosts;
//    private List<UserLocation> postLocations;
//    private List<String> chatList;
//    private String nickname;
//
//    private DataHolder() {}
//
//    public static DataHolder getInstance() {
//        return instance;
//    }
//
//    public List<Post> getNeedPosts() {
//        return needPosts;
//    }
//
//    public void setNeedPosts(List<Post> needPosts) {
//        this.needPosts = needPosts;
//    }
//
//    public List<UserLocation> getPostLocations() {
//        return postLocations;
//    }
//
//    public void setPostLocations(List<UserLocation> postLocations) {
//        this.postLocations = postLocations;
//    }
//
//    public List<String> getChatList() {
//        return chatList;
//    }
//
//    public void setChatList(List<String> chatList) {
//        this.chatList = chatList;
//    }
//
//    public String getNickname() {
//        return nickname;
//    }
//
//    public void setNickname(String nickname) {
//        this.nickname = nickname;
//    }
//}