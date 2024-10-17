package team4s;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ReportsController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;

    @FXML
    private VBox chartArea;
    @FXML
    private Button z_report;

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

    // Switch to manager menu
    public void switchToManager(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ManagerMenu.fxml"));
            root = loader.load();

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML 
    private void x_report(ActionEvent event){
        
        // get the current day in formatter, and the current hour in formatter2
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedCurrentTime = currentTime.format(formatter);
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern( "HH");
        
        //convert it into an hour to safeguard against generating x report after 9 PM
        int current_hour = Integer.parseInt(currentTime.format(formatter2));
        if (current_hour > 21){
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Error");
            alert.setHeaderText("It is after 9:00 PM");
            alert.setContentText("The restaurant is closed- Override?");
            
            // Add Yes and No buttons
            ButtonType buttonYes = new ButtonType("Yes");
            ButtonType buttonNo = new ButtonType("No");
            alert.getButtonTypes().setAll(buttonYes, buttonNo);

            Optional<ButtonType> override = alert.showAndWait();
            if (override.get() == buttonYes) {
                int empty;
            }
            else{
                return;
            }
        }

        //confirmation dialog, main function starts here
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("X Report");
        alert.setHeaderText("Are you sure you want to generate an X Report?");
        alert.setContentText("This will generate an X Report for the current day.");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            // Logic to generate X Report
            System.out.println("Generating X Report...");
            //loop for starting time
            for (int i = 9; i < current_hour; i++) {
                //Sales per hour sicne 9 AM
                System.out.println("--------------------------------------");
                String selectQuery = "SELECT SUM(total_cost) AS total_cost_sum FROM transaction WHERE transaction_date = '10-15-24' AND EXTRACT(HOUR FROM transaction_time) BETWEEN ? AND ?";
                try (Connection conn = Database.connect();
                    PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                        stmt.setInt(1, i);
                        stmt.setInt(2, i+1);
                        ResultSet rs = stmt.executeQuery();
                        boolean empty = true;
                        while (rs.next()) {
                            empty = false;
                            System.out.println("Sales for hour " + i + " to " + (i+ 1) + ": " + rs.getDouble("total_cost_sum"));
                        }
                        if (empty) {
                            System.out.println("No sales for hour " + i + " to " + (i+ 1));
                        }
                    } catch (SQLException e) {
                        System.err.println("Failed to load items from the database.");
                        e.printStackTrace();
                    }


                //Items sold per hour since 9 AM
                System.out.println();
                selectQuery = "SELECT mi.menu_item_id, mi.item_name, SUM(mt.item_quantity) AS total_quantity FROM menu_item mi JOIN menu_item_transaction mt ON mi.menu_item_id = mt.menu_item_id JOIN transaction t ON mt.transaction_id = t.transaction_id WHERE t.transaction_date = '10-15-24' AND EXTRACT(HOUR FROM t.transaction_time) BETWEEN ? AND ? GROUP BY mi.menu_item_id, mi.item_name ORDER BY total_quantity DESC";
                try (Connection conn = Database.connect();
                    PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                        stmt.setInt(1, i);
                        stmt.setInt(2, i+1);
                        ResultSet rs = stmt.executeQuery();
                        boolean empty = true;
                        while (rs.next()) {
                            empty = false;
                            System.out.println("Item: " + rs.getString("item_name") + " Quantity: " + rs.getInt("total_quantity"));
                        }
                        if (empty) {
                            System.out.println("No items sold for hour " + i + " to " + (i+ 1));
                        }

                    } catch (SQLException e) {
                        System.err.println("Failed to load items from the database.");
                        e.printStackTrace();
                    }


                //Transaction type per hour since 9 AM
                System.out.println();
                selectQuery = "SELECT transaction_type, COUNT(*) AS type_count FROM transaction WHERE transaction_date = '10-15-24' AND EXTRACT(HOUR FROM transaction_time) BETWEEN ? AND ? GROUP BY transaction_type;";
                try (Connection conn = Database.connect();
                    PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                        stmt.setInt(1, i);
                        stmt.setInt(2, i+1);
                        ResultSet rs = stmt.executeQuery();
                        boolean empty = true;
                        while (rs.next()) {
                            empty = false;
                            System.out.println("Transaction Type: " + rs.getString("transaction_type") + " Count: " + rs.getInt("type_count"));
                        }
                        if (empty) {
                            System.out.println("No transactions for hour " + i + " to " + (i+ 1));
                        }
                    } catch (SQLException e) {
                        System.err.println("Failed to load items from the database.");
                        e.printStackTrace();
            }
            System.out.println("--------------------------------------"); //end of loop
            
        }
    }
    }
    @FXML
    private void z_report(ActionEvent event){
        
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedCurrentTime = currentTime.format(formatter);
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern( "HH");
        int current_hour = Integer.parseInt(currentTime.format(formatter2));

        //shows warning about generating z report before 9 PM, gives option to override
        if (current_hour < 21){
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot generate Z Report");
            alert.setContentText("Z Reports can only be generated after 9 PM. - Override?");
            
            // Add Yes and No buttons
            ButtonType buttonYes = new ButtonType("Yes");
            ButtonType buttonNo = new ButtonType("No");
            alert.getButtonTypes().setAll(buttonYes, buttonNo);

            Optional<ButtonType> override = alert.showAndWait();
            if (override.get() == buttonYes) {
                int empty;
            }
            else{
                return;
            }
        }
        //actual report
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Z Report");
        alert.setHeaderText("Are you sure you want to generate a Z Report?");
        alert.setContentText("This will generate a Z Report for the current day.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            // Logic to generate Z Report
            System.out.println("Generating Z Report...");

            System.out.println("Z Report generated at: " + formattedCurrentTime);
            System.out.println();


            //total cost of all orders, number of orders generation
            double total_cost = 0;
            int number_of_orders = 0;
            String selectQuery = "SELECT total_cost FROM transaction WHERE transaction_date = '10-15-24'";
            try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                    /* 
                LocalDate localDate = LocalDate.parse(formattedCurrentTime, formatter);
                Date sqlDate = Date.valueOf(localDate);

                // Set the transaction_date parameter
                stmt.setDate(1, sqlDate);
                */
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    //PRINT THIS - TOTAL COST OF ALL ORDERS
                    total_cost += rs.getDouble("total_cost");
                    number_of_orders++;


                }
                System.out.println("Total Cost: " + total_cost);
                System.out.println("Number of Orders: " + number_of_orders);
                System.out.println();     
    
            } catch (SQLException e) {
                System.err.println("Failed to load items from the database.");
                e.printStackTrace();
            }
            System.out.println();
            //Items sold arranged by quantity
            selectQuery = "SELECT mi.menu_item_id, mi.item_name, SUM(mt.item_quantity) AS total_quantity FROM menu_item mi JOIN menu_item_transaction mt ON mi.menu_item_id = mt.menu_item_id JOIN transaction t ON mt.transaction_id = t.transaction_id WHERE t.transaction_date = '10-15-24' GROUP BY mi.menu_item_id, mi.item_name ORDER BY total_quantity DESC";
            try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                /* 
                LocalDate localDate = LocalDate.parse(formattedCurrentTime, formatter);
                Date sqlDate = Date.valueOf(localDate);
                

                // Set the transaction_date parameter
                stmt.setDate(1, sqlDate);
                */
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    //PRINT THIS - ALL ITEMS SOLD ARRANGED BY QUANTITY
                    System.out.println("Item: " + rs.getString("item_name") + " Quantity: " + rs.getInt("total_quantity"));
                }
                
    
            } catch (SQLException e) {
                System.err.println("Failed to load items from the database.");
                e.printStackTrace();
            }
            System.out.println();

            //Transaction type
            selectQuery = "SELECT transaction_type, count(*) FROM transaction WHERE transaction_date = '10-15-24' GROUP BY transaction_type";
            try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                    /* 
                LocalDate localDate = LocalDate.parse(formattedCurrentTime, formatter);
                Date sqlDate = Date.valueOf(localDate);

                // Set the transaction_date parameter
                stmt.setDate(1, sqlDate);
                */
                ResultSet rs = stmt.executeQuery();
    
                while (rs.next()) {
                    //PRINT THIS - TRANSACTION TYPE AND COUNT
                    System.out.println("Transaction Type: " + rs.getString("transaction_type") + " Count: " + rs.getInt("count"));


                }
            } catch (SQLException e) {
                System.err.println("Failed to load items from the database.");
                e.printStackTrace();
            }

        }
    }
}
