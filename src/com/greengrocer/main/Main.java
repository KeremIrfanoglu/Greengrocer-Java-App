package com.greengrocer.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the Login FXML
        // Note: Using file path for simplicity in non-modular/resource-tricky setup, or
        // use standard resource loading.
        // Trying standard resource loading first.
        URL url = new File("src/com/greengrocer/views/login.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url);

        primaryStage.setTitle("Greengrocer Login");
        primaryStage.setScene(new Scene(root, 960, 540));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
