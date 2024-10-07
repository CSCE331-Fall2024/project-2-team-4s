package team4s;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.sql.PreparedStatement;

public class SceneController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;

    // FXML injected UI element for the menu table (used only in Manager.fxml)
    @FXML
    private TableView<MenuItem> menuTable;

    // FXML injected UI elements for the inventory table (used only in Manager.fxml)
    @FXML
    private TableView<InventoryItem> inventoryTable; 

    // Method to initialize only when Manager.fxml is loaded
    public void initializeManager() {
        try {
            conn = Database.connect();
            System.out.println("Database connection opened");

            // Load the menu items into the TableView
            loadMenuItems();
            loadInventoryItems();

            conn.close();
            System.out.println("Database connection closed");
        } catch (SQLException e) {
            System.err.println("Database connection error");
        }
    }

    // Method to load menu items into the TableView
    private void loadMenuItems() {
        ObservableList<MenuItem> menu_items = FXCollections.observableArrayList();

        String query = "SELECT * FROM menu_item ORDER BY menu_item_id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                menu_items.add(new MenuItem(
                    rs.getInt("menu_item_id"),
                    rs.getInt("current_servings"),
                    rs.getString("item_name"),
                    rs.getFloat("item_price"),
                    rs.getString("item_category")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the items in the TableView
        menuTable.setItems(menu_items);
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
                rs.getInt("min_stock")
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    // Set the items in the TableView
    inventoryTable.setItems(inventoryItems);
}

    public void showAddItemDialog(ActionEvent event) {
        // Create a new Stage for the dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Menu Item");

        // Create VBox layout for the dialog
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Input fields for the new item
        TextField itemNameField = new TextField();
        itemNameField.setPromptText("Item Name");

        TextField currentServingsField = new TextField();
        currentServingsField.setPromptText("Current Servings");

        TextField itemPriceField = new TextField();
        itemPriceField.setPromptText("Item Price");

        ComboBox<String> itemCategoryBox = new ComboBox<>();
        itemCategoryBox.getItems().addAll("Drink", "Meal", "Appetizer", "Side", "Entree");
        itemCategoryBox.setPromptText("Select Category");

        // Add button to submit the new item    
        Button addButton = new Button("Add Item");
        addButton.setOnAction(e -> {
            // Collect input data
            String itemName = itemNameField.getText();
            float itemPrice = Float.parseFloat(itemPriceField.getText());
            String itemCategory = itemCategoryBox.getValue();
            int currentServings = Integer.parseInt(currentServingsField.getText());

            // Insert the new item into the database
            try {
                conn = Database.connect();
                String insertQuery = "INSERT INTO menu_item (current_servings, item_name, item_price, item_category) VALUES (?, ?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                stmt.setInt(1, currentServings);
                stmt.setString(2, itemName);
                stmt.setFloat(3, itemPrice);
                stmt.setString(4, itemCategory);

                int rowsInserted = stmt.executeUpdate(); // Execute the INSERT
                if (rowsInserted > 0) {
                    System.out.println("A new menu item was inserted successfully!");

                    // Get the generated item ID (optional, depends on your DB setup)
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    int newItemId = 0;
                    if (generatedKeys.next()) {
                        newItemId = generatedKeys.getInt(1);
                    }

                    // Create a new MenuItem object with the generated ID and the entered details
                    MenuItem newItem = new MenuItem(newItemId, currentServings, itemName, itemPrice, itemCategory);

                    // Add the new item to the TableView
                    menuTable.getItems().add(newItem);

                }

                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // Close the dialog
            dialog.close();
        });

        // Add Cancel button to close the dialog without saving

        // Add fields and button to the VBox
        vbox.getChildren().addAll(
                new Label("Item Name:"), itemNameField,
                new Label("Item Price:"), itemPriceField,
                new Label("Item Category:"), itemCategoryBox,
                new Label("Current Servings:"), currentServingsField,
                addButton
        );

        // Create the scene and show the dialog
        Scene dialogScene = new Scene(vbox, 350, 310);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    
    public void showEditItemDialog() {
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
    
        if (selectedItem == null) {
            // Show a pop-up if no item is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Item Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an item to edit.");
            alert.showAndWait();
            return;
        }
    
        final MenuItem itemToUpdate = selectedItem;
    
        Stage dialog = new Stage();
        dialog.setTitle("Edit Menu Item");
    
        VBox vbox = new VBox(10);  // spacing between elements
    
        // Labels and text fields for each property
        Label itemNameLabel = new Label("Item Name:");
        TextField itemNameField = new TextField(itemToUpdate.getItem_name());
    
        Label itemPriceLabel = new Label("Item Price:");
        TextField itemPriceField = new TextField(String.valueOf(itemToUpdate.getItem_price()));
    
        Label currentServingsLabel = new Label("Current Servings:");
        TextField currentServingsField = new TextField(String.valueOf(itemToUpdate.getCurrent_servings()));
    
        Label itemCategoryLabel = new Label("Item Category:");
        ComboBox<String> itemCategoryBox = new ComboBox<>();
        itemCategoryBox.getItems().addAll("Drink", "Meal", "Appetizer", "Side", "Entree");
        itemCategoryBox.setValue(itemToUpdate.getItem_category());
    
        // Submit button
        Button submitButton = new Button("Update Item");
    
        submitButton.setOnAction(e -> {
            try {
                String itemName = itemNameField.getText();
                float itemPrice = Float.parseFloat(itemPriceField.getText());
                int currentServings = Integer.parseInt(currentServingsField.getText());
                String itemCategory = itemCategoryBox.getValue();
    
                // Open a new connection before updating the item
                conn = Database.connect();
                System.out.println("Database connection opened for updating");
    
                String updateQuery = "UPDATE menu_item SET current_servings = ?, item_name = ?, item_price = ?, item_category = ? WHERE menu_item_id = ?";
                PreparedStatement stmt = conn.prepareStatement(updateQuery);
                stmt.setInt(1, currentServings);
                stmt.setString(2, itemName);
                stmt.setFloat(3, itemPrice);
                stmt.setString(4, itemCategory);
                stmt.setInt(5, itemToUpdate.getMenu_item_id());
    
                stmt.executeUpdate();
    
                loadMenuItems();  // Refresh the table
                dialog.close();   // Close the dialog
    
                conn.close();  // Ensure the connection is closed after use
                System.out.println("Database connection closed after updating");
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                System.out.println("Please enter valid numbers for price and servings.");
            }
        });
    
        vbox.getChildren().addAll(
            itemNameLabel, itemNameField,
            itemPriceLabel, itemPriceField,
            currentServingsLabel, currentServingsField,
            itemCategoryLabel, itemCategoryBox,
            submitButton
        );
    
        vbox.setPadding(new Insets(20));
        Scene dialogScene = new Scene(vbox, 400, 320);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    
    public void handleDeleteItem(ActionEvent event) {
        // Get the selected item
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            // Show confirmation dialog
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Delete Confirmation");
            confirmationAlert.setHeaderText("Are you sure you want to delete this item?");
            confirmationAlert.setContentText("Item Name: " + selectedItem.getItem_name());

            // Wait for user confirmation
            if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
                // If user confirms, proceed with the deletion
                deleteItemFromDatabase(selectedItem);
                // Remove the item from the table view
                menuTable.getItems().remove(selectedItem);
            }
        } else {
            // If no item is selected, show an error alert
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle("No Selection");
            errorAlert.setHeaderText("No item selected");
            errorAlert.setContentText("Please select an item to delete.");
            errorAlert.showAndWait();
        }
    }

    // Method to delete the selected item from the database
    private void deleteItemFromDatabase(MenuItem item) {
        String deleteQuery = "DELETE FROM menu_item WHERE menu_item_id = ?";

        try {
            conn = Database.connect();
            PreparedStatement stmt = conn.prepareStatement(deleteQuery);
            stmt.setInt(1, item.getMenu_item_id());
            stmt.executeUpdate();
            conn.close();
            System.out.println("Item deleted from database: " + item.getItem_name());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
            int currentStock = Integer.parseInt(currentStockField.getText());
            double price = Double.parseDouble(priceField.getText());
            String unit = unitComboBox.getValue();
            int minStock = Integer.parseInt(minStockField.getText());
    
            // Insert the new item into the database
            try {
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
                addButton
        );
    
        // Create the scene and show the dialog
        Scene dialogScene = new Scene(vbox, 350, 370);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

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
            try {
                String ingredientName = ingredientNameField.getText();
                int currentStock = Integer.parseInt(currentStockField.getText());
                double price = Double.parseDouble(priceField.getText());
                String unit = unitComboBox.getValue();
                int minStock = Integer.parseInt(minStockField.getText());
    
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
                stmt.setInt(6, itemToUpdate.getIngredientId());
    
                stmt.executeUpdate();
                loadInventoryItems();  // Refresh the table
                dialog.close();   // Close the dialog
    
                conn.close();  // Ensure the connection is closed after use
                System.out.println("Database connection closed after updating");
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                System.out.println("Please enter valid numbers for price and stock.");
            }
        });
    
        // Add fields and button to the VBox
        vbox.getChildren().addAll(
                ingredientNameLabel, ingredientNameField,
                currentStockLabel, currentStockField,
                priceLabel, priceField,
                unitLabel, unitComboBox,
                minStockLabel, minStockField,
                submitButton
        );
    
        vbox.setPadding(new Insets(20));
        Scene dialogScene = new Scene(vbox, 400, 380);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

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
            stmt.setInt(1, item.getIngredientId());
            stmt.executeUpdate();
            conn.close(); // Close the connection
            System.out.println("Inventory item deleted from database: " + item.getIngredientName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    // Switch to specified scene
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

    // Switch to manager GUI (Manager.fxml)
    public void switchToManager(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Manager.fxml"));
            root = loader.load();

            // Get the controller for Manager.fxml and initialize the table view
            SceneController controller = loader.getController();
            controller.initializeManager();  // Initialize the Manager-specific logic

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Switch to cashier GUI
    public void switchToCashier(ActionEvent event) {
        switchToScene(event, "/fxml/Cashier.fxml");
    }

    // Switch to menu GUI
    public void switchToMenu(ActionEvent event) {
        switchToScene(event, "/fxml/Menu.fxml");
    }
}
