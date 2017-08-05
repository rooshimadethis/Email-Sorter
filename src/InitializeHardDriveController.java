import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class InitializeHardDriveController {

    @FXML private Label warningText;

    private File emailDirectory;


    @FXML
    public void initialize() {

    }


    @FXML
    protected void chooseRootFolder() {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select your root email directory");
        //directoryChooser.setInitialDirectory(new File(System.getProperty("user.home") + "Documents"));
        emailDirectory = directoryChooser.showDialog(Main.getInstance().getStage().getOwner());
    }

    @FXML protected void toInitializePreferences() {
        Main.getInstance().goToInitializePreferences();
        //deleteCheckBox.setSelected(true);
    }

}

