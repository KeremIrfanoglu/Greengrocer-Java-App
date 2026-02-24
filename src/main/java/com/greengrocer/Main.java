package com.greengrocer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.greengrocer.util.StyleHelper;

import java.net.URL;

/**
 * Main entry point for the Greengrocer JavaFX application.
 * 
 * @author Group10
 * @version 1.0
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the Login FXML from classpath
        URL url = getClass().getResource("/com/greengrocer/views/login.fxml");
        Parent root = FXMLLoader.load(url);

        primaryStage.setTitle("Greengrocer Login");
        Scene scene = new Scene(root, 960, 540);

        // Load CSS stylesheet from classpath
        URL cssUrl = getClass().getResource("/com/greengrocer/views/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setScene(scene);
        StyleHelper.applyAppIcon(primaryStage);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
