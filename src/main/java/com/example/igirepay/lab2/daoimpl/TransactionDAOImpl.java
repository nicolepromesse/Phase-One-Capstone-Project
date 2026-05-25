package com.example.igirepay.lab2.daoimpl;

import com.example.igirepay.lab1.model.Account;
import com.example.igirepay.lab1.model.Transaction;
import com.example.igirepay.lab2.dao.DAO;
import com.example.igirepay.lab2.database.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements DAO<Transaction> {

    @Override
    public void save(Transaction transaction) throws SQLException {
        Connection connection = DBConnection.connect();

        try {
            connection.setAutoCommit(false);

            Savepoint savepoint = connection.setSavepoint("before_transaction_insert");

            try {
                if (isAlreadyProcessed(connection, transaction.getReferenceId())) {
                    connection.rollback(savepoint);
                    throw new SQLException("Duplicate transaction: " + transaction.getReferenceId());
                }

                String insertTx =
                        "INSERT INTO transactions(account_id, reference_id, transaction_type, amount, created_at) VALUES(?, ?, ?, ?, ?)";

                try (PreparedStatement ps = connection.prepareStatement(insertTx)) {
                    ps.setInt(1, transaction.getAccount().getId());
                    ps.setString(2, transaction.getReferenceId());
                    ps.setString(3, transaction.getTransactionType());
                    ps.setDouble(4, transaction.getAmount());
                    ps.setTimestamp(5, Timestamp.valueOf(transaction.getTimestamp()));
                    ps.executeUpdate();
                }

                markAsProcessed(connection, transaction.getReferenceId());

                connection.commit();

            } catch (SQLException e) {
                connection.rollback(savepoint);
                throw e;
            }

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    @Override
    public Transaction getById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
        }

        return null;
    }

    @Override
    public List<Transaction> getAll() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                transactions.add(mapRow(rs));
            }
        }

        return transactions;
    }

    @Override
    public void update(Transaction transaction) throws SQLException {
        String sql =
                "UPDATE transactions SET account_id = ?, reference_id = ?, transaction_type = ?, amount = ? WHERE id = ?";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, transaction.getAccount().getId());
            ps.setString(2, transaction.getReferenceId());
            ps.setString(3, transaction.getTransactionType());
            ps.setDouble(4, transaction.getAmount());
            ps.setInt(5, transaction.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Transaction> getByAccountId(int accountId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                transactions.add(mapRow(rs));
            }
        }

        return transactions;
    }

    public void transfer(Account from, Account to, double amount, String referenceId) throws SQLException {
        Connection connection = DBConnection.connect();

        try {
            connection.setAutoCommit(false);

            Savepoint savepoint = connection.setSavepoint("before_transfer");

            try {
                if (isAlreadyProcessed(connection, referenceId)) {
                    connection.rollback(savepoint);
                    throw new SQLException("Duplicate transfer: " + referenceId);
                }

                double senderBalance = getBalance(connection, from.getId());

                if (senderBalance < amount) {
                    connection.rollback(savepoint);
                    throw new SQLException("Insufficient balance for account: " + from.getId());
                }

                debitAccount(connection, from.getId(), amount);
                creditAccount(connection, to.getId(), amount);

                LocalDateTime now = LocalDateTime.now();

                insertTransaction(connection, from.getId(), referenceId + "_DEBIT", "TRANSFER_OUT", amount, now);
                insertTransaction(connection, to.getId(), referenceId + "_CREDIT", "TRANSFER_IN", amount, now);

                markAsProcessed(connection, referenceId);

                connection.commit();

            } catch (SQLException e) {
                connection.rollback(savepoint);
                throw e;
            }

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    private boolean isAlreadyProcessed(Connection connection, String referenceId) throws SQLException {
        String sql = "SELECT id FROM processed_requests WHERE reference_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, referenceId);
            return ps.executeQuery().next();
        }
    }

    private void markAsProcessed(Connection connection, String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests(reference_id, processed_at) VALUES(?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, referenceId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    private double getBalance(Connection connection, int accountId) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            }
        }

        throw new SQLException("Account not found: " + accountId);
    }

    private void debitAccount(Connection connection, int accountId, double amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }

    private void creditAccount(Connection connection, int accountId, double amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }

    private void insertTransaction(Connection connection, int accountId, String referenceId,
                                   String type, double amount, LocalDateTime timestamp) throws SQLException {
        String sql =
                "INSERT INTO transactions(account_id, reference_id, transaction_type, amount, created_at) VALUES(?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, referenceId);
            ps.setString(3, type);
            ps.setDouble(4, amount);
            ps.setTimestamp(5, Timestamp.valueOf(timestamp));
            ps.executeUpdate();
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        int accountId = rs.getInt("account_id");

        Account account = new Account(accountId, null, null, 0, null) {
            @Override
            public void processTransaction() {}
        };

        return new Transaction(
                rs.getInt("id"),
                rs.getString("reference_id"),
                rs.getDouble("amount"),
                rs.getString("transaction_type"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                account
        );
    }
}