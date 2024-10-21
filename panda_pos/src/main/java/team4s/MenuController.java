package team4s;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class MenuController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private boolean isManager = false;
    private Connection conn;

    /**
     * Shows a modal to validate the user for access to the manager GUI.
     */
    public void showValidateManager() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Manager Login");

        // create vbox for modal layout
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // input field for manager id
        Label managerLabel = new Label("Employee ID:");
        TextField managerId = new javafx.scene.control.TextField();
        managerId.setPromptText("Employee ID");

        // label to display error messages
        Label errorLabel = new Label();

        // button to submit id
        Button submitButton = new javafx.scene.control.Button("Submit");

        submitButton.onActionProperty().set(e -> {
            // check if the id is empty
            if (managerId.getText().isEmpty()) {
                errorLabel.setText("Please enter an employee ID");
                return;
            }

            // check if the id is valid
            try {
                conn = Database.connect();
                String query = "SELECT role FROM employee WHERE employee_id = ?";

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, Integer.parseInt(managerId.getText()));

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    if (rs.getString("role").equals("Manager")) {
                        isManager = true;
                    } else {
                        isManager = false;
                    }
                }

                conn.close();
                modal.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // add elements to the vbox
        vbox.getChildren().addAll(managerLabel, managerId, errorLabel, submitButton);

        // create a scene with the vbox and display the modal
        Scene modalScene = new Scene(vbox, 300, 250);
        modal.setScene(modalScene);
        modal.showAndWait();

        if (!isManager) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Invalid Employee ID");
            alert.setHeaderText(null);
            alert.setContentText("You do not have access to the Manager page.");
            alert.showAndWait();

            return;
        }
    }

    /**
     * Switches to the manager GUI.
     * 
     * @param event the ActionEvent object
     */
    public void switchToManager(ActionEvent event) {
        showValidateManager();

        if (isManager) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ManagerMenu.fxml"));
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

    /**
     * Switches to the cashier GUI.
     * 
     * @param event the ActionEvent object
     */
    public void switchToCashier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Cashier.fxml"));
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
