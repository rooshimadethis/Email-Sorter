package java;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class Controller {
    @FXML private TextField userNameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox accountDropdown;
    ObservableList<String> accountDropdownAccounts =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

    }

    @FXML protected void handleLogin(ActionEvent event){
        if ((accountDropdown.getSelectionModel().getSelectedItem()) == null){
            System.out.println("nothing selected");
        }
    }
    @FXML protected void makeNewAccount(ActionEvent event) {

    }
}
