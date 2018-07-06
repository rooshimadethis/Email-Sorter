import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.effects.JFXDepthManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.prefs.Preferences;

public class PrimaryScreenController {
    private final Random rand = new Random();
    private ArrayList<Folder> folders;
    private ArrayList<Folder> disabledFolders;
    private ArrayList<Type> types;
    private String currentDrivePath;
    private String currentFullPath;
    private String currentUserPath;
    private boolean separateInOut;
    private User currentUser;

    //@FXML private ScrollPane folderScrollPane;
    @FXML private JFXMasonryPane masonryPane;
    @FXML private JFXProgressBar progressSpinner;
    @FXML private JFXButton processEmailsButton;
    @FXML private ImageView addFolderImageView;
    @FXML private ImageView disableFolderImageView;
    @FXML private ImageView editFolderImageView;
    @FXML private ImageView preferencesImageView;

    @FXML
    public void initialize() {
        System.setProperty("prism.lcdtext", "true");

        addFolderImageView.smoothProperty().setValue(true);
        disableFolderImageView.smoothProperty().setValue(true);
        editFolderImageView.smoothProperty().setValue(true);
        preferencesImageView.smoothProperty().setValue(true);

        currentUser = Main.getInstance().getCurrentUser();
        progressSpinner.setVisible(false);
        types = DataStore.loadTypes(currentUser);
        folders = DataStore.loadFolders(currentUser);
        disabledFolders = DataStore.loadDisabledFolders(currentUser);

        getHardDriveData();

        listFoldersOnScrollPane();

        loadPreferences();

        JFXDepthManager.setDepth(addFolderImageView, 3);
        JFXDepthManager.setDepth(disableFolderImageView, 3);
        JFXDepthManager.setDepth(editFolderImageView, 3);
        JFXDepthManager.setDepth(preferencesImageView, 3);
    }

    /**
     * This method starts the flow of saving and/or deleting the emails from Gmail
     */
    @FXML protected void processEmails() {
        progressSpinner.setVisible(true);

            Thread emails = new Thread(() -> {
                GmailHandler gmailHandler = new GmailHandler();
                gmailHandler.processEmailsForCurrentFolders();
                Platform.runLater(() -> progressSpinner.setVisible(false));
            });
            emails.start();

    }

    /**
     * I'm particularly proud about this feature. Since external hard drives can change drive letter, the program
     *  saves information about the Hard Drive itself, including the name so that if the drive letter changes, the emails
     *  folder can be found anyways
     */
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
            StringBuilder driveName = new StringBuilder();
            for (int i = 0; i < separated.length; i++) {
                if (separated[i].equals("(" + drive + ":)")){
                } else {
                    if (i == 0){
                        driveName.append(separated[i]);
                    } else {
                        driveName.append(" ").append(separated[i]);
                    }
                }
            }
            Main.getInstance().setHardDriveName(driveName.toString());

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
                removeFolderFromMasonryPane(folderName);
                break;
            }
        }
    }

    public void listFoldersOnScrollPane() {
        //VBox content = new VBox(5);
        //ScrollPane scroller = folderScrollPane;
        JFXMasonryPane mPane = masonryPane;

        /*for (Folder folder : folders) {
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
                    rand.nextInt(128)+128,
                    rand.nextInt(128)+128,
                    rand.nextInt(128)+128);
            anchorPane.setStyle(style);
            Label nameLabel = new Label("Name: " + folder.getName());
            Label typeLabel = new Label("Type: " + folder.getTypeName());
            nameLabel.setFont(new Font("Roboto", 18));
            nameLabel.setStyle("-fx-text-fill: white");
            typeLabel.setFont(new Font("Roboto", 18));
            typeLabel.setStyle("-fx-text-fill: white");


            AnchorPane.setLeftAnchor(nameLabel, 5.0);
            AnchorPane.setTopAnchor(nameLabel, 3.0);

            AnchorPane.setRightAnchor(typeLabel, 5.0);
            AnchorPane.setTopAnchor(typeLabel, 3.0);

            anchorPane.setPrefWidth(599);
            anchorPane.setPrefHeight(30);
            anchorPane.getChildren().add(nameLabel);
            anchorPane.getChildren().add(typeLabel);
            content.getChildren().add(anchorPane);
        }
        scroller.setContent(content);
        */
        ArrayList<Node> children = new ArrayList<>();
        for (Folder folder : folders) {
            addFolderToScrollPane(folder);
        }
    }

    private void addFolderToScrollPane(Folder newFolder) {
        StackPane stackpane = new StackPane();
        stackpane.setOnMouseClicked(e -> {
            String folderName = ((Label)stackpane.getChildren().get(0)).getText().replace("Name: ","");
            String path = currentUserPath + "/" + folderName;
            try {
                File file = new File (path);
                Desktop desktop = Desktop.getDesktop();
                desktop.open(file);
            } catch (Exception ex) {ex.printStackTrace();}
        });
        String style = String.format("-fx-background: rgb(%d, %d, %d);" +
                        "-fx-background-color: -fx-background;",
                rand.nextInt(140)+110,
                rand.nextInt(140)+110,
                rand.nextInt(140)+115);
        stackpane.setStyle(style);
        Label nameLabel = new Label(newFolder.getName());
        nameLabel.translateYProperty().setValue(-10);
        JFXDepthManager.setDepth(nameLabel, 1);
        Label typeLabel = new Label("Type: " + newFolder.getTypeName());
        typeLabel.translateYProperty().setValue(10);
        nameLabel.setFont(new Font("Roboto", 18));
        nameLabel.setStyle("-fx-text-fill: white");
        typeLabel.setFont(new Font("Roboto", 16));
        typeLabel.setStyle("-fx-text-fill: white");

        double nameWidth = TextUtils.computeTextWidth(nameLabel.getFont(), nameLabel.getText(), 0.0D);
        double typeWidth = TextUtils.computeTextWidth(typeLabel.getFont(), typeLabel.getText(), 0.0D);
        stackpane.setMinWidth(Math.max(nameWidth, typeWidth) + 5 + rand.nextInt(10));
        stackpane.setPrefHeight(50);
        JFXDepthManager.setDepth(stackpane, 1);
        stackpane.getChildren().add(nameLabel);
        stackpane.getChildren().add(typeLabel);
        stackpane.setId(newFolder.getName());
        masonryPane.getChildren().add(stackpane);
    }

    private void removeFolderFromMasonryPane(String folderName) {
        ObservableList<Node> children = masonryPane.getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getId().equals(folderName)) {
                masonryPane.getChildren().remove(i);
                break;
            }
        }
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
        addFolderToScrollPane(newFolder);

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
