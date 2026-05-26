package com.example.igirepay.lab3.javafxui;

import com.example.igirepay.lab1.model.Transaction;
import com.example.igirepay.lab3.exception.IgirePayException;
import com.example.igirepay.lab3.exception.InsufficientBalanceException;
import com.example.igirepay.lab3.service.PaymentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * JavaFX controller for Lab3 UI. Wires PaymentService to UI controls.
 * Corresponding FXML: payment-view.fxml
 */
public class PaymentController {

    @FXML private TextField accountIdField;
    @FXML private TextField amountField;
    @FXML private TextField toAccountIdField;
    @FXML private Label statusLabel;
    @FXML private TextArea historyArea;

    private final PaymentService paymentService = new PaymentService();

    @FXML
    protected void onDeposit() {
        try {
            int accountId = parseAccountId(accountIdField.getText());
            double amount = parseAmount(amountField.getText());

            String ref = paymentService.deposit(accountId, amount);
            setStatus("Deposit successful! Reference: " + ref, true);
            refreshHistory(accountId);

        } catch (IgirePayException e) {
            setStatus("Deposit failed: " + e.getMessage(), false);
        }
    }

    @FXML
    protected void onWithdraw() {
        try {
            int accountId = parseAccountId(accountIdField.getText());
            double amount = parseAmount(amountField.getText());

            String ref = paymentService.withdraw(accountId, amount);
            setStatus("Withdrawal successful! Reference: " + ref, true);
            refreshHistory(accountId);

        } catch (InsufficientBalanceException e) {
            setStatus("Insufficient funds: " + e.getMessage(), false);
        } catch (IgirePayException e) {
            setStatus("Withdrawal failed: " + e.getMessage(), false);
        }
    }

    @FXML
    protected void onTransfer() {
        try {
            int fromId = parseAccountId(accountIdField.getText());
            int toId = parseAccountId(toAccountIdField.getText());
            double amount = parseAmount(amountField.getText());

            String ref = paymentService.transfer(fromId, toId, amount);
            setStatus("Transfer successful! Reference: " + ref, true);
            refreshHistory(fromId);

        } catch (InsufficientBalanceException e) {
            setStatus("Transfer rolled back — " + e.getMessage(), false);
        } catch (IgirePayException e) {
            setStatus("Transfer failed: " + e.getMessage(), false);
        }
    }

    @FXML
    protected void onCheckBalance() {
        try {
            int accountId = parseAccountId(accountIdField.getText());
            var account = paymentService.getAccount(accountId);
            setStatus(String.format("Balance: %.2f RWF  |  Type: %s",
                    account.getBalance(), account.getAccountType()), true);
        } catch (IgirePayException e) {
            setStatus(e.getMessage(), false);
        }
    }

    @FXML
    protected void onViewHistory() {
        try {
            int accountId = parseAccountId(accountIdField.getText());
            refreshHistory(accountId);
        } catch (IgirePayException e) {
            setStatus(e.getMessage(), false);
        }
    }

    private void refreshHistory(int accountId) {
        List<Transaction> txs = paymentService.getTransactionHistory(accountId);
        StringBuilder sb = new StringBuilder();
        if (txs.isEmpty()) {
            sb.append("No transactions found.");
        } else {
            txs.forEach(tx -> sb.append(String.format(
                    "[%s] %.2f RWF | Ref: %s | %s%n",
                    tx.getTransactionType(),
                    tx.getAmount(),
                    tx.getReferenceId(),
                    tx.getTimestamp()
            )));
        }
        historyArea.setText(sb.toString());
    }

    private void setStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private int parseAccountId(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IgirePayException("Invalid account ID: " + text);
        }
    }

    private double parseAmount(String text) {
        try {
            double val = Double.parseDouble(text.trim());
            if (val <= 0) throw new IgirePayException("Amount must be positive");
            return val;
        } catch (NumberFormatException e) {
            throw new IgirePayException("Invalid amount: " + text);
        }
    }
}
