package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LeafVisionApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                LeafVisionApp.class.getResource("main-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 1100, 700);

        stage.setTitle("Autumn Leaves Viewer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}