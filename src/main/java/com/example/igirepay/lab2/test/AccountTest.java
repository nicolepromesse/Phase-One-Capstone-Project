package com.example.igirepay.lab2.test;

import com.example.igirepay.lab1.model.Customer;
import com.example.igirepay.lab1.model.WalletAccount;
import com.example.igirepay.lab2.daoimpl.AccountDAOImpl;

import java.time.LocalDateTime;

public class AccountTest {

    public static void main(String[] args) {

        AccountDAOImpl accountDAO = new AccountDAOImpl();

        try {
            Customer customer = new Customer();
               customer.setId(1);
            WalletAccount account = new WalletAccount(
                    0,
                    customer,
                    5000.0,
                    LocalDateTime.now(),
                    10000.0,
                    50.0,
                    true,
                    0.0,
                    null
            );

            accountDAO.save(account);

            System.out.println("Account created successfully!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}