package vetclinic.model;

import vetclinic.util.ValidationUtil;

/**
 * Cat - a cat patient.
 * Owner: Rishan
 */
public class Cat extends Animal {

    private boolean indoor;
    private String coatColor;

    public Cat(String animalId, String name, int age, double weightKg, Owner owner, boolean indoor, String coatColor) {
        super(animalId, name, age, weightKg, owner);
        setIndoor(indoor);
        setCoatColor(coatColor);
    }

    // Boolean getters start with "is" by our naming rules.
    public boolean isIndoor() {
        return indoor;
    }

    public void setIndoor(boolean indoor) {
        this.indoor = indoor;
    }

    public String getCoatColor() {
        return coatColor;
    }

    public void setCoatColor(String coatColor) {
        if (ValidationUtil.isBlank(coatColor)) {
            throw new IllegalArgumentException("Coat color must not be blank.");
        }
        this.coatColor = coatColor.trim();
    }

    @Override
    public String getSpecies() {
        return "Cat";
    }

    // Different behavior from Dog and Livestock (this is what polymorphism shows on screen).
    @Override
    public String getCareNote() {
        if (indoor) {
            return "Watch the weight and dental health. Keep the litter box clean.";
        }
        return "Check for fleas, ticks and injuries. Keep the vaccines up to date.";
    }

    @Override
    public int getCheckupIntervalMonths() {
        return 12;
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " | " + (indoor ? "Indoor" : "Outdoor") + " | Coat: " + coatColor;
    }
}
