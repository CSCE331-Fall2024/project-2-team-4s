package team4s;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Parent;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import team4s.Database; // Add this line to import the Database class

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.event.ActionEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import java.time.LocalDate;
import java.time.LocalTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.scene.input.InputMethodRequests;

/*
 *  This code has three main parts:
 * 1. A list storing the type of order (bowl, plate, bigger plate, appetizer, side, entree, drink), 
 *  which will be appended to a list of list in ID and name form for the checkout screen and a list of ID for the transaction table
 * 2. Buttons for each type of order, which will be disabled when the order is selected and enabled when the order is cancelled
 * several variables to keep track of the quantity of each item and for protection against invalid orders, should all be some variation of _num
 * 3. Buttons to update the lists and change the scene to the checkout screen
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */

public class OrderController {
    // Initialization-----------------------------------------------------------



    List<Integer> order = new ArrayList<Integer>();
    List<List<Integer>> orders = new ArrayList<List<Integer>>();
    List<String> order_names = new ArrayList<String>();
    List<List<String>> orders_names = new ArrayList<List<String>>();
    @FXML 
    private Button bowl;
    @FXML 
    private Button plate;
    @FXML 
    private Button bigger_plate;
    @FXML 
    private Button appetizer;
    @FXML
    private Button side;
    @FXML 
    private Button entree;
    @FXML 
    private Button drink;
    @FXML 
    private Button sides;
    @FXML 
    private Button entrees;
    @FXML 
    private Button appetizers;
    @FXML 
    private Button drinks;
    @FXML 
    private Button up;
    @FXML 
    private Button down;
    @FXML 
    private Button add_1;
    @FXML 
    private Button add_2;
    @FXML 
    private Button add_3;
    @FXML 
    private Button add_4;
    @FXML 
    private Button redo;
    @FXML 
    private Button redo_last_order;
    @FXML 
    private Button confirm;
    @FXML 
    private Button checkout;
    @FXML 
    private Button previous_screen;
    @FXML
    private ListView menu_items_display;
    @FXML
    private List<String> current_display = new ArrayList<String>();
    private List<Integer> ids = new ArrayList<Integer>();
    @FXML
    private Label current_order;
    private int sides_num = 0;
    private int entrees_num = 0;
    private int appetizers_num = 0;
    private int drinks_num = 0;
    private int offsetter = 0;
    private String tagger;
    private int type_of_order = 0;
//update appetizer_num and stuff, use tagger in add()
    private void updateNums(String name){
        if (name == "Appetizer"){
            System.out.println("appetizer num: " + appetizers_num);
            appetizers_num -= 1;
        }
        else if (name == "Side"){
            sides_num -= 1;
        }
        else if (name == "Entree"){
            entrees_num -= 1;
        }
        else if (name == "Drink"){
            drinks_num -= 1;
        }

    }
    private Integer getNums(String name){
        if (name == "Appetizer"){
            return appetizers_num;
        }
        else if (name == "Side"){
            return sides_num;
        }
        else if (name == "Entree"){
            return entrees_num;
        }
        else {
            return drinks_num;
        }
    }
    public void loadItems(String item_category, int offset) {
        current_display.clear();
        ids.clear();
        System.out.println("Loading items from the database.");
        String selectQuery = "SELECT item_name, menu_item_id FROM menu_item WHERE item_category = ? LIMIT 4 OFFSET ?";
        ObservableList<String> items = FXCollections.observableArrayList();
        ObservableList<Integer> ids1 = FXCollections.observableArrayList(); // Initialize the ids list
        
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
        
            stmt.setString(1, item_category);  // Set the item_category parameter
            stmt.setInt(2, offset);  // Set the offset parameter
            ResultSet rs = stmt.executeQuery();
        
            while (rs.next()) {
                String itemName = rs.getString("item_name");
                Integer id = rs.getInt("menu_item_id");
                System.out.println("Retrieved item: " + itemName + " with ID: " + id); // Debug print statement
                items.add(itemName);
                ids1.add(id);  // Add the ID to the ids list
        
                current_display.add(itemName);
                ids.add(id);
            }
        
            System.out.println("IDs: " + ids); // Print the ids list to verify
            menu_items_display.setItems(items);
            System.out.println("Items: " + items); // Print the items list to verify
        
        } catch (SQLException e) {
            System.err.println("Failed to load items from the database.");
            e.printStackTrace();
        }
    
}


        public void bowl(ActionEvent event){
        cancel();
        if (type_of_order == 0){
            order.add(17);
            order_names.add("Bowl");
            current_order.setText("Current Order: " + order_names);
            bowl.setStyle("-fx-background-color: #ff0000");
            sides_num = 1;
            entrees_num = 1;
            appetizers_num = 0;
            drinks_num = 0;
            type_of_order = 1;
            sides.setDisable(false);
            entrees.setDisable(false);
            appetizers.setDisable(true);
            drinks.setDisable(true);
        }
        else{
            order.remove(0);
            order_names.remove(0);
            current_order.setText("Current Order: " + order_names);
            bowl.setStyle(null);
            sides_num = 0;
            entrees_num = 0;
            appetizers_num = 0;
            drinks_num = 0;
            type_of_order = 0;
            sides.setDisable(false);
            entrees.setDisable(false);
            appetizers.setDisable(false);
            drinks.setDisable(false);
        }
    }
    public void plate(ActionEvent event){
        cancel();
    if (type_of_order == 0){
        order.add(18);
        order_names.add("Plate");
        current_order.setText("Current Order: " + order_names);
        plate.setStyle("-fx-background-color: #ff0000");
        sides_num = 1;
        entrees_num = 2;
        appetizers_num = 0;
        drinks_num = 0;
        type_of_order = 1;
        sides.setDisable(false);
        entrees.setDisable(false);
        appetizers.setDisable(true);
        drinks.setDisable(true);
    }
    else{
        order.remove(0);
        order_names.remove(0);
        current_order.setText("Current Order: " + order_names);
        plate.setStyle(null);
        sides_num = 0;
        entrees_num = 0;
        appetizers_num = 0;
        drinks_num = 0;
        type_of_order = 0;
        sides.setDisable(false);
        entrees.setDisable(false);
        appetizers.setDisable(false);
        drinks.setDisable(false);
    }
}
    public void bigger_plate(ActionEvent event){
            cancel();
    if (type_of_order == 0){
        order.add(19);
        order_names.add("Bigger Plate");
        current_order.setText("Current Order: " + order_names);
        bigger_plate.setStyle("-fx-background-color: #ff0000");
        sides_num = 1;
        entrees_num = 3;
        appetizers_num = 0;
        drinks_num = 0;
        type_of_order = 1;
        sides.setDisable(false);
        entrees.setDisable(false);
        appetizers.setDisable(true);
        drinks.setDisable(true);
    }
    else{
        order.remove(0);
        order_names.remove(0);
        current_order.setText("Current Order: " + order_names);
        bigger_plate.setStyle(null);
        sides_num = 0;
        entrees_num = 0;
        appetizers_num = 0;
        drinks_num = 0;
        type_of_order = 0;
        sides.setDisable(false);
        entrees.setDisable(false);
        appetizers.setDisable(false);
        drinks.setDisable(false);
    }
}
    public void appetizer(ActionEvent event){
    cancel();
    if (type_of_order == 0){
        System.out.println("appetizer button clicked");
        order.add(20);
        order_names.add("Appetizer");
        current_order.setText("Current Order: " + order_names);
        appetizer.setStyle("-fx-background-color: #ff0000");
        appetizers_num = 1;
        sides.setDisable(true);
        entrees.setDisable(true);
        appetizers.setDisable(false);
        drinks.setDisable(true);
        drinks_num = 0;
        type_of_order = 1;
    }
    else{
        order.remove(1);
        order_names.remove(1);
        current_order.setText("Current Order: " + order_names);
        appetizer.setStyle(null);
        appetizers_num = 0;
        sides.setDisable(false);
        entrees.setDisable(false);
        appetizers.setDisable(false);
        drinks.setDisable(false);
        drinks_num = 0;
        type_of_order = 0;
    }
}
    public void side(ActionEvent event){
            cancel();
    if (type_of_order == 0){
        order.add(21);
        order_names.add("Side");
        current_order.setText("Current Order: " + order_names);
        side.setStyle("-fx-background-color: #ff0000");
        sides_num = 1;
        sides.setDisable(false);
        entrees.setDisable(true);
        appetizers.setDisable(true);
        drinks.setDisable(true);
        entrees_num = 0;
        appetizers_num = 0;
        drinks_num = 0;
        type_of_order = 1;
    }
    else{
        order.remove(1);
        order_names.remove(1);
        current_order.setText("Current Order: " + order_names);
        side.setStyle(null);
        sides_num = 0;
        sides.setDisable(false);
        entrees.setDisable(false);
        appetizers.setDisable(false);
        drinks.setDisable(false);
        entrees_num = 0;
        appetizers_num = 0;
        drinks_num = 0;
        type_of_order = 0;
    }
}
    public void entree(ActionEvent event){
            cancel();
    if (type_of_order == 0){
        order.add(22);
        order_names.add("Entree");
        current_order.setText("Current Order: " + order_names);
        entree.setStyle("-fx-background-color: #ff0000");
        entrees_num = 1;
        sides.setDisable(true);
        entrees.setDisable(false);
        appetizers.setDisable(true);
        drinks.setDisable(true);
        sides_num = 0;
        appetizers_num = 0;
        drinks_num = 0;
        type_of_order = 1;
    }
    else{
        order.remove(1);
        order_names.remove(1);
        current_order.setText("Current Order: " + order_names);
        entree.setStyle(null);
        entrees_num = 0;
        sides.setDisable(false);
        entrees.setDisable(false);
        appetizers.setDisable(false);
        drinks.setDisable(false);
        sides_num = 0;
        appetizers_num = 0;
        drinks_num = 0;
        type_of_order = 0;
    }
}
    public void drink(ActionEvent event){
            cancel();
    if (type_of_order == 0){
        order_names.add("Drink");   
        order.add(23);
        current_order.setText("Current Order: " + order_names);
        drink.setStyle("-fx-background-color: #ff0000");
        drinks_num = 1;
        sides.setDisable(true);
        entrees.setDisable(true);
        appetizers.setDisable(true);
        drinks.setDisable(false);
        sides_num = 0;
        entrees_num = 0;
        appetizers_num = 0;
        type_of_order = 1;
    }
    else{
        order.remove(1);
        order_names.remove(1);
        current_order.setText("Current Order: " + order_names);
        drink.setStyle(null);
        drinks_num = 0;
        sides.setDisable(false);
        entrees.setDisable(false);
        appetizers.setDisable(false);
        drinks.setDisable(false);
        sides_num = 0;
        entrees_num = 0;
        appetizers_num = 0;
        type_of_order = 0;
    }
}
//--------------------------------------------------------
    public void sides(ActionEvent event){
        tagger = "Side";
        offsetter = 0;
        loadItems(tagger, offsetter);
    }
    public void entrees(ActionEvent event){
        tagger = "Entree";
        offsetter = 0;
        loadItems(tagger, offsetter);
    }
    public void appetizers(ActionEvent event){
        tagger = "Appetizer";
        offsetter = 0;
        loadItems(tagger, offsetter);
    }
    public void drinks(ActionEvent event){
        tagger = "Drink";
        offsetter = 0;
        loadItems(tagger, offsetter);
    }
    public void up(ActionEvent event){
        System.out.println("up button clicked");
        if (offsetter > 0){
            offsetter -= 4;
            loadItems(tagger, offsetter);
            current_display.clear();
            ids.clear();
        }

    }
    public void down(ActionEvent event){
        System.out.println("down button clicked");
        offsetter += 4;
        current_display.clear();
        ids.clear();
        loadItems(tagger, offsetter);
    }
    //--------------------------------------------------------------------------------
    
    private void cancel(){
        System.out.println("cancel button clicked");
        bowl.setStyle(null);
        plate.setStyle(null);
        bigger_plate.setStyle(null);
        appetizer.setStyle(null);
        side.setStyle(null);
        entree.setStyle(null);
        drink.setStyle(null);
        sides_num = 0;
        entrees_num = 0;
        appetizers_num = 0;
        drinks_num = 0;
        type_of_order = 0;
        sides.setDisable(true);
        entrees.setDisable(true);
        appetizers.setDisable(true);
        drinks.setDisable(true);
        current_order.setText("Current Order: ");
        order_names.clear();

    }
    @FXML
    private void redo(ActionEvent event){
        cancel();
        order.clear();
        order_names.clear();

    }
    @FXML
    private void redo_last_order(ActionEvent event){
        System.out.println("Redo Last Order button clicked");
        if (orders.size() > 0){
            cancel();
        orders.remove(orders.size()-1);
        orders_names.remove(orders_names.size()-1);

        
        }

    }

    @FXML
    private void confirm(ActionEvent event){ //checks if order is valid and add it to the grand list of things i will be sending over to the checkout screen, if it is not valid, show what is missing
        System.out.println(appetizers_num);
        System.out.println(sides_num);
        System.out.println(entrees_num);
        System.out.println(drinks_num);
        System.out.println(type_of_order);
        menu_items_display.setItems(null);
        if (appetizers_num == 0 && sides_num == 0 && entrees_num == 0 && drinks_num == 0  && type_of_order == 1){
            System.out.println(order_names);
            
            System.out.println("confirm button clicked1");
            orders.add(new ArrayList<>(order)); //list are mutable so add a copy
            orders_names.add(new ArrayList<>(order_names));
            for (List<Integer> order : orders) {
                System.out.println(order);
            }
            System.out.println(orders);
            System.out.println(orders_names);
            order.clear();
            order_names.clear();
            cancel();
        }
        else if (appetizers_num > 0 || sides_num > 0 || entrees_num > 0 || drinks_num > 0){
            showPopupMessage("Please select the correct amount of items: \n"+appetizers_num+" appetizers,\n"+sides_num+" sides,\n"+entrees_num+" entrees, \n"+drinks_num+" drinks");
            System.out.println("Please select the correct amount of items: "+appetizers_num+" appetizers, "+sides_num+" sides, "+entrees_num+" entrees, "+drinks_num+" drinks");
        }
        else{
            System.out.println("confirm button clicked");
}

    }
    @FXML
    private void previous_screen(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Menu.fxml"));
            Parent root = loader.load();

            // Get the controller and cast to the correct type
            MenuController menuController = loader.getController();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        private void showPopupMessage(String message) { //popup message for bad orders
        // Create a new Stage (window)
        Stage popupStage = new Stage();
        popupStage.setAlwaysOnTop(true);

        // Create a Label to display the message
        Label messageLabel = new Label(message);

        // Create a layout and add the Label to it
        StackPane layout = new StackPane(messageLabel);
        Scene scene = new Scene(layout, 300, 100);

        // Set the scene to the Stage
        popupStage.setScene(scene);

        // Show the Stage
        popupStage.show();

        // Create a Timeline to hide the Stage after 1 second
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2.5), e -> popupStage.hide()));
        timeline.play();
    }

    public List<List<String>> getOrderNames() {
        return orders_names;
    }
    public void checkout(ActionEvent event) { //switches scene to checkout
        cancel();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Checkout.fxml"));
            Parent root = loader.load();

            // Get the controller and set the variable
            CheckoutController checkoutController = loader.getController();
            checkoutController.setOrderNames(orders_names);
            checkoutController.setOrder(orders);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void initialize() {
        // Your logic for initializing the controller
        drinks.setDisable(true);
        sides.setDisable(true);
        entrees.setDisable(true);
        appetizers.setDisable(true);
        current_order.setText("Current Order: ");
    }
    @FXML 
    private void add_1(ActionEvent event){
        System.out.println("works add 1");
        

        if (getNums(tagger) > 0){
            updateNums(tagger);
            order_names.add(current_display.get(0));
            order.add(ids.get(0));
            System.out.println(order_names);
            current_order.setText("Current Order:\n" + order_names);
            System.out.println(order);
        }
        }
    
    @FXML
    private void add_2(ActionEvent event){
        if (getNums(tagger) > 0){
            updateNums(tagger);
            order_names.add(current_display.get(1));
            order.add(ids.get(1));
            System.out.println(order_names);
            current_order.setText("Current Order:\n" + order_names);
            System.out.println(order);
        }
    }
    @FXML
    private void add_3(ActionEvent event){
        if (getNums(tagger) > 0){
            updateNums(tagger);
            order_names.add(current_display.get(2));
            order.add(ids.get(2));
            System.out.println(order_names);
            current_order.setText("Current Order:\n" + order_names);
            System.out.println(order);
        }
    }
    @FXML
    private void add_4(ActionEvent event){
        if (getNums(tagger) > 0){
            updateNums(tagger);
            order_names.add(current_display.get(3));
            order.add(ids.get(3));
            System.out.println(order_names);
            current_order.setText("Current Order:\n" + order_names);
            System.out.println(order);
        }
}



}
