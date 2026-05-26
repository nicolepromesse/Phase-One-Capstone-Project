package com.example.igirepay.lab3.exception;

public class DuplicateTransactionException extends RuntimeException {
    private final String referenceId;

    public DuplicateTransactionException(String referenceId) {
        super("Duplicate transaction detected. Reference ID already processed: " + referenceId);
        this.referenceId = referenceId;
    }
    public String getReferenceId() { return referenceId; }
}
