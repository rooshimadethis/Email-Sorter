import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

import java.util.prefs.Preferences;

public class SetPreferencesController {

    @FXML private CheckBox deleteCheckBox;
    @FXML private CheckBox readCheckBox;
    @FXML private CheckBox receivedCheckBox;
    @FXML private CheckBox sentCheckBox;
    @FXML private CheckBox separateCheckBox;
    @FXML private ComboBox<String> saveDelayComboBox;
    @FXML private Button nextButton;


    @FXML
    public void initialize() {
        System.setProperty("prism.lcdtext", "true");

        ObservableList<String> saveDelays =
                FXCollections.observableArrayList();
        saveDelays.add(0, "Any age");
        saveDelays.add(1, "1 Week");
        saveDelays.add(2, "2 Weeks");
        saveDelays.add(3, "1 Month");
        saveDelayComboBox.setItems(saveDelays);

        Preferences preferences = DataStore.getPreferencesForCurrentUser();
        deleteCheckBox.setSelected(preferences.getBoolean(DataStore.getDeleteKey(), false));
        readCheckBox.setSelected(preferences.getBoolean(DataStore.getReadKey(), true));
        receivedCheckBox.setSelected(preferences.getBoolean(DataStore.getIncomingKey(), false));
        sentCheckBox.setSelected(preferences.getBoolean(DataStore.getOutgoingKey(), false));
        separateCheckBox.setSelected(preferences.getBoolean(DataStore.getSeparateKey(), true));
        saveDelayComboBox.setValue(preferences.get(DataStore.getSaveDelayKey(), "1 Month"));

        if (!receivedCheckBox.isSelected() || !sentCheckBox.isSelected()) {
            separateCheckBox.setDisable(true);
        }

        receivedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (receivedCheckBox.isSelected() && sentCheckBox.isSelected()){
                separateCheckBox.setDisable(false);
            } else {
                separateCheckBox.setSelected(false);
                separateCheckBox.setDisable(true);
            }
        });
        sentCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (receivedCheckBox.isSelected() && sentCheckBox.isSelected()){
                separateCheckBox.setDisable(false);
            } else {
                separateCheckBox.setSelected(false);
                separateCheckBox.setDisable(true);
            }
        });
        saveDelayComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (saveDelayComboBox.getSelectionModel().getSelectedItem() != null){
                nextButton.setDisable(false);
            }
        });
    }

    @FXML protected void savePreferences() {
        Preferences preferences = DataStore.getPreferencesForCurrentUser();
        preferences.putBoolean(DataStore.getDeleteKey(), deleteCheckBox.isSelected());
        preferences.putBoolean(DataStore.getReadKey(), readCheckBox.isSelected());
        preferences.putBoolean(DataStore.getIncomingKey(), receivedCheckBox.isSelected());
        preferences.putBoolean(DataStore.getOutgoingKey(), sentCheckBox.isSelected());
        preferences.putBoolean(DataStore.getSeparateKey(), separateCheckBox.isSelected());
        if (saveDelayComboBox.getValue() != null) {
            preferences.put(DataStore.getSaveDelayKey(), saveDelayComboBox.getValue());
        }
        Main.getInstance().getCurrentUser().finishedInitialSetup();
        Main.getInstance().closeModalWindow();
    }

}

