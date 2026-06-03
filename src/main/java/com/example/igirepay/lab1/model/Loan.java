package com.example.igirepay.lab1.model;

import java.time.LocalDateTime;

public class Loan {
    private int id;
    private Account account;
    private Customer customer;
    private double requestedAmount;
    private double approvedAmount;
    private double interestRate;
    private double repaidAmount;
    private String status;
    private String referenceId;
    private LocalDateTime requestedAt;
    private LocalDateTime updatedAt;

    public Loan(int id, Account account, Customer customer, double requestedAmount,
                double approvedAmount, double interestRate, double repaidAmount,
                String status, String referenceId, LocalDateTime requestedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.account = account;
        this.customer = customer;
        this.requestedAmount = requestedAmount;
        this.approvedAmount = approvedAmount;
        this.interestRate = interestRate;
        this.repaidAmount = repaidAmount;
        this.status = status;
        this.referenceId = referenceId;
        this.requestedAt = requestedAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Account getAccount() { return account; }
    public Customer getCustomer() { return customer; }
    public double getRequestedAmount() { return requestedAmount; }
    public double getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(double approvedAmount) { this.approvedAmount = approvedAmount; }
    public double getInterestRate() { return interestRate; }
    public double getRepaidAmount() { return repaidAmount; }
    public void setRepaidAmount(double repaidAmount) { this.repaidAmount = repaidAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReferenceId() { return referenceId; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
