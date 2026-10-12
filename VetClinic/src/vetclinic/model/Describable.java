package vetclinic.model;

/**
 * Describable - gives any object a readable one-line summary.
 * Owner: Gab
 *
 * OOP concept: INTERFACE. Animal, Staff, Owner and TreatmentRecord are UNRELATED classes,
 * yet all of them promise the same method (getSummary). This is the rubric's
 * "interface implemented by two or more unrelated classes".
 */
public interface Describable {

    // Every class that implements this interface must write its own getSummary().
    String getSummary();
}
