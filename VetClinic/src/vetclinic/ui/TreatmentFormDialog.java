package vetclinic.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;

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
import vetclinic.model.TreatmentRecord;
import vetclinic.service.ClinicManager;
import vetclinic.util.ValidationUtil;

/**
 * TreatmentFormDialog - the pop-up form to UPDATE an existing treatment record.
 * Integrates modern UI design with polymorphic role-based access control.
 */
public class TreatmentFormDialog extends JDialog {

    private ClinicManager manager;
    private TreatmentRecord originalRecord;

    // UI Components for data binding
    private JComboBox<Staff> cmbStaff;
    private JTextField txtPatient;
    private JTextField txtTreatmentDate;
    private JTextField txtDiagnosis;
    private JTextField txtTreatment;
    private JTextField txtMedication;
    private JTextField txtFollowUpDate;

    // Coordinate trackers used to calculate mouse movement for dragging the window
    private int pX, pY;

    // Modern UI Colors
    private final Color BORDER_COLOR = new Color(200, 204, 212);
    private final Color BG_COLOR = Color.WHITE;
    private final Color FIELD_BG = new Color(248, 249, 250);
    private final Color PRIMARY_BLUE = new Color(66, 103, 178);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);

    public TreatmentFormDialog(JFrame parent, ClinicManager manager, TreatmentRecord record) {
        super(parent, "", true);
        this.manager = manager;
        this.originalRecord = record;

        setUndecorated(true); // Removes standard Windows OS header (minimize, maximize, close buttons)

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new LineBorder(BORDER_COLOR, 1));

        mainPanel.add(createCustomHeader(record.getRecordId()), BorderLayout.NORTH);
        mainPanel.add(createFormBody(record), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Evaluates the locked staff member's role to determine if Diagnosis/Medication should be greyed out
        applyRolePermissions();

        setSize(550, 520);
        setLocationRelativeTo(parent); // Centers the dialog on the screen relative to the main window
    }

    // --- 1. Custom Header ---
    private JPanel createCustomHeader(String recordId) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR), // Bottom border line
                new EmptyBorder(15, 25, 15, 25)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BG_COLOR);

        JLabel lblTitle = new JLabel("Update Treatment Record");
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Record ID: " + recordId);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2)); // Adds 2 pixels of vertical space between title and subtitle
        titlePanel.add(lblSubtitle);

        JLabel closeBtn = new JLabel("X");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        closeBtn.setForeground(new Color(200, 200, 200));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose(); // Destroys the dialog window and returns to main screen
            }
        });

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(closeBtn, BorderLayout.EAST);

        // Drag functionality: Calculates the difference between initial click and mouse movement
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

    // --- 2. Form Body (GridBagLayout) ---
    private JPanel createFormBody(TreatmentRecord record) {
        // GridBagLayout uses an invisible grid. We position components using GridBagConstraints (gbc).
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        formPanel.setBorder(new EmptyBorder(20, 25, 20, 25)); // Overall padding: Top, Left, Bottom, Right

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; // Forces components to stretch and fill their column width

        Animal animal = manager.findAnimalById(record.getAnimalId());
        String animalStr = (animal != null) ? animal.getName() + " (" + animal.getAnimalId() + ")" : record.getAnimalId();

        // ROW 1: LABELS (Patient | Performed By)
        // gridy = Row number. gridx = Column number.
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 0.5; // Allocates exactly 50% of the horizontal space to Column 0
        gbc.insets = new Insets(0, 0, 4, 15); // Margins around component: Top, Left, Bottom, Right
        formPanel.add(createLabel("Patient"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5; // Allocates the remaining 50% to Column 1
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Performed by*"), gbc);

        // ROW 2: FIELDS (Patient | Performed By)
        gbc.gridy = 1;

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 15, 15); // 15px bottom margin creates spacing between rows
        txtPatient = new JTextField(animalStr);
        styleTextField(txtPatient);
        txtPatient.setEnabled(false);
        formPanel.add(txtPatient, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 15, 0);

        // INITIALIZE LOCKED COMBO BOX:
        // We only load the original staff member into an array of size 1.
        // This optimizes memory and permanently locks the historical record.
        cmbStaff = new JComboBox<>(new Staff[]{ record.getPerformedBy() });
        styleComboBox(cmbStaff);

        gbc.ipady = 9; // Stretches the internal Y padding to make the dropdown taller
        cmbStaff.setEnabled(false); // Prevents user interaction
        formPanel.add(cmbStaff, gbc);
        gbc.ipady = 0; // Reset internal padding back to 0 for the remaining fields

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
        txtTreatmentDate = new JTextField(record.getTreatmentDate().toString());
        styleTextField(txtTreatmentDate);
        formPanel.add(txtTreatmentDate, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        String followUp = (record.getFollowUpDate() == null) ? "" : record.getFollowUpDate().toString();
        txtFollowUpDate = new JTextField(followUp);
        styleTextField(txtFollowUpDate);
        formPanel.add(txtFollowUpDate, gbc);

        // ROW 5: DIAGNOSIS (Full width span)
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2; // Tells the layout manager to merge Column 0 and 1 so the field spans the full width
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Diagnosis*"), gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 15, 0);
        txtDiagnosis = new JTextField(record.getDiagnosis());
        styleTextField(txtDiagnosis);
        formPanel.add(txtDiagnosis, gbc);

        // ROW 6: TREATMENT (Full width span)
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Treatment*"), gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 15, 0);
        txtTreatment = new JTextField(record.getTreatment());
        styleTextField(txtTreatment);
        formPanel.add(txtTreatment, gbc);

        // ROW 7: MEDICATION (Full width span)
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("Medication*"), gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 0, 0); // Bottom margin is 0 because this is the last element
        txtMedication = new JTextField(record.getMedication());
        styleTextField(txtMedication);
        formPanel.add(txtMedication, gbc);

        return formPanel;
    }

    // --- 3. Footer (Buttons) ---
    private JPanel createFooter() {
        // FlowLayout places components in a row (Aligned RIGHT, 10px horizontal gap, 15px vertical gap)
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        footerPanel.setBackground(BG_COLOR);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnCancel = new JButton("Cancel");
        styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> dispose());

        JButton btnUpdate = new JButton("Update Record");
        stylePrimaryButton(btnUpdate);
        btnUpdate.addActionListener(e -> handleSave());

        footerPanel.add(btnCancel);
        footerPanel.add(btnUpdate);

        // Allows the user to trigger the Update action by pressing the "Enter" key on their keyboard
        getRootPane().setDefaultButton(btnUpdate);

        return footerPanel;
    }

    // --- POLYMORPHISM LOGIC ---
    private void applyRolePermissions() {
        Staff selectedStaff = (Staff) cmbStaff.getSelectedItem();
        if (selectedStaff == null) return;

        // Polymorphism in action: The program doesn't need to know if 'selectedStaff' is a Vet or Assistant.
        // It simply asks the object. If it's a Vet, it returns true. If it's an Assistant, it returns false.
        boolean canDiagnose = selectedStaff.canDiagnose();
        boolean canPrescribe = selectedStaff.canPrescribeMedication();

        txtDiagnosis.setEnabled(canDiagnose);
        if (!canDiagnose) {
            txtDiagnosis.setText("Basic care only");
            txtDiagnosis.setBackground(new Color(233, 236, 239)); // Applies a disabled/grey look
        } else {
            if (txtDiagnosis.getText().equals("Basic care only")) {
                txtDiagnosis.setText("");
            }
            txtDiagnosis.setBackground(FIELD_BG);
        }

        txtMedication.setEnabled(canPrescribe);
        if (!canPrescribe) {
            txtMedication.setText("None");
            txtMedication.setBackground(new Color(233, 236, 239));
        } else {
            if (txtMedication.getText().equals("None")) {
                txtMedication.setText("");
            }
            txtMedication.setBackground(FIELD_BG);
        }
    }

    // --- SAVE LOGIC ---
    private void handleSave() {
        try {
            // Parses the text inputs into LocalDate objects. Throws an error if format is invalid.
            LocalDate treatmentDate = ValidationUtil.parseDate(txtTreatmentDate.getText());
            LocalDate followUpDate = null;
            if (!ValidationUtil.isBlank(txtFollowUpDate.getText())) {
                followUpDate = ValidationUtil.parseDate(txtFollowUpDate.getText());
            }

            Staff selectedStaff = (Staff) cmbStaff.getSelectedItem();

            // Creates a new record object merging the unchangeable IDs with the newly typed text
            TreatmentRecord updatedRecord = new TreatmentRecord(
                    originalRecord.getRecordId(),
                    originalRecord.getAnimalId(),
                    treatmentDate,
                    txtDiagnosis.getText(),
                    txtTreatment.getText(),
                    txtMedication.getText(),
                    followUpDate,
                    selectedStaff
            );

            // Passes the updated object to the business logic layer to save in memory and SQL
            manager.updateTreatment(updatedRecord);
            manager.saveAll();

            JOptionPane.showMessageDialog(this, "Treatment record updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Please check the form", JOptionPane.ERROR_MESSAGE);
            return; // Stops the save process so the user can fix their input
        } catch (StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose(); // Closes the dialog upon successful save
    }

    // --- Helper Styling Methods ---

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