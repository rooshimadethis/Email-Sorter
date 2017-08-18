import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
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
import java.util.Random;
import java.util.prefs.Preferences;

public class PrimaryScreenController {
    private ArrayList<Folder> folders;
    private ArrayList<Folder> disabledFolders;
    private ArrayList<Type> types;
    private String currentDrivePath;
    private String currentFullPath;
    private boolean separateInOut;

    @FXML private ScrollPane folderScrollPane;

    @FXML
    public void initialize() {
        types = DataStore.loadTypes();
        folders = DataStore.loadFolders();
        disabledFolders = DataStore.loadDisabledFolders();

        getHardDriveData();

        listFoldersOnScrollPane();

        loadPreferences();
    }

    private void getHardDriveData() {
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
            
        } else {
            currentDrivePath = expectedRoot.getAbsolutePath().substring(0,2);
            currentFullPath = expectedRoot.getAbsolutePath();
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
                String path = currentFullPath + "/" + folderName;
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
            Label typeLabel = new Label("Type: " + folder.getType().getName());
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
        Preferences preferences = DataStore.getPreferencesforCurrentUser();
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

    public void addNewFolder(String name, String type, ArrayList<String> keywords){
        Type newType = null;


        for (Type iteratingType : types){
            if (iteratingType.getName().equals(type)){
                newType = iteratingType;
            }
        }
        String path = currentFullPath + "/" + name;
        Folder newFolder = new Folder(name, newType, keywords, path);

        File newFile = new File(path);
        newFolder.setPath(path);
        newFile.mkdir();
        folders.add(newFolder);

    }

    public void saveTypes() {
        DataStore.saveTypes(types);
    }
    public void saveFolders() {
        DataStore.saveFolders(folders);
    }
    public void saveDisabledFolders() {
        DataStore.saveDisabledFolders(disabledFolders);
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
