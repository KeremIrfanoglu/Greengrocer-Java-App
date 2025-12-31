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

        // Username max length check
        if (username.length() > 20) {
            showError("Username must be at most 20 characters.");
            return;
        }

        // Username must be lowercase letters and numbers only (no special chars, no
        // uppercase)
        if (!username.matches("^[a-z0-9]+$")) {
            showError("Username must contain only lowercase letters and numbers (no special characters).");
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

        // Name length validation
        if (firstName.length() < 2 || firstName.length() > 50) {
            showError("First name must be 2-50 characters.");
            return;
        }

        if (lastName.length() < 2 || lastName.length() > 50) {
            showError("Last name must be 2-50 characters.");
            return;
        }

        // Name format validation (letters and spaces only)
        if (!firstName.matches("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$")) {
            showError("First name can only contain letters.");
            return;
        }

        if (!lastName.matches("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$")) {
            showError("Last name can only contain letters.");
            return;
        }

        // Address length validation (optional but if provided, max 200 chars)
        if (address.length() > 200) {
            showError("Address must be at most 200 characters.");
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
                statusLabel.setText("Registration successful! Please go back to login.");
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
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #f44336;");
    }

    /**
     * Validate phone number format
     * Supports Turkish (+90) and international formats
     * 
     * @return error message or null if valid
     */
    private String validatePhone(String phone) {
        // Remove spaces, dashes, parentheses, and dots for validation
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");

        // Empty check (phone is optional)
        if (cleanPhone.isEmpty()) {
            return null;
        }

        // Check for invalid characters (only digits and optional + at start)
        if (!cleanPhone.matches("^\\+?[0-9]+$")) {
            return "Phone number can only contain digits, spaces, dashes, and optional + prefix.";
        }

        // Turkish phone number validation
        if (cleanPhone.startsWith("+90")) {
            // Turkish format: +90 XXX XXX XX XX (10 digits after +90)
            String turkishNumber = cleanPhone.substring(3);
            if (turkishNumber.length() != 10) {
                return "Turkish phone number must be 10 digits after +90 (e.g., +90 555 123 4567).";
            }
        } else if (cleanPhone.startsWith("90") && cleanPhone.length() == 12) {
            // Turkish format without +: 90 XXX XXX XX XX
            // Valid format - 10 digits after 90
        } else if (cleanPhone.startsWith("0") && cleanPhone.length() == 11) {
            // Local Turkish format: 0XXX XXX XX XX
            // Valid format
        } else if (cleanPhone.length() == 10 && !cleanPhone.startsWith("+")) {
            // Short format: XXX XXX XX XX (10 digits)
            // Valid format
        } else if (cleanPhone.startsWith("+")) {
            // Other international format
            String intlNumber = cleanPhone.substring(1);
            if (intlNumber.length() < 10 || intlNumber.length() > 15) {
                return "International phone number must be 10-15 digits (e.g., +1 555 123 4567).";
            }
        } else {
            // Generic format - must be 10-15 digits
            if (cleanPhone.length() < 10 || cleanPhone.length() > 15) {
                return "Phone number must be 10-15 digits.";
            }
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
            stage.setScene(StyleHelper.createStyledScene(root, 1200, 750));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
