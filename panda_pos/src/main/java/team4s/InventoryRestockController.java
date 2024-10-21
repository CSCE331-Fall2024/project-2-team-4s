package team4s;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
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

public class InventoryRestockController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;
    private double orderTotal = 0.0;

    @FXML
    private TableView<InventoryItem> inventoryTable;

    @FXML
    private TableView<InventoryItem> recommendedRestockTable;

    @FXML
    private ComboBox<String> ingredientComboBox;

    @FXML
    private TextField quantityTextField;

    @FXML
    private TableView<RestockInventoryItem> restockOrderTable;

    @FXML
    private Label errorLabel;

    @FXML
    private Label orderTotalLabel;

    /**
     * Initializes the inventory and restock tables, the ingredient combo box, and
     * the restock order table.
     */
    public void initialize() {
        try {
            conn = Database.connect();

            // Load the menu items into the TableView
            loadInventoryItems();

            // Load the recommended restock items into the TableView
            loadRestockRecommendations();

            // Load the ingredientComboBox
            loadIngredientComboBox();

            conn.close();
        } catch (SQLException e) {
            System.err.println("Database connection error");
        }
    }

    /**
     * Queries the database for inventory items and loads them into the TableView.
     */
    private void loadInventoryItems() {
        ObservableList<InventoryItem> inventoryItems = FXCollections.observableArrayList();

        String query = "SELECT * FROM inventory ORDER BY ingredient_id";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

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

    /**
     * Shows the modal to add a new inventory item and handles the insertion into
     * the database.
     */
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

    /**
     * Shows the modal to edit an inventory item and handles the update in the
     * database.
     */
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

    /**
     * Shows the confirmation alert to delete an inventory item and handles the
     * deletion from the database.
     * 
     * @param event the action event from the button click
     */
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
                String deleteQuery = "DELETE FROM inventory WHERE ingredient_id = ?";

                // If user confirms, proceed with the deletion
                try {
                    conn = Database.connect(); // Open a new connection
                    PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                    stmt.setInt(1, selectedItem.getIngredientID());
                    stmt.executeUpdate();
                    conn.close(); // Close the connection
                    System.out.println("Inventory item deleted from database: " + selectedItem.getIngredientName());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

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

    /**
     * Queries the database for inventory items that need restocking and loads them
     * into the TableView.
     */
    private void loadRestockRecommendations() {
        ObservableList<InventoryItem> recommendedRestockItems = FXCollections.observableArrayList();

        String query = "SELECT * FROM inventory WHERE current_stock < min_stock ORDER BY ingredient_id";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                recommendedRestockItems.add(new InventoryItem(
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
        recommendedRestockTable.setItems(recommendedRestockItems);
    }

    /**
     * Queries the database for ingredients and loads them into the ComboBox.
     */
    public void loadIngredientComboBox() {
        ObservableList<String> ingredients = FXCollections.observableArrayList();

        String fetchIngredientsQuery = "SELECT ingredient_id, ingredient_name, current_stock, price, unit, min_stock FROM inventory ORDER BY ingredient_name";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(fetchIngredientsQuery);

            while (rs.next()) {
                String ingredientName = rs.getString("ingredient_name");

                ingredients.add(ingredientName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the ingredients in the ComboBox
        ingredientComboBox.setItems(ingredients);
    }

    // Add to restock order
    public void addToRestockOrder(ActionEvent event) {
        String ingredientName = ingredientComboBox.getValue();
        String quantityText = quantityTextField.getText();

        // Error handling
        if (ingredientName == null || ingredientName.isEmpty()) {
            errorLabel.setText("Please select an ingredient.");
            return;
        }

        if (quantityText == null || quantityText.isEmpty()) {
            errorLabel.setText("Please enter a quantity.");
            return;
        }

        int quantity = Integer.parseInt(quantityText);
        String fetchIngredientQuery = "SELECT * FROM inventory WHERE ingredient_name = ?";

        try {
            conn = Database.connect();

            PreparedStatement stmt = conn.prepareStatement(fetchIngredientQuery);
            stmt.setString(1, ingredientName);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int ingredientID = rs.getInt("ingredient_id");
                double price = rs.getDouble("price");
                String unit = rs.getString("unit");
                double totalPrice = price * quantity;

                // Create a new RestockInventoryItem object
                RestockInventoryItem item = new RestockInventoryItem(ingredientID, ingredientName, quantity, price,
                        unit, totalPrice);

                // Add the item to the restock order table
                restockOrderTable.getItems().add(item);

                // Update the order total
                orderTotal += totalPrice;
                orderTotalLabel.setText(String.format("Order Total: $%.2f", orderTotal));

                // Clear combo box
                ingredientComboBox.setValue(null);

                // Clear the quantity text field
                quantityTextField.clear();
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes the selected item from the restock order.
     * 
     * @param event the action event from the button click
     */
    public void removeFromRestockOrder(ActionEvent event) {
        RestockInventoryItem selectedItem = restockOrderTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            // Remove the item from the table
            restockOrderTable.getItems().remove(selectedItem);

            // Update the order total
            orderTotal -= selectedItem.getTotalPrice();
            orderTotalLabel.setText(String.format("Order Total: $%.2f", orderTotal));
        } else {
            // Show alert if no item is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Item Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an ingredient to remove from the restock order");
            alert.showAndWait();
            return;
        }
    }

    /**
     * Clears the restock order table and resets the order total.
     * 
     * @param event the action event from the button click
     */
    public void clearRestockOrder(ActionEvent event) {
        restockOrderTable.getItems().clear();
        orderTotal = 0.0;
        orderTotalLabel.setText("Order Total: $0.00");
    }

    /**
     * Submits the restock order and updates the inventory in the database.
     * 
     * @param event the action event from the button click
     */
    public void submitRestockOrder(ActionEvent event) {
        if (restockOrderTable.getItems().isEmpty()) {
            // Show an error alert if the order is empty
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Empty Order");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Please add items to the restock order before submitting");
            errorAlert.showAndWait();
            return;
        }

        // Show a confirmation dialog
        Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Restock Order");
        confirmationAlert.setHeaderText("Are you sure you want to submit this order?");
        confirmationAlert.setContentText("Order Total: $" + orderTotal);

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // On confirmation, update the inventory and clear the order
            try {
                conn = Database.connect();

                // Put items in a separate list to avoid exception
                List<RestockInventoryItem> itemsToUpdate = new ArrayList<>(restockOrderTable.getItems());

                for (RestockInventoryItem item : itemsToUpdate) {
                    int ingredientID = item.getIngredientID();
                    int quantity = item.getQuantity();

                    // Update the inventory
                    String updateInventoryQuery = "UPDATE inventory SET current_stock = current_stock + ? WHERE ingredient_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(updateInventoryQuery);
                    stmt.setInt(1, quantity);
                    stmt.setInt(2, ingredientID);
                    stmt.executeUpdate();

                    System.out.println("Inventory updated for ingredient ID " + ingredientID);
                }

                // Clear the order
                restockOrderTable.getItems().clear();

                // Update the inventory table
                loadInventoryItems();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Switches to the manager menu
     * 
     * @param event the action event from the button click
     */
    public void switchToManager(ActionEvent event) {
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
