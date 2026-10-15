package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import vetclinic.Main;
import vetclinic.model.Animal;
import vetclinic.model.TreatmentRecord;
import vetclinic.service.ClinicManager;

/**
 * DashboardPanel - the first page after login: three number cards and the Recent Treatments table.
 * Owner: Ben
 *
 * Swing concepts: BoxLayout (page), GridLayout (cards), BorderLayout (table card), JTable that cannot be edited.
 */
public class DashboardPanel extends JPanel {

    private ClinicManager manager;
    private JLabel lblTotalPatients;
    private JLabel lblTotalStaff;
    private JLabel lblTotalTreatments;
    private JTable tblRecentTreatments;
    private JLabel lblDueForCheckup;
    private JTable tblRecentPatients;
    private DonutChartPanel pnlSpeciesChart;

    public DashboardPanel(ClinicManager manager) {
        this.manager = manager;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));   // stacks the parts top to bottom
        setBackground(new Color(0xF4F7FC));
        setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));

        JLabel lblTitle = new JLabel("Dashboard");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitle.setForeground(new Color(0x1F2A44));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblSubtitle = new JLabel("A quick look at the clinic");
        lblSubtitle.setForeground(new Color(0x6B7A99));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblTitle);
        add(lblSubtitle);
        add(javax.swing.Box.createVerticalStrut(16));

        buildCards();
        add(javax.swing.Box.createVerticalStrut(16));
        buildRecentTreatmentsTable();
        add(javax.swing.Box.createVerticalStrut(16));
        buildAdvancedStats();
        refresh();
    }

    // The three number cards in one row (GridLayout: 1 row, 3 columns).
    private void buildCards() {
        lblTotalPatients = new JLabel("0");
        lblTotalStaff = new JLabel("0");
        lblTotalTreatments = new JLabel("0");

        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 16, 0));
        pnlCards.setOpaque(false);
        pnlCards.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        pnlCards.setPreferredSize(new Dimension(800, 110));
        pnlCards.add(makeCard("Total Patients", lblTotalPatients));
        pnlCards.add(makeCard("Total Staff", lblTotalStaff));
        pnlCards.add(makeCard("Total Treatments", lblTotalTreatments));
        add(pnlCards);
    }

    // The table with the newest treatment records.
    private void buildRecentTreatmentsTable() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Treatment Date", "Record ID", "Patient", "Diagnosis", "Performed By"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // the user cannot type inside the table
            }
        };
        tblRecentTreatments = new JTable(model);
        tblRecentTreatments.setRowHeight(26);

        JPanel pnlTable = new JPanel(new BorderLayout(0, 8));
        pnlTable.setBackground(Color.WHITE);
        pnlTable.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD0D7E5)), BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        pnlTable.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblCaption = new JLabel("Recent Treatments");
        lblCaption.setFont(new Font("SansSerif", Font.BOLD, 16));
        pnlTable.add(lblCaption, BorderLayout.NORTH);
        pnlTable.add(new JScrollPane(tblRecentTreatments), BorderLayout.CENTER);
        add(pnlTable);
    }

    // Reloads the numbers and the table. MainFrame calls this every time the page is shown.
    public void refresh() {
        lblTotalPatients.setText(String.valueOf(manager.getAllAnimals().size()));
        lblTotalStaff.setText(String.valueOf(manager.getAllStaff().size()));
        lblTotalTreatments.setText(String.valueOf(manager.getTotalTreatments()));

        DefaultTableModel model = (DefaultTableModel) tblRecentTreatments.getModel();
        model.setRowCount(0);
        for (TreatmentRecord record : manager.getRecentTreatments(8)) {
            Animal animal = manager.findAnimalById(record.getAnimalId());
            String patient = (animal == null) ? record.getAnimalId() : animal.getName() + " (" + animal.getAnimalId() + ")";
            model.addRow(new Object[]{record.getTreatmentDate().toString(), record.getRecordId(), patient,
                    record.getDiagnosis(), record.getPerformedBy().getStaffName()});
        }
        refreshAdvancedStats();
    }

    // Due for Checkup card + Recent Patients table + Patients by Species chart.
    private void buildAdvancedStats() {
        lblDueForCheckup = new JLabel("0");
        tblRecentPatients = new JTable(new DefaultTableModel(new String[]{"ID", "Name", "Species", "Registered"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        pnlSpeciesChart = new DonutChartPanel();
        pnlSpeciesChart.setBackground(Color.WHITE);

        JPanel pnlAdvanced = new JPanel(new GridLayout(1, 3, 16, 0));
        pnlAdvanced.setOpaque(false);
        pnlAdvanced.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlAdvanced.setPreferredSize(new Dimension(800, 180));
        pnlAdvanced.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        pnlAdvanced.add(makeCard("Due for Checkup", lblDueForCheckup));
        pnlAdvanced.add(new JScrollPane(tblRecentPatients));
        pnlAdvanced.add(pnlSpeciesChart);
        add(pnlAdvanced);
    }

    // fills the extra widgets.
    private void refreshAdvancedStats() {
        lblDueForCheckup.setText(String.valueOf(manager.getAnimalsDueForCheckup().size()));
        pnlSpeciesChart.setCounts(manager.getSpeciesCounts());
        DefaultTableModel model = (DefaultTableModel) tblRecentPatients.getModel();
        model.setRowCount(0);
        List<Animal> recent = manager.getRecentAnimals(5);
        for (Animal animal : recent) {
            model.addRow(new Object[]{animal.getAnimalId(), animal.getName(), animal.getSpecies(),
                    animal.getRegisteredDate().toString()});
        }
    }

    // Builds one white card: a small caption on top and a big number below.
    private JPanel makeCard(String caption, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD0D7E5)), BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        JLabel lblCaption = new JLabel(caption);
        lblCaption.setForeground(new Color(0x6B7A99));
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        valueLabel.setForeground(new Color(0x1F2A44));
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(lblCaption, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }
}
