package vetclinic.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import vetclinic.exception.StorageException;
import vetclinic.model.Animal;
import vetclinic.model.Cat;
import vetclinic.model.Dog;
import vetclinic.model.Livestock;
import vetclinic.model.Owner;
import vetclinic.service.ClinicManager;

public class AnimalFormDialog extends JDialog {

    private ClinicManager manager;
    private Animal animal; // null = adding new patient

    // UI Components
    private JComboBox<String> cmbSpecies;
    private JTextField txtName;
    private JTextField txtAge;
    private JTextField txtWeight;
    private JTextField txtOwnerName;
    private JTextField txtOwnerContact;
    private JTextField txtOwnerAddress;

    private JPanel pnlSpeciesFields;
    private JTextField txtBreed;
    private JCheckBox chkRabiesVaccinated;
    private JCheckBox chkIndoor;
    private JTextField txtCoatColor;
    private JTextField txtTagNumber;
    private JComboBox<String> cmbLivestockType;

    // For dragging the undecorated window
    private int pX, pY;

    // Theme Constants
    private final Color BORDER_COLOR = new Color(200, 204, 212);
    private final Color BG_COLOR = Color.WHITE;
    private final Color FIELD_BG = new Color(248, 249, 250);
    private final Color PRIMARY_BLUE = new Color(66, 103, 178);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);

    public AnimalFormDialog(JFrame parent, ClinicManager manager, Animal animal) {
        super(parent, "", true);
        this.manager = manager;
        this.animal = animal;

        setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new LineBorder(BORDER_COLOR, 1));

        mainPanel.add(createCustomHeader(), BorderLayout.NORTH);
        mainPanel.add(createFormBody(), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(mainPanel);

        if (animal != null) {
            fillFormFromAnimal(animal);
        } else {
            handleSpeciesChanged();
        }

        setSize(580, 560);
        setLocationRelativeTo(parent);
    }

    // --- 1. Custom Header ---
    private JPanel createCustomHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BG_COLOR);

        String titleText = (animal == null) ? "Register New Patient" : "Update Patient Record";
        JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        lblTitle.setForeground(TEXT_DARK);
        titlePanel.add(lblTitle);

        if (animal != null) {
            titlePanel.add(Box.createVerticalStrut(2));
            JLabel lblSubtitle = new JLabel("Patient ID: " + animal.getAnimalId());
            lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lblSubtitle.setForeground(TEXT_MUTED);
            titlePanel.add(lblSubtitle);
        }

        JLabel closeBtn = new JLabel("X");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        closeBtn.setForeground(new Color(180, 180, 180));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(closeBtn, BorderLayout.EAST);

        headerPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) { pX = me.getX(); pY = me.getY(); }
        });
        headerPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent me) {
                setLocation(getLocation().x + me.getX() - pX, getLocation().y + me.getY() - pY);
            }
        });

        return headerPanel;
    }

    // --- 2. Form Body ---
    private JPanel createFormBody() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        formPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ROW 0 & 1: Species Dropdown
        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 4, 0);
        String speciesLabel = (animal == null) ? "Species *" : "Species (Cannot be changed after registration)*";
        formPanel.add(createLabel(speciesLabel), gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 12, 0);
        cmbSpecies = new JComboBox<>(new String[]{"Dog", "Cat", "Livestock"});
        styleComboBox(cmbSpecies);
        cmbSpecies.addActionListener(e -> handleSpeciesChanged());
        formPanel.add(cmbSpecies, gbc);

        // ROW 2: Labels (Name, Age, Weight)
        gbc.gridy = 2;
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.weightx = 0.6;
        gbc.insets = new Insets(0, 0, 4, 12);
        formPanel.add(createLabel("Patient Name*"), gbc);

        gbc.gridwidth = 1; gbc.gridx = 2; gbc.weightx = 0.2;
        gbc.insets = new Insets(0, 0, 4, 12);
        formPanel.add(createLabel("Age (Years)*"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.2;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Weight (kg)*"), gbc);

        // ROW 3: Fields (Name, Age, Weight)
        gbc.gridy = 3;
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.weightx = 0.6;
        gbc.insets = new Insets(0, 0, 12, 12);
        txtName = new JTextField(); styleTextField(txtName);
        txtName.setToolTipText("e.g., Bella"); // UPDATED
        formPanel.add(txtName, gbc);

        gbc.gridwidth = 1; gbc.gridx = 2; gbc.weightx = 0.2;
        txtAge = new JTextField(); styleTextField(txtAge);
        txtAge.setToolTipText("e.g., 3"); // UPDATED
        formPanel.add(txtAge, gbc);

        gbc.gridx = 3; gbc.weightx = 0.2;
        gbc.insets = new Insets(0, 0, 12, 0);
        txtWeight = new JTextField(); styleTextField(txtWeight);
        txtWeight.setToolTipText("e.g., 15.5"); // UPDATED
        formPanel.add(txtWeight, gbc);

        // ROW 4 & 5: Owner Name & Contact
        gbc.gridy = 4;
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 4, 12);
        formPanel.add(createLabel("Owner Name*"), gbc);

        gbc.gridx = 2; gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Contact Number*"), gbc);

        gbc.gridy = 5;
        gbc.gridx = 0; gbc.insets = new Insets(0, 0, 12, 12);
        txtOwnerName = new JTextField(); styleTextField(txtOwnerName);
        txtOwnerName.setToolTipText("e.g., Juan dela Cruz"); // UPDATED
        formPanel.add(txtOwnerName, gbc);

        gbc.gridx = 2; gbc.insets = new Insets(0, 0, 12, 0);
        txtOwnerContact = new JTextField(); styleTextField(txtOwnerContact);
        txtOwnerContact.setToolTipText("e.g., 09171234567"); // UPDATED
        formPanel.add(txtOwnerContact, gbc);

        // ROW 6 & 7: Address
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Address"), gbc);

        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 15, 0);
        txtOwnerAddress = new JTextField(); styleTextField(txtOwnerAddress);
        txtOwnerAddress.setToolTipText("e.g., 123 Main St., Apalit, Pampanga"); // UPDATED
        formPanel.add(txtOwnerAddress, gbc);

        // ROW 8: Separator
        gbc.gridy = 8; gbc.insets = new Insets(5, 0, 15, 0);
        formPanel.add(createSeparatorPanel("Species-Specific Details"), gbc);

        // ROW 9: Dynamic Cards
        gbc.gridy = 9; gbc.insets = new Insets(0, 0, 0, 0);

        pnlSpeciesFields = new JPanel(new CardLayout());
        pnlSpeciesFields.setBackground(BG_COLOR);

        pnlSpeciesFields.add(createDogCard(), "Dog");
        pnlSpeciesFields.add(createCatCard(), "Cat");
        pnlSpeciesFields.add(createLivestockCard(), "Livestock");

        formPanel.add(pnlSpeciesFields, gbc);

        return formPanel;
    }

    private JPanel createDogCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 0.5;

        g.gridy = 0; g.gridx = 0; g.insets = new Insets(0, 0, 4, 12);
        panel.add(createLabel("Breed"), g);

        g.gridy = 1; g.insets = new Insets(0, 0, 0, 12);
        txtBreed = new JTextField(); styleTextField(txtBreed);
        txtBreed.setToolTipText("e.g., Golden Retriever");
        panel.add(txtBreed, g);

        g.gridy = 1; g.gridx = 1; g.insets = new Insets(0, 0, 0, 0);
        chkRabiesVaccinated = new JCheckBox("Rabies Vaccinated");
        chkRabiesVaccinated.setBackground(BG_COLOR); chkRabiesVaccinated.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(chkRabiesVaccinated, g);

        return panel;
    }

    private JPanel createCatCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 0.5;

        g.gridy = 0; g.gridx = 0; g.insets = new Insets(0, 0, 4, 12);
        panel.add(createLabel("Coat Color"), g);

        g.gridy = 1; g.insets = new Insets(0, 0, 0, 12);
        txtCoatColor = new JTextField(); styleTextField(txtCoatColor);
        txtCoatColor.setToolTipText("e.g., Orange Tabby");
        panel.add(txtCoatColor, g);

        g.gridy = 1; g.gridx = 1; g.insets = new Insets(0, 0, 0, 0);
        chkIndoor = new JCheckBox("Indoor Cat");
        chkIndoor.setBackground(BG_COLOR); chkIndoor.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(chkIndoor, g);

        return panel;
    }

    private JPanel createLivestockCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 0.5;

        g.gridy = 0; g.gridx = 0; g.insets = new Insets(0, 0, 4, 12);
        panel.add(createLabel("Livestock Type"), g);
        g.gridx = 1; g.insets = new Insets(0, 0, 4, 0);
        panel.add(createLabel("Tag Number"), g);

        g.gridy = 1; g.gridx = 0; g.insets = new Insets(0, 0, 0, 12);
        cmbLivestockType = new JComboBox<>(new String[]{"Cow", "Goat", "Pig", "Sheep", "Horse"});
        styleComboBox(cmbLivestockType);
        panel.add(cmbLivestockType, g);

        g.gridx = 1; g.insets = new Insets(0, 0, 0, 0);
        txtTagNumber = new JTextField(); styleTextField(txtTagNumber);
        txtTagNumber.setToolTipText("e.g., TAG-9912"); // UPDATED
        panel.add(txtTagNumber, g);

        return panel;
    }

    private JPanel createSeparatorPanel(String titleText) {
        JPanel sepPanel = new JPanel(new BorderLayout(10, 0));
        sepPanel.setBackground(BG_COLOR);
        JLabel lblSepTitle = new JLabel(titleText);
        lblSepTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSepTitle.setForeground(TEXT_MUTED);
        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(220, 224, 230));
                g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }
        };
        line.setBackground(BG_COLOR);
        line.setPreferredSize(new Dimension(100, 10));
        sepPanel.add(lblSepTitle, BorderLayout.WEST);
        sepPanel.add(line, BorderLayout.CENTER);
        return sepPanel;
    }

    // --- 3. Footer Buttons ---
    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        footerPanel.setBackground(BG_COLOR);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnCancel = new JButton("Cancel");
        styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> dispose());

        String btnText = (animal == null) ? "Save Patient" : "Save Changes";
        JButton btnSave = new JButton(btnText);
        stylePrimaryButton(btnSave);
        btnSave.addActionListener(e -> handleSave());

        footerPanel.add(btnCancel);
        footerPanel.add(btnSave);
        getRootPane().setDefaultButton(btnSave);

        return footerPanel;
    }

    // --- Data Handlers ---
    private void handleSpeciesChanged() {
        CardLayout cl = (CardLayout) pnlSpeciesFields.getLayout();
        cl.show(pnlSpeciesFields, (String) cmbSpecies.getSelectedItem());
    }

    private void fillFormFromAnimal(Animal a) {
        cmbSpecies.setSelectedItem(a.getSpecies());
        cmbSpecies.setEnabled(false);
        txtName.setText(a.getName());
        txtAge.setText(String.valueOf(a.getAge()));
        txtWeight.setText(String.valueOf(a.getWeightKg()));
        txtOwnerName.setText(a.getOwner().getOwnerName());
        txtOwnerContact.setText(a.getOwner().getContactNumber());
        txtOwnerAddress.setText(a.getOwner().getAddress());

        if (a instanceof Dog) {
            Dog dog = (Dog) a;
            txtBreed.setText(dog.getBreed());
            chkRabiesVaccinated.setSelected(dog.isRabiesVaccinated());
        } else if (a instanceof Cat) {
            Cat cat = (Cat) a;
            chkIndoor.setSelected(cat.isIndoor());
            txtCoatColor.setText(cat.getCoatColor());
        } else if (a instanceof Livestock) {
            Livestock livestock = (Livestock) a;
            txtTagNumber.setText(livestock.getTagNumber());
            cmbLivestockType.setSelectedItem(livestock.getLivestockType());
        }
        handleSpeciesChanged();
    }

    private Animal createAnimalFromForm() {
        String species = (String) cmbSpecies.getSelectedItem();
        String id = (animal == null) ? manager.generateAnimalId() : animal.getAnimalId();

        int age;
        try {
            age = Integer.parseInt(txtAge.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Age must be a whole number (e.g., 3).");
        }
        double weight = 0;
        if (!txtWeight.getText().trim().isEmpty()) {
            try {
                weight = Double.parseDouble(txtWeight.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Weight must be a valid number (e.g., 12.5).");
            }
        }
        Owner owner = new Owner(txtOwnerName.getText(), txtOwnerContact.getText(), txtOwnerAddress.getText());

        if (species.equals("Dog")) {
            return new Dog(id, txtName.getText(), age, weight, owner, txtBreed.getText(), chkRabiesVaccinated.isSelected());
        } else if (species.equals("Cat")) {
            return new Cat(id, txtName.getText(), age, weight, owner, chkIndoor.isSelected(), txtCoatColor.getText());
        } else {
            return new Livestock(id, txtName.getText(), age, weight, owner, txtTagNumber.getText(), (String) cmbLivestockType.getSelectedItem());
        }
    }

    private void handleSave() {
        try {
            Animal newAnimal = createAnimalFromForm();
            if (animal == null) {
                manager.addAnimal(newAnimal);
            } else {
                manager.updateAnimal(newAnimal);
            }
            manager.saveAll();
            dispose();
        } catch (IllegalArgumentException | StorageException e) {
            if (animal == null && e instanceof IllegalArgumentException) {
                Animal.setTotalAnimals(manager.getAllAnimals().size());
            }
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Styling Helpers ---
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(TEXT_DARK);
        return label;
    }

    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textField.setBackground(FIELD_BG);
        textField.setForeground(TEXT_DARK);
        textField.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        comboBox.setBackground(FIELD_BG);
        comboBox.setForeground(TEXT_DARK);

        // Force the dropdown height to 36 pixels to perfectly match the padded text fields
        Dimension currentSize = comboBox.getPreferredSize();
        comboBox.setPreferredSize(new Dimension(currentSize.width, 36));

        // Optional: Apply the same border color to match the text fields exactly
        comboBox.setBorder(new LineBorder(BORDER_COLOR, 1, true));
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY_BLUE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_BLUE, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(TEXT_DARK);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}