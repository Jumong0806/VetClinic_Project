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
import java.util.List;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import vetclinic.exception.StorageException;
import vetclinic.model.Staff;
import vetclinic.model.VetAssistant;
import vetclinic.model.Veterinarian;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;
import vetclinic.util.ImageUtil;

// StaffPanel manages the veterinary team and staff members view with search, filtering, and stable cards.
public class StaffPanel extends JPanel {

    // Core Dependencies
    private ClinicManager manager;
    private AuthService authService;

    // UI Components
    private JTextField txtSearch;
    private final String SEARCH_PLACEHOLDER = "Search by name, role, or ID...";
    private JComboBox<String> cmbRoleFilter;
    private JTable tblStaff;

    private JButton btnAddStaff;
    private JButton btnUpdate;
    private JButton btnDelete;

    private JLabel lblTitle;
    private JPanel pnlContainer;
    private JPanel pnlEmptyState;

    // Detail Component Labels
    private JLabel lblDetailName;
    private JLabel lblDetailId;
    private JLabel lblDetailRole;
    private JLabel lblDetailContact;
    private JLabel lblDetailRoleInfoKey1;
    private JLabel lblDetailRoleInfoVal1;
    private JLabel lblDetailRoleInfoKey2;
    private JLabel lblDetailRoleInfoVal2;

    // Theme Constants
    private final Color TEXT_DARK = new Color(0x1E293B);
    private final Color TEXT_MUTED = new Color(0x64748B);
    private final Color BORDER_COLOR = new Color(0xCBD5E1);
    private final Color BG_COLOR = new Color(0xF8FAFC);

