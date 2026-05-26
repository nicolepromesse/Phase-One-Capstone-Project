package com.example.igirepay.lab2.util;

public class ValidationUtil {

    private ValidationUtil() {}

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return false;
        return phone.matches("^07[2-9]\\d{7}$");
    }

    public static boolean isValidPin(String pin) {
        if (pin == null) return false;
        return pin.matches("^\\d{4}$");
    }

    public static boolean isPositiveAmount(double amount) {
        return amount > 0;
    }

    public static boolean hasSufficientBalance(double balance, double amount) {
        return balance >= amount;
    }

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    public static void requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive, got: " + value);
        }
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
