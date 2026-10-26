package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import vetclinic.exception.StorageException;
import vetclinic.model.Animal;
import vetclinic.model.Dog;
import vetclinic.model.Cat;
import vetclinic.model.Livestock;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;
import vetclinic.util.ImageUtil;

// AnimalPanel manages patient records with search, species filtering, table view, and 4-column detail panel.
public class AnimalPanel extends JPanel {

    // Core Dependencies
    private ClinicManager manager;
    private AuthService authService;

    // UI Components
    private JTextField txtSearch;
    private final String SEARCH_PLACEHOLDER = "Search by name, owner, or ID...";
    private JComboBox<String> cmbSpeciesFilter;
    private JTable tblAnimals;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnAddTreatment;
    private JButton btnViewHistory;

    // Layout Containers & States
    private JLabel lblTitle;
    private JPanel pnlContainer;
    private JPanel pnlEmptyState;
    private JPanel pnlDetailsContent;

    // Detail Component Labels (Patient Info)
    private JLabel lblDetailName;
    private JLabel lblDetailId;
    private JLabel lblDetailWeight;

    // Dynamic Polymorphic Subtype Labels (Card 1 rows 2 and 3)
    private JLabel lblDetailSubKey1;
    private JLabel lblDetailSubVal1;
    private JLabel lblDetailSubKey2;
    private JLabel lblDetailSubVal2;

    private JLabel lblDetailRegistered;

    // Detail Component Labels (Owner Info)
    private JLabel lblDetailOwnerName;
    private JLabel lblDetailOwnerContact;
    private JLabel lblDetailOwnerAddress;

    // Theme Constants
    private final Color TEXT_DARK = new Color(0x1E293B);
    private final Color TEXT_MUTED = new Color(0x64748B);
    private final Color BORDER_COLOR = new Color(0xCBD5E1);
    private final Color BG_COLOR = new Color(0xF8FAFC);

