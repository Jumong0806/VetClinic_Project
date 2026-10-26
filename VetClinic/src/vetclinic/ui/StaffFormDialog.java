package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import vetclinic.exception.StorageException;
import vetclinic.model.Staff;
import vetclinic.model.VetAssistant;
import vetclinic.model.Veterinarian;
import vetclinic.service.ClinicManager;

/**
 * StaffFormDialog - the pop-up form to ADD a new staff member or UPDATE an existing one.
 * Perfectly matches the Register/Update Staff Member prototype with dynamic labels.
 */
public class StaffFormDialog extends JDialog {

    private ClinicManager manager;
    private Staff staff;

    // UI Components
    private JTextField txtStaffName;
    private JTextField txtStaffContact;
    private JComboBox<String> cmbRole;

    private JPanel pnlRoleFields; // CardLayout container for role-specific details
    private JTextField txtLicenseNumber;
    private JTextField txtSpecialization;
    private JComboBox<String> cmbShift;

    // For dragging the undecorated window
    private int pX, pY;

    // Modern UI Colors
    private final Color BORDER_COLOR = new Color(200, 204, 212);
    private final Color BG_COLOR = Color.WHITE;
    private final Color FIELD_BG = new Color(248, 249, 250);
    private final Color PRIMARY_BLUE = new Color(66, 103, 178);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);

    public StaffFormDialog(JFrame parent, ClinicManager manager, Staff staff) {
        super(parent, "", true);
        this.manager = manager;
        this.staff = staff; // <--- Stores whether we are adding (null) or updating (existing object)

        setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new LineBorder(BORDER_COLOR, 1));

        mainPanel.add(createCustomHeader(), BorderLayout.NORTH);
        mainPanel.add(createFormBody(), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(mainPanel);

        if (staff != null) {
            fillFormFromStaff(staff); // <--- Populates fields if updating
        } else {
            handleRoleChanged(); // <--- Sets up defaults if adding
        }

        setSize(560, 420);
        setLocationRelativeTo(parent);
    }

    // --- 1. Custom Header ---
    private JPanel createCustomHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(18, 25, 18, 25)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BG_COLOR);

        // Dynamic Title matching the prototype
        String titleText = (staff == null) ? "Register New Staff Member" : "Update Staff Member";
        JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        lblTitle.setForeground(TEXT_DARK);

        titlePanel.add(lblTitle);

        // Dynamic Subtitle
        if (staff != null) {
            titlePanel.add(Box.createVerticalStrut(2));
            JLabel lblSubtitle = new JLabel("Staff ID: " + staff.getStaffId());
            lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lblSubtitle.setForeground(TEXT_MUTED);
            titlePanel.add(lblSubtitle);
        }

        JLabel closeBtn = new JLabel("X");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
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

    // --- 2. Form Body (GridBagLayout & CardLayout) ---
    private JPanel createFormBody() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        formPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ROW 0: LABEL (Role - dynamically warns if updating)
        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        String roleLabelText = (staff == null) ? "Role*" : "Role (Cannot be changed after registration)*";
        formPanel.add(createLabel(roleLabelText), gbc);

        // ROW 1: FIELD (Role Dropdown)
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        cmbRole = new JComboBox<>(new String[]{"Veterinarian", "Vet Assistant"});
        styleComboBox(cmbRole);
        gbc.ipady = 9;
        cmbRole.addActionListener(e -> handleRoleChanged());
        formPanel.add(cmbRole, gbc);
        gbc.ipady = 0;

        // ROW 2: LABELS (Staff Name | Contact Number)
        gbc.gridy = 2; gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 4, 15);
        formPanel.add(createLabel("Staff Name*"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Contact Number*"), gbc);

        // ROW 3: FIELDS (Staff Name | Contact Number)
        gbc.gridy = 3;

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 15, 15);
        txtStaffName = new JTextField();
        styleTextField(txtStaffName);
        txtStaffName.setToolTipText("e.g., Olivia Dean");
        formPanel.add(txtStaffName, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        txtStaffContact = new JTextField();
        styleTextField(txtStaffContact);
        txtStaffContact.setToolTipText("e.g., +09876532112");
        formPanel.add(txtStaffContact, gbc);

        // ROW 4: PROTOTYPE SEPARATOR LINE
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 0, 15, 0);
        formPanel.add(createSeparatorPanel("Role-Specific Details"), gbc);

        // ROW 5: DYNAMIC ROLE-SPECIFIC CARDS
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 0, 0);

        pnlRoleFields = new JPanel(new CardLayout());
        pnlRoleFields.setBackground(BG_COLOR);

        pnlRoleFields.add(createVetCard(), "Veterinarian");
        pnlRoleFields.add(createAssistantCard(), "Vet Assistant");

        formPanel.add(pnlRoleFields, gbc);

        return formPanel;
    }

    private JPanel createSeparatorPanel(String titleText) {
        JPanel sepPanel = new JPanel(new BorderLayout(10, 0));
        sepPanel.setBackground(BG_COLOR);

        JLabel lblSepTitle = new JLabel(titleText);
        lblSepTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSepTitle.setForeground(TEXT_MUTED);

        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(220, 224, 230));
                g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }
        };
        line.setBackground(BG_COLOR);
        line.setPreferredSize(new java.awt.Dimension(100, 10));

        sepPanel.add(lblSepTitle, BorderLayout.WEST);
        sepPanel.add(line, BorderLayout.CENTER);
        return sepPanel;
    }

    private JPanel createVetCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0;
        g.gridx = 0; g.weightx = 0.5; g.insets = new Insets(0, 0, 4, 15);
        panel.add(createLabel("License No.*"), g);

        g.gridx = 1; g.weightx = 0.5; g.insets = new Insets(0, 0, 4, 0);
        panel.add(createLabel("Specialization"), g);

        g.gridy = 1;
        g.gridx = 0; g.insets = new Insets(0, 0, 0, 15);
        txtLicenseNumber = new JTextField();
        styleTextField(txtLicenseNumber);
        txtLicenseNumber.setToolTipText("e.g., VET-2026-991");
        panel.add(txtLicenseNumber, g);

        g.gridx = 1; g.insets = new Insets(0, 0, 0, 0);
        txtSpecialization = new JTextField();
        styleTextField(txtSpecialization);
        txtSpecialization.setToolTipText("e.g., Small Animals");
        panel.add(txtSpecialization, g);

        return panel;
    }

    private JPanel createAssistantCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0; g.gridx = 0; g.weightx = 1.0; g.insets = new Insets(0, 0, 4, 0);
        panel.add(createLabel("Shift*"), g);

        g.gridy = 1; g.insets = new Insets(0, 0, 0, 0);
        cmbShift = new JComboBox<>(new String[]{"Morning", "Afternoon", "Night"});
        styleComboBox(cmbShift);
        g.ipady = 9;
        panel.add(cmbShift, g);

        return panel;
    }

    // --- 3. Footer (Buttons) ---
    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        footerPanel.setBackground(BG_COLOR);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnCancel = new JButton("Cancel");
        styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> dispose());

        String btnText = (staff == null) ? "Save Staff" : "Update Staff";
        JButton btnSave = new JButton(btnText);
        stylePrimaryButton(btnSave);
        btnSave.addActionListener(e -> handleSave());

        footerPanel.add(btnCancel);
        footerPanel.add(btnSave);
        getRootPane().setDefaultButton(btnSave);

        return footerPanel;
    }

    // --- DYNAMIC POLYMORPHISM & LOGIC METHODS ---
    private void handleRoleChanged() {
        CardLayout cl = (CardLayout) pnlRoleFields.getLayout();
        cl.show(pnlRoleFields, (String) cmbRole.getSelectedItem());
    }

    private void fillFormFromStaff(Staff s) {
        cmbRole.setSelectedItem(s.getRole());
        cmbRole.setEnabled(false); // <--- Cannot change the role of an existing staff member
        txtStaffName.setText(s.getStaffName());
        txtStaffContact.setText(s.getContactNumber());

        if (s instanceof Veterinarian) {
            Veterinarian vet = (Veterinarian) s;
            txtLicenseNumber.setText(vet.getLicenseNumber());
            txtSpecialization.setText(vet.getSpecialization());
        } else if (s instanceof VetAssistant) {
            cmbShift.setSelectedItem(((VetAssistant) s).getShift());
        }
        handleRoleChanged();
    }

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

    private void handleSave() {
        try {
            Staff checked = createStaffFromForm();
            String successMessage;

            if (staff == null) {
                manager.addStaff(checked); // <--- ADD STAFF LOGIC
                successMessage = "New staff member registered successfully.";
            } else {
                // <--- UPDATE STAFF LOGIC
                staff.setStaffName(checked.getStaffName());
                staff.setContactNumber(checked.getContactNumber());
                if (staff instanceof Veterinarian) {
                    ((Veterinarian) staff).setLicenseNumber(txtLicenseNumber.getText());
                    ((Veterinarian) staff).setSpecialization(txtSpecialization.getText());
                } else if (staff instanceof VetAssistant) {
                    ((VetAssistant) staff).setShift((String) cmbShift.getSelectedItem());
                }
                successMessage = "Staff member record updated successfully.";
            }

            manager.saveAll();

            // --- ADDED: Success Confirmation Pop-up ---
            JOptionPane.showMessageDialog(this, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);

            dispose();
        } catch (IllegalArgumentException | StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- STYLING HELPERS ---
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