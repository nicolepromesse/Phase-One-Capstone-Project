package com.example.igirepay.lab3.service;

import com.example.igirepay.lab1.model.*;
import com.example.igirepay.lab2.daoimpl.*;
import com.example.igirepay.lab3.exception.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PaymentService {

    private final AccountDAOImpl accountDAO = new AccountDAOImpl();
    private final CustomerDAOImpl customerDAO = new CustomerDAOImpl();
    private final TransactionDAOImpl transactionDAO = new TransactionDAOImpl();

    public String deposit(int accountId, double amount) {
        if (amount <= 0) throw new IgirePayException("Deposit amount must be positive");

        try {
            Account account = accountDAO.getById(accountId);
            if (account == null) throw new AccountNotFoundException(accountId);

            String referenceId = UUID.randomUUID().toString();
            Transaction tx = new Transaction(0, referenceId, amount, "DEPOSIT", LocalDateTime.now(), account);
            transactionDAO.save(tx);

            account.setBalance(account.getBalance() + amount);
            accountDAO.update(account);

            return referenceId;

        } catch (SQLException e) {
            throw new IgirePayException("Deposit failed: " + e.getMessage(), e);
        }
    }

    public String withdraw(int accountId, double amount) {
        if (amount <= 0) throw new IgirePayException("Withdrawal amount must be positive");

        try {
            Account account = accountDAO.getById(accountId);
            if (account == null) throw new AccountNotFoundException(accountId);
            if (account.getBalance() < amount)
                throw new InsufficientBalanceException(account.getBalance(), amount);

            String referenceId = UUID.randomUUID().toString();
            Transaction tx = new Transaction(0, referenceId, amount, "WITHDRAW", LocalDateTime.now(), account);
            transactionDAO.save(tx);

            account.setBalance(account.getBalance() - amount);
            accountDAO.update(account);

            return referenceId;

        } catch (SQLException e) {
            throw new IgirePayException("Withdrawal failed: " + e.getMessage(), e);
        }
    }

    public String transfer(int fromAccountId, int toAccountId, double amount) {
        if (amount <= 0) throw new IgirePayException("Transfer amount must be positive");
        if (fromAccountId == toAccountId) throw new IgirePayException("Cannot transfer to the same account");

        try {
            Account from = accountDAO.getById(fromAccountId);
            Account to = accountDAO.getById(toAccountId);

            if (from == null) throw new AccountNotFoundException(fromAccountId);
            if (to == null) throw new AccountNotFoundException(toAccountId);
            if (from.getBalance() < amount)
                throw new InsufficientBalanceException(from.getBalance(), amount);

            String referenceId = UUID.randomUUID().toString();
            transactionDAO.transfer(from, to, amount, referenceId);

            return referenceId;

        } catch (SQLException e) {
            throw new IgirePayException("Transfer failed: " + e.getMessage(), e);
        }
    }

    public List<Transaction> getTransactionHistory(int accountId) {
        try {
            Account account = accountDAO.getById(accountId);
            if (account == null) throw new AccountNotFoundException(accountId);
            return transactionDAO.getByAccountId(accountId);
        } catch (SQLException e) {
            throw new IgirePayException("Failed to fetch transactions: " + e.getMessage(), e);
        }
    }

    public Account getAccount(int accountId) {
        try {
            Account account = accountDAO.getById(accountId);
            if (account == null) throw new AccountNotFoundException(accountId);
            return account;
        } catch (SQLException e) {
            throw new IgirePayException("Failed to fetch account: " + e.getMessage(), e);
        }
    }

    public Customer getCustomer(int customerId) {
        try {
            Customer customer = customerDAO.getById(customerId);
            if (customer == null) throw new IgirePayException("Customer not found with ID: " + customerId);
            return customer;
        } catch (SQLException e) {
            throw new IgirePayException("Failed to fetch customer: " + e.getMessage(), e);
        }
    }
}
