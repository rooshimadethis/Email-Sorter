import java.io.Serializable;

public class User implements Serializable{

    private String emailAddress;
    private String userID;
    private String userPath;
    private String shortAddress;
    private boolean doneInitialSetup;

 public User(String userID, String emailAddress) {
     this.emailAddress = emailAddress;
     shortAddress = emailAddress.substring(0, emailAddress.indexOf("@"));
     this.userID = userID;
     //TODO doneInitialSetup = false;
     userPath = "\\" + emailAddress;
 }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getShortAddress() {
        return shortAddress;
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
