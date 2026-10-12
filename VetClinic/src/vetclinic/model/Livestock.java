package vetclinic.model;

import vetclinic.util.ValidationUtil;

/**
 * Livestock - a farm animal patient (cow, goat, pig, ...).
 * Owner: Jermil
 *
 * OOP concepts: INHERITANCE, POLYMORPHISM (the sound depends on the livestock type), ENCAPSULATION.
 */
public class Livestock extends Animal {

    private static final long serialVersionUID = 1L;

    private String tagNumber;
    private String livestockType;   // Cow, Goat, Pig... decides the sound

    public Livestock(String animalId, String name, int age, double weightKg, Owner owner, String tagNumber, String livestockType) {
        super(animalId, name, age, weightKg, owner);
        setTagNumber(tagNumber);
        setLivestockType(livestockType);
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(String tagNumber) {
        if (ValidationUtil.isBlank(tagNumber)) {
            throw new IllegalArgumentException("Tag number must not be blank.");
        }
        this.tagNumber = tagNumber.trim();
    }

    public String getLivestockType() {
        return livestockType;
    }

    public void setLivestockType(String livestockType) {
        if (ValidationUtil.isBlank(livestockType)) {
            throw new IllegalArgumentException("Livestock type must not be blank.");
        }
        this.livestockType = livestockType.trim();
    }

    @Override
    public String getSpecies() {
        return "Livestock";
    }

    @Override
    public String getCareNote() {
        return "Herd check and deworming every 6 months. Check the hooves and the tag number.";
    }

    // The sound is chosen from the livestock type.
    @Override
    public String makeSound() {
        String type = livestockType.toLowerCase();
        if (type.equals("cow")) {
            return "Moo!";
        } else if (type.equals("goat") || type.equals("sheep")) {
            return "Baa!";
        } else if (type.equals("pig")) {
            return "Oink!";
        } else if (type.equals("horse")) {
            return "Neigh!";
        }
        return "Grunt!";
    }

    @Override
    public String getSpeciesDetails() {
        return "Tag: " + tagNumber + " | Type: " + livestockType;
    }

    @Override
    public int getCheckupIntervalMonths() {
        return 6;
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " | Tag: " + tagNumber + " | Type: " + livestockType;
    }
}
