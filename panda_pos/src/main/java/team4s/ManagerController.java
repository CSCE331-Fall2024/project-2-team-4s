package team4s;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
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

public class ManagerController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;

    @FXML
    private VBox chartArea;

    @FXML
    private TableView<MenuItem> menuTable;

    @FXML
    private TableView<InventoryItem> inventoryTable;

    @FXML
    private TableView<Employee> employeeTable;

    // ------------------------------- Initializers -------------------------------
    // Method to initialize only when Manager.fxml is loaded
    public void initializeManager() {
        try {
            conn = Database.connect();
            System.out.println("Database connection opened");

            // Load the menu items into the TableView
            loadMenuItems();
            loadInventoryItems();

            // load the employees
            loadEmployees();

            conn.close();
            System.out.println("Database connection closed");
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

    // --------------------------- Menu Item Management ---------------------------
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

    // --------------------------- Inventory Management ---------------------------
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

    // ---------------------------- Employee Management ----------------------------
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

    // ---------------------------------- REPORTS ----------------------------------
    // Show the customizer for generating charts
    @FXML
    public void showReportCustomizer() {
        // Create a new dialog
        Stage customizerStage = new Stage();
        customizerStage.setTitle("Graph Customizer");

        // Create layout elements
        VBox layout = new VBox(10);
        Label label = new Label("Customize your graph settings");

        // Dropdown to select graph type
        ComboBox<String> graphTypeComboBox = new ComboBox<>();
        graphTypeComboBox.getItems().addAll("Pie Chart", "Line Graph", "Bar Chart");

        // Button to confirm settings
        Button confirmButton = new Button("Confirm");

        // Set default graph type
        graphTypeComboBox.setValue("Pie Chart");

        layout.getChildren().addAll(label, graphTypeComboBox, confirmButton);

        Scene scene = new Scene(layout, 300, 200);
        customizerStage.setScene(scene);
        customizerStage.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
        customizerStage.show();

        // Handle confirm button action
        confirmButton.setOnAction(e -> {
            String selectedGraphType = graphTypeComboBox.getValue();
            customizerStage.close();
            handleReportTypeChange(selectedGraphType);
        });
    }

    // Handle report type change and generate appropriate chart
    private void handleReportTypeChange(String selectedType) {
        chartArea.getChildren().clear(); // Clear the chart area before adding a new chart

        switch (selectedType) {
            case "Pie Chart":
                loadForPieChart(); // Load PieChart data
                break;
            case "Line Graph":
                loadForLineChart(); // Load LineChart data
                break;
            case "Bar Chart":
                loadForBarChart(); // Load BarChart data
                break;
        }
    }

    // Load PieChart data (popularity of entrees)
    private void loadForPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // Query to count number of transactions by payment type (can be adapted to
        // entrees)
        String query = "SELECT transaction.transaction_type, COUNT(transaction.transaction_id) AS transaction_count, SUM(transaction.total_cost) AS total_revenue "
                +
                "FROM transaction " +
                "GROUP BY transaction.transaction_type " +
                "ORDER BY transaction_count DESC";

        try (Connection conn = Database.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String paymentType = rs.getString("transaction_type");
                int count = rs.getInt("transaction_count");

                // Add data for PieChart: payment type and count
                pieChartData.add(new PieChart.Data(paymentType, count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create and display the PieChart
        PieChart pieChart = new PieChart(pieChartData);
        chartArea.getChildren().clear(); // Clear the chart area before adding the new chart
        chartArea.getChildren().add(pieChart);
    }

    // Load LineChart data (total sales per week)
    private void loadForLineChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Query for total sales per week
        String query = "SELECT week_number, SUM(total_cost) AS total_revenue " +
                "FROM transaction " +
                "GROUP BY week_number " +
                "ORDER BY week_number";

        try (Connection conn = Database.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String week = "Week " + rs.getInt("week_number");
                double totalSales = rs.getDouble("total_revenue");

                // Add data for LineChart: week number and total sales
                series.getData().add(new XYChart.Data<>(week, totalSales));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create and display the LineChart
        LineChart<String, Number> lineChart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        lineChart.getData().add(series);
        lineChart.setLegendVisible(false);
        chartArea.getChildren().clear(); // Clear the chart area before adding the new chart
        chartArea.getChildren().add(lineChart);
    }

    // Load BarChart data (current inventory)
    private void loadForBarChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Query to get inventory items sorted by current stock minus minimum stock
        String query = "SELECT i.ingredient_name, i.current_stock, i.min_stock, i.current_stock - i.min_stock AS difference "
                +
                "FROM inventory AS i " +
                "ORDER BY difference ASC";

        try (Connection conn = Database.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String ingredientName = rs.getString("ingredient_name");
                int currentStock = rs.getInt("current_stock");

                // Add data for BarChart: ingredient name and current stock
                series.getData().add(new XYChart.Data<>(ingredientName, currentStock));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create and display the BarChart
        BarChart<String, Number> barChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        chartArea.getChildren().clear(); // Clear the chart area before adding the new chart
        chartArea.getChildren().add(barChart);
    }

    // Export the current report (for now, just a placeholder)
    public void exportReport() {
        // Logic to export the current chart as a file (e.g., PDF, CSV)
        System.out.println("Exporting report...");
    }

    // ------------------------------ Scene Switching ------------------------------
    // Switch to menu
    public void switchToMenu(ActionEvent event) {
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/Menu.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
