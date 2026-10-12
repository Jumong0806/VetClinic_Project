package vetclinic.storage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vetclinic.exception.StorageException;
import vetclinic.model.Animal;
import vetclinic.model.Cat;
import vetclinic.model.Dog;
import vetclinic.model.Livestock;
import vetclinic.model.Owner;
import vetclinic.model.Staff;
import vetclinic.model.TreatmentRecord;
import vetclinic.model.VetAssistant;
import vetclinic.model.Veterinarian;

/**
 * MySqlStorage - BONUS (+5%): stores the same data in a real MySQL database using JDBC.
 * It uses PreparedStatement (safe against SQL injection) and try-with-resources (connections are always closed).
 * Owner: All
 *
 * OOP concepts: INTERFACE (implements Storable) and POLYMORPHISM: Main can plug this class in
 * instead of FileStorage and the rest of the program does not change.
 * The "species" column of the animals table tells us which subclass (Dog, Cat, Livestock) to build.
 * The "role" column of the staff table tells us whether to build a Veterinarian or a VetAssistant.
 */
public class MySqlStorage implements Storable {

    // CHANGE THESE THREE to match your MySQL installation (see the connection guide).
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vetclinic_db_2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Jpjumong0806*";   // never commit your real password to GitHub

    // ---------------- animals ----------------

