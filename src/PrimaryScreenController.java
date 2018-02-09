import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.jfoenix.controls.JFXBadge;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXScrollPane;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;

import javax.swing.filechooser.FileSystemView;
import javax.xml.crypto.Data;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;

public class PrimaryScreenController {
    private ArrayList<Folder> folders;
    private ArrayList<Folder> disabledFolders;
    private ArrayList<Type> types;
    private String currentDrivePath;
    private String currentFullPath;
    private String currentUserPath;
    private boolean separateInOut;
    private User currentUser;

    @FXML private ScrollPane folderScrollPane;
    @FXML private JFXProgressBar progressSpinner;
    @FXML private JFXButton processEmailsButton;

    @FXML
    public void initialize() {
        currentUser = Main.getInstance().getCurrentUser();
        progressSpinner.setVisible(false);
        types = DataStore.loadTypes(currentUser);
        folders = DataStore.loadFolders(currentUser);
        disabledFolders = DataStore.loadDisabledFolders(currentUser);

        getHardDriveData();

        listFoldersOnScrollPane();

        loadPreferences();
    }

    @FXML protected void processEmails() {
        progressSpinner.setVisible(true);

            Thread emails = new Thread(() -> {
                GmailHandler gmailHandler = new GmailHandler();
                gmailHandler.processEmailsForCurrentFolders();
                Platform.runLater(() -> progressSpinner.setVisible(false));
            });
            emails.start();

    }

    private void getHardDriveData() {
        User currentUser = Main.getInstance().getCurrentUser();
        String loadedHardDriveName = Main.getInstance().getHardDriveName();
        String loadedFolderPath = Main.getInstance().getRootFolder();

        File[] drives = File.listRoots();
        for (File drive : drives){
            String letter = drive.getAbsolutePath().substring(0,1);
            String driveName = FileSystemView.getFileSystemView().getSystemDisplayName(drive);
            driveName = driveName.replace(" (" + letter + ":)", "");

            if (driveName.equals(loadedHardDriveName)){
                currentDrivePath = drive.getAbsolutePath();
            }
        }

        File expectedRoot = new File(currentDrivePath + loadedFolderPath);
        if (!expectedRoot.exists()){
            File newDirectory;
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Previous directory not found, find previous or create a new one");
            newDirectory = directoryChooser.showDialog(Main.getInstance().getStage().getOwner());

            Path path = Paths.get(newDirectory.getAbsolutePath());
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

            String folderPath = newDirectory.getAbsolutePath().substring(2);
            Main.getInstance().setRootFolder(folderPath);

            currentDrivePath =  newDirectory.getAbsolutePath().substring(0,2);
            currentFullPath = newDirectory.getAbsolutePath();
            currentUserPath = currentFullPath + "/" + currentUser.getUserPath();
            File userPath = new File(currentUserPath);
            userPath.mkdirs();
        } else {
            currentDrivePath = expectedRoot.getAbsolutePath().substring(0,2);
            currentFullPath = expectedRoot.getAbsolutePath();
            currentUserPath = currentFullPath + "/" + currentUser.getUserPath();
            File userPath = new File(currentUserPath);
            userPath.mkdirs();
        }
    }

    public void moveFolderToDisabled(String folderName) {
        for(Folder folder : folders){
            if (folder.getName().equals(folderName)){
                disabledFolders.add(folder);
                folders.remove(folder);
                break;
            }
        }
    }

    public void listFoldersOnScrollPane() {
        final Random rng = new Random();
        VBox content = new VBox(5);
        ScrollPane scroller = folderScrollPane;

        for (Folder folder : folders) {
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.setOnMouseClicked(e -> {
                String folderName = ((Label)anchorPane.getChildren().get(0)).getText().replace("Name: ","");
                String path = currentUserPath + "/" + folderName;
                try {
                    File file = new File (path);
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(file);
                } catch (Exception ex) {ex.printStackTrace();}
            });
            String style = String.format("-fx-background: rgb(%d, %d, %d);" +
                            "-fx-background-color: -fx-background;",
                    rng.nextInt(128)+128,
                    rng.nextInt(128)+128,
                    rng.nextInt(128)+128);
            anchorPane.setStyle(style);
            Label nameLabel = new Label("Name: " + folder.getName());
            Label typeLabel = new Label("Type: " + folder.getTypeName());
            nameLabel.setFont(new Font("Roboto", 20));
            typeLabel.setFont(new Font("Roboto", 20));

            AnchorPane.setLeftAnchor(nameLabel, 5.0);
            AnchorPane.setTopAnchor(nameLabel, 3.0);

            AnchorPane.setRightAnchor(typeLabel, 5.0);
            AnchorPane.setTopAnchor(typeLabel, 3.0);

            //ImageView trash = new ImageView("/res/images/trash.png");
            //trash.setPreserveRatio(true);
            //AnchorPane.setRightAnchor(trash, 5.0);
            //AnchorPane.setTopAnchor(trash, 5.0);
            //AnchorPane.setBottomAnchor(trash, 5.0);
            anchorPane.setPrefWidth(590);
            anchorPane.setPrefHeight(30);
            anchorPane.getChildren().add(nameLabel);
            anchorPane.getChildren().add(typeLabel);
            //anchorPane.getChildren().add(trash);
            content.getChildren().add(anchorPane);
        }
        scroller.setContent(content);
    }

    public void loadPreferences() {
        Preferences preferences = DataStore.getPreferencesForCurrentUser();
        separateInOut = preferences.getBoolean(DataStore.getSeparateKey(), true);
    }

    @FXML protected void goToAddNewFolder() {
        Main.getInstance().goToAddNewFolder();
    }

    @FXML protected void goToDisableFolder() {
        Main.getInstance().goToDisableFolder();
    }

    @FXML protected void goToEditFolder() {
        Main.getInstance().goToEditFolder();
    }

    @FXML protected void goToInitializePreferences() {
        Main.getInstance().goToInitializePreferences();
    }

    @FXML protected void goToSetPreferences() {
        Main.getInstance().goToSetPreferences();
    }

    public void addNewFolder(String name, String type, ArrayList<String> keywords){
        Type newType = null;


        for (Type iteratingType : types){
            if (iteratingType.getName().equals(type)){
                newType = iteratingType;
            }
        }
        String path = currentUserPath + "/" + name;
        Folder newFolder = new Folder(name, newType, keywords, path);

        File newFile = new File(path);
        newFolder.setPath(path);
        newFile.mkdir();
        folders.add(newFolder);

    }

    public void saveTypes() {
        DataStore.saveTypes(types, currentUser);
    }
    public void saveFolders() {
        DataStore.saveFolders(folders, currentUser);
    }
    public void saveDisabledFolders() {
        DataStore.saveDisabledFolders(disabledFolders, currentUser);
    }

    public ArrayList<Folder> getFolders() {
        return folders;
    }

    public ArrayList<Type> getTypes() {
        return types;
    }

    public boolean getSeparateInOut() {
        return separateInOut;
    }
}
