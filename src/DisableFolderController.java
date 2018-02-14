import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DisableFolderController {
    @FXML private ScrollPane scrollPane;

    @FXML
    public void initialize() {
        listFoldersOnScrollPane();

    }

    private void listFoldersOnScrollPane() {
        final Random rng = new Random();
        VBox content = new VBox(5);
        ScrollPane scroller = scrollPane;

        ArrayList<Folder> folders = Main.getInstance().getPrimaryScreenController().getFolders();
        for (Folder folder : folders) {
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.setOnMouseClicked(e -> {
                String folderName = ((Label)anchorPane.getChildren().get(0)).getText();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + folderName + "\" ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    for(Folder folder2 : folders){
                        if (folder2.getName().equals(folderName)){
                            moveToDisabledList(folder2.getName());
                            break;
                        }
                    }
                }
            });
            String style = String.format("-fx-background: rgb(%d, %d, %d);" +
                            "-fx-background-color: -fx-background;",
                    rng.nextInt(128)+128,
                    rng.nextInt(128)+128,
                    rng.nextInt(128)+128);
            anchorPane.setStyle(style);
            Label label = new Label(folder.getName());
            AnchorPane.setLeftAnchor(label, 5.0);
            AnchorPane.setTopAnchor(label, 5.0);

            ImageView trash = new ImageView("/res/images/trash.png");
            trash.setPreserveRatio(true);
            AnchorPane.setRightAnchor(trash, 5.0);
            AnchorPane.setTopAnchor(trash, 5.0);
            AnchorPane.setBottomAnchor(trash, 5.0);
            anchorPane.setPrefWidth(590);
            anchorPane.getChildren().add(label);
            anchorPane.getChildren().add(trash);
            content.getChildren().add(anchorPane);
        }
        scroller.setContent(content);
    }

    private void moveToDisabledList(String folderName) {
        Main.getInstance().getPrimaryScreenController().moveFolderToDisabled(folderName);
        //listFoldersOnScrollPane();
        //Main.getInstance().getPrimaryScreenController().listFoldersOnScrollPane();
    }


    }

