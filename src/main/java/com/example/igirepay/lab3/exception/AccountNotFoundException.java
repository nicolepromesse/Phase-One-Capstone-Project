package com.example.igirepay.lab3.exception;

public class AccountNotFoundException extends RuntimeException {
    private final int accountId;

    public AccountNotFoundException(int accountId) {
        super("Account not found with ID: " + accountId);
        this.accountId = accountId;
    }
    public int getAccountId() { return accountId; }
}
