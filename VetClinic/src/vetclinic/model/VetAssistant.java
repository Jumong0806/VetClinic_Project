package vetclinic.model;

import java.time.LocalDate;

import vetclinic.util.ValidationUtil;

/**
 * VetAssistant - helps the vet. Can only record basic care (no diagnosis, no medication).
 * Owner: Jermil
 *
 * OOP concepts: INHERITANCE (extends Staff), POLYMORPHISM (treat() behaves differently from Veterinarian),
 * ENCAPSULATION (shift is validated).
 */
public class VetAssistant extends Staff {

    private String shift;   // Morning, Afternoon or Night

    // Initializes a new Vet Assistant.
    public VetAssistant(String staffId, String staffName, String contactNumber, String shift) {
        super(staffId, staffName, contactNumber);
        setShift(shift);
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        if (!"Morning".equals(shift) && !"Afternoon".equals(shift) && !"Night".equals(shift)) {
            throw new IllegalArgumentException("Shift must be Morning, Afternoon or Night.");
        }
        this.shift = shift;
    }

    @Override
    public String getRole() {
        return "Vet Assistant";
    }

    @Override
    public boolean canPrescribeMedication() {
        return false;
    }

    @Override
    public boolean canDiagnose() {
        return false;
    }

    @Override
    public String getRoleDetails() {
        return "Shift: " + shift;
    }

    // An assistant records BASIC CARE ONLY: whatever diagnosis or medication was typed is ignored.
    @Override
    public TreatmentRecord treat(Treatable patient, LocalDate treatmentDate, String diagnosis, String treatment, String medication) {
        if (patient == null) {
            throw new IllegalArgumentException("No patient was selected.");
        }
        if (ValidationUtil.isBlank(treatment)) {
            throw new IllegalArgumentException("Treatment must not be blank.");
        }
        String recordId = patient.getAnimalId() + "-T" + (patient.getTreatmentHistory().size() + 1);
        TreatmentRecord record = new TreatmentRecord(recordId, patient.getAnimalId(), treatmentDate,
                "Basic care only", treatment, "None", this);
        patient.addTreatment(record);
        return record;
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " | " + getRoleDetails();
    }
}
