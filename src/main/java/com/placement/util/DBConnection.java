package com.placement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * 
 * Utility class to manage MySQL database connections using JDBC.
 * This class follows the Singleton-like pattern for connection management.
 * 
 * IMPORTANT: Update DB_URL, DB_USER, and DB_PASSWORD before running!
 */
public class DBConnection {

    // ── Database Configuration ─────────────────────────────────────────────
    // Change "localhost" and "3306" if your MySQL runs elsewhere
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/placement_tracker"
                                              + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER     = "root";        // your MySQL username
    private static final String DB_PASSWORD = "root123";        // your MySQL password
    private static final String DRIVER      = "com.mysql.cj.jdbc.Driver";

    // Static block: load the JDBC driver once when the class is first used
    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            // If this error appears, the MySQL connector JAR is not in the classpath
            throw new RuntimeException("MySQL JDBC Driver not found. Add mysql-connector-java to your project.", e);
        }
    }

    /**
     * Returns a new database connection.
     * Always close the connection after use (use try-with-resources).
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Safely close a connection without throwing exceptions.
     * Useful in finally blocks.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Warning: Could not close DB connection: " + e.getMessage());
            }
        }
    }
}
