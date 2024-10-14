package team4s;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MenuController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    // Switch to manager GUI (Manager.fxml)
    public void switchToManager(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ManagerMenu.fxml"));
            root = loader.load();

            // Get the controller for Manager.fxml and initialize the table view
            // ManagerController controller = loader.getController();
            // controller.initializeManager(); // Initialize the Manager-specific logic

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Switch to cashier GUI (Cashier.fxml)
    public void switchToCashier(ActionEvent event) {
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/Cashier.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
