package vetclinic.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * ImageUtil - loads an image from the classpath and gives back an icon that stays SHARP on any screen.
 *
 * Why icons were blurry: Windows display scaling (125%, 150%...) makes Java draw everything bigger.
 * A 32x32 icon on a 125% screen needs 40x40 real pixels, so a ready-made 32x32 picture gets stretched
 * and turns soft. CrispIcon fixes this by keeping the BIG original and scaling it to the real number of
 * pixels at the moment it is painted.
 *
 * OOP concepts: INHERITANCE (CrispIcon extends ImageIcon, so every caller that expects an ImageIcon still
 * works) and POLYMORPHISM (CrispIcon overrides paintIcon).
 */
public class ImageUtil {

    // Loads an image from the classpath. width/height are the size in "normal" (100%) pixels.
    public static ImageIcon load(String path, int width, int height) {
        // Locate the image resource using the class loader
        URL imgUrl = ImageUtil.class.getResource(path);

        // Return an empty icon and log a warning if the file path is incorrect or missing
        if (imgUrl == null) {
            System.err.println("Warning: Image not found -> " + path);
            return new ImageIcon();
        }

        try {
            // ImageIO.read gives a BufferedImage, which we can resize and cache easily.
            BufferedImage original = ImageIO.read(imgUrl);
            if (original == null) {
                throw new IOException("Unsupported image format");
            }
            return new CrispIcon(original, width, height);
        } catch (IOException e) {
            System.err.println("Warning: Could not read image -> " + path + " (" + e.getMessage() + ")");
            return new ImageIcon();
        }
    }

    // Resizes with high quality. Shrinking in ONE big jump (for example 512 px -> 32 px) looks rough,
    // so we shrink by at most half each time and only do the last small step to the exact size.
    static BufferedImage scale(BufferedImage source, int width, int height) {
        BufferedImage current = source;
        int w = source.getWidth();
        int h = source.getHeight();
        while (w > width * 2 || h > height * 2) {
            w = Math.max(width, w / 2);    // never go below the target size
            h = Math.max(height, h / 2);
            current = drawScaled(current, w, h);
        }
        return drawScaled(current, width, height);
    }

    // Draws the image onto a new transparent canvas of the given size.
    private static BufferedImage drawScaled(BufferedImage source, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        // Bicubic interpolation + quality rendering + anti-aliasing = the smoothest resize Java offers.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(source, 0, 0, width, height, null);
        g2.dispose();   // release the system resources used by the Graphics2D object
        return result;
    }

    // An icon that re-scales itself to the REAL pixel size of the screen it is painted on.
    private static class CrispIcon extends ImageIcon {
        private final BufferedImage original;   // the big, untouched picture
        private final int width;                // size in normal (100%) pixels
        private final int height;
        private BufferedImage cached;           // the last real-pixel version, so we do not re-scale every repaint
        private int cachedWidth;
        private int cachedHeight;

        CrispIcon(BufferedImage original, int width, int height) {
            super(scale(original, width, height));   // the normal-size picture: what getImage() returns
            this.original = original;
            this.width = width;
            this.height = height;
        }

        @Override   // @Override = "I am replacing a method of the parent class"
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();   // create() = a private copy, so our changes do not leak
            try {
                // The graphics' scale factor is the display scaling: 1.0 = 100%, 1.25 = 125%, 1.5 = 150%...
                double scaleX = g2.getTransform().getScaleX();
                double scaleY = g2.getTransform().getScaleY();
                int realWidth = (int) Math.round(width * scaleX);    // how many REAL pixels the icon covers
                int realHeight = (int) Math.round(height * scaleY);

                if (realWidth == width && realHeight == height) {
                    super.paintIcon(c, g, x, y);   // normal 100% screen: the default drawing is already sharp
                    return;
                }

                if (cached == null || cachedWidth != realWidth || cachedHeight != realHeight) {
                    cached = scale(original, realWidth, realHeight);
                    cachedWidth = realWidth;
                    cachedHeight = realHeight;
                }

                // Move to the icon's position, then undo the display scaling, so that
                // 1 drawing unit = 1 real pixel and our picture is copied over WITHOUT being stretched again.
                g2.translate(x, y);
                g2.scale(1.0 / scaleX, 1.0 / scaleY);
                g2.drawImage(cached, 0, 0, null);
            } finally {
                g2.dispose();
            }
        }
    }
}