    // Constructs the AnimalPanel, initializes layout components, and loads initial data.
    public AnimalPanel(ClinicManager manager, AuthService authService) {
        this.manager = manager;
        this.authService = authService;

        setLayout(new BorderLayout(0, 16));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));

        buildSearchBar();
        buildTablePanel();
        buildDetailsPanel();
        refreshTable(manager.getAllAnimals());
        clearDetails();
    }

    // Builds the top search bar, species filter dropdown, and the Add Patient action button.
    private void buildSearchBar() {
        JPanel pnlSearchBox = new JPanel(new BorderLayout(8, 0));
        pnlSearchBox.setBackground(Color.WHITE);
        pnlSearchBox.setPreferredSize(new Dimension(0, 42));
        pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JLabel lblSearchIcon = new JLabel(ImageUtil.load("/images/search.png", 18, 18));

        txtSearch = new JTextField(SEARCH_PLACEHOLDER);
        txtSearch.setBorder(null);
        txtSearch.setForeground(TEXT_MUTED);
        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));

        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(SEARCH_PLACEHOLDER)) {
                    txtSearch.setText("");
                    txtSearch.setForeground(TEXT_DARK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setText(SEARCH_PLACEHOLDER);
                    txtSearch.setForeground(TEXT_MUTED);
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { handleSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { handleSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { handleSearch(); }
        });

        pnlSearchBox.add(lblSearchIcon, BorderLayout.WEST);
        pnlSearchBox.add(txtSearch, BorderLayout.CENTER);

        cmbSpeciesFilter = new JComboBox<>(new String[]{"All Species", "Dog", "Cat", "Livestock"});
        cmbSpeciesFilter.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cmbSpeciesFilter.setBackground(Color.WHITE);
        cmbSpeciesFilter.setForeground(TEXT_DARK);
        cmbSpeciesFilter.setFocusable(false);
        cmbSpeciesFilter.setPreferredSize(new Dimension(130, 42));
        cmbSpeciesFilter.setBorder(null);

        if (cmbSpeciesFilter.getEditor().getEditorComponent() instanceof JComponent) {
            ((JComponent) cmbSpeciesFilter.getEditor().getEditorComponent()).setBorder(null);
            ((JComponent) cmbSpeciesFilter.getEditor().getEditorComponent()).setOpaque(false);
        }

        cmbSpeciesFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
                lbl.setBackground(isSelected ? new Color(0xDBEAFE) : Color.WHITE);
                lbl.setForeground(TEXT_DARK);
                return lbl;
            }
        });
        cmbSpeciesFilter.addActionListener(e -> handleSpeciesFilterChanged());

        JPanel pnlFilterBox = new JPanel(new BorderLayout());
        pnlFilterBox.setBackground(Color.WHITE);
        pnlFilterBox.setPreferredSize(new Dimension(130, 42));
        pnlFilterBox.add(cmbSpeciesFilter, BorderLayout.CENTER);

        btnAdd = new JButton("+ Add Patient");
        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBackground(new Color(0x2563EB));
        btnAdd.setFocusPainted(false);
        btnAdd.setOpaque(true);
        btnAdd.setPreferredSize(new Dimension(130, 42));
        btnAdd.setBorder(BorderFactory.createLineBorder(new Color(0x1D4ED8), 1, true));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> handleAddAnimal());

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        pnlRight.setOpaque(false);
        pnlRight.add(pnlFilterBox);
        pnlRight.add(btnAdd);

        JPanel pnlTop = new JPanel(new BorderLayout(8, 0));
        pnlTop.setOpaque(false);
        pnlTop.add(pnlSearchBox, BorderLayout.CENTER);
        pnlTop.add(pnlRight, BorderLayout.EAST);

        add(pnlTop, BorderLayout.NORTH);
    }

    // Constructs the central JTable component with fixed boundaries to prevent collapsing.
    private void buildTablePanel() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Patient ID", "Patient Name", "Species", "Age", "Weight (kg)", "Owner", "Contact", "Registered Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblAnimals = new JTable(model);
        tblAnimals.setRowHeight(38);
        tblAnimals.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tblAnimals.setForeground(TEXT_DARK);
        tblAnimals.setSelectionBackground(new Color(0xDBEAFE));
        tblAnimals.setSelectionForeground(TEXT_DARK);
        tblAnimals.setShowVerticalLines(true);
        tblAnimals.setGridColor(new Color(0xE2E8F0));

        tblAnimals.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tblAnimals.getTableHeader().setBackground(new Color(0xF8FAFC));
        tblAnimals.getTableHeader().setForeground(TEXT_DARK);
        tblAnimals.getTableHeader().setPreferredSize(new Dimension(0, 40));

        tblAnimals.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        javax.swing.table.DefaultTableCellRenderer paddingRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        };

        for (int i = 0; i < tblAnimals.getColumnCount(); i++) {
            tblAnimals.getColumnModel().getColumn(i).setCellRenderer(paddingRenderer);
        }

        tblAnimals.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSelectionChanged();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblAnimals);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        pnlTableWrapper.setOpaque(false);
        pnlTableWrapper.add(scrollPane, BorderLayout.CENTER);

        add(pnlTableWrapper, BorderLayout.CENTER);
    }

    // Constructs the bottom details section perfectly synchronized with StaffPanel sizing (180px height).
    private void buildDetailsPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setOpaque(false);
        pnlWrapper.setPreferredSize(new Dimension(0, 220));

        JPanel pnlDetails = new JPanel(new BorderLayout(0, 12));
        pnlDetails.setBackground(Color.WHITE);
        pnlDetails.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 24, 16, 24)
        ));

        lblTitle = new JLabel("Selected Patient Details");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(new Color(0x1E3A8A));
        pnlDetails.add(lblTitle, BorderLayout.NORTH);

        // Empty State UI
        JPanel pnlEmptyContent = new JPanel();
        pnlEmptyContent.setLayout(new BoxLayout(pnlEmptyContent, BoxLayout.Y_AXIS));
        pnlEmptyContent.setOpaque(false);

        JLabel lblEmptyIcon = new JLabel(ImageUtil.load("/images/patient-empty-2.png", 300, 100), SwingConstants.CENTER);
        lblEmptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlEmptyContent.add(lblEmptyIcon);
        pnlEmptyContent.add(Box.createVerticalStrut(6)); // Spacing between image and title

        JLabel lblEmptyTitle = new JLabel("No Patient Selected", SwingConstants.CENTER);
        lblEmptyTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblEmptyTitle.setForeground(TEXT_DARK);
        lblEmptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptySubtitle = new JLabel("Select a patient from the table to view their details and available actions.", SwingConstants.CENTER);
        lblEmptySubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblEmptySubtitle.setForeground(TEXT_MUTED);
        lblEmptySubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlEmptyContent.add(lblEmptyTitle);
        pnlEmptyContent.add(Box.createVerticalStrut(4));
        pnlEmptyContent.add(lblEmptySubtitle);

        pnlEmptyState = new JPanel(new GridBagLayout());
        pnlEmptyState.setOpaque(false);
        pnlEmptyState.add(pnlEmptyContent, new GridBagConstraints());

        // Active Data State UI (Stable 4-Column Layout)
        pnlDetailsContent = new JPanel(new BorderLayout(16, 0));
        pnlDetailsContent.setOpaque(false);

        // 1. LEFT ZONE: Name & ID stack with locked width boundary (Matching Staff Tab Typography)
        JPanel pnlID = new JPanel(new GridBagLayout());
        pnlID.setOpaque(false);
        pnlID.setPreferredSize(new Dimension(220, 100));

        lblDetailName = new JLabel("-");
        lblDetailName.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblDetailName.setForeground(new Color(0x1E293B));
        lblDetailName.setHorizontalAlignment(SwingConstants.CENTER);
        lblDetailName.setPreferredSize(new Dimension(200, 26));

        lblDetailId = new JLabel("-");
        lblDetailId.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDetailId.setForeground(new Color(0x64748B));
        lblDetailId.setHorizontalAlignment(SwingConstants.CENTER);
        lblDetailId.setPreferredSize(new Dimension(200, 20));

        GridBagConstraints gbcId = new GridBagConstraints();
        gbcId.gridx = 0; gbcId.gridy = 0;
        gbcId.insets = new Insets(0, 0, 4, 0);
        pnlID.add(lblDetailName, gbcId);

        gbcId.gridy = 1;
        gbcId.insets = new Insets(0, 0, 0, 0);
        pnlID.add(lblDetailId, gbcId);

        // 2. CENTER ZONE: Patient Info & Owner Info Cards (Equal sizing via GridLayout)
        lblDetailWeight = new JLabel("-");
        lblDetailRegistered = new JLabel("-");

        lblDetailSubKey1 = new JLabel("Detail 1:");
        lblDetailSubVal1 = new JLabel("-");
        lblDetailSubKey2 = new JLabel("Detail 2:");
        lblDetailSubVal2 = new JLabel("-");

        JPanel pnlCard1 = createInfoCard(
                "Patient Info",
                new JLabel[]{new JLabel("Weight:"), lblDetailSubKey1, lblDetailSubKey2, new JLabel("Registered:")},
                new JLabel[]{lblDetailWeight, lblDetailSubVal1, lblDetailSubVal2, lblDetailRegistered}
        );

        JPanel pnlCard2 = createInfoCard(
                "Owner Info",
                new JLabel[]{new JLabel("Name:"), new JLabel("Contact:"), new JLabel("Address:")},
                new JLabel[]{
                        lblDetailOwnerName = new JLabel("-"),
                        lblDetailOwnerContact = new JLabel("-"),
                        lblDetailOwnerAddress = new JLabel("-")
                }
        );

        JPanel pnlCardsWrapper = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlCardsWrapper.setOpaque(false);
        pnlCardsWrapper.add(pnlCard1);
        pnlCardsWrapper.add(pnlCard2);

        // 3. RIGHT ZONE: Action Buttons Stack (Exact StaffPanel Button Styling & Dimensions)
        JPanel pnlButtonsStack = new JPanel(new GridLayout(4, 1, 0, 6));
        pnlButtonsStack.setOpaque(false);

        btnAddTreatment = createOutlineButton("+ Add Treatment", Color.WHITE, new Color(0x1D4ED8), new Color(0x2563EB));
        btnUpdate       = createOutlineButton("\u270E Update", new Color(0x2563EB), new Color(0xBFDBFE), new Color(0xEFF6FF));
        btnViewHistory  = createOutlineButton("\uD83D\uDD52 View History", TEXT_DARK, BORDER_COLOR, Color.WHITE);
        btnDelete       = createOutlineButton("\uD83D\uDDD1 Delete", new Color(0xDC2626), new Color(0xFECACA), new Color(0xFEF2F2));

        btnAddTreatment.addActionListener(e -> handleAddTreatment());
        btnUpdate.addActionListener(e -> handleUpdateAnimal());
        btnViewHistory.addActionListener(e -> handleViewHistory());
        btnDelete.addActionListener(e -> handleDeleteAnimal());

        pnlButtonsStack.add(btnAddTreatment);
        pnlButtonsStack.add(btnUpdate);
        pnlButtonsStack.add(btnViewHistory);
        pnlButtonsStack.add(btnDelete);

        JPanel pnlBtnWrapper = new JPanel(new GridBagLayout());
        pnlBtnWrapper.setOpaque(false);
        pnlBtnWrapper.setPreferredSize(new Dimension(140, 160));
        pnlBtnWrapper.add(pnlButtonsStack, new GridBagConstraints());

        // Assemble the 4-column layout securely into BorderLayout regions
        pnlDetailsContent.add(pnlID, BorderLayout.WEST);
        pnlDetailsContent.add(pnlCardsWrapper, BorderLayout.CENTER);
        pnlDetailsContent.add(pnlBtnWrapper, BorderLayout.EAST);

        // CardLayout container to switch between Empty and Data states
        pnlContainer = new JPanel(new CardLayout());
        pnlContainer.setOpaque(false);
        pnlContainer.add(pnlEmptyState, "EMPTY");
        pnlContainer.add(pnlDetailsContent, "DATA");

        pnlDetails.add(pnlContainer, BorderLayout.CENTER);
        pnlWrapper.add(pnlDetails, BorderLayout.CENTER);
        add(pnlWrapper, BorderLayout.SOUTH);
    }

    // Helper method to generate uniform key-value info cards matching Staff tab styling.
    private JPanel createInfoCard(String title, JLabel[] captions, JLabel[] values) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblCardTitle = new JLabel(title);
        lblCardTitle.setForeground(new Color(0x1E3A8A));
        lblCardTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        card.add(lblCardTitle, BorderLayout.NORTH);

        JPanel innerGrid = new JPanel(new GridBagLayout());
        innerGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 0, 3, 16);

        for (int i = 0; i < captions.length; i++) {
            captions[i].setForeground(new Color(0x475569));
            captions[i].setFont(new Font("SansSerif", Font.PLAIN, 13));

            values[i].setForeground(TEXT_DARK);
            values[i].setFont(new Font("SansSerif", Font.PLAIN, 13));

            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            innerGrid.add(captions[i], gbc);

            gbc.gridx = 1;
            gbc.gridy = i;
            gbc.weightx = 1.0;
            innerGrid.add(values[i], gbc);
        }

        card.add(innerGrid, BorderLayout.CENTER);
        return card;
    }

    // Helper method directly mirroring StaffPanel's createOutlineButton implementation.
    private JButton createOutlineButton(String text, Color textColor, Color borderColor, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(textColor);
        btn.setBackground(bgColor);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(130, 28));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                new EmptyBorder(2, 8, 2, 8)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Clears and repopulates the table model with the given animals.
    public void refreshTable(List<Animal> animals) {
        DefaultTableModel model = (DefaultTableModel) tblAnimals.getModel();
        model.setRowCount(0);
        for (Animal animal : animals) {
            model.addRow(new Object[]{
                    animal.getAnimalId(),
                    animal.getName(),
                    animal.getSpecies(),
                    animal.getAge(),
                    animal.getWeightKg(),
                    animal.getOwner().getOwnerName(),
                    animal.getOwner().getContactNumber(),
                    animal.getRegisteredDate().toString()
            });
        }
        clearDetails();
    }

    // Populates and displays the active data state view, dynamically updating subclass-specific fields.
    private void showDetails(Animal animal) {
        lblTitle.setVisible(true);
        CardLayout cl = (CardLayout) pnlContainer.getLayout();
        cl.show(pnlContainer, "DATA");

        // Staff-style name and ID stack representation
        lblDetailName.setText(animal.getName());
        lblDetailId.setText("(" + animal.getAnimalId() + ")");

        lblDetailWeight.setText(animal.getWeightKg() == 0 ? "Not weighed yet" : animal.getWeightKg() + " kg");

        // Dynamically populate fields based on subclass type
        if (animal instanceof Dog) {
            Dog dog = (Dog) animal;
            lblDetailSubKey1.setText("Breed:");
            lblDetailSubVal1.setText(dog.getBreed());
            lblDetailSubKey2.setText("Rabies Vaccinated:");
            lblDetailSubVal2.setText(dog.isRabiesVaccinated() ? "Yes" : "No");
        } else if (animal instanceof Cat) {
            Cat cat = (Cat) animal;
            lblDetailSubKey1.setText("Indoor:");
            lblDetailSubVal1.setText(cat.isIndoor() ? "Yes" : "No");
            lblDetailSubKey2.setText("Coat Color:");
            lblDetailSubVal2.setText(cat.getCoatColor());
        } else if (animal instanceof Livestock) {
            Livestock livestock = (Livestock) animal;
            lblDetailSubKey1.setText("Tag Number:");
            lblDetailSubVal1.setText(livestock.getTagNumber());
            lblDetailSubKey2.setText("Livestock Type:");
            lblDetailSubVal2.setText(livestock.getLivestockType());
        }

        lblDetailRegistered.setText(animal.getRegisteredDate().toString());

        lblDetailOwnerName.setText(animal.getOwner().getOwnerName());
        lblDetailOwnerContact.setText(animal.getOwner().getContactNumber());
        lblDetailOwnerAddress.setText(animal.getOwner().getAddress() != null ? animal.getOwner().getAddress() : "---");

        setActionsEnabled(true);
    }

    // Switches the detail container to the empty state view and disables action buttons.
    private void clearDetails() {
        if (lblTitle != null) {
            lblTitle.setVisible(false);
        }
        CardLayout cl = (CardLayout) pnlContainer.getLayout();
        cl.show(pnlContainer, "EMPTY");
        setActionsEnabled(false);
    }

    // Toggles enabled state for patient action buttons.
    private void setActionsEnabled(boolean enabled) {
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnAddTreatment.setEnabled(enabled);
        btnViewHistory.setEnabled(enabled);
    }

    // Retrieves the currently selected Animal object from the table model.
    private Animal getSelectedAnimal() {
        int row = tblAnimals.getSelectedRow();
        if (row == -1) return null;
        String id = (String) tblAnimals.getValueAt(row, 0);
        return manager.findAnimalById(id);
    }

    // Event handler for table row selection changes.
    private void handleSelectionChanged() {
        Animal selected = getSelectedAnimal();
        if (selected == null) {
            clearDetails();
        } else {
            showDetails(selected);
        }
    }

    // Opens the dialog form to create a new patient.
    private void handleAddAnimal() {
        new AnimalFormDialog(getParentFrame(), manager, null).setVisible(true);
        handleSearch();
    }

    // Opens the dialog form to update the selected patient.
    private void handleUpdateAnimal() {
        Animal selected = getSelectedAnimal();
        if (selected == null) return;
        new AnimalFormDialog(getParentFrame(), manager, selected).setVisible(true);
        handleSearch();
    }

    // Filters and updates table results based on search query and species filter.
    private void handleSearch() {
        String query = txtSearch.getText();
        if (query.equals(SEARCH_PLACEHOLDER)) {
            query = "";
        }

        // 1. Retrieve text search results from the manager
        List<Animal> searched = manager.searchAnimals(query);
        String selectedSpecies = (String) cmbSpeciesFilter.getSelectedItem();

        // 2. Refine the searched results in-memory based on the active species filter dropdown
        List<Animal> filtered = new java.util.ArrayList<>();
        for (Animal animal : searched) {
            if (selectedSpecies == null || selectedSpecies.equals("All Species") || animal.getSpecies().equalsIgnoreCase(selectedSpecies)) {
                filtered.add(animal);
            }
        }

        // 3. Update the table display
        refreshTable(filtered);
    }

    // Opens the dialog to add a new treatment record for the selected patient.
    private void handleAddTreatment() {
        Animal selected = getSelectedAnimal();
        if (selected == null) return;
        new TreatmentDialog(getParentFrame(), manager, selected).setVisible(true);
    }

    // Opens the history dialog to view all past treatments for the selected patient.
    private void handleViewHistory() {
        Animal selected = getSelectedAnimal();
        if (selected == null) return;
        new HistoryDialog(getParentFrame(), selected).setVisible(true);
    }

    // Event handler when the species filter selection changes.
    private void handleSpeciesFilterChanged() {
        handleSearch();
    }

    // Prompts for confirmation and deletes the selected patient record from the database manager.
    private void handleDeleteAnimal() {
        Animal selected = getSelectedAnimal();
        if (selected == null) return;

        int answer = JOptionPane.showConfirmDialog(this,
                "Delete " + selected.getName() + " (" + selected.getAnimalId() + ")?\nIts treatment history will be deleted too.",
                "Delete patient", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) return;

        try {
            manager.removeAnimal(selected.getAnimalId());
            manager.saveAll();

            // --- ADDED: Success Confirmation Pop-up ---
            JOptionPane.showMessageDialog(this, "Animal deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException | StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        handleSearch();
    }

    // Utility method to fetch the parent window frame context.
    private JFrame getParentFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }
}