package com.example.igirepay.lab3.exception;

public class InsufficientBalanceException extends RuntimeException {
    private final double available;
    private final double requested;

    public InsufficientBalanceException(double available, double requested) {
        super(String.format("Insufficient balance. Available: %.2f RWF, Requested: %.2f RWF", available, requested));
        this.available = available;
        this.requested = requested;
    }
    public double getAvailable() { return available; }
    public double getRequested() { return requested; }
}
