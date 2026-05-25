package com.example.igirepay.lab2.daoimpl;

import com.example.igirepay.lab1.model.Customer;
import com.example.igirepay.lab2.dao.DAO;
import com.example.igirepay.lab2.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl
        implements DAO<Customer> {

    @Override
    public void save(Customer customer)
            throws SQLException {

        String sql =
                "INSERT INTO customers(full_name, email, phone_number, pin) VALUES(?, ?, ?, ?)";

        try (

                Connection connection =
                        DBConnection.connect();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql)

        ) {

            preparedStatement.setString(
                    1,
                    customer.getFullName()
            );

            preparedStatement.setString(
                    2,
                    customer.getEmail()
            );

            preparedStatement.setString(
                    3,
                    customer.getPhoneNumber()
            );

            preparedStatement.setString(
                    4,
                    customer.getPin()
            );

            preparedStatement.executeUpdate();

            System.out.println(
                    "Customer saved successfully"
            );
        }
    }

    @Override
    public Customer getById(int id)
            throws SQLException {

        String sql =
                "SELECT * FROM customers WHERE id = ?";

        try (

                Connection connection =
                        DBConnection.connect();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql)

        ) {

            preparedStatement.setInt(1, id);

            ResultSet resultSet =
                    preparedStatement.executeQuery();

            if (resultSet.next()) {

                return new Customer(

                        resultSet.getInt("id"),

                        resultSet.getString("full_name"),

                        resultSet.getString("email"),

                        resultSet.getString("phone_number"),

                        resultSet.getString("pin")
                );
            }
        }

        return null;
    }

    @Override
    public List<Customer> getAll()
            throws SQLException {

        List<Customer> customers =
                new ArrayList<>();

        String sql =
                "SELECT * FROM customers";

        try (

                Connection connection =
                        DBConnection.connect();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        preparedStatement.executeQuery()

        ) {

            while (resultSet.next()) {

                Customer customer =
                        new Customer(

                                resultSet.getInt("id"),

                                resultSet.getString("full_name"),

                                resultSet.getString("email"),

                                resultSet.getString("phone_number"),

                                resultSet.getString("pin")
                        );

                customers.add(customer);
            }
        }

        return customers;
    }

    @Override
    public void update(Customer customer)
            throws SQLException {

        String sql =
                "UPDATE customers SET full_name = ?, email = ?, phone_number = ?, pin = ? WHERE id = ?";

        try (

                Connection connection =
                        DBConnection.connect();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql)

        ) {

            preparedStatement.setString(
                    1,
                    customer.getFullName()
            );

            preparedStatement.setString(
                    2,
                    customer.getEmail()
            );

            preparedStatement.setString(
                    3,
                    customer.getPhoneNumber()
            );

            preparedStatement.setString(
                    4,
                    customer.getPin()
            );

            preparedStatement.setInt(
                    5,
                    customer.getId()
            );

            preparedStatement.executeUpdate();

            System.out.println(
                    "Customer updated successfully"
            );
        }
    }

    @Override
    public void delete(int id)
            throws SQLException {

        String sql =
                "DELETE FROM customers WHERE id = ?";

        try (

                Connection connection =
                        DBConnection.connect();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql)

        ) {

            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();

            System.out.println(
                    "Customer deleted successfully"
            );
        }
    }
}