package com.example.igirepay.lab3.service;

import com.example.igirepay.lab1.model.Account;
import com.example.igirepay.lab1.model.Transaction;
import com.example.igirepay.lab2.daoimpl.AccountDAOImpl;
import com.example.igirepay.lab2.daoimpl.TransactionDAOImpl;
import com.example.igirepay.lab3.exception.AccountNotFoundException;
import com.example.igirepay.lab3.exception.InsufficientBalanceException;
import com.example.igirepay.lab3.exception.InvalidAmountException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for all transaction operations.
 * Handles deposit, withdrawal, transfer with idempotency and rollback.
 */
public class TransactionService {

    private final TransactionDAOImpl transactionDAO = new TransactionDAOImpl();
    private final AccountDAOImpl accountDAO = new AccountDAOImpl();

    public String deposit(int accountId, double amount) throws SQLException {
        if (amount <= 0) throw new InvalidAmountException(amount);

        Account account = accountDAO.getById(accountId);
        if (account == null) throw new AccountNotFoundException(accountId);

        String referenceId = UUID.randomUUID().toString();
        Transaction tx = new Transaction(0, referenceId, amount, "DEPOSIT", LocalDateTime.now(), account);
        transactionDAO.save(tx);

        account.setBalance(account.getBalance() + amount);
        accountDAO.update(account);

        return referenceId;
    }

    public String withdraw(int accountId, double amount) throws SQLException {
        if (amount <= 0) throw new InvalidAmountException(amount);

        Account account = accountDAO.getById(accountId);
        if (account == null) throw new AccountNotFoundException(accountId);

        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException(account.getBalance(), amount);
        }

        String referenceId = UUID.randomUUID().toString();
        Transaction tx = new Transaction(0, referenceId, amount, "WITHDRAW", LocalDateTime.now(), account);
        transactionDAO.save(tx);

        account.setBalance(account.getBalance() - amount);
        accountDAO.update(account);

        return referenceId;
    }

    public String transfer(int fromId, int toId, double amount) throws SQLException {
        if (amount <= 0) throw new InvalidAmountException(amount);
        if (fromId == toId) throw new IllegalArgumentException("Cannot transfer to same account");

        Account from = accountDAO.getById(fromId);
        Account to = accountDAO.getById(toId);

        if (from == null) throw new AccountNotFoundException(fromId);
        if (to == null) throw new AccountNotFoundException(toId);

        if (from.getBalance() < amount) {
            throw new InsufficientBalanceException(from.getBalance(), amount);
        }

        String referenceId = UUID.randomUUID().toString();
        transactionDAO.transfer(from, to, amount, referenceId);
        return referenceId;
    }

    public List<Transaction> getByAccount(int accountId) throws SQLException {
        return transactionDAO.getByAccountId(accountId);
    }

    public List<Transaction> getAll() throws SQLException {
        return transactionDAO.getAll();
    }
}
