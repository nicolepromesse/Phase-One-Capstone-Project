package com.example.igirepay.lab3.exception;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("Account is locked due to too many failed PIN attempts. Please contact support.");
    }
}
