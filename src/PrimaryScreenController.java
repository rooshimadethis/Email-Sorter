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
    private ArrayList<Folder> folders;
    private ArrayList<Type> types;

    @FXML
    public void initialize() {
        types = DataStore.loadTypes();
        folders = DataStore.loadFolders();
    }

    @FXML protected void goToAddNewFolder() {
        Main.getInstance().goToAddNewFolder();
    }

    @FXML protected void goToEditFolder() {
        Main.getInstance().goToEditFolder();
    }

    public void addNewFolder(String name, String type, ArrayList<String> keywords){
        for (Type iteratingType : types){
            if (iteratingType.getName().equals(type)){
                folders.add(new Folder(name, iteratingType, keywords));
            }
        }
    }

    public void saveTypes() {
        DataStore.saveTypes(types);
    }

    public ArrayList<Folder> getFolders() {
        return folders;
    }

    public ArrayList<Type> getTypes() {
        return types;
    }
}
