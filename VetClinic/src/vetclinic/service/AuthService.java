package vetclinic.service;

import vetclinic.exception.AuthenticationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * AuthService - handles administrator authentication and login verification.
 * Owner: JP
 *
 * OOP concepts: ENCAPSULATION (private loggedIn session flag),
 * custom EXCEPTION (AuthenticationException).
 */
public class AuthService {

    private boolean loggedIn = false;

    // Master admin credentials for the kiosk terminal
    private final String ADMIN_USER = "admin";
    private final String ADMIN_HASH = hash("admin123");

    public boolean login(String username, String password) throws AuthenticationException {
        // Hashes the input and compares it to the stored hash
        if (ADMIN_USER.equals(username) && ADMIN_HASH.equals(hash(password))) {
            loggedIn = true;
            return true;
        }
        throw new AuthenticationException("Invalid admin username or password.");
    }

    // Returns the SHA-256 hash of the password as hex text, for example "a665a459...".
    public static String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));   // each byte becomes 2 hex characters
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 exists in every Java installation, so this should never happen.
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }
}

