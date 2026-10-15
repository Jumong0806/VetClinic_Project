package vetclinic.service;

import vetclinic.exception.AuthenticationException;
import vetclinic.util.PasswordUtil;

/**
 * AuthService - handles login and logout and remembers who is logged in.
 * Owner: JP
 *
 * OOP concepts: ENCAPSULATION (currentStaff is private), custom EXCEPTION (AuthenticationException),
 * POLYMORPHISM (currentStaff may be a Veterinarian or a VetAssistant; we only use the Staff type).
 */
public class AuthService {

    private boolean loggedIn = false;

    // Master admin credentials for the kiosk terminal
    private final String ADMIN_USER = "admin";
    private final String ADMIN_HASH = PasswordUtil.hash("admin123");

    public boolean login(String username, String password) throws AuthenticationException {
        // Hashes the input and compares it to the stored hash
        if (ADMIN_USER.equals(username) && ADMIN_HASH.equals(PasswordUtil.hash(password))) {
            loggedIn = true;
            return true;
        }
        throw new AuthenticationException("Invalid admin username or password.");
    }

    public void logout() {
        loggedIn = false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}