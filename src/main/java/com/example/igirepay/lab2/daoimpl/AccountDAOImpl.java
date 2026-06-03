package com.example.igirepay.lab2.daoimpl;

import com.example.igirepay.lab1.model.Account;
import com.example.igirepay.lab1.model.Customer;
import com.example.igirepay.lab1.model.SavingsAccount;
import com.example.igirepay.lab1.model.WalletAccount;
import com.example.igirepay.lab2.dao.DAO;
import com.example.igirepay.lab2.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements DAO<Account> {

    @Override
    public void save(Account account) throws SQLException {

        String sql =
                "INSERT INTO accounts(customer_id, account_type, balance, created_at) VALUES(?, ?, ?, ?)";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {

            preparedStatement.setInt(1, account.getCustomer().getId());
            preparedStatement.setString(2, account.getAccountType());
            preparedStatement.setDouble(3, account.getBalance());
            preparedStatement.setTimestamp(
                    4,
                    java.sql.Timestamp.valueOf(account.getCreatedAt())
            );

            preparedStatement.executeUpdate();

            System.out.println("Account saved successfully");
        }
    }

    @Override
    public Account getById(int id) throws SQLException {

        String sql = "SELECT * FROM accounts WHERE id = ?";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {

            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                int accountId = resultSet.getInt("id");
                int customerId = resultSet.getInt("customer_id");
                String accountType = resultSet.getString("account_type");
                double balance = resultSet.getDouble("balance");
                LocalDateTime createdAt =
                        resultSet.getTimestamp("created_at").toLocalDateTime();

                Customer customer = new Customer();
                customer.setId(customerId);

                if (accountType.equalsIgnoreCase("WALLET")) {

                    return new WalletAccount(
                            accountId,
                            customer,
                            balance,
                            createdAt,
                            0,
                            0,
                            true,
                            0,
                            null
                    );

                } else {

                    return new SavingsAccount(
                            accountId,
                            customer,
                            accountType,
                            balance,
                            createdAt,
                            0,
                            0,
                            0,
                            0,
                            null
                    );
                }
            }
        }

        return null;
    }

    @Override
    public List<Account> getAll() throws SQLException {

        List<Account> accounts = new ArrayList<>();

        String sql = "SELECT * FROM accounts";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {

            while (resultSet.next()) {

                int accountId = resultSet.getInt("id");
                int customerId = resultSet.getInt("customer_id");
                String accountType = resultSet.getString("account_type");
                double balance = resultSet.getDouble("balance");
                LocalDateTime createdAt =
                        resultSet.getTimestamp("created_at").toLocalDateTime();

                Customer customer = new Customer();
                customer.setId(customerId);

                if (accountType.equalsIgnoreCase("WALLET")) {

                    accounts.add(
                            new WalletAccount(
                                    accountId,
                                    customer,
                                    balance,
                                    createdAt,
                                    0,
                                    0,
                                    true,
                                    0,
                                    null
                            )
                    );

                } else {

                    accounts.add(
                            new SavingsAccount(
                                    accountId,
                                    customer,
                                    accountType,
                                    balance,
                                    createdAt,
                                    0,
                                    0,
                                    0,
                                    0,
                                    null
                            )
                    );
                }
            }
        }

        return accounts;
    }

    @Override
    public void update(Account account) throws SQLException {

        String sql =
                "UPDATE accounts SET customer_id = ?, account_type = ?, balance = ? WHERE id = ?";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {

            preparedStatement.setInt(1, account.getCustomer().getId());
            preparedStatement.setString(2, account.getAccountType());
            preparedStatement.setDouble(3, account.getBalance());
            preparedStatement.setInt(4, account.getId());

            preparedStatement.executeUpdate();

            System.out.println("Account updated successfully");
        }
    }

    @Override
    public void delete(int id) throws SQLException {

        String sql = "DELETE FROM accounts WHERE id = ?";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {

            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();

            System.out.println("Account deleted successfully");
        }
    }

    public List<Account> getByCustomerId(int customerId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        try (
                Connection connection = DBConnection.connect();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, customerId);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                int accountId = resultSet.getInt("id");
                String accountType = resultSet.getString("account_type");
                double balance = resultSet.getDouble("balance");
                LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                Customer customer = new Customer();
                customer.setId(customerId);
                if (accountType.equalsIgnoreCase("WALLET")) {
                    accounts.add(new WalletAccount(accountId, customer, balance, createdAt, 0, 0, true, 0, null));
                } else {
                    accounts.add(new SavingsAccount(accountId, customer, accountType, balance, createdAt, 0, 0, 0, 0, null));
                }
            }
        }
        return accounts;
    }
}