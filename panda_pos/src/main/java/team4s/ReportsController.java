package team4s;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ReportsController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;
    
    private boolean isScatterplot = false;

    @FXML
    private ComboBox<String> graphTypeComboBox;
    @FXML
    private Button selectTableOneButton;
    @FXML
    private ComboBox<String> tableOneComboBox;
    @FXML
    private ComboBox<String> xAxisComboBox;
    @FXML
    private ComboBox<String> tableTwoComboBox;
    @FXML
    private ComboBox<String> yAxisComboBox;
    @FXML
    private VBox inputArea;
    @FXML
    private Label secondTableLabel;
    @FXML
    private HBox secondTableButtons;
    @FXML
    private VBox chartArea;
    @FXML
    private Button returnToManagerButton;
    @FXML
    private Label scatterplotLabel;
    @FXML
    private HBox scatterplotButtons;

    @FXML
    public void initialize() {
        populateGraphTypeComboBox();
        selectTableOneButton.setVisible(false);
        secondTableLabel.setVisible(false);
        secondTableButtons.setVisible(false);
    }
    //creates the dropdown menu for the graph types
    private void populateGraphTypeComboBox() {
        graphTypeComboBox.setItems(FXCollections.observableArrayList("Pie Chart", "Bar Chart", "Line Graph"));
        graphTypeComboBox.setOnAction(this::handleGraphTypeSelection);
    }
    //sets yes for scatterplot if the yes button for scatterplot is clicked
    @FXML
    private void setScatterplot(ActionEvent event) {
        isScatterplot = true;
    }
    //sets no for scatterplot if the no button for scatterplot is clicked
    @FXML
    private void setSummedData(ActionEvent event) {
        isScatterplot = false;
    }

    //updates the gui based on the selected graph type
    @FXML
    private void handleGraphTypeSelection(ActionEvent event) {
        String selectedGraphType = graphTypeComboBox.getValue();
        selectTableOneButton.setVisible(true);
    
        if ("Line Graph".equals(selectedGraphType)) {
            yAxisComboBox.setVisible(false);
            secondTableLabel.setVisible(true);
            secondTableButtons.setVisible(true);
            scatterplotLabel.setVisible(true);
            scatterplotButtons.setVisible(true);
        } else {
            scatterplotLabel.setVisible(false);
            scatterplotButtons.setVisible(false);
            secondTableLabel.setVisible(true);
            secondTableButtons.setVisible(true);
        }
    }

    //populates the dropdown for the first table once the button is clicked and then sets the xaxis options, based on the selected value
    @FXML
    private void selectFirstTable(ActionEvent event) {
        tableOneComboBox.setVisible(true);
        tableOneComboBox.setItems(FXCollections.observableArrayList("inventory", "transaction", "employee", "customer", "menu_item"));
        tableOneComboBox.setOnAction(this::populateXAxisOptions);
    }

    //populates the x axis options based on the selected value for the first table
    private void populateXAxisOptions(ActionEvent event) {
        String selectedTable = tableOneComboBox.getValue();
        if (selectedTable != null) {
            xAxisComboBox.setVisible(true);
            switch (selectedTable) {
                case "inventory":
                    xAxisComboBox.setItems(FXCollections.observableArrayList("ingredient_name", "current_stock", "price"));
                    break;
                case "transaction":
                    xAxisComboBox.setItems(FXCollections.observableArrayList("transaction_id", "total_cost", "transaction_time", "transaction_date", "transaction_type", "customer_id", "employee_id",  "week_number"));
                    break;
                case "employee":
                    xAxisComboBox.setItems(FXCollections.observableArrayList("employee_id", "first_name", "last_name", "role"));
                    break;
                case "customer":
                    xAxisComboBox.setItems(FXCollections.observableArrayList("customer_id", "first_name", "last_name", "reward_points"));
                    break;
                case "menu_item":
                    xAxisComboBox.setItems(FXCollections.observableArrayList("menu_item_id", "current_servings", "item_name", "item_price", "item_category"));
                    break;
            }
        }
    }

    //called when yes is selected for adding second table
    @FXML
    private void addSecondTable(ActionEvent event) {
        tableTwoComboBox.setVisible(true);
        tableTwoComboBox.setItems(FXCollections.observableArrayList("inventory", "transaction", "employee", "customer", "menu_item"));
        tableTwoComboBox.setOnAction(this::populateYAxisFromSecondTable);
    }

    //called when no is selected for adding second table
    @FXML
    private void skipSecondTable(ActionEvent event) {
        yAxisComboBox.setVisible(true);
        populateYAxisFromFirstTable();
    }

    //populates the y axis values based on the selected second table, if addSecondTable was called
    private void populateYAxisFromSecondTable(ActionEvent event) {
        String selectedTable = tableTwoComboBox.getValue();
        if (selectedTable != null) {
            yAxisComboBox.setVisible(true);
            switch (selectedTable) {
                case "inventory":
                    yAxisComboBox.setItems(FXCollections.observableArrayList("current_stock", "price"));
                    break;
                case "transaction":
                    yAxisComboBox.setItems(FXCollections.observableArrayList( "total_cost", "week_number"));
                    break;
                case "employee":
                    yAxisComboBox.setItems(FXCollections.observableArrayList("employee_id"));
                    break;
                case "customer":
                    yAxisComboBox.setItems(FXCollections.observableArrayList( "reward_points"));
                    break;
                case "menu_item":
                    yAxisComboBox.setItems(FXCollections.observableArrayList( "current_servings", "item_price"));
                    break;
            }
        }
    }

    //populates the y axis values based on the selected first table, if skipSecondTable was called
    private void populateYAxisFromFirstTable() {
        String selectedTable = tableOneComboBox.getValue();
        if (selectedTable != null) {
            switch (selectedTable) {
                case "inventory":
                    yAxisComboBox.setItems(FXCollections.observableArrayList("current_stock", "price"));
                    break;
                case "transaction":
                    yAxisComboBox.setItems(FXCollections.observableArrayList("total_cost", "week_number"));
                    break;
                case "employee":
                    yAxisComboBox.setItems(FXCollections.observableArrayList("employee_id"));
                    break;
                case "customer":
                    yAxisComboBox.setItems(FXCollections.observableArrayList( "reward_points"));
                    break;
                case "menu_item":
                    yAxisComboBox.setItems(FXCollections.observableArrayList( "current_servings", "item_price"));
                    break;
            }
        }
    }

    //determines what graph to create, based on all of the inputted data
    @FXML
    private void generateReport(ActionEvent event) {
        chartArea.getChildren().clear();
        String graphType = graphTypeComboBox.getValue();
        String tableOne = tableOneComboBox.getValue();
        String xAxis = xAxisComboBox.getValue();
        String tableTwo = (tableTwoComboBox != null && tableTwoComboBox.isVisible()) ? tableTwoComboBox.getValue() : null;
        String yAxis = yAxisComboBox.getValue();

        switch (graphType) {
            case "Line Graph":
                if(isScatterplot){
                    LineChart<String, Number> scatterplot = createScatterplot(xAxis, yAxis, tableOne, tableTwo);
                    chartArea.getChildren().add(scatterplot);
                }
                else{
                    LineChart<String, Number> lineChart = createLineChart(xAxis, yAxis, tableOne, tableTwo);
                    chartArea.getChildren().add(lineChart);
                }
                break;
            case "Bar Chart":
                BarChart<String, Number> barChart = createBarChart(xAxis, yAxis, tableOne, tableTwo);
                chartArea.getChildren().add(barChart);
                break;
            case "Pie Chart":
                PieChart pieChart = createPieChart(xAxis, yAxis, tableOne, tableTwo);
                chartArea.getChildren().add(pieChart);
                break;
            default:
                System.out.println("Unknown graph type selected.");
        }
    }
    //fetches the data so it can be put into the graph, when not a line graph
    private List<XYChart.Data<String, Number>> fetchDataFromDatabase(String xAxis, String yAxis, String tableOne, String tableTwo) {
        List<XYChart.Data<String, Number>> dataList = new ArrayList<>();
        
        String query;

        if (tableTwo == null) {
            // Single table query
            query = "SELECT " + xAxis + ", " + yAxis + " FROM " + tableOne + " ORDER BY " + xAxis + " ASC";
        } else {
            // Two-table query with a JOIN
            query = "SELECT " + tableOne + "." + xAxis + ", " + tableTwo + "." + yAxis + 
                " FROM " + tableOne + 
                " JOIN " + tableTwo + " ON " + tableOne + ".common_column = " + tableTwo + ".common_column" +
                " ORDER BY " + tableOne + "." + xAxis + " ASC";
        }

        try {
            conn = Database.connect();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String xValue = rs.getString(1);
                Number yValue = rs.getDouble(2); 
                dataList.add(new XYChart.Data<>(xValue, yValue));
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataList;
    }
    //this is only called when it is a line graph
    private List<XYChart.Data<String, Number>> fetchSummedDataFromDatabase(String xAxis, String yAxis, String tableOne, String tableTwo) {
        List<XYChart.Data<String, Number>> dataList = new ArrayList<>();

        // SQL query to group and sum the Y values by the X values
        String query = "SELECT " + xAxis + ", SUM(" + yAxis + ") FROM " + tableOne + " GROUP BY " + xAxis + " ORDER BY " + xAxis + " ASC";

        try (Connection conn = Database.connect();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String xValue = rs.getString(1);
                Number yValue = rs.getDouble(2);
                dataList.add(new XYChart.Data<>(xValue, yValue));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    //this is called when the graph type was line graph, but then isScatterplot was true. It creates and displays the chart
    private LineChart<String, Number> createScatterplot(String xAxisLabel, String yAxisLabel, String tableOne, String tableTwo) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Line Graph of " + xAxisLabel + " vs " + yAxisLabel);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Data");

        List<XYChart.Data<String, Number>> dataList = fetchDataFromDatabase(xAxisLabel, yAxisLabel, tableOne, tableTwo);
        series.getData().addAll(dataList);

        lineChart.getData().add(series);
        return lineChart;
    }
    //this is called when isScatterplot is false and the graph type was line graph. It creates and displays the chart
    private LineChart<String, Number> createLineChart(String xAxisLabel, String yAxisLabel, String tableOne, String tableTwo) {
        // Group by X value and sum the Y values
        List<XYChart.Data<String, Number>> summedData = fetchSummedDataFromDatabase(xAxisLabel, yAxisLabel, tableOne, tableTwo);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Summed Line Graph of " + xAxisLabel + " vs " + yAxisLabel);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Summed Data");

        // Add summed data to the series
        series.getData().addAll(summedData);

        lineChart.getData().add(series);
        return lineChart;
    }


    //creates the barChart (need to add more to this) TODO sum the x value entries so it can be displayed properly
    private BarChart<String, Number> createBarChart(String xAxisLabel, String yAxisLabel, String tableOne, String tableTwo) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Bar Chart of " + xAxisLabel + " vs " + yAxisLabel);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Data");

        List<XYChart.Data<String, Number>> dataList = fetchDataFromDatabase(xAxisLabel, yAxisLabel, tableOne, tableTwo);
        series.getData().addAll(dataList);

        barChart.getData().add(series);
        return barChart;
    }
    //creates the pieChart (need to do more to this) TODO actually calculate the percents for each component
    private PieChart createPieChart(String xAxisLabel, String yAxisLabel, String tableOne, String tableTwo) {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Pie Chart of " + xAxisLabel + " vs " + yAxisLabel);

        List<XYChart.Data<String, Number>> dataList = fetchDataFromDatabase(xAxisLabel, yAxisLabel, tableOne, tableTwo);

        for (XYChart.Data<String, Number> data : dataList) {
            pieChart.getData().add(new PieChart.Data(data.getXValue(), data.getYValue().doubleValue()));
        }

        return pieChart;
    }
    
    //used to reset the graph ui back to its original form
    @FXML
    private void resetForm(ActionEvent event) {
        graphTypeComboBox.getSelectionModel().clearSelection();
        tableOneComboBox.getSelectionModel().clearSelection();
        xAxisComboBox.getSelectionModel().clearSelection();
        tableTwoComboBox.getSelectionModel().clearSelection();
        yAxisComboBox.getSelectionModel().clearSelection();

        selectTableOneButton.setVisible(false);
        tableOneComboBox.setVisible(false);
        xAxisComboBox.setVisible(false);
        yAxisComboBox.setVisible(false);
        scatterplotLabel.setVisible(false);
        scatterplotButtons.setVisible(false);
        secondTableLabel.setVisible(false);
        secondTableButtons.setVisible(false);
        tableTwoComboBox.setVisible(false);

        // Clear the chart area
        chartArea.getChildren().clear();
    }

    //switches the screen back to the main manager page
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
}