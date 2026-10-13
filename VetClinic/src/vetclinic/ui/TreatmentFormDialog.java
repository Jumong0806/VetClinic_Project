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
import vetclinic.model.TreatmentRecord;
import vetclinic.service.ClinicManager;
import vetclinic.util.ValidationUtil;

/**
 * TreatmentFormDialog - the pop-up form to UPDATE an existing treatment record.
 */
public class TreatmentFormDialog extends JDialog {

    private ClinicManager manager;
    private TreatmentRecord originalRecord;
    private Staff currentStaff;            // the currently logged-in staff member
    private JLabel lblPatient;
    private JLabel lblPerformedBy;
    private JTextField txtTreatmentDate;
    private JTextField txtDiagnosis;
    private JTextField txtTreatment;
    private JTextField txtMedication;
    private JTextField txtFollowUpDate;
    private JButton btnSave;
    private JButton btnCancel;

    public TreatmentFormDialog(JFrame parent, ClinicManager manager, TreatmentRecord record, Staff currentStaff) {
        super(parent, "Update Treatment " + record.getRecordId(), true);
        this.manager = manager;
        this.originalRecord = record;
        this.currentStaff = currentStaff;

        setLayout(new BorderLayout(8, 8));

        Animal animal = manager.findAnimalById(record.getAnimalId());
        String animalStr = (animal != null) ? animal.getName() + " (" + animal.getAnimalId() + ")" : record.getAnimalId();

        lblPatient = new JLabel(animalStr);
        // The original staff who performed it is shown, but role permissions are checked against current user
        lblPerformedBy = new JLabel(record.getPerformedBy().getStaffName() + " - " + record.getPerformedBy().getRole());

        txtTreatmentDate = new JTextField(record.getTreatmentDate().toString(), 18);
        txtDiagnosis = new JTextField(record.getDiagnosis(), 18);
        txtTreatment = new JTextField(record.getTreatment(), 18);
        txtMedication = new JTextField(record.getMedication(), 18);

        String followUp = (record.getFollowUpDate() == null) ? "" : record.getFollowUpDate().toString();
        txtFollowUpDate = new JTextField(followUp, 18);
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

    private void applyRolePermissions() {
        if (!currentStaff.canDiagnose()) {
            txtDiagnosis.setEnabled(false);
        }
        if (!currentStaff.canPrescribeMedication()) {
            txtMedication.setEnabled(false);
        }
    }

    private void handleSave() {
        try {
            LocalDate treatmentDate = ValidationUtil.parseDate(txtTreatmentDate.getText());
            LocalDate followUpDate = null;
            if (!ValidationUtil.isBlank(txtFollowUpDate.getText())) {
                followUpDate = ValidationUtil.parseDate(txtFollowUpDate.getText());
            }

            // Build the updated record reusing the immutable IDs
            TreatmentRecord updatedRecord = new TreatmentRecord(
                    originalRecord.getRecordId(),
                    originalRecord.getAnimalId(),
                    treatmentDate,
                    txtDiagnosis.getText(),
                    txtTreatment.getText(),
                    txtMedication.getText(),
                    followUpDate,
                    originalRecord.getPerformedBy() // Keep the original staff context
            );

            manager.updateTreatment(updatedRecord);
            manager.saveAll();

            JOptionPane.showMessageDialog(this, "Treatment record updated.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Please check the form", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
    }

    private void handleCancel() {
        dispose();
    }

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