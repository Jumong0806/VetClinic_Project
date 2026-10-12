package vetclinic.service;

import vetclinic.exception.AuthenticationException;
import vetclinic.model.Staff;
import vetclinic.util.ValidationUtil;

/**
 * AuthService - handles login and logout and remembers who is logged in.
 * Owner: JP
 *
 * OOP concepts: ENCAPSULATION (currentStaff is private), custom EXCEPTION (AuthenticationException),
 * POLYMORPHISM (currentStaff may be a Veterinarian or a VetAssistant; we only use the Staff type).
 */
public class AuthService {

    private ClinicManager manager;
    private Staff currentStaff;   // null when nobody is logged in

    public AuthService(ClinicManager manager) {
        this.manager = manager;
    }

    // Checks the username and password. Returns the staff member, or throws AuthenticationException.
    public Staff login(String username, String password) throws AuthenticationException {
        if (ValidationUtil.isBlank(username) || password == null || password.isEmpty()) {
            throw new AuthenticationException("Please enter your username and password.");
        }
        Staff staff = manager.findStaffByUsername(username);
        // The same message for a wrong username and a wrong password, so nobody can guess which one was right.
        if (staff == null || !staff.checkPassword(password)) {
            throw new AuthenticationException("Wrong username or password.");
        }
        currentStaff = staff;
        return staff;
    }

    public void logout() {
        currentStaff = null;
    }

    // Used for the header and for the treatment permissions.
    public Staff getCurrentStaff() {
        return currentStaff;
    }

    public boolean isLoggedIn() {
        return currentStaff != null;
    }
}
