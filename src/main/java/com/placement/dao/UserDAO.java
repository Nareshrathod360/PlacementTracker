package com.placement.dao;

import com.placement.model.User;
import com.placement.util.DBConnection;
import com.placement.util.PasswordUtil;

import java.sql.*;

/**
 * UserDAO.java - Data Access Object for User operations.
 * Handles all database queries related to users.
 * Uses PreparedStatement to prevent SQL injection.
 */
public class UserDAO {

    /**
     * Register a new user. Hashes the password before saving.
     *
     * @param user User object with plain-text password
     * @return true if registration succeeded
     */
    public boolean registerUser(User user) {
        // SQL to insert a new user (? placeholders prevent SQL injection)
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Hash the plain-text password before storing
            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, hashedPassword);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;   // true = success

        } catch (SQLIntegrityConstraintViolationException e) {
            // Email already exists (UNIQUE constraint violation)
            System.out.println("Registration failed: Email already registered.");
            return false;
        } catch (SQLException e) {
            System.err.println("DB Error in registerUser: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate login credentials.
     * Fetches the user by email, then verifies the password hash.
     *
     * @param email    entered email
     * @param password entered plain-text password
     * @return User object if valid, null if invalid credentials
     */
    public User loginUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                // Verify the entered password against the stored hash
                if (PasswordUtil.verifyPassword(password, storedHash)) {
                    // Build and return the User object
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setCreatedAt(rs.getString("created_at"));
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("DB Error in loginUser: " + e.getMessage());
        }

        return null; // Login failed
    }

    /**
     * Check if an email is already registered.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true if any row found
        } catch (SQLException e) {
            System.err.println("DB Error in emailExists: " + e.getMessage());
            return false;
        }
    }
}
