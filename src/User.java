import java.io.Serializable;

public class User implements Serializable{

    private String emailAddress;
    private String userID;

 public User(String userID, String emailAddress) {
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
