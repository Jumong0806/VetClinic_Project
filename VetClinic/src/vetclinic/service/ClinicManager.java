package vetclinic.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vetclinic.exception.StorageException;
import vetclinic.model.Animal;
import vetclinic.model.Staff;
import vetclinic.model.Treatable;
import vetclinic.model.TreatmentRecord;
import vetclinic.model.VetAssistant;
import vetclinic.model.Veterinarian;
import vetclinic.storage.Storable;
import vetclinic.util.ValidationUtil;

/**
 * ClinicManager - the "brain" between the GUI and the data.
 * The GUI never touches the lists or the files directly: it always asks this class.
 *
 * OOP concepts: POLYMORPHISM (animals are handled as the base type Animal, staff as Staff, and the
 * storage as the Storable interface), ENCAPSULATION (the lists are private; we hand out COPIES).
 */
public class ClinicManager {

    private List<Animal> animals;
    private List<Staff> staffMembers;
    private Storable storage;

    public ClinicManager(Storable storage) {
        this.storage = storage;
        this.animals = new ArrayList<>();
        this.staffMembers = new ArrayList<>();
    }

    // ---------------- animals ----------------

    public void addAnimal(Animal animal) {
        if (animal == null) {
            throw new IllegalArgumentException("No animal to add.");
        }
        if (findAnimalById(animal.getAnimalId()) != null) {
            Animal.setTotalAnimals(animals.size());   // the rejected object raised the static counter, so reset it
            throw new IllegalArgumentException("An animal with ID " + animal.getAnimalId() + " already exists.");
        }
        animals.add(animal);
        Animal.setTotalAnimals(animals.size());
    }

    // Replaces the animal that has the same ID. The old history and registered date are kept.
    public void updateAnimal(Animal animal) {
        if (animal == null) {
            throw new IllegalArgumentException("No animal to update.");
        }
        for (int i = 0; i < animals.size(); i++) {
            if (animals.get(i).getAnimalId().equals(animal.getAnimalId())) {
                Animal old = animals.get(i);
                animal.setTreatmentHistory(old.getTreatmentHistory());
                animal.setRegisteredDate(old.getRegisteredDate());
                animals.set(i, animal);
                Animal.setTotalAnimals(animals.size());   // the new object raised the static counter
                return;
            }
        }
        Animal.setTotalAnimals(animals.size());       // the rejected object raised the static counter, so reset it
        throw new IllegalArgumentException("Animal " + animal.getAnimalId() + " was not found.");
    }

    public void removeAnimal(String animalId) {
        Animal animal = findAnimalById(animalId);
        if (animal == null) {
            throw new IllegalArgumentException("Animal " + animalId + " was not found.");
        }
        animals.remove(animal);
        Animal.setTotalAnimals(animals.size());
    }

    public Animal findAnimalById(String animalId) {
        for (Animal animal : animals) {
            if (animal.getAnimalId().equals(animalId)) {
                return animal;
            }
        }
        return null;
    }

    // Returns a COPY so the GUI cannot change our private list (encapsulation).
    public List<Animal> getAllAnimals() {
        return new ArrayList<>(animals);
    }

    // Matches the animal name, the owner name, or the animal ID (not case sensitive).
    public List<Animal> searchAnimals(String keyword) {
        if (ValidationUtil.isBlank(keyword)) {
            return getAllAnimals();
        }
        String text = keyword.trim().toLowerCase();
        List<Animal> result = new ArrayList<>();
        for (Animal animal : animals) {
            if (animal.getName().toLowerCase().contains(text)
                    || animal.getOwner().getOwnerName().toLowerCase().contains(text)
                    || animal.getAnimalId().toLowerCase().contains(text)) {
                result.add(animal);
            }
        }
        return result;
    }

    // Phase 2 (advanced filtering).
    public List<Animal> filterBySpecies(String species) {
        if (ValidationUtil.isBlank(species) || species.equals("All Species")) {
            return getAllAnimals();
        }
        List<Animal> result = new ArrayList<>();
        for (Animal animal : animals) {
            if (animal.getSpecies().equalsIgnoreCase(species)) {   // getSpecies() is POLYMORPHIC
                result.add(animal);
            }
        }
        return result;
    }

