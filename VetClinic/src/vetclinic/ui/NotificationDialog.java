package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;

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

/**
 * NotificationDialog - PHASE 2. Lists the patients whose checkup is overdue, due today, or coming up in 7 days.
 * Owner: Unassigned (Phase 2)
 *
 * Swing concepts: modal JDialog, BorderLayout + FlowLayout, JTable inside a JScrollPane.
 */
public class NotificationDialog extends JDialog {

    private ClinicManager manager;
    private JTable tblNotifications;
    private JButton btnViewPatient;
    private JButton btnClose;

    public NotificationDialog(JFrame parent, ClinicManager manager) {
        super(parent, "Notifications", true);   // true = modal: the main window is locked until this closes
        this.manager = manager;

        setLayout(new BorderLayout(8, 8));
        // A table that the user cannot edit (isCellEditable returns false).
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Owner", "Next Checkup", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblNotifications = new JTable(model);
        add(new JScrollPane(tblNotifications), BorderLayout.CENTER);

        btnViewPatient = new JButton("View Patient");
        btnClose = new JButton("Close");
        btnViewPatient.addActionListener(e -> handleViewPatient());
        btnClose.addActionListener(e -> handleClose());
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.add(btnViewPatient);
        pnlButtons.add(btnClose);
        add(pnlButtons, BorderLayout.SOUTH);

        loadNotifications();
        setSize(640, 400);
        setLocationRelativeTo(parent);
    }

    // Fills the table with the due and the upcoming checkups.
    private void loadNotifications() {
        DefaultTableModel model = (DefaultTableModel) tblNotifications.getModel();
        model.setRowCount(0);
        LocalDate today = LocalDate.now();

        List<Animal> due = manager.getAnimalsDueForCheckup();
        for (Animal animal : due) {
            LocalDate next = animal.getNextCheckupDate();
            String status = next.isBefore(today) ? "Overdue" : "Due today";
            model.addRow(new Object[]{animal.getAnimalId(), animal.getName(), animal.getOwner().getOwnerName(), next.toString(), status});
        }
        for (Animal animal : manager.getUpcomingCheckups(7)) {
            model.addRow(new Object[]{animal.getAnimalId(), animal.getName(), animal.getOwner().getOwnerName(),
                    animal.getNextCheckupDate().toString(), "Upcoming (7 days)"});
        }
    }

    // Shows the summary of the selected patient.
    private void handleViewPatient() {
        int row = tblNotifications.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Animal animal = manager.findAnimalById((String) tblNotifications.getValueAt(row, 0));
        if (animal != null) {
            JOptionPane.showMessageDialog(this, animal.getSummary() + "\n\nCare note: " + animal.getCareNote(),
                    "Patient", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleClose() {
        dispose();
    }
}
