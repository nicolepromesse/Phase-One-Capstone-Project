package com.example.igirepay.lab1.model;
import java.time.LocalDateTime;

public abstract class Account {
    private int id;
    private Customer customer;
    private String accountType;
    private double balance;
    private LocalDateTime createdAt;

    public Account(int id, Customer customer, String accountType, double balance, LocalDateTime createdAt) {
        this.id = id;
        this.customer = customer;
        this.accountType = accountType;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public void withdraw(double amount) {
        balance -= amount;
    }

    public void processTransaction() {
        System.out.println("Processing transaction...");
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", customer=" + customer +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                ", createdAt=" + createdAt +
                '}';
    }
}
