package vetclinic.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.util.Map;

import javax.swing.JPanel;

public class DonutChartPanel extends JPanel {

    private static final Color[] COLORS = {
            new Color(0x6E9BFF),
            new Color(0xBFD4FF),
            new Color(0xA3B3D9),
            new Color(0x2563EB)
    };

    private static final Color TEXT_DARK = new Color(0x1E293B);
    private static final Color TEXT_MUTED = new Color(0x64748B);

    private Map<String, Integer> counts;

    public DonutChartPanel() {
        setOpaque(false);
    }

    public void setCounts(Map<String, Integer> counts) {
        this.counts = counts;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (counts == null) {
            return;
        }

        int total = 0;
        for (int value : counts.values()) {
            total += value;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Reserve space around the entire chart.
        int padding = 8;
        int availableWidth = getWidth() - padding * 2;
        int availableHeight = getHeight() - padding * 2;

        // The donut uses the left portion of the available space.
        int size = Math.min(availableHeight, availableWidth / 2);

        if (size < 40) {
            g2.dispose();
            return;
        }

        int x = padding;
        int y = (getHeight() - size) / 2;
        int thickness = size / 5;

        double arcSize = size - thickness;
        double arcX = x + thickness / 2.0;
        double arcY = y + thickness / 2.0;

        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        if (total == 0) {
            g2.setColor(new Color(0xE6ECF7));
            g2.draw(new Arc2D.Double(arcX, arcY, arcSize, arcSize, 0, 360, Arc2D.OPEN));
        } else {
            double startAngle = 90;
            int colorIndex = 0;

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                double sweep = entry.getValue() * 360.0 / total;

                if (sweep > 0) {
                    g2.setColor(COLORS[colorIndex % COLORS.length]);
                    g2.draw(new Arc2D.Double(arcX, arcY, arcSize, arcSize, startAngle, -sweep, Arc2D.OPEN));
                    startAngle -= sweep;
                }
                colorIndex++;
            }
        }

        // Center the total number inside the donut.
        drawCenterText(g2, x + size / 2, y + size / 2, total);

        // Place the legend to the right of the donut.
        int legendX = x + size + 20;
        drawLegend(g2, legendX);

        g2.dispose();
    }

    private void drawCenterText(Graphics2D g2, int centerX, int centerY, int total) {
        Font numberFont = new Font("SansSerif", Font.BOLD, 18);
        Font wordFont = new Font("SansSerif", Font.PLAIN, 10);

        FontMetrics numberMetrics = g2.getFontMetrics(numberFont);
        FontMetrics wordMetrics = g2.getFontMetrics(wordFont);

        String number = String.valueOf(total);
        String word = "Total";

        int blockHeight = numberMetrics.getHeight() + 1 + wordMetrics.getHeight();
        int numberBaseline = centerY - blockHeight / 2 + numberMetrics.getAscent();
        int wordBaseline = numberBaseline + 1 + wordMetrics.getHeight();

        g2.setFont(numberFont);
        g2.setColor(TEXT_DARK);
        g2.drawString(number, centerX - numberMetrics.stringWidth(number) / 2, numberBaseline);

        g2.setFont(wordFont);
        g2.setColor(TEXT_MUTED);
        g2.drawString(word, centerX - wordMetrics.stringWidth(word) / 2, wordBaseline);
    }

    private void drawLegend(Graphics2D g2, int legendX) {
        Font titleFont = new Font("SansSerif", Font.BOLD, 12);
        Font labelFont = new Font("SansSerif", Font.PLAIN, 11);
        Font countFont = new Font("SansSerif", Font.BOLD, 11);

        FontMetrics labelMetrics = g2.getFontMetrics(labelFont);

        // Find the widest species label to align all counts.
        int widest = 0;
        for (String species : counts.keySet()) {
            widest = Math.max(widest, labelMetrics.stringWidth(legendLabel(species)));
        }

        int countX = legendX + 18 + widest + 12;
        int rowHeight = 25;
        int titleHeight = 18;
        int titleGap = 8;

        // Calculate the total height of the title and all legend rows.
        int legendHeight = titleHeight + titleGap + counts.size() * rowHeight;

        // Center the title and legend as one group vertically.
        int startY = (getHeight() - legendHeight) / 2;

        // Title centered horizontally over the legend section.
        g2.setFont(titleFont);
        FontMetrics titleMetrics = g2.getFontMetrics(titleFont);

        int legendContentWidth = countX + 24 - legendX;
        int titleX = legendX + Math.max(0, (legendContentWidth - titleMetrics.stringWidth("Patients by Species")) / 2);

        g2.setColor(TEXT_DARK);
        g2.drawString("Patients by Species", titleX, startY + titleMetrics.getAscent());

        // Start the legend rows below the title.
        int legendY = startY + titleHeight + titleGap + 10;
        int colorIndex = 0;

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            // Colored circle.
            g2.setColor(COLORS[colorIndex % COLORS.length]);
            g2.fillOval(legendX, legendY - 9, 10, 10);

            // Species label.
            g2.setFont(labelFont);
            g2.setColor(TEXT_DARK);
            g2.drawString(legendLabel(entry.getKey()), legendX + 18, legendY);

            // Species count.
            g2.setFont(countFont);
            g2.drawString(String.valueOf(entry.getValue()), countX, legendY);

            legendY += rowHeight;
            colorIndex++;
        }
    }

    private String legendLabel(String species) {
        if (species == null || species.isBlank()) {
            return "Other:";
        }
        if (species.equalsIgnoreCase("Livestock")) {
            return "Livestock:";
        }
        if (species.endsWith("s")) {
            return species + ":";
        }
        return species + "s:";
    }
}