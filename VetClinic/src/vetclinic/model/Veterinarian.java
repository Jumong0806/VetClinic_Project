package vetclinic.model;

import java.time.LocalDate;

import vetclinic.util.ValidationUtil;

/**
 * Veterinarian - a licensed vet. Can diagnose and prescribe medication.
 * Owner: Gab
 *
 * OOP concepts: INHERITANCE (extends Staff, uses super), POLYMORPHISM (own version of treat()),
 * ENCAPSULATION (private fields with validation).
 */
public class Veterinarian extends Staff {

    private static final long serialVersionUID = 1L;

    private String licenseNumber;
    private String specialization;

    public Veterinarian(String staffId, String staffName, String contactNumber, String username, String password, String licenseNumber, String specialization) {
        super(staffId, staffName, contactNumber, username, password);
        setLicenseNumber(licenseNumber);
        setSpecialization(specialization);
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        if (ValidationUtil.isBlank(licenseNumber)) {
            throw new IllegalArgumentException("License number must not be blank.");
        }
        this.licenseNumber = licenseNumber.trim();
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        if (ValidationUtil.isBlank(specialization)) {
            throw new IllegalArgumentException("Specialization must not be blank.");
        }
        this.specialization = specialization.trim();
    }

    @Override
    public String getRole() {
        return "Veterinarian";
    }

    @Override
    public boolean canPrescribeMedication() {
        return true;
    }

    @Override
    public boolean canDiagnose() {
        return true;
    }

    @Override
    public String getRoleDetails() {
        return "License: " + licenseNumber + " | Specialization: " + specialization;
    }

    // A veterinarian saves the FULL diagnosis and medication that was entered.
    @Override
    public TreatmentRecord treat(Treatable patient, LocalDate treatmentDate, String diagnosis, String treatment, String medication) {
        if (patient == null) {
            throw new IllegalArgumentException("No patient was selected.");
        }
        // If no medication was typed, we save "None" so the record is still complete.
        String medicationToSave = ValidationUtil.isBlank(medication) ? "None" : medication;
        String recordId = patient.getAnimalId() + "-T" + (patient.getTreatmentHistory().size() + 1);
        TreatmentRecord record = new TreatmentRecord(recordId, patient.getAnimalId(), treatmentDate,
                diagnosis, treatment, medicationToSave, this);
        patient.addTreatment(record);   // works for ANY Treatable (interface in action)
        return record;
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " | " + getRoleDetails();
    }
}
