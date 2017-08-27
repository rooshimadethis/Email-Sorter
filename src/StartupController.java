import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import com.google.api.services.gmail.model.Profile;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

public class StartupController {
    Credential currentUserCredentials;
    ArrayList<User> users;
    Authorizer mainAuthorizer;
    int currentUserIndex;
    Boolean newAccountThreadRunning;

    private DataStore dataStore;

    @FXML private Label warningText;
    @FXML private Text newAccountText;
    @FXML private ComboBox<String> accountDropdown;
    @FXML private Label titleLabel;

    //TODO maybe keep in a few methods

    @FXML
    public void initialize() {
        mainAuthorizer = new Authorizer();
        newAccountThreadRunning = false;
        users = new ArrayList<User>();
        dataStore = new DataStore();
        users = DataStore.loadUsers();
        updateDropdownList();
        titleLabel.setStyle("-fx-font-smoothing-type: gray");

    }

    public void saveUsers() {
        DataStore.saveUsers(users);
    }

    private void updateDropdownList() {
        ObservableList<String> accountDropdownAccounts =
                FXCollections.observableArrayList();
        for (User user : users){
            accountDropdownAccounts.add(user.getEmailAddress());
        }
        accountDropdown.setItems(accountDropdownAccounts);
    }

    @FXML protected void handleLogin(){
        String selectedItem = accountDropdown.getSelectionModel().getSelectedItem();

        //TODO say nothing was selected in GUI
        if (selectedItem == null) {
            System.out.println("Nothing was selected");
            return;
        }

        for (int i = 0; i < users.size(); i++){
            if (selectedItem.equals(users.get(i).getEmailAddress())){
                currentUserIndex = i;
                if (!users.get(i).hasDoneInitialSetup()){
                    Main.getInstance().setCurrentUser(users.get((i)));
                    goToInitializeHardDrive();

                }
            }
        }
    }

    //TODO change Thread to Task
    @FXML protected void makeNewAccount() {
        //if (!newAccountThreadRunning) {

            warningText.setVisible(true);


            Thread newAccountThread = new Thread(
                    () -> {
                        newAccountThreadRunning = true;

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

                        DataStore.createNewPreferences(newUserID);

                        Platform.runLater(() -> warningText.setVisible(false));
                        newAccountThreadRunning = false;
                        updateDropdownList();
                    });
            newAccountThread.start();
        System.out.println();
        //}
    }

    private String generateNewUserID() {
        /*int lastNumber = 0;
        for (User user : users) {
            String num = user.getUserID().replace("user", "");
            int number = Integer.parseInt(num);
            if (number > lastNumber) {
                lastNumber = number;
            }
        }
        return "user" + (lastNumber+1);
        */
        Random random = new Random();
        return ("user" + random.nextInt(100000));
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

    private void goToInitializeHardDrive() {

        Main.getInstance().setAuthorizer(mainAuthorizer);
        Main.getInstance().goToInitializeHardDrive();
    }
}
