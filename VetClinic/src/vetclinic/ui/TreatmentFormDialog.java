package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
 * The Admin can update the performing staff member from a dropdown.
 */
public class TreatmentFormDialog extends JDialog {

    private ClinicManager manager;
    private TreatmentRecord originalRecord;
    private JLabel lblPatient;
    private JComboBox<Staff> cmbStaff;
    private JTextField txtTreatmentDate;
    private JTextField txtDiagnosis;
    private JTextField txtTreatment;
    private JTextField txtMedication;
    private JTextField txtFollowUpDate;
    private JButton btnSave;
    private JButton btnCancel;

    public TreatmentFormDialog(JFrame parent, ClinicManager manager, TreatmentRecord record) {
        super(parent, "Update Treatment " + record.getRecordId(), true);
        this.manager = manager;
        this.originalRecord = record;

        setLayout(new BorderLayout(8, 8));

        Animal animal = manager.findAnimalById(record.getAnimalId());
        String animalStr = (animal != null) ? animal.getName() + " (" + animal.getAnimalId() + ")" : record.getAnimalId();

        lblPatient = new JLabel(animalStr);

        // Builds the staff dropdown and selects the doctor who originally performed the treatment
        List<Staff> allStaff = manager.getAllStaff();
        cmbStaff = new JComboBox<>(allStaff.toArray(new Staff[0]));
        for (int i = 0; i < cmbStaff.getItemCount(); i++) {
            if (cmbStaff.getItemAt(i).getStaffId().equals(record.getPerformedBy().getStaffId())) {
                cmbStaff.setSelectedIndex(i);
                break;
            }
        }
        cmbStaff.addActionListener(e -> applyRolePermissions());

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
        addRow(pnlForm, 1, "Performed by", cmbStaff);
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

    // Adjusts input fields based on the selected staff member's clearance
    private void applyRolePermissions() {
        Staff selectedStaff = (Staff) cmbStaff.getSelectedItem();
        if (selectedStaff == null) return;

        boolean canDiagnose = selectedStaff.canDiagnose();
        boolean canPrescribe = selectedStaff.canPrescribeMedication();

        txtDiagnosis.setEnabled(canDiagnose);
        if (!canDiagnose) {
            txtDiagnosis.setText("Basic care only");
        } else if (txtDiagnosis.getText().equals("Basic care only")) {
            txtDiagnosis.setText("");
        }

        txtMedication.setEnabled(canPrescribe);
        if (!canPrescribe) {
            txtMedication.setText("None");
        } else if (txtMedication.getText().equals("None")) {
            txtMedication.setText("");
        }
    }

    private void handleSave() {
        try {
            LocalDate treatmentDate = ValidationUtil.parseDate(txtTreatmentDate.getText());
            LocalDate followUpDate = null;
            if (!ValidationUtil.isBlank(txtFollowUpDate.getText())) {
                followUpDate = ValidationUtil.parseDate(txtFollowUpDate.getText());
            }

            Staff selectedStaff = (Staff) cmbStaff.getSelectedItem();

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