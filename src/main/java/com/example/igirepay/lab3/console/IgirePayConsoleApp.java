package com.example.igirepay.lab3.console;

import com.example.igirepay.lab1.model.*;
import com.example.igirepay.lab3.exception.*;
import com.example.igirepay.lab3.report.TransactionReportGenerator;
import com.example.igirepay.lab3.service.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Lab 3 - Console-based Payment Management Application
 * Full menu-driven app combining Labs 1 & 2 with service layer, exception handling,
 * reporting, PIN auth, and duplicate-transaction prevention.
 */
public class IgirePayConsoleApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerService customerService = new CustomerService();
    private static final AccountService accountService = new AccountService();
    private static final TransactionService transactionService = new TransactionService();
    private static final TransactionReportGenerator reportGen = new TransactionReportGenerator();

    public static void main(String[] args) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║     IgirePay Payment Gateway v1.0   ║");
        System.out.println("╚══════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("→ ");
            switch (choice) {
                case 1 -> customerMenu();
                case 2 -> accountMenu();
                case 3 -> transactionMenu();
                case 4 -> reportMenu();
                case 0 -> { System.out.println("Goodbye! Murakoze!"); running = false; }
                default -> System.out.println("[!] Invalid option.");
            }
        }
    }

    // ── Menus ──────────────────────────────────────────────────────

    private static void printMainMenu() {
        System.out.println("\n┌─ MAIN MENU ─────────────────────────┐");
        System.out.println("│  1. Customer Management              │");
        System.out.println("│  2. Account Management               │");
        System.out.println("│  3. Transaction Management           │");
        System.out.println("│  4. Reports                          │");
        System.out.println("│  0. Exit                             │");
        System.out.println("└──────────────────────────────────────┘");
    }

    private static void customerMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n── Customer Management ──");
            System.out.println("1. Register Customer");
            System.out.println("2. Update Customer");
            System.out.println("3. View All Customers");
            System.out.println("4. Find Customer by ID");
            System.out.println("5. Delete Customer");
            System.out.println("6. Change PIN");
            System.out.println("0. Back");

            switch (readInt("→ ")) {
                case 1 -> registerCustomer();
                case 2 -> updateCustomer();
                case 3 -> viewAllCustomers();
                case 4 -> findCustomerById();
                case 5 -> deleteCustomer();
                case 6 -> changePin();
                case 0 -> back = true;
                default -> System.out.println("[!] Invalid option.");
            }
        }
    }

    private static void accountMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n── Account Management ──");
            System.out.println("1. Create Wallet Account");
            System.out.println("2. Create Savings Account");
            System.out.println("3. View Account Balance");
            System.out.println("4. View All Accounts");
            System.out.println("5. Delete Inactive Account");
            System.out.println("0. Back");

            switch (readInt("→ ")) {
                case 1 -> createWalletAccount();
                case 2 -> createSavingsAccount();
                case 3 -> viewAccountBalance();
                case 4 -> viewAllAccounts();
                case 5 -> deleteAccount();
                case 0 -> back = true;
                default -> System.out.println("[!] Invalid option.");
            }
        }
    }

    private static void transactionMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n── Transaction Management ──");
            System.out.println("1. Deposit Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. View Account Transactions");
            System.out.println("5. View All Transactions");
            System.out.println("0. Back");

            switch (readInt("→ ")) {
                case 1 -> depositMoney();
                case 2 -> withdrawMoney();
                case 3 -> transferMoney();
                case 4 -> viewAccountTransactions();
                case 5 -> viewAllTransactions();
                case 0 -> back = true;
                default -> System.out.println("[!] Invalid option.");
            }
        }
    }

    private static void reportMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n── Reports ──");
            System.out.println("1. Export All Transactions to CSV");
            System.out.println("2. Daily Transaction Summary");
            System.out.println("3. Account Statement");
            System.out.println("0. Back");

            switch (readInt("→ ")) {
                case 1 -> exportCsv();
                case 2 -> dailySummary();
                case 3 -> accountStatement();
                case 0 -> back = true;
                default -> System.out.println("[!] Invalid option.");
            }
        }
    }

    // ── Customer Operations ────────────────────────────────────────

    private static void registerCustomer() {
        System.out.println("\n-- Register Customer --");
        System.out.print("Full Name : "); String name = scanner.nextLine();
        System.out.print("Email     : "); String email = scanner.nextLine();
        System.out.print("Phone     : "); String phone = scanner.nextLine();
        System.out.print("PIN (4-6 digits): "); String pin = scanner.nextLine();

        if (pin.isBlank() || !pin.matches("\\d{4,6}")) {
            System.out.println("[!] PIN must be 4–6 digits.");
            return;
        }

        try {
            customerService.register(new Customer(0, name, email, phone, pin));
            System.out.println("[✓] Customer registered.");
        } catch (SQLException e) {
            System.out.println("[✗] DB Error: " + e.getMessage());
        }
    }

    private static void updateCustomer() {
        int id = readInt("Customer ID: ");
        try {
            Customer c = customerService.getById(id);
            if (c == null) { System.out.println("[!] Not found."); return; }
            System.out.println("Current: " + c);
            System.out.print("New Name  (Enter to keep): "); String n = scanner.nextLine();
            System.out.print("New Email (Enter to keep): "); String e = scanner.nextLine();
            System.out.print("New Phone (Enter to keep): "); String p = scanner.nextLine();
            if (!n.isBlank()) c.setFullName(n);
            if (!e.isBlank()) c.setEmail(e);
            if (!p.isBlank()) c.setPhoneNumber(p);
            customerService.update(c);
            System.out.println("[✓] Customer updated.");
        } catch (SQLException ex) { System.out.println("[✗] " + ex.getMessage()); }
    }

    private static void viewAllCustomers() {
        try {
            List<Customer> list = customerService.getAll();
            if (list.isEmpty()) { System.out.println("No customers."); return; }
            list.forEach(System.out::println);
        } catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    private static void findCustomerById() {
        int id = readInt("Customer ID: ");
        try {
            Customer c = customerService.getById(id);
            System.out.println(c != null ? c : "[!] Not found.");
        } catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    private static void deleteCustomer() {
        int id = readInt("Customer ID: ");
        try {
            if (customerService.getById(id) == null) { System.out.println("[!] Not found."); return; }
            customerService.delete(id);
            System.out.println("[✓] Customer deleted.");
        } catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    private static void changePin() {
        int id = readInt("Customer ID: ");
        try {
            Customer c = customerService.getById(id);
            if (c == null) { System.out.println("[!] Not found."); return; }
            System.out.print("Current PIN: "); String old = scanner.nextLine();
            System.out.print("New PIN    : "); String newPin = scanner.nextLine();
            if (!newPin.matches("\\d{4,6}")) { System.out.println("[!] PIN must be 4–6 digits."); return; }
            customerService.changePin(c, old, newPin);
            System.out.println("[✓] PIN changed successfully.");
        } catch (InvalidPinException | AccountLockedException ex) {
            System.out.println("[✗] " + ex.getMessage());
        } catch (SQLException e) { System.out.println("[✗] DB Error: " + e.getMessage()); }
    }

    // ── Account Operations ─────────────────────────────────────────

    private static void createWalletAccount() {
        int cid = readInt("Customer ID: ");
        try {
            Customer c = customerService.getById(cid);
            if (c == null) { System.out.println("[!] Customer not found."); return; }
            double bal = readDouble("Initial Balance (RWF): ");
            double limit = readDouble("Daily Transfer Limit (RWF): ");
            double fee = readDouble("Transaction Fee (RWF): ");
            accountService.createAccount(
                new WalletAccount(0, c, bal, LocalDateTime.now(), limit, fee, true, 0, null));
            System.out.println("[✓] Wallet account created.");
        } catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    private static void createSavingsAccount() {
        int cid = readInt("Customer ID: ");
        try {
            Customer c = customerService.getById(cid);
            if (c == null) { System.out.println("[!] Customer not found."); return; }
            double bal = readDouble("Initial Balance (RWF): ");
            double rate = readDouble("Interest Rate (%): ");
            double minBal = readDouble("Minimum Balance (RWF): ");
            int wLimit = readInt("Monthly Withdrawal Limit: ");
            accountService.createAccount(
                new SavingsAccount(0, c, "SAVINGS", bal, LocalDateTime.now(), rate, minBal, wLimit, 0, null));
            System.out.println("[✓] Savings account created.");
        } catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    private static void viewAccountBalance() {
        int id = readInt("Account ID: ");
        try {
            Account a = accountService.getById(id);
            System.out.printf("Account %d (%s) | Balance: %.2f RWF%n",
                a.getId(), a.getAccountType(), a.getBalance());
        } catch (AccountNotFoundException e) { System.out.println("[!] " + e.getMessage()); }
          catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    private static void viewAllAccounts() {
        try {
            List<Account> list = accountService.getAll();
            if (list.isEmpty()) { System.out.println("No accounts."); return; }
            list.forEach(a -> System.out.printf("ID:%-4d | %-8s | Balance: %10.2f RWF | CustomerID: %d%n",
                a.getId(), a.getAccountType(), a.getBalance(), a.getCustomer().getId()));
        } catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    private static void deleteAccount() {
        int id = readInt("Account ID: ");
        try {
            accountService.deleteInactive(id);
            System.out.println("[✓] Account deleted.");
        } catch (AccountNotFoundException | IllegalStateException e) { System.out.println("[!] " + e.getMessage()); }
          catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    // ── Transaction Operations ─────────────────────────────────────

    private static void depositMoney() {
        int id = readInt("Account ID: ");
        double amt = readDouble("Amount (RWF): ");
        try {
            String ref = transactionService.deposit(id, amt);
            System.out.println("[✓] Deposit successful. Ref: " + ref);
        } catch (InvalidAmountException | AccountNotFoundException e) { System.out.println("[!] " + e.getMessage()); }
          catch (SQLException e) { System.out.println("[✗] DB Error: " + e.getMessage()); }
    }

    private static void withdrawMoney() {
        int id = readInt("Account ID: ");
        double amt = readDouble("Amount (RWF): ");
        try {
            String ref = transactionService.withdraw(id, amt);
            System.out.println("[✓] Withdrawal successful. Ref: " + ref);
        } catch (InvalidAmountException | AccountNotFoundException | InsufficientBalanceException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (SQLException e) { System.out.println("[✗] DB Error: " + e.getMessage()); }
    }

    private static void transferMoney() {
        int from = readInt("From Account ID: ");
        int to   = readInt("To Account ID  : ");
        double amt = readDouble("Amount (RWF): ");
        try {
            String ref = transactionService.transfer(from, to, amt);
            System.out.println("[✓] Transfer successful. Ref: " + ref);
        } catch (InvalidAmountException | AccountNotFoundException | InsufficientBalanceException | IllegalArgumentException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (SQLException e) { System.out.println("[✗] Transfer rolled back: " + e.getMessage()); }
    }

    private static void viewAccountTransactions() {
        int id = readInt("Account ID: ");
        try {
            var list = transactionService.getByAccount(id);
            if (list.isEmpty()) { System.out.println("No transactions."); return; }
            list.forEach(t -> System.out.printf("%-6d %-14s %10.2f RWF  %s%n",
                t.getId(), t.getTransactionType(), t.getAmount(), t.getTimestamp()));
        } catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    private static void viewAllTransactions() {
        try {
            var list = transactionService.getAll();
            if (list.isEmpty()) { System.out.println("No transactions."); return; }
            list.forEach(t -> System.out.printf("ID:%-4d AcctID:%-4d %-14s %10.2f RWF  %s%n",
                t.getId(), t.getAccount().getId(), t.getTransactionType(), t.getAmount(), t.getTimestamp()));
        } catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    // ── Reports ───────────────────────────────────────────────────

    private static void exportCsv() {
        System.out.print("Output file path (e.g. transactions.csv): ");
        String path = scanner.nextLine();
        if (path.isBlank()) path = "transactions_export.csv";
        try {
            String out = reportGen.exportAllToCSV(path);
            System.out.println("[✓] Exported to: " + out);
        } catch (Exception e) { System.out.println("[✗] " + e.getMessage()); }
    }

    private static void dailySummary() {
        try { System.out.println(reportGen.getDailySummary()); }
        catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    private static void accountStatement() {
        int id = readInt("Account ID: ");
        try { System.out.println(reportGen.getAccountStatement(id)); }
        catch (SQLException e) { System.out.println("[✗] " + e.getMessage()); }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("[!] Enter a valid number."); }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("[!] Enter a valid amount."); }
        }
    }
}
