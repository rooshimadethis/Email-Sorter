import java.io.Serializable;

public class User implements Serializable{

    private String emailAddress;
    private String userID;

 public User(String emailAddress, String userID) {
     this.emailAddress = emailAddress;
     this.userID = userID;
 }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getUserID() {
        return userID;
    }

}
