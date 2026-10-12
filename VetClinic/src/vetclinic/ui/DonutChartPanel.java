package vetclinic.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.util.Map;

import javax.swing.JPanel;

/**
 * DonutChartPanel - PHASE 2. Draws the "Patients by Species" donut chart with Graphics2D (no library).
 * Owner: Unassigned (Phase 2)
 *
 * OOP concepts: INHERITANCE (extends JPanel) and OVERRIDING (paintComponent replaces the parent's drawing).
 */
public class DonutChartPanel extends JPanel {

    private Map<String, Integer> counts;   // species name -> number of animals

    // Gives the chart new numbers and asks Swing to draw it again.
    public void setCounts(Map<String, Integer> counts) {
        this.counts = counts;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);   // clears the panel first
        if (counts == null) {
            return;
        }
        int total = 0;
        for (int value : counts.values()) {
            total += value;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth() / 2, getHeight()) - 20;
        if (size < 40) {
            g2.dispose();
            return;
        }
        int x = 10;
        int y = (getHeight() - size) / 2;
        Color[] colors = {new Color(0x6E9BFF), new Color(0xBFD4FF), new Color(0xA3B3D9), new Color(0x2563EB)};

        // Each species gets a slice. Angles are in degrees; the slice size is count / total * 360.
        double startAngle = 90;
        int colorIndex = 0;
        int legendY = y + 20;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            double sweep = (total == 0) ? 0 : entry.getValue() * 360.0 / total;
            g2.setColor(colors[colorIndex % colors.length]);
            g2.fill(new Arc2D.Double(x, y, size, size, startAngle, -sweep, Arc2D.PIE));
            startAngle -= sweep;

            // Legend on the right: colored square + text.
            g2.fillRect(x + size + 20, legendY - 10, 12, 12);
            g2.setColor(new Color(0x1F2A44));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString(entry.getKey() + ": " + entry.getValue(), x + size + 38, legendY);
            legendY += 22;
            colorIndex++;
        }

        // A white circle in the middle turns the pie into a donut.
        int hole = size / 2;
        g2.setColor(getBackground());
        g2.fillOval(x + (size - hole) / 2, y + (size - hole) / 2, hole, hole);
        g2.setColor(new Color(0x1F2A44));
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        String text = String.valueOf(total);
        g2.drawString(text, x + size / 2 - g2.getFontMetrics().stringWidth(text) / 2, y + size / 2 + 7);
        g2.setStroke(new BasicStroke(1f));
        g2.dispose();
    }
}
