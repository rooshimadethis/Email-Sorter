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

    /**
     * Initialize mainly sets the combobox to contain the available types and sets the listener for the combobox
     */
    @FXML
    public void initialize() {
		
		//for text antialiasing
        System.setProperty("prism.lcdtext", "true");

        keywordTextArea.setWrapText(true);

		//this chunk of code adds a listener for the dropdown to add the premade Types into the textArea
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


		//populate dropdown with the available types
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
                //Main.getInstance().getPrimaryScreenController().listFoldersOnScrollPane();
            }
        }
    }
    //TODO set warning/cancel
        //closes the open windows after a folder is created
        Main.getInstance().closeModalWindow();


}
}
