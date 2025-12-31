package com.greengrocer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.greengrocer.util.StyleHelper;

import java.io.File;
import java.net.URL;

/**
 * Main entry point for the Greengrocer JavaFX application.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the Login FXML
        URL url = new File("src/com/greengrocer/views/login.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url);

        primaryStage.setTitle("Greengrocer Login");
        Scene scene = new Scene(root, 960, 540);

        // Load CSS stylesheet
        URL cssUrl = new File("src/com/greengrocer/views/styles.css").toURI().toURL();
        scene.getStylesheets().add(cssUrl.toExternalForm());

        primaryStage.setScene(scene);
        StyleHelper.applyAppIcon(primaryStage);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