    // Saves the whole list: updates animals that exist, inserts new ones, deletes the ones that were removed.
    @Override
    public void saveAnimals(List<Animal> animals) throws StorageException {
        List<String> idsInDatabase = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT animal_id FROM animals");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                idsInDatabase.add(rs.getString("animal_id"));
            }
        } catch (SQLException e) {
            throw new StorageException("Could not read the animal IDs from MySQL: " + e.getMessage(), e);
        }

        List<String> idsInProgram = new ArrayList<>();
        for (Animal animal : animals) {
            idsInProgram.add(animal.getAnimalId());
            if (idsInDatabase.contains(animal.getAnimalId())) {
                updateAnimal(animal);
            } else {
                insertAnimal(animal);
            }
        }
        for (String id : idsInDatabase) {
            if (!idsInProgram.contains(id)) {
                deleteAnimal(id);
            }
        }
    }

    // Reads every animal and its treatment records, and builds Dog, Cat or Livestock objects.
    @Override
    public List<Animal> loadAnimals() throws StorageException {
        // The records point to staff, so we load the staff first and look them up by ID.
        Map<String, Staff> staffById = new HashMap<>();
        for (Staff staff : loadStaff()) {
            staffById.put(staff.getStaffId(), staff);
        }

        List<Animal> animals = new ArrayList<>();
        String sql = "SELECT * FROM animals ORDER BY animal_id";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Owner owner = new Owner(rs.getString("owner_name"), rs.getString("owner_contact"), rs.getString("owner_address"));
                String species = rs.getString("species");
                Animal animal;
                // The species column decides which subclass we create (INHERITANCE).
                if (species.equals("Dog")) {
                    animal = new Dog(rs.getString("animal_id"), rs.getString("name"), rs.getInt("age"), rs.getDouble("weight_kg"),
                            owner, rs.getString("breed"), rs.getBoolean("rabies_vaccinated"));
                } else if (species.equals("Cat")) {
                    animal = new Cat(rs.getString("animal_id"), rs.getString("name"), rs.getInt("age"), rs.getDouble("weight_kg"),
                            owner, rs.getBoolean("indoor"), rs.getString("coat_color"));
                } else {
                    animal = new Livestock(rs.getString("animal_id"), rs.getString("name"), rs.getInt("age"), rs.getDouble("weight_kg"),
                            owner, rs.getString("tag_number"), rs.getString("livestock_type"));
                }
                animal.setRegisteredDate(rs.getDate("registered_date").toLocalDate());
                animals.add(animal);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not load the animals from MySQL: " + e.getMessage(), e);
        }

        // Second step: load the treatment records of each animal.
        for (Animal animal : animals) {
            List<TreatmentRecord> records = new ArrayList<>();
            String sql2 = "SELECT * FROM treatment_records WHERE animal_id = ? ORDER BY treatment_date, record_id";
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql2)) {
                ps.setString(1, animal.getAnimalId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Staff performedBy = staffById.get(rs.getString("performed_by_staff_id"));
                        Date followUp = rs.getDate("follow_up_date");
                        records.add(new TreatmentRecord(rs.getString("record_id"), animal.getAnimalId(),
                                rs.getDate("treatment_date").toLocalDate(), rs.getString("diagnosis"),
                                rs.getString("treatment"), rs.getString("medication"),
                                followUp == null ? null : followUp.toLocalDate(), performedBy));
                    }
                }
            } catch (SQLException e) {
                throw new StorageException("Could not load the treatment records from MySQL: " + e.getMessage(), e);
            }
            animal.setTreatmentHistory(records);
        }
        return animals;
    }

    // ---------------- staff ----------------

    // Saves every staff member: updates the existing rows, inserts the new ones, deletes the removed ones.
    @Override
    public void saveStaff(List<Staff> staffMembers) throws StorageException {
        List<String> idsInDatabase = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT staff_id FROM staff");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                idsInDatabase.add(rs.getString("staff_id"));
            }
        } catch (SQLException e) {
            throw new StorageException("Could not read the staff IDs from MySQL: " + e.getMessage(), e);
        }

        List<String> idsInProgram = new ArrayList<>();
        // "REPLACE-style" upsert: one statement works for both new and existing rows.
        String sql = "INSERT INTO staff (staff_id, staff_name, contact_number, username, password_hash, role, license_number, specialization, shift) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE staff_name = VALUES(staff_name), contact_number = VALUES(contact_number), "
                + "username = VALUES(username), password_hash = VALUES(password_hash), role = VALUES(role), "
                + "license_number = VALUES(license_number), specialization = VALUES(specialization), shift = VALUES(shift)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Staff staff : staffMembers) {
                idsInProgram.add(staff.getStaffId());
                ps.setString(1, staff.getStaffId());
                ps.setString(2, staff.getStaffName());
                ps.setString(3, staff.getContactNumber());
                ps.setString(4, staff.getUsername());
                ps.setString(5, staff.getPasswordHash());
                ps.setString(6, staff.getRole());
                // instanceof is only used here to read fields that exist in one subclass.
                if (staff instanceof Veterinarian) {
                    Veterinarian vet = (Veterinarian) staff;
                    ps.setString(7, vet.getLicenseNumber());
                    ps.setString(8, vet.getSpecialization());
                    ps.setNull(9, java.sql.Types.VARCHAR);
                } else {
                    VetAssistant assistant = (VetAssistant) staff;
                    ps.setNull(7, java.sql.Types.VARCHAR);
                    ps.setNull(8, java.sql.Types.VARCHAR);
                    ps.setString(9, assistant.getShift());
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new StorageException("Could not save the staff to MySQL: " + e.getMessage(), e);
        }

        for (String id : idsInDatabase) {
            if (!idsInProgram.contains(id)) {
                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM staff WHERE staff_id = ?")) {
                    ps.setString(1, id);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new StorageException("Could not delete staff " + id + " from MySQL: " + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<Staff> loadStaff() throws StorageException {
        List<Staff> staffMembers = new ArrayList<>();
        String sql = "SELECT * FROM staff ORDER BY staff_id";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Staff staff;
                // The role column decides which subclass we create.
                // The constructor needs a plain password, so we give a temporary one and then
                // put the stored hash back with setPasswordHash().
                if (rs.getString("role").equals("Veterinarian")) {
                    staff = new Veterinarian(rs.getString("staff_id"), rs.getString("staff_name"), rs.getString("contact_number"),
                            rs.getString("username"), "temporary1", rs.getString("license_number"), rs.getString("specialization"));
                } else {
                    staff = new VetAssistant(rs.getString("staff_id"), rs.getString("staff_name"), rs.getString("contact_number"),
                            rs.getString("username"), "temporary1", rs.getString("shift"));
                }
                staff.setPasswordHash(rs.getString("password_hash"));
                staffMembers.add(staff);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not load the staff from MySQL: " + e.getMessage(), e);
        }
        return staffMembers;
    }

    // ---------------- helpers ----------------

    // Opens a new connection. The caller closes it with try-with-resources.
    private Connection getConnection() throws StorageException {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            throw new StorageException("Could not connect to MySQL (" + DB_URL + "): " + e.getMessage()
                    + ". Check that MySQL is running, the Connector/J library is added, and DB_USER/DB_PASSWORD are correct.", e);
        }
    }

    // Inserts one animal and all of its treatment records in ONE transaction.
    private void insertAnimal(Animal animal) throws StorageException {
        String sql = "INSERT INTO animals (animal_id, species, name, age, weight_kg, owner_name, owner_contact, owner_address, "
                + "registered_date, breed, rabies_vaccinated, indoor, coat_color, tag_number, livestock_type) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);   // everything below is saved together or not at all
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                fillAnimalStatement(ps, animal, false);
                ps.executeUpdate();
                insertTreatmentRecords(con, animal);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new StorageException("Could not insert animal " + animal.getAnimalId() + ": " + e.getMessage(), e);
        }
    }

    // Updates the animal row, then replaces its treatment records, in ONE transaction.
    private void updateAnimal(Animal animal) throws StorageException {
        String sql = "UPDATE animals SET species = ?, name = ?, age = ?, weight_kg = ?, owner_name = ?, owner_contact = ?, "
                + "owner_address = ?, registered_date = ?, breed = ?, rabies_vaccinated = ?, indoor = ?, coat_color = ?, "
                + "tag_number = ?, livestock_type = ? WHERE animal_id = ?";
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql);
                 PreparedStatement deleteRecords = con.prepareStatement("DELETE FROM treatment_records WHERE animal_id = ?")) {
                fillAnimalStatement(ps, animal, true);
                ps.executeUpdate();
                deleteRecords.setString(1, animal.getAnimalId());
                deleteRecords.executeUpdate();
                insertTreatmentRecords(con, animal);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new StorageException("Could not update animal " + animal.getAnimalId() + ": " + e.getMessage(), e);
        }
    }

    // Deletes the animal. Its treatment records are deleted by ON DELETE CASCADE in schema.sql.
    private void deleteAnimal(String animalId) throws StorageException {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM animals WHERE animal_id = ?")) {
            ps.setString(1, animalId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Could not delete animal " + animalId + ": " + e.getMessage(), e);
        }
    }

    // Fills the "?" placeholders of the insert or update statement. For an UPDATE the ID goes last.
    private void fillAnimalStatement(PreparedStatement ps, Animal animal, boolean forUpdate) throws SQLException {
        int i = 1;
        if (!forUpdate) {
            ps.setString(i++, animal.getAnimalId());
        }
        ps.setString(i++, animal.getSpecies());
        ps.setString(i++, animal.getName());
        ps.setInt(i++, animal.getAge());
        ps.setDouble(i++, animal.getWeightKg());
        ps.setString(i++, animal.getOwner().getOwnerName());
        ps.setString(i++, animal.getOwner().getContactNumber());
        ps.setString(i++, animal.getOwner().getAddress());
        ps.setDate(i++, Date.valueOf(animal.getRegisteredDate()));
        // Species-specific columns: only the one that belongs to this subclass is filled, the others are NULL.
        String breed = null;
        Boolean rabies = null;
        Boolean indoor = null;
        String coatColor = null;
        String tagNumber = null;
        String livestockType = null;
        if (animal instanceof Dog) {
            Dog dog = (Dog) animal;
            breed = dog.getBreed();
            rabies = dog.isRabiesVaccinated();
        } else if (animal instanceof Cat) {
            Cat cat = (Cat) animal;
            indoor = cat.isIndoor();
            coatColor = cat.getCoatColor();
        } else if (animal instanceof Livestock) {
            Livestock livestock = (Livestock) animal;
            tagNumber = livestock.getTagNumber();
            livestockType = livestock.getLivestockType();
        }
        ps.setString(i++, breed);
        if (rabies == null) { ps.setNull(i++, java.sql.Types.BOOLEAN); } else { ps.setBoolean(i++, rabies); }
        if (indoor == null) { ps.setNull(i++, java.sql.Types.BOOLEAN); } else { ps.setBoolean(i++, indoor); }
        ps.setString(i++, coatColor);
        ps.setString(i++, tagNumber);
        ps.setString(i++, livestockType);
        if (forUpdate) {
            ps.setString(i++, animal.getAnimalId());   // the WHERE animal_id = ? part
        }
    }

    // Inserts all treatment records of one animal using the SAME connection (same transaction).
    private void insertTreatmentRecords(Connection con, Animal animal) throws SQLException {
        String sql = "INSERT INTO treatment_records (record_id, animal_id, treatment_date, diagnosis, treatment, medication, "
                + "follow_up_date, performed_by_staff_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (TreatmentRecord record : animal.getTreatmentHistory()) {
                ps.setString(1, record.getRecordId());
                ps.setString(2, animal.getAnimalId());
                ps.setDate(3, Date.valueOf(record.getTreatmentDate()));
                ps.setString(4, record.getDiagnosis());
                ps.setString(5, record.getTreatment());
                ps.setString(6, record.getMedication());
                if (record.getFollowUpDate() == null) {
                    ps.setNull(7, java.sql.Types.DATE);
                } else {
                    ps.setDate(7, Date.valueOf(record.getFollowUpDate()));
                }
                ps.setString(8, record.getPerformedBy().getStaffId());
                ps.executeUpdate();
            }
        }
    }
}
