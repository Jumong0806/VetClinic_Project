package vetclinic.model;

import vetclinic.util.ValidationUtil;

/**
 * Dog - a dog patient.
 * Owner: Richnie
 */
public class Dog extends Animal {

    private String breed;
    private boolean rabiesVaccinated;

    // super(...) runs the Animal constructor first, then we set the Dog-only fields.
    public Dog(String animalId, String name, int age, double weightKg, Owner owner, String breed, boolean rabiesVaccinated) {
        super(animalId, name, age, weightKg, owner);
        setBreed(breed);
        setRabiesVaccinated(rabiesVaccinated);
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        if (ValidationUtil.isBlank(breed)) {
            throw new IllegalArgumentException("Breed must not be blank.");
        }
        this.breed = breed.trim();
    }

    public boolean isRabiesVaccinated() {
        return rabiesVaccinated;
    }

    // A boolean can only be true or false, so there is nothing to validate here.
    public void setRabiesVaccinated(boolean rabiesVaccinated) {
        this.rabiesVaccinated = rabiesVaccinated;
    }

    // ---------------- overrides of the abstract methods ----------------

    @Override
    public String getSpecies() {
        return "Dog";
    }

    @Override
    public String getCareNote() {
        if (!rabiesVaccinated) {
            return "Rabies vaccine is missing. Schedule it as soon as possible. Daily walks needed.";
        }
        return "Daily walks, yearly vaccine booster and regular deworming.";
    }

    @Override
    public int getCheckupIntervalMonths() {
        return 12;
    }

    // Overrides getSummary() but reuses the parent's text first (super.getSummary()).
    @Override
    public String getSummary() {
        return super.getSummary() + " | Breed: " + breed;
    }
}
