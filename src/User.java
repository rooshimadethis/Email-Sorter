import java.io.Serializable;

public class User implements Serializable{

    private String emailAddress;
    private String userID;
    private boolean doneInitialSetup;

 public User(String userID, String emailAddress) {
     this.emailAddress = emailAddress;
     this.userID = userID;
     doneInitialSetup = false;
 }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getUserID() {
        return userID;
    }

    public boolean hasDoneInitialSetup() {
        return doneInitialSetup;
    }

    public void finishedInitialSetup() {
     doneInitialSetup = true;
    }
}
