package vetclinic.storage;

import java.util.List;

import vetclinic.exception.StorageException;
import vetclinic.model.Animal;
import vetclinic.model.Staff;

/**
 * Storable - the storage contract. ClinicManager only knows THIS interface, so we can switch
 * between FileStorage and MySqlStorage without changing the GUI or the model.
 * Owner: Gab
 *
 * OOP concepts: INTERFACE, ABSTRACTION, POLYMORPHISM (any Storable can be plugged in).
 */
public interface Storable {

    void saveAnimals(List<Animal> animals) throws StorageException;

    List<Animal> loadAnimals() throws StorageException;

    void saveStaff(List<Staff> staffMembers) throws StorageException;

    List<Staff> loadStaff() throws StorageException;
}
