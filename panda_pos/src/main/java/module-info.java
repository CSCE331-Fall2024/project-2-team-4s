module team4s {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires javafx.swing;

    opens team4s to javafx.fxml;

    exports team4s;
}
