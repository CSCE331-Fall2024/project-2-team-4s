package team4s;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EmployeeController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;
    @FXML
    private TableView<Employee> employeeTable;
        // Method to initialize only when Manager.fxml is loaded
        public void initialize() {
            try {
                conn = Database.connect();
                //System.out.println("Database connection opened");
    
    
                // load the employees
                loadEmployees();
    
                conn.close();
                //System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Database connection error");
            }
        }
    // load employees into the table view
    private void loadEmployees() {
        ObservableList<Employee> employees = FXCollections.observableArrayList();

        String query = "SELECT * FROM employee ORDER BY employee_id";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                // add a new employee object to the list for each row in the result set
                employees.add(new Employee(
                        rs.getInt("employee_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // set the employees in the table view
        employeeTable.setItems(employees);
    }
    public void showAddEmployeeModal() {
        // create a stage for the modal
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Add New Employee");

        // create vbox for modal layout
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // input fields for the new employee
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        Label roleLabel = new Label("Role:");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Manager", "Cashier", "Chef");
        roleComboBox.setPromptText("Select Role");

        // button to submit the new employee
        Button addButton = new Button("Add Employee");

        addButton.setOnAction(e -> {
            // get input data
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String role = roleComboBox.getValue();

            // insert the new employee into the database
            try {
                conn = Database.connect();
                System.out.println("Database connection opened");
                String insertQuery = "INSERT INTO employee (first_name, last_name, role) VALUES (?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                // set the values for the statement
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, role);

                stmt.executeUpdate(); // execute the insert statement

                loadEmployees(); // refresh the table view

                conn.close();
                System.out.println("Database connection closed");
                modal.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // add elements to the vbox
        vbox.getChildren().addAll(firstNameLabel, firstNameField, lastNameLabel, lastNameField, roleLabel,
                roleComboBox, addButton);

        // create a scene with the vbox and display the modal
        Scene modalScene = new Scene(vbox, 300, 250);
        modal.setScene(modalScene);
        modal.showAndWait();
    }

    public void showEditEmployeeModal() {
        // get the selected employee
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();

        if (selectedEmployee == null) {
            // show an alert if no employee is selected
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Employee Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an employee to edit");
            alert.showAndWait();
            return;
        }

        // create a stage for the modal
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Edit Employee Information");

        // create vbox for modal layout
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // input fields for the employee
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField(selectedEmployee.getFirstName());

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField(selectedEmployee.getLastName());

        Label roleLabel = new Label("Role:");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Manager", "Cashier", "Chef");
        roleComboBox.setValue(selectedEmployee.getRole());

        // button to upadte the edited employee
        Button updateButton = new Button("Update Employee");

        updateButton.setOnAction(e -> {
            // get input data
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String role = roleComboBox.getValue();

            // update the employee in the database
            try {
                conn = Database.connect();
                System.out.println("Database connection opened");
                String updateQuery = "UPDATE employee SET first_name = ?, last_name = ?, role = ? WHERE employee_id = ?";

                PreparedStatement stmt = conn.prepareStatement(updateQuery);
                // set the values for the statement
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, role);
                stmt.setInt(4, selectedEmployee.getEmployeeID());

                stmt.executeUpdate(); // execute the update statement

                loadEmployees(); // refresh the table view

                conn.close();
                System.out.println("Database connection closed");
                modal.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // add elements to the vbox
        vbox.getChildren().addAll(firstNameLabel, firstNameField, lastNameLabel, lastNameField, roleLabel,
                roleComboBox, updateButton);

        // display modal
        Scene modalScene = new Scene(vbox, 300, 250);
        modal.setScene(modalScene);
        modal.showAndWait();
    };

    public void showDeleteEmployeeModal() {
        // get the selected employee
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();

        if (selectedEmployee != null) {
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Delete Confirmation");
            confirmationAlert.setHeaderText("Are you sure you want to delete this employee?");
            confirmationAlert.setContentText(
                    "Employee: " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName());

            // wait for user confirmation
            Optional<ButtonType> response = confirmationAlert.showAndWait();

            if (response.isPresent() && response.get() == ButtonType.OK) {
                try {
                    conn = Database.connect();
                    System.out.println("Database connection opened");
                    String deleteQuery = "DELETE FROM employee WHERE employee_id = ?";

                    PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                    // set the employee id for the statement
                    stmt.setInt(1, selectedEmployee.getEmployeeID());

                    stmt.executeUpdate(); // execute the delete statement

                    loadEmployees(); // refresh the table view

                    conn.close();
                    System.out.println("Database connection closed");
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        } else {
            // show an alert if no employee is selected
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Employee Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an employee to delete");
            alert.showAndWait();
            return;
        }
    };
    // Switch to manager menu
    public void switchToManager(ActionEvent event) {
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/ManagerMenu.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
