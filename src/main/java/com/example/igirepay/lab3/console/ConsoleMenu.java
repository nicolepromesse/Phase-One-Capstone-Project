package com.example.igirepay.lab3.console;

import com.example.igirepay.lab1.model.*;
import com.example.igirepay.lab2.daoimpl.*;
import com.example.igirepay.lab3.exception.*;
import com.example.igirepay.lab3.report.TransactionReport;
import com.example.igirepay.lab3.service.PaymentService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Lab3 enhanced console menu — wraps PaymentService with proper
 * exception handling and report generation.
 */
public class ConsoleMenu {

    private static final Scanner scanner = new Scanner(System.in);
    private static final PaymentService paymentService = new PaymentService();
    private static final CustomerDAOImpl customerDAO = new CustomerDAOImpl();
    private static final AccountDAOImpl accountDAO = new AccountDAOImpl();
    private static final TransactionDAOImpl transactionDAO = new TransactionDAOImpl();

    public static void run() {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> registerCustomer();
                case 2 -> createAccount();
                case 3 -> deposit();
                case 4 -> withdraw();
                case 5 -> transfer();
                case 6 -> viewBalance();
                case 7 -> viewTransactionHistory();
                case 8 -> viewFullReport();
                case 0 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n========================================");
        System.out.println("    IgirePay — Lab3 Enhanced Menu       ");
        System.out.println("========================================");
        System.out.println("1. Register Customer");
        System.out.println("2. Create Account (Wallet / Savings)");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Transfer");
        System.out.println("6. View Account Balance");
        System.out.println("7. View Transaction History");
        System.out.println("8. View Full Transaction Report");
        System.out.println("0. Exit");
        System.out.println("========================================");
    }

    private static void registerCustomer() {
        System.out.println("\n-- Register Customer --");
        System.out.print("Full Name: "); String name = scanner.nextLine();
        System.out.print("Email: "); String email = scanner.nextLine();
        System.out.print("Phone: "); String phone = scanner.nextLine();
        System.out.print("PIN (4 digits): "); String pin = scanner.nextLine();

        try {
            customerDAO.save(new Customer(0, name, email, phone, pin));
            System.out.println("Customer registered successfully.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void createAccount() {
        System.out.println("\n-- Create Account --");
        System.out.println("1. Wallet Account");
        System.out.println("2. Savings Account");
        int type = readInt("Choose type: ");

        int customerId = readInt("Customer ID: ");

        try {
            Customer customer = customerDAO.getById(customerId);
            if (customer == null) {
                System.out.println("Customer not found.");
                return;
            }

            double balance = readDouble("Initial Balance: ");

            if (type == 1) {
                double limit = readDouble("Daily Transfer Limit: ");
                double fee = readDouble("Transaction Fee: ");
                accountDAO.save(new WalletAccount(0, customer, balance, LocalDateTime.now(), limit, fee, true, 0, null));
            } else {
                double rate = readDouble("Interest Rate (%): ");
                double minBal = readDouble("Minimum Balance: ");
                int wLimit = readInt("Monthly Withdrawal Limit: ");
                accountDAO.save(new SavingsAccount(0, customer, "SAVINGS", balance, LocalDateTime.now(), rate, minBal, wLimit, 0, null));
            }

            System.out.println("Account created successfully.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deposit() {
        try {
            int accountId = readInt("Account ID: ");
            double amount = readDouble("Amount: ");
            String ref = paymentService.deposit(accountId, amount);
            System.out.println("Deposit successful. Reference: " + ref);
        } catch (IgirePayException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    private static void withdraw() {
        try {
            int accountId = readInt("Account ID: ");
            double amount = readDouble("Amount: ");
            String ref = paymentService.withdraw(accountId, amount);
            System.out.println("Withdrawal successful. Reference: " + ref);
        } catch (InsufficientBalanceException e) {
            System.out.println("Insufficient funds: " + e.getMessage());
        } catch (IgirePayException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    private static void transfer() {
        try {
            int from = readInt("From Account ID: ");
            int to = readInt("To Account ID: ");
            double amount = readDouble("Amount: ");
            String ref = paymentService.transfer(from, to, amount);
            System.out.println("Transfer successful. Reference: " + ref);
        } catch (InsufficientBalanceException e) {
            System.out.println("Transfer rolled back — " + e.getMessage());
        } catch (IgirePayException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }

    private static void viewBalance() {
        try {
            int accountId = readInt("Account ID: ");
            Account account = paymentService.getAccount(accountId);
            System.out.printf("Account #%d | Type: %s | Balance: %.2f RWF%n",
                    account.getId(), account.getAccountType(), account.getBalance());
        } catch (IgirePayException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void viewTransactionHistory() {
        try {
            int accountId = readInt("Account ID: ");
            List<Transaction> txs = paymentService.getTransactionHistory(accountId);
            TransactionReport report = new TransactionReport("Account #" + accountId, txs);
            report.printDetailed();
        } catch (IgirePayException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void viewFullReport() {
        try {
            List<Transaction> all = transactionDAO.getAll();
            TransactionReport report = new TransactionReport("All Transactions", all);
            report.printSummary();
            report.printDetailed();
        } catch (SQLException e) {
            System.out.println("Error fetching transactions: " + e.getMessage());
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Please enter a valid number."); }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Please enter a valid amount."); }
        }
    }
}
