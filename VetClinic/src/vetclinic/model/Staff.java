package vetclinic.model;

import java.io.Serializable;
import java.time.LocalDate;

import vetclinic.util.PasswordUtil;
import vetclinic.util.ValidationUtil;

/**
 * Staff - base class for the people who work in the clinic.
 * Owner: Richnie
 *
 * OOP concepts shown here:
 *  - ABSTRACTION: abstract class with abstract methods (getRole, canDiagnose, treat, ...).
 *  - INHERITANCE: Veterinarian and VetAssistant extend this class.
 *  - ENCAPSULATION: private fields; the password is stored only as a hash.
 *  - INTERFACE: implements Describable (same interface as Animal, but the classes are unrelated).
 */
public abstract class Staff implements Describable, Serializable {

    private static final long serialVersionUID = 1L;

    private String staffId;
    private String staffName;
    private String contactNumber;
    private String username;       // login name, must be unique
    private String passwordHash;   // SHA-256 hash, never the plain password

    // The constructor uses the setters so every value is validated.
    public Staff(String staffId, String staffName, String contactNumber, String username, String password) {
        setStaffId(staffId);
        setStaffName(staffName);
        setContactNumber(contactNumber);
        setUsername(username);
        setPassword(password);      // turns the plain password into a hash
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        if (ValidationUtil.isBlank(staffId)) {
            throw new IllegalArgumentException("Staff ID must not be blank.");
        }
        this.staffId = staffId.trim();
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        if (ValidationUtil.isBlank(staffName)) {
            throw new IllegalArgumentException("Staff name must not be blank.");
        }
        this.staffName = staffName.trim();
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        if (!ValidationUtil.isValidContactNumber(contactNumber)) {
            throw new IllegalArgumentException("Staff contact number must be 11 digits and start with 09.");
        }
        this.contactNumber = contactNumber.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (!ValidationUtil.isValidUsername(username)) {
            throw new IllegalArgumentException("Username must have at least 4 characters and no spaces.");
        }
        this.username = username.trim();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    // Used when a stored hash is loaded back (for example from the database).
    public void setPasswordHash(String passwordHash) {
        if (ValidationUtil.isBlank(passwordHash)) {
            throw new IllegalArgumentException("Password hash must not be blank.");
        }
        this.passwordHash = passwordHash;
    }

    // Validates the plain password, then stores only its hash.
    public void setPassword(String plainPassword) {
        if (!ValidationUtil.isValidPassword(plainPassword)) {
            throw new IllegalArgumentException("Password must have at least 6 characters.");
        }
        this.passwordHash = PasswordUtil.hash(plainPassword);
    }

    // Used by AuthService.login(): hashes what the user typed and compares the hashes.
    public boolean checkPassword(String plainPassword) {
        return PasswordUtil.matches(plainPassword, passwordHash);
    }

    @Override
    public String getSummary() {
        return staffId + " | " + staffName + " | " + getRole() + " | " + contactNumber;
    }

    // ---------------- abstract methods (ABSTRACTION + POLYMORPHISM) ----------------

    public abstract String getRole();

    public abstract boolean canPrescribeMedication();

    // Role restriction: only some roles may write a diagnosis.
    public abstract boolean canDiagnose();

    // Role-specific information shown in "Selected Staff Details".
    public abstract String getRoleDetails();

    // Creates a TreatmentRecord for any Treatable patient. Each role does it differently.
    public abstract TreatmentRecord treat(Treatable patient, LocalDate treatmentDate, String diagnosis, String treatment, String medication);
}
