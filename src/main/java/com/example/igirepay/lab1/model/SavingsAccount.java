package com.example.igirepay.lab1.model;
import java.time.LocalDateTime;

public class  SavingsAccount extends Account {

    private double interestRate;
    private double minimumBalance;
    private int withdrawalLimitPerMonth;
    private int withdrawalsThisMonth;
    private LocalDateTime lastInterestApplied;

    public SavingsAccount(int id, Customer customer, String accountType, double balance, LocalDateTime createdAt, double interestRate, double minimumBalance, int withdrawalLimitPerMonth, int withdrawalsThisMonth, LocalDateTime lastInterestApplied) {
        super(id, customer, "SAVINGS", balance, createdAt);
        this.interestRate = interestRate;
        this.minimumBalance = minimumBalance;
        this.withdrawalLimitPerMonth = withdrawalLimitPerMonth;
        this.withdrawalsThisMonth = withdrawalsThisMonth;
        this.lastInterestApplied = lastInterestApplied;
    }



    @Override
    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Invalid deposit amount");
            return;
        }

        super.deposit(amount);
        System.out.println("Savings deposit processed (may earn interest later)");
    }

    @Override
    public void withdraw(double amount) {

        if (amount <= 0) {
            System.out.println("Invalid withdrawal amount");
            return;
        }

        if (withdrawalsThisMonth >= withdrawalLimitPerMonth) {
            System.out.println("Withdrawal limit reached for this month");
            return;
        }

        if (getBalance() - amount < minimumBalance) {
            System.out.println("Cannot go below minimum balance");
            return;
        }

        super.withdraw(amount);
        withdrawalsThisMonth++;

        System.out.println("Savings withdrawal processed with restrictions");
    }

    @Override
    public void processTransaction() {
        System.out.println("Savings transaction processed with validation rules");
    }





    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    public int getWithdrawalLimitPerMonth() {
        return withdrawalLimitPerMonth;
    }

    public void setWithdrawalLimitPerMonth(int withdrawalLimitPerMonth) {
        this.withdrawalLimitPerMonth = withdrawalLimitPerMonth;
    }

    public int getWithdrawalsThisMonth() {
        return withdrawalsThisMonth;
    }

    public void setWithdrawalsThisMonth(int withdrawalsThisMonth) {
        this.withdrawalsThisMonth = withdrawalsThisMonth;
    }

    public LocalDateTime getLastInterestApplied() {
        return lastInterestApplied;
    }

    public void setLastInterestApplied(LocalDateTime lastInterestApplied) {
        this.lastInterestApplied = lastInterestApplied;
    }

    @Override
    public String toString() {
        return "SavingsAccount{" +
                "id=" + getId() +
                ", customer=" + getCustomer() +
                ", balance=" + getBalance() +
                ", interestRate=" + interestRate +
                ", minimumBalance=" + minimumBalance +
                ", withdrawalLimitPerMonth=" + withdrawalLimitPerMonth +
                ", withdrawalsThisMonth=" + withdrawalsThisMonth +
                ", lastInterestApplied=" + lastInterestApplied +
                '}';
    }
}
