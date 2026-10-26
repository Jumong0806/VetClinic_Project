package vetclinic.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import vetclinic.model.Animal;
import vetclinic.model.Treatable;
import vetclinic.model.TreatmentRecord;

/**
 * HistoryDialog - shows ALL treatment records of one patient in a table (newest first).
 * Styled to match NotificationDialog / TreatmentFormDialog (undecorated, custom header,
 * styled table, bordered footer).
 * Owner: Ben
 *
 * OOP concept: INTERFACE. The dialog takes a Treatable, so it works with ANY class that has a treatment history.
 */
public class HistoryDialog extends JDialog {

    private Treatable patient;
    private JTable tblHistory;
    private JButton btnClose;

    // For dragging the undecorated window
    private int pX, pY;

    // Same palette as NotificationDialog
    private final Color BORDER_COLOR = new Color(200, 204, 212);
    private final Color BG_COLOR = Color.WHITE;
    private final Color TEXT_DARK = new Color(30, 41, 59);
    private final Color HEADER_BG = new Color(248, 250, 252);
    private final Color ACCENT_BLUE = new Color(66, 103, 178);   // patient subtitle

    public HistoryDialog(JFrame parent, Treatable patient) {
        super(parent, "", true);
        this.patient = patient;

        setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new LineBorder(BORDER_COLOR, 1));

        mainPanel.add(createCustomHeader(), BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
        loadHistory();

        setSize(900, 420);
        setLocationRelativeTo(parent);
    }

    // --- 1. Custom Header: title + blue "Patient: Name (ID - Species)" subtitle + X ---
    private JPanel createCustomHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BG_COLOR);

        JLabel lblTitle = new JLabel("Treatment History");
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSubtitle = new JLabel(buildPatientLabel());
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(ACCENT_BLUE);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblSubtitle);

        JLabel closeBtn = new JLabel("X");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        closeBtn.setForeground(new Color(150, 150, 150));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });

        headerPanel.add(titlePanel, BorderLayout.WEST);
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

    // Builds "Patient: Mochi (A004 - Dog)". Falls back to the ID alone for non-Animal Treatables.
    private String buildPatientLabel() {
        if (patient instanceof Animal) {
            Animal animal = (Animal) patient;
            return "Patient: " + animal.getName() + " (" + animal.getAnimalId() + " - " + animal.getSpecies() + ")";
        }
        return "Patient: " + patient.getAnimalId();
    }

    // --- 2. Table Panel with Uniform Styling ---
    private JPanel createTablePanel() {
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(BG_COLOR);
        tableContainer.setBorder(new EmptyBorder(16, 24, 16, 24));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Record ID", "Date", "Diagnosis", "Treatment", "Medication", "Follow-up", "Performed By"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblHistory = new JTable(model);
        tblHistory.setRowHeight(56);   // tall enough for 3 wrapped lines
        tblHistory.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tblHistory.setForeground(TEXT_DARK);
        tblHistory.setSelectionBackground(new Color(0xDBEAFE));
        tblHistory.setSelectionForeground(TEXT_DARK);
        tblHistory.setShowVerticalLines(true);
        tblHistory.setGridColor(new Color(226, 232, 240));
        tblHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header styling (same as NotificationDialog, text centered per the prototype)
        JTableHeader header = tblHistory.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(HEADER_BG);
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 40));
        header.setResizingAllowed(false);
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // Set preferred widths for the seven columns.
        int[] widths = {110, 120, 170, 170, 140, 120, 150};

        for (int i = 0; i < widths.length; i++) {
            tblHistory.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

// Let the columns adjust to the available table width.
        tblHistory.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Centered, padded, word-wrapping cell renderer
        WrapCenterRenderer renderer = new WrapCenterRenderer();
        for (int i = 0; i < tblHistory.getColumnCount(); i++) {
            tblHistory.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scrollPane = new JScrollPane(tblHistory);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(BG_COLOR);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        return tableContainer;
    }

    // Centers cell text consistently beneath each column header.
    private class WrapCenterRenderer extends DefaultTableCellRenderer {

        public WrapCenterRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);

            return this;
        }
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // --- 3. Footer (Close only, history is read-only) ---
    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        footerPanel.setBackground(BG_COLOR);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        btnClose = new JButton("Close");
        styleSecondaryButton(btnClose);
        btnClose.addActionListener(e -> handleClose());
        footerPanel.add(btnClose);

        getRootPane().setDefaultButton(btnClose);
        return footerPanel;
    }

    // Fills the table from the patient's history, newest record first.
    private void loadHistory() {
        DefaultTableModel model = (DefaultTableModel) tblHistory.getModel();
        model.setRowCount(0);
        List<TreatmentRecord> history = patient.getTreatmentHistory();
        for (int i = history.size() - 1; i >= 0; i--) {   // go from the last record to the first
            TreatmentRecord record = history.get(i);
            String followUp = (record.getFollowUpDate() == null) ? "---" : record.getFollowUpDate().toString();
            model.addRow(new Object[]{record.getRecordId(), record.getTreatmentDate().toString(), record.getDiagnosis(),
                    record.getTreatment(), record.getMedication(), followUp, record.getPerformedBy().getStaffName()});
        }
    }

    private void handleClose() {
        dispose();
    }

    // --- Styling Helper (identical to NotificationDialog) ---
    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(TEXT_DARK);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}