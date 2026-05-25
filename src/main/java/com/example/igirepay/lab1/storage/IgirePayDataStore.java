package com.example.igirepay.lab1.storage;

import com.example.igirepay.lab1.model.*;

import java.util.*;

public class IgirePayDataStore {

    private Map<Integer, List<Account>> customerAccounts = new HashMap<>();
    private List<Transaction> transactionHistory = new ArrayList<>();
    private Set<String> processedReferenceIds = new HashSet<>();
    private List<Transaction> failedTransactions = new ArrayList<>();

    public void addAccount(int customerId, Account account) {
        customerAccounts.computeIfAbsent(customerId, k -> new ArrayList<>()).add(account);
    }

    public List<Account> getAccountsByCustomerId(int customerId) {
        return customerAccounts.getOrDefault(customerId, new ArrayList<>());
    }

    public boolean processTransaction(Transaction transaction) {
        String ref = transaction.getReferenceId();

        if (processedReferenceIds.contains(ref)) {
            failedTransactions.add(transaction);
            return false;
        }

        processedReferenceIds.add(ref);
        transactionHistory.add(transaction);
        return true;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public Set<String> getProcessedReferenceIds() {
        return processedReferenceIds;
    }

    public List<Transaction> getFailedTransactions() {
        return failedTransactions;
    }

    public Map<Integer, List<Account>> getCustomerAccounts() {
        return customerAccounts;
    }
}