package com.example.igirepay.lab1.test;

import com.example.igirepay.lab1.collection.AccountCollection;
import com.example.igirepay.lab1.model.*;
import com.example.igirepay.lab1.storage.IgirePayDataStore;

import java.time.LocalDateTime;
import java.util.UUID;

public class AccountModelTest {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("     IgirePay Lab1 Model Tests         ");
        System.out.println("========================================\n");

        testCustomer();
        testWalletAccount();
        testSavingsAccount();
        testAccountCollection();
        testDataStore();

        System.out.println("\nAll tests completed.");
    }

    private static void testCustomer() {
        System.out.println("--- Test: Customer ---");
        Customer c = new Customer(1, "Alice Uwase", "alice@igirepay.rw", "0781234567", "1234");
        System.out.println("Created: " + c);
        c.setFullName("Alice K. Uwase");
        System.out.println("Updated name: " + c.getFullName());
        System.out.println();
    }

    private static void testWalletAccount() {
        System.out.println("--- Test: WalletAccount ---");
        Customer c = new Customer(1, "Bob Niyonzima", "bob@igirepay.rw", "0789999999", "5678");
        WalletAccount wallet = new WalletAccount(
                1, c, 10000.0, LocalDateTime.now(),
                50000.0, 100.0, true, 0.0, null
        );

        System.out.println("Initial balance: " + wallet.getBalance());
        wallet.deposit(5000.0);
        System.out.println("After deposit 5000: " + wallet.getBalance());
        wallet.withdraw(2000.0);
        System.out.println("After withdraw 2000 (+ fee 100): " + wallet.getBalance());
        System.out.println();
    }

    private static void testSavingsAccount() {
        System.out.println("--- Test: SavingsAccount ---");
        Customer c = new Customer(2, "Carol Ingabire", "carol@igirepay.rw", "0788888888", "9012");
        SavingsAccount savings = new SavingsAccount(
                2, c, "SAVINGS", 20000.0, LocalDateTime.now(),
                5.0, 5000.0, 3, 0, null
        );

        System.out.println("Initial balance: " + savings.getBalance());
        savings.deposit(10000.0);
        System.out.println("After deposit 10000: " + savings.getBalance());
        savings.withdraw(2000.0);
        System.out.println("After withdraw 2000: " + savings.getBalance());
        System.out.println("Withdrawals this month: " + savings.getWithdrawalsThisMonth());
        System.out.println();
    }

    private static void testAccountCollection() {
        System.out.println("--- Test: AccountCollection ---");
        Customer c = new Customer(1, "Dave", "dave@igirepay.rw", "0777777777", "3456");

        AccountCollection collection = new AccountCollection();
        WalletAccount w = new WalletAccount(1, c, 5000.0, LocalDateTime.now(), 20000.0, 50.0, true, 0.0, null);
        SavingsAccount s = new SavingsAccount(2, c, "SAVINGS", 10000.0, LocalDateTime.now(), 3.5, 1000.0, 5, 0, null);

        collection.add(w);
        collection.add(s);

        System.out.println("Collection size: " + collection.size());
        System.out.println("Find by ID 1: " + collection.findById(1));
        collection.remove(1);
        System.out.println("After remove ID 1, size: " + collection.size());
        System.out.println();
    }

    private static void testDataStore() {
        System.out.println("--- Test: IgirePayDataStore ---");
        IgirePayDataStore store = new IgirePayDataStore();
        Customer c = new Customer(1, "Eve Mutesi", "eve@igirepay.rw", "0766666666", "7890");
        WalletAccount wallet = new WalletAccount(1, c, 15000.0, LocalDateTime.now(), 50000.0, 50.0, true, 0.0, null);

        store.addAccount(1, wallet);

        String refId = UUID.randomUUID().toString();
        Transaction tx = new Transaction(1, refId, 3000.0, "DEPOSIT", LocalDateTime.now(), wallet);

        boolean first = store.processTransaction(tx);
        boolean second = store.processTransaction(tx); // duplicate

        System.out.println("First process (should be true): " + first);
        System.out.println("Second process (should be false): " + second);
        System.out.println("Transaction history size: " + store.getTransactionHistory().size());
        System.out.println("Failed transactions size: " + store.getFailedTransactions().size());
    }
}
