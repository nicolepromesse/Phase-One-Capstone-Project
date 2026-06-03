package com.example.igirepay.lab3.service;

import com.example.igirepay.lab1.model.Account;
import com.example.igirepay.lab1.model.SavingsAccount;
import com.example.igirepay.lab1.model.WalletAccount;
import com.example.igirepay.lab2.daoimpl.AccountDAOImpl;
import com.example.igirepay.lab3.exception.AccountNotFoundException;

import java.sql.SQLException;
import java.util.List;

public class AccountService {

    private final AccountDAOImpl accountDAO = new AccountDAOImpl();

    public void createAccount(Account account) throws SQLException {
        accountDAO.save(account);
    }

    public Account getById(int id) throws SQLException {
        Account account = accountDAO.getById(id);
        if (account == null) throw new AccountNotFoundException(id);
        return account;
    }

    public List<Account> getAll() throws SQLException {
        return accountDAO.getAll();
    }

    public void update(Account account) throws SQLException {
        accountDAO.update(account);
    }

    public void deleteInactive(int id) throws SQLException {
        Account account = getById(id);
        if (account.getBalance() > 0) {
            throw new IllegalStateException(
                "Cannot delete active account with balance: " + account.getBalance() + " RWF");
        }
        accountDAO.delete(id);
    }

    public String getAccountTypeName(Account account) {
        if (account instanceof WalletAccount) return "Wallet";
        if (account instanceof SavingsAccount) return "Savings";
        return account.getAccountType();
    }

    public List<Account> getByCustomerId(int customerId) throws SQLException {
        return accountDAO.getByCustomerId(customerId);
    }
}