import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import com.google.api.services.gmail.model.Profile;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.event.ChangeListener;
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

    @FXML private AnchorPane anchorPane;
    @FXML private Label warningText;
    @FXML private Text newAccountText;
    @FXML private ComboBox<String> accountDropdown;
    @FXML private Label titleLabel;
    @FXML private Button loginButton;


    @FXML
    public void initialize() {
        mainAuthorizer = new Authorizer();
        newAccountThreadRunning = false;
        users = new ArrayList<User>();
        dataStore = new DataStore();
        users = DataStore.loadUsers();
        updateDropdownList();
        //anchorPane.setStyle("-fx-background-color: #" + Design.getPrimaryColor());
        Font font = Font.loadFont(getClass().getResourceAsStream("/res/fonts/Roboto/Roboto-Light.ttf"), 66);
        loginButton.setDisable(true);
        accountDropdown.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (accountDropdown.getSelectionModel().getSelectedItem() != null){
                loginButton.setDisable(false);
            }
        });
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
                Main.getInstance().setCurrentUser(users.get((i)));
                Main.getInstance().setAuthorizer(mainAuthorizer);
                if (!users.get(i).hasDoneInitialSetup()){
                    goToInitializeHardDrive();
                } else {
                    goToPrimaryScreen();
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

                        DataStore.createNewPreferences(newUser.getShortAddress());

                        Platform.runLater(() -> warningText.setVisible(false));
                        newAccountThreadRunning = false;
                        updateDropdownList();
                    });
            newAccountThread.start();
        System.out.println();
        //}
    }

    private String generateNewUserID() {
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
        Main.getInstance().goToInitializeHardDrive();
    }

    private void goToPrimaryScreen() {
        Main.getInstance().goToPrimaryScreen();
    }
}
