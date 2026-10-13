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

import vetclinic.exception.StorageException;
import vetclinic.model.Animal;
import vetclinic.model.TreatmentRecord;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;

/**
 * TreatmentPanel - the Treatments page: search, table, and "Selected Treatment Details".
 * Note: Add Treatment is restricted to the Patients page.
 */
public class TreatmentPanel extends JPanel {

    private ClinicManager manager;
    private AuthService authService;
    private JTextField txtSearch;
    private JTable tblTreatments;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JLabel lblNoSelection;
    private JLabel lblDetailRecordId;
    private JLabel lblDetailDate;
    private JLabel lblDetailPatient;
    private JLabel lblDetailDiagnosis;
    private JLabel lblDetailTreatment;
    private JLabel lblDetailMedication;
    private JLabel lblDetailFollowUp;
    private JLabel lblDetailPerformedBy;

    public TreatmentPanel(ClinicManager manager, AuthService authService) {
        this.manager = manager;
        this.authService = authService;

        setLayout(new BorderLayout(0, 12));
        setBackground(new Color(0xF4F7FC));
        setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));

        buildSearchBar();
        buildTablePanel();
        buildDetailsPanel();
        clearDetails();
    }

    private void buildSearchBar() {
        JPanel pnlTitle = new JPanel();
        pnlTitle.setOpaque(false);
        pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Treatments");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitle.setForeground(new Color(0x1F2A44));
        JLabel lblSubtitle = new JLabel("View and manage all clinic treatment records");
        lblSubtitle.setForeground(new Color(0x6B7A99));
        pnlTitle.add(lblTitle);
        pnlTitle.add(lblSubtitle);

        txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createTitledBorder("Search by record ID, patient ID, diagnosis, or treatment"));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { handleSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { handleSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { handleSearch(); }
        });

        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.add(txtSearch, BorderLayout.CENTER);

        JPanel pnlTop = new JPanel(new BorderLayout(0, 10));
        pnlTop.setOpaque(false);
        pnlTop.add(pnlTitle, BorderLayout.NORTH);
        pnlTop.add(pnlSearch, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);
    }

    private void buildTablePanel() {
        // Kept "Patient ID", added "Patient Name", "Treatment", and "Medication"
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Record ID", "Date", "Patient ID", "Patient Name", "Diagnosis", "Treatment", "Medication", "Performed By"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblTreatments = new JTable(model);
        tblTreatments.setRowHeight(26);
        tblTreatments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTreatments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSelectionChanged();
            }
        });

        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(Color.WHITE);
        pnlTable.setBorder(BorderFactory.createTitledBorder("All Treatment Records"));
        pnlTable.add(new JScrollPane(tblTreatments), BorderLayout.CENTER);
        add(pnlTable, BorderLayout.CENTER);
    }

    private void buildDetailsPanel() {
        lblNoSelection = new JLabel("No Record Selected. Click a row in the table to see the details.");
        lblNoSelection.setForeground(new Color(0x6B7A99));

        lblDetailRecordId = new JLabel("-");
        lblDetailRecordId.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblDetailDate = new JLabel("-");
        lblDetailDate.setForeground(new Color(0x6B7A99));

        lblDetailPatient = new JLabel("-");
        lblDetailDiagnosis = new JLabel("-");
        lblDetailTreatment = new JLabel("-");
        lblDetailMedication = new JLabel("-");
        lblDetailFollowUp = new JLabel("-");
        lblDetailPerformedBy = new JLabel("-");

        JPanel pnlName = new JPanel();
        pnlName.setOpaque(false);
        pnlName.setLayout(new BoxLayout(pnlName, BoxLayout.Y_AXIS));
        pnlName.setPreferredSize(new Dimension(180, 0));
        pnlName.add(lblDetailRecordId);
        pnlName.add(lblDetailDate);

        JPanel pnlInfo = new JPanel(new GridLayout(3, 2, 8, 8));
        pnlInfo.setOpaque(false);
        addInfoRow(pnlInfo, "Patient", lblDetailPatient);
        addInfoRow(pnlInfo, "Diagnosis", lblDetailDiagnosis);
        addInfoRow(pnlInfo, "Treatment", lblDetailTreatment);
        addInfoRow(pnlInfo, "Medication", lblDetailMedication);
        addInfoRow(pnlInfo, "Follow-up", lblDetailFollowUp);
        addInfoRow(pnlInfo, "Performed By", lblDetailPerformedBy);

        btnUpdate = makeButton("Update Record", Color.WHITE, new Color(0x2563EB));
        btnDelete = makeButton("Delete Record", Color.WHITE, new Color(0xD93025));
        btnUpdate.addActionListener(e -> handleUpdateTreatment());
        btnDelete.addActionListener(e -> handleDeleteTreatment());

        JPanel pnlButtons = new JPanel(new GridLayout(2, 1, 8, 8));
        pnlButtons.setOpaque(false);
        pnlButtons.setPreferredSize(new Dimension(180, 0));
        pnlButtons.add(btnUpdate);
        pnlButtons.add(btnDelete);

        JPanel pnlDetails = new JPanel(new BorderLayout(16, 8));
        pnlDetails.setBackground(Color.WHITE);
        pnlDetails.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Selected Record Details"), BorderFactory.createEmptyBorder(6, 10, 10, 10)));
        pnlDetails.setPreferredSize(new Dimension(800, 190));
        pnlDetails.add(lblNoSelection, BorderLayout.NORTH);
        pnlDetails.add(pnlName, BorderLayout.WEST);
        pnlDetails.add(pnlInfo, BorderLayout.CENTER);
        pnlDetails.add(pnlButtons, BorderLayout.EAST);
        add(pnlDetails, BorderLayout.SOUTH);
    }

    public void refreshTable(List<TreatmentRecord> records) {
        DefaultTableModel model = (DefaultTableModel) tblTreatments.getModel();
        model.setRowCount(0);
        for (TreatmentRecord record : records) {
            // Find the animal object to safely extract its name
            Animal patient = manager.findAnimalById(record.getAnimalId());
            String patientName = (patient != null) ? patient.getName() : "Unknown";

            model.addRow(new Object[]{
                    record.getRecordId(),
                    record.getTreatmentDate().toString(),
                    record.getAnimalId(),
                    patientName,
                    record.getDiagnosis(),
                    record.getTreatment(),
                    record.getMedication(),
                    record.getPerformedBy().getStaffName()
            });
        }
        clearDetails();
    }

    private void showDetails(TreatmentRecord record) {
        lblNoSelection.setVisible(false);
        lblDetailRecordId.setText(record.getRecordId());
        lblDetailDate.setText(record.getTreatmentDate().toString());

        Animal patient = manager.findAnimalById(record.getAnimalId());
        String patientStr = (patient != null) ? patient.getName() + " (" + patient.getAnimalId() + ")" : record.getAnimalId();
        lblDetailPatient.setText(patientStr);

        lblDetailDiagnosis.setText(record.getDiagnosis());
        lblDetailTreatment.setText(record.getTreatment());
        lblDetailMedication.setText(record.getMedication());
        lblDetailFollowUp.setText(record.getFollowUpDate() == null ? "None" : record.getFollowUpDate().toString());
        lblDetailPerformedBy.setText(record.getPerformedBy().getStaffName());

        setActionsEnabled(true);
    }

    private void clearDetails() {
        lblNoSelection.setVisible(true);
        lblDetailRecordId.setText("-");
        lblDetailDate.setText("-");
        lblDetailPatient.setText("-");
        lblDetailDiagnosis.setText("-");
        lblDetailTreatment.setText("-");
        lblDetailMedication.setText("-");
        lblDetailFollowUp.setText("-");
        lblDetailPerformedBy.setText("-");
        setActionsEnabled(false);
    }

    private void setActionsEnabled(boolean enabled) {
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
    }

    private TreatmentRecord getSelectedRecord() {
        int row = tblTreatments.getSelectedRow();
        if (row == -1) return null;
        String recordId = (String) tblTreatments.getValueAt(row, 0);
        // Find record in list
        for (TreatmentRecord rec : manager.getAllTreatments()) {
            if (rec.getRecordId().equals(recordId)) return rec;
        }
        return null;
    }

    private void handleSelectionChanged() {
        TreatmentRecord selected = getSelectedRecord();
        if (selected == null) {
            clearDetails();
        } else {
            showDetails(selected);
        }
    }

    private void handleSearch() {
        List<TreatmentRecord> result = manager.searchTreatments(txtSearch.getText());
        refreshTable(result);
    }

    private void handleUpdateTreatment() {
        TreatmentRecord selected = getSelectedRecord();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a record first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new TreatmentFormDialog(getParentFrame(), manager, selected, authService.getCurrentStaff()).setVisible(true);
        handleSearch();
    }

    private void handleDeleteTreatment() {
        TreatmentRecord selected = getSelectedRecord();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a record first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete treatment record " + selected.getRecordId() + "?",
                "Delete Record", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        try {
            manager.removeTreatment(selected.getRecordId());
            manager.saveAll();
        } catch (IllegalArgumentException | StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        handleSearch();
    }

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