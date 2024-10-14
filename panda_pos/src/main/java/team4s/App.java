package team4s;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    static Stage mainStage;

    /**
     * Serves as the entry point for the application.
     * It launches the application by calling the launch method.
     *
     * @param args the command line arguments used for execution
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the application by setting up the primary stage.
     * This method is called after the application is initialized.
     *
     * @param stage the primary stage, used to set the application scene
     * @throws Exception if an error occurs while loading the Menu.fxml file or
     *                   showing the stage
     */
    @Override
    public void start(Stage stage) {
        try {
            // load fxml file
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Menu.fxml"));

            // create scene
            Scene scene = new Scene(root);

            // set scene to stage
            stage.setScene(scene);
            stage.setTitle("POS");

            // show stage
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