    // Counts only treatment follow-ups that are DUE TODAY or OVERDUE.
    // Upcoming follow-ups (date after today) are intentionally excluded.
    public List<TreatmentRecord> getDueTreatments() {
        List<TreatmentRecord> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Animal animal : animals) {
            for (TreatmentRecord record : animal.getTreatmentHistory()) {
                LocalDate followUp = record.getFollowUpDate();
                if (followUp != null && !followUp.isAfter(today)) {   // today or earlier
                    result.add(record);
                }
            }
        }
        return result;
    }

    // ---------------- staff ----------------

    // Validates ID uniqueness before adding a staff member.
    public void addStaff(Staff staff) {
        if (staff == null) throw new IllegalArgumentException("No staff member to add.");
        if (findStaffById(staff.getStaffId()) != null) {
            throw new IllegalArgumentException("A staff member with ID " + staff.getStaffId() + " already exists.");
        }
        staffMembers.add(staff);
    }

    // A staff member who has treatment records cannot be deleted, so old records never point to nobody.
    public void removeStaff(String staffId) {
        Staff staff = findStaffById(staffId);
        if (staff == null) {
            throw new IllegalArgumentException("Staff " + staffId + " was not found.");
        }
        for (Animal animal : animals) {
            for (TreatmentRecord record : animal.getTreatmentHistory()) {
                if (record.getPerformedBy().getStaffId().equals(staffId)) {
                    throw new IllegalArgumentException(staff.getStaffName()
                            + " has treatment records and cannot be deleted.");
                }
            }
        }
        staffMembers.remove(staff);
    }

    public Staff findStaffById(String staffId) {
        for (Staff staff : staffMembers) {
            if (staff.getStaffId().equals(staffId)) {
                return staff;
            }
        }
        return null;
    }

    public List<Staff> getAllStaff() {
        return new ArrayList<>(staffMembers);
    }

    // Matches the staff name, the staff ID, or the role (not case sensitive).
    public List<Staff> searchStaff(String keyword) {
        if (ValidationUtil.isBlank(keyword)) {
            return getAllStaff();
        }
        String text = keyword.trim().toLowerCase();
        List<Staff> result = new ArrayList<>();
        for (Staff staff : staffMembers) {
            if (staff.getStaffName().toLowerCase().contains(text)
                    || staff.getStaffId().toLowerCase().contains(text)
                    || staff.getRole().toLowerCase().contains(text)) {
                result.add(staff);
            }
        }
        return result;
    }


    // Seeds initial staff data.
    public void seedDefaultStaffIfEmpty() {
        if (!staffMembers.isEmpty()) return;
        staffMembers.add(new Veterinarian("S001", "Dr. Olivia Rodrigo", "09171234567", "VET-2026-001", "Small Animals"));
        staffMembers.add(new VetAssistant("S002", "Julia Montes", "09181234567", "Morning"));
    }

    // ---------------- treatments ----------------

    // Finds the animal and the staff, then asks the STAFF to do the treatment.
    // staff.treat(...) is POLYMORPHIC: a Veterinarian and a VetAssistant create different records.
    // The animal is passed as a Treatable (the INTERFACE), not as an Animal.
    public TreatmentRecord recordTreatment(String animalId, String staffId, LocalDate treatmentDate, String diagnosis,
                                           String treatment, String medication, LocalDate followUpDate) {
        Animal animal = findAnimalById(animalId);
        if (animal == null) {
            throw new IllegalArgumentException("Animal " + animalId + " was not found.");
        }
        Staff staff = findStaffById(staffId);
        if (staff == null) {
            throw new IllegalArgumentException("Staff " + staffId + " was not found.");
        }
        if (treatmentDate == null) {
            throw new IllegalArgumentException("Treatment date must not be empty.");
        }
        // Check the follow-up date BEFORE the record is created, so a bad date never leaves a half-saved record.
        if (followUpDate != null && followUpDate.isBefore(treatmentDate)) {
            throw new IllegalArgumentException("Follow-up date cannot be before the treatment date.");
        }
        Treatable patient = animal;
        TreatmentRecord record = staff.treat(patient, treatmentDate, diagnosis, treatment, medication);
        record.setFollowUpDate(followUpDate);
        return record;
    }

    // Gets all treatments across all animals, sorted newest first.
    public List<TreatmentRecord> getAllTreatments() {
        List<TreatmentRecord> all = new ArrayList<>();
        for (Animal animal : animals) {
            all.addAll(animal.getTreatmentHistory());
        }
        Collections.sort(all, (a, b) -> b.getTreatmentDate().compareTo(a.getTreatmentDate()));
        return all;
    }

    // Upgraded: Searches all visible columns including Patient Name, Medication, and Staff
    public List<TreatmentRecord> searchTreatments(String keyword) {
        if (ValidationUtil.isBlank(keyword)) {
            return getAllTreatments();
        }
        String text = keyword.trim().toLowerCase();
        List<TreatmentRecord> result = new ArrayList<>();
        for (TreatmentRecord record : getAllTreatments()) {
            Animal patient = findAnimalById(record.getAnimalId());
            String patientName = (patient != null) ? patient.getName().toLowerCase() : "";

            if (record.getRecordId().toLowerCase().contains(text)
                    || record.getAnimalId().toLowerCase().contains(text)
                    || patientName.contains(text)
                    || record.getDiagnosis().toLowerCase().contains(text)
                    || record.getTreatment().toLowerCase().contains(text)
                    || record.getMedication().toLowerCase().contains(text)
                    || record.getPerformedBy().getStaffName().toLowerCase().contains(text)) {
                result.add(record);
            }
        }
        return result;
    }

    // New: Filters treatment records based on the selected time frame
    public List<TreatmentRecord> filterTreatmentsByDate(String period) {
        if (ValidationUtil.isBlank(period) || period.equals("All Time")) {
            return getAllTreatments();
        }
        List<TreatmentRecord> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (TreatmentRecord record : getAllTreatments()) {
            LocalDate tDate = record.getTreatmentDate();
            if (period.equals("Today") && tDate.isEqual(today)) {
                result.add(record);
            } else if (period.equals("Last 7 Days") && !tDate.isBefore(today.minusDays(7)) && !tDate.isAfter(today)) {
                result.add(record);
            } else if (period.equals("Last 30 Days") && !tDate.isBefore(today.minusDays(30)) && !tDate.isAfter(today)) {
                result.add(record);
            }
        }
        return result;
    }

    // Updates an existing treatment record inside the animal's history.
    public void updateTreatment(TreatmentRecord updatedRecord) {
        if (updatedRecord == null) {
            throw new IllegalArgumentException("No treatment record to update.");
        }
        Animal animal = findAnimalById(updatedRecord.getAnimalId());
        if (animal == null) {
            throw new IllegalArgumentException("Animal " + updatedRecord.getAnimalId() + " was not found.");
        }
        List<TreatmentRecord> history = animal.getTreatmentHistory();
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).getRecordId().equals(updatedRecord.getRecordId())) {
                // The edit form builds a NEW object, so copy the original "added at" time into it.
                // Otherwise an edited record would jump to the top of Recent Treatments.
                updatedRecord.setCreatedAt(history.get(i).getCreatedAt());
                history.set(i, updatedRecord);
                animal.setTreatmentHistory(history); // Apply the changes
                return;
            }
        }
        throw new IllegalArgumentException("Treatment record " + updatedRecord.getRecordId() + " was not found.");
    }

    // Deletes a specific treatment record from the animal's history.
    public void removeTreatment(String recordId) {
        for (Animal animal : animals) {
            List<TreatmentRecord> history = animal.getTreatmentHistory();
            boolean removed = history.removeIf(r -> r.getRecordId().equals(recordId));
            if (removed) {
                animal.setTreatmentHistory(history);
                return;
            }
        }
        throw new IllegalArgumentException("Treatment record " + recordId + " was not found.");
    }

    // Dashboard card: counts the records of all animals.
    public int getTotalTreatments() {
        int total = 0;
        for (Animal animal : animals) {
            total += animal.getTreatmentHistory().size();
        }
        return total;
    }

    // Dashboard table: the newest records first, at most "limit" rows.
    public List<TreatmentRecord> getRecentTreatments(int limit) {
        List<TreatmentRecord> all = new ArrayList<>();
        for (Animal animal : animals) {
            all.addAll(animal.getTreatmentHistory());
        }
        // Newest ADDED first. getCreatedAt() is the exact moment the record was added, so it does not matter
        // which patient it belongs to or what its ID is. (b compared to a gives the descending order.)
        // If two records have the very same time, the one with the later visit date goes first.
        Collections.sort(all, (a, b) -> {
            int byAdded = b.getCreatedAt().compareTo(a.getCreatedAt());
            return (byAdded != 0) ? byAdded : b.getTreatmentDate().compareTo(a.getTreatmentDate());
        });
        if (all.size() > limit) {
            return new ArrayList<>(all.subList(0, limit));
        }
        return all;
    }

    // ---------------- IDs ----------------

    // Finds the highest number used so far and adds 1, for example A003 -> A004.
    public String generateAnimalId() {
        int max = 0;
        for (Animal animal : animals) {
            max = Math.max(max, numberPartOf(animal.getAnimalId()));
        }
        return String.format("A%03d", max + 1);
    }

    public String generateStaffId() {
        int max = 0;
        for (Staff staff : staffMembers) {
            max = Math.max(max, numberPartOf(staff.getStaffId()));
        }
        return String.format("S%03d", max + 1);
    }

    // ---------------- saving and loading ----------------

    // Staff are saved first because the treatment records point to them.
    public void saveAll() throws StorageException {
        storage.saveStaff(staffMembers);
        storage.saveAnimals(animals);
    }

    // Called once at startup.
    public void loadAll() throws StorageException {
        List<Animal> loadedAnimals = storage.loadAnimals();
        List<Staff> loadedStaff = storage.loadStaff();
        animals = loadedAnimals;
        staffMembers = loadedStaff;
        Animal.setTotalAnimals(animals.size());
        relinkTreatmentStaff();
    }

    // ---------------- Phase 2 ----------------

    // Patients by Species chart.
    public Map<String, Integer> getSpeciesCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("Dog", 0);
        counts.put("Cat", 0);
        counts.put("Livestock", 0);
        for (Animal animal : animals) {
            String species = animal.getSpecies();
            counts.put(species, counts.get(species) + 1);
        }
        return counts;
    }

    // Recent Patients table: newest registered date first.
    public List<Animal> getRecentAnimals(int limit) {
        List<Animal> sorted = new ArrayList<>(animals);
        // Newest registered first. Two patients registered on the same day are ordered by their ID number:
        // IDs are generated in increasing order (A001, A002, ...), so the higher number was added later.
        Collections.sort(sorted, (a, b) -> {
            int byDate = b.getRegisteredDate().compareTo(a.getRegisteredDate());
            return (byDate != 0) ? byDate : numberPartOf(b.getAnimalId()) - numberPartOf(a.getAnimalId());
        });
        if (sorted.size() > limit) {
            return new ArrayList<>(sorted.subList(0, limit));
        }
        return sorted;
    }

    // "All Roles" dropdown.
    public List<Staff> filterStaffByRole(String role) {
        if (ValidationUtil.isBlank(role) || role.equals("All Roles")) {
            return getAllStaff();
        }
        List<Staff> result = new ArrayList<>();
        for (Staff staff : staffMembers) {
            if (staff.getRole().equalsIgnoreCase(role)) {   // getRole() is POLYMORPHIC
                result.add(staff);
            }
        }
        return result;
    }

    // Notifications: checkups that fall between today and today + withinDays.
    public List<Animal> getUpcomingCheckups(int withinDays) {
        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.plusDays(withinDays);
        List<Animal> result = new ArrayList<>();
        for (Animal animal : animals) {
            LocalDate next = animal.getNextCheckupDate();
            if (next.isAfter(today) && !next.isAfter(lastDay)) {
                result.add(animal);
            }
        }
        return result;
    }

    // Notifications: pulls treatment records with follow-up dates today, overdue, or within the next 7 days
    public List<TreatmentRecord> getUpcomingFollowUps() {
        List<TreatmentRecord> upcomingFollowUps = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate oneWeekFromNow = today.plusDays(7);

        for (Animal animal : animals) {
            for (TreatmentRecord record : animal.getTreatmentHistory()) {
                LocalDate followUp = record.getFollowUpDate();
                if (followUp != null) {
                    // Check if the follow-up is today, overdue, or within the next 7 days
                    if (!followUp.isAfter(oneWeekFromNow)) {
                        upcomingFollowUps.add(record);
                    }
                }
            }
        }
        return upcomingFollowUps;
    }

    // Marks a follow-up as complete by clearing the follow-up date and saving to storage
    public void resolveFollowUp(String recordId) {
        for (Animal animal : animals) {
            for (TreatmentRecord record : animal.getTreatmentHistory()) {
                if (record.getRecordId().equals(recordId)) {
                    record.setFollowUpDate(null); // Clear the date
                    try {
                        saveAll(); // Immediately save changes to MySQL or File Storage
                    } catch (StorageException e) {
                        System.err.println("Failed to save resolution: " + e.getMessage());
                    }
                    return;
                }
            }
        }
    }

    // ---------------- private helpers (not in the Excel, see DEVIATIONS) ----------------

    // "A004" -> 4. Returns 0 if the ID has no number.
    private int numberPartOf(String id) {
        try {
            return Integer.parseInt(id.substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // After loading, each saved record holds its OWN copy of the staff. This makes every record
    // point to the loaded staff object with the same ID, so a renamed staff shows the new name.
    private void relinkTreatmentStaff() {
        for (Animal animal : animals) {
            for (TreatmentRecord record : animal.getTreatmentHistory()) {
                Staff current = findStaffById(record.getPerformedBy().getStaffId());
                if (current != null) {
                    record.setPerformedBy(current);
                }
            }
        }
    }
}