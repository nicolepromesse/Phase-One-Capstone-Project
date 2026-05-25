package com.example.igirepay.lab1.model;
import java.time.LocalDateTime;

public class WalletAccount extends Account {


    private double dailyTransferLimit;
    private double transactionFee;
    private boolean active;
    private double dailyTransferredAmount;
    private LocalDateTime lastResetDate;

    public WalletAccount(int id, Customer customer, double balance,LocalDateTime createdAt, double dailyTransferLimit, double transactionFee, boolean active, double dailyTransferredAmount, LocalDateTime lastResetDate){
        super(id, customer, "WALLET", balance, createdAt);
        this.dailyTransferLimit = dailyTransferLimit;
        this.transactionFee = transactionFee;
        this.active = active;
        this.dailyTransferredAmount = dailyTransferredAmount;
        this.lastResetDate = lastResetDate;
    }



    @Override
    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Invalid deposit amount");
            return;
        }

        super.deposit(amount);
        System.out.println("Wallet deposit processed instantly");
    }

    @Override
    public void withdraw(double amount) {

        if (!active) {
            System.out.println("Wallet account is inactive");
            return;
        }

        double totalAmount = amount + transactionFee;

        if (getBalance() < totalAmount) {
            System.out.println("Insufficient balance (including fee)");
            return;
        }

        resetDailyLimit();

        if (dailyTransferredAmount + amount > dailyTransferLimit) {
            System.out.println("Daily transfer limit exceeded");
            return;
        }

        super.withdraw(totalAmount);
        dailyTransferredAmount += amount;

        System.out.println("Wallet withdrawal processed instantly");
    }

    @Override
    public void processTransaction() {
        System.out.println("Wallet transaction processed instantly");
    }

    public void resetDailyLimit() {
        LocalDateTime now = LocalDateTime.now();

        if (lastResetDate == null || lastResetDate.toLocalDate().isBefore(now.toLocalDate())) {
            dailyTransferredAmount = 0;
            lastResetDate = now;
        }
    }

    public double getDailyTransferLimit() {
        return dailyTransferLimit;
    }

    public void setDailyTransferLimit(double dailyTransferLimit) {
        this.dailyTransferLimit = dailyTransferLimit;
    }

    public double getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(double transactionFee) {
        this.transactionFee = transactionFee;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getDailyTransferredAmount() {
        return dailyTransferredAmount;
    }

    public void setDailyTransferredAmount(double dailyTransferredAmount) {
        this.dailyTransferredAmount = dailyTransferredAmount;
    }

    public LocalDateTime getLastResetDate() {
        return lastResetDate;
    }

    public void setLastResetDate(LocalDateTime lastResetDate) {
        this.lastResetDate = lastResetDate;
    }


    @Override
    public String toString() {
        return "WalletAccount{" +
                "id=" + getId() +
                ", customer=" + getCustomer() +
                ", balance=" + getBalance() +
                ", dailyTransferLimit=" + dailyTransferLimit +
                ", transactionFee=" + transactionFee +
                ", active=" + active +
                ", dailyTransferredAmount=" + dailyTransferredAmount +
                ", lastResetDate=" + lastResetDate +
                '}';
    }
}
