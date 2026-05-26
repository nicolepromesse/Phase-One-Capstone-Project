package com.example.igirepay.lab3.javafxui;

import com.example.igirepay.lab1.model.Customer;
import com.example.igirepay.lab3.service.CustomerService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField pinField;
    @FXML private PasswordField confirmPinField;
    @FXML private Label messageLabel;

    private final CustomerService customerService = new CustomerService();

    @FXML
    private void handleRegister() {
        messageLabel.setStyle("-fx-text-fill:#dc2626;");
        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pin   = pinField.getText().trim();
        String conf  = confirmPinField.getText().trim();

        if (name.isBlank() || email.isBlank() || phone.isBlank() || pin.isBlank()) {
            messageLabel.setText("All fields are required."); return;
        }
        if (!pin.matches("\\d{4,6}")) {
            messageLabel.setText("PIN must be 4–6 digits."); return;
        }
        if (!pin.equals(conf)) {
            messageLabel.setText("PINs do not match."); return;
        }

        try {
            customerService.register(new Customer(0, name, email, phone, pin));
            messageLabel.setStyle("-fx-text-fill:#16a34a;");
            messageLabel.setText("✓ Registered! You can now log in.");
            nameField.clear(); emailField.clear(); phoneField.clear(); pinField.clear(); confirmPinField.clear();
        } catch (SQLException e) {
            messageLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/igirepay_payment_gateway_project/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 600));
            stage.setTitle("IgirePay — Login");
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
        }
    }
}
