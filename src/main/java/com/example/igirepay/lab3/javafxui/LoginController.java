package com.example.igirepay.lab3.javafxui;

import com.example.igirepay.lab1.model.Customer;
import com.example.igirepay.lab3.service.CustomerService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField pinField;

    @FXML
    private Label errorLabel;

    private final CustomerService customerService = new CustomerService();

    @FXML
    private void handleLogin() {

        errorLabel.setText("");

        String phone = phoneField.getText().trim();
        String pin   = pinField.getText().trim();

        if (phone.isEmpty()) {
            errorLabel.setText("Please enter your phone number.");
            return;
        }

        if (pin.isEmpty()) {
            errorLabel.setText("Please enter your PIN.");
            return;
        }

        try {

            Customer customer = customerService.getCustomerByPhoneAndPin(phone, pin);

            if (customer == null) {
                errorLabel.setText("Incorrect phone number or PIN. Please try again.");
                return;
            }

            openDashboard(customer);

        } catch (Exception e) {
            errorLabel.setText("Unable to connect. Please check your connection and try again.");
        }
    }

    @FXML
    private void handleRegister() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/igirepay_payment_gateway_project/register-view.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) pinField.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 600));
            stage.setTitle("IgirePay Register");

        } catch (Exception e) {
            errorLabel.setText("Cannot open register page.");
        }
    }

    private void openDashboard(Customer customer) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/igirepay_payment_gateway_project/dashboard-view.fxml")
            );

            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.initData(customer);

            Stage stage = (Stage) pinField.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 700));
            stage.setTitle("IgirePay Dashboard");

        } catch (Exception e) {
            errorLabel.setText("Cannot open dashboard.");
        }
    }
}