import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
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
        folderDropdown.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                listFoldersOnScrollPane();
            }
        });


        folderListScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        retrievedFolders = Main.getInstance().getPrimaryScreenController().getFolders();
        fillUpComboBox();
        //listFoldersOnScrollPane();

    }

    private void fillUpComboBox() {
        ObservableList<String> folderNameList =
                FXCollections.observableArrayList();
        for (Folder folder : retrievedFolders){
            folderNameList.add(folder.getName());
        }
        folderDropdown.setItems(folderNameList);
    }

    private Folder getChosenFolderFromDropdown() {
        for (Folder folder : retrievedFolders){
            if (folderDropdown.getValue().equals(folder.getName())){
                return folder;
            }
        }
        return null;
    }

    private void listFoldersOnScrollPane() {
        final Random rng = new Random();
        VBox content = new VBox(5);
        ScrollPane scroller = folderListScrollPane;

        Folder folderChosen = getChosenFolderFromDropdown();
        if (folderChosen != null) {
            ArrayList<Subfolder> subfolders = getChosenFolderFromDropdown().getSubfolders();

            for (Subfolder folder : subfolders) {
                AnchorPane anchorPane = new AnchorPane();
                String style = String.format("-fx-background: rgb(%d, %d, %d);" +
                                "-fx-background-color: -fx-background;",
                        rng.nextInt(128),
                        rng.nextInt(128),
                        rng.nextInt(128));
                anchorPane.setStyle(style);
                Label label = new Label(folder.getName());
                AnchorPane.setLeftAnchor(label, 5.0);
                AnchorPane.setTopAnchor(label, 5.0);

                ImageView trash = new ImageView("/res/images/trash.png");
                trash.setPreserveRatio(true);
                AnchorPane.setRightAnchor(trash, 5.0);
                AnchorPane.setTopAnchor(trash, 5.0);
                AnchorPane.setBottomAnchor(trash, 5.0);
                anchorPane.getChildren().add(label);
                anchorPane.getChildren().add(trash);
                content.getChildren().add(anchorPane);
            }
        }
        scroller.setContent(content);
    }
}

