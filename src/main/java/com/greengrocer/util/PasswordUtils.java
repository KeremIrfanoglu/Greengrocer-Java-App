package com.greengrocer.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password hashing and verification.
 * Currently uses SHA-256 for hashing.
 */
public class PasswordUtils {

    /**
     * Hashes a plain text password using SHA-256.
     * 
     * @param password The plain text password.
     * @return The hashed password string.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // fallback to plain text if hashing fails
        }
    }

    /**
     * Verify a password against a hash
     */
    public static boolean verifyPassword(String password, String storedHash) {
        String hashedInput = hashPassword(password);
        return hashedInput.equals(storedHash);
    }
}
