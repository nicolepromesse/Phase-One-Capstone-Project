package com.example.igirepay.lab2.daoimpl;

import com.example.igirepay.lab1.model.Account;
import com.example.igirepay.lab1.model.Customer;
import com.example.igirepay.lab1.model.Loan;
import com.example.igirepay.lab2.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAOImpl {

    public void save(Loan loan) throws SQLException {
        String sql = "INSERT INTO loans(account_id, customer_id, requested_amount, approved_amount, " +
                "interest_rate, repaid_amount, status, reference_id, requested_at, updated_at) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getAccount().getId());
            ps.setInt(2, loan.getCustomer().getId());
            ps.setDouble(3, loan.getRequestedAmount());
            ps.setDouble(4, loan.getApprovedAmount());
            ps.setDouble(5, loan.getInterestRate());
            ps.setDouble(6, loan.getRepaidAmount());
            ps.setString(7, loan.getStatus());
            ps.setString(8, loan.getReferenceId());
            ps.setTimestamp(9, Timestamp.valueOf(loan.getRequestedAt()));
            ps.setTimestamp(10, Timestamp.valueOf(loan.getUpdatedAt()));
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) loan.setId(keys.getInt(1));
        }
    }

    public List<Loan> getByCustomerId(int customerId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE customer_id = ? ORDER BY requested_at DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) loans.add(mapRow(rs));
        }
        return loans;
    }

    public List<Loan> getAll() throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans ORDER BY requested_at DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) loans.add(mapRow(rs));
        }
        return loans;
    }

    private Loan mapRow(ResultSet rs) throws SQLException {
        int accountId = rs.getInt("account_id");
        int customerId = rs.getInt("customer_id");
        Account account = new Account(accountId, null, null, 0, null) {
            @Override public void processTransaction() {}
        };
        Customer customer = new Customer(customerId, null, null, null, null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new Loan(
                rs.getInt("id"),
                account,
                customer,
                rs.getDouble("requested_amount"),
                rs.getDouble("approved_amount"),
                rs.getDouble("interest_rate"),
                rs.getDouble("repaid_amount"),
                rs.getString("status"),
                rs.getString("reference_id"),
                rs.getTimestamp("requested_at").toLocalDateTime(),
                updatedAt != null ? updatedAt.toLocalDateTime() : null
        );
    }
}
