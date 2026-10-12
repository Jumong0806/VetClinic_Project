package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import vetclinic.exception.StorageException;
import vetclinic.model.Animal;
import vetclinic.model.Cat;
import vetclinic.model.Dog;
import vetclinic.model.Livestock;
import vetclinic.model.Owner;
import vetclinic.service.ClinicManager;

/**
 * AnimalFormDialog - the pop-up form to ADD a new patient or UPDATE an existing one.
 * Owner: Ben
 *
 * Swing concepts: modal JDialog, GridBagLayout (form), CardLayout (species fields), FlowLayout (buttons).
 * OOP concepts: INHERITANCE and POLYMORPHISM. The form creates a Dog, a Cat or a Livestock object,
 * but the rest of the program only sees it as an Animal.
 */
public class AnimalFormDialog extends JDialog {

    private ClinicManager manager;
    private Animal animal;                       // null = we are adding a new patient
    private JComboBox<String> cmbSpecies;
    private JTextField txtName;
    private JTextField txtAge;
    private JTextField txtWeight;
    private JTextField txtOwnerName;
    private JTextField txtOwnerContact;
    private JTextField txtOwnerAddress;
    private JPanel pnlSpeciesFields;             // CardLayout: one card per species
    private JTextField txtBreed;
    private JCheckBox chkRabiesVaccinated;
    private JCheckBox chkIndoor;
    private JTextField txtCoatColor;
    private JTextField txtTagNumber;
    private JComboBox<String> cmbLivestockType;
    private JButton btnSave;
    private JButton btnClear;
    private JButton btnCancel;

    public AnimalFormDialog(JFrame parent, ClinicManager manager, Animal animal) {
        super(parent, "Patient", true);   // true = modal (locks the main window)
        this.manager = manager;
        this.animal = animal;

        buildForm();
        if (animal == null) {
            setTitle("Add Patient");
            clearForm();
        } else {
            setTitle("Update Patient " + animal.getAnimalId());
            fillFormFromAnimal(animal);
        }
        pack();
        setLocationRelativeTo(parent);
    }

    // ---------------- building the form ----------------

    private void buildForm() {
        setLayout(new BorderLayout(8, 8));

        cmbSpecies = new JComboBox<>(new String[]{"Dog", "Cat", "Livestock"});
        txtName = new JTextField(18);
        txtAge = new JTextField(18);
        txtWeight = new JTextField(18);
        txtOwnerName = new JTextField(18);
        txtOwnerContact = new JTextField(18);
        txtOwnerAddress = new JTextField(18);

        // Species-specific fields. Each species has its own small panel (a "card").
        txtBreed = new JTextField(14);
        chkRabiesVaccinated = new JCheckBox("Rabies vaccinated");
        chkIndoor = new JCheckBox("Indoor cat");
        txtCoatColor = new JTextField(14);
        txtTagNumber = new JTextField(14);
        cmbLivestockType = new JComboBox<>(new String[]{"Cow", "Goat", "Pig", "Sheep", "Horse"});

        JPanel pnlDog = new JPanel(new GridLayout(2, 2, 6, 6));
        pnlDog.add(new JLabel("Breed"));
        pnlDog.add(txtBreed);
        pnlDog.add(new JLabel(""));
        pnlDog.add(chkRabiesVaccinated);

        JPanel pnlCat = new JPanel(new GridLayout(2, 2, 6, 6));
        pnlCat.add(new JLabel(""));
        pnlCat.add(chkIndoor);
        pnlCat.add(new JLabel("Coat color"));
        pnlCat.add(txtCoatColor);

        JPanel pnlLivestock = new JPanel(new GridLayout(2, 2, 6, 6));
        pnlLivestock.add(new JLabel("Tag number"));
        pnlLivestock.add(txtTagNumber);
        pnlLivestock.add(new JLabel("Livestock type"));
        pnlLivestock.add(cmbLivestockType);

        pnlSpeciesFields = new JPanel(new CardLayout());
        pnlSpeciesFields.setBorder(BorderFactory.createTitledBorder("Species details"));
        pnlSpeciesFields.add(pnlDog, "Dog");
        pnlSpeciesFields.add(pnlCat, "Cat");
        pnlSpeciesFields.add(pnlLivestock, "Livestock");

        // Main form: label on the left, box on the right (GridBagLayout).
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        addRow(pnlForm, 0, "Species", cmbSpecies);
        addRow(pnlForm, 1, "Name", txtName);
        addRow(pnlForm, 2, "Age (years)", txtAge);
        addRow(pnlForm, 3, "Weight (kg)", txtWeight);
        addRow(pnlForm, 4, "Owner name", txtOwnerName);
        addRow(pnlForm, 5, "Owner contact (09...)", txtOwnerContact);
        addRow(pnlForm, 6, "Owner address", txtOwnerAddress);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 4, 4, 4);
        pnlForm.add(pnlSpeciesFields, gbc);

        btnSave = new JButton("Save");
        btnClear = new JButton("Clear");
        btnCancel = new JButton("Cancel");
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlButtons.add(btnSave);
        pnlButtons.add(btnClear);
        pnlButtons.add(btnCancel);

