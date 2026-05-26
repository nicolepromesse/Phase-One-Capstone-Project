package com.example.igirepay;

import com.example.igirepay.lab1.model.*;
import com.example.igirepay.lab2.daoimpl.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerDAOImpl customerDAO = new CustomerDAOImpl();
    private static final AccountDAOImpl accountDAO = new AccountDAOImpl();
    private static final TransactionDAOImpl transactionDAO = new TransactionDAOImpl();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> customerMenu();
                case 2 -> accountMenu();
                case 3 -> transactionMenu();
                case 0 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n========================================");
        System.out.println("        IgirePay   MAIN MENU                    ");
        System.out.println("========================================");
        System.out.println("1. Customer Management");
        System.out.println("2. Account Management");
        System.out.println("3. Transaction Management");
        System.out.println("0. Exit");
        System.out.println("========================================");
    }


    // ─────────────────────────────────────────
    //              CUSTOMER MENU
    // ─────────────────────────────────────────

    private static void customerMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Customer Management ---");
            System.out.println("1. Register Customer");
            System.out.println("2. Update Customer");
            System.out.println("3. View All Customers");
            System.out.println("4. Find Customer by ID");
            System.out.println("5. Delete Customer");
            System.out.println("0. Back");

            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> registerCustomer();
                case 2 -> updateCustomer();
                case 3 -> viewAllCustomers();
                case 4 -> findCustomerById();
                case 5 -> deleteCustomer();
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void registerCustomer() {
        System.out.println("\n-- Register New Customer --");
        System.out.print("Full Name: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Phone Number: ");
        String phone = scanner.nextLine();

        System.out.print("PIN (4 digits): ");
        String pin = scanner.nextLine();

        Customer customer = new Customer(0, name, email, phone, pin);

        try {
            customerDAO.save(customer);
            System.out.println("Customer registered successfully.");
        } catch (SQLException e) {
            System.out.println("Error registering customer: " + e.getMessage());
        }
    }

    private static void updateCustomer() {
        System.out.println("\n-- Update Customer --");
        int id = readInt("Customer ID to update: ");

        try {
            Customer existing = customerDAO.getById(id);

            if (existing == null) {
                System.out.println("Customer not found with ID: " + id);
                return;
            }

            System.out.println("Current: " + existing);
            System.out.print("New Full Name (" + existing.getFullName() + "): ");
            String name = scanner.nextLine();

            System.out.print("New Email (" + existing.getEmail() + "): ");
            String email = scanner.nextLine();

            System.out.print("New Phone (" + existing.getPhoneNumber() + "): ");
            String phone = scanner.nextLine();

            if (!name.isBlank()) existing.setFullName(name);
            if (!email.isBlank()) existing.setEmail(email);
            if (!phone.isBlank()) existing.setPhoneNumber(phone);

            customerDAO.update(existing);
            System.out.println("Customer updated successfully.");

        } catch (SQLException e) {
            System.out.println("Error updating customer: " + e.getMessage());
        }
    }

    private static void viewAllCustomers() {
        System.out.println("\n-- All Customers --");

        try {
            List<Customer> customers = customerDAO.getAll();

            if (customers.isEmpty()) {
                System.out.println("No customers found.");
                return;
            }

            customers.forEach(System.out::println);

        } catch (SQLException e) {
            System.out.println("Error fetching customers: " + e.getMessage());
        }
    }

    private static void findCustomerById() {
        int id = readInt("Enter Customer ID: ");

        try {
            Customer customer = customerDAO.getById(id);

            if (customer == null) {
                System.out.println("No customer found with ID: " + id);
            } else {
                System.out.println(customer);
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteCustomer() {
        int id = readInt("Enter Customer ID to delete: ");

        try {
            Customer existing = customerDAO.getById(id);

            if (existing == null) {
                System.out.println("Customer not found with ID: " + id);
                return;
            }

            customerDAO.delete(id);
            System.out.println("Customer deleted successfully.");

        } catch (SQLException e) {
            System.out.println("Error deleting customer: " + e.getMessage());
        }
    }


    private static void accountMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Account Management ---");
            System.out.println("1. Create Wallet Account");
            System.out.println("2. Create Savings Account");
            System.out.println("3. View Account Balance");
            System.out.println("4. View All Accounts");
            System.out.println("5. Delete Inactive Account");
            System.out.println("0. Back");

            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> createWalletAccount();
                case 2 -> createSavingsAccount();
                case 3 -> viewAccountBalance();
                case 4 -> viewAllAccounts();
                case 5 -> deleteAccount();
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void createWalletAccount() {
        System.out.println("\n-- Create Wallet Account --");
        int customerId = readInt("Customer ID: ");

        try {
            Customer customer = customerDAO.getById(customerId);

            if (customer == null) {
                System.out.println("Customer not found with ID: " + customerId);
                return;
            }

            double initialBalance = readDouble("Initial Balance: ");
            double dailyLimit = readDouble("Daily Transfer Limit: ");
            double fee = readDouble("Transaction Fee: ");

            WalletAccount wallet = new WalletAccount(
                    0, customer, initialBalance, LocalDateTime.now(),
                    dailyLimit, fee, true, 0, null
            );

            accountDAO.save(wallet);
            System.out.println("Wallet account created successfully.");

        } catch (SQLException e) {
            System.out.println("Error creating wallet account: " + e.getMessage());
        }
    }

    private static void createSavingsAccount() {
        System.out.println("\n-- Create Savings Account --");
        int customerId = readInt("Customer ID: ");

        try {
            Customer customer = customerDAO.getById(customerId);

            if (customer == null) {
                System.out.println("Customer not found with ID: " + customerId);
                return;
            }

            double initialBalance = readDouble("Initial Balance: ");
            double interestRate = readDouble("Interest Rate (%): ");
            double minBalance = readDouble("Minimum Balance: ");
            int withdrawLimit = readInt("Monthly Withdrawal Limit: ");

            SavingsAccount savings = new SavingsAccount(
                    0, customer, "SAVINGS", initialBalance, LocalDateTime.now(),
                    interestRate, minBalance, withdrawLimit, 0, null
            );

            accountDAO.save(savings);
            System.out.println("Savings account created successfully.");

        } catch (SQLException e) {
            System.out.println("Error creating savings account: " + e.getMessage());
        }
    }

    private static void viewAccountBalance() {
        int accountId = readInt("Enter Account ID: ");

        try {
            Account account = accountDAO.getById(accountId);

            if (account == null) {
                System.out.println("Account not found with ID: " + accountId);
                return;
            }

            System.out.println("Account ID : " + account.getId());
            System.out.println("Type       : " + account.getAccountType());
            System.out.println("Balance    : " + account.getBalance() + " RWF");

        } catch (SQLException e) {
            System.out.println("Error fetching account: " + e.getMessage());
        }
    }

    private static void viewAllAccounts() {
        System.out.println("\n-- All Accounts --");

        try {
            List<Account> accounts = accountDAO.getAll();

            if (accounts.isEmpty()) {
                System.out.println("No accounts found.");
                return;
            }

            accounts.forEach(a -> System.out.println(
                    "ID: " + a.getId() +
                            " | Type: " + a.getAccountType() +
                            " | Balance: " + a.getBalance() + " RWF" +
                            " | CustomerID: " + a.getCustomer().getId()
            ));

        } catch (SQLException e) {
            System.out.println("Error fetching accounts: " + e.getMessage());
        }
    }

    private static void deleteAccount() {
        int accountId = readInt("Enter Account ID to delete: ");

        try {
            Account account = accountDAO.getById(accountId);

            if (account == null) {
                System.out.println("Account not found with ID: " + accountId);
                return;
            }

            accountDAO.delete(accountId);
            System.out.println("Account deleted successfully.");

        } catch (SQLException e) {
            System.out.println("Error deleting account: " + e.getMessage());
        }
    }


    private static void transactionMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Transaction Management ---");
            System.out.println("1. Deposit Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. View Transaction History");
            System.out.println("5. View All Transactions");
            System.out.println("0. Back");

            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> depositMoney();
                case 2 -> withdrawMoney();
                case 3 -> transferMoney();
                case 4 -> viewTransactionHistory();
                case 5 -> viewAllTransactions();
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void depositMoney() {
        System.out.println("\n-- Deposit Money --");
        int accountId = readInt("Account ID: ");

        try {
            Account account = accountDAO.getById(accountId);

            if (account == null) {
                System.out.println("Account not found with ID: " + accountId);
                return;
            }

            double amount = readDouble("Amount to deposit: ");

            if (amount <= 0) {
                System.out.println("Amount must be greater than zero.");
                return;
            }

            String referenceId = UUID.randomUUID().toString();

            Transaction transaction = new Transaction(
                    0, referenceId, amount, "DEPOSIT", LocalDateTime.now(), account
            );

            transactionDAO.save(transaction);

            account.setBalance(account.getBalance() + amount);
            accountDAO.update(account);

            System.out.println("Deposit successful. Reference: " + referenceId);
            System.out.println("New Balance: " + account.getBalance() + " RWF");

        } catch (SQLException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    private static void withdrawMoney() {
        System.out.println("\n-- Withdraw Money --");
        int accountId = readInt("Account ID: ");

        try {
            Account account = accountDAO.getById(accountId);

            if (account == null) {
                System.out.println("Account not found with ID: " + accountId);
                return;
            }

            double amount = readDouble("Amount to withdraw: ");

            if (amount <= 0) {
                System.out.println("Amount must be greater than zero.");
                return;
            }

            if (account.getBalance() < amount) {
                System.out.println("Insufficient balance. Current balance: " + account.getBalance() + " RWF");
                return;
            }

            String referenceId = UUID.randomUUID().toString();

            Transaction transaction = new Transaction(
                    0, referenceId, amount, "WITHDRAW", LocalDateTime.now(), account
            );

            transactionDAO.save(transaction);

            account.setBalance(account.getBalance() - amount);
            accountDAO.update(account);

            System.out.println("Withdrawal successful. Reference: " + referenceId);
            System.out.println("New Balance: " + account.getBalance() + " RWF");

        } catch (SQLException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    private static void transferMoney() {
        System.out.println("\n-- Transfer Money --");
        int fromId = readInt("From Account ID: ");
        int toId = readInt("To Account ID: ");

        try {
            Account from = accountDAO.getById(fromId);
            Account to = accountDAO.getById(toId);

            if (from == null) {
                System.out.println("Sender account not found with ID: " + fromId);
                return;
            }

            if (to == null) {
                System.out.println("Recipient account not found with ID: " + toId +
                        " — transaction has been rolled back.");
                return;
            }

            if (fromId == toId) {
                System.out.println("Cannot transfer to the same account.");
                return;
            }

            double amount = readDouble("Amount to transfer: ");

            if (amount <= 0) {
                System.out.println("Amount must be greater than zero.");
                return;
            }

            if (from.getBalance() < amount) {
                System.out.println("Insufficient balance. Current balance: " + from.getBalance() + " RWF");
                System.out.println("Transaction rolled back — no funds moved.");
                return;
            }

            String referenceId = UUID.randomUUID().toString();

            transactionDAO.transfer(from, to, amount, referenceId);

            System.out.println("Transfer successful. Reference: " + referenceId);
            System.out.println("Amount transferred: " + amount + " RWF");

        } catch (SQLException e) {
            System.out.println("Transfer failed and rolled back: " + e.getMessage());
        }
    }

    private static void viewTransactionHistory() {
        int accountId = readInt("Enter Account ID: ");

        try {
            Account account = accountDAO.getById(accountId);

            if (account == null) {
                System.out.println("Account not found with ID: " + accountId);
                return;
            }

            List<Transaction> transactions = transactionDAO.getByAccountId(accountId);

            if (transactions.isEmpty()) {
                System.out.println("No transactions found for account ID: " + accountId);
                return;
            }

            System.out.println("\n-- Transaction History for Account " + accountId + " --");
            transactions.forEach(t -> System.out.println(
                    "ID: " + t.getId() +
                            " | Type: " + t.getTransactionType() +
                            " | Amount: " + t.getAmount() + " RWF" +
                            " | Ref: " + t.getReferenceId() +
                            " | Time: " + t.getTimestamp()
            ));

        } catch (SQLException e) {
            System.out.println("Error fetching transactions: " + e.getMessage());
        }
    }

    private static void viewAllTransactions() {
        System.out.println("\n-- All Transactions --");

        try {
            List<Transaction> transactions = transactionDAO.getAll();

            if (transactions.isEmpty()) {
                System.out.println("No transactions found.");
                return;
            }

            transactions.forEach(t -> System.out.println(
                    "ID: " + t.getId() +
                            " | AccountID: " + t.getAccount().getId() +
                            " | Type: " + t.getTransactionType() +
                            " | Amount: " + t.getAmount() + " RWF" +
                            " | Ref: " + t.getReferenceId() +
                            " | Time: " + t.getTimestamp()
            ));

        } catch (SQLException e) {
            System.out.println("Error fetching transactions: " + e.getMessage());
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }
}