package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import vetclinic.model.Staff;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;

/**
 * StaffPanel - the Staff page: search, "+ Add Staff", the staff table, and "Selected Staff Details".
 * Owner: Ben
 *
 * Swing concepts: BorderLayout (page), GridLayout (details), FlowLayout (search buttons), JTable (cannot be edited).
 * OOP concept: POLYMORPHISM. The Role column and the role information come from getRole() and
 * getRoleDetails(): a Veterinarian and a VetAssistant answer differently.
 */
public class StaffPanel extends JPanel {

    private ClinicManager manager;
    private AuthService authService;
    private JTextField txtSearch;
    private JComboBox<String> cmbRoleFilter;   // "All Roles" dropdown
    private JTable tblStaff;
    private JButton btnAddStaff;
    private JButton btnUpdateStaff;
    private JButton btnDeleteStaff;
    private JLabel lblNoSelection;              // empty state message
    private JLabel lblDetailName;
    private JLabel lblDetailId;
    private JLabel lblDetailRole;
    private JLabel lblDetailContact;
    private JLabel lblDetailRoleInfo;           // from getRoleDetails()

    public StaffPanel(ClinicManager manager, AuthService authService) {
        this.manager = manager;
        this.authService = authService;

        setLayout(new BorderLayout(0, 12));
        setBackground(new Color(0xF4F7FC));
        setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));

        buildSearchBar();      // NORTH
        buildTablePanel();     // CENTER
        buildDetailsPanel();   // SOUTH (also creates the Update and Delete buttons)
        clearDetails();
    }

    // ---------------- building the screen ----------------

    // Top part: title, search box and "+ Add Staff".
    private void buildSearchBar() {
        JPanel pnlTitle = new JPanel();
        pnlTitle.setOpaque(false);
        pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Staff");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitle.setForeground(new Color(0x1F2A44));
        JLabel lblSubtitle = new JLabel("Manage your veterinary team and staff members");
        lblSubtitle.setForeground(new Color(0x6B7A99));
        pnlTitle.add(lblTitle);
        pnlTitle.add(lblSubtitle);

        txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createTitledBorder("Search by name, ID, or role"));
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

        cmbRoleFilter = new JComboBox<>(new String[]{"All Roles", "Veterinarian", "Vet Assistant"});
        cmbRoleFilter.addActionListener(e -> handleRoleFilterChanged());

        btnAddStaff = makeButton("+ Add Staff", new Color(0x2563EB), Color.WHITE);
        btnAddStaff.addActionListener(e -> handleAddStaff());

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlRight.setOpaque(false);
        pnlRight.add(cmbRoleFilter);
        pnlRight.add(btnAddStaff);

        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(pnlRight, BorderLayout.EAST);

        JPanel pnlTop = new JPanel(new BorderLayout(0, 10));
        pnlTop.setOpaque(false);
        pnlTop.add(pnlTitle, BorderLayout.NORTH);
        pnlTop.add(pnlSearch, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);
    }

    // Middle part: the staff table.
    private void buildTablePanel() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Role", "Contact Number"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblStaff = new JTable(model);
        tblStaff.setRowHeight(26);
        tblStaff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblStaff.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSelectionChanged();
            }
        });

        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(Color.WHITE);
        pnlTable.setBorder(BorderFactory.createTitledBorder("Staff List"));
        pnlTable.add(new JScrollPane(tblStaff), BorderLayout.CENTER);
        add(pnlTable, BorderLayout.CENTER);
    }

    // Bottom part: the "Selected Staff Details" panel with the Update and Delete buttons.
    private void buildDetailsPanel() {
        lblNoSelection = new JLabel("No Staff Selected. Click a row in the table to see the details.");
        lblNoSelection.setForeground(new Color(0x6B7A99));
        lblDetailName = new JLabel("-");
        lblDetailName.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblDetailId = new JLabel("-");
        lblDetailId.setForeground(new Color(0x6B7A99));
        lblDetailRole = new JLabel("-");
        lblDetailContact = new JLabel("-");
        lblDetailRoleInfo = new JLabel("-");

        JPanel pnlName = new JPanel();
        pnlName.setOpaque(false);
        pnlName.setLayout(new BoxLayout(pnlName, BoxLayout.Y_AXIS));
        pnlName.setPreferredSize(new Dimension(180, 0));
        pnlName.add(lblDetailName);
        pnlName.add(lblDetailId);

        JPanel pnlInfo = new JPanel(new GridLayout(0, 2, 8, 8));   // caption, value, caption, value...
        pnlInfo.setOpaque(false);
        addInfoRow(pnlInfo, "Role", lblDetailRole);
        addInfoRow(pnlInfo, "Contact", lblDetailContact);
        addInfoRow(pnlInfo, "Role Information", lblDetailRoleInfo);

        btnUpdateStaff = makeButton("Update Record", Color.WHITE, new Color(0x2563EB));
        btnDeleteStaff = makeButton("Delete Staff", Color.WHITE, new Color(0xD93025));
        btnUpdateStaff.addActionListener(e -> handleUpdateStaff());
        btnDeleteStaff.addActionListener(e -> handleDeleteStaff());
        JPanel pnlButtons = new JPanel(new GridLayout(2, 1, 8, 8));
        pnlButtons.setOpaque(false);
        pnlButtons.setPreferredSize(new Dimension(180, 0));
        pnlButtons.add(btnUpdateStaff);
        pnlButtons.add(btnDeleteStaff);

        JPanel pnlDetails = new JPanel(new BorderLayout(16, 8));
        pnlDetails.setBackground(Color.WHITE);
        pnlDetails.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Selected Staff Details"), BorderFactory.createEmptyBorder(6, 10, 10, 10)));
        pnlDetails.setPreferredSize(new Dimension(800, 190));
        pnlDetails.add(lblNoSelection, BorderLayout.NORTH);
        pnlDetails.add(pnlName, BorderLayout.WEST);
        pnlDetails.add(pnlInfo, BorderLayout.CENTER);
        pnlDetails.add(pnlButtons, BorderLayout.EAST);
        add(pnlDetails, BorderLayout.SOUTH);
    }

    // ---------------- showing data ----------------

    // Fills the table. Loops over the BASE type Staff (polymorphism).
    public void refreshTable(List<Staff> staffMembers) {
        DefaultTableModel model = (DefaultTableModel) tblStaff.getModel();
        model.setRowCount(0);
        for (Staff staff : staffMembers) {
            model.addRow(new Object[]{staff.getStaffId(), staff.getStaffName(), staff.getRole(), staff.getContactNumber()});
        }
        clearDetails();
    }

    // Fills the details labels from the selected staff member.
    private void showDetails(Staff staff) {
        lblNoSelection.setVisible(false);
        lblDetailName.setText(staff.getStaffName());
        lblDetailId.setText(staff.getStaffId());
        lblDetailRole.setText(staff.getRole());
        lblDetailContact.setText(staff.getContactNumber());
        lblDetailRoleInfo.setText(staff.getRoleDetails());   // license + specialization, or shift (polymorphic)
        setActionsEnabled(true);
    }

    // Empty state: nobody is selected.
    private void clearDetails() {
        lblNoSelection.setVisible(true);
        lblDetailName.setText("-");
        lblDetailId.setText("-");
        lblDetailRole.setText("-");
        lblDetailContact.setText("-");
        lblDetailRoleInfo.setText("-");
        setActionsEnabled(false);
    }

    private void setActionsEnabled(boolean enabled) {
        btnUpdateStaff.setEnabled(enabled);
        btnDeleteStaff.setEnabled(enabled);
    }

    // Returns the staff member of the selected row, or null.
    private Staff getSelectedStaff() {
        int row = tblStaff.getSelectedRow();
        if (row == -1) {
            return null;
        }
        return manager.findStaffById((String) tblStaff.getValueAt(row, 0));
    }

    // ---------------- events ----------------

    private void handleSelectionChanged() {
        Staff selected = getSelectedStaff();
        if (selected == null) {
            clearDetails();
        } else {
            showDetails(selected);
        }
    }

    // Searches by name, ID or role
    private void handleSearch() {
        List<Staff> result = manager.searchStaff(txtSearch.getText());
        List<Staff> byRole = manager.filterStaffByRole((String) cmbRoleFilter.getSelectedItem());
        List<Staff> both = new java.util.ArrayList<>();
        for (Staff staff : result) {
            if (byRole.contains(staff)) {
                both.add(staff);
            }
        }
        refreshTable(both);
    }

    // "+ Add Staff": opens the form (staff = null means a new staff member).
    private void handleAddStaff() {
        new StaffFormDialog(getParentFrame(), manager, null).setVisible(true);
        handleSearch();
    }

    // "Update Record": opens the form filled with the selected staff member.
    private void handleUpdateStaff() {
        Staff selected = getSelectedStaff();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a staff member first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new StaffFormDialog(getParentFrame(), manager, selected).setVisible(true);
        handleSearch();
    }

    // "Delete Staff": asks first. You cannot delete yourself or a staff member who has treatment records.
    private void handleDeleteStaff() {
        Staff selected = getSelectedStaff();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a staff member first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Staff loggedIn = authService.getCurrentStaff();
        if (loggedIn != null && loggedIn.getStaffId().equals(selected.getStaffId())) {
            JOptionPane.showMessageDialog(this, "You cannot delete the staff member who is logged in.",
                    "Not allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete " + selected.getStaffName() + " (" + selected.getStaffId() + ")?",
                "Delete staff", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            manager.removeStaff(selected.getStaffId());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot delete", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            manager.saveAll();
        } catch (StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
        }
        handleSearch();
    }

    // "All Roles" dropdown changed.
    private void handleRoleFilterChanged() {
        handleSearch();
    }

    // ---------------- small private helpers (not in the Excel, see DEVIATIONS) ----------------

    private JFrame getParentFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }

    private void addInfoRow(JPanel panel, String caption, JLabel value) {
        JLabel lblCaption = new JLabel(caption);
        lblCaption.setForeground(new Color(0x6B7A99));
        panel.add(lblCaption);
        panel.add(value);
    }

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
