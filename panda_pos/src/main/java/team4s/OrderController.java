package team4s;

import javafx.fxml.FXML;
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

    // Method to display the employee selection pop-up
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

    // Method to load employee data from the database
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

    // Method to handle Bowl button click
    @FXML
    public void selectBowl() {
        selectPlate("Bowl");
    }

    // Method to handle Plate button click
    @FXML
    public void selectPlate() {
        selectPlate("Plate");
    }

    // Method to handle Bigger Plate button click
    @FXML
    public void selectBiggerPlate() {
        selectPlate("Bigger Plate");
    }

    // Method to handle Appetizer button click
    @FXML
    public void selectAppetizer() {
        resetButtonStyles();
        appetizerButton.setStyle("-fx-background-color: #ffcccc"); // Change background color
        selectedPlateType = ""; // Reset plate type
        loadItems("Appetizer");
    }

    // Method to handle Drinks button click
    @FXML
    public void selectDrinks() {
        resetButtonStyles();
        drinksButton.setStyle("-fx-background-color: #ffcccc"); // Change background color
        selectedPlateType = ""; // Reset plate type
        loadItems("Drink");
    }

    // Method to handle Side button click
    @FXML
    public void selectSide() {
        resetButtonStyles();
        sideButton.setStyle("-fx-background-color: #ffcccc"); // Change background color
        selectedPlateType = ""; // Reset plate type
        loadItems("Side");
    }

    // Method to handle Entree button click
    @FXML
    public void selectEntree() {
        resetButtonStyles();
        entreeButton.setStyle("-fx-background-color: #ffcccc"); // Change background color
        selectedPlateType = ""; // Reset plate type
        loadItems("Entree");
    }

    @FXML
    public void selectDisposables() {
        resetButtonStyles();
        disposablesButton.setStyle("-fx-background-color: #ffcccc"); // Change background color
        selectedPlateType = ""; // Reset plate type as it's not relevant for disposables
        loadDisposables(); // Load disposables from inventory
    }

    // Load disposable items from the inventory table
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
                disposableButton.setOnAction(event -> addToOrderDisposable(disposableName)); // Add disposable to the order
                dynamicButtons.getChildren().add(disposableButton);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add disposable item to the order and subtract servings from the inventory
    private void addToOrderDisposable(String disposableName) {
        String orderItem = "Disposable (" + disposableName + ")";
        currentOrder.add(orderItem);
        currentOrderList.getItems().add(orderItem);

        // Subtract from inventory for disposables
        updateInventoryServing(disposableName, 1); // Reduce the inventory count by 1 (or adjust based on required serving size)

        updateOrderTotal();
        resetSelections();
    }

    // Method to update the inventory by subtracting the number of servings for the specified item
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

    // Fetch menu_item_id from menu_item table based on item name
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
            showAlert("Insufficient Ingredients", "There are insufficient ingredients available to restock " + itemName);
            return;
        }

        restockServings(menuItemId);
        showAlert("Restock Successful", "Servings for " + itemName + " have been successfully restocked.");
    }
    

    // Check if ingredients are available to restock servings
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

    // Fetch available servings for a menu item
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


    // Check if sufficient ingredients are available in the inventory
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

    // Perform the restock operation by updating the servings and decrementing inventory
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

    // Decrement the ingredient stock in the inventory
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
    
    // Load side buttons from the database
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

    // Load all entree buttons based on the selected side
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
                entreeButton.setOnAction(event -> handleEntreeSelection(entreeButton, entreeName)); // Handle entree selection
                dynamicButtons.getChildren().add(entreeButton);
                entreeButtons.add(entreeButton); // Track this button
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



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

    // Method to change button color based on selection count
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

    // Adjusted addToOrder to handle multiple counts for entrees
    private void addToOrder(String side) {
        double platePrice = fetchPriceFromDatabase(selectedPlateType); // Fetch price for the plate type
        double sidePrice = fetchPriceFromDatabase(side); // Fetch price for the side
        
        StringBuilder orderItem = new StringBuilder(selectedPlateType + " (" + side);
        orderTotal += platePrice; // Add the plate price to the total
        orderTotal += sidePrice;  // Add the side price to the total
        
        // Add each entree with its count and update the total price
        for (Map.Entry<String, Integer> entry : selectedEntrees.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                double entreePrice = fetchPriceFromDatabase(entry.getKey()); // Fetch price for the entree
                orderItem.append(", ").append(entry.getKey()).append(" ($").append(String.format("%.2f", entreePrice)).append(")"); // Include price with entree
                orderTotal += entreePrice; // Add entree price to the total
            }
        }
        orderItem.append(")\n$").append(String.format("%.2f", platePrice + sidePrice)); // Display total price for plate and side
        
        currentOrder.add(orderItem.toString());
        currentOrderList.getItems().add(orderItem.toString()); // Add item with price
        
        updateOrderTotal(); // Update the total displayed in the UI
        
        // Reset selections
        resetSelections();
    }
    

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
    
    
    
    private void addToOrderDrink(String item) {
        double drinkPrice = fetchPriceFromDatabase(item); // Fetch price for the drink
        String orderItem = "Drink (" + item + ")\n$" + String.format("%.2f", drinkPrice);
        
        orderTotal += drinkPrice; // Update the total
        
        currentOrder.add(orderItem);
        currentOrderList.getItems().add(orderItem); // Add item with price
        
        updateOrderTotal(); // Update the total displayed in the UI
        
        resetSelections();
    }
    

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
    
    
    
    
    // Fetch the price of an item from the database
    private double fetchPriceFromDatabase(String itemName) {
        String query = "SELECT item_price FROM menu_item WHERE item_name = ?";
        try (Connection conn = Database.connect();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, itemName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double price = rs.getDouble("item_price");
                System.out.println("Price for " + itemName + ": " + price);  // Debugging price retrieval
                return price; // Return the price of the item
            } else {
                System.out.println("Item not found in database: " + itemName); // Debugging if item is missing
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Return 0 if the item is not found
    }
    
    

    private void updateOrderTotal() {
        orderTotalLabel.setText(String.format("Total: $%.2f", orderTotal));
    }
    
    
    
    

    // Reset selections for all items
    private void resetSelections() {
        selectedEntrees.clear();
        selectedSide = "";
        dynamicButtons.getChildren().clear();
        resetButtonStyles();
    }
    
    // Load items (sides or entrees) based on category
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
    

    // Confirm order
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

    // Delete selected item from the order
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
            totalPrice+= fetchPriceFromDatabase(sideName);
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


    // Edit selected item from the order
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

    // Show a selection dialog based on item type
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

    // Update method for appetizers
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


    // Update method for drinks
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




    // Show a pop-up alert dialog
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Show a selection dialog based on item type
    private void showSelectionDialog(String type, String itemType, int maxEntrees, String preFillSide, List<String> preFillEntrees) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit " + selectedPlateType);  // Use selectedPlateType here
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

    // Update the current order at the selected index instead of adding a new one
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

    // Method to load items into ComboBox
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

    @FXML
    private void increaseItemQuantity() {
        String selectedItem = currentOrderList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            final int[] currentQuantity = {1}; // Use an array to store currentQuantity
            String itemName;
            final double[] itemPrice = {0.0}; // Use an array to store itemPrice

            // Check if the selected item contains a quantity indicator (e.g., " x2")
            if (selectedItem.contains("x")) {
                try {
                    int quantityIndex = selectedItem.lastIndexOf("x");
                    itemName = selectedItem.substring(0, quantityIndex).trim(); // Get the item name
                    currentQuantity[0] = Integer.parseInt(selectedItem.substring(quantityIndex + 1).trim()); // Extract quantity
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
            TextInputDialog quantityDialog = new TextInputDialog(String.valueOf(currentQuantity[0])); // Pre-fill with current quantity
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
                    String updatedItem = itemName + " x" + newQuantity + "\n$" + String.format("%.2f", updatedTotalPrice);

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
        dialogPaneContent.getChildren().addAll(orderLabel, orderListView, totalLabel, taxLabel, totalWithTaxLabel, paymentTypeLabel, paymentTypeComboBox);
    
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
    

    // Extract the item quantity from the order string (e.g., "Entree (Chicken) x2")
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


    private void placeOrderInDatabase(double totalWithTax, String paymentType) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        int transactionId = -1;
    
        try {
            conn = Database.connect(); // Open a connection to the database
            conn.setAutoCommit(false);  // Turn off auto-commit for transaction handling
    
            // Step 1: Insert the transaction into the "transaction" table with the selected payment type
            String transactionQuery = "INSERT INTO transaction (total_cost, transaction_time, transaction_date, transaction_type, customer_id, employee_id, week_number) VALUES (?, CURRENT_TIME, CURRENT_DATE, ?, ?, ?, 40)";
            stmt = conn.prepareStatement(transactionQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setDouble(1, totalWithTax);  // Set total cost with tax
            stmt.setString(2, paymentType);  // Set the selected payment type
            stmt.setInt(4, selectedEmployeeId);  // Set the employee ID
    
            // Check if a customer is selected, set the customer_id to NULL if not selected
            if (selectedCustomerId == 0) {
                stmt.setNull(3, java.sql.Types.INTEGER);  // Set customer_id to NULL
            } else {
                stmt.setInt(3, selectedCustomerId);  // Set the customer ID
            }
    
            stmt.executeUpdate();
    
            // Get the generated transaction ID
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                transactionId = generatedKeys.getInt(1);  // Store the transaction ID
            }
    
            // Step 2: Insert each item from the current order into the "menu_item_transaction" table
            if (transactionId != -1) {
                String itemQuery = "INSERT INTO menu_item_transaction (menu_item_id, transaction_id, item_quantity) VALUES (?, ?, ?)";
                stmt = conn.prepareStatement(itemQuery);
    
                for (String orderItem : currentOrder) {
                    String itemName = extractItemName(orderItem);  // Extract the item name from the order
                    int quantity = extractItemQuantity(orderItem);  // Get the item quantity
    
                    // Determine if the item is disposable or a menu item
                    if (orderItem.startsWith("Disposable")) {
                        // Subtract from the inventory for disposables
                        updateInventoryServing(itemName, quantity);
                    } else {
                        // Subtract from the menu_item table for menu items
                        int menuItemId = fetchMenuItemId(itemName);  // Get the menu item ID from the database
                        stmt.setInt(1, menuItemId);  // Set menu item ID
                        stmt.setInt(2, transactionId);  // Set transaction ID
                        stmt.setInt(3, quantity);  // Set item quantity
                        stmt.addBatch();  // Add this item to the batch
    
                        // Subtract servings from menu_item table for non-disposable items
                        updateMenuItemServing(itemName, quantity);
                    }
                }
                stmt.executeBatch();  // Execute all the items in the batch
            }
    
            conn.commit();  // Commit the transaction if everything is successful
    
            // Reset the order after successful insertion
            currentOrder.clear();
            currentOrderList.getItems().clear();
            orderTotal = 0.0;
            updateOrderTotal();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();  // Rollback transaction on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            // Close resources
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    
    // Method to update the menu_item table by subtracting the number of servings for the specified item
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


    // Extract the item name from the order string
    private String extractItemName(String orderItem) {
        return orderItem.substring(orderItem.indexOf("(") + 1, orderItem.indexOf(")"));  // Adjust parsing logic based on your format
    }

    // Fetch menu item ID from the menu_item table
    private int fetchMenuItemId(String itemName) {
        String query = "SELECT menu_item_id FROM menu_item WHERE item_name = ?";
        try (Connection conn = Database.connect();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, itemName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("menu_item_id");  // Return the ID of the menu item
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // Return -1 if item is not found
    }

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
    
        dialogPaneContent.getChildren().addAll(existingCustomerLabel, customerComboBox, firstNameLabel, firstNameField, lastNameLabel, lastNameField, emailLabel, emailField, phoneLabel, phoneField, clearButton);
    
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
                    saveNewCustomer(firstNameField.getText(), lastNameField.getText(), emailField.getText(), phoneField.getText());
                    customerLabel.setText(String.format("Selected Customer: " +  customerFullName));
                } else {
                    // Existing customer selected; update the label in the main dialog
                    customerFullName = firstNameField.getText() + " " + lastNameField.getText();
                    customerLabel.setText(String.format("Selected Customer: " +  customerFullName));
                }
            }
            return null; // Close only the customer dialog
        });
    
        dialog.showAndWait(); // Only closes the Add Customer dialog when done

    }
    
    // Method to load customers from the database into the ComboBox
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
    
    // Method to load selected customer details into the fields
    private void loadCustomerDetails(int customerId, TextField firstNameField, TextField lastNameField, TextField emailField, TextField phoneField) {
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
    
    // Method to save a new customer to the database
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