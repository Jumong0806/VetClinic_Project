package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import vetclinic.model.Animal;
import vetclinic.model.TreatmentRecord;
import vetclinic.service.ClinicManager;
import vetclinic.util.ImageUtil;

/**
 * DashboardPanel - the first page after login:
 *   row 1 (NORTH)  : three number cards (patients, staff, treatments)
 *   row 2 (CENTER) : the Recent Treatments table
 *   row 3 (SOUTH)  : Due for Checkup card, Recent Patients table, Patients by Species chart
 * Owner: Ben
 *
 * Swing concepts: BorderLayout (page), GridLayout (row 1), GridBagLayout (row 3), custom painting,
 * JTable styled like the Treatments page, with equal-width columns.
 * OOP concepts: INHERITANCE (extends JPanel; the nested classes extend JPanel and JLabel) and ENCAPSULATION (all widgets are private; MainFrame only uses refresh()).
 */
public class DashboardPanel extends JPanel {

    // ---------------- theme (the same colors as the Treatments page, so every page looks uniform) ----------------
    private static final Color PAGE_BG     = new Color(0xF8FAFC);   // page background
    private static final Color CARD_BORDER = new Color(0xCBD5E1);   // thin line around every card
    private static final Color TEXT_DARK   = new Color(0x1E293B);   // numbers and table text
    private static final Color TEXT_MUTED  = new Color(0x64748B);   // small captions
    private static final Color TITLE_NAVY  = new Color(0x1E293B);   // card titles
    private static final Color GRID_COLOR  = new Color(0xE2E8F0);   // table lines
    private static final Color HEADER_BG   = new Color(0xF8FAFC);   // table header row (same as the Treatments table)
    private static final Color BADGE_BG    = new Color(0xE5F1FD);   // circle behind the card icons

    // How many rows each table shows. Change these two numbers to show more or fewer rows.
    private static final int RECENT_TREATMENT_ROWS = 7;
    private static final int RECENT_PATIENT_ROWS   = 5;

