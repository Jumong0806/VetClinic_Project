package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import vetclinic.Main;
import vetclinic.exception.StorageException;
import vetclinic.model.Animal;
import vetclinic.model.Staff;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;

/**
 * AnimalPanel - the Patients page: search, "+ Add Patient", the patient table, and the
 * "Selected Patient Details" panel with the Update, Delete, Add Treatment and View History buttons.
 * Owner: Ben
 *
 * Swing concepts: BorderLayout (page), GridLayout (details and buttons), FlowLayout (search buttons),
 * JTable (cannot be edited), ListSelectionListener, DocumentListener, JOptionPane.
 * OOP concept: POLYMORPHISM. The table and the details loop over / use the base type Animal and call
 * getSpecies(), getSpeciesDetails(), getCareNote() and makeSound(): each subclass answers differently.
 */
public class AnimalPanel extends JPanel {

    private ClinicManager manager;
    private AuthService authService;
    private JTextField txtSearch;
    private JComboBox<String> cmbSpeciesFilter;
    private JTable tblAnimals;
    private JButton btnAdd;                        // "+ Add Patient"
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnAddTreatment;
    private JButton btnViewHistory;
    private JLabel lblNoSelection;                 // empty state message
    private JLabel lblDetailName;
    private JLabel lblDetailId;
    private JLabel lblDetailSpecies;
    private JLabel lblDetailSpeciesInfo;           // from getSpeciesDetails()
    private JLabel lblDetailAge;
    private JLabel lblDetailWeight;
    private JLabel lblDetailRegistered;
    private JLabel lblDetailOwnerName;
    private JLabel lblDetailOwnerContact;
    private JLabel lblDetailOwnerAddress;
    private JLabel lblDetailCareNote;              // from getCareNote()
    private JLabel lblDetailSound;                 // from makeSound()

