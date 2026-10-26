package vetclinic.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;

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
import vetclinic.model.Animal;
import vetclinic.model.Staff;
import vetclinic.service.ClinicManager;
import vetclinic.util.ValidationUtil;

/**
 * TreatmentDialog - the pop-up form to ADD a treatment to the selected patient.
 * Uses the same layout/styling as TreatmentFormDialog (the UPDATE dialog), but keeps its own
 * behaviour: the staff dropdown is selectable, the date defaults to today, and Save creates a new record.
 * The form adjusts dynamically based on the selected staff member's permissions (polymorphism).
 * Owner: Ben
 */
public class TreatmentDialog extends JDialog {

    private ClinicManager manager;
    private Animal animal;

    // UI Components for data binding
    private JComboBox<Staff> cmbStaff;
    private JTextField txtPatient;
    private JTextField txtTreatmentDate;   // defaults to today, format yyyy-MM-dd
    private JTextField txtDiagnosis;
    private JTextField txtTreatment;
    private JTextField txtMedication;
    private JTextField txtFollowUpDate;    // optional

    // Coordinate trackers used to calculate mouse movement for dragging the window
    private int pX, pY;

    // Same palette as TreatmentFormDialog
    private final Color BORDER_COLOR = new Color(200, 204, 212);
    private final Color BG_COLOR = Color.WHITE;
    private final Color FIELD_BG = new Color(248, 249, 250);
    private final Color DISABLED_BG = new Color(233, 236, 239);
    private final Color PRIMARY_BLUE = new Color(66, 103, 178);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);

    public TreatmentDialog(JFrame parent, ClinicManager manager, Animal animal) {
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

        // Greys out Diagnosis/Medication when the selected staff member is not allowed to fill them
        applyRolePermissions();

        setSize(550, 520);
        setLocationRelativeTo(parent);
    }

    // --- 1. Custom Header ---
    private JPanel createCustomHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(15, 25, 15, 25)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BG_COLOR);

        JLabel lblTitle = new JLabel("Add Treatment Record");
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Patient: " + animal.getName() + " (" + animal.getAnimalId() + ")");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblSubtitle);

        JLabel closeBtn = new JLabel("X");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        closeBtn.setForeground(new Color(200, 200, 200));
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
            public void mousePressed(MouseEvent me) {
                pX = me.getX();
                pY = me.getY();
            }
        });
        headerPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent me) {
                setLocation(getLocation().x + me.getX() - pX, getLocation().y + me.getY() - pY);
            }
        });

        return headerPanel;
    }

    // --- 2. Form Body (GridBagLayout, same grid as TreatmentFormDialog) ---
    private JPanel createFormBody() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        formPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ROW 1: LABELS (Patient | Performed By)
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 4, 15);
        formPanel.add(createLabel("Patient"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Performed by*"), gbc);

        // ROW 2: FIELDS (Patient | Performed By)
        gbc.gridy = 1;

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 15, 15);
        txtPatient = new JTextField(animal.getName() + " (" + animal.getAnimalId() + ")");
        styleTextField(txtPatient);
        txtPatient.setEnabled(false);
        formPanel.add(txtPatient, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 15, 0);

        // UNIQUE TO "ADD": the dropdown lists ALL staff and stays selectable
        List<Staff> allStaff = manager.getAllStaff();
        cmbStaff = new JComboBox<>(allStaff.toArray(new Staff[0]));
        styleComboBox(cmbStaff);
        cmbStaff.addActionListener(e -> applyRolePermissions());

        gbc.ipady = 9;
        formPanel.add(cmbStaff, gbc);
        gbc.ipady = 0;

        // ROW 3: LABELS (Treatment Date | Follow-up)
        gbc.gridy = 2;

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 4, 15);
        formPanel.add(createLabel("Treatment Date*"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Follow-up (Optional)"), gbc);

        // ROW 4: FIELDS (Treatment Date | Follow-up)
        gbc.gridy = 3;

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 15, 15);
        txtTreatmentDate = new JTextField(LocalDate.now().toString());   // defaults to today
        styleTextField(txtTreatmentDate);
        formPanel.add(txtTreatmentDate, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        txtFollowUpDate = new JTextField();
        txtFollowUpDate.setToolTipText("Optional. Format: yyyy-MM-dd");
        styleTextField(txtFollowUpDate);
        formPanel.add(txtFollowUpDate, gbc);

        // ROW 5: DIAGNOSIS (Full width span)
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Diagnosis*"), gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 15, 0);
        txtDiagnosis = new JTextField();
        styleTextField(txtDiagnosis);
        formPanel.add(txtDiagnosis, gbc);

        // ROW 6: TREATMENT (Full width span)
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Treatment*"), gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 15, 0);
        txtTreatment = new JTextField();
        styleTextField(txtTreatment);
        formPanel.add(txtTreatment, gbc);

        // ROW 7: MEDICATION (Full width span)
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Medication*"), gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 0, 0);
        txtMedication = new JTextField();
        styleTextField(txtMedication);
        formPanel.add(txtMedication, gbc);

        return formPanel;
    }

    // --- 3. Footer (Buttons) ---
    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        footerPanel.setBackground(BG_COLOR);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnCancel = new JButton("Cancel");
        styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> handleCancel());

        JButton btnSave = new JButton("Save Treatment");
        stylePrimaryButton(btnSave);
        btnSave.addActionListener(e -> handleSave());

        footerPanel.add(btnCancel);
        footerPanel.add(btnSave);

        getRootPane().setDefaultButton(btnSave);
        return footerPanel;
    }

    // --- POLYMORPHISM LOGIC: adjusts input fields based on the selected staff member's clearance ---
    private void applyRolePermissions() {
        Staff selectedStaff = (Staff) cmbStaff.getSelectedItem();
        if (selectedStaff == null) return;

        boolean canDiagnose = selectedStaff.canDiagnose();
        boolean canPrescribe = selectedStaff.canPrescribeMedication();

        txtDiagnosis.setEnabled(canDiagnose);
        if (!canDiagnose) {
            txtDiagnosis.setText("Basic care only");
            txtDiagnosis.setBackground(DISABLED_BG);
        } else {
            if (txtDiagnosis.getText().equals("Basic care only")) {
                txtDiagnosis.setText("");
            }
            txtDiagnosis.setBackground(FIELD_BG);
        }

        txtMedication.setEnabled(canPrescribe);
        if (!canPrescribe) {
            txtMedication.setText("None");
            txtMedication.setBackground(DISABLED_BG);
        } else {
            if (txtMedication.getText().equals("None")) {
                txtMedication.setText("");
            }
            txtMedication.setBackground(FIELD_BG);
        }
    }

    // --- SAVE LOGIC: read the form, ask the manager to record the treatment, save, close ---
    private void handleSave() {
        try {
            LocalDate treatmentDate = ValidationUtil.parseDate(txtTreatmentDate.getText());
            LocalDate followUpDate = null;
            if (!ValidationUtil.isBlank(txtFollowUpDate.getText())) {
                followUpDate = ValidationUtil.parseDate(txtFollowUpDate.getText());
            }

            Staff selectedStaff = (Staff) cmbStaff.getSelectedItem();

            manager.recordTreatment(animal.getAnimalId(), selectedStaff.getStaffId(), treatmentDate,
                    txtDiagnosis.getText(), txtTreatment.getText(), txtMedication.getText(), followUpDate);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Please check the form", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            manager.saveAll();
            JOptionPane.showMessageDialog(this, "Treatment saved for " + animal.getName() + ".", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
        }
        dispose();
    }

    private void handleCancel() {
        dispose();
    }

    // --- Helper Styling Methods (identical to TreatmentFormDialog) ---

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
                new EmptyBorder(8, 15, 8, 15)
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
                new EmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}