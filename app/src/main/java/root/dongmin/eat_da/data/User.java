package root.dongmin.eat_da.data;

import java.util.Objects;

public class User {
    private String name;
    private String email;
    private String uId;
    private String alergic;
    private String nickname;

    // 기본 생성자
    public User() {
        this.name = "";
        this.email = "";
        this.uId = "";
        this.alergic = "";
    }

    // 매개변수가 있는 생성자
    public User(String name, String email, String uId) {
        this.name = name;
        this.email = email;
        this.uId = uId;

    }

    // Getter 및 Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    // toString(), equals(), hashCode() 메서드 추가 가능
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", uId='" + uId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return name.equals(user.name) && email.equals(user.email) && uId.equals(user.uId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, uId);
    }
}

