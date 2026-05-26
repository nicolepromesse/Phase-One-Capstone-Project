package com.example.igirepay.lab3.service;

import com.example.igirepay.lab1.model.Customer;
import com.example.igirepay.lab2.daoimpl.CustomerDAOImpl;
import com.example.igirepay.lab3.exception.AccountLockedException;
import com.example.igirepay.lab3.exception.InvalidPinException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerService {

    private static final int MAX_ATTEMPTS = 3;

    private final CustomerDAOImpl customerDAO = new CustomerDAOImpl();

    private final Map<Integer, Integer> failedAttempts = new HashMap<>();

    public void register(Customer customer) throws SQLException {
        customerDAO.save(customer);
    }

    public Customer getById(int id) throws SQLException {
        return customerDAO.getById(id);
    }

    public Customer getCustomerByPin(String pin) throws SQLException {
        List<Customer> customers = customerDAO.getAll();

        for (Customer customer : customers) {
            if (customer.getPin().equals(pin)) {
                return customer;
            }
        }

        return null;
    }

    public List<Customer> getAll() throws SQLException {
        return customerDAO.getAll();
    }

    public void update(Customer customer) throws SQLException {
        customerDAO.update(customer);
    }

    public void delete(int id) throws SQLException {
        customerDAO.delete(id);
    }

    public boolean validatePin(Customer customer, String enteredPin) {

        int id = customer.getId();

        int attempts = failedAttempts.getOrDefault(id, 0);

        if (attempts >= MAX_ATTEMPTS) {
            throw new AccountLockedException();
        }

        if (customer.getPin().equals(enteredPin)) {

            failedAttempts.remove(id);

            return true;

        } else {

            int newAttempts = attempts + 1;

            failedAttempts.put(id, newAttempts);

            int remaining = MAX_ATTEMPTS - newAttempts;

            if (remaining <= 0) {
                throw new AccountLockedException();
            }

            throw new InvalidPinException(remaining);
        }
    }

    public void changePin(Customer customer,
                          String oldPin,
                          String newPin) throws SQLException {

        validatePin(customer, oldPin);

        customer.setPin(newPin);

        customerDAO.update(customer);
    }

    public void resetFailedAttempts(int customerId) {
        failedAttempts.remove(customerId);
    }
}