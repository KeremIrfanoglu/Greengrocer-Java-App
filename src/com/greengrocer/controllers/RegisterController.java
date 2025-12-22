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
    private PasswordField confirmPasswordField;
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
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();

        // Required fields validation
        if (username.isEmpty()) {
            showError("Username is required.");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters.");
            return;
        }

        // Username must be lowercase only
        if (!username.equals(username.toLowerCase())) {
            showError("Username must be lowercase only (no capital letters).");
            return;
        }

        if (password.isEmpty()) {
            showError("Password is required.");
            return;
        }

        // Strong password validation
        String passwordError = validatePassword(password);
        if (passwordError != null) {
            showError(passwordError);
            return;
        }

        // Confirm password check
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showError("First name and last name are required.");
            return;
        }

        // Phone validation (optional but if provided must be valid)
        if (!phone.isEmpty()) {
            String phoneError = validatePhone(phone);
            if (phoneError != null) {
                showError(phoneError);
                return;
            }
        }

        try {
            boolean success = userDAO.register(username, password, "customer", firstName, lastName, address, phone);
            if (success) {
                statusLabel.setText("✅ Registration successful! Please go back to login.");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                // Clear fields
                usernameField.clear();
                passwordField.clear();
                confirmPasswordField.clear();
                firstNameField.clear();
                lastNameField.clear();
                addressField.clear();
                phoneField.clear();
            } else {
                showError("Username already taken. Please choose another.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error: " + e.getMessage());
        }
    }

    /**
     * Validate password strength
     * 
     * @return error message or null if valid
     */
    private String validatePassword(String password) {
        if (password.length() < 6) {
            return "Password must be at least 6 characters.";
        }

        boolean hasUppercase = false;
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUppercase = true;
            if (Character.isDigit(c))
                hasNumber = true;
        }

        if (!hasUppercase) {
            return "Password must contain at least 1 uppercase letter.";
        }

        if (!hasNumber) {
            return "Password must contain at least 1 number.";
        }

        return null; // Password is valid
    }

    private void showError(String message) {
        statusLabel.setText("❌ " + message);
        statusLabel.setStyle("-fx-text-fill: #f44336;");
    }

    /**
     * Validate phone number format
     * 
     * @return error message or null if valid
     */
    private String validatePhone(String phone) {
        // Remove spaces, dashes, and parentheses for validation
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");

        // Must be only digits (with optional + at start)
        if (!cleanPhone.matches("^\\+?[0-9]{10,15}$")) {
            return "Phone number must be 10-15 digits (e.g., 5551234567).";
        }

        return null; // Phone is valid
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
