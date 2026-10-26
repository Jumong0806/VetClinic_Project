package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import vetclinic.exception.StorageException;
import vetclinic.model.Animal;
import vetclinic.model.TreatmentRecord;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;
import vetclinic.util.ImageUtil;

/**
 * TreatmentPanel - the Treatments page: search, table, and "Selected Record Details".
 */
public class TreatmentPanel extends JPanel {

    // --- Core Dependencies ---
    private ClinicManager manager;
    private AuthService authService;

    // --- UI Components ---
    private JTextField txtSearch;
    private final String SEARCH_PLACEHOLDER = "Search by record ID, patient ID, diagnosis, or treatment...";
    private JComboBox<String> cmbDateFilter;
    private JTable tblTreatments;

    private JButton btnUpdate;
    private JButton btnDelete;

    private JLabel lblTitle;
    private JPanel pnlContainer;       // CardLayout container to switch views
    private JPanel pnlDetailsContent;  // Active data view
    private JPanel pnlEmptyState;      // Empty state view

    // Detail Labels (populated when a table row is clicked)
    private JLabel lblDetailRecordId;
    private JLabel lblDetailDate;
    private JLabel lblDetailPatient;
    private JLabel lblDetailDiagnosis;
    private JLabel lblDetailTreatment;
    private JLabel lblDetailMedication;
    private JLabel lblDetailFollowUp;
    private JLabel lblDetailPerformedBy;

    // --- Theme Constants ---
    private final Color TEXT_DARK = new Color(0x1E293B);
    private final Color TEXT_MUTED = new Color(0x64748B);
    private final Color BORDER_COLOR = new Color(0xCBD5E1);
    private final Color BG_COLOR = new Color(0xF8FAFC);

