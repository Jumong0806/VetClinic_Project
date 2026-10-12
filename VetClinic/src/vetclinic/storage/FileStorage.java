package vetclinic.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import vetclinic.exception.StorageException;
import vetclinic.model.Animal;
import vetclinic.model.Staff;

/**
 * FileStorage - saves and loads the records with Java SERIALIZATION (REQUIRED persistence, 20% of the grade).
 * The animals (with their treatment history) go to data/animals.dat and the staff accounts to data/staff.dat.
 * Owner: Richnie
 *
 * OOP concepts: INTERFACE (implements Storable), POLYMORPHISM (ClinicManager can use it or MySqlStorage),
 * EXCEPTIONS (low-level IOException is wrapped in our StorageException).
 */
public class FileStorage implements Storable {

    private static final String ANIMAL_FILE = "data/animals.dat";
    private static final String STAFF_FILE = "data/staff.dat";

    @Override
    public void saveAnimals(List<Animal> animals) throws StorageException {
        createDataFolderIfMissing();
        // try-with-resources closes the file automatically, even if an error happens.
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ANIMAL_FILE))) {
            out.writeObject(new ArrayList<>(animals));   // the whole list is written in one go
        } catch (IOException e) {
            throw new StorageException("Could not save the animals to " + ANIMAL_FILE + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Animal> loadAnimals() throws StorageException {
        File file = new File(ANIMAL_FILE);
        if (!file.exists()) {
            return new ArrayList<>();   // first run: nothing saved yet
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            List<Animal> animals = (List<Animal>) in.readObject();
            return animals;
        } catch (IOException | ClassNotFoundException e) {
            throw new StorageException("Could not load the animals from " + ANIMAL_FILE + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void saveStaff(List<Staff> staffMembers) throws StorageException {
        createDataFolderIfMissing();
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(STAFF_FILE))) {
            out.writeObject(new ArrayList<>(staffMembers));
        } catch (IOException e) {
            throw new StorageException("Could not save the staff to " + STAFF_FILE + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Staff> loadStaff() throws StorageException {
        File file = new File(STAFF_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            List<Staff> staffMembers = (List<Staff>) in.readObject();
            return staffMembers;
        } catch (IOException | ClassNotFoundException e) {
            throw new StorageException("Could not load the staff from " + STAFF_FILE + ": " + e.getMessage(), e);
        }
    }

    // Makes the "data" folder if it does not exist yet.
    private void createDataFolderIfMissing() {
        File folder = new File(ANIMAL_FILE).getParentFile();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
    }
}
