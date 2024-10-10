package team4s;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class OrderController {
    List<Integer> order = new ArrayList<Integer>();
    List<List<Integer>> orders = new ArrayList<List<Integer>>();

    @FXML
    private Button appetizer;

    @FXML
    private Button apple_pie_roll;

    @FXML
    private Button beijing_beef;

    @FXML
    private Button bigger_plate;

    @FXML
    private Button black_pepper_chicken;

    @FXML
    private Button bottled_drink;

    @FXML
    private Button bowl;

    @FXML
    private Button broccoli_beef;

    @FXML
    private Button chicken_eggrolls;

    @FXML
    private Button chow_mein;

    @FXML
    private Button entree;

    @FXML
    private Button fountain_drink;

    @FXML
    private Button fried_rice;

    @FXML
    private Button mushroom_chicken;

    @FXML
    private Button orange_chicken;

    @FXML
    private Button plate;

    @FXML
    private Button rangoons;

    @FXML
    private Button side;
    @FXML
    private Button drink;

    @FXML
    private Button super_greens;

    @FXML
    private Button teriyaki_chicken;

    @FXML
    private Button vegetable_eggrolls;

    @FXML
    private Button white_rice;
    
    @FXML
    private Button redo_last_order;
    @FXML
    private Button checkout;
    @FXML
    private Button confirm;
    @FXML
    private HBox sides_hbox;
    @FXML
    private HBox entree_hbox;
    @FXML
    private HBox appetizers_hbox;
    @FXML
    private HBox drink_hbox;
    @FXML
    private Button redo;

    @FXML
    private HBox control_hbox;

    private int appetizer_num = 0;
    private int sides_num = 0;
    private int entree_num = 0;
    private int drink_num = 0;
    private int type_of_order = 0;
    private int white_rice_quantity = 0;
    private int super_greens_quantity = 0;
    private int fried_rice_quantity = 0;
    private int chow_mein_quantity = 0;
    private int black_pepper_chicken_quantity = 0;
    private int broccoli_beef_quantity = 0;
    private int teriyaki_chicken_quantity = 0;
    private int mushroom_chicken_quantity = 0;
    private int beijing_beef_quantity = 0;
    private int orange_chicken_quantity = 0;
    private int chicken_eggrolls_quantity = 0;
    private int rangoons_quantity = 0;
    private int apple_pie_roll_quantity = 0;
    private int vegetable_eggrolls_quantity = 0;
    private int fountain_drink_quantity = 0;
    private int bottled_drink_quantity = 0;

    private void cancel(){
        appetizer_num = 0;
        sides_num = 0;
        entree_num = 0;
        drink_num = 0;
        type_of_order = 0;
        bowl.setDisable(false);
        plate.setDisable(false);
        bigger_plate.setDisable(false);
        appetizer.setDisable(false);
        side.setDisable(false);
        entree.setDisable(false);
        bowl.setStyle(null);
        plate.setStyle(null);
        bigger_plate.setStyle(null);
        appetizer.setStyle(null);
        side.setStyle(null);
        entree.setStyle(null);
        sides_hbox.setDisable(true);
        entree_hbox.setDisable(true);
        appetizers_hbox.setDisable(true);
        drink_hbox.setDisable(true);
        control_hbox.setDisable(false);
        white_rice_quantity = 0;
        super_greens_quantity = 0;
        fried_rice_quantity = 0;
        chow_mein_quantity = 0;
        black_pepper_chicken_quantity = 0;
        broccoli_beef_quantity = 0;
        teriyaki_chicken_quantity = 0;
        mushroom_chicken_quantity = 0;
        beijing_beef_quantity = 0;
        orange_chicken_quantity = 0;
        chicken_eggrolls_quantity = 0;
        rangoons_quantity = 0;
        apple_pie_roll_quantity = 0;
        vegetable_eggrolls_quantity = 0;
        fountain_drink_quantity = 0;
        bottled_drink_quantity = 0;
        white_rice.setText("White Rice x"+white_rice_quantity);
        super_greens.setText("Super Greens x"+super_greens_quantity);
        fried_rice.setText("Fried Rice x"+fried_rice_quantity);
        chow_mein.setText("Chow Mein x"+chow_mein_quantity);
        black_pepper_chicken.setText("Black Pepper Chicken x"+black_pepper_chicken_quantity);
        broccoli_beef.setText("Broccoli Beef x"+broccoli_beef_quantity);
        teriyaki_chicken.setText("Teriyaki Chicken x"+teriyaki_chicken_quantity);
        mushroom_chicken.setText("Mushroom Chicken x"+mushroom_chicken_quantity);
        beijing_beef.setText("Beijing Beef x"+beijing_beef_quantity);
        orange_chicken.setText("Orange Chicken x"+orange_chicken_quantity);
        chicken_eggrolls.setText("Chicken Eggrolls x"+chicken_eggrolls_quantity);
        rangoons.setText("Rangoons x"+rangoons_quantity);
        apple_pie_roll.setText("Apple Pie Roll x"+apple_pie_roll_quantity);
        vegetable_eggrolls.setText("Vegetable Eggrolls x"+vegetable_eggrolls_quantity);
        fountain_drink.setText("Fountain Drink x"+fountain_drink_quantity);
        bottled_drink.setText("Bottled Drink x"+bottled_drink_quantity);

    }
    @FXML
    private void initialize(){
        cancel();
    }
    @FXML
    private void confirm(ActionEvent event){
        System.out.println("confirm button clicked");
        if (appetizer_num == 0 && sides_num == 0 && entree_num == 0 && drink_num == 0){
            
            System.out.println("confirm button clicked1");
            orders.add(new ArrayList<>(order)); //list are mutable so add a copy
            for (List<Integer> order : orders) {
                System.out.println(order);
            }
            System.out.println(orders);
            order.clear();
            cancel();
        }
        if (appetizer_num > 0 || sides_num > 0 || entree_num > 0 || drink_num > 0){
            showPopupMessage("Please select the correct amount of items: \n"+appetizer_num+" appetizers,\n"+sides_num+" sides,\n"+entree_num+" entrees, \n"+drink_num+" drinks");
            System.out.println("Please select the correct amount of items: "+appetizer_num+" appetizers, "+sides_num+" sides, "+entree_num+" entrees, "+drink_num+" drinks");
        }


    }
        private void showPopupMessage(String message) {
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
    @FXML
    private void checkout(ActionEvent event){
        System.out.println("checkout button clicked");
    }

    @FXML
    private void bowl(ActionEvent event) {
        if (type_of_order == 0) {
            type_of_order = 1;
            sides_num = 1;
            entree_num = 1;
            order.add(17);
            bowl.setStyle("-fx-background-color: #ff0000");
            plate.setDisable(true);
            bigger_plate.setDisable(true);
            appetizer.setDisable(true);
            side.setDisable(true);
            entree.setDisable(true);
            sides_hbox.setDisable(false);
            entree_hbox.setDisable(false);
            control_hbox.setDisable(false);
        }
        else {
            cancel();
            bowl.setStyle(null);
            order.remove(order.size()-1);
        }
        System.out.println("Bowl button clicked");
    }

    @FXML
    private void plate(ActionEvent event) {
        if (type_of_order == 0) {
            type_of_order = 1;
            sides_num = 1;
            entree_num = 2;
            order.add(18);
            plate.setStyle("-fx-background-color: #ff0000");
            bowl.setDisable(true);
            bigger_plate.setDisable(true);
            appetizer.setDisable(true);
            side.setDisable(true);
            entree.setDisable(true);
            sides_hbox.setDisable(false);
            entree_hbox.setDisable(false);
        }
        else{
            cancel();
            plate.setStyle(null);
            order.remove(order.size()-1);
        }
        System.out.println("Plate button clicked");
    }

    @FXML
    private void bigger_plate(ActionEvent event) {
        if (type_of_order == 0) {
            type_of_order = 1;
            sides_num = 1;
            entree_num = 3;
            order.add(19);
            bowl.setDisable(true);
            plate.setDisable(true);
            appetizer.setDisable(true);
            side.setDisable(true);
            entree.setDisable(true);
            bigger_plate.setStyle("-fx-background-color: #ff0000");
            sides_hbox.setDisable(false);
            entree_hbox.setDisable(false);
        }
        else{
            cancel();
            bigger_plate.setStyle(null);
            order.remove(order.size()-1);
        }
        System.out.println("Bigger Plate button clicked");
    }

    @FXML
    private void appetizer(ActionEvent event) {
        if (type_of_order == 0) {
            type_of_order = 1;
            appetizer_num = 1;
            order.add(20);
            bowl.setDisable(true);
            plate.setDisable(true);
            bigger_plate.setDisable(true);
            side.setDisable(true);
            entree.setDisable(true);
            appetizer.setStyle("-fx-background-color: #ff0000");
            appetizers_hbox.setDisable(false);
        }
        else{
            cancel();
            appetizer.setStyle(null);
            order.remove(order.size()-1);
        }
        System.out.println("Appetizer button clicked");
    }

    @FXML
    private void side(ActionEvent event) {
        if (type_of_order == 0) {
            type_of_order = 1;
            sides_num = 1;
            order.add(21);
            bowl.setDisable(true);
            plate.setDisable(true);
            bigger_plate.setDisable(true);
            appetizer.setDisable(true);
            entree.setDisable(true);
            side.setStyle("-fx-background-color: #ff0000");
            sides_hbox.setDisable(false);
        }
        else{
            cancel();
            side.setStyle(null);
            order.remove(order.size()-1);
        }
        System.out.println("Side button clicked");
    }

    @FXML
    private void entree(ActionEvent event) {
        if (type_of_order == 0) {
            type_of_order = 1;
            entree_num = 1;
            order.add(22);
            bowl.setDisable(true);
            plate.setDisable(true);
            bigger_plate.setDisable(true);
            appetizer.setDisable(true);
            side.setDisable(true);
            entree.setStyle("-fx-background-color: #ff0000");
            entree_hbox.setDisable(false);

        }
        else{
            cancel();
            entree.setStyle(null);
            order.remove(order.size()-1);
        }
        System.out.println("Entree button clicked");
    }
    @FXML
    private void drink(ActionEvent event) {
        if (type_of_order == 0) {
            type_of_order = 1;
            drink_num = 1;
            order.add(23);
            bowl.setDisable(true);
            plate.setDisable(true);
            bigger_plate.setDisable(true);
            appetizer.setDisable(true);
            side.setDisable(true);
            entree.setDisable(true);
            drink.setStyle("-fx-background-color: #ff0000");
            drink_hbox.setDisable(false);
        }
        else{
            cancel();
            drink.setStyle(null);
            order.remove(order.size()-1);
        }
        System.out.println("Drink button clicked");
    }
//-------------------Sides-------------------
    @FXML
    private void white_rice(ActionEvent event) {
        if (sides_num >0){
            sides_num--;
            order.add(1);
            white_rice_quantity++;
            white_rice.setText("White Rice x"+white_rice_quantity);
            System.out.println("White Rice button clicked");
        }

    }

    @FXML
    private void super_greens(ActionEvent event) {
        if (sides_num >0){
            sides_num--;
            order.add(2);
            super_greens_quantity++;
            super_greens.setText("Super Greens x"+super_greens_quantity);
            System.out.println("Super Greens button clicked");
        }
        System.out.println("Super Greens button clicked");
    }

    @FXML
    private void fried_rice(ActionEvent event) {
        if (sides_num >0){
            sides_num--;
            order.add(3);
            fried_rice_quantity++;
            fried_rice.setText("Fried Rice x"+fried_rice_quantity);
            System.out.println("Fried Rice button clicked");
        }
        System.out.println("Fried Rice button clicked");
    }

    @FXML
    private void chow_mein(ActionEvent event) {
        if (sides_num >0){
            sides_num--;
            order.add(4);
            chow_mein_quantity++;
            chow_mein.setText("Chow Mein x"+chow_mein_quantity);
            System.out.println("Chow Mein button clicked");
        }
        System.out.println("Chow Mein button clicked");
    }
    //-------------------Entree-------------------

    @FXML
    private void black_pepper_chicken(ActionEvent event) {
        if (entree_num >0){
            entree_num--;
            order.add(5);
            black_pepper_chicken_quantity++;
            black_pepper_chicken.setText("Black Pepper Chicken x"+black_pepper_chicken_quantity);
            System.out.println("Black Pepper Chicken button clicked");
        }
        System.out.println("Black Pepper Chicken button clicked");
    }

    @FXML
    private void broccoli_beef(ActionEvent event) {
        if (entree_num >0){
            entree_num--;
            order.add(6);
            broccoli_beef_quantity++;
            broccoli_beef.setText("Broccoli Beef x"+broccoli_beef_quantity);
            System.out.println("Broccoli Beef button clicked");
        }
        System.out.println("Broccoli Beef button clicked");
    }

    @FXML
    private void teriyaki_chicken(ActionEvent event) {
        if (entree_num >0){
            entree_num--;
            order.add(7);
            teriyaki_chicken_quantity++;
            teriyaki_chicken.setText("Teriyaki Chicken x"+teriyaki_chicken_quantity);
            System.out.println("Teriyaki Chicken button clicked");
        }
        System.out.println("Teriyaki Chicken button clicked");
    }

    @FXML
    private void mushroom_chicken(ActionEvent event) {
        if (entree_num >0){
            entree_num--;
            order.add(8);
            mushroom_chicken_quantity++;
            mushroom_chicken.setText("Mushroom Chicken x"+mushroom_chicken_quantity);
            System.out.println("Mushroom Chicken button clicked");
        }
        System.out.println("Mushroom Chicken button clicked");
    }

    @FXML
    private void beijing_beef(ActionEvent event) {
        if (entree_num >0){
            entree_num--;
            order.add(9);
            beijing_beef_quantity++;
            beijing_beef.setText("Beijing Beef x"+beijing_beef_quantity);
            System.out.println("Beijing Beef button clicked");
        }
        System.out.println("Beijing Beef button clicked");
    }

    @FXML
    private void orange_chicken(ActionEvent event) {
        if (entree_num >0){
            entree_num--;
            order.add(10);
            orange_chicken_quantity++;
            orange_chicken.setText("Orange Chicken x"+orange_chicken_quantity);
            System.out.println("Orange Chicken button clicked");
        }
        System.out.println("Orange Chicken button clicked");
    }
    //-------------------Appetizers-------------------
    @FXML
    private void chicken_eggrolls(ActionEvent event) {
        if (appetizer_num >0){
            appetizer_num--;
            order.add(11);
            chicken_eggrolls_quantity++;
            chicken_eggrolls.setText("Chicken Eggrolls x"+chicken_eggrolls_quantity);
            System.out.println("Chicken Eggrolls button clicked");
        }
        System.out.println("Chicken Eggrolls button clicked");
    }

    @FXML
    private void rangoons(ActionEvent event) {
        if (appetizer_num >0){
            appetizer_num--;
            order.add(12);
            rangoons_quantity++;
            rangoons.setText("Rangoons x"+rangoons_quantity);
            System.out.println("Rangoons button clicked");
        }
        System.out.println("Rangoons button clicked");
    }

    @FXML
    private void apple_pie_roll(ActionEvent event) {
        if (appetizer_num >0){
            appetizer_num--;
            order.add(13);
            apple_pie_roll_quantity++;
            apple_pie_roll.setText("Apple Pie Roll x"+apple_pie_roll_quantity);
            System.out.println("Apple Pie Roll button clicked");
        }
        System.out.println("Apple Pie Roll button clicked");
    }

    @FXML
    private void vegetable_eggrolls(ActionEvent event) {
        if (appetizer_num >0){
            appetizer_num--;
            order.add(14);
            vegetable_eggrolls_quantity++;
            vegetable_eggrolls.setText("Vegetable Eggrolls x"+vegetable_eggrolls_quantity);
            System.out.println("Vegetable Eggrolls button clicked");
        }
        System.out.println("Vegetable Eggrolls button clicked");
    }
    //-------------------Drinks-------------------
    @FXML
    private void fountain_drink(ActionEvent event) {
        if (drink_num >0){
            drink_num--;
            order.add(15);
            fountain_drink_quantity++;
            fountain_drink.setText("Fountain Drink x"+fountain_drink_quantity);
            System.out.println("Fountain Drink button clicked");
        }
        System.out.println("Fountain Drink button clicked");
    }

    @FXML
    private void bottled_drink(ActionEvent event) {
        if (drink_num >0){
            drink_num--;
            order.add(16);
            bottled_drink_quantity++;
            bottled_drink.setText("Bottled Drink x"+bottled_drink_quantity);
            System.out.println("Bottled Drink button clicked");
        }
        System.out.println("Bottled Drink button clicked");
    }
    @FXML
    private void redo(ActionEvent event){
        cancel();
        order.clear();

    }
    @FXML
    private void redo_last_order(ActionEvent event){
        System.out.println("Redo Last Order button clicked");
        if (orders.size() > 0){
        orders.remove(orders.size()-1);
        cancel();
        
        }

    }


}