    public AnimalPanel(ClinicManager manager, AuthService authService) {
        this.manager = manager;
        this.authService = authService;

        setLayout(new BorderLayout(0, 12));
        setBackground(new Color(0xF4F7FC));
        setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));

        buildSearchBar();      // NORTH
        buildTablePanel();     // CENTER
        buildDetailsPanel();   // SOUTH
        buildButtonPanel();    // goes inside the details panel (EAST)
        clearDetails();        // start with "No Patient Selected"
    }

    // ---------------- building the screen ----------------

    // Top part: the title, the search box and the "+ Add Patient" button.
    private void buildSearchBar() {
        JPanel pnlTop = new JPanel(new BorderLayout(0, 10));
        pnlTop.setOpaque(false);

        JPanel pnlTitle = new JPanel();
        pnlTitle.setOpaque(false);
        pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Patients");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitle.setForeground(new Color(0x1F2A44));
        JLabel lblSubtitle = new JLabel("Manage your clinic's patients");
        lblSubtitle.setForeground(new Color(0x6B7A99));
        pnlTitle.add(lblTitle);
        pnlTitle.add(lblSubtitle);

        txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createTitledBorder("Search by name, owner, or ID"));
        // Live search: every time the text changes we search again.
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleSearch();
            }
        });

        cmbSpeciesFilter = new JComboBox<>(new String[]{"All Species", "Dog", "Cat", "Livestock"});
        cmbSpeciesFilter.addActionListener(e -> handleSpeciesFilterChanged());

        btnAdd = makeButton("+ Add Patient", new Color(0x2563EB), Color.WHITE);
        btnAdd.addActionListener(e -> handleAddAnimal());

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlRight.setOpaque(false);
        pnlRight.add(cmbSpeciesFilter);
        pnlRight.add(btnAdd);

        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(pnlRight, BorderLayout.EAST);

        pnlTop.add(pnlTitle, BorderLayout.NORTH);
        pnlTop.add(pnlSearch, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);
    }

    // Middle part: the patient table.
    private void buildTablePanel() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Name", "Species", "Age", "Weight (kg)", "Owner", "Contact", "Registered Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // the table is only for viewing
            }
        };
        tblAnimals = new JTable(model);
        tblAnimals.setRowHeight(26);
        tblAnimals.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Event: when a row is clicked, show that patient's details.
        tblAnimals.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSelectionChanged();
            }
        });

        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(Color.WHITE);
        pnlTable.setBorder(BorderFactory.createTitledBorder("Patient List"));
        pnlTable.add(new JScrollPane(tblAnimals), BorderLayout.CENTER);
        add(pnlTable, BorderLayout.CENTER);
    }

    // Bottom part: the "Selected Patient Details" panel.
    private void buildDetailsPanel() {
        lblNoSelection = new JLabel("No Patient Selected. Click a row in the table to see the details.");
        lblNoSelection.setForeground(new Color(0x6B7A99));

        lblDetailName = new JLabel("-");
        lblDetailName.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblDetailId = new JLabel("-");
        lblDetailId.setForeground(new Color(0x6B7A99));
        lblDetailSpecies = new JLabel("-");
        lblDetailSpeciesInfo = new JLabel("-");
        lblDetailAge = new JLabel("-");
        lblDetailWeight = new JLabel("-");
        lblDetailRegistered = new JLabel("-");
        lblDetailOwnerName = new JLabel("-");
        lblDetailOwnerContact = new JLabel("-");
        lblDetailOwnerAddress = new JLabel("-");
        lblDetailCareNote = new JLabel("-");
        lblDetailSound = new JLabel("-");

        // Left: name and ID.
        JPanel pnlName = new JPanel();
        pnlName.setOpaque(false);
        pnlName.setLayout(new BoxLayout(pnlName, BoxLayout.Y_AXIS));
        pnlName.setPreferredSize(new Dimension(130, 0));
        pnlName.add(lblDetailName);
        pnlName.add(lblDetailId);

        // Middle: two columns (GridLayout: 1 row, 2 columns).
        // Left = patient info. Right = owner info on top and care info below.
        JPanel pnlInfo = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlInfo.setOpaque(false);
        pnlInfo.add(makeInfoColumn("Patient Info",
                new String[]{"Species", "Details", "Age", "Weight", "Reg. date"},
                new JLabel[]{lblDetailSpecies, lblDetailSpeciesInfo, lblDetailAge, lblDetailWeight, lblDetailRegistered}));
        JPanel pnlOwnerAndCare = new JPanel(new GridLayout(2, 1, 0, 6));
        pnlOwnerAndCare.setOpaque(false);
        pnlOwnerAndCare.add(makeInfoColumn("Owner Info",
                new String[]{"Name", "Contact", "Address"},
                new JLabel[]{lblDetailOwnerName, lblDetailOwnerContact, lblDetailOwnerAddress}));
        pnlOwnerAndCare.add(makeInfoColumn("Care Info",
                new String[]{"Note", "Sound"},
                new JLabel[]{lblDetailCareNote, lblDetailSound}));
        pnlInfo.add(pnlOwnerAndCare);

        JPanel pnlDetails = new JPanel(new BorderLayout(16, 8));
        pnlDetails.setBackground(Color.WHITE);
        pnlDetails.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Selected Patient Details"), BorderFactory.createEmptyBorder(6, 10, 10, 10)));
        pnlDetails.setPreferredSize(new Dimension(800, 270));
        pnlDetails.add(lblNoSelection, BorderLayout.NORTH);
        pnlDetails.add(pnlName, BorderLayout.WEST);
        pnlDetails.add(pnlInfo, BorderLayout.CENTER);
        add(pnlDetails, BorderLayout.SOUTH);
    }

    // The four action buttons. They are added to the EAST side of the details panel.
    private void buildButtonPanel() {
        btnUpdate = makeButton("Update", Color.WHITE, new Color(0x2563EB));
        btnDelete = makeButton("Delete", Color.WHITE, new Color(0xD93025));
        btnAddTreatment = makeButton("+ Add Treatment", new Color(0x2563EB), Color.WHITE);
        btnViewHistory = makeButton("View History", Color.WHITE, new Color(0x1F2A44));

        btnUpdate.addActionListener(e -> handleUpdateAnimal());
        btnDelete.addActionListener(e -> handleDeleteAnimal());
        btnAddTreatment.addActionListener(e -> handleAddTreatment());
        btnViewHistory.addActionListener(e -> handleViewHistory());

        JPanel pnlButtons = new JPanel(new GridLayout(2, 2, 8, 8));
        pnlButtons.setOpaque(false);
        pnlButtons.setPreferredSize(new Dimension(260, 0));
        pnlButtons.add(btnUpdate);
        pnlButtons.add(btnDelete);
        pnlButtons.add(btnAddTreatment);
        pnlButtons.add(btnViewHistory);

        // Find the details panel that buildDetailsPanel() put at the bottom (SOUTH) of this page.
        JPanel pnlDetails = (JPanel) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        pnlDetails.add(pnlButtons, BorderLayout.EAST);
    }

    // ---------------- showing data ----------------

    // Fills the table with the given animals. Loops over the BASE type Animal (polymorphism).
    public void refreshTable(List<Animal> animals) {
        DefaultTableModel model = (DefaultTableModel) tblAnimals.getModel();
        model.setRowCount(0);   // remove the old rows
        for (Animal animal : animals) {
            model.addRow(new Object[]{
                    animal.getAnimalId(),
                    animal.getName(),
                    animal.getSpecies(),                          // each subclass answers differently
                    animal.getAge(),
                    animal.getWeightKg(),
                    animal.getOwner().getOwnerName(),
                    animal.getOwner().getContactNumber(),
                    animal.getRegisteredDate().toString()});
        }
        clearDetails();
    }

    // Fills the details labels from the selected animal.
    private void showDetails(Animal animal) {
        lblNoSelection.setVisible(false);
        lblDetailName.setText(animal.getName());
        lblDetailId.setText(animal.getAnimalId());
        lblDetailSpecies.setText(animal.getSpecies());
        lblDetailSpeciesInfo.setText(wrap(animal.getSpeciesDetails()));   // breed, coat, tag... (polymorphic)
        lblDetailAge.setText(animal.getAge() + " years");
        lblDetailWeight.setText(animal.getWeightKg() == 0 ? "Not weighed yet" : animal.getWeightKg() + " kg");
        lblDetailRegistered.setText(animal.getRegisteredDate().toString());
        lblDetailOwnerName.setText(animal.getOwner().getOwnerName());
        lblDetailOwnerContact.setText(animal.getOwner().getContactNumber());
        lblDetailOwnerAddress.setText(wrap(animal.getOwner().getAddress()));
        lblDetailCareNote.setText(wrap(animal.getCareNote()));            // polymorphic
        lblDetailSound.setText(animal.makeSound());                       // polymorphic
        setActionsEnabled(true);
    }

    // Empty state: nothing is selected.
    private void clearDetails() {
        lblNoSelection.setVisible(true);
        lblDetailName.setText("-");
        lblDetailId.setText("-");
        lblDetailSpecies.setText("-");
        lblDetailSpeciesInfo.setText("-");
        lblDetailAge.setText("-");
        lblDetailWeight.setText("-");
        lblDetailRegistered.setText("-");
        lblDetailOwnerName.setText("-");
        lblDetailOwnerContact.setText("-");
        lblDetailOwnerAddress.setText("-");
        lblDetailCareNote.setText("-");
        lblDetailSound.setText("-");
        setActionsEnabled(false);
    }

    // The buttons that need a selected patient are disabled when nothing is selected.
    private void setActionsEnabled(boolean enabled) {
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnAddTreatment.setEnabled(enabled);
        btnViewHistory.setEnabled(enabled);
    }

    // Returns the animal of the selected row, or null if no row is selected.
    private Animal getSelectedAnimal() {
        int row = tblAnimals.getSelectedRow();
        if (row == -1) {
            return null;
        }
        String id = (String) tblAnimals.getValueAt(row, 0);
        return manager.findAnimalById(id);
    }

    // ---------------- events ----------------

    private void handleSelectionChanged() {
        Animal selected = getSelectedAnimal();
        if (selected == null) {
            clearDetails();
        } else {
            showDetails(selected);
        }
    }

    // "+ Add Patient": opens the form (animal = null means a new patient).
    private void handleAddAnimal() {
        new AnimalFormDialog(getParentFrame(), manager, null).setVisible(true);
        handleSearch();
    }

    // "Update": opens the same form, filled with the selected patient.
    private void handleUpdateAnimal() {
        Animal selected = getSelectedAnimal();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new AnimalFormDialog(getParentFrame(), manager, selected).setVisible(true);
        handleSearch();
    }

    // "Delete": asks first, then removes the patient and saves.
    private void handleDeleteAnimal() {
        Animal selected = getSelectedAnimal();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete " + selected.getName() + " (" + selected.getAnimalId() + ")?\nIts treatment history will be deleted too.",
                "Delete patient", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            manager.removeAnimal(selected.getAnimalId());
            saveChanges();
            handleSearch();
        }
    }

    // Searches by name, owner or ID
    private void handleSearch() {
        List<Animal> result = manager.searchAnimals(txtSearch.getText());
        List<Animal> bySpecies = manager.filterBySpecies((String) cmbSpeciesFilter.getSelectedItem());
        List<Animal> both = new ArrayList<>();
        for (Animal animal : result) {
            if (bySpecies.contains(animal)) {
                both.add(animal);
            }
        }
        refreshTable(both);
    }

    private void handleAddTreatment() {
        Animal selected = getSelectedAnimal();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new TreatmentDialog(getParentFrame(), manager, selected).setVisible(true);
    }

    // "View History": shows all treatments of the selected patient.
    private void handleViewHistory() {
        Animal selected = getSelectedAnimal();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new HistoryDialog(getParentFrame(), selected).setVisible(true);   // Animal is passed as a Treatable
    }

    // The species dropdown changed.
    private void handleSpeciesFilterChanged() {
        handleSearch();
    }

    // ---------------- small private helpers (not in the Excel, see DEVIATIONS) ----------------

    // Saves everything; shows a dialog if saving fails.
    private void saveChanges() {
        try {
            manager.saveAll();
        } catch (StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // The window that contains this panel (the main window), used as the parent of dialogs.
    private JFrame getParentFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }

    // Makes long text wrap inside a JLabel by writing it as HTML.
    private String wrap(String text) {
        return "<html><body style='width:135px'>" + text + "</body></html>";
    }

    // One column of "caption: value" rows with a bold title.
    private JPanel makeInfoColumn(String title, String[] captions, JLabel[] values) {
        JPanel pnlColumn = new JPanel(new BorderLayout(0, 6));
        pnlColumn.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        // GridBagLayout: the caption column is only as wide as needed, the value column takes the rest.
        JPanel pnlRows = new JPanel(new GridBagLayout());
        pnlRows.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(2, 0, 2, 8);
        for (int i = 0; i < captions.length; i++) {
            JLabel lblCaption = new JLabel(captions[i]);
            lblCaption.setForeground(new Color(0x6B7A99));
            lblCaption.setMinimumSize(lblCaption.getPreferredSize());   // never squeeze the caption text
            gbc.gridy = i;
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            pnlRows.add(lblCaption, gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pnlRows.add(values[i], gbc);
        }
        gbc.gridy = captions.length;   // an empty "spring" row keeps the rows at the top
        gbc.gridx = 0;
        gbc.weighty = 1;
        pnlRows.add(new JLabel(""), gbc);
        pnlColumn.add(lblTitle, BorderLayout.NORTH);
        pnlColumn.add(pnlRows, BorderLayout.CENTER);
        return pnlColumn;
    }

    // Same look for the buttons of this page.
    private JButton makeButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(0xB8C4E0)));
        button.setPreferredSize(new Dimension(130, 36));
        return button;
    }
}
