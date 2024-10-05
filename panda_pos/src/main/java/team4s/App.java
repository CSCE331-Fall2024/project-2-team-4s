package team4s;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
    static Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        mainStage.setTitle("App");

        createMainMenuScene();

        mainStage.setMaximized(true);
        mainStage.show();
    }

    public void createMainMenuScene() {
        VBox root = new VBox();
        root.setSpacing(10);
        root.setAlignment(Pos.CENTER);

        Button man = new Button("Manager");
        Button cahsier = new Button("Cashier");

        man.setOnAction(e -> openManager());
        cahsier.setOnAction(e -> openCashier());

        root.getChildren().addAll(man, cahsier);
        Scene scene = new Scene(root, 400, 400);
        mainStage.setScene(scene);

        mainStage.setMaximized(true);
    }

    public void openManager() {
        Manager managerUI = new Manager();
        managerUI.start(mainStage);

        mainStage.setMaximized(true);
    }

    public void openCashier() {
        Cashier cashierUI = new Cashier();
        cashierUI.start(mainStage);

        mainStage.setMaximized(true);
    }
}

