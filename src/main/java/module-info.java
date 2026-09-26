module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    opens org.example to javafx.fxml;
    exports org.example;
    opens vision to javafx.fxml;
    exports vision;
    opens uf to javafx.fxml;
    exports uf;
    opens model to javafx.fxml;
    exports model;
}
