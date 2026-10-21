package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import vetclinic.model.Animal;
import vetclinic.service.ClinicManager;

public class NotificationDialog extends JDialog {

    private ClinicManager manager;
    private JTable tblNotifications;
    private JButton btnViewPatient;
    private JButton btnResolve;
    private JButton btnClose;

    public NotificationDialog(JFrame parent, ClinicManager manager) {
        super(parent, "Notifications", true);
        this.manager = manager;

        setLayout(new BorderLayout(8, 8));

        // ADDED: "Record ID" as the first column
        DefaultTableModel model = new DefaultTableModel(new String[]{"Record ID", "Patient ID", "Name", "Owner", "Next Checkup", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblNotifications = new JTable(model);

        // ADDED: Hide the "Record ID" column so the user doesn't see it, but the data is still there for the system
        tblNotifications.getColumnModel().getColumn(0).setMinWidth(0);
        tblNotifications.getColumnModel().getColumn(0).setMaxWidth(0);
        tblNotifications.getColumnModel().getColumn(0).setWidth(0);

        add(new JScrollPane(tblNotifications), BorderLayout.CENTER);

        btnViewPatient = new JButton("View Patient");
        btnResolve = new JButton("Mark Resolved"); // ADDED BUTTON
        btnClose = new JButton("Close");

        btnViewPatient.addActionListener(e -> handleViewPatient());
        btnResolve.addActionListener(e -> handleResolve()); // ADDED ACTION
        btnClose.addActionListener(e -> handleClose());

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.add(btnResolve);
        pnlButtons.add(btnViewPatient);
        pnlButtons.add(btnClose);
        add(pnlButtons, BorderLayout.SOUTH);

        loadNotifications();
        setSize(640, 400);
        setLocationRelativeTo(parent);
    }

    private void loadNotifications() {
        DefaultTableModel model = (DefaultTableModel) tblNotifications.getModel();
        model.setRowCount(0);

        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        for (Animal animal : manager.getAllAnimals()) {
            for (vetclinic.model.TreatmentRecord record : animal.getTreatmentHistory()) {
                LocalDate followUp = record.getFollowUpDate();

                if (followUp != null) {
                    if (!followUp.isAfter(today)) {
                        String status = followUp.isBefore(today) ? "Overdue" : "Due today";
                        model.addRow(new Object[]{
                                record.getRecordId(), // ADDED: Store the exact treatment record ID
                                animal.getAnimalId(),
                                animal.getName(),
                                animal.getOwner().getOwnerName(),
                                followUp.toString(),
                                status
                        });
                    } else if (!followUp.isAfter(nextWeek)) {
                        model.addRow(new Object[]{
                                record.getRecordId(), // ADDED: Store the exact treatment record ID
                                animal.getAnimalId(),
                                animal.getName(),
                                animal.getOwner().getOwnerName(),
                                followUp.toString(),
                                "Upcoming (7 days)"
                        });
                    }
                }
            }
        }
    }

    // ADDED: Logic to resolve the notification
    private void handleResolve() {
        int row = tblNotifications.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a notification to resolve.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Retrieve the hidden Record ID from column 0
        String recordId = (String) tblNotifications.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark this follow-up as completed? It will be removed from the notifications.",
                "Confirm Resolution", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            manager.resolveFollowUp(recordId); // Tell manager to clear the date
            loadNotifications(); // Refresh the table so it disappears
        }
    }

    private void handleViewPatient() {
        int row = tblNotifications.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Notice we now pull from column 1 (Patient ID) because Record ID is column 0
        Animal animal = manager.findAnimalById((String) tblNotifications.getValueAt(row, 1));
        if (animal != null) {
            JOptionPane.showMessageDialog(this, animal.getSummary() + "\n\nCare note: " + animal.getCareNote(),
                    "Patient", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleClose() {
        dispose();
    }
}