    // Constructs the StaffPanel, initializes layout components, and loads initial data.
    public StaffPanel(ClinicManager manager, AuthService authService) {
        this.manager = manager;
        this.authService = authService;

        setLayout(new BorderLayout(0, 16));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));

        buildSearchBar();
        buildTablePanel();
        buildDetailsPanel();
        refreshTable(manager.getAllStaff());
        clearDetails();
    }

    // Builds the top search bar, role filter dropdown, and the Add Staff action button.
    private void buildSearchBar() {
        // Search text input box setup
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

        // Placeholder behavior on focus gain/loss
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
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText(SEARCH_PLACEHOLDER);
                    txtSearch.setForeground(TEXT_MUTED);
                }
            }
        });

        // Document listener for live search triggering as user types
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { handleSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { handleSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { handleSearch(); }
        });

        pnlSearchBox.add(lblSearchIcon, BorderLayout.WEST);
        pnlSearchBox.add(txtSearch, BorderLayout.CENTER);

        // Role filter combo box setup
        cmbRoleFilter = new JComboBox<>(new String[]{"All Roles", "Veterinarian", "Vet Assistant"});
        cmbRoleFilter.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cmbRoleFilter.setBackground(Color.WHITE);
        cmbRoleFilter.setForeground(TEXT_DARK);
        cmbRoleFilter.setFocusable(false);
        cmbRoleFilter.setPreferredSize(new Dimension(130, 42));
        cmbRoleFilter.setBorder(null);

        if (cmbRoleFilter.getEditor().getEditorComponent() instanceof JComponent) {
            ((JComponent) cmbRoleFilter.getEditor().getEditorComponent()).setBorder(null);
            ((JComponent) cmbRoleFilter.getEditor().getEditorComponent()).setOpaque(false);
        }

        cmbRoleFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
                lbl.setBackground(isSelected ? new Color(0xDBEAFE) : Color.WHITE);
                lbl.setForeground(TEXT_DARK);
                return lbl;
            }
        });
        cmbRoleFilter.addActionListener(e -> handleSearch());

        JPanel pnlFilterBox = new JPanel(new BorderLayout());
        pnlFilterBox.setBackground(Color.WHITE);
        pnlFilterBox.setPreferredSize(new Dimension(130, 42));
        pnlFilterBox.add(cmbRoleFilter, BorderLayout.CENTER);

        // Add Staff Button setup
        btnAddStaff = new JButton("+ Add Staff");
        btnAddStaff.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAddStaff.setForeground(Color.WHITE);
        btnAddStaff.setBackground(new Color(0x2563EB));
        btnAddStaff.setFocusPainted(false);
        btnAddStaff.setOpaque(true);
        btnAddStaff.setPreferredSize(new Dimension(120, 42));
        btnAddStaff.setBorder(BorderFactory.createLineBorder(new Color(0x1D4ED8), 1, true));
        btnAddStaff.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddStaff.addActionListener(e -> handleAddStaff());

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        pnlRight.setOpaque(false);
        pnlRight.add(pnlFilterBox);
        pnlRight.add(btnAddStaff);

        JPanel pnlTop = new JPanel(new BorderLayout(8, 0));
        pnlTop.setOpaque(false);
        pnlTop.add(pnlSearchBox, BorderLayout.CENTER);
        pnlTop.add(pnlRight, BorderLayout.EAST);

        add(pnlTop, BorderLayout.NORTH);
    }

    // Filters and updates the table results based on the text search query and role filter selection.
    private void handleSearch() {
        String query = txtSearch.getText();
        if (query.equals(SEARCH_PLACEHOLDER)) {
            query = "";
        }

        // 1. Retrieve text search results from the manager
        List<Staff> searched = manager.searchStaff(query);
        String selectedRole = (String) cmbRoleFilter.getSelectedItem();

        // 2. Refine the searched results in-memory based on the active species filter dropdown
        List<Staff> filtered = new java.util.ArrayList<>();
        for (Staff staff : searched) {
            if (selectedRole == null || selectedRole.equals("All Roles") || staff.getRole().equalsIgnoreCase(selectedRole)) {
                filtered.add(staff);
            }
        }

        // 3. Update the table display
        refreshTable(filtered);
    }

    // Constructs the central JTable component displaying staff records with padding and row selection hooks.
    private void buildTablePanel() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Staff ID", "Staff Name", "Role", "Contact"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblStaff = new JTable(model);
        tblStaff.setRowHeight(38);
        tblStaff.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tblStaff.setForeground(TEXT_DARK);
        tblStaff.setSelectionBackground(new Color(0xDBEAFE));
        tblStaff.setSelectionForeground(TEXT_DARK);
        tblStaff.setShowVerticalLines(true);
        tblStaff.setGridColor(new Color(0xE2E8F0));

        tblStaff.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tblStaff.getTableHeader().setBackground(new Color(0xF8FAFC));
        tblStaff.getTableHeader().setForeground(TEXT_DARK);
        tblStaff.getTableHeader().setPreferredSize(new Dimension(0, 40));

        tblStaff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer to add inner cell padding
        javax.swing.table.DefaultTableCellRenderer paddingRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        };

        for (int i = 0; i < tblStaff.getColumnCount(); i++) {
            tblStaff.getColumnModel().getColumn(i).setCellRenderer(paddingRenderer);
        }

        // Trigger detail pane update when row selection changes
        tblStaff.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSelectionChanged();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblStaff);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);
    }

    // Constructs the bottom details section containing the CardLayout container, empty state, cards, and buttons.
    private void buildDetailsPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setOpaque(false);
        pnlWrapper.setPreferredSize(new Dimension(0, 180));

        JPanel pnlDetails = new JPanel(new BorderLayout(0, 12));
        pnlDetails.setBackground(Color.WHITE);
        pnlDetails.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 24, 16, 24)
        ));

        lblTitle = new JLabel("Selected Staff Details");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(new Color(0x1E3A8A));
        pnlDetails.add(lblTitle, BorderLayout.NORTH);

        // Empty State UI (Shown when no row is selected)
        JPanel pnlEmptyContent = new JPanel();
        pnlEmptyContent.setLayout(new BoxLayout(pnlEmptyContent, BoxLayout.Y_AXIS));
        pnlEmptyContent.setOpaque(false);

        JLabel lblEmptyIcon = new JLabel(ImageUtil.load("/images/staff-empty-final.png", 64, 64), SwingConstants.CENTER);
        lblEmptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyTitle = new JLabel("No Staff Selected", SwingConstants.CENTER);
        lblEmptyTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblEmptyTitle.setForeground(TEXT_DARK);
        lblEmptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptySubtitle = new JLabel("Select a staff member from the table to view their details and available actions.", SwingConstants.CENTER);
        lblEmptySubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblEmptySubtitle.setForeground(TEXT_MUTED);
        lblEmptySubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlEmptyContent.add(lblEmptyIcon);
        pnlEmptyContent.add(Box.createVerticalStrut(8));
        pnlEmptyContent.add(lblEmptyTitle);
        pnlEmptyContent.add(Box.createVerticalStrut(4));
        pnlEmptyContent.add(lblEmptySubtitle);

        pnlEmptyState = new JPanel(new GridBagLayout());
        pnlEmptyState.setOpaque(false);
        pnlEmptyState.add(pnlEmptyContent, new GridBagConstraints());

        // Active Data State UI (Stable 4-Column Layout via BorderLayout)
        JPanel pnlDetailsContent = new JPanel(new BorderLayout(16, 0));
        pnlDetailsContent.setOpaque(false);

        // LEFT ZONE: Name & ID stack with a locked width boundary to prevent layout shifting
        JPanel pnlID = new JPanel(new GridBagLayout());
        pnlID.setOpaque(false);
        pnlID.setPreferredSize(new Dimension(240, 100));

        lblDetailName = new JLabel("-");
        lblDetailName.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblDetailName.setForeground(new Color(0x1E293B));
        lblDetailName.setHorizontalAlignment(SwingConstants.CENTER);
        lblDetailName.setPreferredSize(new Dimension(220, 26));

        lblDetailId = new JLabel("-");
        lblDetailId.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDetailId.setForeground(new Color(0x64748B));
        lblDetailId.setHorizontalAlignment(SwingConstants.CENTER);
        lblDetailId.setPreferredSize(new Dimension(220, 20));

        GridBagConstraints gbcId = new GridBagConstraints();
        gbcId.gridx = 0; gbcId.gridy = 0;
        gbcId.insets = new Insets(0, 0, 4, 0);
        pnlID.add(lblDetailName, gbcId);

        gbcId.gridy = 1;
        gbcId.insets = new Insets(0, 0, 0, 0);
        pnlID.add(lblDetailId, gbcId);

        // CENTER ZONE: General Info & Role Information Cards (Forced equal size via GridLayout)
        lblDetailRole = new JLabel("-");
        lblDetailContact = new JLabel("-");
        lblDetailRoleInfoKey1 = new JLabel("License No:");
        lblDetailRoleInfoVal1 = new JLabel("-");
        lblDetailRoleInfoKey2 = new JLabel("Specialization:");
        lblDetailRoleInfoVal2 = new JLabel("-");

        JPanel pnlCard1 = createInfoCard(
                "General Info",
                new JLabel[]{new JLabel("Role:"), new JLabel("Contact:")},
                new JLabel[]{lblDetailRole, lblDetailContact}
        );

        JPanel pnlCard2 = createInfoCard(
                "Role Information",
                new JLabel[]{lblDetailRoleInfoKey1, lblDetailRoleInfoKey2},
                new JLabel[]{lblDetailRoleInfoVal1, lblDetailRoleInfoVal2}
        );

        JPanel pnlCardsWrapper = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlCardsWrapper.setOpaque(false);
        pnlCardsWrapper.add(pnlCard1);
        pnlCardsWrapper.add(pnlCard2);

        // RIGHT ZONE: Action Buttons Stack (Update and Delete)
        JPanel pnlButtonsStack = new JPanel(new GridLayout(2, 1, 0, 12));
        pnlButtonsStack.setOpaque(false);

        btnUpdate = createOutlineButton("\u270E Update", new Color(0x2563EB), new Color(0xBFDBFE), new Color(0xEFF6FF));
        btnDelete = createOutlineButton("\uD83D\uDDD1 Delete", new Color(0xDC2626), new Color(0xFECACA), new Color(0xFEF2F2));

        btnUpdate.addActionListener(e -> handleUpdateStaff());
        btnDelete.addActionListener(e -> handleDeleteStaff());

        pnlButtonsStack.add(btnUpdate);
        pnlButtonsStack.add(btnDelete);

        JPanel pnlBtnWrapper = new JPanel(new GridBagLayout());
        pnlBtnWrapper.setOpaque(false);
        pnlBtnWrapper.setPreferredSize(new Dimension(150, 100));
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

    // Helper method to generate uniform key-value info cards with a styled header.
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
        gbc.insets = new Insets(4, 0, 4, 16);

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

    // Helper method to build custom outlined buttons with border colors and hover themes.
    private JButton createOutlineButton(String text, Color textColor, Color borderColor, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(textColor);
        btn.setBackground(bgColor);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(130, 34));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Clears and repopulates the table model with the provided list of staff members.
    public void refreshTable(List<Staff> staffList) {
        DefaultTableModel model = (DefaultTableModel) tblStaff.getModel();
        model.setRowCount(0);
        for (Staff staff : staffList) {
            model.addRow(new Object[]{
                    staff.getStaffId(),
                    staff.getStaffName(),
                    staff.getRole(),
                    staff.getContactNumber() != null ? staff.getContactNumber() : "---"
            });
        }
        clearDetails();
    }

    // Populates and displays the active data state view using polymorphic checks for specific staff subtypes.
    private void showDetails(Staff staff) {
        lblTitle.setVisible(true);
        CardLayout cl = (CardLayout) pnlContainer.getLayout();
        cl.show(pnlContainer, "DATA");

        lblDetailName.setText(staff.getStaffName());
        lblDetailId.setText("(" + staff.getStaffId() + ")");

        lblDetailRole.setText(staff.getRole() != null ? staff.getRole() : "---");
        lblDetailContact.setText(staff.getContactNumber() != null ? staff.getContactNumber() : "---");

        // Polymorphic check to extract role-specific attributes safely
        if (staff instanceof Veterinarian) {
            Veterinarian vet = (Veterinarian) staff;
            lblDetailRoleInfoKey1.setText("License No.:");
            lblDetailRoleInfoVal1.setText(vet.getLicenseNumber() != null ? vet.getLicenseNumber() : "---");
            lblDetailRoleInfoKey2.setText("Specialization:");
            lblDetailRoleInfoVal2.setText(vet.getSpecialization() != null ? vet.getSpecialization() : "---");
        } else if (staff instanceof VetAssistant) {
            VetAssistant assistant = (VetAssistant) staff;
            lblDetailRoleInfoKey1.setText("Assigned Shift:");
            lblDetailRoleInfoVal1.setText(assistant.getShift() != null ? assistant.getShift() : "---");
            lblDetailRoleInfoKey2.setText("");
            lblDetailRoleInfoVal2.setText("");
        } else {
            lblDetailRoleInfoKey1.setText("");
            lblDetailRoleInfoVal1.setText("");
            lblDetailRoleInfoKey2.setText("");
            lblDetailRoleInfoVal2.setText("");
        }

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

    // Toggles enabled state for modification action buttons.
    private void setActionsEnabled(boolean enabled) {
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
    }

    // Retrieves the currently selected Staff object from the table model.
    private Staff getSelectedStaff() {
        int row = tblStaff.getSelectedRow();
        if (row == -1) return null;
        String staffId = (String) tblStaff.getValueAt(row, 0);
        for (Staff s : manager.getAllStaff()) {
            if (s.getStaffId().equals(staffId)) return s;
        }
        return null;
    }

    // Event handler for table row selection changes.
    private void handleSelectionChanged() {
        Staff selected = getSelectedStaff();
        if (selected == null) {
            clearDetails();
        } else {
            showDetails(selected);
        }
    }

    // Opens the dialog form to create a new staff member.
    private void handleAddStaff() {
        new StaffFormDialog(getParentFrame(), manager, null).setVisible(true);
        handleSearch();
    }

    // Opens the dialog form to update the selected staff member.
    private void handleUpdateStaff() {
        Staff selected = getSelectedStaff();
        if (selected == null) return;
        new StaffFormDialog(getParentFrame(), manager, selected).setVisible(true);
        handleSearch();
    }

    // Prompts for confirmation and deletes the selected staff member from the database manager.
    private void handleDeleteStaff() {
        Staff selected = getSelectedStaff();
        if (selected == null) return;

        // ---Enhanced Confirmation Pop-up ---
        int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure yoou want to delete Staff ID: " + selected.getStaffId() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) return;

        try {
            manager.removeStaff(selected.getStaffId());
            manager.saveAll();

            // --- ADDED: Success Confirmation Pop-up ---
            JOptionPane.showMessageDialog(this, "Staff member deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);

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