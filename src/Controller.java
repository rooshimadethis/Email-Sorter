import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import com.google.api.services.gmail.model.Profile;
import javax.xml.crypto.Data;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Controller {
    Credential currentUserCredentials;
    ArrayList<User> users;
    Authorizer mainAuthorizer;
    User currentUser;
    Boolean newAccountThreadRunning;
    private final String USER_FILE_DIR = System.getProperty("user.dir") + "\\data\\UserData";
    private DataStore dataStore;

    @FXML private Label warningText;
    @FXML private Text newAccountText;
    @FXML private ComboBox<String> accountDropdown;

    //TODO maybe keep in a few methods





    @FXML
    public void initialize() {
        mainAuthorizer = new Authorizer();
        newAccountThreadRunning = false;
        users = new ArrayList<User>();
        dataStore = new DataStore();
        users = dataStore.loadUsers(USER_FILE_DIR);
        setupDropdownList();

    }

    public void saveUsers() {
        dataStore.saveUsers(USER_FILE_DIR, users);
    }

    private void setupDropdownList() {
        ObservableList<String> accountDropdownAccounts =
                FXCollections.observableArrayList();
        for (User user : users){
            accountDropdownAccounts.add(user.getEmailAddress());
        }
        accountDropdown.setItems(accountDropdownAccounts);
    }

    @FXML protected void handleLogin(ActionEvent event){
        if ((accountDropdown.getSelectionModel().getSelectedItem()) == null){
            System.out.println("nothing selected");
        }
    }
    @FXML protected void makeNewAccount() {
        if (!newAccountThreadRunning) {

            warningText.setVisible(true);


            Thread newAccountThread = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            newAccountThreadRunning = true;

                            try {
                                Thread.sleep(200);
                            } catch (Exception e){e.printStackTrace();}

                            String newUserID = generateNewUserID();
                            currentUserCredentials = mainAuthorizer.authorizeUser(newUserID);

                            if (currentUserCredentials == null) {
                                System.out.println("New user not created.");
                                newAccountThreadRunning = false;
                                return;
                            }
                            String newEmailAddress = getNewUserEmailAddress();


                            User newUser = new User(newUserID, newEmailAddress);
                            users.add(newUser);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    warningText.setVisible(false);
                                }
                            });
                            newAccountThreadRunning = false;

                        }

                    });
            newAccountThread.start();
        }
    }
    private String generateNewUserID() {
        int lastNumber = 0;
        for (User user : users) {
            String num = user.getUserID().replace("user", "");
            int number = Integer.parseInt(num);
            if (number > lastNumber) {
                lastNumber = number;
            }
        }
        return "user" + (lastNumber+1);
    }

    private String getNewUserEmailAddress(){
        String emailAddress = null;
        try {
            Gmail gmail = mainAuthorizer.getGmailService(currentUserCredentials);
            Profile profile = gmail.users().getProfile("me").execute();
            emailAddress = profile.getEmailAddress();

        } catch (Exception e){e.printStackTrace();}

        return emailAddress;
    }
}
