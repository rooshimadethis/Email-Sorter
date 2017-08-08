import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class Main extends Application {
    private static Main instance;

    private StartupController startupController;
    private PrimaryScreenController primaryScreenController;
    private Stage stage;
    private Stage modal;
    private User currentUser;
    private String hardDriveName;
    private String rootFolder;


    public Main() {
        instance = this;
    }

    // static method to get instance of view
    public static Main getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            stage = primaryStage;
            goToStartup();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadHardDriveData();

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        startupController.saveUsers();
        primaryScreenController.saveTypes();
        primaryScreenController.saveFolders();
        DataStore.saveHardDriveInfo(hardDriveName, rootFolder);
    }

    private void loadHardDriveData(){
        String[] data = DataStore.loadHardDriveInfo();
        if (data.length == 2) {
            hardDriveName = data[0];
            rootFolder = data[1];
        }
    }

    public void goToStartup() {
        try {
            replaceSceneContent("/fxml/startup.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToInitializeHardDrive() {
        try {
            replaceSceneContent("/fxml/initializeHardDrive.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToInitializePreferences() {
        try {
            replaceSceneContent("/fxml/initializePreferences.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToPrimaryScreen() {
        try {
            replaceSceneContent("/fxml/primaryScreen.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToAddNewFolder() {
        try {
            popUpModalWindow("/fxml/addNewFolder.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToEditFolder() {
        try {
            popUpModalWindow("/fxml/editFolder.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void popUpModalWindow(String fxml) {
        try {
            modal = new Stage();

            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
            Parent root = loader.load();

            modal.setScene(new Scene(root));
            modal.initModality(Modality.APPLICATION_MODAL);

            modal.initOwner(modal.getOwner());

            modal.showAndWait();

        } catch (Exception e) {e.printStackTrace();}

    }

    public void closeModalWindow(){
        if (modal != null){
            modal.close();
        }
    }

    private Parent replaceSceneContent(String fxml) {
        try {

            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
            Parent page = loader.load();
            //Parent page = FXMLLoader.load(Main.class.getResource(fxml));

            if (fxml.contains("startup")) {
                startupController = loader.getController();
            } else if (fxml.contains("primaryScreen")){
                primaryScreenController = loader.getController();
            }

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(page, 700, 450);
                //scene.getStylesheets().add(Main.class.getResource("demo.css").toExternalForm());
                stage.setScene(scene);
            } else {
                stage.getScene().setRoot(page);
            }
            stage.sizeToScene();
            return page;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Stage getStage() {
        return stage;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public PrimaryScreenController getPrimaryScreenController() {
        return primaryScreenController;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public String getHardDriveName() {
        return hardDriveName;
    }

    public void setHardDriveName(String hardDrivename) {
        this.hardDriveName = hardDrivename;
    }
}
