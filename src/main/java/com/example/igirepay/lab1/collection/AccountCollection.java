package com.example.igirepay.lab1.collection;

import com.example.igirepay.lab1.model.Account;

import java.util.*;

public class AccountCollection {

    private final List<Account> accounts = new ArrayList<>();

    public void add(Account account) {
        accounts.add(account);
    }

    public boolean remove(int accountId) {
        return accounts.removeIf(a -> a.getId() == accountId);
    }

    public Optional<Account> findById(int accountId) {
        return accounts.stream()
                .filter(a -> a.getId() == accountId)
                .findFirst();
    }

    public List<Account> getAll() {
        return Collections.unmodifiableList(accounts);
    }

    public int size() {
        return accounts.size();
    }

    public boolean isEmpty() {
        return accounts.isEmpty();
    }

    @Override
    public String toString() {
        return "AccountCollection{accounts=" + accounts + '}';
    }
}
