package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import vetclinic.model.Treatable;
import vetclinic.model.TreatmentRecord;

/**
 * HistoryDialog - shows ALL treatment records of one patient in a table (newest first).
 * This is the screen that solves the problem statement: the history is found in seconds.
 * Owner: Ben
 *
 * Swing concepts: modal JDialog, BorderLayout, FlowLayout, JTable (cannot be edited).
 * OOP concept: INTERFACE. The dialog takes a Treatable, so it works with ANY class that has a treatment history.
 */
public class HistoryDialog extends JDialog {

    private Treatable patient;
    private JTable tblHistory;
    private JButton btnClose;

    public HistoryDialog(JFrame parent, Treatable patient) {
        super(parent, "Treatment History", true);
        this.patient = patient;

        setLayout(new BorderLayout(8, 8));
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Record ID", "Date", "Diagnosis", "Treatment", "Medication", "Follow-up", "Performed By"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHistory = new JTable(model);
        tblHistory.setRowHeight(26);
        JScrollPane scroll = new JScrollPane(tblHistory);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(scroll, BorderLayout.CENTER);

        btnClose = new JButton("Close");
        btnClose.addActionListener(e -> handleClose());
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlButtons.add(btnClose);
        add(pnlButtons, BorderLayout.SOUTH);

        loadHistory();
        setPreferredSize(new Dimension(900, 380));
        pack();
        setLocationRelativeTo(parent);
    }

    // Fills the table from the patient's history, newest record first.
    private void loadHistory() {
        DefaultTableModel model = (DefaultTableModel) tblHistory.getModel();
        model.setRowCount(0);
        List<TreatmentRecord> history = patient.getTreatmentHistory();
        for (int i = history.size() - 1; i >= 0; i--) {   // go from the last record to the first
            TreatmentRecord record = history.get(i);
            String followUp = (record.getFollowUpDate() == null) ? "-" : record.getFollowUpDate().toString();
            model.addRow(new Object[]{record.getRecordId(), record.getTreatmentDate().toString(), record.getDiagnosis(),
                    record.getTreatment(), record.getMedication(), followUp, record.getPerformedBy().getStaffName()});
        }
        setTitle("Treatment History - " + patient.getAnimalId() + " (" + history.size() + " records)");
    }

    private void handleClose() {
        dispose();
    }
}
