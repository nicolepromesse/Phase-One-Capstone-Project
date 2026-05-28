package com.example.igirepay.lab1.collection;

import com.example.igirepay.lab1.model.Transaction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TransactionCollection — in-memory store for Transaction objects.
 * Supports add, remove, lookup by reference ID or account, and filtering by type.
 */
public class TransactionCollection {

    private final List<Transaction> transactions = new ArrayList<>();

    /** Add a transaction to the collection. */
    public void add(Transaction transaction) {
        if (transaction == null) throw new IllegalArgumentException("Transaction cannot be null");
        transactions.add(transaction);
    }

    /** Remove a transaction by its numeric ID. Returns true if found and removed. */
    public boolean removeById(int transactionId) {
        return transactions.removeIf(t -> t.getId() == transactionId);
    }

    /** Find a single transaction by its unique reference ID (UUID string). */
    public Optional<Transaction> findByReferenceId(String referenceId) {
        return transactions.stream()
                .filter(t -> t.getReferenceId().equals(referenceId))
                .findFirst();
    }

    /** Return all transactions belonging to a given account ID. */
    public List<Transaction> getByAccountId(int accountId) {
        return transactions.stream()
                .filter(t -> t.getAccount() != null && t.getAccount().getId() == accountId)
                .collect(Collectors.toList());
    }

    /** Return all transactions of a given type, e.g. "DEPOSIT", "WITHDRAW", "TRANSFER". */
    public List<Transaction> getByType(String transactionType) {
        return transactions.stream()
                .filter(t -> t.getTransactionType().equalsIgnoreCase(transactionType))
                .collect(Collectors.toList());
    }

    /** Return an unmodifiable view of all transactions. */
    public List<Transaction> getAll() {
        return Collections.unmodifiableList(transactions);
    }

    /** Total number of transactions in this collection. */
    public int size() {
        return transactions.size();
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    /** Sum of all amounts across all transactions. */
    public double totalAmount() {
        return transactions.stream().mapToDouble(Transaction::getAmount).sum();
    }

    @Override
    public String toString() {
        return "TransactionCollection{size=" + transactions.size() + ", total=" + totalAmount() + "}";
    }
}
