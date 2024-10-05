package team4s;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Manager extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Manager");
        VBox clerkRoot = new VBox();
        clerkRoot.setSpacing(10);
        clerkRoot.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> goBackToMainMenu(primaryStage));

        clerkRoot.getChildren().add(backButton);
        Scene clerkScene = new Scene(clerkRoot, 400, 400);
        primaryStage.setScene(clerkScene);

        primaryStage.setMaximized(true);
    }

    private void goBackToMainMenu(Stage primaryStage) {
        App app = new App();
        app.createMainMenuScene();

        primaryStage.setMaximized(true);
    }
}
