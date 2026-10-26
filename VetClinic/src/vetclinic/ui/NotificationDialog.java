package vetclinic.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import vetclinic.model.Animal;
import vetclinic.model.TreatmentRecord;
import vetclinic.service.ClinicManager;

public class NotificationDialog extends JDialog {

    private ClinicManager manager;
    private JTable tblNotifications;
    private JButton btnViewPatient;
    private JButton btnResolve;
    private JButton btnClose;

    // For dragging the undecorated window
    private int pX, pY;

    // Modern UI Colors matching your app's theme
    private final Color BORDER_COLOR = new Color(200, 204, 212);
    private final Color BG_COLOR = Color.WHITE;
    private final Color PRIMARY_BLUE = new Color(37, 99, 235); // 0x2563EB
    private final Color TEXT_DARK = new Color(30, 41, 59);      // 0x1E293B
    private final Color HEADER_BG = new Color(248, 250, 252);    // 0xF8FAFC

    public NotificationDialog(JFrame parent, ClinicManager manager) {
        super(parent, "", true);
        this.manager = manager;

        setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new LineBorder(BORDER_COLOR, 1));

        mainPanel.add(createCustomHeader(), BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
        loadNotifications();

        setSize(720, 440);
        setLocationRelativeTo(parent);
    }

    // --- 1. Custom Header ---
    private JPanel createCustomHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel lblTitle = new JLabel("Notifications (Checkup Reminders)");
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblTitle.setForeground(TEXT_DARK);

        JLabel closeBtn = new JLabel("✕");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        closeBtn.setForeground(new Color(150, 150, 150));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(closeBtn, BorderLayout.EAST);

        // Window dragging mechanics
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

    // --- 2. Table Panel with Uniform Styling ---
    private JPanel createTablePanel() {
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(BG_COLOR);
        tableContainer.setBorder(new EmptyBorder(16, 24, 16, 24));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Record ID", "Patient ID", "Name", "Owner", "Follow-up", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblNotifications = new JTable(model);
        tblNotifications.setRowHeight(38);
        tblNotifications.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tblNotifications.setForeground(TEXT_DARK);
        tblNotifications.setSelectionBackground(new Color(0xDBEAFE));
        tblNotifications.setSelectionForeground(TEXT_DARK);
        tblNotifications.setShowVerticalLines(true);
        tblNotifications.setGridColor(new Color(226, 232, 240));
        tblNotifications.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header styling
        JTableHeader header = tblNotifications.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(HEADER_BG);
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 40));
        header.setResizingAllowed(false);
        header.setReorderingAllowed(false);

        // Hide "Record ID" column (index 0)
        tblNotifications.getColumnModel().getColumn(0).setMinWidth(0);
        tblNotifications.getColumnModel().getColumn(0).setMaxWidth(0);
        tblNotifications.getColumnModel().getColumn(0).setWidth(0);

        // Cell padding renderer
        DefaultTableCellRenderer paddingRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        };

        for (int i = 1; i < tblNotifications.getColumnCount(); i++) {
            tblNotifications.getColumnModel().getColumn(i).setCellRenderer(paddingRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tblNotifications);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        return tableContainer;
    }

    // --- 3. Footer Buttons ---
    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        footerPanel.setBackground(BG_COLOR);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        btnResolve = new JButton("Mark Resolved");
        stylePrimaryButton(btnResolve);
        btnResolve.addActionListener(e -> handleResolve());

        btnViewPatient = new JButton("View Patient");
        styleSecondaryButton(btnViewPatient);
        btnViewPatient.addActionListener(e -> handleViewPatient());

        btnClose = new JButton("Close");
        styleSecondaryButton(btnClose);
        btnClose.addActionListener(e -> handleClose());

        footerPanel.add(btnResolve);
        footerPanel.add(btnViewPatient);
        footerPanel.add(btnClose);

        getRootPane().setDefaultButton(btnResolve); // Make Enter trigger "Mark Resolved"
        return footerPanel;
    }

    // --- Data Loading Logic (Fetching from Manager & Sorting by Urgency) ---
    private void loadNotifications() {
        DefaultTableModel model = (DefaultTableModel) tblNotifications.getModel();
        model.setRowCount(0);

        LocalDate today = LocalDate.now();
        LocalDate threeDays = today.plusDays(3);
        LocalDate sevenDays = today.plusDays(7);

        // Fetch follow-ups directly from ClinicManager (which queries MySQL/File storage loaded records)
        List<TreatmentRecord> followUps = manager.getUpcomingFollowUps();
        List<Object[]> rowsToSort = new ArrayList<>();

        for (TreatmentRecord record : followUps) {
            LocalDate followUp = record.getFollowUpDate();
            if (followUp == null) continue;

            // Only process follow-ups up to 7 days in the future
            if (followUp.isAfter(sevenDays)) {
                continue;
            }

            Animal animal = manager.findAnimalById(record.getAnimalId());
            if (animal == null) continue;

            String ownerName = (animal.getOwner() != null) ? animal.getOwner().getOwnerName() : "Unknown";

            String status;
            int urgencyRank; // 1 = Overdue, 2 = Due Today, 3 = Due in 3 Days, 4 = Upcoming (7 Days)

            if (followUp.isBefore(today)) {
                status = "Overdue";
                urgencyRank = 1;
            } else if (followUp.isEqual(today)) {
                status = "Due Today";
                urgencyRank = 2;
            } else if (!followUp.isAfter(threeDays)) {
                status = "Due in 3 Days";
                urgencyRank = 3;
            } else {
                status = "Upcoming (7 Days)";
                urgencyRank = 4;
            }

            rowsToSort.add(new Object[]{
                    urgencyRank,
                    record.getRecordId(),
                    animal.getAnimalId(),
                    animal.getName(),
                    ownerName,
                    followUp.toString(),
                    status
            });
        }

        // Sort strictly by urgency rank first, then by closest follow-up date string
        rowsToSort.sort((rowA, rowB) -> {
            int rankCompare = Integer.compare((Integer) rowA[0], (Integer) rowB[0]);
            if (rankCompare != 0) {
                return rankCompare;
            }
            return ((String) rowA[5]).compareTo((String) rowB[5]);
        });

        // Insert sorted rows into the table model
        for (Object[] row : rowsToSort) {
            model.addRow(new Object[]{
                    row[1], // Record ID (Hidden)
                    row[2], // Patient ID
                    row[3], // Name
                    row[4], // Owner
                    row[5], // Follow-up Date
                    row[6]  // Status
            });
        }
    }

    private void handleResolve() {
        int row = tblNotifications.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a notification to resolve.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String recordId = (String) tblNotifications.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark this follow-up as completed? It will be removed from the notifications.",
                "Confirm Resolution", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            manager.resolveFollowUp(recordId); // Clears date and triggers saveAll() in database
            loadNotifications(); // Refresh table view
        }
    }

    private void handleViewPatient() {
        int row = tblNotifications.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Animal animal = manager.findAnimalById((String) tblNotifications.getValueAt(row, 1));
        if (animal != null) {
            JOptionPane.showMessageDialog(this, animal.getSummary() + "\n\nCare note: " + animal.getCareNote(),
                    "Patient Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleClose() {
        dispose();
    }

    // --- Styling Helpers ---
    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY_BLUE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(TEXT_DARK);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}