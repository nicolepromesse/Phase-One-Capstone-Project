package com.example.igirepay.lab1.model;
import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private String referenceId;
    private double amount;
    private String transactionType; // DEPOSIT, WITHDRAW, TRANSFER
    private LocalDateTime timestamp;
    private Account account;

    public Transaction(int id, String referenceId, double amount, String transactionType, LocalDateTime timestamp, Account account) {
        this.id = id;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = timestamp;
        this.account = account;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", referenceId='" + referenceId + '\'' +
                ", amount=" + amount +
                ", transactionType='" + transactionType + '\'' +
                ", timestamp=" + timestamp +
                ", account=" + account +
                '}';
    }
}
