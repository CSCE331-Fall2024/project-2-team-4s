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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class SceneController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;

    // FXML injected UI elements for the menu table (used only in Manager.fxml)
    @FXML
    private TableView<MenuItem> menuTable;
    @FXML
    private TableColumn<MenuItem, Integer> itemIdColumn;
    @FXML
    private TableColumn<MenuItem, Integer> currentServingsColumn;
    @FXML
    private TableColumn<MenuItem, String> itemNameColumn;
    @FXML
    private TableColumn<MenuItem, Float> itemPriceColumn;
    @FXML
    private TableColumn<MenuItem, String> itemCategoryColumn;

    // Initialize method for Menu.fxml
    public void initialize() {
        try {
            conn = Database.connect();
            System.out.println("Database connection opened");
            conn.close();
            System.out.println("Database connection closed");
        } catch (SQLException e) {
            System.err.println("Database connection error");
        }
    }

    // Method to initialize only when Manager.fxml is loaded
    public void initializeManager() {
        try {
            conn = Database.connect();
            System.out.println("Database connection opened");

            // Setup table columns (Manager.fxml)
            itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("menu_item_id"));
            currentServingsColumn.setCellValueFactory(new PropertyValueFactory<>("current_servings"));
            itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("item_name"));
            itemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("item_price"));
            itemCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("item_category"));

            // Load the menu items into the TableView
            loadMenuItems();

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
