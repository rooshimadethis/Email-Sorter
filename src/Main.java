import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {
    private static Main instance;

    private StartupController startupController;
    private PrimaryScreenController primaryScreenController;
    private Stage stage;
    private Stage modal;
    private User currentUser;
    private String hardDriveName;
    private String rootFolder;
    private Authorizer authorizer;


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
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadHardDriveData();

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (primaryScreenController != null) {
            primaryScreenController.saveTypes();
            primaryScreenController.saveFolders();
            primaryScreenController.saveDisabledFolders();
        }
        startupController.saveUsers();

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
            replaceSceneContent("/fxml/startup.fxml", Paint.valueOf(Design.getPrimaryLightColor()), 600, 400);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToInitializeHardDrive() {
        try {
            replaceSceneContent("/fxml/initializeHardDrive.fxml", Paint.valueOf(Design.getPrimaryLightColor()), 400, 150);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToInitializePreferences() {
        try {
            replaceSceneContent("/fxml/initializePreferences.fxml", Paint.valueOf(Design.getPrimaryLightColor()), 500, 300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToPrimaryScreen() {
        try {
            replaceSceneContent("/fxml/primaryScreen.fxml", Paint.valueOf(Design.getPrimaryLightColor()), 600, 500);
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

    public void goToDisableFolder() {
        try {
            popUpModalWindow("/fxml/disableFolder.fxml");
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

    public void goToSetPreferences() {
        try {
            popUpModalWindow("/fxml/setPreferences.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void popUpModalWindow(String fxml) {
        try {
            modal = new Stage();

            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
            Parent root = loader.load();
            modal.getIcons().add(new Image("/res/images/icon_512.png"));
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

    private Parent replaceSceneContent(String fxml, Paint fill, int width, int height) {
        try {

            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
            Parent page = loader.load();
            //Parent page = FXMLLoader.load(Main.class.getResource(fxml));

            if (fxml.contains("startup")) {
                startupController = loader.getController();
            } else if (fxml.contains("primaryScreen")){
                primaryScreenController = loader.getController();
            }

            stage.close();
            Scene scene = new Scene(page, width, height);
            //scene.setFill(Color.TRANSPARENT);
            stage = new Stage();
            //stage.initStyle(StageStyle.TRANSPARENT);
            stage.getIcons().add(new Image("/res/images/icon_512.png"));
            stage.setScene(scene);

            stage.show();

            return page;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {

        //System.setProperty("prism.lcdtext", "false");
        //System.setProperty("prism.subpixeltext", "false");

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

    public void setHardDriveName(String hardDriveName) {
        this.hardDriveName = hardDriveName;
    }

    public Authorizer getAuthorizer() {
        return authorizer;
    }

    public void setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
    }
}
