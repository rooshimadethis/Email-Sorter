import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Random;


public class EditFolderController {

    @FXML private ComboBox<String> folderDropdown;
    @FXML private ScrollPane folderListScrollPane;
    private ArrayList<Folder> retrievedFolders;

    @FXML
    public void initialize() {
        retrievedFolders = Main.getInstance().getPrimaryScreenController().getFolders();
        fillUpComboBox();
        listFoldersOnScrollPane();

    }

    private void fillUpComboBox() {
        ObservableList<String> folderNameList =
                FXCollections.observableArrayList();
        for (Folder folder : retrievedFolders){
            folderNameList.add(folder.getName());
        }
        folderDropdown.setItems(folderNameList);
    }

    private void listFoldersOnScrollPane() {
        final Random rng = new Random();
        VBox content = new VBox(5);
        ScrollPane scroller = folderListScrollPane;
        AnchorPane anchorPane = new AnchorPane();
        String style = String.format("-fx-background: rgb(%d, %d, %d);"+
                        "-fx-background-color: -fx-background;",
                rng.nextInt(256),
                rng.nextInt(256),
                rng.nextInt(256));
        anchorPane.setStyle(style);
        Label label = new Label("Pane "+(content.getChildren().size()+1));
        AnchorPane.setLeftAnchor(label, 5.0);
        AnchorPane.setTopAnchor(label, 5.0);
        Button button = new Button("Remove");
        AnchorPane.setRightAnchor(button, 5.0);
        AnchorPane.setTopAnchor(button, 5.0);
        AnchorPane.setBottomAnchor(button, 5.0);
        anchorPane.getChildren().addAll(label, button);
        content.getChildren().add(anchorPane);
    }
}

