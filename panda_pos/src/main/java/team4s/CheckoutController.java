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

public class CheckoutController {
    private Connection conn;

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
        this.orderNames = orderNames; // order names taken from ordercontroller and stored neatly to display

        // Add sublist as a single item in the ListView and calculate the total cost
        for (List<String> sublist : orderNames) {
            checkout_listing.getItems()
                    .add(sublist.toString() + "\n" + "$"
                            + String.valueOf(roundToTwoDecimalPlaces(totalCostSub(sublist))));
        }

        // Calculate the total cost
        total_cost = roundToTwoDecimalPlaces(totalCostList(orderNames));
        sum_cost = total_cost;
        sum.setText("Sum - $" + String.valueOf(total_cost));
        tax_cost = roundToTwoDecimalPlaces(total_cost * 0.0825);
        total_cost = roundToTwoDecimalPlaces(total_cost + tax_cost);
        tax.setText("Tax - $" + String.valueOf(tax_cost));
        total.setText("Total - $" + String.valueOf(total_cost));

    }

    public void setOrder(List<List<Integer>> orders) { // order names taken from ordercontroller and stored neatly for
                                                       // transaction table
        this.orders = orders;
    }

    public double roundToTwoDecimalPlaces(double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public double totalCostSub(List<String> items) {
        String selectQuery = "SELECT * FROM menu_item WHERE item_name = ?";
        double func_total = 0;

        try {
            conn = Database.connect();
            System.out.println("Database connection opened");

            for (String item : items) {
                PreparedStatement stmt = conn.prepareStatement(selectQuery);

                stmt.setString(1, item);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double cost = rs.getDouble("item_price");
                    System.out.println("Cost: " + cost);

                    func_total += cost;
                }
            }

            conn.close();
            System.out.println("Database connection closed");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return func_total;
    }

    public double totalCostList(List<List<String>> items) {
        String selectQuery = "SELECT * FROM menu_item WHERE item_name = ?";
        double func_total = 0;

        try {
            conn = Database.connect();
            System.out.println("Database connection opened");

            for (List<String> sublist : items) {
                for (String item : sublist) {
                    PreparedStatement stmt = conn.prepareStatement(selectQuery);

                    stmt.setString(1, item);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        double cost = rs.getDouble("item_price");
                        System.out.println("Cost: " + cost);

                        func_total += cost;
                    }
                }
            }

            conn.close();
            System.out.println("Database connection closed");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return func_total;
    }

    private List<Integer> flattenOrderList(List<List<Integer>> orders) {
        List<Integer> flattened = new ArrayList<>();

        for (List<Integer> sublist : orders) {
            for (Integer item : sublist) {
                flattened.add(item);
            }
        }

        System.out.println("Flattened order list: " + flattened);
        return flattened;
    }

    @FXML
    private void confirm_checkout(ActionEvent event) {
        System.out.println(orderNames);

        try {
            conn = Database.connect();
            System.out.println("Database connection opened");
            String insertQuery = "INSERT INTO transaction (total_cost, transaction_time, transaction_date, transaction_type, customer_id, employee_id, week_number) VALUES (?, ?, ?, ?, ?, ?, ?)";

            // get current time
            LocalTime time = LocalTime.now();
            java.sql.Time sqlTime = java.sql.Time.valueOf(time);

            // get current date
            LocalDate date = LocalDate.now();
            java.sql.Date sqlDate = java.sql.Date.valueOf(date);

            PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            stmt.setDouble(1, total_cost);
            stmt.setTime(2, sqlTime);
            stmt.setDate(3, sqlDate);
            stmt.setString(4, "Credit/Debit");
            stmt.setNull(5, java.sql.Types.INTEGER); // set customer_id to null
            stmt.setInt(6, 1);
            stmt.setInt(7, 40);

            stmt.executeUpdate(); // execute the insert statement

            // get the transaction_id of the newly inserted transaction
            int transactionID;
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transactionID = generatedKeys.getInt(1);
                    System.out.println("transaction_id: " + transactionID);
                } else {
                    throw new SQLException("Creating transaction failed");
                }
            }

            // flatten the orders list and create a set of unique items
            List<Integer> flattenedOrders = flattenOrderList(orders);
            Set<Integer> ordersSet = new HashSet<>(flattenedOrders);

            // set join table entries
            for (Integer order : ordersSet) {
                String insertJoinTableQuery = "INSERT INTO menu_item_transaction (menu_item_id, transaction_id, item_quantity) VALUES (?, ?, ?)";
                PreparedStatement joinTableStmt = conn.prepareStatement(insertJoinTableQuery);

                int quantity = 0;
                for (Integer item : flattenedOrders) {
                    if (item == order) {
                        quantity++;
                    }
                }

                joinTableStmt.setInt(1, order);
                joinTableStmt.setInt(2, transactionID);
                joinTableStmt.setInt(3, quantity);

                joinTableStmt.executeUpdate();

                System.out.println(
                        "Inserted into menu_item_transaction: " + order + ", " + transactionID + ", " + quantity);
            }

            conn.close();
            System.out.println("Database connection closed");

            // navigate back to the cashier screen
            go_back(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void go_back(ActionEvent event) {
        // Your logic for going back
        System.out.println(orders);
        System.out.println(orderNames);
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
