import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Profile;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.Random;

public class StartupController {
    private Credential currentUserCredentials;
    private ArrayList<User> users;
    private Authorizer mainAuthorizer;
    private int currentUserIndex;
    private Boolean newAccountThreadRunning;

    private DataStore dataStore;

    @FXML private JFXComboBox<String> accountDropdown;
    @FXML private JFXButton loginButton;


    @FXML
    public void initialize() {
        System.setProperty("prism.lcdtext", "true");

        mainAuthorizer = new Authorizer();
        newAccountThreadRunning = false;
        users = new ArrayList<User>();
        dataStore = new DataStore();
        users = DataStore.loadUsers();
        updateDropdownList();
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
