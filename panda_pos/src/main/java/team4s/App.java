package team4s;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    static Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * start method to show the stage
     * 
     * @params stage the stage to be shown
     * @returns void
     * @throws Exception if an error occurs
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
