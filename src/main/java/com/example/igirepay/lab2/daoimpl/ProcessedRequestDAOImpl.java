package com.example.igirepay.lab2.daoimpl;

import com.example.igirepay.lab1.model.ProcessedRequest;
import com.example.igirepay.lab2.dao.DAO;
import com.example.igirepay.lab2.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessedRequestDAOImpl implements DAO<ProcessedRequest> {

    @Override
    public void save(ProcessedRequest request) throws SQLException {
        String sql = "INSERT INTO processed_requests(reference_id, processed_at) VALUES(?, ?)";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, request.getReferenceId());
            ps.setTimestamp(2, Timestamp.valueOf(request.getProcessedAt()));
            ps.executeUpdate();
        }
    }

    @Override
    public ProcessedRequest getById(int id) throws SQLException {
        String sql = "SELECT * FROM processed_requests WHERE id = ?";

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
    public List<ProcessedRequest> getAll() throws SQLException {
        List<ProcessedRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM processed_requests ORDER BY processed_at DESC";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                requests.add(mapRow(rs));
            }
        }

        return requests;
    }

    @Override
    public void update(ProcessedRequest request) throws SQLException {
        String sql = "UPDATE processed_requests SET reference_id = ?, processed_at = ? WHERE id = ?";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, request.getReferenceId());
            ps.setTimestamp(2, Timestamp.valueOf(request.getProcessedAt()));
            ps.setInt(3, request.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM processed_requests WHERE id = ?";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existsByReferenceId(String referenceId) throws SQLException {
        String sql = "SELECT id FROM processed_requests WHERE reference_id = ?";

        try (
                Connection connection = DBConnection.connect();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, referenceId);
            return ps.executeQuery().next();
        }
    }

    private ProcessedRequest mapRow(ResultSet rs) throws SQLException {
        return new ProcessedRequest(
                rs.getInt("id"),
                rs.getString("reference_id"),
                rs.getTimestamp("processed_at").toLocalDateTime()
        );
    }
}