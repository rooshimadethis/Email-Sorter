import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Profile;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class PrimaryScreenController {

    @FXML
    public void initialize() {

    }

    @FXML protected void goToAddNewFolder() {
        Main.getInstance().goToAddNewFolder();
    }

}