    // Constructor: Assembles the main layout vertically (North, Center, South)
    public TreatmentPanel(ClinicManager manager, AuthService authService) {
        this.manager = manager;
        this.authService = authService;

        setLayout(new BorderLayout(0, 16));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));

        buildSearchBar();
        buildTablePanel();
        buildDetailsPanel();
        clearDetails(); // Forces the "empty state" on initial load
    }

    // Constructs the top section containing the text search and date filter
    private void buildSearchBar() {
        JPanel pnlSearchBox = new JPanel(new BorderLayout(8, 0));
        pnlSearchBox.setBackground(Color.WHITE);
        pnlSearchBox.setPreferredSize(new Dimension(0, 42));
        pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JLabel lblSearchIcon = new JLabel(ImageUtil.load("/images/search.png", 18, 18));

        txtSearch = new JTextField(SEARCH_PLACEHOLDER);
        txtSearch.setBorder(null);
        txtSearch.setForeground(TEXT_MUTED);
        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));

        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(SEARCH_PLACEHOLDER)) {
                    txtSearch.setText("");
                    txtSearch.setForeground(TEXT_DARK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText(SEARCH_PLACEHOLDER);
                    txtSearch.setForeground(TEXT_MUTED);
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { handleSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { handleSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { handleSearch(); }
        });

        pnlSearchBox.add(lblSearchIcon, BorderLayout.WEST);
        pnlSearchBox.add(txtSearch, BorderLayout.CENTER);

        cmbDateFilter = new JComboBox<>(new String[]{"All Time", "Today", "Last 7 Days", "Last 30 Days"});
        cmbDateFilter.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cmbDateFilter.setBackground(Color.WHITE);
        cmbDateFilter.setForeground(TEXT_DARK);
        cmbDateFilter.setFocusable(false);
        cmbDateFilter.setPreferredSize(new Dimension(140, 42));
        cmbDateFilter.setBorder(null);

        if (cmbDateFilter.getEditor().getEditorComponent() instanceof JComponent) {
            ((JComponent) cmbDateFilter.getEditor().getEditorComponent()).setBorder(null);
            ((JComponent) cmbDateFilter.getEditor().getEditorComponent()).setOpaque(false);
        }

        cmbDateFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
                lbl.setBackground(isSelected ? new Color(0xDBEAFE) : Color.WHITE);
                lbl.setForeground(TEXT_DARK);
                return lbl;
            }
        });
        cmbDateFilter.addActionListener(e -> handleSearch());

        JPanel pnlFilterBox = new JPanel(new BorderLayout());
        pnlFilterBox.setBackground(Color.WHITE);
        pnlFilterBox.setPreferredSize(new Dimension(140, 42));
        pnlFilterBox.add(cmbDateFilter, BorderLayout.CENTER);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlRight.setOpaque(false);
        pnlRight.add(pnlFilterBox);

        JPanel pnlTop = new JPanel(new BorderLayout(8, 0));
        pnlTop.setOpaque(false);
        pnlTop.add(pnlSearchBox, BorderLayout.CENTER);
        pnlTop.add(pnlRight, BorderLayout.EAST);

        add(pnlTop, BorderLayout.NORTH);
    }

    private void handleSearch() {
        String query = txtSearch.getText();
        if (query.equals(SEARCH_PLACEHOLDER)) {
            query = "";
        }

        List<TreatmentRecord> searched = manager.searchTreatments(query);
        List<TreatmentRecord> filtered = manager.filterTreatmentsByDate((String) cmbDateFilter.getSelectedItem());

        List<TreatmentRecord> both = new java.util.ArrayList<>();
        for (TreatmentRecord record : searched) {
            if (filtered.contains(record)) {
                both.add(record);
            }
        }
        refreshTable(both);
    }

    private void buildTablePanel() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Record ID", "Date", "Patient ID", "Patient Name", "Diagnosis", "Treatment", "Medication", "Follow-up", "Performed By"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblTreatments = new JTable(model);
        tblTreatments.setRowHeight(38);
        tblTreatments.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tblTreatments.setForeground(TEXT_DARK);
        tblTreatments.setSelectionBackground(new Color(0xDBEAFE));
        tblTreatments.setSelectionForeground(TEXT_DARK);
        tblTreatments.setShowVerticalLines(true);
        tblTreatments.setGridColor(new Color(0xE2E8F0));

        tblTreatments.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tblTreatments.getTableHeader().setBackground(new Color(0xF8FAFC));
        tblTreatments.getTableHeader().setForeground(TEXT_DARK);
        tblTreatments.getTableHeader().setPreferredSize(new Dimension(0, 40));

        tblTreatments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        javax.swing.table.DefaultTableCellRenderer paddingRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        };

        for (int i = 0; i < tblTreatments.getColumnCount(); i++) {
            tblTreatments.getColumnModel().getColumn(i).setCellRenderer(paddingRenderer);
        }

        tblTreatments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSelectionChanged();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblTreatments);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);
    }

    // Constructs the bottom section containing the dynamic details cards and action buttons
    private void buildDetailsPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setOpaque(false);
        pnlWrapper.setPreferredSize(new Dimension(0, 210));

        JPanel pnlDetails = new JPanel(new BorderLayout(0, 12));
        pnlDetails.setBackground(Color.WHITE);
        pnlDetails.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 24, 16, 24)
        ));

        lblTitle = new JLabel("Selected Record Details");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(new Color(0x1E3A8A));
        pnlDetails.add(lblTitle, BorderLayout.NORTH);

        // Empty State UI
        JPanel pnlEmptyContent = new JPanel();
        pnlEmptyContent.setLayout(new BoxLayout(pnlEmptyContent, BoxLayout.Y_AXIS));
        pnlEmptyContent.setOpaque(false);

        JLabel lblEmptyIcon = new JLabel(ImageUtil.load("/images/treatments-empty-final.png", 100, 100), SwingConstants.CENTER);
        lblEmptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyTitle = new JLabel("No Record Selected", SwingConstants.CENTER);
        lblEmptyTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblEmptyTitle.setForeground(TEXT_DARK);
        lblEmptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptySubtitle = new JLabel("Select a treatment from the table to view their details and available actions.", SwingConstants.CENTER);
        lblEmptySubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblEmptySubtitle.setForeground(TEXT_MUTED);
        lblEmptySubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlEmptyContent.add(lblEmptyIcon);
        pnlEmptyContent.add(Box.createVerticalStrut(3));
        pnlEmptyContent.add(lblEmptyTitle);
        pnlEmptyContent.add(Box.createVerticalStrut(4));
        pnlEmptyContent.add(lblEmptySubtitle);

        pnlEmptyState = new JPanel(new GridBagLayout());
        pnlEmptyState.setOpaque(false);
        pnlEmptyState.add(pnlEmptyContent, new GridBagConstraints());

        // --- Active Data State UI (Stable 4-Column Layout via BorderLayout) ---
        pnlDetailsContent = new JPanel(new BorderLayout(16, 0));
        pnlDetailsContent.setOpaque(false);

        // 1. LEFT ZONE: Record ID & Date stack with locked width boundary
        JPanel pnlID = new JPanel(new GridBagLayout());
        pnlID.setOpaque(false);
        pnlID.setPreferredSize(new Dimension(240, 100));

        lblDetailRecordId = new JLabel("-");
        lblDetailRecordId.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblDetailRecordId.setForeground(TEXT_DARK);
        lblDetailRecordId.setHorizontalAlignment(SwingConstants.CENTER);
        lblDetailRecordId.setPreferredSize(new Dimension(220, 26));

        lblDetailDate = new JLabel("-");
        lblDetailDate.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDetailDate.setForeground(TEXT_MUTED);
        lblDetailDate.setHorizontalAlignment(SwingConstants.CENTER);
        lblDetailDate.setPreferredSize(new Dimension(220, 20));

        GridBagConstraints gbcId = new GridBagConstraints();
        gbcId.gridx = 0; gbcId.gridy = 0;
        gbcId.insets = new Insets(0, 0, 4, 0);
        pnlID.add(lblDetailRecordId, gbcId);

        gbcId.gridy = 1;
        gbcId.insets = new Insets(0, 0, 0, 0);
        pnlID.add(lblDetailDate, gbcId);

        // Instantiate labels before passing them to the card helper
        lblDetailPatient = new JLabel("-");
        lblDetailPerformedBy = new JLabel("-");
        lblDetailFollowUp = new JLabel("-");
        lblDetailDiagnosis = new JLabel("-");
        lblDetailTreatment = new JLabel("-");
        lblDetailMedication = new JLabel("-");

        // 2. CENTER ZONE: Patient Info & Medical Info Cards (Equal sizing via GridLayout)
        JPanel pnlCard1 = createInfoCard(
                "Patient & Staff Info",
                new String[]{"Patient:", "Performed By:", "Follow-up:"},
                new JLabel[]{lblDetailPatient, lblDetailPerformedBy, lblDetailFollowUp}
        );

        JPanel pnlCard2 = createInfoCard(
                "Medical Details",
                new String[]{"Diagnosis:", "Treatment:", "Medication:"},
                new JLabel[]{lblDetailDiagnosis, lblDetailTreatment, lblDetailMedication}
        );

        JPanel pnlCardsWrapper = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlCardsWrapper.setOpaque(false);
        pnlCardsWrapper.add(pnlCard1);
        pnlCardsWrapper.add(pnlCard2);

        // 3. RIGHT ZONE: Action Buttons Stack (Update and Delete)
        JPanel pnlButtonsStack = new JPanel(new GridLayout(2, 1, 0, 12));
        pnlButtonsStack.setOpaque(false);

        btnUpdate = createOutlineButton("\u270E Update", new Color(0x2563EB), new Color(0xBFDBFE), new Color(0xEFF6FF));
        btnDelete = createOutlineButton("\uD83D\uDDD1 Delete", new Color(0xDC2626), new Color(0xFECACA), new Color(0xFEF2F2));

        btnUpdate.addActionListener(e -> handleUpdateTreatment());
        btnDelete.addActionListener(e -> handleDeleteTreatment());

        pnlButtonsStack.add(btnUpdate);
        pnlButtonsStack.add(btnDelete);

        JPanel pnlBtnWrapper = new JPanel(new GridBagLayout());
        pnlBtnWrapper.setOpaque(false);
        pnlBtnWrapper.setPreferredSize(new Dimension(150, 100));
        pnlBtnWrapper.add(pnlButtonsStack, new GridBagConstraints());

        // Assemble the 4-column layout securely into BorderLayout regions
        pnlDetailsContent.add(pnlID, BorderLayout.WEST);
        pnlDetailsContent.add(pnlCardsWrapper, BorderLayout.CENTER);
        pnlDetailsContent.add(pnlBtnWrapper, BorderLayout.EAST);

        // --- CardLayout Container to smoothly swap between Empty State and Active Data ---
        pnlContainer = new JPanel(new CardLayout());
        pnlContainer.setOpaque(false);
        pnlContainer.add(pnlEmptyState, "EMPTY");
        pnlContainer.add(pnlDetailsContent, "DATA");

        pnlDetails.add(pnlContainer, BorderLayout.CENTER);
        pnlWrapper.add(pnlDetails, BorderLayout.CENTER);
        add(pnlWrapper, BorderLayout.SOUTH);
    }

    // Helper Method: Dynamically builds a uniform, bordered inner card with a title and key-value pairs
    private JPanel createInfoCard(String title, String[] captions, JLabel[] values) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblCardTitle = new JLabel(title);
        lblCardTitle.setForeground(new Color(0x1E3A8A));
        lblCardTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        card.add(lblCardTitle, BorderLayout.NORTH);

        JPanel innerGrid = new JPanel(new GridBagLayout());
        innerGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 0, 3, 16);

        for (int i = 0; i < captions.length; i++) {
            JLabel lblCaption = new JLabel(captions[i]);
            lblCaption.setForeground(new Color(0x475569));
            lblCaption.setFont(new Font("SansSerif", Font.PLAIN, 13));

            values[i].setForeground(TEXT_DARK);
            values[i].setFont(new Font("SansSerif", Font.PLAIN, 13));

            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            innerGrid.add(lblCaption, gbc);

            gbc.gridx = 1;
            gbc.gridy = i;
            gbc.weightx = 1.0;
            innerGrid.add(values[i], gbc);
        }

        card.add(innerGrid, BorderLayout.CENTER);
        return card;
    }

    // Helper Method: Standardizes button dimensions to match StaffPanel exactly
    private JButton createOutlineButton(String text, Color textColor, Color borderColor, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(textColor);
        btn.setBackground(bgColor);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(130, 34));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void refreshTable(List<TreatmentRecord> records) {
        DefaultTableModel model = (DefaultTableModel) tblTreatments.getModel();
        model.setRowCount(0);
        for (TreatmentRecord record : records) {
            Animal patient = manager.findAnimalById(record.getAnimalId());
            String patientName = (patient != null) ? patient.getName() : "Unknown";
            String followUp = (record.getFollowUpDate() == null) ? "---" : record.getFollowUpDate().toString();

            model.addRow(new Object[]{
                    record.getRecordId(),
                    record.getTreatmentDate().toString(),
                    record.getAnimalId(),
                    patientName,
                    record.getDiagnosis(),
                    record.getTreatment(),
                    record.getMedication().equals("None") ? "---" : record.getMedication(),
                    followUp,
                    record.getPerformedBy().getStaffName()
            });
        }
        clearDetails();
    }

    private void showDetails(TreatmentRecord record) {
        lblTitle.setVisible(true);
        CardLayout cl = (CardLayout) pnlContainer.getLayout();
        cl.show(pnlContainer, "DATA");

        lblDetailRecordId.setText(record.getRecordId());
        lblDetailDate.setText(record.getTreatmentDate().toString());

        Animal patient = manager.findAnimalById(record.getAnimalId());
        String patientStr = (patient != null) ? patient.getName() + " (" + patient.getAnimalId() + ")" : record.getAnimalId();
        lblDetailPatient.setText(patientStr);

        lblDetailDiagnosis.setText(record.getDiagnosis());
        lblDetailTreatment.setText(record.getTreatment());
        lblDetailMedication.setText(record.getMedication().equals("None") ? "None" : record.getMedication());
        lblDetailFollowUp.setText(record.getFollowUpDate() == null ? "None" : record.getFollowUpDate().toString());
        lblDetailPerformedBy.setText(record.getPerformedBy().getStaffName());

        setActionsEnabled(true);
    }

    private void clearDetails() {
        if (lblTitle != null) {
            lblTitle.setVisible(false);
        }
        CardLayout cl = (CardLayout) pnlContainer.getLayout();
        cl.show(pnlContainer, "EMPTY");
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

    private void handleUpdateTreatment() {
        TreatmentRecord selected = getSelectedRecord();
        if (selected == null) return;
        new TreatmentFormDialog(getParentFrame(), manager, selected).setVisible(true);
        handleSearch();
    }

    // Prompts for confirmation and deletes the selected treatment record from the database manager.
    private void handleDeleteTreatment() {
        TreatmentRecord selected = getSelectedRecord();
        if (selected == null) return;

        // Enhanced Confirmation Pop-up with warning icon and explicit record ID check
        int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Treatment Record ID: " + selected.getRecordId() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) return;

        try {
            manager.removeTreatment(selected.getRecordId());
            manager.saveAll();

            // Success Confirmation Pop-up
            JOptionPane.showMessageDialog(this, "Treatment record deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException | StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        handleSearch();
    }

    private JFrame getParentFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }
}