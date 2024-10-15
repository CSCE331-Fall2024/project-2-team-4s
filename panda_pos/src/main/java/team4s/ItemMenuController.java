package team4s;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

public class ItemMenuController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;


    @FXML
    private TableView<MenuItem> menuTable;
    public void initialize() {
        try {
            conn = Database.connect();
            //System.out.println("Database connection opened");

            // Load the menu items into the TableView
            loadMenuItems();

            conn.close();
            //System.out.println("Database connection closed");
        } catch (SQLException e) {
            //System.err.println("Database connection error");
        }
    }
    // load menu items into the TableView
    private void loadMenuItems() {
        ObservableList<MenuItem> menu_items = FXCollections.observableArrayList();

        String query = "SELECT mi.menu_item_id, mi.current_servings, mi.item_name, mi.item_price, mi.item_category, "
                + "COALESCE(STRING_AGG(i.ingredient_name || ' (' || im.ingredient_amount || ')', ', '), '') AS ingredients "
                + "FROM menu_item mi "
                + "LEFT JOIN inventory_menu_item im ON mi.menu_item_id = im.menu_item_id "
                + "LEFT JOIN inventory i ON im.ingredient_id = i.ingredient_id "
                + "GROUP BY mi.menu_item_id "
                + "ORDER BY mi.menu_item_id";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                menu_items.add(new MenuItem(
                        rs.getInt("menu_item_id"),
                        rs.getInt("current_servings"),
                        rs.getString("item_name"),
                        rs.getFloat("item_price"),
                        rs.getString("item_category"),
                        rs.getString("ingredients"))); // Added ingredients column
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the items in the TableView
        menuTable.setItems(menu_items);
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
    // Method to handle the Add Ingredients button
    public void showAddItemDialog(ActionEvent event) {
        // Create a new Stage for the dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Menu Item");

        // Create VBox layout for the dialog
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Input fields for the new menu item
        TextField itemNameField = new TextField();
        itemNameField.setPromptText("Item Name");

        TextField currentServingsField = new TextField();
        currentServingsField.setPromptText("Current Servings");

        TextField itemPriceField = new TextField();
        itemPriceField.setPromptText("Item Price");

        ComboBox<String> itemCategoryBox = new ComboBox<>();
        itemCategoryBox.getItems().addAll("Drink", "Meal", "Appetizer", "Side", "Entree");
        itemCategoryBox.setPromptText("Select Category");

        // Create ListView for InventoryItems
        ListView<InventoryItem> ingredientListView = new ListView<>();
        ingredientListView.setPrefHeight(150); // Set a preferred height

        // Load ingredients into the ListView with checkboxes and spinners for amounts
        loadIngredients(ingredientListView);

        // Add button to submit the new menu item
        Button addButton = new Button("Add Item");
        addButton.setOnAction(e -> {
            // Collect input data
            String itemName = itemNameField.getText();
            String itemPriceText = itemPriceField.getText();
            String itemCategory = itemCategoryBox.getValue();
            String currentServingsText = currentServingsField.getText();

            // Input validation
            if (itemName.isEmpty() || itemPriceText.isEmpty() || itemCategory == null
                    || currentServingsText.isEmpty()) {
                // Show error alert if any field is empty
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Input Error");
                errorAlert.setHeaderText("Missing Information");
                errorAlert.setContentText("Please fill out all fields.");
                errorAlert.showAndWait();
                return; // Exit the method early
            }

            // Parse the price and current servings, assuming inputs are valid
            try {
                float itemPrice = Float.parseFloat(itemPriceText);
                int currentServings = Integer.parseInt(currentServingsText);

                // Insert the new item into the database
                conn = Database.connect();
                String insertMenuItemQuery = "INSERT INTO menu_item (current_servings, item_name, item_price, item_category) VALUES (?, ?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(insertMenuItemQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, currentServings);
                stmt.setString(2, itemName);
                stmt.setFloat(3, itemPrice);
                stmt.setString(4, itemCategory);

                int rowsInserted = stmt.executeUpdate(); // Execute the INSERT
                if (rowsInserted > 0) {
                    System.out.println("A new menu item was inserted successfully!");

                    // Get the generated menu item ID
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    int newMenuItemId = 0;
                    if (generatedKeys.next()) {
                        newMenuItemId = generatedKeys.getInt(1);
                    }

                    // Insert selected ingredients with amounts into inventory_menu_item table
                    for (InventoryItem ingredient : ingredientListView.getItems()) {
                        if (ingredient.isSelected()) {
                            String insertIngredientQuery = "INSERT INTO inventory_menu_item (menu_item_id, ingredient_id, ingredient_amount) VALUES (?, ?, ?)";

                            PreparedStatement ingredientStmt = conn.prepareStatement(insertIngredientQuery);
                            ingredientStmt.setInt(1, newMenuItemId);
                            ingredientStmt.setInt(2, ingredient.getIngredientID());
                            ingredientStmt.setInt(3, ingredient.getAmount()); // Use the selected amount

                            ingredientStmt.executeUpdate();
                        }
                    }

                    // Reload the menu items into the table after the insert
                    loadMenuItems(); // Refresh the table view
                }

                conn.close(); // Close the connection only after all operations are complete
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                // Show an error alert if the input is not valid (e.g., non-numeric price or
                // servings)
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Input Error");
                errorAlert.setHeaderText("Invalid Number Format");
                errorAlert.setContentText("Please enter valid numbers for price and servings.");
                errorAlert.showAndWait();
            }

            // Close the dialog
            dialog.close();
        });

        // Add fields and the ListView of ingredients to the VBox
        vbox.getChildren().addAll(
                new Label("Item Name:"), itemNameField,
                new Label("Item Price:"), itemPriceField,
                new Label("Item Category:"), itemCategoryBox,
                new Label("Current Servings:"), currentServingsField,
                new Label("Select Ingredients:"), ingredientListView,
                addButton);

        // Create the scene and show the dialog
        Scene dialogScene = new Scene(vbox, 350, 500);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // Method to handle the Edit Item button
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

        VBox vbox = new VBox(10); // spacing between elements
        vbox.setPadding(new Insets(10));

        // Labels and text fields for each property
        Label itemNameLabel = new Label("Item Name:");
        TextField itemNameField = new TextField(itemToUpdate.getItemName());

        Label itemPriceLabel = new Label("Item Price:");
        TextField itemPriceField = new TextField(String.valueOf(itemToUpdate.getItemPrice()));

        Label currentServingsLabel = new Label("Current Servings:");
        TextField currentServingsField = new TextField(String.valueOf(itemToUpdate.getCurrentServings()));

        Label itemCategoryLabel = new Label("Item Category:");
        ComboBox<String> itemCategoryBox = new ComboBox<>();
        itemCategoryBox.getItems().addAll("Drink", "Meal", "Appetizer", "Side", "Entree");
        itemCategoryBox.setValue(itemToUpdate.getItemCategory());

        // Create ListView for InventoryItems (to include current and potential new
        // ingredients)
        ListView<InventoryItem> ingredientListView = new ListView<>();
        ingredientListView.setPrefHeight(150);

        // Load all available ingredients, marking the ones that are already selected
        loadAllIngredientsWithCurrentSelection(itemToUpdate.getMenuItemID(), ingredientListView);

        // Submit button to update the item
        Button submitButton = new Button("Update Item");

        submitButton.setOnAction(e -> {
            // Collect input data
            String itemName = itemNameField.getText();
            String itemPriceText = itemPriceField.getText();
            String itemCategory = itemCategoryBox.getValue();
            String currentServingsText = currentServingsField.getText();

            // Input validation
            if (itemName.isEmpty() || itemPriceText.isEmpty() || itemCategory == null
                    || currentServingsText.isEmpty()) {
                // Show error alert if any field is empty
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Input Error");
                errorAlert.setHeaderText("Missing Information");
                errorAlert.setContentText("Please fill out all fields.");
                errorAlert.showAndWait();
                return; // Exit the method early
            }

            // Parse the price and current servings, assuming inputs are valid
            try {
                float itemPrice = Float.parseFloat(itemPriceText);
                int currentServings = Integer.parseInt(currentServingsText);

                // Open a new connection before updating the item
                conn = Database.connect();

                // Update the menu item details
                String updateQuery = "UPDATE menu_item SET current_servings = ?, item_name = ?, item_price = ?, item_category = ? WHERE menu_item_id = ?";
                PreparedStatement stmt = conn.prepareStatement(updateQuery);
                stmt.setInt(1, currentServings);
                stmt.setString(2, itemName);
                stmt.setFloat(3, itemPrice);
                stmt.setString(4, itemCategory);
                stmt.setInt(5, itemToUpdate.getMenuItemID());

                stmt.executeUpdate();

                // Delete existing ingredient records for this menu item
                String deleteIngredientsQuery = "DELETE FROM inventory_menu_item WHERE menu_item_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteIngredientsQuery);
                deleteStmt.setInt(1, itemToUpdate.getMenuItemID());
                deleteStmt.executeUpdate();

                // Insert the selected ingredients with amounts
                for (InventoryItem ingredient : ingredientListView.getItems()) {
                    if (ingredient.isSelected()) {
                        String insertIngredientQuery = "INSERT INTO inventory_menu_item (menu_item_id, ingredient_id, ingredient_amount) VALUES (?, ?, ?)";
                        PreparedStatement ingredientStmt = conn.prepareStatement(insertIngredientQuery);
                        ingredientStmt.setInt(1, itemToUpdate.getMenuItemID());
                        ingredientStmt.setInt(2, ingredient.getIngredientID());
                        ingredientStmt.setInt(3, ingredient.getAmount()); // Use the selected amount

                        ingredientStmt.executeUpdate();
                    }
                }

                // Reload the menu items to reflect the changes
                loadMenuItems();
                dialog.close(); // Close the dialog

                conn.close(); // Ensure the connection is closed after use
                System.out.println("Database connection closed after updating");
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                // Show an error alert if the input is not valid (e.g., non-numeric price or
                // servings)
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Input Error");
                errorAlert.setHeaderText("Invalid Number Format");
                errorAlert.setContentText("Please enter valid numbers for price and servings.");
                errorAlert.showAndWait();
            }
        });

        // Add fields and button to the VBox
        vbox.getChildren().addAll(
                itemNameLabel, itemNameField,
                itemPriceLabel, itemPriceField,
                currentServingsLabel, currentServingsField,
                itemCategoryLabel, itemCategoryBox,
                new Label("Edit Ingredients (Add/Remove):"), ingredientListView,
                submitButton);

        Scene dialogScene = new Scene(vbox, 400, 500);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // Load all ingredients, marking those that are currently selected for the given
    // menu item
    private void loadAllIngredientsWithCurrentSelection(int menuItemId, ListView<InventoryItem> ingredientListView) {
        ObservableList<InventoryItem> ingredients = FXCollections.observableArrayList();

        try {
            conn = Database.connect();
            String fetchIngredientsQuery = "SELECT i.ingredient_id, i.ingredient_name, i.current_stock, i.price, i.unit, i.min_stock, "
                    + "COALESCE(im.ingredient_amount, 1) AS ingredient_amount, " // Default amount set to 1 if no amount
                                                                                 // exists
                    + "CASE WHEN im.menu_item_id IS NOT NULL THEN TRUE ELSE FALSE END AS selected "
                    + "FROM inventory i "
                    + "LEFT JOIN inventory_menu_item im ON i.ingredient_id = im.ingredient_id AND im.menu_item_id = ? "
                    + "ORDER BY i.ingredient_name";
            PreparedStatement stmt = conn.prepareStatement(fetchIngredientsQuery);
            stmt.setInt(1, menuItemId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int ingredientId = rs.getInt("ingredient_id");
                    String ingredientName = rs.getString("ingredient_name");
                    int currentStock = rs.getInt("current_stock");
                    double price = rs.getDouble("price");
                    String unit = rs.getString("unit");
                    int minStock = rs.getInt("min_stock");
                    int ingredientAmount = rs.getInt("ingredient_amount"); // Get the actual amount
                    boolean selected = rs.getBoolean("selected");

                    // Create the InventoryItem object
                    InventoryItem item = new InventoryItem(ingredientId, ingredientName, currentStock, price, unit,
                            minStock);
                    item.setAmount(ingredientAmount); // Set the actual amount to the item
                    item.setSelected(selected); // Mark as selected if it's currently used by the menu item
                    ingredients.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the ingredients in ListView with checkboxes and spinners for amounts
        ingredientListView.setItems(ingredients);
        ingredientListView.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private final Spinner<Integer> amountSpinner = new Spinner<>(1, 100, 1); // Default range 1-100
            private final HBox hbox = new HBox(10); // Layout

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
                    checkBox.setSelected(item.isSelected());

                    // Initialize the spinner with the current ingredient amount
                    amountSpinner.getValueFactory().setValue(item.getAmount());

                    // Update item state when checkbox is clicked
                    checkBox.setOnAction(event -> item.setSelected(checkBox.isSelected()));

                    // Update item state when spinner value changes
                    amountSpinner.valueProperty().addListener((obs, oldValue, newValue) -> item.setAmount(newValue));

                    hbox.getChildren().setAll(checkBox, amountSpinner);
                    setGraphic(hbox);
                }
            }
        });
    }

    // Method to handle the Delete Item button
    public void handleDeleteItem(ActionEvent event) {
        // Get the selected item
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            // Show confirmation dialog
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Delete Confirmation");
            confirmationAlert.setHeaderText("Are you sure you want to delete this item?");
            confirmationAlert.setContentText("Item Name: " + selectedItem.getItemName());

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

    // Method to delete the selected item from the database, including its
    // ingredients
    private void deleteItemFromDatabase(MenuItem item) {
        String deleteIngredientsQuery = "DELETE FROM inventory_menu_item WHERE menu_item_id = ?";
        String deleteMenuItemQuery = "DELETE FROM menu_item WHERE menu_item_id = ?";

        try {
            conn = Database.connect();

            // Start a transaction
            conn.setAutoCommit(false);

            // Delete ingredients associated with the menu item
            try (PreparedStatement stmt = conn.prepareStatement(deleteIngredientsQuery)) {
                stmt.setInt(1, item.getMenuItemID());
                stmt.executeUpdate();
            }

            // Delete the menu item itself
            try (PreparedStatement stmt = conn.prepareStatement(deleteMenuItemQuery)) {
                stmt.setInt(1, item.getMenuItemID());
                stmt.executeUpdate();
            }

            // Commit the transaction
            conn.commit();
            System.out
                    .println("Menu item and associated ingredients deleted from the database: " + item.getItemName());
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction if an error occurs
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
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

