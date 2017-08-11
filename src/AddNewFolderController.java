import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Arrays;

public class AddNewFolderController {

    @FXML private TextArea keywordTextArea;
    @FXML private ComboBox<String> typesComboBox;
    @FXML private TextField nameTextField;
    private String oldArea;

    @FXML
    public void initialize() {
        keywordTextArea.setWrapText(true);
        /*nameTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String newArea = keywordTextArea.getText().replace(oldArea + ", ", "");
                String[] texts = {nameTextField.getText(), newArea};
                if (texts[0].length() > 0) {
                    keywordTextArea.setText(texts[0] + ", " + texts[1]);
                } else {
                    keywordTextArea.setText(texts[1]);
                }
                oldArea = nameTextField.getText();
            }
        });
        */

        typesComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String selected = typesComboBox.getSelectionModel().getSelectedItem();
                if (selected != null){
                    ArrayList<Type> types = Main.getInstance().getPrimaryScreenController().getTypes();
                    for (Type type : types){
                        if (type.getName().equals(selected)){
                            String keywords = "";
                            for (String subcategory : type.getSubcategories()){
                                keywords += (subcategory + ", ");
                            }
                            keywordTextArea.appendText(keywords);
                            break;
                        }
                    }
                }
            }
        });



        ObservableList<String> typesList =
                FXCollections.observableArrayList();
        ArrayList<Type> typesArrayList = Main.getInstance().getPrimaryScreenController().getTypes();
        for (Type type : typesArrayList){
            typesList.add(type.getName());
        }
        typesComboBox.setItems(typesList);
    }

    @FXML protected void createNewFolder(){
        String name = nameTextField.getText();
        String type = typesComboBox.getValue();
        String[] rawKeysArray = keywordTextArea.getText().split(", ");
        ArrayList<String> keywords = new ArrayList<String>(Arrays.asList(rawKeysArray));

        if (name != null) {
            if (!name.equals("")) {
                Boolean exists = false;
                for (Folder currentFolder : Main.getInstance().getPrimaryScreenController().getFolders()) {
                    if (currentFolder.getName().equals(name)) {
                        exists = true;
                }
            }
            if (!exists) {
                Main.getInstance().getPrimaryScreenController().addNewFolder(name, type, keywords);

            }
        }
    }
    //TODO set warning/cancel
        Main.getInstance().closeModalWindow();


}
}
