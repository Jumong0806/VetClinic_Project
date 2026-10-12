package vetclinic.model;

import java.util.List;

/**
 * Treatable - something that can receive treatments and show its treatment history.
 * Owner: Gab
 *
 * OOP concept: INTERFACE + ABSTRACTION. Staff.treat(...) and HistoryDialog work with a
 * Treatable, not with a specific class, so they do not care HOW the patient stores its records.
 */
public interface Treatable {

    // Adds one treatment record to this patient.
    void addTreatment(TreatmentRecord record);

    // Returns the patient's treatment records.
    List<TreatmentRecord> getTreatmentHistory();

    // Lets Staff.treat() stamp the new record with the patient's ID.
    String getAnimalId();
}
