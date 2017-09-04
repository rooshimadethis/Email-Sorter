import java.io.Serializable;

public class User implements Serializable{

    private String emailAddress;
    private String userID;
    private String userPath;
    private boolean doneInitialSetup;

 public User(String userID, String emailAddress) {
     this.emailAddress = emailAddress;
     this.userID = userID;
     //TODO doneInitialSetup = false;
     userPath = emailAddress;
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

    public String getUserPath() {
        return userPath;
    }

    public void finishedInitialSetup() {
     doneInitialSetup = true;
    }
}
