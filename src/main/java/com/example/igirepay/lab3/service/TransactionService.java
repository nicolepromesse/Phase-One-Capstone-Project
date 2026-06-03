package com.example.igirepay.lab3.service;

import com.example.igirepay.lab1.model.Account;
import com.example.igirepay.lab1.model.SavingsAccount;
import com.example.igirepay.lab1.model.Transaction;
import com.example.igirepay.lab1.model.WalletAccount;
import com.example.igirepay.lab2.daoimpl.AccountDAOImpl;
import com.example.igirepay.lab2.daoimpl.TransactionDAOImpl;
import com.example.igirepay.lab3.exception.AccountNotFoundException;
import com.example.igirepay.lab3.exception.InsufficientBalanceException;
import com.example.igirepay.lab3.exception.InvalidAmountException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public class TransactionService {

    private static final double WALLET_DAILY_TRANSFER_LIMIT = 500_000.0;
    private static final double WALLET_WITHDRAWAL_FEE       = 100.0;
    private static final double SAVINGS_MIN_BALANCE         = 1_000.0;
    private static final int    SAVINGS_MAX_WITHDRAWALS     = 5;

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

        if (account instanceof WalletAccount wallet) {
            double fee = WALLET_WITHDRAWAL_FEE;
            double totalDeducted = amount + fee;

            if (account.getBalance() < totalDeducted) {
                throw new InsufficientBalanceException(account.getBalance(),
                        totalDeducted);
            }

            String referenceId = UUID.randomUUID().toString();
            transactionDAO.save(new Transaction(0, referenceId, amount, "WITHDRAW", LocalDateTime.now(), account));
            account.setBalance(account.getBalance() - totalDeducted);
            accountDAO.update(account);
            return referenceId;

        } else if (account instanceof SavingsAccount) {
            int withdrawalsThisMonth = countWithdrawalsThisMonth(accountId);
            if (withdrawalsThisMonth >= SAVINGS_MAX_WITHDRAWALS) {
                throw new InvalidAmountException(
                        "Savings account withdrawal limit reached. Maximum " + SAVINGS_MAX_WITHDRAWALS + " withdrawals per month.");
            }
            if (account.getBalance() - amount < SAVINGS_MIN_BALANCE) {
                throw new InsufficientBalanceException(
                        account.getBalance() - SAVINGS_MIN_BALANCE, amount);
            }

            String referenceId = UUID.randomUUID().toString();
            transactionDAO.save(new Transaction(0, referenceId, amount, "WITHDRAW", LocalDateTime.now(), account));
            account.setBalance(account.getBalance() - amount);
            accountDAO.update(account);
            return referenceId;

        } else {
            if (account.getBalance() < amount) {
                throw new InsufficientBalanceException(account.getBalance(), amount);
            }
            String referenceId = UUID.randomUUID().toString();
            transactionDAO.save(new Transaction(0, referenceId, amount, "WITHDRAW", LocalDateTime.now(), account));
            account.setBalance(account.getBalance() - amount);
            accountDAO.update(account);
            return referenceId;
        }
    }

    public String transfer(int fromId, int toId, double amount) throws SQLException {
        if (amount <= 0) throw new InvalidAmountException(amount);
        if (fromId == toId) throw new IllegalArgumentException("Cannot transfer to the same account.");

        Account from = accountDAO.getById(fromId);
        Account to   = accountDAO.getById(toId);

        if (from == null) throw new AccountNotFoundException(fromId);
        if (to   == null) throw new AccountNotFoundException(toId);

        if (from instanceof WalletAccount) {
            double todayTotal = sumTransfersTodayFrom(fromId);
            if (todayTotal + amount > WALLET_DAILY_TRANSFER_LIMIT) {
                double remaining = WALLET_DAILY_TRANSFER_LIMIT - todayTotal;
                throw new InvalidAmountException(
                        String.format("Daily transfer limit exceeded. You can still transfer %.2f RWF today.", remaining));
            }
        }

        if (from instanceof SavingsAccount) {
            if (from.getBalance() - amount < SAVINGS_MIN_BALANCE) {
                throw new InsufficientBalanceException(
                        from.getBalance() - SAVINGS_MIN_BALANCE, amount);
            }
            int withdrawalsThisMonth = countWithdrawalsThisMonth(fromId);
            if (withdrawalsThisMonth >= SAVINGS_MAX_WITHDRAWALS) {
                throw new InvalidAmountException(
                        "Savings account transfer limit reached. Maximum " + SAVINGS_MAX_WITHDRAWALS + " withdrawals/transfers per month.");
            }
        }

        if (from.getBalance() < amount) {
            throw new InsufficientBalanceException(from.getBalance(), amount);
        }

        String referenceId = UUID.randomUUID().toString();
        transactionDAO.transfer(from, to, amount, referenceId);
        return referenceId;
    }

    public double applyInterest(int accountId) throws SQLException {
        Account account = accountDAO.getById(accountId);
        if (account == null) throw new AccountNotFoundException(accountId);
        if (!(account instanceof SavingsAccount savings)) {
            throw new InvalidAmountException("Interest only applies to Savings accounts.");
        }

        double interest = savings.getBalance() * savings.getInterestRate();
        savings.setBalance(savings.getBalance() + interest);
        accountDAO.update(savings);

        String referenceId = UUID.randomUUID().toString();
        transactionDAO.save(new Transaction(0, referenceId, interest, "INTEREST", LocalDateTime.now(), savings));
        return interest;
    }

    private int countWithdrawalsThisMonth(int accountId) throws SQLException {
        YearMonth current = YearMonth.now();
        List<Transaction> all = transactionDAO.getByAccountId(accountId);
        return (int) all.stream()
                .filter(t -> (t.getTransactionType().equals("WITHDRAW") || t.getTransactionType().equals("TRANSFER_OUT")))
                .filter(t -> YearMonth.from(t.getTimestamp()).equals(current))
                .count();
    }

    private double sumTransfersTodayFrom(int accountId) throws SQLException {
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Transaction> all = transactionDAO.getByAccountId(accountId);
        return all.stream()
                .filter(t -> t.getTransactionType().equals("TRANSFER_OUT"))
                .filter(t -> t.getTimestamp().toLocalDate().equals(today))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public List<Transaction> getByAccount(int accountId) throws SQLException {
        return transactionDAO.getByAccountId(accountId);
    }

    public List<Transaction> getAll() throws SQLException {
        return transactionDAO.getAll();
    }
}