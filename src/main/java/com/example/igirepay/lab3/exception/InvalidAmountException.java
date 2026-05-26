package com.example.igirepay.lab3.exception;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) {
        super(message);
    }
    public InvalidAmountException(double amount) {
        super(String.format("Invalid transaction amount: %.2f. Amount must be greater than zero.", amount));
    }
}