    // "Oct 8, 2026" style date. Locale.ENGLISH keeps the month names in English on any computer.
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    // ---------------- widgets that refresh() must update ----------------
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
        setLayout(new BorderLayout(0, 16));   // 16 px gap between the three rows
        setBackground(PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));   // same padding as the other pages

        buildCards();                    // NORTH
        buildRecentTreatmentsTable();    // CENTER (takes all the leftover height)
        buildAdvancedStats();            // SOUTH
        refresh();
    }

    // =====================================================================================
    //  ROW 1: the three number cards
    // =====================================================================================

    // GridLayout(1, 3, 16, 0) = 1 row, 3 columns, 16 px gap between columns. Every cell gets the same size.
    private void buildCards() {
        lblTotalPatients = new JLabel("0");
        lblTotalStaff = new JLabel("0");
        lblTotalTreatments = new JLabel("0");

        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 16, 0));
        pnlCards.setOpaque(false);                            // let the page color show through
        pnlCards.setPreferredSize(new Dimension(800, 100));   // BorderLayout NORTH only uses the HEIGHT of this
        pnlCards.add(makeCard("Total Patients", lblTotalPatients,
                cardIcon("/images/total-patients.png")));
        pnlCards.add(makeCard("Total Staff", lblTotalStaff,
                cardIcon("/images/total-staff.png")));
        pnlCards.add(makeCard("Total Treatments", lblTotalTreatments,
                cardIcon("/images/total-treatments.png")));
        add(pnlCards, BorderLayout.NORTH);
    }

    // Builds one card: [ round icon ] [ small caption on top, big number below ].
    private JPanel makeCard(String caption, JLabel valueLabel, Icon icon) {
        RoundedCard card = new RoundedCard(new BorderLayout(16, 0));   // 16 px gap between icon and text
        card.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        // A GridBagLayout with one child centers it and keeps its preferred size,
        // so the round badge is not stretched into an oval by the card's height.
        JPanel pnlIcon = new JPanel(new GridBagLayout());
        pnlIcon.setOpaque(false);
        pnlIcon.add(new IconBadge(icon));

        JLabel lblCaption = new JLabel(caption);
        lblCaption.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblCaption.setForeground(TEXT_MUTED);
        lblCaption.setAlignmentX(Component.LEFT_ALIGNMENT);   // BoxLayout needs every child to use the same alignment

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        valueLabel.setForeground(TEXT_DARK);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Vertical glue = stretchy empty space. One above and one below centers the two labels vertically.
        JPanel pnlText = new JPanel();
        pnlText.setOpaque(false);
        pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
        pnlText.add(Box.createVerticalGlue());
        pnlText.add(lblCaption);
        pnlText.add(valueLabel);
        pnlText.add(Box.createVerticalGlue());

        card.add(pnlIcon, BorderLayout.WEST);
        card.add(pnlText, BorderLayout.CENTER);
        return card;
    }

    // Loads a card icon.
    private Icon cardIcon(String path) {
        return ImageUtil.load(path, 32, 32);
    }

    // =====================================================================================
    //  ROW 2: Recent Treatments table
    // =====================================================================================

    // Same sizes as the Treatments page: 38 px rows, 40 px header.
    private void buildRecentTreatmentsTable() {
        tblRecentTreatments = makeTable(
                new String[]{"Treatment Date", "Record ID", "Patient", "Diagnosis", "Performed by"}, 38, 40, 13, 13);
        // 18 = title font size; rows may be between 28 and 50 px tall, whatever lets all 7 fit exactly.
        add(makeTableCard("Recent Treatments", tblRecentTreatments, 18, RECENT_TREATMENT_ROWS, 28, 50),
                BorderLayout.CENTER);
    }

    // =====================================================================================
    //  ROW 3: Due for Checkup + Recent Patients + Patients by Species
    // =====================================================================================

    private void buildAdvancedStats() {
        lblDueForCheckup = new JLabel("0");

        // --- left card: same style as the three number cards ---
        JPanel pnlDue = makeCard("Due for Checkup", lblDueForCheckup,
                cardIcon("/images/due-checkup.png"));

        // --- middle card: small table ---
        tblRecentPatients = makeTable(new String[]{"ID", "Name", "Species", "Registered Date"}, 24, 24, 11, 10);
        JPanel pnlPatients = makeTableCard("Recent Patients", tblRecentPatients, 12, RECENT_PATIENT_ROWS, 20, 30);

        // --- right section: donut chart ---
        pnlSpeciesChart = new DonutChartPanel();

        // Create the chart panel with a white background and matching border.
        JPanel pnlChart = new JPanel(new BorderLayout(0, 4));
        pnlChart.setBackground(Color.WHITE);
        pnlChart.setBorder(
                BorderFactory.createLineBorder(CARD_BORDER)
        );

        // Add padding inside the border, just like the Due for Checkup card.
        JPanel pnlChartContent = new JPanel(new BorderLayout(0, 4));
        pnlChartContent.setOpaque(false);
        pnlChartContent.setBorder(
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        );

        // Assemble the chart section.
        pnlChartContent.add(pnlSpeciesChart, BorderLayout.CENTER);
        pnlChart.add(pnlChartContent, BorderLayout.CENTER);

        // The three cards need DIFFERENT widths (the table is the widest), so GridLayout does not fit here.
        // GridBagLayout hands out the free width by "weightx" (25% / 43% / 32%). The preferred widths set in
        // addToRow() are deliberately small, so there is always free width to share and the ratio stays exact.
        JPanel pnlAdvanced = new JPanel(new GridBagLayout());
        pnlAdvanced.setOpaque(false);
        pnlAdvanced.setPreferredSize(new Dimension(800, 170));
        addToRow(pnlAdvanced, pnlDue,      0, 0.25, new Insets(0, 0, 0, 8));
        addToRow(pnlAdvanced, pnlPatients, 1, 0.43, new Insets(0, 8, 0, 8));
        addToRow(pnlAdvanced, pnlChart,    2, 0.32, new Insets(0, 8, 0, 0));
        add(pnlAdvanced, BorderLayout.SOUTH);
    }

    // Puts one card into the GridBagLayout row. "fill BOTH" = stretch the card to fill its whole cell.
    private void addToRow(JPanel row, JPanel card, int column, double weight, Insets gaps) {
        card.setPreferredSize(new Dimension((int) (weight * 400), 170));   // small on purpose, see buildAdvancedStats()
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.gridy = 0;
        gbc.weightx = weight;     // share of the extra width this card receives
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = gaps;        // space around the card (top, left, bottom, right)
        row.add(card, gbc);
    }

    // =====================================================================================
    //  Showing data
    // =====================================================================================

    // Reloads the numbers and the tables. MainFrame calls this every time the page is shown.
    public void refresh() {
        // Update the summary cards.
        lblTotalPatients.setText(String.valueOf(manager.getAllAnimals().size()));
        lblTotalStaff.setText(String.valueOf(manager.getAllStaff().size()));
        lblTotalTreatments.setText(String.valueOf(manager.getTotalTreatments()));

        // Get the table's existing model.
        DefaultTableModel model =
                (DefaultTableModel) tblRecentTreatments.getModel();

        // Remove the previously displayed treatment records.
        model.setRowCount(0);

        // Retrieve the newest treatments. The list is already ordered: the most recently ADDED one is first.
        List<TreatmentRecord> recentTreatments =
                manager.getRecentTreatments(RECENT_TREATMENT_ROWS);

        for (TreatmentRecord record : recentTreatments) {
            // Find the patient associated with this treatment.
            Animal animal = manager.findAnimalById(record.getAnimalId());

            // Display the patient's name, or the ID if the patient cannot be found.
            String patient = (animal == null)
                    ? record.getAnimalId()
                    : animal.getName();

            // Add the treatment to the dashboard table.
            model.addRow(new Object[]{
                    record.getTreatmentDate().format(DATE_FORMAT),
                    record.getRecordId(),
                    patient,
                    record.getDiagnosis(),
                    record.getPerformedBy().getStaffName()
            });
        }

        // Update the bottom section of the dashboard.
        refreshAdvancedStats();
    }

    // Fills the bottom row: checkup number, Recent Patients table, and the chart.
    private void refreshAdvancedStats() {
        // AFTER: same list the Notifications dialog shows (overdue + due today + upcoming 7 days)
        lblDueForCheckup.setText(String.valueOf(manager.getDueTreatments().size()));
        pnlSpeciesChart.setCounts(manager.getSpeciesCounts());   // setCounts() also repaints the chart

        DefaultTableModel model = (DefaultTableModel) tblRecentPatients.getModel();
        model.setRowCount(0);
        List<Animal> recent = manager.getRecentAnimals(RECENT_PATIENT_ROWS);
        for (Animal animal : recent) {
            model.addRow(new Object[]{animal.getAnimalId(), animal.getName(), animal.getSpecies(),
                    animal.getRegisteredDate().toString()});
        }
    }

    // =====================================================================================
    //  Helpers used by both tables, so they look the same
    // =====================================================================================

    // Builds one table section: a title on top, then the table inside a scroll pane.
    //
    // The scroll pane is sized so that it holds EXACTLY maxRows rows (plus the header). So when the table is
    // full there is no scrollbar and no leftover strip; with fewer rows, the rest stays the gray table area.
    // The row height is calculated from the space this section really gets, so it also adapts when the
    // window is resized: it never goes below minRowHeight or above maxRowHeight.
    private JPanel makeTableCard(String title, JTable table, int titleSize, int maxRows,
                                 int minRowHeight, int maxRowHeight) {
        JLabel lblTitle = makeTitle(title, titleSize);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER));   // thin outline around the whole table area

        // The gray tone of the empty area under the last row (same as the Treatments table).
        Color tableAreaGray = new Color(0xEEEEEE);
        scroll.setBackground(tableAreaGray);
        scroll.getViewport().setBackground(tableAreaGray);

        // "section" is exactly as tall as its content: title, a 4 px gap, then the scroll pane.
        JPanel section = new JPanel(new BorderLayout(0, 4));
        section.setOpaque(false);
        section.add(lblTitle, BorderLayout.NORTH);
        section.add(scroll, BorderLayout.CENTER);

        // "holder" is the part the page layout stretches. The section sits at its top (NORTH) and keeps
        // its own height, so the holder's size tells us how much room we are allowed to use.
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(section, BorderLayout.NORTH);

        int headerHeight = table.getTableHeader().getPreferredSize().height;
        final int titleGap = 4;       // must match the gap in "section" above
        final int outline = 2;        // the scroll pane's 1 px line on the top and on the bottom

        // A ComponentAdapter is a listener with empty methods; we only fill in the one we need.
        // componentResized runs every time the holder gets a new size (first layout, window resize...).
        holder.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Room left for the rows = total height - title - gap - header - outline.
                int roomForRows = holder.getHeight() - lblTitle.getPreferredSize().height
                        - titleGap - headerHeight - outline;
                // Whole pixels per row, kept between the minimum and the maximum.
                int rowHeight = Math.max(minRowHeight, Math.min(maxRowHeight, roomForRows / maxRows));
                table.setRowHeight(rowHeight);
                // Make the scroll pane exactly as tall as header + maxRows rows. Width 0 = "use the full width".
                // Only if the window is too short even for the minimum row height, the pane shrinks to the room
                // that is left (at least 2 rows) and the scrollbar appears, instead of covering the next section.
                int rowsHeight = Math.min(rowHeight * maxRows, Math.max(roomForRows, minRowHeight * 2));
                scroll.setPreferredSize(new Dimension(0, headerHeight + outline + rowsHeight));
                section.revalidate();   // ask Swing to lay the section out again with the new height
            }
        });
        return holder;
    }

    // Creates a consistent title label for dashboard sections.
    private JLabel makeTitle(String text, int size) {

        // Create the label using the provided title.
        JLabel lbl = new JLabel(text);

        // Apply bold text with the specified font size.
        lbl.setFont(new Font("SansSerif", Font.BOLD, size));

        // Use the dashboard's navy title color.
        lbl.setForeground(TITLE_NAVY);

        return lbl;
    }

    // Creates a read-only table with the given headers and applies the shared look.
    private JTable makeTable(String[] headers, int rowHeight, int headerHeight, int fontSize, int headerFontSize) {
        // "new DefaultTableModel(...) { ... }" is an anonymous subclass: it overrides one method
        // so that no cell can be edited. The table is only for viewing.
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        styleTable(table, rowHeight, headerHeight, fontSize, headerFontSize);
        return table;
    }

    // The table look. These are the same settings as the table in TreatmentPanel, plus equal column widths.
    // Anything not set here stays at Swing's default (left-aligned cells, white rows, standard header).
    private void styleTable(JTable table, int rowHeight, int headerHeight, int fontSize, int headerFontSize) {
        table.setRowHeight(rowHeight);
        table.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        table.setForeground(TEXT_DARK);
        table.setSelectionBackground(new Color(0xDBEAFE));   // light blue when a row is clicked
        table.setSelectionForeground(TEXT_DARK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(true);
        table.setGridColor(GRID_COLOR);

        // --- ADDED: Custom cell renderer to add padding ---
        javax.swing.table.DefaultTableCellRenderer paddingRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // Adds an empty border: 0px top, 10px left, 0px bottom, 10px right
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        };

        // Header: bold text, light background, fixed height.
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, headerFontSize));
        header.setBackground(HEADER_BG);
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, headerHeight));   // width 0 = "let the scroll pane decide"

        // EQUAL COLUMNS, two steps:
        // 1. give every column the same starting width,
        // 2. AUTO_RESIZE_ALL_COLUMNS shares any extra or missing width evenly between ALL columns,
        //    so they stay equal when the window is resized.
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(100);

            // --- ADDED: Apply the padding renderer to every column ---
            table.getColumnModel().getColumn(i).setCellRenderer(paddingRenderer);
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        header.setResizingAllowed(false);      // the user cannot drag the columns out of balance
        header.setReorderingAllowed(false);
    }

    // =====================================================================================
    //  Small nested classes. "static" = they do not need a DashboardPanel object to exist.
    // =====================================================================================

    // A JPanel that paints a white rectangular card with a thin border.
    private static class RoundedCard extends JPanel {

        RoundedCard(LayoutManager layout) {
            super(layout);

            // Swing should not paint its own background because we paint it below.
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Create a separate graphics object so our drawing settings stay local.
            Graphics2D g2 = (Graphics2D) g.create();

            // Draw the white background with square corners.
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw a thin border around the card.
            g2.setColor(CARD_BORDER);
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

            g2.dispose();

            // Let Swing draw the card's contents, such as labels and tables.
            super.paintComponent(g);
        }
    }

    // A JLabel that shows its icon on top of a light blue circle.
    private static class IconBadge extends JLabel {
        IconBadge(Icon icon) {
            super(icon, SwingConstants.CENTER);          // the icon is centered inside the label
            setPreferredSize(new Dimension(64, 64));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BADGE_BG);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);   // the circle first...
            g2.dispose();
            super.paintComponent(g);                              // ...then the icon on top of it
        }
    }
}