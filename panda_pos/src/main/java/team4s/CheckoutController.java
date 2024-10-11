package team4s;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

public class CheckoutController {

    @FXML
    private ListView<String> checkout_listing;

    private List<List<String>> orderNames;
    private List<List<Integer>> orders;

    // Setter method to accept the variable
    public void setOrderNames(List<List<String>> orderNames) {
        this.orderNames = orderNames; //order names taken from ordercontroller and stored neatly to display
        // Add each sublist as a single item in the ListView
        for (List<String> sublist : orderNames) {
            checkout_listing.getItems().add(sublist.toString());
        }
    }
    public void setOrder(List<List<Integer>> orders){ //order names taken from ordercontroller and stored neatly for transaction table
        this.orders = orders;
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