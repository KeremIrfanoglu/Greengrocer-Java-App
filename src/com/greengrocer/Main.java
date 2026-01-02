package com.greengrocer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.greengrocer.util.StyleHelper;
import com.greengrocer.controllers.SplashController;

import java.io.File;
import java.net.URL;

/**
 * Main entry point for the Greengrocer JavaFX application.
 * Shows a splash screen with video before the login screen.
 * 
 * @author Group10
 * @version 1.0
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Check if splash video exists
            File videoFile = new File("src/com/greengrocer/assets/intro.mp4");

            if (videoFile.exists()) {
                // Load splash screen with video
                URL splashUrl = new File("src/com/greengrocer/views/splash.fxml").toURI().toURL();
                FXMLLoader loader = new FXMLLoader(splashUrl);
                Parent splashRoot = loader.load();

                SplashController splashController = loader.getController();
                splashController.setStage(primaryStage);

                Scene splashScene = new Scene(splashRoot, 960, 540);

                primaryStage.setTitle("Greengrocer");
                primaryStage.setScene(splashScene);
                StyleHelper.applyAppIcon(primaryStage);
                primaryStage.centerOnScreen();
                primaryStage.show();

                // Setup key listener after scene is shown
                splashController.setupKeyListener();

            } else {
                // No video - go directly to login
                showLoginScreen(primaryStage);
            }

        } catch (Exception e) {
            System.err.println("Error loading splash screen: " + e.getMessage());
            e.printStackTrace();
            // Fallback to login screen
            showLoginScreen(primaryStage);
        }
    }

    /**
     * Shows the login screen directly (fallback when no splash video).
     */
    private void showLoginScreen(Stage primaryStage) throws Exception {
        // Start background music
        com.greengrocer.util.BackgroundMusicService.getInstance().play();

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
