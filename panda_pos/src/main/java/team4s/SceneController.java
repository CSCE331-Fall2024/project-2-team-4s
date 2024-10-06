package team4s;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.SQLException;

public class SceneController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;

    // test database connection
    public void initialize() {
        try {
            conn = Database.connect();
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.err.println("Database connection error");
        }
    }

    // switch to specified scene
    public void switchToScene(ActionEvent event, String fxmlFile) {
        try {
            root = FXMLLoader.load(getClass().getResource(fxmlFile));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // switch to manager gui
    public void switchToManager(ActionEvent event) {
        switchToScene(event, "/fxml/Manager.fxml");
    }

    // switch to cashier gui
    public void switchToCashier(ActionEvent event) {
        switchToScene(event, "/fxml/Cashier.fxml");
    }

    // switch to menu gui
    public void switchToMenu(ActionEvent event) {
        switchToScene(event, "/fxml/Menu.fxml");
    }
}