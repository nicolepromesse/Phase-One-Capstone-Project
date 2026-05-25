package com.example.igirepay;

import com.example.igirepay.lab1.model.*;
import com.example.igirepay.lab1.storage.IgirePayDataStore;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        IgirePayDataStore store = new IgirePayDataStore();

        Customer customer = new Customer(1, "John Doe", "john@gmail.com", "0788888888", "1234");

        WalletAccount wallet = new WalletAccount(
                1, customer, 10000, LocalDateTime.now(),
                5000, 200, true, 0, LocalDateTime.now()
        );

        store.addAccount(customer.getId(), wallet);

        wallet.deposit(1000);
        wallet.withdraw(500);

        Transaction t1 = new Transaction(1, "REF001", 500, "WITHDRAW", LocalDateTime.now(), wallet);
        Transaction t2 = new Transaction(2, "REF001", 500, "WITHDRAW", LocalDateTime.now(), wallet);

        System.out.println(store.processTransaction(t1));
        System.out.println(store.processTransaction(t2));

        System.out.println(store.getTransactionHistory());
        System.out.println(store.getFailedTransactions());
    }
}