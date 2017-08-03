import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AddNewFolderController {

    @FXML private TextArea keywordTextArea;
    @FXML private TextField nameTextField;
    private String oldArea;

    @FXML
    public void initialize() {
        nameTextField.textProperty().addListener(new ChangeListener<String>() {
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
    }


    private void changed(ObservableValue<? extends String> observable, String oldText, String newText) {
        keywordTextArea.setText(newText);
    }
}
