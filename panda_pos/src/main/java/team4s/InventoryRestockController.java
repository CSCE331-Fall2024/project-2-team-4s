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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
public class InventoryRestockController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;

    @FXML
    private TableView<InventoryItem> inventoryTable;
    // Method to initialize only when Manager.fxml is loaded
    public void initialize() {
        try {
            conn = Database.connect();
            //System.out.println("Database connection opened");

            // Load the menu items into the TableView
            loadInventoryItems();

            conn.close();
            //System.out.println("Database connection closed");
        } catch (SQLException e) {
            System.err.println("Database connection error");
        }
    }
    // Load Inventory Items
    private void loadInventoryItems() {
        ObservableList<InventoryItem> inventoryItems = FXCollections.observableArrayList();

        String query = "SELECT * FROM inventory ORDER BY ingredient_id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                inventoryItems.add(new InventoryItem(
                        rs.getInt("ingredient_id"),
                        rs.getString("ingredient_name"),
                        rs.getInt("current_stock"),
                        rs.getDouble("price"),
                        rs.getString("unit"),
                        rs.getInt("min_stock")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the items in the TableView
        inventoryTable.setItems(inventoryItems);
    }

    // Load Ingredients
    private void loadIngredients(ListView<InventoryItem> ingredientListView) {
        ObservableList<InventoryItem> ingredients = FXCollections.observableArrayList();

        // Fetch ingredients from the database
        try {
            conn = Database.connect();
            String fetchIngredientsQuery = "SELECT ingredient_id, ingredient_name, current_stock, price, unit, min_stock FROM inventory ORDER BY ingredient_name";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(fetchIngredientsQuery)) {
                while (rs.next()) {
                    int ingredientId = rs.getInt("ingredient_id");
                    String ingredientName = rs.getString("ingredient_name");
                    int currentStock = rs.getInt("current_stock");
                    double price = rs.getDouble("price");
                    String unit = rs.getString("unit");
                    int minStock = rs.getInt("min_stock");

                    // Create the InventoryItem object
                    InventoryItem item = new InventoryItem(ingredientId, ingredientName, currentStock, price, unit,
                            minStock);
                    ingredients.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the ingredients in ListView
        ingredientListView.setItems(ingredients);

        // Set a custom cell factory to add checkboxes and amount spinners to the
        // ListView
        ingredientListView.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private final Spinner<Integer> amountSpinner = new Spinner<>(1, 100, 1); // Spinner for amount, range 1-100,
                                                                                     // default 1
            private final HBox hbox = new HBox(10); // HBox to arrange CheckBox and Spinner

            {
                amountSpinner.setPrefWidth(70);
            }

            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setText(
                            item.getIngredientName() + " (" + item.getCurrentStock() + " " + item.getUnit() + ")");
                    checkBox.setSelected(item.isSelected()); // Bind the checkbox to the item’s selected state
                    amountSpinner.getValueFactory().setValue(item.getAmount()); // Bind the spinner to the item’s amount

                    // Update item state when checkbox is clicked
                    checkBox.setOnAction(event -> item.setSelected(checkBox.isSelected()));

                    // Update item state when spinner value changes
                    amountSpinner.valueProperty().addListener((obs, oldValue, newValue) -> item.setAmount(newValue));

                    hbox.getChildren().setAll(checkBox, amountSpinner); // Add CheckBox and Spinner to HBox
                    setGraphic(hbox);
                }
            }
        });
    }
    // Method to handle the Add Inventory Item button
    public void showAddInventoryItemDialog(ActionEvent event) {
        // Create a new Stage for the dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Inventory Item");

        // Create VBox layout for the dialog
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Input fields for the new inventory item
        TextField ingredientNameField = new TextField();
        ingredientNameField.setPromptText("Ingredient Name");

        TextField currentStockField = new TextField();
        currentStockField.setPromptText("Current Stock");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        ComboBox<String> unitComboBox = new ComboBox<>();
        unitComboBox.getItems().addAll("cups", "tsp", "lbs", "g", "kgs", "Oz", "ml", "L", "Pcs");
        unitComboBox.setPromptText("Select Unit");

        TextField minStockField = new TextField();
        minStockField.setPromptText("Minimum Stock");

        // Add button to submit the new inventory item
        Button addButton = new Button("Add Item");
        addButton.setOnAction(e -> {
            // Collect input data
            String ingredientName = ingredientNameField.getText();
            String currentStockText = currentStockField.getText();
            String priceText = priceField.getText();
            String unit = unitComboBox.getValue();
            String minStockText = minStockField.getText();

            // Input validation
            if (ingredientName.isEmpty() || currentStockText.isEmpty() || priceText.isEmpty() || unit == null
                    || minStockText.isEmpty()) {
                // Show error alert if any field is empty
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Input Error");
                errorAlert.setHeaderText("Missing Information");
                errorAlert.setContentText("Please fill out all fields.");
                errorAlert.showAndWait();
                return; // Exit the method early
            }

            // Parse the current stock, price, and min stock, assuming inputs are valid
            try {
                int currentStock = Integer.parseInt(currentStockText);
                double price = Double.parseDouble(priceText);
                int minStock = Integer.parseInt(minStockText);

                // Insert the new item into the database
                conn = Database.connect();
                String insertQuery = "INSERT INTO inventory (ingredient_name, current_stock, price, unit, min_stock) VALUES (?, ?, ?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, ingredientName);
                stmt.setInt(2, currentStock);
                stmt.setDouble(3, price);
                stmt.setString(4, unit);
                stmt.setInt(5, minStock);

                int rowsInserted = stmt.executeUpdate(); // Execute the INSERT
                if (rowsInserted > 0) {
                    System.out.println("A new inventory item was inserted successfully!");

                    // Reload the inventory items to reflect the changes
                    loadInventoryItems();
                }

                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                // Show an error alert if the input is not valid (e.g., non-numeric price or
                // stock)
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Input Error");
                errorAlert.setHeaderText("Invalid Number Format");
                errorAlert.setContentText("Please enter valid numbers for stock, price, and minimum stock.");
                errorAlert.showAndWait();
            }

            // Close the dialog
            dialog.close();
        });

        // Add fields and buttons to the VBox
        vbox.getChildren().addAll(
                new Label("Ingredient Name:"), ingredientNameField,
                new Label("Current Stock:"), currentStockField,
                new Label("Price:"), priceField,
                new Label("Unit:"), unitComboBox,
                new Label("Minimum Stock:"), minStockField,
                addButton);

        // Create the scene and show the dialog
        Scene dialogScene = new Scene(vbox, 350, 370);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // Method to handle the Edit Inventory Item button
    public void showEditInventoryItemDialog() {
        InventoryItem selectedItem = inventoryTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            // Show a pop-up if no item is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Item Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an inventory item to edit.");
            alert.showAndWait();
            return;
        }

        final InventoryItem itemToUpdate = selectedItem;

        // Create a new Stage for the dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Inventory Item");

        // Create VBox layout for the dialog
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Labels and text fields for each property
        Label ingredientNameLabel = new Label("Ingredient Name:");
        TextField ingredientNameField = new TextField(itemToUpdate.getIngredientName());

        Label currentStockLabel = new Label("Current Stock:");
        TextField currentStockField = new TextField(String.valueOf(itemToUpdate.getCurrentStock()));

        Label priceLabel = new Label("Price:");
        TextField priceField = new TextField(String.valueOf(itemToUpdate.getPrice()));

        Label unitLabel = new Label("Unit:");
        ComboBox<String> unitComboBox = new ComboBox<>();
        unitComboBox.getItems().addAll("cups", "tsp", "lbs", "g", "kgs", "Oz", "ml", "L", "Pcs");
        unitComboBox.setValue(itemToUpdate.getUnit());

        Label minStockLabel = new Label("Minimum Stock:");
        TextField minStockField = new TextField(String.valueOf(itemToUpdate.getMinStock()));

        // Submit button
        Button submitButton = new Button("Update Item");

        submitButton.setOnAction(e -> {
            // Collect input data
            String ingredientName = ingredientNameField.getText();
            String currentStockText = currentStockField.getText();
            String priceText = priceField.getText();
            String unit = unitComboBox.getValue();
            String minStockText = minStockField.getText();

            // Input validation
            if (ingredientName.isEmpty() || currentStockText.isEmpty() || priceText.isEmpty() || unit == null
                    || minStockText.isEmpty()) {
                // Show error alert if any field is empty
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Input Error");
                errorAlert.setHeaderText("Missing Information");
                errorAlert.setContentText("Please fill out all fields.");
                errorAlert.showAndWait();
                return; // Exit the method early
            }

            // Parse the current stock, price, and min stock, assuming inputs are valid
            try {
                int currentStock = Integer.parseInt(currentStockText);
                double price = Double.parseDouble(priceText);
                int minStock = Integer.parseInt(minStockText);

                // Open a new connection before updating the item
                conn = Database.connect();
                System.out.println("Database connection opened for updating");

                String updateQuery = "UPDATE inventory SET ingredient_name = ?, current_stock = ?, price = ?, unit = ?, min_stock = ? WHERE ingredient_id = ?";
                PreparedStatement stmt = conn.prepareStatement(updateQuery);
                stmt.setString(1, ingredientName);
                stmt.setInt(2, currentStock);
                stmt.setDouble(3, price);
                stmt.setString(4, unit);
                stmt.setInt(5, minStock);
                stmt.setInt(6, itemToUpdate.getIngredientID());

                stmt.executeUpdate();
                loadInventoryItems(); // Refresh the table
                dialog.close(); // Close the dialog

                conn.close(); // Ensure the connection is closed after use
                System.out.println("Database connection closed after updating");
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                // Show an error alert if the input is not valid (e.g., non-numeric price or
                // stock)
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Input Error");
                errorAlert.setHeaderText("Invalid Number Format");
                errorAlert.setContentText("Please enter valid numbers for stock, price, and minimum stock.");
                errorAlert.showAndWait();
            }
        });

        // Add fields and button to the VBox
        vbox.getChildren().addAll(
                ingredientNameLabel, ingredientNameField,
                currentStockLabel, currentStockField,
                priceLabel, priceField,
                unitLabel, unitComboBox,
                minStockLabel, minStockField,
                submitButton);

        vbox.setPadding(new Insets(20));
        Scene dialogScene = new Scene(vbox, 400, 380);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // Method to handle the Delete Inventory Item button
    public void showDeleteInventoryItemDialog(ActionEvent event) {
        // Get the selected inventory item
        InventoryItem selectedItem = inventoryTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            // Show confirmation dialog
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Delete Confirmation");
            confirmationAlert.setHeaderText("Are you sure you want to delete this item?");
            confirmationAlert.setContentText("Ingredient Name: " + selectedItem.getIngredientName());

            // Wait for user confirmation
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // If user confirms, proceed with the deletion
                deleteItemFromInventoryDatabase(selectedItem);
                // Remove the item from the table view
                inventoryTable.getItems().remove(selectedItem);
            }
        } else {
            // If no item is selected, show an error alert
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle("No Selection");
            errorAlert.setHeaderText("No item selected");
            errorAlert.setContentText("Please select an inventory item to delete.");
            errorAlert.showAndWait();
        }
    }

    // Method to delete the selected item from the database
    private void deleteItemFromInventoryDatabase(InventoryItem item) {
        String deleteQuery = "DELETE FROM inventory WHERE ingredient_id = ?";

        try {
            conn = Database.connect(); // Open a new connection
            PreparedStatement stmt = conn.prepareStatement(deleteQuery);
            stmt.setInt(1, item.getIngredientID());
            stmt.executeUpdate();
            conn.close(); // Close the connection
            System.out.println("Inventory item deleted from database: " + item.getIngredientName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
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
