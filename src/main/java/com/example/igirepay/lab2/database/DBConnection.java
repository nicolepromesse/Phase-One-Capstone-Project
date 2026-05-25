package com.example.igirepay.lab2.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:postgresql://localhost:2000/Igirepay";

    private static final String USER = "postgres";
    private static final String PASSWORD = "123";

    public static Connection connect() throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}