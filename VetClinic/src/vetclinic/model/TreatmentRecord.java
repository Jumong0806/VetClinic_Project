package vetclinic.model;

import java.io.Serializable;
import java.time.LocalDate;

import vetclinic.util.ValidationUtil;

/**
 * TreatmentRecord - ONE visit in an animal's history. This class solves the problem statement:
 * every treatment is stored, so the history can be shown in seconds.
 * Owner: JP
 *
 * OOP concepts: ENCAPSULATION (validating setters), OVERLOADING (two constructors),
 * INTERFACE (implements Describable).
 */
public class TreatmentRecord implements Describable, Serializable {

    private static final long serialVersionUID = 1L;

    private String recordId;          // animalId + "-T" + number, e.g. "A001-T3"
    private String animalId;          // which patient this record belongs to
    private LocalDate treatmentDate;
    private String diagnosis;
    private String treatment;
    private String medication;
    private LocalDate followUpDate;   // optional (can be null)
    private Staff performedBy;

    // Constructor 1: no follow-up date.
    public TreatmentRecord(String recordId, String animalId, LocalDate treatmentDate, String diagnosis, String treatment, String medication, Staff performedBy) {
        setRecordId(recordId);
        setAnimalId(animalId);
        setTreatmentDate(treatmentDate);
        setDiagnosis(diagnosis);
        setTreatment(treatment);
        setMedication(medication);
        setPerformedBy(performedBy);
    }

    // Constructor 2 (OVERLOADED): adds the follow-up date. It reuses constructor 1 first.
    public TreatmentRecord(String recordId, String animalId, LocalDate treatmentDate, String diagnosis, String treatment, String medication, LocalDate followUpDate, Staff performedBy) {
        this(recordId, animalId, treatmentDate, diagnosis, treatment, medication, performedBy);
        setFollowUpDate(followUpDate);
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        if (ValidationUtil.isBlank(recordId)) {
            throw new IllegalArgumentException("Record ID must not be blank.");
        }
        this.recordId = recordId.trim();
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        if (ValidationUtil.isBlank(animalId)) {
            throw new IllegalArgumentException("Animal ID of the record must not be blank.");
        }
        this.animalId = animalId.trim();
    }

    public LocalDate getTreatmentDate() {
        return treatmentDate;
    }

    public void setTreatmentDate(LocalDate treatmentDate) {
        if (treatmentDate == null) {
            throw new IllegalArgumentException("Treatment date must not be empty.");
        }
        if (ValidationUtil.isFutureDate(treatmentDate)) {
            throw new IllegalArgumentException("Treatment date cannot be in the future.");
        }
        this.treatmentDate = treatmentDate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        if (ValidationUtil.isBlank(diagnosis)) {
            throw new IllegalArgumentException("Diagnosis must not be blank.");
        }
        this.diagnosis = diagnosis.trim();
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        if (ValidationUtil.isBlank(treatment)) {
            throw new IllegalArgumentException("Treatment must not be blank.");
        }
        this.treatment = treatment.trim();
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        if (ValidationUtil.isBlank(medication)) {
            throw new IllegalArgumentException("Medication must not be blank (write None if there is none).");
        }
        this.medication = medication.trim();
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    // The follow-up date is optional, but if given it must not be before the treatment date.
    public void setFollowUpDate(LocalDate followUpDate) {
        if (followUpDate != null && followUpDate.isBefore(treatmentDate)) {
            throw new IllegalArgumentException("Follow-up date cannot be before the treatment date.");
        }
        this.followUpDate = followUpDate;
    }

    public Staff getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Staff performedBy) {
        if (performedBy == null) {
            throw new IllegalArgumentException("A treatment must have a staff member who performed it.");
        }
        this.performedBy = performedBy;
    }

    // True when a follow-up was planned and its date is today or already passed.
    public boolean isFollowUpDue() {
        return followUpDate != null && !followUpDate.isAfter(LocalDate.now());
    }

    @Override
    public String getSummary() {
        String followUp = (followUpDate == null) ? "none" : followUpDate.toString();
        return recordId + " | " + treatmentDate + " | " + diagnosis + " | " + treatment
                + " | Medication: " + medication + " | Follow-up: " + followUp
                + " | By: " + performedBy.getStaffName();
    }
}
