package com.example.igirepay.lab3.service;

import com.example.igirepay.lab1.model.Account;
import com.example.igirepay.lab1.model.Customer;
import com.example.igirepay.lab1.model.Loan;
import com.example.igirepay.lab2.daoimpl.LoanDAOImpl;
import com.example.igirepay.lab3.exception.InvalidAmountException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LoanService {

    private final LoanDAOImpl loanDAO = new LoanDAOImpl();

    public Loan requestLoan(Account account, Customer customer, double amount) throws SQLException {
        if (amount <= 0) throw new InvalidAmountException(amount);
        Loan loan = new Loan(
                0, account, customer, amount, 0, 5.00, 0,
                "PENDING", UUID.randomUUID().toString(),
                LocalDateTime.now(), LocalDateTime.now()
        );
        loanDAO.save(loan);
        return loan;
    }

    public List<Loan> getLoansForCustomer(int customerId) throws SQLException {
        return loanDAO.getByCustomerId(customerId);
    }

    public List<Loan> getAll() throws SQLException {
        return loanDAO.getAll();
    }
}
