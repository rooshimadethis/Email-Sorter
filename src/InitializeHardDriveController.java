import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InitializeHardDriveController {

    @FXML private JFXTextField directoryField;
    @FXML private JFXButton nextButton;

    @FXML
    public void initialize() {
        nextButton.setDisable(true);
        directoryField.setPrefWidth(200);
        //directoryField.prefColumnCountProperty().bind(directoryField.textProperty().length());

        directoryField.textProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue.length() > 0){
                nextButton.setDisable(false);
                directoryField.setPrefWidth(TextUtils.computeTextWidth(directoryField.getFont(),
                        directoryField.getText(), 0.0D) + 10);
            } else {
                nextButton.setDisable(true);
                directoryField.setPrefWidth(200);

            }

        });
    }

    @FXML
    protected void chooseRootFolder() {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select your root email directory (create a folder if you want)");
        //directoryChooser.setInitialDirectory(new File(System.getProperty("user.home") + "Documents"));
        File emailDirectory = directoryChooser.showDialog(Main.getInstance().getStage().getOwner());
        directoryField.setText(emailDirectory.getAbsolutePath());

        Path path = Paths.get(emailDirectory.getAbsolutePath());
        Path drivePath = path.getRoot();
        String drive = drivePath.toString().substring(0,1);


        String systemDisplayName = FileSystemView.getFileSystemView().getSystemDisplayName(drivePath.toFile());

        String[] separated = systemDisplayName.split(" ");
        String driveName = "";
        for (int i = 0; i < separated.length; i++) {
            if (separated[i].equals("(" + drive + ":)")){
            } else {
                if (i == 0){
                    driveName += separated[i];
                } else {
                    driveName += (" " + separated[i]);
                }
            }
        }
        Main.getInstance().setHardDriveName(driveName);

        String folderPath = emailDirectory.getAbsolutePath().substring(2);
        Main.getInstance().setRootFolder(folderPath);

    }

    @FXML protected void toInitializePreferences() {
        Main.getInstance().goToInitializePreferences();
    }

}

