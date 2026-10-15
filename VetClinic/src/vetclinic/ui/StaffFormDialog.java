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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import vetclinic.exception.StorageException;
import vetclinic.model.Staff;
import vetclinic.model.VetAssistant;
import vetclinic.model.Veterinarian;
import vetclinic.service.ClinicManager;
import vetclinic.util.ValidationUtil;

/**
 * StaffFormDialog - the pop-up form to ADD a new staff member or UPDATE an existing one.
 * Owner: Richnie
 *
 * Swing concepts: modal JDialog, GridBagLayout (form), CardLayout (role fields), FlowLayout (buttons).
 * OOP concepts: INHERITANCE and POLYMORPHISM. The form creates a Veterinarian or a VetAssistant object,
 * and the rest of the program only sees a Staff.
 */
public class StaffFormDialog extends JDialog {

    private ClinicManager manager;
    private Staff staff;                         // null = we are adding new staff
    private JTextField txtStaffName;
    private JTextField txtStaffContact;
    private JTextField txtUsername;
    private JPasswordField pwdPassword;          // when updating, blank means "keep the current password"
    private JComboBox<String> cmbRole;
    private JPanel pnlRoleFields;                // CardLayout: one card per role
    private JTextField txtLicenseNumber;
    private JTextField txtSpecialization;
    private JComboBox<String> cmbShift;
    private JButton btnSave;
    private JButton btnClear;
    private JButton btnCancel;

    public StaffFormDialog(JFrame parent, ClinicManager manager, Staff staff) {
        super(parent, "Staff", true);
        this.manager = manager;
        this.staff = staff;

        buildForm();
        if (staff == null) {
            setTitle("Add Staff");
            clearForm();
        } else {
            setTitle("Update Staff " + staff.getStaffId());
            fillFormFromStaff(staff);
        }
        pack();
        setLocationRelativeTo(parent);
    }

    // ---------------- building the form ----------------

    private void buildForm() {
        setLayout(new BorderLayout(8, 8));

        cmbRole = new JComboBox<>(new String[]{"Veterinarian", "Vet Assistant"});
        txtStaffName = new JTextField(18);
        txtStaffContact = new JTextField(18);
        txtLicenseNumber = new JTextField(14);
        txtSpecialization = new JTextField(14);
        cmbShift = new JComboBox<>(new String[]{"Morning", "Afternoon", "Night"});

        JPanel pnlVet = new JPanel(new GridLayout(2, 2, 6, 6));
        pnlVet.add(new JLabel("License number"));
        pnlVet.add(txtLicenseNumber);
        pnlVet.add(new JLabel("Specialization"));
        pnlVet.add(txtSpecialization);

        JPanel pnlAssistant = new JPanel(new GridLayout(1, 2, 6, 6));
        pnlAssistant.add(new JLabel("Shift"));
        pnlAssistant.add(cmbShift);

        pnlRoleFields = new JPanel(new CardLayout());
        pnlRoleFields.setBorder(BorderFactory.createTitledBorder("Role details"));
        pnlRoleFields.add(pnlVet, "Veterinarian");
        pnlRoleFields.add(pnlAssistant, "Vet Assistant");

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        addRow(pnlForm, 0, "Role", cmbRole);
        addRow(pnlForm, 1, "Full name", txtStaffName);
        addRow(pnlForm, 2, "Contact (09...)", txtStaffContact);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 4, 4, 4);
        pnlForm.add(pnlRoleFields, gbc);

        btnSave = new JButton("Save");
        btnClear = new JButton("Clear");
        btnCancel = new JButton("Cancel");
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlButtons.add(btnSave);
        pnlButtons.add(btnClear);
        pnlButtons.add(btnCancel);

        cmbRole.addActionListener(e -> handleRoleChanged());
        btnSave.addActionListener(e -> handleSave());
        btnClear.addActionListener(e -> handleClear());
        btnCancel.addActionListener(e -> handleCancel());
        getRootPane().setDefaultButton(btnSave);

        add(pnlForm, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    // Puts the values of an existing staff member into the boxes (update mode).
    private void fillFormFromStaff(Staff staff) {
        cmbRole.setSelectedItem(staff.getRole());
        cmbRole.setEnabled(false);   // the role of an existing staff member cannot change
        txtStaffName.setText(staff.getStaffName());
        txtStaffContact.setText(staff.getContactNumber());

        // instanceof is only used here to read fields that exist in ONE subclass.
        if (staff instanceof Veterinarian) {
            Veterinarian vet = (Veterinarian) staff;
            txtLicenseNumber.setText(vet.getLicenseNumber());
            txtSpecialization.setText(vet.getSpecialization());
        } else if (staff instanceof VetAssistant) {
            cmbShift.setSelectedItem(((VetAssistant) staff).getShift());
        }
        handleRoleChanged();
    }

    // Reads the form and creates a Veterinarian or a VetAssistant (INHERITANCE).
    // When updating with an empty password box, a temporary password is used only to validate the other fields.
    // Reads the form and builds the staff object
    private Staff createStaffFromForm() {
        String id = (staff == null) ? manager.generateStaffId() : staff.getStaffId();
        String role = (String) cmbRole.getSelectedItem();

        if (role.equals("Veterinarian")) {
            return new Veterinarian(id, txtStaffName.getText(), txtStaffContact.getText(),
                    txtLicenseNumber.getText(), txtSpecialization.getText());
        }
        return new VetAssistant(id, txtStaffName.getText(), txtStaffContact.getText(),
                (String) cmbShift.getSelectedItem());
    }

    private void clearForm() {
        txtStaffName.setText("");
        txtStaffContact.setText("");
        txtLicenseNumber.setText("");
        txtSpecialization.setText("");
        cmbShift.setSelectedIndex(0);
        handleRoleChanged();
    }

    // ---------------- events ----------------

    // Shows the card of the chosen role (CardLayout).
    private void handleRoleChanged() {
        ((CardLayout) pnlRoleFields.getLayout()).show(pnlRoleFields, (String) cmbRole.getSelectedItem());
    }

    // Save: add a new staff member, or change the existing one with its validating setters.
    private void handleSave() {
        try {
            Staff checked = createStaffFromForm();   // throws IllegalArgumentException if any value is wrong
            if (staff == null) {
                manager.addStaff(checked);           // also checks that the username is free
            } else {
                staff.setStaffName(checked.getStaffName());
                staff.setContactNumber(checked.getContactNumber());
                if (staff instanceof Veterinarian) {
                    ((Veterinarian) staff).setLicenseNumber(txtLicenseNumber.getText());
                    ((Veterinarian) staff).setSpecialization(txtSpecialization.getText());
                } else if (staff instanceof VetAssistant) {
                    ((VetAssistant) staff).setShift((String) cmbShift.getSelectedItem());
                }
            }
        } catch (IllegalArgumentException e) {
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
