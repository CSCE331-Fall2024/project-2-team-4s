package team4s;

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
import java.util.stream.Collectors;

public class CheckoutController {
    @FXML 
    private Label total;
    @FXML
    private Label tax;
    @FXML
    private Label sum;
    private double total_cost = 0;
    private double tax_cost = 0;
    private double sum_cost = 0;

    @FXML
    private ListView<String> checkout_listing;

    private List<List<String>> orderNames;
    private List<List<Integer>> orders;

    // Setter method to accept the variable
    public void setOrderNames(List<List<String>> orderNames) {
        this.orderNames = orderNames; //order names taken from ordercontroller and stored neatly to display
        // Add each sublist as a single item in the ListView
        for (List<String> sublist : orderNames) {
            checkout_listing.getItems().add(sublist.toString() + "\n" + "$" +String.valueOf(NamesToCost(sublist.get(0))));
            total_cost += NamesToCost(sublist.get(0));
        }
        total_cost = roundToTwoDecimalPlaces(total_cost);
        sum_cost = total_cost;
        sum.setText("Sum - $" + String.valueOf(total_cost));
        tax_cost = roundToTwoDecimalPlaces(total_cost * 0.0825);
        total_cost = roundToTwoDecimalPlaces(total_cost + tax_cost);
        tax.setText("Tax - $" + String.valueOf(tax_cost));
        total.setText("Total - $" + String.valueOf(total_cost));

    }
    public void setOrder(List<List<Integer>> orders){ //order names taken from ordercontroller and stored neatly for transaction table
        this.orders = orders;
    }
    public double roundToTwoDecimalPlaces(double value) {
    BigDecimal bd = new BigDecimal(Double.toString(value));
    bd = bd.setScale(2, RoundingMode.HALF_UP);
    return bd.doubleValue();
}
    public double NamesToCost(String orderNames){
        if (orderNames == "Bowl"){
                return 8.3;
            }
        else if (orderNames == "Plate"){
            return 9.3;
        }
        else if (orderNames == "Bigger Plate"){
            return 11.3;
        }
        else if (orderNames == "Appetizer"){
            return 2;
        }
        else if (orderNames == "Side"){
            return 4.4;
        }
        else if (orderNames == "Entree"){
            return 5.2;
        }
        else if (orderNames == "Fountain Drink"){
            return 2.1;
        }
        else if (orderNames == "Bottled Drink"){
            return 3;
        }
        else{
            return 9999;
        }
        }
    

    @FXML
    private void confirm_checkout(ActionEvent event) {
        // Your logic for confirming checkout
        System.out.println(orderNames);
    }

    @FXML
    private void go_back(ActionEvent event) {
        // Your logic for going back
        System.out.println(orders);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Cashier.fxml"));
            Parent root = loader.load();

        // Get the controller and set the variable
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Your logic for initializing the controller
    }
}
