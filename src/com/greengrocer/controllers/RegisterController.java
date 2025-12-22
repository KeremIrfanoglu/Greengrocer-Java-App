package com.greengrocer.controllers;

import com.greengrocer.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.File;
import java.sql.SQLException;

import com.greengrocer.util.StyleHelper;

public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField phoneField;
    @FXML
    private Label statusLabel;

    private UserDAO userDAO;

    public RegisterController() {
        this.userDAO = new UserDAO();
    }

    @FXML
    public void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String address = addressField.getText();
        String phone = phoneField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and Password are required.");
            return;
        }

        // Basic validation could go here (e.g. strong password check)

        try {
            boolean success = userDAO.register(username, password, "customer", firstName, lastName, address, phone);
            if (success) {
                statusLabel.setText("Registration successful! Please go back to login.");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Registration failed. Username might be taken.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void handleBack() {
        try {
            // Navigate back to Login
            Stage stage = (Stage) usernameField.getScene().getWindow();
            // Use File URI for consistency
            Parent root = FXMLLoader.load(new File("src/com/greengrocer/views/login.fxml").toURI().toURL());
            stage.setTitle("Greengrocer Login");
            stage.setScene(StyleHelper.createStyledScene(root, 960, 540));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
