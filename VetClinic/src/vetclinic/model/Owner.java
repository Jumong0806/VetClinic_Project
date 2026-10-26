package vetclinic.model;

import vetclinic.util.ValidationUtil;

/**
 * Owner - the person who owns an animal.
 * Owner: JP
 *
 * OOP concepts: ENCAPSULATION (private fields + validation), INTERFACE (implements Describable,
 * even though Owner is NOT related to Animal or Staff).
 */
public class Owner implements Describable{

    private String ownerName;
    private String contactNumber;
    private String address;

    public Owner(String ownerName, String contactNumber, String address) {
        setOwnerName(ownerName);
        setContactNumber(contactNumber);
        setAddress(address);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        if (ValidationUtil.isBlank(ownerName)) {
            throw new IllegalArgumentException("Owner name must not be blank.");
        }
        this.ownerName = ownerName.trim();
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        if (!ValidationUtil.isValidContactNumber(contactNumber)) {
            throw new IllegalArgumentException("Owner contact number must be 11 digits and start with 09.");
        }
        this.contactNumber = contactNumber.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (ValidationUtil.isBlank(address)) {
            throw new IllegalArgumentException("Owner address must not be blank.");
        }
        this.address = address.trim();
    }

    @Override
    public String getSummary() {
        return ownerName + " (" + contactNumber + "), " + address;
    }
}
