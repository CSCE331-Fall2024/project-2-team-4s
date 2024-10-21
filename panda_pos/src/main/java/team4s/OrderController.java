package team4s;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderController {

    @FXML
    private Button bowlButton, plateButton, biggerPlateButton, appetizerButton, drinksButton, sideButton, entreeButton;
    @FXML
    private Button confirmButton, cancelButton;
    @FXML
    private Button editItemButton, deleteItemButton; // New buttons
    @FXML
    private ListView<String> currentOrderList;
    @FXML
    private VBox dynamicButtons;
    @FXML
    private Label orderTotalLabel, selectedEmployeeLabel, customerLabel; // Add this to bind with the label in FXML
    @FXML
    private Button selectEmployeeButton;
    @FXML
    private Button addCustomerButton; // New button
    @FXML
    private Button disposablesButton; // New button for disposables

    private List<String> currentOrder = new ArrayList<>();
    private String selectedPlateType = "";
    private Map<String, Integer> selectedEntrees = new HashMap<>(); // Track selected entrees and counts
    private String selectedSide = ""; // To track the selected side
    private List<Button> entreeButtons = new ArrayList<>(); // To track the dynamically created entree buttons

    private int editingIndex = -1; // To track which item in the order is being edited

    private double orderTotal = 0.0;

    private int selectedEmployeeId; // To store the selected employee's ID
    private int selectedCustomerId;
    private String customerFullName;
    @FXML
    private Button goBackButton;

    /**
     * Handles the action when the "Go Back" button is clicked.
     * This method loads the main menu by switching the scene from the current order
     * page.
     * It uses the FXMLLoader to load the FXML file for the main menu and sets the
     * new scene on the stage.
     * If an IOException occurs during loading, it is caught and logged, displaying
     * an error alert.
     *
     * @throws IOException if the FXML file for the main menu cannot be loaded.
     */
    @FXML
    private void goBackToMainMenu() {
        try {
            // Load the main menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Menu.fxml"));
            Parent mainMenuRoot = loader.load();

            // Get the current stage
            Stage stage = (Stage) goBackButton.getScene().getWindow();

            // Set the main menu scene
            Scene mainMenuScene = new Scene(mainMenuRoot);
            stage.setScene(mainMenuScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the main menu.");
        }
    }

    /**
     * Displays a dialog to allow the selection of an employee from a list.
     * The selected employee's ID is stored and their name is shown in the UI.
     */
    public void showEmployeeSelectionDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Employee");
        dialog.setHeaderText("Please select an employee to continue.");

        // Create a VBox to hold the ComboBox
        VBox dialogPaneContent = new VBox();
        Label employeeLabel = new Label("Employee:");
        ComboBox<String> employeeComboBox = new ComboBox<>();

        // Load employee names into the ComboBox
        Map<String, Integer> employeeMap = loadEmployeesFromDatabase();
        employeeComboBox.getItems().addAll(employeeMap.keySet());

        dialogPaneContent.getChildren().addAll(employeeLabel, employeeComboBox);
        dialog.getDialogPane().setContent(dialogPaneContent);

        // Add OK and Cancel buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // When OK is clicked, store the selected employee ID
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String selectedEmployeeName = employeeComboBox.getSelectionModel().getSelectedItem();
                if (selectedEmployeeName != null) {
                    selectedEmployeeId = employeeMap.get(selectedEmployeeName); // Store the employee ID
                    selectedEmployeeLabel.setText("Selected Employee: " + selectedEmployeeName);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Loads employee data from the database and returns a map of employee names to
     * IDs.
     *
     * @return a map where the keys are employee names and the values are their
     *         corresponding IDs.
     */
    private Map<String, Integer> loadEmployeesFromDatabase() {
        Map<String, Integer> employeeMap = new HashMap<>();

        String query = "SELECT employee_id, first_name, last_name FROM employee"; // Adjust column names as needed

        try (Connection conn = Database.connect(); // Replace with your DB connection method
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int employeeId = rs.getInt("employee_id");
                String employeeFName = rs.getString("first_name");
                String employeeLName = rs.getString("last_name");
                String employeeName = employeeFName + " " + employeeLName;
                employeeMap.put(employeeName, employeeId); // Add employee name and ID to the map
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employeeMap;
    }

    /**
     * Handles the event when the "Bowl" button is clicked.
     * This method sets the selected plate type to "Bowl".
     */
    @FXML
    public void selectBowl() {
        selectPlate("Bowl");
    }

    /**
     * Handles the event when the "Plate" button is clicked.
     * This method sets the selected plate type to "Plate".
     */
    @FXML
    public void selectPlate() {
        selectPlate("Plate");
    }

    /**
     * Handles the event when the "Bigger Plate" button is clicked.
     * This method sets the selected plate type to "Bigger Plate".
     */
    @FXML
    public void selectBiggerPlate() {
        selectPlate("Bigger Plate");
    }

    /**
     * Handles the event when the "Appetizer" button is clicked.
     * This method resets the button styles and loads the available appetizer items.
     */
    @FXML
    public void selectAppetizer() {
        resetButtonStyles();
        appetizerButton.setStyle("-fx-background-color: #ffcccc"); // Change background color
        selectedPlateType = ""; // Reset plate type
        loadItems("Appetizer");
    }

    /**
     * Handles the event when the "Drinks" button is clicked.
     * This method resets the button styles and loads the available drink items.
     */
    @FXML
    public void selectDrinks() {
        resetButtonStyles();
        drinksButton.setStyle("-fx-background-color: #ffcccc"); // Change background color
        selectedPlateType = ""; // Reset plate type
        loadItems("Drink");
    }

    /**
     * Handles the event when the "Side" button is clicked.
     * This method resets the button styles and loads the available side items.
     */
    @FXML
    public void selectSide() {
        resetButtonStyles();
        sideButton.setStyle("-fx-background-color: #ffcccc"); // Change background color
        selectedPlateType = ""; // Reset plate type
        loadItems("Side");
    }

    /**
     * Handles the event when the "Entree" button is clicked.
     * This method resets the button styles and loads the available entree items.
     */
    @FXML
    public void selectEntree() {
        resetButtonStyles();
        entreeButton.setStyle("-fx-background-color: #ffcccc"); // Change background color
        selectedPlateType = ""; // Reset plate type
        loadItems("Entree");
    }

    /**
     * Handles the event when the "Disposables" button is clicked.
     * This method resets the button styles and loads the available disposable items
     * from the inventory.
     */
    @FXML
    public void selectDisposables() {
        resetButtonStyles();
        disposablesButton.setStyle("-fx-background-color: #ffcccc"); // Change background color
        selectedPlateType = ""; // Reset plate type as it's not relevant for disposables
        loadDisposables(); // Load disposables from inventory
    }

    /**
     * Loads disposable items from the inventory table and dynamically creates
     * buttons for each item.
     * These items are displayed on the UI, allowing the user to select a disposable
     * item for the order.
     */
    private void loadDisposables() {
        dynamicButtons.getChildren().clear(); // Clear previous buttons

        // Add a label for picking a disposable item
        Label pickDisposableLabel = new Label("Please Pick a Disposable Item:");
        dynamicButtons.getChildren().add(pickDisposableLabel);

        String query = "SELECT ingredient_name FROM inventory WHERE ingredient_id IN (36,37,38,40)";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String disposableName = rs.getString("ingredient_name");
                Button disposableButton = new Button(disposableName);
                disposableButton.setOnAction(event -> addToOrderDisposable(disposableName)); // Add disposable to the
                                                                                             // order
                dynamicButtons.getChildren().add(disposableButton);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a disposable item to the current order and updates the inventory by
     * subtracting one serving from the stock.
     *
     * @param disposableName the name of the disposable item being added to the
     *                       order.
     */
    private void addToOrderDisposable(String disposableName) {
        String orderItem = "Disposable (" + disposableName + ")";
        currentOrder.add(orderItem);
        currentOrderList.getItems().add(orderItem);

        // Subtract from inventory for disposables
        updateInventoryServing(disposableName, 1); // Reduce the inventory count by 1 (or adjust based on required
                                                   // serving size)

        updateOrderTotal();
        resetSelections();
    }

    /**
     * Fetches the menu_item_id from the menu_item table based on the item's name.
     *
     * @param itemName the name of the menu item.
     * @return the ID of the menu item, or -1 if the item is not found.
     */
    private void updateInventoryServing(String itemName, int servingsUsed) {
        String query = "UPDATE inventory SET current_stock = current_stock - ? WHERE ingredient_name = ?";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, servingsUsed); // Subtract the number of servings used
            stmt.setString(2, itemName); // Update the item by name

            int rowsUpdated = stmt.executeUpdate(); // Execute the update
            if (rowsUpdated > 0) {
                System.out.println("Inventory updated for item: " + itemName);
            } else {
                System.out.println("No inventory record found for item: " + itemName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches the menu_item_id from the menu_item table based on the item's name.
     *
     * @param itemName the name of the menu item.
     * @return the ID of the menu item, or -1 if the item is not found.
     */
    private int getMenuItemId(String itemName) {
        String query = "SELECT menu_item_id FROM menu_item WHERE item_name = ?";
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, itemName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("menu_item_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if no menu_item_id is found for the item
    }

    /**
     * Handles the request to restock servings for a selected menu item (entree or
     * side).
     * Ensures that sufficient ingredients are available before performing the
     * restock operation.
     */
    @FXML
    private void requestRestockForMenuItem() {
        String selectedItem = currentOrderList.getSelectionModel().getSelectedItem();
        if (selectedItem == null || (!selectedItem.startsWith("Entree") && !selectedItem.startsWith("Side"))) {
            showAlert("No Item Selected", "Please select a valid menu item (entree or side) to restock.");
            return;
        }

        String itemName = extractItemName(selectedItem);
        int menuItemId = getMenuItemId(itemName);
        if (menuItemId == -1) {
            showAlert("Item Not Found", "Selected item is not available in the menu.");
            return;
        }

        if (!canRestockServings(menuItemId)) {
            showAlert("Insufficient Ingredients",
                    "There are insufficient ingredients available to restock " + itemName);
            return;
        }

        restockServings(menuItemId);
        showAlert("Restock Successful", "Servings for " + itemName + " have been successfully restocked.");
    }

    /**
     * Checks if sufficient ingredients are available in the inventory to restock
     * servings for a menu item.
     *
     * @param menuItemId the ID of the menu item to restock.
     * @return true if ingredients are available, false otherwise.
     */
    private boolean canRestockServings(int menuItemId) {
        String query = "SELECT ingredient_id, ingredient_amount FROM inventory_menu_item WHERE menu_item_id = ?";
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, menuItemId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int ingredientId = rs.getInt("ingredient_id");
                double amountNeeded = rs.getDouble("ingredient_amount");
                if (!hasEnoughIngredients(ingredientId, amountNeeded)) {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Retrieves the available servings for a specified menu item from the database.
     *
     * @param itemName the name of the menu item.
     * @return the number of available servings for the item, or 0 if not found.
     */
    private int getAvailableServings(String itemName) {
        String query = "SELECT current_servings FROM menu_item WHERE item_name = ?";
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, itemName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("current_servings");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Checks if there are enough ingredients in the inventory to meet the specified
     * requirement.
     *
     * @param ingredientId the ID of the ingredient.
     * @param amountNeeded the required amount of the ingredient.
     * @return true if the current stock is sufficient, false otherwise.
     */
    private boolean hasEnoughIngredients(int ingredientId, double amountNeeded) {
        String query = "SELECT current_stock FROM inventory WHERE ingredient_id = ?";
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double currentStock = rs.getDouble("current_stock");
                return currentStock >= amountNeeded;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Restocks servings for a menu item by decrementing the inventory and
     * increasing the available servings.
     *
     * @param menuItemId the ID of the menu item to restock.
     */
    private void restockServings(int menuItemId) {
        String query = "SELECT ingredient_id, ingredient_amount FROM inventory_menu_item WHERE menu_item_id = ?";
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, menuItemId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int ingredientId = rs.getInt("ingredient_id");
                double amountUsed = rs.getDouble("ingredient_amount");

                // Decrement the inventory by the amount used
                decrementInventory(ingredientId, amountUsed);
            }

            // Increase the servings in the menu_item table
            String updateQuery = "UPDATE menu_item SET current_servings = current_servings + 10 WHERE menu_item_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, menuItemId);
                updateStmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Decrements the stock of an ingredient in the inventory by the specified
     * amount.
     *
     * @param ingredientId the ID of the ingredient to update.
     * @param amountUsed   the amount to subtract from the current stock.
     */
    private void decrementInventory(int ingredientId, double amountUsed) {
        String query = "UPDATE inventory SET current_stock = current_stock - ? WHERE ingredient_id = ?";
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, amountUsed);
            stmt.setInt(2, ingredientId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the plate selection for the given plate type and updates the UI.
     *
     * @param plateType the type of plate selected (e.g., "Bowl", "Plate", "Bigger
     *                  Plate").
     */
    private void selectPlate(String plateType) {
        // Reset button styles
        resetButtonStyles();

        // Set the selected plate type
        selectedPlateType = plateType;

        // Change the background color of the selected button
        switch (plateType) {
            case "Bowl":
                bowlButton.setStyle("-fx-background-color: #ffcccc");
                break;
            case "Plate":
                plateButton.setStyle("-fx-background-color: #ffcccc");
                break;
            case "Bigger Plate":
                biggerPlateButton.setStyle("-fx-background-color: #ffcccc");
                break;
        }

        // Show side buttons
        showSideButtons();
    }

    /**
     * Resets the styles of all buttons to their default state.
     */
    private void resetButtonStyles() {
        // Reset all button styles to default
        bowlButton.setStyle(null);
        plateButton.setStyle(null);
        biggerPlateButton.setStyle(null);
        appetizerButton.setStyle(null);
        drinksButton.setStyle(null);
        sideButton.setStyle(null); // Reset side button style
        entreeButton.setStyle(null); // Reset entree button style
        disposablesButton.setStyle(null);

        // Reset all entree button styles to default
        for (Button entreeButton : entreeButtons) {
            entreeButton.setStyle(null);
        }
    }

    /**
     * Loads side options from the menu_item table and dynamically creates buttons
     * for each available side.
     */
    private void showSideButtons() {
        dynamicButtons.getChildren().clear(); // Clear previous buttons

        // Add a label for picking a side
        Label pickSideLabel = new Label("Please Pick a Side:");
        dynamicButtons.getChildren().add(pickSideLabel);

        String query = "SELECT item_name FROM menu_item WHERE item_category = 'Side'";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String sideName = rs.getString("item_name");
                Button sideButton = new Button(sideName);
                sideButton.setOnAction(event -> loadEntreeButtons(sideName)); // Load entrees for the selected side
                dynamicButtons.getChildren().add(sideButton);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads entree options based on the selected side and dynamically creates
     * buttons for each available entree.
     *
     * @param selectedSide the name of the selected side.
     */
    private void loadEntreeButtons(String selectedSide) {
        dynamicButtons.getChildren().clear(); // Clear previous buttons
        selectedEntrees.clear(); // Clear previously selected entrees
        this.selectedSide = selectedSide; // Set the selected side
        entreeButtons.clear(); // Clear the list of entree buttons

        // Add a label for picking an entree
        Label pickEntreeLabel = new Label("Please Pick an Entree:");
        dynamicButtons.getChildren().add(pickEntreeLabel);

        String query = "SELECT item_name FROM menu_item WHERE item_category = 'Entree'";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String entreeName = rs.getString("item_name");
                Button entreeButton = new Button(entreeName);
                entreeButton.setOnAction(event -> handleEntreeSelection(entreeButton, entreeName)); // Handle entree
                                                                                                    // selection
                dynamicButtons.getChildren().add(entreeButton);
                entreeButtons.add(entreeButton); // Track this button
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the selection of an entree, ensuring the maximum number of entrees
     * for the selected plate type is not exceeded.
     * Updates the UI to reflect the selected entrees.
     *
     * @param entreeButton the button representing the selected entree.
     * @param entreeName   the name of the selected entree.
     */
    private void handleEntreeSelection(Button entreeButton, String entreeName) {
        int maxEntrees = 0;

        // Determine the maximum number of entrees based on the plate type
        switch (selectedPlateType) {
            case "Bowl":
                maxEntrees = 1;
                break;
            case "Plate":
                maxEntrees = 2;
                break;
            case "Bigger Plate":
                maxEntrees = 3;
                break;
        }

        // Check if entree has already been selected and increase its count
        int currentCount = selectedEntrees.getOrDefault(entreeName, 0);

        if (currentCount > 0) {
            if (currentCount < 3) { // If already selected and less than 3 times, increase count
                selectedEntrees.put(entreeName, currentCount + 1);
                changeButtonColor(entreeButton, currentCount + 1); // Darker color for multiple selections
            } else {
                System.out.println("Maximum times this entree can be selected reached.");
            }
        } else {
            // If it's the first time selecting the entree, add it
            if (selectedEntrees.size() < maxEntrees) {
                selectedEntrees.put(entreeName, 1);
                changeButtonColor(entreeButton, 1); // Lightest color for first selection
            } else {
                System.out.println("Maximum number of entrees reached.");
            }
        }

        // Automatically add to order when max entrees are reached
        int totalSelected = selectedEntrees.values().stream().mapToInt(Integer::intValue).sum();
        if (totalSelected == maxEntrees) {
            addToOrder(selectedSide);
        }
    }

    /**
     * Changes the color of an entree button based on the number of times it has
     * been selected.
     *
     * @param entreeButton the button representing the entree.
     * @param count        the number of times the entree has been selected.
     */
    private void changeButtonColor(Button entreeButton, int count) {
        String color;
        switch (count) {
            case 1:
                color = "#ff9999"; // Light color for first selection
                break;
            case 2:
                color = "#ff6666"; // Darker color for second selection
                break;
            case 3:
                color = "#ff3333"; // Darkest color for third selection
                break;
            default:
                color = "#ffcccc"; // Default color if something goes wrong
                break;
        }
        entreeButton.setStyle("-fx-background-color: " + color);
    }

    /**
     * Adds a plate with the selected side and entrees to the order and updates the
     * total price.
     *
     * @param side the name of the selected side.
     */
    private void addToOrder(String side) {
        double platePrice = fetchPriceFromDatabase(selectedPlateType); // Fetch price for the plate type
        double sidePrice = fetchPriceFromDatabase(side); // Fetch price for the side

        StringBuilder orderItem = new StringBuilder(selectedPlateType + " (" + side);
        orderTotal += platePrice; // Add the plate price to the total
        orderTotal += sidePrice; // Add the side price to the total

        // Add each entree with its count and update the total price
        for (Map.Entry<String, Integer> entry : selectedEntrees.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                double entreePrice = fetchPriceFromDatabase(entry.getKey()); // Fetch price for the entree
                orderItem.append(", ").append(entry.getKey()).append(" ($").append(String.format("%.2f", entreePrice))
                        .append(")"); // Include price with entree
                orderTotal += entreePrice; // Add entree price to the total
            }
        }
        orderItem.append(")\n$").append(String.format("%.2f", platePrice + sidePrice)); // Display total price for plate
                                                                                        // and side

        currentOrder.add(orderItem.toString());
        currentOrderList.getItems().add(orderItem.toString()); // Add item with price

        updateOrderTotal(); // Update the total displayed in the UI

        // Reset selections
        resetSelections();
    }

    /**
     * Adds an appetizer to the order if it has available servings, and updates the
     * total price.
     * If no servings are available, an alert is shown.
     *
     * @param item the name of the appetizer to add.
     */
    private void addToOrderAppetizer(String item) {
        int availableServings = getAvailableServings(item);
        if (availableServings <= 0) {
            showAlert("Out of Stock", "No servings available for " + item);
            return;
        }

        double appetizerPrice = fetchPriceFromDatabase("Appetizer"); // Fetch price for the appetizer
        String orderItem = "Appetizer (" + item + ")\n$" + String.format("%.2f", appetizerPrice);

        orderTotal += appetizerPrice; // Update the total

        currentOrder.add(orderItem);
        currentOrderList.getItems().add(orderItem); // Add item with price

        updateOrderTotal(); // Update the total displayed in the UI

        // Subtract one serving from the menu_item table
        updateMenuItemServing(item, 1); // Subtract one serving from the database

        resetSelections();
    }

    /**
     * Adds a drink to the current order and updates the total price.
     *
     * @param item the name of the drink to be added.
     */
    private void addToOrderDrink(String item) {
        double drinkPrice = fetchPriceFromDatabase(item); // Fetch price for the drink
        String orderItem = "Drink (" + item + ")\n$" + String.format("%.2f", drinkPrice);

        orderTotal += drinkPrice; // Update the total

        currentOrder.add(orderItem);
        currentOrderList.getItems().add(orderItem); // Add item with price

        updateOrderTotal(); // Update the total displayed in the UI

        resetSelections();
    }

    /**
     * Adds a side to the current order and updates the total price.
     *
     * @param side the name of the side to be added.
     */
    private void addToOrderSide(String side) {
        int availableServings = getAvailableServings(side);
        if (availableServings <= 0) {
            showAlert("Out of Stock", "No servings available for " + side);
            return;
        }

        double sidePrice = fetchPriceFromDatabase(side); // Fetch price for the side
        double aLaCarteSidePrice = fetchPriceFromDatabase("A La Carte Side"); // Fetch a la carte side price

        String orderItem = "Side (" + side + ")\n$" + String.format("%.2f", sidePrice + aLaCarteSidePrice);

        orderTotal += sidePrice + aLaCarteSidePrice; // Add to total

        currentOrder.add(orderItem);
        currentOrderList.getItems().add(orderItem); // Add item with price

        updateOrderTotal(); // Update total

        // Subtract one serving from the menu_item table
        updateMenuItemServing(side, 1); // Subtract one serving from the database

        resetSelections();
    }

    /**
     * Adds an entree to the current order and updates the total price.
     *
     * @param entree the name of the entree to be added.
     */
    private void addToOrderEntree(String entree) {
        int availableServings = getAvailableServings(entree);
        if (availableServings <= 0) {
            showAlert("Out of Stock", "No servings available for " + entree);
            return;
        }

        double entreePrice = fetchPriceFromDatabase(entree); // Fetch price for the entree
        double aLaCarteEntreePrice = fetchPriceFromDatabase("A La Carte Entree"); // Fetch a la carte entree price

        String orderItem = "Entree (" + entree + ")\n$" + String.format("%.2f", entreePrice + aLaCarteEntreePrice);

        orderTotal += entreePrice + aLaCarteEntreePrice; // Add to total

        currentOrder.add(orderItem);
        currentOrderList.getItems().add(orderItem); // Add item with price

        updateOrderTotal(); // Update total

        // Subtract one serving from the menu_item table
        updateMenuItemServing(entree, 1); // Subtract one serving from the database

        resetSelections();
    }

    /**
     * Fetches the price of an item from the database.
     *
     * @param itemName the name of the item to fetch the price for.
     * @return the price of the item, or 0.0 if not found.
     */
    private double fetchPriceFromDatabase(String itemName) {
        String query = "SELECT item_price FROM menu_item WHERE item_name = ?";
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, itemName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double price = rs.getDouble("item_price");
                System.out.println("Price for " + itemName + ": " + price); // Debugging price retrieval
                return price; // Return the price of the item
            } else {
                System.out.println("Item not found in database: " + itemName); // Debugging if item is missing
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Return 0 if the item is not found
    }

    /**
     * Updates the order total in the UI.
     */
    private void updateOrderTotal() {
        orderTotalLabel.setText(String.format("Total: $%.2f", orderTotal));
    }

    /**
     * Resets all selections, clearing selected entrees, side, and dynamic buttons.
     */
    private void resetSelections() {
        selectedEntrees.clear();
        selectedSide = "";
        dynamicButtons.getChildren().clear();
        resetButtonStyles();
    }

    /**
     * Loads menu items (such as sides, entrees, drinks, or appetizers) based on
     * their category and dynamically
     * adds buttons to the UI for user selection.
     *
     * @param category the category of items to load (e.g., "Side", "Entree",
     *                 "Drink", "Appetizer").
     */
    private void loadItems(String category) {
        dynamicButtons.getChildren().clear(); // Clear previous buttons

        String query = "SELECT item_name, item_price FROM menu_item WHERE item_category = ?";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, category); // Set the category (Appetizer, Drink, Side, or Entree)
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String itemName = rs.getString("item_name");
                Button itemButton;

                if (category.equals("Drink")) {
                    double itemPrice = rs.getDouble("item_price"); // Fetch price for drinks
                    // Set the button text to include both the name and price for drinks
                    itemButton = new Button(itemName + " ($" + String.format("%.2f", itemPrice) + ")");
                    itemButton.setOnAction(event -> addToOrderDrink(itemName)); // Add drink to order
                } else {
                    // For other categories, just show the item name
                    itemButton = new Button(itemName);

                    if (category.equals("Appetizer")) {
                        itemButton.setOnAction(event -> addToOrderAppetizer(itemName)); // Add appetizer to order
                    } else if (category.equals("Side")) {
                        itemButton.setOnAction(event -> addToOrderSide(itemName)); // Add side to order
                    } else if (category.equals("Entree")) {
                        itemButton.setOnAction(event -> addToOrderEntree(itemName)); // Add entree to order
                    }
                }

                dynamicButtons.getChildren().add(itemButton); // Add button to the dynamic button panel
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Confirms the current order by displaying a dialog with order details and
     * payment type options.
     * The order is then placed in the database with the selected payment type.
     */
    @FXML
    public void confirmOrder() {
        StringBuilder orderDetails = new StringBuilder("Order confirmed: ");
        for (String item : currentOrder) {
            orderDetails.append(item).append(", ");
        }
        // Remove trailing comma and space
        if (orderDetails.length() > 2) {
            orderDetails.setLength(orderDetails.length() - 2);
        }
        System.out.println(orderDetails);
    }

    /**
     * Deletes the selected item from the current order, updates the total price,
     * and removes the item from the UI.
     */
    @FXML
    private void deleteSelectedItem() {
        String selectedItem = currentOrderList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Calculate the price of the item being deleted
            double itemPrice = calculateOrderItemPrice(selectedItem);
            // Subtract the price of the deleted item from the total
            orderTotal -= itemPrice;

            // Remove the item from the current order
            currentOrder.remove(selectedItem);
            currentOrderList.getItems().remove(selectedItem);

            // Update the total label
            updateOrderTotal();
        }
    }

    /**
     * Calculates the price of a specific order item.
     *
     * @param orderItem the string representing the order item.
     * @return the total price of the order item.
     */
    private double calculateOrderItemPrice(String orderItem) {
        double totalPrice = 0.0;

        if (orderItem.startsWith("Appetizer")) {
            totalPrice = fetchPriceFromDatabase("Appetizer"); // Always use generic "Appetizer" item
        } else if (orderItem.startsWith("Drink")) {
            String drinkName = orderItem.substring(orderItem.indexOf("(") + 1, orderItem.indexOf(")"));
            totalPrice = fetchPriceFromDatabase(drinkName);
        } else if (orderItem.startsWith("Side")) {
            String sideName = orderItem.substring(orderItem.indexOf("(") + 1, orderItem.indexOf(")"));
            totalPrice = fetchPriceFromDatabase("A La Carte Side");
            totalPrice += fetchPriceFromDatabase(sideName);
        } else if (orderItem.startsWith("Entree")) {
            String entreeName = orderItem.substring(orderItem.indexOf("(") + 1, orderItem.indexOf(")"));
            totalPrice = fetchPriceFromDatabase(entreeName);
            totalPrice += fetchPriceFromDatabase("A La Carte Entree");
        } else {
            // Handle bowls, plates, and bigger plates
            String[] parts = orderItem.split(" \\+ ");
            for (String part : parts) {
                totalPrice += fetchPriceFromDatabase(part.trim());
            }
        }

        return totalPrice;
    }

    /**
     * Edits the selected item from the current order.
     * Opens the appropriate selection dialog based on the type of item selected
     * (Appetizer, Drink, Side, Entree, or Plate).
     */
    @FXML
    public void editSelectedItem() {
        String selectedItem = currentOrderList.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Item Selected", "Please select an item from the order list to edit.");
            return;
        }

        if (selectedItem.startsWith("Appetizer")) {
            String appetizerName = selectedItem.substring(selectedItem.indexOf("(") + 1, selectedItem.indexOf(")"));
            editingIndex = currentOrder.indexOf(selectedItem); // Store the index of the item being edited
            showSelectionDialog("Appetizer", "Appetizer", appetizerName);
        } else if (selectedItem.startsWith("Drink")) {
            String drinkName = selectedItem.substring(selectedItem.indexOf("(") + 1, selectedItem.indexOf(")"));
            editingIndex = currentOrder.indexOf(selectedItem); // Store the index of the item being edited
            showSelectionDialog("Drink", "Drink", drinkName);
        } else if (selectedItem.startsWith("Side")) {
            String sideName = selectedItem.substring(selectedItem.indexOf("(") + 1, selectedItem.indexOf(")"));
            editingIndex = currentOrder.indexOf(selectedItem); // Store the index of the item being edited
            showSelectionDialog("Side", "Side", sideName);
        } else if (selectedItem.startsWith("Entree")) {
            String entreeName = selectedItem.substring(selectedItem.indexOf("(") + 1, selectedItem.indexOf(")"));
            editingIndex = currentOrder.indexOf(selectedItem); // Store the index of the item being edited
            showSelectionDialog("Entree", "Entree", entreeName);
        } else {
            String[] parts = selectedItem.split(" \\+ ");
            String plateType = parts[0];
            selectedSide = parts[1];
            List<String> selectedEntrees = new ArrayList<>();

            for (int i = 2; i < parts.length; i++) {
                selectedEntrees.add(parts[i].trim());
            }

            editingIndex = currentOrder.indexOf(selectedItem); // Store the index of the item being edited

            if (plateType.equals("Bowl")) {
                selectedPlateType = "Bowl";
                showSelectionDialog(plateType, "Side", 1, selectedSide, selectedEntrees);
            } else if (plateType.equals("Plate")) {
                selectedPlateType = "Plate";
                showSelectionDialog(plateType, "Side", 2, selectedSide, selectedEntrees);
            } else if (plateType.equals("Bigger Plate")) {
                selectedPlateType = "Bigger Plate";
                showSelectionDialog(plateType, "Side", 3, selectedSide, selectedEntrees);
            }
        }
    }

    /**
     * Displays a selection dialog for editing an item (Appetizer or Drink).
     * Loads the items into a ComboBox for user selection.
     *
     * @param type        the type of the item to edit (Appetizer or Drink).
     * @param itemType    the category of the item being edited.
     * @param preFillItem the name of the item to pre-fill in the selection dialog.
     */
    private void showSelectionDialog(String type, String itemType, String preFillItem) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit " + itemType);
        dialog.setHeaderText("Edit your " + itemType);

        VBox dialogPane = new VBox();
        Label itemLabel = new Label(itemType + ":");
        ComboBox<String> comboBox = new ComboBox<>();

        // Load items into the dropdown
        loadComboBoxItems(itemType, comboBox);
        comboBox.getSelectionModel().select(preFillItem);

        dialogPane.getChildren().addAll(itemLabel, comboBox);

        dialog.getDialogPane().setContent(dialogPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String selectedItem = comboBox.getSelectionModel().getSelectedItem();
                // Update the order for Appetizer or Drink
                if (type.equals("Appetizer")) {
                    updateAppetizerOrder(selectedItem);
                } else if (type.equals("Drink")) {
                    updateDrinkOrder(selectedItem);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Updates the current order with the new selected appetizer.
     *
     * @param appetizer the name of the selected appetizer.
     */
    private void updateAppetizerOrder(String appetizer) {
        // Subtract the price of the old item (generic Appetizer price) from the total
        double oldPrice = fetchPriceFromDatabase("Appetizer"); // Always use "Appetizer" price
        orderTotal -= oldPrice;

        // Add the price of the new item (still the generic Appetizer price)
        double newPrice = fetchPriceFromDatabase("Appetizer"); // Always use "Appetizer" price
        orderTotal += newPrice;

        // Update the order item with the new appetizer name
        String orderItem = "Appetizer (" + appetizer + ")";
        currentOrder.set(editingIndex, orderItem);
        currentOrderList.getItems().set(editingIndex, orderItem);

        // Update the total label
        updateOrderTotal();

        resetSelections();
    }

    /**
     * Updates the current order with the new selected drink.
     *
     * @param drink the name of the selected drink.
     */
    private void updateDrinkOrder(String drink) {
        // Subtract the price of the old item from the total
        double oldPrice = calculateOrderItemPrice(currentOrder.get(editingIndex));
        orderTotal -= oldPrice;

        // Add the price of the new item
        double newPrice = fetchPriceFromDatabase(drink);
        orderTotal += newPrice;

        String orderItem = "Drink (" + drink + ")";
        currentOrder.set(editingIndex, orderItem);
        currentOrderList.getItems().set(editingIndex, orderItem);

        // Update the total label
        updateOrderTotal();

        resetSelections();
    }

    /**
     * Displays an alert dialog with the provided title and message.
     *
     * @param title   the title of the alert dialog.
     * @param message the message content of the alert dialog.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a selection dialog for editing an item (Plate, Side, or Entree).
     * Allows the user to edit a plate order by choosing the side and entrees.
     *
     * @param type           the type of the item to edit (Plate).
     * @param itemType       the category of the item being edited (Side or Entree).
     * @param maxEntrees     the maximum number of entrees allowed for the plate.
     * @param preFillSide    the side to pre-fill in the selection dialog.
     * @param preFillEntrees the entrees to pre-fill in the selection dialog.
     */
    private void showSelectionDialog(String type, String itemType, int maxEntrees, String preFillSide,
            List<String> preFillEntrees) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit " + selectedPlateType); // Use selectedPlateType here
        dialog.setHeaderText("Edit your " + selectedPlateType + " order");

        VBox dialogPane = new VBox();

        // Add a label and ComboBox for the side
        Label sideLabel = new Label("Side:");
        ComboBox<String> sideComboBox = new ComboBox<>();
        loadComboBoxItems("Side", sideComboBox);
        sideComboBox.getSelectionModel().select(preFillSide);

        dialogPane.getChildren().addAll(sideLabel, sideComboBox);

        // Add labels and ComboBoxes for entrees
        List<ComboBox<String>> entreeComboBoxes = new ArrayList<>();
        for (int i = 0; i < maxEntrees; i++) {
            Label entreeLabel = new Label("Entree " + (i + 1) + ":");
            ComboBox<String> entreeComboBox = new ComboBox<>();
            loadComboBoxItems("Entree", entreeComboBox);
            if (i < preFillEntrees.size()) {
                entreeComboBox.getSelectionModel().select(preFillEntrees.get(i));
            }
            entreeComboBoxes.add(entreeComboBox);
            dialogPane.getChildren().addAll(entreeLabel, entreeComboBox);
        }

        dialog.getDialogPane().setContent(dialogPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String selectedSide = sideComboBox.getSelectionModel().getSelectedItem();
                List<String> selectedEntrees = new ArrayList<>();
                for (ComboBox<String> entreeComboBox : entreeComboBoxes) {
                    selectedEntrees.add(entreeComboBox.getSelectionModel().getSelectedItem());
                }
                // Call updateOrder instead of addToOrder
                updateOrder(selectedSide, selectedEntrees);
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Updates the current order at the selected index with the new side and
     * entrees.
     *
     * @param side    the new selected side.
     * @param entrees the list of new selected entrees.
     */
    private void updateOrder(String side, List<String> entrees) {
        StringBuilder orderItem = new StringBuilder(selectedPlateType + " + " + side);
        for (String entree : entrees) {
            if (entree != null) {
                orderItem.append(" + ").append(entree);
            }
        }

        // Replace the existing item in the order list
        currentOrder.set(editingIndex, orderItem.toString());
        currentOrderList.getItems().set(editingIndex, orderItem.toString());

        resetSelections();
    }

    /**
     * Loads items into the provided ComboBox based on the specified category.
     *
     * @param category the category of items to load (e.g., Side, Entree).
     * @param comboBox the ComboBox to populate with items from the database.
     */
    private void loadComboBoxItems(String category, ComboBox<String> comboBox) {
        String query = "SELECT item_name FROM menu_item WHERE item_category = ?";
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                comboBox.getItems().add(rs.getString("item_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancels the current order by clearing the order list, resetting selections,
     * and setting the total to zero.
     */
    @FXML
    public void cancelOrder() {
        // Clear the order list and reset the total
        currentOrderList.getItems().clear();
        currentOrder.clear();
        dynamicButtons.getChildren().clear(); // Clear any dynamic buttons
        resetButtonStyles(); // Reset button styles
        selectedEntrees.clear(); // Clear selected entrees
        selectedSide = ""; // Clear selected side

        // Reset the total to 0
        orderTotal = 0.0;
        updateOrderTotal();
    }

    /**
     * Increases the quantity of the selected item in the order by prompting the
     * user for a new quantity.
     * Updates the total cost based on the new quantity.
     */
    @FXML
    private void increaseItemQuantity() {
        String selectedItem = currentOrderList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            final int[] currentQuantity = { 1 }; // Use an array to store currentQuantity
            String itemName;
            final double[] itemPrice = { 0.0 }; // Use an array to store itemPrice

            // Check if the selected item contains a quantity indicator (e.g., " x2")
            if (selectedItem.contains("x")) {
                try {
                    int quantityIndex = selectedItem.lastIndexOf("x");
                    itemName = selectedItem.substring(0, quantityIndex).trim(); // Get the item name
                    currentQuantity[0] = Integer.parseInt(selectedItem.substring(quantityIndex + 1).trim()); // Extract
                                                                                                             // quantity
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to parse item quantity. Please check the item format.");
                    return;
                }
            } else {
                // No quantity indicator, so assume it's a single item
                itemName = selectedItem.trim();
            }

            // Create a TextInputDialog for the user to input the new quantity
            TextInputDialog quantityDialog = new TextInputDialog(String.valueOf(currentQuantity[0])); // Pre-fill with
                                                                                                      // current
                                                                                                      // quantity
            quantityDialog.setTitle("Select Quantity");
            quantityDialog.setHeaderText("Change the quantity for: " + itemName);
            quantityDialog.setContentText("Please enter the desired quantity:");

            // Show the dialog and wait for user input
            quantityDialog.showAndWait().ifPresent(input -> {
                try {
                    int newQuantity = Integer.parseInt(input);

                    // Ensure the quantity is positive
                    if (newQuantity <= 0) {
                        showAlert("Invalid Quantity", "Please enter a positive number for the quantity.");
                        return;
                    }

                    // Calculate the price for the item
                    itemPrice[0] = calculateOrderItemPrice(itemName); // Assume the price calculation logic exists
                    double updatedTotalPrice = itemPrice[0] * newQuantity;

                    // Update the order item string to show the new quantity and updated price
                    String updatedItem = itemName + " x" + newQuantity + "\n$"
                            + String.format("%.2f", updatedTotalPrice);

                    // Replace the selected item with the updated item in the order
                    int selectedIndex = currentOrderList.getSelectionModel().getSelectedIndex();
                    currentOrder.set(selectedIndex, updatedItem); // Update in the order list
                    currentOrderList.getItems().set(selectedIndex, updatedItem); // Update in the ListView

                    // Update the total order amount by adjusting the difference in quantity
                    double oldTotalPrice = itemPrice[0] * currentQuantity[0]; // Previous total for the item
                    orderTotal += (updatedTotalPrice - oldTotalPrice); // Update total with new quantity
                    updateOrderTotal(); // Update the total displayed in the UI
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid number.");
                }
            });

        } else {
            showAlert("No Item Selected", "Please select an item from the order list to change its quantity.");
        }
    }

    /**
     * Handles the confirmation of the order by verifying that an employee is
     * selected,
     * displaying a summary of the order and payment type options, and placing the
     * order in the database.
     */
    @FXML
    public void handleConfirmButton() {
        // Check if an employee has been selected (customer selection is optional now)
        if (selectedEmployeeId == 0) {
            // Show an alert if the employee is missing
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Information");
            alert.setHeaderText(null);
            alert.setContentText("Please select an employee before confirming the order.");
            alert.showAndWait();
            return; // Exit the method if validation fails
        }

        // Safely handle customerFullName being null
        String orderLabelContent = "Current Order";
        if (customerFullName != null && !customerFullName.isEmpty()) {
            orderLabelContent = "Current Order: (" + customerFullName + ")";
        }

        // Create a custom dialog for order confirmation
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Confirm Order");
        dialog.setHeaderText("Please review your order and select a payment type.");

        // Create a VBox to hold the current order details, total, tax, and payment type
        VBox dialogPaneContent = new VBox();
        dialogPaneContent.setSpacing(10); // Add some spacing between elements

        // Display the current order
        Label orderLabel = new Label(orderLabelContent);
        ListView<String> orderListView = new ListView<>();
        orderListView.getItems().addAll(currentOrder); // Add current orders to the ListView
        orderListView.setPrefHeight(200); // Adjust height to fit the orders

        // Calculate tax and total
        double tax = orderTotal * 0.0825; // Assume 8.25% sales tax
        double totalWithTax = orderTotal + tax;

        Label totalLabel = new Label(String.format("Total: $%.2f", orderTotal));
        Label taxLabel = new Label(String.format("Tax (8.25%%): $%.2f", tax));
        Label totalWithTaxLabel = new Label(String.format("Total with Tax: $%.2f", totalWithTax));

        // Add a ComboBox for payment type selection
        Label paymentTypeLabel = new Label("Payment Type:");
        ComboBox<String> paymentTypeComboBox = new ComboBox<>();
        paymentTypeComboBox.getItems().addAll("Credit/Debit", "Dining Dollars", "Gift Card");
        paymentTypeComboBox.setValue("Credit/Debit"); // Set default payment type

        // Add all elements to the VBox
        dialogPaneContent.getChildren().addAll(orderLabel, orderListView, totalLabel, taxLabel, totalWithTaxLabel,
                paymentTypeLabel, paymentTypeComboBox);

        // Set the dialog content
        dialog.getDialogPane().setContent(dialogPaneContent);

        // Add "Go Back" and "Confirm Order" buttons
        ButtonType goBackButton = new ButtonType("Go Back", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType confirmOrderButton = new ButtonType("Confirm Order", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(goBackButton, confirmOrderButton);

        // Handle the result of the dialog
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmOrderButton) {
                // Confirm the order and insert into the database with the selected payment type
                String selectedPaymentType = paymentTypeComboBox.getValue();
                placeOrderInDatabase(totalWithTax, selectedPaymentType);
            } else {
                // User clicked "Go Back"
                System.out.println("Order not confirmed.");
            }
            return null;
        });

        // Show the dialog and wait for user response
        dialog.showAndWait();
        customerFullName = ""; // Reset customer name after order is confirmed
    }

    /**
     * Extracts the item quantity from the order string (e.g., "Entree (Chicken)
     * x2").
     *
     * @param orderItem the order item string.
     * @return the quantity of the item, or 1 if no quantity is specified.
     */
    private int extractItemQuantity(String orderItem) {
        if (orderItem.contains("x")) {
            try {
                // Find the index of "x" and extract only the number after "x"
                int quantityIndex = orderItem.lastIndexOf("x");
                String quantityString = orderItem.substring(quantityIndex + 1).trim();
                // Ensure the string doesn't contain any price or extra characters
                quantityString = quantityString.split("\\s")[0]; // Split by space and take the first part (the number)
                return Integer.parseInt(quantityString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                // Default to 1 if the quantity is not properly parsed
                return 1;
            }
        }
        // Default to 1 if no "x" is found in the string (no quantity specified)
        return 1;
    }

    /**
     * Places the current order in the database by creating a transaction entry
     * and updating the inventory and menu item serving counts for each item in the
     * order.
     *
     * @param totalWithTax the total cost of the order including tax.
     * @param paymentType  the selected payment type (e.g., Credit/Debit, Dining
     *                     Dollars, Gift Card).
     */
    private void placeOrderInDatabase(double totalWithTax, String paymentType) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        int transactionId = -1;

        try {
            conn = Database.connect(); // Open a connection to the database
            conn.setAutoCommit(false); // Turn off auto-commit for transaction handling

            // Step 1: Insert the transaction into the "transaction" table with the selected
            // payment type
            String transactionQuery = "INSERT INTO transaction (total_cost, transaction_time, transaction_date, transaction_type, customer_id, employee_id, week_number) VALUES (?, CURRENT_TIME, CURRENT_DATE, ?, ?, ?, 40)";
            stmt = conn.prepareStatement(transactionQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setDouble(1, totalWithTax); // Set total cost with tax
            stmt.setString(2, paymentType); // Set the selected payment type
            stmt.setInt(4, selectedEmployeeId); // Set the employee ID

            // Check if a customer is selected, set the customer_id to NULL if not selected
            if (selectedCustomerId == 0) {
                stmt.setNull(3, java.sql.Types.INTEGER); // Set customer_id to NULL
            } else {
                stmt.setInt(3, selectedCustomerId); // Set the customer ID
            }

            stmt.executeUpdate();

            // Get the generated transaction ID
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                transactionId = generatedKeys.getInt(1); // Store the transaction ID
            }

            // Step 2: Insert each item from the current order into the
            // "menu_item_transaction" table
            if (transactionId != -1) {
                String itemQuery = "INSERT INTO menu_item_transaction (menu_item_id, transaction_id, item_quantity) VALUES (?, ?, ?)";
                stmt = conn.prepareStatement(itemQuery);

                for (String orderItem : currentOrder) {
                    String itemName = extractItemName(orderItem); // Extract the item name from the order
                    int quantity = extractItemQuantity(orderItem); // Get the item quantity

                    // Determine if the item is disposable or a menu item
                    if (orderItem.startsWith("Disposable")) {
                        // Subtract from the inventory for disposables
                        updateInventoryServing(itemName, quantity);
                    } else {
                        // Subtract from the menu_item table for menu items
                        int menuItemId = fetchMenuItemId(itemName); // Get the menu item ID from the database
                        stmt.setInt(1, menuItemId); // Set menu item ID
                        stmt.setInt(2, transactionId); // Set transaction ID
                        stmt.setInt(3, quantity); // Set item quantity
                        stmt.addBatch(); // Add this item to the batch

                        // Subtract servings from menu_item table for non-disposable items
                        updateMenuItemServing(itemName, quantity);
                    }
                }
                stmt.executeBatch(); // Execute all the items in the batch
            }

            conn.commit(); // Commit the transaction if everything is successful

            // Reset the order after successful insertion
            currentOrder.clear();
            currentOrderList.getItems().clear();
            orderTotal = 0.0;
            updateOrderTotal();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            // Close resources
            if (generatedKeys != null)
                try {
                    generatedKeys.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * Updates the menu_item table by subtracting the number of servings used for
     * the specified item.
     *
     * @param itemName     the name of the item in the menu.
     * @param servingsUsed the number of servings to subtract from the menu item's
     *                     available servings.
     */
    private void updateMenuItemServing(String itemName, int servingsUsed) {
        String query = "UPDATE menu_item SET current_servings = current_servings - ? WHERE item_name = ?";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, servingsUsed); // Subtract the number of servings used
            stmt.setString(2, itemName); // Update the item by name

            int rowsUpdated = stmt.executeUpdate(); // Execute the update
            if (rowsUpdated > 0) {
                System.out.println("Menu item servings updated for: " + itemName);
            } else {
                System.out.println("No menu item record found for: " + itemName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts the item name from the order string.
     *
     * @param orderItem the order string that contains the item name.
     * @return the extracted item name from the order string.
     */
    private String extractItemName(String orderItem) {
        return orderItem.substring(orderItem.indexOf("(") + 1, orderItem.indexOf(")")); // Adjust parsing logic based on
                                                                                        // your format
    }

    /**
     * Fetches the menu item ID from the menu_item table based on the item name.
     *
     * @param itemName the name of the item for which to fetch the menu item ID.
     * @return the menu item ID, or -1 if the item is not found in the database.
     */
    private int fetchMenuItemId(String itemName) {
        String query = "SELECT menu_item_id FROM menu_item WHERE item_name = ?";
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, itemName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("menu_item_id"); // Return the ID of the menu item
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if item is not found
    }

    /**
     * Displays a dialog to either select an existing customer or add a new
     * customer.
     * If an existing customer is selected, their details are auto-filled. A new
     * customer can also be added manually.
     */
    public void showCustomerDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Customer");
        dialog.setHeaderText("Select or Add a Customer");

        // Create a VBox for the layout
        VBox dialogPaneContent = new VBox();

        // Create the dropdown (ComboBox) for existing customers
        Label existingCustomerLabel = new Label("Existing Customer:");
        ComboBox<String> customerComboBox = new ComboBox<>();

        // Load customers from the database into the ComboBox
        Map<String, Integer> customerMap = loadCustomersFromDatabase();
        customerComboBox.getItems().addAll(customerMap.keySet());

        // Create fields for customer details
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Label phoneLabel = new Label("Phone:");
        TextField phoneField = new TextField();

        // Auto-fill fields when an existing customer is selected
        customerComboBox.setOnAction(event -> {
            String selectedCustomerName = customerComboBox.getSelectionModel().getSelectedItem();
            if (selectedCustomerName != null) {
                selectedCustomerId = customerMap.get(selectedCustomerName); // Store the customer ID
                // Fetch customer details and auto-fill the fields
                loadCustomerDetails(selectedCustomerId, firstNameField, lastNameField, emailField, phoneField);
            }
        });

        // Create a clear button to reset all fields
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> {
            firstNameField.clear();
            lastNameField.clear();
            emailField.clear();
            phoneField.clear();
            customerComboBox.getSelectionModel().clearSelection();
        });

        dialogPaneContent.getChildren().addAll(existingCustomerLabel, customerComboBox, firstNameLabel, firstNameField,
                lastNameLabel, lastNameField, emailLabel, emailField, phoneLabel, phoneField, clearButton);

        dialog.getDialogPane().setContent(dialogPaneContent);

        // Add Save and Cancel buttons
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        // Handle button clicks
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                if (selectedCustomerId == -1) {
                    // If no existing customer is selected, add a new customer
                    customerFullName = firstNameField.getText() + " " + lastNameField.getText();
                    saveNewCustomer(firstNameField.getText(), lastNameField.getText(), emailField.getText(),
                            phoneField.getText());
                    customerLabel.setText(String.format("Selected Customer: " + customerFullName));
                } else {
                    // Existing customer selected; update the label in the main dialog
                    customerFullName = firstNameField.getText() + " " + lastNameField.getText();
                    customerLabel.setText(String.format("Selected Customer: " + customerFullName));
                }
            }
            return null; // Close only the customer dialog
        });

        dialog.showAndWait(); // Only closes the Add Customer dialog when done

    }

    /**
     * Loads the list of customers from the customer table in the database into a
     * map, mapping customer names to their IDs.
     *
     * @return a map of customer names and their corresponding customer IDs.
     */
    private Map<String, Integer> loadCustomersFromDatabase() {
        Map<String, Integer> customerMap = new HashMap<>();

        String query = "SELECT customer_id, first_name FROM customer"; // Adjust table and column names

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("first_name");
                customerMap.put(customerName, customerId); // Map customer name to ID
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customerMap;
    }

    /**
     * Loads the details of a customer from the database and fills the provided text
     * fields with the customer's data.
     *
     * @param customerId     the ID of the customer to load details for.
     * @param firstNameField the text field for the customer's first name.
     * @param lastNameField  the text field for the customer's last name.
     * @param emailField     the text field for the customer's email.
     * @param phoneField     the text field for the customer's phone number.
     */
    private void loadCustomerDetails(int customerId, TextField firstNameField, TextField lastNameField,
            TextField emailField, TextField phoneField) {
        String query = "SELECT first_name, last_name, email, phone FROM customer WHERE customer_id = ?";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId); // Set the customer ID
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                firstNameField.setText(rs.getString("first_name"));
                lastNameField.setText(rs.getString("last_name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a new customer to the database by inserting their details into the
     * customer table.
     *
     * @param firstName the first name of the new customer.
     * @param lastName  the last name of the new customer.
     * @param email     the email address of the new customer.
     * @param phone     the phone number of the new customer.
     */
    private void saveNewCustomer(String firstName, String lastName, String email, String phone) {
        String query = "INSERT INTO customer (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, phone);

            stmt.executeUpdate();
            System.out.println("New customer added: " + firstName + " " + lastName);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}