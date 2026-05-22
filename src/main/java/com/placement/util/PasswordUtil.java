package com.placement.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PasswordUtil.java
 *
 * Simple password hashing utility using SHA-256 + salt.
 * In production, consider bcrypt (e.g., jBCrypt library).
 */
public class PasswordUtil {

    /**
     * Hash a plain-text password with a random salt.
     * Format stored in DB: base64(salt):base64(hash)
     */
    public static String hashPassword(String plainPassword) {
        try {
            // Generate a random 16-byte salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // Hash password + salt using SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedBytes = md.digest(plainPassword.getBytes());

            // Encode both salt and hash as Base64 strings, join with ":"
            String saltStr = Base64.getEncoder().encodeToString(salt);
            String hashStr = Base64.getEncoder().encodeToString(hashedBytes);
            return saltStr + ":" + hashStr;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verify a plain-text password against a stored hash.
     *
     * @param plainPassword the password entered by the user
     * @param storedHash    the value from the database (salt:hash)
     * @return true if the password matches
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        try {
            // Split stored value back into salt and hash
            String[] parts = storedHash.split(":");
            if (parts.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            // Hash the provided password with the same salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] actualHash = md.digest(plainPassword.getBytes());

            // Compare byte-by-byte (constant-time comparison)
            if (actualHash.length != expectedHash.length) return false;
            int diff = 0;
            for (int i = 0; i < actualHash.length; i++) {
                diff |= actualHash[i] ^ expectedHash[i];
            }
            return diff == 0;

        } catch (Exception e) {
            return false;
        }
    }
}