        // Events.
        cmbSpecies.addActionListener(e -> handleSpeciesChanged());
        btnSave.addActionListener(e -> handleSave());
        btnClear.addActionListener(e -> handleClear());
        btnCancel.addActionListener(e -> handleCancel());
        getRootPane().setDefaultButton(btnSave);

        add(pnlForm, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    // Puts the form values of an existing animal into the boxes (update mode).
    private void fillFormFromAnimal(Animal animal) {
        cmbSpecies.setSelectedItem(animal.getSpecies());
        cmbSpecies.setEnabled(false);   // the species of an existing patient cannot change
        txtName.setText(animal.getName());
        txtAge.setText(String.valueOf(animal.getAge()));
        txtWeight.setText(String.valueOf(animal.getWeightKg()));
        txtOwnerName.setText(animal.getOwner().getOwnerName());
        txtOwnerContact.setText(animal.getOwner().getContactNumber());
        txtOwnerAddress.setText(animal.getOwner().getAddress());

        // instanceof is only used here to read fields that exist in ONE subclass.
        if (animal instanceof Dog) {
            Dog dog = (Dog) animal;
            txtBreed.setText(dog.getBreed());
            chkRabiesVaccinated.setSelected(dog.isRabiesVaccinated());
        } else if (animal instanceof Cat) {
            Cat cat = (Cat) animal;
            chkIndoor.setSelected(cat.isIndoor());
            txtCoatColor.setText(cat.getCoatColor());
        } else if (animal instanceof Livestock) {
            Livestock livestock = (Livestock) animal;
            txtTagNumber.setText(livestock.getTagNumber());
            cmbLivestockType.setSelectedItem(livestock.getLivestockType());
        }
        handleSpeciesChanged();
    }

    // Reads the form and creates a Dog, Cat or Livestock (INHERITANCE).
    // Throws IllegalArgumentException with a clear message if something is wrong.
    private Animal createAnimalFromForm() {
        String species = (String) cmbSpecies.getSelectedItem();
        String id = (animal == null) ? manager.generateAnimalId() : animal.getAnimalId();

        int age;
        try {
            age = Integer.parseInt(txtAge.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Age must be a whole number, for example 3.");
        }
        double weight = 0;   // an empty weight box means "not weighed yet"
        if (!txtWeight.getText().trim().isEmpty()) {
            try {
                weight = Double.parseDouble(txtWeight.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Weight must be a number, for example 12.5.");
            }
        }
        Owner owner = new Owner(txtOwnerName.getText(), txtOwnerContact.getText(), txtOwnerAddress.getText());

        // The subclass is chosen by the species. The setters inside the constructors validate everything.
        if (species.equals("Dog")) {
            return new Dog(id, txtName.getText(), age, weight, owner, txtBreed.getText(), chkRabiesVaccinated.isSelected());
        } else if (species.equals("Cat")) {
            return new Cat(id, txtName.getText(), age, weight, owner, chkIndoor.isSelected(), txtCoatColor.getText());
        } else {
            return new Livestock(id, txtName.getText(), age, weight, owner, txtTagNumber.getText(),
                    (String) cmbLivestockType.getSelectedItem());
        }
    }

    // Empties every box.
    private void clearForm() {
        txtName.setText("");
        txtAge.setText("");
        txtWeight.setText("");
        txtOwnerName.setText("");
        txtOwnerContact.setText("");
        txtOwnerAddress.setText("");
        txtBreed.setText("");
        chkRabiesVaccinated.setSelected(false);
        chkIndoor.setSelected(false);
        txtCoatColor.setText("");
        txtTagNumber.setText("");
        cmbLivestockType.setSelectedIndex(0);
        handleSpeciesChanged();
    }

    // ---------------- events ----------------

    // Shows the card of the chosen species (CardLayout).
    private void handleSpeciesChanged() {
        ((CardLayout) pnlSpeciesFields.getLayout()).show(pnlSpeciesFields, (String) cmbSpecies.getSelectedItem());
    }

    // Save: create the animal, add or update it, save to storage, close.
    private void handleSave() {
        try {
            Animal newAnimal = createAnimalFromForm();
            if (animal == null) {
                manager.addAnimal(newAnimal);
            } else {
                manager.updateAnimal(newAnimal);
            }
        } catch (IllegalArgumentException e) {
            // A setter rejected a value: show the message and keep the form open.
            // The half-built animal may have raised the static counter, so we put it back to the real number.
            Animal.setTotalAnimals(manager.getAllAnimals().size());
            JOptionPane.showMessageDialog(this, e.getMessage(), "Please check the form", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            manager.saveAll();
        } catch (StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
        }
        dispose();
    }

    private void handleClear() {
        clearForm();
    }

    private void handleCancel() {
        dispose();
    }

    // ---------------- helper (not in the Excel, see DEVIATIONS) ----------------

    // Adds "label + box" as one row of the GridBagLayout.
    private void addRow(JPanel panel, int row, String label, JComponent field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }
}
