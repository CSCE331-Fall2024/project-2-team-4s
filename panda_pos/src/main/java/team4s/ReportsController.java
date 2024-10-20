package team4s;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
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
    private Label firstTableLabel;
    @FXML
    private ComboBox<String> tableOneComboBox;
    @FXML
    private Label xAxisLabel;
    @FXML
    private ComboBox<String> xAxisComboBox;
    @FXML
    private ComboBox<String> tableTwoComboBox;
    @FXML
    private Label yAxisLabel;
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
    private Label dateLabel;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label startTimeLabel;
    @FXML
    private ComboBox<Integer> startTimeComboBox;
    @FXML
    private Label endTimeLabel;
    @FXML
    private ComboBox<Integer> endTimeComboBox;
    @FXML
    private Label startDayLabel;
    @FXML
    private Label endDayLabel;
    @FXML
    private DatePicker startDay;
    @FXML
    private DatePicker endDay;
    @FXML
    private ScrollPane chartScrollPane;
    @FXML
    private GridPane chartGrid;

    @FXML
    public void initialize() {
        populateGraphTypeComboBox();
        selectTableOneButton.setVisible(false);
        secondTableLabel.setVisible(false);
        secondTableButtons.setVisible(false);
        datePicker.setValue(LocalDate.now());
        datePicker.setVisible(false);
        startTimeComboBox.setVisible(false);
        endTimeComboBox.setVisible(false);
        startDay.setValue(LocalDate.now());
        startDay.setVisible(false);
        endDay.setValue(LocalDate.now());
        endDay.setVisible(false);

        // Populate the start and end time ComboBoxes with values from 9 to 21 (9 AM to 9 PM)
        startTimeComboBox.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(9, 21).boxed().collect(Collectors.toList())));
        endTimeComboBox.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(9, 21).boxed().collect(Collectors.toList())));

        startTimeComboBox.setValue(9); 
        endTimeComboBox.setValue(21); 
    }
    //creates the dropdown menu for the graph types
    private void populateGraphTypeComboBox() {
        graphTypeComboBox.setItems(FXCollections.observableArrayList("Product Usage", "X-report", "Z-report", "Pie Chart", "Bar Chart", "Line Graph"));
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
    
        if("Product Usage".equals(selectedGraphType)){
            startDayLabel.setVisible(true);
            startDay.setVisible(true);
            endDayLabel.setVisible(true);
            endDay.setVisible(true);
            startTimeLabel.setVisible(true);
            endTimeLabel.setVisible(true);
            startTimeComboBox.setVisible(true);
            endTimeComboBox.setVisible(true);
        }
        else if("X-report".equals(selectedGraphType)){
            dateLabel.setVisible(true);
            datePicker.setVisible(true);
            startTimeLabel.setVisible(true);
            endTimeLabel.setVisible(true);
            startTimeComboBox.setVisible(true);
            endTimeComboBox.setVisible(true);
        }
        else if("Z-report".equals(selectedGraphType)){
            datePicker.setVisible(true);
        }
        else if ("Line Graph".equals(selectedGraphType)) {
            selectTableOneButton.setVisible(true);
            yAxisComboBox.setVisible(false);
            secondTableLabel.setVisible(true);
            secondTableButtons.setVisible(true);
            scatterplotLabel.setVisible(true);
            scatterplotButtons.setVisible(true);
        } 
        else {
            selectTableOneButton.setVisible(true);
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
        firstTableLabel.setVisible(true);
        tableOneComboBox.setItems(FXCollections.observableArrayList("inventory", "transaction", "employee", "customer", "menu_item"));
        tableOneComboBox.setOnAction(this::populateXAxisOptions);
    }

    //populates the x axis options based on the selected value for the first table
    private void populateXAxisOptions(ActionEvent event) {
        String selectedTable = tableOneComboBox.getValue();
        if (selectedTable != null) {
            xAxisComboBox.setVisible(true);
            xAxisLabel.setVisible(true);
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
        secondTableLabel.setVisible(true);
        tableTwoComboBox.setItems(FXCollections.observableArrayList("inventory", "transaction", "employee", "customer", "menu_item"));
        tableTwoComboBox.setOnAction(this::populateYAxisFromSecondTable);
    }

    //called when no is selected for adding second table
    @FXML
    private void skipSecondTable(ActionEvent event) {
        yAxisComboBox.setVisible(true);
        yAxisLabel.setVisible(true);
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
        // get the current day in formatter, and the current hour in formatter2
        LocalDate selectedDate = datePicker.getValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = selectedDate.format(formatter);

        LocalDate fixedDate = LocalDate.parse("2024-10-15", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if(fixedDate.isBefore(selectedDate)){
            selectedDate = fixedDate;
        }

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
                chartArea.getChildren().add(createBarChart(xAxis, yAxis, tableOne, tableTwo));
                break;
            case "Pie Chart":
                PieChart pieChart = createPieChart(xAxis, yAxis, tableOne, tableTwo);
                chartArea.getChildren().add(pieChart);
                break;
            case "X-report":
                chartArea.getChildren().add(x_report_hourly_sales(selectedDate, startTimeComboBox.getValue(), endTimeComboBox.getValue()));
                chartArea.getChildren().add(x_report_items_sold(selectedDate, startTimeComboBox.getValue(), endTimeComboBox.getValue()));
                chartArea.getChildren().add(transactionTypes_xreport(selectedDate, startTimeComboBox.getValue(), endTimeComboBox.getValue()));
                break;
            case "Z-report":
                chartArea.getChildren().add(z_report_hourly_sales(selectedDate));
                chartArea.getChildren().add(z_report_items_sold(selectedDate));
                chartArea.getChildren().add(transactionTypes_zreport(selectedDate));
                break;
            case "Product Usage":
                // Get the charts for each unit
                List<BarChart<String, Number>> unitBarCharts = productUsageChart(startDay.getValue(), endDay.getValue(), startTimeComboBox.getValue(), endTimeComboBox.getValue());

                // Log the number of charts created
                System.out.println("Number of bar charts created: " + unitBarCharts.size());

                // Clear previous content from the chart area
                chartArea.getChildren().clear();

                // Add each chart to the VBox (chartArea)
                for (BarChart<String, Number> barChart : unitBarCharts) {
                    barChart.setPrefWidth(600);  // Adjust chart width
                    barChart.setPrefHeight(400); // Adjust chart height

                    // Add the chart directly to the VBox
                    chartArea.getChildren().add(barChart);
                }

                // Ensure the layout is updated
                chartArea.layout();
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
        datePicker.setValue(LocalDate.now());
        datePicker.setVisible(false);
        startTimeComboBox.setVisible(false);
        startTimeLabel.setVisible(false);
        endTimeComboBox.setVisible(false);
        endTimeLabel.setVisible(false);

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
    public List<BarChart<String, Number>> productUsageChart(LocalDate startDate, LocalDate endDate, int startHour, int endHour) {
        List<BarChart<String, Number>> unitBarCharts = new ArrayList<>();
    
        // Query to fetch the inventory usage data grouped by units
        String query = "SELECT i.ingredient_name, i.unit, " +
                       "SUM(mit.item_quantity * imi.ingredient_amount / 10) AS total_used " +
                       "FROM transaction t " +
                       "JOIN menu_item_transaction mit ON t.transaction_id = mit.transaction_id " +
                       "JOIN menu_item mi ON mit.menu_item_id = mi.menu_item_id " +
                       "JOIN inventory_menu_item imi ON mi.menu_item_id = imi.menu_item_id " +
                       "JOIN inventory i ON imi.ingredient_id = i.ingredient_id " +
                       "WHERE t.transaction_date BETWEEN ? AND ? " +
                       "AND EXTRACT(HOUR FROM t.transaction_time) BETWEEN ? AND ? " +
                       "GROUP BY i.unit, i.ingredient_name " +
                       "ORDER BY i.unit";
    
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            // Set the date range and time window as parameters
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate)); 
            stmt.setInt(3, startHour); 
            stmt.setInt(4, endHour);  
    
            // Execute the query and process the results
            ResultSet rs = stmt.executeQuery();
    
            // Create a map to store ingredients and usage amounts by unit
            Map<String, List<XYChart.Data<String, Number>>> unitDataMap = new HashMap<>();
    
            while (rs.next()) {
                String ingredientName = rs.getString("ingredient_name");
                String unit = rs.getString("unit");
                double totalUsed = rs.getDouble("total_used");
    
                // Add data to the corresponding unit's list of data
                unitDataMap.computeIfAbsent(unit, k -> new ArrayList<>())
                           .add(new XYChart.Data<>(ingredientName, totalUsed));
            }
    
            // creates a separate BarChart for each unit
            for (Map.Entry<String, List<XYChart.Data<String, Number>>> entry : unitDataMap.entrySet()) {
                String unit = entry.getKey();
                List<XYChart.Data<String, Number>> unitData = entry.getValue();
    
                // Create axes for the BarChart
                CategoryAxis xAxis = new CategoryAxis();
                xAxis.setLabel("Ingredient");
    
                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Total Amount Used");
                yAxis.setAutoRanging(true);
    
                // Create the BarChart object
                BarChart<String, Number> unitBarChart = new BarChart<>(xAxis, yAxis);
                unitBarChart.setTitle("Inventory Usage for Unit: " + unit + " (from " + startHour + ":00 to " + endHour + ":00)");
                unitBarChart.setLegendVisible(false);

                // Create a series to hold the data for this unit
                XYChart.Series<String, Number> unitSeries = new XYChart.Series<>();
                unitSeries.setName("Unit: " + unit);
    
                // Add data to the series
                unitSeries.getData().addAll(unitData);
    
                // Add the series to the BarChart
                unitBarChart.getData().add(unitSeries);
    
                // Add the BarChart to the list of charts
                unitBarCharts.add(unitBarChart);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return a bar chart for each different unit, as a list
        return unitBarCharts;  
    }
    private LineChart<String, Number> x_report_hourly_sales(LocalDate selectedDate, int startHour, int endHour){

        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH");

        //convert it into an hour to safeguard against generating x report after 9 PM
        int current_hour = Integer.parseInt(currentTime.format(formatter2));
        if (current_hour > 21 && selectedDate.equals(LocalDate.now())){
            //shows warning about generating x report after 9 PM, gives option to override
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
                return null;
            }
        }
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time (Hours)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total Sales");

        LineChart<String, Number> totalSalesChart = new LineChart<>(xAxis, yAxis);
        totalSalesChart.setTitle("Total Sales From Hour " + startHour + " to Hour " + endHour + " on " + selectedDate);

        XYChart.Series<String, Number> totalCostSeries = new XYChart.Series<>();
        totalCostSeries.setName("Sales per Hour");
        totalSalesChart.setLegendVisible(false);

        // Fetch the data for each hour and populate the series
        for (int i = startHour; i < endHour; i++) {
            String selectQuery = "SELECT SUM(total_cost) AS total_cost_sum FROM transaction WHERE transaction_date = ? AND EXTRACT(HOUR FROM transaction_time) BETWEEN ? AND ?";
            try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setDate(1, java.sql.Date.valueOf(selectedDate));
                stmt.setInt(2, i);
                stmt.setInt(3, i+1);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    totalCostSeries.getData().add(new XYChart.Data<>("Hour " + i, rs.getDouble("total_cost_sum")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        totalSalesChart.getData().add(totalCostSeries);
        return totalSalesChart;

    }
    private LineChart<String, Number> z_report_hourly_sales(LocalDate selectedDate){

        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH");

        //convert it into an hour to safeguard against generating z report before 9 PM
        int current_hour = Integer.parseInt(currentTime.format(formatter2));
        if (current_hour < 21 && selectedDate.equals(LocalDate.now())){
            //shows warning about generating z report before 9 PM, gives option to override
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
                return null;
            }
        }
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time (Hours)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total Sales");

        LineChart<String, Number> totalSalesChart = new LineChart<>(xAxis, yAxis);
        totalSalesChart.setTitle("Total Sales per Hour on " + selectedDate);

        XYChart.Series<String, Number> totalCostSeries = new XYChart.Series<>();
        totalCostSeries.setName("Sales per Hour");
        totalSalesChart.setLegendVisible(false);

        // Fetch the data for each hour and populate the series
        for (int i = 9; i < 21; i++) {
            String selectQuery = "SELECT SUM(total_cost) AS total_cost_sum FROM transaction WHERE transaction_date = ? AND EXTRACT(HOUR FROM transaction_time) BETWEEN ? AND ?";
            try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setDate(1, java.sql.Date.valueOf(selectedDate));
                stmt.setInt(2, i);
                stmt.setInt(3, i+1);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    totalCostSeries.getData().add(new XYChart.Data<>("Hour " + i, rs.getDouble("total_cost_sum")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        totalSalesChart.getData().add(totalCostSeries);
        return totalSalesChart;
    }

    // Create the BarChart for Items Sold per Hour
    private BarChart<String, Number> x_report_items_sold(LocalDate selectedDate, int startHour, int endHour) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time (Hours)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Items Sold");

        BarChart<String, Number> itemsSoldChart = new BarChart<>(xAxis, yAxis);
        itemsSoldChart.setTitle("Items Sold from Hour " + startHour + " to Hour " + endHour + " on " + selectedDate);
        itemsSoldChart.setLegendVisible(false);

        XYChart.Series<String, Number> itemsSoldSeries = new XYChart.Series<>();
        itemsSoldSeries.setName("Items per Hour");

        // Fetch the data for each hour and populate the series
        for (int i = startHour; i < endHour; i++) {
            String selectQuery = "SELECT SUM(mt.item_quantity) AS total_quantity FROM menu_item_transaction mt JOIN transaction t ON mt.transaction_id = t.transaction_id WHERE t.transaction_date = ? AND EXTRACT(HOUR FROM t.transaction_time) BETWEEN ? AND ?";
            try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setDate(1, java.sql.Date.valueOf(selectedDate));
                stmt.setInt(2, i);
                stmt.setInt(3, i+1);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    itemsSoldSeries.getData().add(new XYChart.Data<>("Hour " + i, rs.getInt("total_quantity")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        itemsSoldChart.getData().add(itemsSoldSeries);
        return itemsSoldChart;
    }

    // Create the BarChart for Items Sold per Hour
    private BarChart<String, Number> z_report_items_sold(LocalDate selectedDate) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time (Hours)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Items Sold");

        BarChart<String, Number> itemsSoldChart = new BarChart<>(xAxis, yAxis);
        itemsSoldChart.setTitle("Items Sold per Hour on " + selectedDate);
        itemsSoldChart.setLegendVisible(false);

        XYChart.Series<String, Number> itemsSoldSeries = new XYChart.Series<>();
        itemsSoldSeries.setName("Items per Hour");

        // Fetch the data for each hour and populate the series
        for (int i = 9; i < 21; i++) {
            String selectQuery = "SELECT SUM(mt.item_quantity) AS total_quantity FROM menu_item_transaction mt JOIN transaction t ON mt.transaction_id = t.transaction_id WHERE t.transaction_date = ? AND EXTRACT(HOUR FROM t.transaction_time) BETWEEN ? AND ?";
            try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setDate(1, java.sql.Date.valueOf(selectedDate));
                stmt.setInt(2, i);
                stmt.setInt(3, i+1);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    itemsSoldSeries.getData().add(new XYChart.Data<>("Hour " + i, rs.getInt("total_quantity")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        itemsSoldChart.getData().add(itemsSoldSeries);
        return itemsSoldChart;
    }
    //transaction types for an x report
    private LineChart<String, Number> transactionTypes_xreport(LocalDate selectedDate, int startHour, int endHour) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time (Hours)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Transaction Count");

        LineChart<String, Number> transactionTypeChart = new LineChart<>(xAxis, yAxis);
        transactionTypeChart.setTitle("Transaction Types from Hour " + startHour + " to Hour " + endHour + " on " + selectedDate);

        // Create separate series for each transaction type
        XYChart.Series<String, Number> creditDebitSeries = new XYChart.Series<>();
        creditDebitSeries.setName("Credit/Debit");

        XYChart.Series<String, Number> diningDollarsSeries = new XYChart.Series<>();
        diningDollarsSeries.setName("Dining Dollars");

        XYChart.Series<String, Number> maroonMealSeries = new XYChart.Series<>();
        maroonMealSeries.setName("Maroon Meal");

        XYChart.Series<String, Number> giftCardSeries = new XYChart.Series<>();
        giftCardSeries.setName("Gift Card");

        // Fetch the data for each hour and populate the series
        for (int i = startHour; i < endHour; i++) {
            String selectQuery = "SELECT transaction_type, COUNT(*) AS type_count " +
                                "FROM transaction " +
                                "WHERE transaction_date = ? AND EXTRACT(HOUR FROM transaction_time) BETWEEN ? AND ? " +
                                "GROUP BY transaction_type";
            try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setDate(1, java.sql.Date.valueOf(selectedDate));
                stmt.setInt(2, i);
                stmt.setInt(3, i + 1);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String transactionType = rs.getString("transaction_type");
                    int count = rs.getInt("type_count");
                    switch (transactionType) {
                        case "Credit/Debit":
                            creditDebitSeries.getData().add(new XYChart.Data<>("Hour " + i, count));
                            break;
                        case "Dining Dollars":
                            diningDollarsSeries.getData().add(new XYChart.Data<>("Hour " + i, count));
                            break;
                        case "Maroon Meal":
                            maroonMealSeries.getData().add(new XYChart.Data<>("Hour " + i, count));
                            break;
                        case "Gift Card":
                            giftCardSeries.getData().add(new XYChart.Data<>("Hour " + i, count));
                            break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Add all series to the chart
        transactionTypeChart.getData().addAll(creditDebitSeries, diningDollarsSeries, maroonMealSeries, giftCardSeries);
        return transactionTypeChart;
    }

    //transaction type for z report
    private LineChart<String, Number> transactionTypes_zreport(LocalDate selectedDate) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time (Hours)");
    
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Transaction Count");
    
        LineChart<String, Number> transactionTypeChart = new LineChart<>(xAxis, yAxis);
        transactionTypeChart.setTitle("Transaction Types per Hour on " + selectedDate);
    
        // Create separate series for each transaction type
        XYChart.Series<String, Number> creditDebitSeries = new XYChart.Series<>();
        creditDebitSeries.setName("Credit/Debit");
    
        XYChart.Series<String, Number> diningDollarsSeries = new XYChart.Series<>();
        diningDollarsSeries.setName("Dining Dollars");
    
        XYChart.Series<String, Number> maroonMealSeries = new XYChart.Series<>();
        maroonMealSeries.setName("Maroon Meal");
    
        XYChart.Series<String, Number> giftCardSeries = new XYChart.Series<>();
        giftCardSeries.setName("Gift Card");
    
        // Fetch the data for each hour and populate the series
        for (int i = 9; i < 21; i++) {
            String selectQuery = "SELECT transaction_type, COUNT(*) AS type_count FROM transaction WHERE transaction_date = ? AND EXTRACT(HOUR FROM transaction_time) BETWEEN ? AND ? GROUP BY transaction_type";
            try (Connection conn = Database.connect();
                 PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setDate(1, java.sql.Date.valueOf(selectedDate));
                stmt.setInt(2, i);
                stmt.setInt(3, i + 1);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String transactionType = rs.getString("transaction_type");
                    int count = rs.getInt("type_count");
                    switch (transactionType) {
                        case "Credit/Debit":
                            creditDebitSeries.getData().add(new XYChart.Data<>("Hour " + i, count));
                            break;
                        case "Dining Dollars":
                            diningDollarsSeries.getData().add(new XYChart.Data<>("Hour " + i, count));
                            break;
                        case "Maroon Meal":
                            maroonMealSeries.getData().add(new XYChart.Data<>("Hour " + i, count));
                            break;
                        case "Gift Card":
                            giftCardSeries.getData().add(new XYChart.Data<>("Hour " + i, count));
                            break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    
        // Add all the series to the chart
        transactionTypeChart.getData().addAll(creditDebitSeries, diningDollarsSeries, maroonMealSeries, giftCardSeries);
    
        return transactionTypeChart;
    }
}