package vetclinic.model;

import java.time.LocalDate;
import vetclinic.util.ValidationUtil;

/**
 * Staff - base class for the people who work in the clinic.
 * Owner: Richnie
 *
 * OOP concepts shown here:
 *  - ABSTRACTION: abstract class with abstract methods (getRole, canDiagnose, treat, ...).
 *  - INHERITANCE: Veterinarian and VetAssistant extend this class.
 *  - ENCAPSULATION: private fields
 *  - INTERFACE: implements Describable (same interface as Animal, but the classes are unrelated).
 */
public abstract class Staff implements Describable {

    private String staffId;
    private String staffName;
    private String contactNumber;

    // The constructor uses the setters so every value is validated.
    public Staff(String staffId, String staffName, String contactNumber) {
        setStaffId(staffId);
        setStaffName(staffName);
        setContactNumber(contactNumber);
    }

    // Formats the staff details for dropdown menus.
    @Override
    public String toString() {
        return staffName + " (" + getRole() + ")";
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
