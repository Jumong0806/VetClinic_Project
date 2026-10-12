package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import vetclinic.exception.StorageException;
import vetclinic.model.Animal;
import vetclinic.model.Staff;
import vetclinic.service.ClinicManager;
import vetclinic.util.ValidationUtil;

/**
 * TreatmentDialog - the pop-up form to add a treatment to the selected patient.
 * The LOGGED-IN staff member performs the treatment. A Vet Assistant cannot diagnose or prescribe.
 * Owner: Ben
 *
 * Swing concepts: modal JDialog, GridBagLayout (form), FlowLayout (buttons), setEnabled() to lock boxes.
 * OOP concept: POLYMORPHISM. We ask the staff object canDiagnose() and canPrescribeMedication();
 * a Veterinarian and a VetAssistant give different answers, so the form behaves differently.
 */
public class TreatmentDialog extends JDialog {

    private ClinicManager manager;
    private Animal animal;
    private Staff currentStaff;            // the logged-in staff
    private JLabel lblPatient;
    private JLabel lblPerformedBy;
    private JTextField txtTreatmentDate;   // defaults to today, format yyyy-MM-dd
    private JTextField txtDiagnosis;
    private JTextField txtTreatment;
    private JTextField txtMedication;
    private JTextField txtFollowUpDate;    // optional
    private JButton btnSave;
    private JButton btnCancel;

    public TreatmentDialog(JFrame parent, ClinicManager manager, Animal animal, Staff currentStaff) {
        super(parent, "Add Treatment", true);
        this.manager = manager;
        this.animal = animal;
        this.currentStaff = currentStaff;

        setLayout(new BorderLayout(8, 8));

        lblPatient = new JLabel(animal.getName() + " (" + animal.getAnimalId() + ")");
        lblPerformedBy = new JLabel(currentStaff.getStaffName() + " - " + currentStaff.getRole());
        txtTreatmentDate = new JTextField(LocalDate.now().toString(), 18);
        txtDiagnosis = new JTextField(18);
        txtTreatment = new JTextField(18);
        txtMedication = new JTextField(18);
        txtFollowUpDate = new JTextField(18);
        txtFollowUpDate.setToolTipText("Optional. Format: yyyy-MM-dd");

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        addRow(pnlForm, 0, "Patient", lblPatient);
        addRow(pnlForm, 1, "Performed by", lblPerformedBy);
        addRow(pnlForm, 2, "Treatment date", txtTreatmentDate);
        addRow(pnlForm, 3, "Diagnosis", txtDiagnosis);
        addRow(pnlForm, 4, "Treatment", txtTreatment);
        addRow(pnlForm, 5, "Medication", txtMedication);
        addRow(pnlForm, 6, "Follow-up date (optional)", txtFollowUpDate);

        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlButtons.add(btnSave);
        pnlButtons.add(btnCancel);
        btnSave.addActionListener(e -> handleSave());
        btnCancel.addActionListener(e -> handleCancel());
        getRootPane().setDefaultButton(btnSave);

        add(pnlForm, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);

        applyRolePermissions();
        pack();
        setLocationRelativeTo(parent);
    }

    // Locks the boxes that this staff role is not allowed to use.
    private void applyRolePermissions() {
        if (!currentStaff.canDiagnose()) {                  // a Vet Assistant cannot diagnose
            txtDiagnosis.setText("Basic care only");
            txtDiagnosis.setEnabled(false);
        }
        if (!currentStaff.canPrescribeMedication()) {       // a Vet Assistant cannot prescribe
            txtMedication.setText("None");
            txtMedication.setEnabled(false);
        }
    }

    // Save: read the form, ask the manager to record the treatment, save, close.
    private void handleSave() {
        try {
            LocalDate treatmentDate = ValidationUtil.parseDate(txtTreatmentDate.getText());
            LocalDate followUpDate = null;
            if (!ValidationUtil.isBlank(txtFollowUpDate.getText())) {
                followUpDate = ValidationUtil.parseDate(txtFollowUpDate.getText());
            }
            manager.recordTreatment(animal.getAnimalId(), currentStaff.getStaffId(), treatmentDate,
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
