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
import javafx.stage.DirectoryChooser;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class PrimaryScreenController {
    private ArrayList<Folder> folders;
    private ArrayList<Type> types;
    private String currentDrivePath;
    private String currentFullPath;

    @FXML
    public void initialize() {
        types = DataStore.loadTypes();
        folders = DataStore.loadFolders();

        getHardDriveData();

        System.out.println();
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

    @FXML protected void goToAddNewFolder() {
        Main.getInstance().goToAddNewFolder();
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

    public ArrayList<Folder> getFolders() {
        return folders;
    }

    public ArrayList<Type> getTypes() {
        return types;
    }
}
