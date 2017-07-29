import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private FXMLLoader loader;
    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        loader = new FXMLLoader(getClass().getResource("/fxml/startup.fxml"));
        Parent root = loader.load();
        controller = loader.getController();


        primaryStage.setTitle("Welcome");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        controller.saveUsers();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
