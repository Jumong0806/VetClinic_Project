package vetclinic.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import vetclinic.util.ValidationUtil;

/**
 * Animal - base class for every patient of the clinic.
 * Owner: JP
 *
 * OOP concepts shown here:
 *  - ABSTRACTION: the class is abstract and has abstract methods (getSpecies, getCareNote, ...).
 *  - ENCAPSULATION: every field is private and every setter validates its input.
 *  - INHERITANCE: Dog, Cat and Livestock extend this class.
 *  - INTERFACES: implements Describable and Treatable.
 *  - OVERLOADING: two constructors with different parameters.
 *  - STATIC member: totalAnimals counts how many animals exist.
 */
public abstract class Animal implements Describable, Treatable, Serializable {

    // Needed for Java serialization (saving objects to a file).
    private static final long serialVersionUID = 1L;

    private String animalId;                              // e.g. "A001"
    private String name;
    private int age;
    private double weightKg;                              // 0 = not yet weighed
    private Owner owner;
    private List<TreatmentRecord> treatmentHistory;
    private LocalDate registeredDate;
    private static int totalAnimals;                      // STATIC: belongs to the class, not to one animal
    public static final int MAX_AGE = 40;                 // limits used by the setters
    public static final double MAX_WEIGHT_KG = 1500;

    // Constructor 1: no weight given, so it calls constructor 2 with weight 0.
    public Animal(String animalId, String name, int age, Owner owner) {
        this(animalId, name, age, 0, owner);
    }

    // Constructor 2 (OVERLOADED): all details. It uses the setters so the validation always runs.
    public Animal(String animalId, String name, int age, double weightKg, Owner owner) {
        setAnimalId(animalId);
        setName(name);
        setAge(age);
        setWeightKg(weightKg);
        setOwner(owner);
        this.treatmentHistory = new ArrayList<>();
        this.registeredDate = LocalDate.now();
        totalAnimals++;                                   // only counted if all validation passed
    }

    // ---------------- getters and setters (ENCAPSULATION) ----------------

    @Override
    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        if (ValidationUtil.isBlank(animalId)) {
            throw new IllegalArgumentException("Animal ID must not be blank.");
        }
        this.animalId = animalId.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (ValidationUtil.isBlank(name)) {
            throw new IllegalArgumentException("Animal name must not be blank.");
        }
        this.name = name.trim();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (!ValidationUtil.isInRange(age, 0, MAX_AGE)) {
            throw new IllegalArgumentException("Age must be between 0 and " + MAX_AGE + " years.");
        }
        this.age = age;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        if (!ValidationUtil.isInRange(weightKg, 0, MAX_WEIGHT_KG)) {
            throw new IllegalArgumentException("Weight must be between 0 and " + MAX_WEIGHT_KG + " kg.");
        }
        this.weightKg = weightKg;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        if (owner == null) {
            throw new IllegalArgumentException("An animal must have an owner.");
        }
        this.owner = owner;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDate registeredDate) {
        if (registeredDate == null || ValidationUtil.isFutureDate(registeredDate)) {
            throw new IllegalArgumentException("Registered date must not be empty or in the future.");
        }
        this.registeredDate = registeredDate;
    }

    // ---------------- Treatable (interface) ----------------

    @Override
    public void addTreatment(TreatmentRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Treatment record must not be empty.");
        }
        treatmentHistory.add(record);
    }

    // Returns a COPY so other classes cannot change our private list without validation (encapsulation).
    @Override
    public List<TreatmentRecord> getTreatmentHistory() {
        return new ArrayList<>(treatmentHistory);
    }

    // Used by ClinicManager.updateAnimal() to keep the old history when an animal is replaced.
    public void setTreatmentHistory(List<TreatmentRecord> history) {
        if (history == null) {
            throw new IllegalArgumentException("Treatment history must not be null.");
        }
        this.treatmentHistory = new ArrayList<>(history);
    }

    // Finds the record with the newest treatment date. Returns null if there are no records.
    public TreatmentRecord getLatestTreatment() {
        TreatmentRecord latest = null;
        for (TreatmentRecord record : treatmentHistory) {
            if (latest == null || record.getTreatmentDate().isAfter(latest.getTreatmentDate())) {
                latest = record;
            }
        }
        return latest;
    }

    // Phase 2 (notifications): latest visit + the interval of this species.
    public LocalDate getNextCheckupDate() {
        TreatmentRecord latest = getLatestTreatment();
        LocalDate start = (latest == null) ? registeredDate : latest.getTreatmentDate();
        return start.plusMonths(getCheckupIntervalMonths());
    }

    // ---------------- Describable (interface) ----------------

    @Override
    public String getSummary() {
        return animalId + " | " + name + " | " + getSpecies() + " | " + age + " yr | "
                + weightKg + " kg | Owner: " + owner.getOwnerName();
    }

    // ---------------- static member ----------------

    public static int getTotalAnimals() {
        return totalAnimals;
    }

    // ClinicManager resets the counter after loading, updating or deleting.
    public static void setTotalAnimals(int total) {
        if (total < 0) {
            throw new IllegalArgumentException("Total animals cannot be negative.");
        }
        totalAnimals = total;
    }

    // ---------------- abstract methods (ABSTRACTION) ----------------
    // Each subclass MUST write these, and each one gives a different answer (POLYMORPHISM).

    public abstract String getSpecies();

    public abstract String getCareNote();

    public abstract String makeSound();

    public abstract String getSpeciesDetails();

    // Phase 2: used for the next checkup date.
    public abstract int getCheckupIntervalMonths();
}
