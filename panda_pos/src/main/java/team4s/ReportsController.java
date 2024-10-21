package team4s;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

import javax.imageio.ImageIO;

import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ReportsController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Connection conn;
    private Node selectedGraph;
    private XYChart<String, Number> selectedChart;

    @FXML
    private ComboBox<String> graphTypeComboBox;
    @FXML
    private VBox inputArea;
    @FXML
    private VBox chartArea;
    @FXML
    private Button returnToManagerButton;
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

    /**
     * Initialize the report gui by setting up the customization features (and
     * hiding most of them initially) and populating the different graph types into
     * the dropdown
     */
    @FXML
    public void initialize() {
        populateGraphTypeComboBox();
        datePicker.setValue(LocalDate.now());
        datePicker.setVisible(false);
        startTimeComboBox.setVisible(false);
        endTimeComboBox.setVisible(false);
        startDay.setValue(LocalDate.now());
        startDay.setVisible(false);
        endDay.setValue(LocalDate.now());
        endDay.setVisible(false);

        // Populate the start and end time ComboBoxes with values from 9 to 21 (9 AM to
        // 9 PM)
        startTimeComboBox.setItems(
                FXCollections.observableArrayList(IntStream.rangeClosed(9, 21).boxed().collect(Collectors.toList())));
        endTimeComboBox.setItems(
                FXCollections.observableArrayList(IntStream.rangeClosed(9, 21).boxed().collect(Collectors.toList())));

        startTimeComboBox.setValue(9);
        endTimeComboBox.setValue(21);
    }
    // creates the dropdown menu for the graph types

    /**
     * populates the dropdown menu for the different graph types that can be made
     */
    public void populateGraphTypeComboBox() {
        graphTypeComboBox
                .setItems(FXCollections.observableArrayList("Product Usage", "X-report", "Z-report", "custom"));
        graphTypeComboBox.setOnAction(this::handleGraphTypeSelection);
    }

    /**
     * updates the gui based on the selected graph type and allows the user to
     * change the date and time parameters
     * also displays a warning for when custom is selected as the graph type,
     * because we were unable to implement it in time
     * 
     * @param event the action event from the button click
     */
    @FXML
    public void handleGraphTypeSelection(ActionEvent event) {
        String selectedGraphType = graphTypeComboBox.getValue();

        if ("Product Usage".equals(selectedGraphType)) {
            startDayLabel.setVisible(true);
            startDay.setVisible(true);
            endDayLabel.setVisible(true);
            endDay.setVisible(true);
            startTimeLabel.setVisible(true);
            endTimeLabel.setVisible(true);
            startTimeComboBox.setVisible(true);
            endTimeComboBox.setVisible(true);
        } else if ("X-report".equals(selectedGraphType)) {
            dateLabel.setVisible(true);
            datePicker.setVisible(true);
            startTimeLabel.setVisible(true);
            endTimeLabel.setVisible(true);
            startTimeComboBox.setVisible(true);
            endTimeComboBox.setVisible(true);
        } else if ("Z-report".equals(selectedGraphType)) {
            datePicker.setVisible(true);
        } else if ("custom".equals(selectedGraphType)) {
            // Warning popup that custom is not currently implemented
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Future feature");
            alert.setHeaderText("Custom is not currently available.");
            alert.setContentText("Hopefully we can make it a feature in project 3!");
            alert.showAndWait();
        }
    }

    /**
     * determines what graph to create, based on all of the inputted data
     * also displays a warning if the start time value is larger than the end time
     * value
     * as well as another warning if the start day is after the end day
     * 
     * @param event the action event from the button click
     */
    @FXML
    public void generateReport(ActionEvent event) {
        chartArea.getChildren().clear();
        String graphType = graphTypeComboBox.getValue();
        // get the current day in formatter, and the current hour in formatter2
        LocalDate selectedDate = datePicker.getValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = selectedDate.format(formatter);
        if (startTimeComboBox.getValue() > endTimeComboBox.getValue()) {
            // alerts if the user picks a starting hour that is after the end hour
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid End time");
            alert.setHeaderText("Start time must come before end time.");
            alert.setContentText(
                    "Please change the start and/or end time to make the start time come earlier. Then try again!");
            alert.showAndWait();
        }
        switch (graphType) {
            case "X-report":
                Node salesChart = wrapGraphForSelection(
                        x_report_hourly_sales(selectedDate, startTimeComboBox.getValue(), endTimeComboBox.getValue()));
                Node itemsChart = wrapGraphForSelection(
                        x_report_items_sold(selectedDate, startTimeComboBox.getValue(), endTimeComboBox.getValue()));
                Node typesChart = wrapGraphForSelection(transactionTypes_xreport(selectedDate,
                        startTimeComboBox.getValue(), endTimeComboBox.getValue()));
                chartArea.getChildren().addAll(salesChart, itemsChart, typesChart);
                break;
            case "Z-report":
                Node zSalesChart = wrapGraphForSelection(z_report_hourly_sales(selectedDate));
                Node zItemsChart = wrapGraphForSelection(z_report_items_sold(selectedDate));
                Node zTypesChart = wrapGraphForSelection(transactionTypes_zreport(selectedDate));
                chartArea.getChildren().addAll(zSalesChart, zItemsChart, zTypesChart);
                break;
            case "Product Usage":
                if (startDay.getValue().isAfter(endDay.getValue())) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Invalid Days");
                    alert.setHeaderText("Start day must come before end day.");
                    alert.setContentText(
                            "Please change the start and/or end day to make the start day come first. Then try again!");
                    alert.showAndWait();
                }
                List<BarChart<String, Number>> unitBarCharts = productUsageChart(startDay.getValue(), endDay.getValue(),
                        startTimeComboBox.getValue(), endTimeComboBox.getValue());

                for (BarChart<String, Number> barChart : unitBarCharts) {
                    Node wrappedBarChart = wrapGraphForSelection(barChart);
                    chartArea.getChildren().add(wrappedBarChart);
                }
                break;
            case "custom":
                System.out.println("Will be included in future update...");
            default:
                System.out.println("Unknown graph type selected.");
        }
    }

    /**
     * Generates product usage charts based on ingredient usage data within a
     * specified timeframe
     * 
     * @param startDate the start date that the query will find
     * @param endDate   the last day that the query will collect data for
     * @param startHour the hour for each day that the query will begin collecting
     *                  data for (inclusive)
     * @param endHour   the hour at which the query data collection will stop
     *                  (exclusive)
     * @return a list of BarCharts that are classified by their unit type
     */
    public List<BarChart<String, Number>> productUsageChart(LocalDate startDate, LocalDate endDate, int startHour,
            int endHour) {
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
                unitBarChart.setTitle(
                        "Inventory Usage for Unit: " + unit + " (from " + startHour + ":00 to " + endHour + ":00)");
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

    /**
     * generates the line chart for an X report in order to display total sales per
     * hour within a given time frame and a specific day
     * 
     * @param selectedDate the date that will be used in the query in order to
     *                     process the correct data
     * @param startHour    the hour for each day that the query will begin
     *                     collecting data for (inclusive)
     * @param endHour      the hour at which the query data collection will stop
     *                     (exclusive)
     * @return a lineChart that depicts that total sales per hour for a given
     *         timeframe for a specific day
     */
    public LineChart<String, Number> x_report_hourly_sales(LocalDate selectedDate, int startHour, int endHour) {

        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH");

        // convert it into an hour to safeguard against generating x report after 9 PM
        int current_hour = Integer.parseInt(currentTime.format(formatter2));
        if (current_hour > 21 && selectedDate.equals(LocalDate.now())) {
            // shows warning about generating x report after 9 PM, gives option to override
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
            } else {
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
                stmt.setInt(3, i + 1);
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

    /**
     * generates the line chart for a Z report in order to display total sales per
     * hour for a specific day
     * 
     * @param selectedDate the date that will be used in the query in order to
     *                     process the correct data
     * @return a lineChart that depicts that total sales per hour for an entire
     *         workday
     */
    public LineChart<String, Number> z_report_hourly_sales(LocalDate selectedDate) {

        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH");

        // convert it into an hour to safeguard against generating z report before 9 PM
        int current_hour = Integer.parseInt(currentTime.format(formatter2));
        if (current_hour < 21 && selectedDate.equals(LocalDate.now())) {
            // shows warning about generating z report before 9 PM, gives option to override
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
            } else {
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
                stmt.setInt(3, i + 1);
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

    /**
     * generates the bar chart for items sold per hour for a specific time frame on
     * a specific day
     * 
     * @param selectedDate the date that will be used in the query in order to
     *                     process the correct data
     * @param startHour    the hour for each day that the query will begin
     *                     collecting data for (inclusive)
     * @param endHour      the hour at which the query data collection will stop
     *                     (exclusive)
     * @return a barChart that depicts the number of items sold per hour for a given
     *         timeframe for a specific day
     */
    public BarChart<String, Number> x_report_items_sold(LocalDate selectedDate, int startHour, int endHour) {
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
                stmt.setInt(3, i + 1);
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

    /**
     * generates the bar chart for items sold per hour for a specific day
     * 
     * @param selectedDate the date that will be used in the query in order to
     *                     process the correct data
     * @return a barChart that depicts the number of items sold per hour for all
     *         work hours specific day
     */
    public BarChart<String, Number> z_report_items_sold(LocalDate selectedDate) {
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
                stmt.setInt(3, i + 1);
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

    /**
     * generates a line chart showing each of the transaction types and how many
     * times they are used each hour for a specific time frame on a specific day
     * 
     * @param selectedDate the date that will be used in the query in order to
     *                     process the correct data
     * @param startHour    the hour for each day that the query will begin
     *                     collecting data for (inclusive)
     * @param endHour      the hour at which the query data collection will stop
     *                     (exclusive)
     * @return a LineChart displaying the number of transactions per hour for each
     *         transaction type for a given time window on a specific day
     */
    public LineChart<String, Number> transactionTypes_xreport(LocalDate selectedDate, int startHour, int endHour) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time (Hours)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Transaction Count");

        LineChart<String, Number> transactionTypeChart = new LineChart<>(xAxis, yAxis);
        transactionTypeChart
                .setTitle("Transaction Types from Hour " + startHour + " to Hour " + endHour + " on " + selectedDate);

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

    /**
     * generates a line chart showing each of the transaction types and how many
     * times they are used each hour for a specific day
     * 
     * @param selectedDate the date that will be used in the query in order to
     *                     process the correct data
     * @return a LineChart displaying the number of transactions per hour for each
     *         transaction type for a specific day
     */
    public LineChart<String, Number> transactionTypes_zreport(LocalDate selectedDate) {
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

    /**
     * exports the selected chart as a PNG image
     * 
     * @param chart    the created report that is selected and will be exported
     * @param fileName the name of the file that the chart image will be saved as on
     *                 your computer
     */
    public void exportAsPNG(Node chart, String fileName) {
        // Create a FileChooser to specify where to save the image
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Chart Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        // Show save dialog
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            // Capture the snapshot of the chart
            WritableImage image = chart.snapshot(new SnapshotParameters(), null);

            // Write the image to the file
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                System.out.println("Chart saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * exports the selected chart as a CSV file
     * 
     * @param chart the selected chart in a XYChart format so that it can be
     *              exported as a CSV
     */
    public void exportAsCSV(XYChart<String, Number> chart) {
        // File chooser dialog for saving the CSV
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Chart Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(chartArea.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write chart data to CSV
                writer.append("X-Axis, Y-Axis\n");
                for (XYChart.Series<String, Number> series : chart.getData()) {
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        writer.append(data.getXValue()).append(",").append(data.getYValue().toString()).append("\n");
                    }
                }
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the export of the currently selected graph as a PNG file
     * Also displays a warning if no graph is selected
     * 
     * @param event the action event from the button click
     */
    @FXML
    public void handleExportAsPNG(ActionEvent event) {
        if (selectedGraph != null) {
            WritableImage image = selectedGraph.snapshot(new SnapshotParameters(), null);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Graph as PNG");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
            File file = fileChooser.showSaveDialog(selectedGraph.getScene().getWindow());

            if (file != null) {
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                    System.out.println("Graph saved as PNG: " + file.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Keep the graph highlighted after exporting
            ((VBox) selectedGraph.getParent()).setStyle("-fx-border-color: blue; -fx-border-width: 3;");
        } else {
            // Warning popup if no chart is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Graph");
            alert.setHeaderText("A graph is not selected.");
            alert.setContentText("Please select a graph and try again!");
            alert.showAndWait();
        }
    }

    /**
     * wraps each of the graphs into their own VBox so that one of them can be
     * selected, and then exported
     * 
     * @param graph the graph that is being wrapped, so that it can be signalled as
     *              selected
     * @return a VBOX containing the selected graph
     */
    public VBox wrapGraphForSelection(Node graph) {
        VBox graphContainer = new VBox(graph);
        graphContainer.setStyle("-fx-border-color: transparent; -fx-border-width: 2;");

        graphContainer.setOnMouseClicked(event -> {
            // Clear selection from previously selected graph
            if (selectedGraph != null) {
                ((VBox) selectedGraph.getParent()).setStyle("-fx-border-color: transparent; -fx-border-width: 2;");
            }

            // Highlight the selected graph
            graphContainer.setStyle("-fx-border-color: blue; -fx-border-width: 3;");

            // Store the selected graph
            selectedGraph = graph;

            if (graph instanceof XYChart) {
                selectedChart = (XYChart<String, Number>) graph;
            } else {
                selectedChart = null;
            }
        });

        return graphContainer;
    }

    /**
     * Handles the export of the currently selected graph as a CSV file
     * Also displays a warning if no graph is selected
     * 
     * @param event the action event from the button click
     */
    @FXML
    public void handleExportAsCSV(ActionEvent event) {
        if (selectedChart == null) {
            // Warning popup if no chart is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Chart Selected");
            alert.setHeaderText("Please select a chart to export.");
            alert.setContentText("You must select a chart before exporting it as a CSV.");
            alert.showAndWait();
        } else {
            // Export selected chart data as CSV
            exportAsCSV(selectedChart);
        }
    }

    /**
     * used to reset the reports ui back to its original form
     * 
     * @param event the action event from the button click
     */
    @FXML
    public void resetForm(ActionEvent event) {
        graphTypeComboBox.getSelectionModel().clearSelection();

        datePicker.setValue(LocalDate.now());
        datePicker.setVisible(false);
        dateLabel.setVisible(false);
        startTimeComboBox.setVisible(false);
        startTimeLabel.setVisible(false);
        endTimeComboBox.setVisible(false);
        endTimeLabel.setVisible(false);
        startDayLabel.setVisible(false);
        startDay.setVisible(false);
        endDayLabel.setVisible(false);
        endDay.setVisible(false);

        startDay.setValue(LocalDate.now());
        endDay.setValue(LocalDate.now());
        startTimeComboBox.setValue(9);
        endTimeComboBox.setValue(21);

        // Clear the chart area
        chartArea.getChildren().clear();
    }

    /**
     * used to switches the screen back to the main manager page
     * 
     * @param event the action event from the button click
     */
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