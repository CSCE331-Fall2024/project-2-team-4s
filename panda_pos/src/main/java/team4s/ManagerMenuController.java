package team4s;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ManagerMenuController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    // Switch to employee GUI (Employee.fxml)
    /*
     * Switches to the employee GUI (Employee.fxml) when the "Employee" button is
     * clicked.
     * 
     * @param event the ActionEvent object
     */
    public void switchToEmployee(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Employee.fxml"));
            root = loader.load();

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Switch to inventory restock GUI (InventoryRestock.fxml)
    /*
     * Switches to the inventory restock GUI (InventoryRestock.fxml) when the
     * "Inventory Restock" button is clicked.
     * 
     * @param event the ActionEvent object
     */
    public void switchToInventoryRestock(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InventoryRestock.fxml"));
            root = loader.load();

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Switch to item menu GUI (ItemMenu.fxml)
    /*
     * Switches to the item menu GUI (ItemMenu.fxml) when the "Item Menu" button is
     * clicked.
     * 
     * @param event the ActionEvent object
     */
    public void switchToItemMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemMenu.fxml"));
            root = loader.load();

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Switch to reports GUI (Reports.fxml)
    /*
     * Switches to the reports GUI (Reports.fxml) when the "Reports" button is
     * clicked.
     * 
     * @param event the ActionEvent object
     */
    public void switchToReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Reports.fxml"));
            root = loader.load();

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Switch to menu GUI (Menu.fxml)
    /*
     * Switches to the menu GUI (Menu.fxml) when the "Menu" button is clicked.
     * 
     * @param event the ActionEvent object
     */
    public void switchToMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Menu.fxml"));
            root = loader.load();

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
