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

public class ReportsController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;

    @FXML
    private VBox chartArea;

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
            root = FXMLLoader.load(getClass().getResource("/fxml/ManagerMenu.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
