package com.example.igirepay.lab2.test;

import com.example.igirepay.lab1.model.Customer;
import com.example.igirepay.lab2.daoimpl.CustomerDAOImpl;

public class CustomerTest {

    public static void main(String[] args) {

        CustomerDAOImpl customerDAO =
                new CustomerDAOImpl();

        try {

            Customer customer =
                    new Customer(
                            0,
                            "Nicole",
                            "nicole@gmail.com",
                            "0780000000",
                            "124"
                    );

            customerDAO.save(customer);

        } catch (Exception e) {

            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }
}