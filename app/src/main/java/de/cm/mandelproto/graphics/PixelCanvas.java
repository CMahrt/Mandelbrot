package de.cm.mandelproto.graphics;

import de.cm.mandelproto.math.IterationMap;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Slf4j
public class PixelCanvas extends JComponent {



    public PixelCanvas(int width, int height, IterationMap iterationMap, Color[] palette) {
        super();
        this.iterationMap = iterationMap;
        this.palette = palette;
        setSize(width, height);
        setDoubleBuffered(true);
        image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        drawImage();
    }

    private final BufferedImage image;
    private final IterationMap iterationMap;
    private Color[] palette;
    private Rectangle previewRect;
    private final Random rnd = new Random();


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
        if (previewRect != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.RED);
            g2.draw(previewRect);
        }
    }

    public void setPreviewRect(Rectangle previewRect) {
        this.previewRect = previewRect;
        repaint();
    }

    public void setPalette(Color[] newPalette) {
        this.palette = newPalette;
        drawImage();
    }

    public void drawImage() {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int iteration = iterationMap.getIterationForCoordinate(x, y);
                image.setRGB(x, y, palette[iteration % palette.length].getRGB());
            }
        }
        repaint();
    }


    public void rotatePalette(int devR, int devG, int devB, boolean forward) {
        Color neighbor;
        if (forward) {
            // Links-Shift: neuer Eintrag am Ende (innen → außen)
            neighbor = palette[palette.length - 1];
            System.arraycopy(palette, 1, palette, 0, palette.length - 1);
            palette[palette.length - 1] = randomDeviation(neighbor, devR, devG, devB);
        } else {
            // Rechts-Shift: neuer Eintrag am Anfang (außen → innen)
            neighbor = palette[0];
            System.arraycopy(palette, 0, palette, 1, palette.length - 1);
            palette[0] = randomDeviation(neighbor, devR, devG, devB);
        }
        drawImage();
    }

    private Color randomDeviation(Color base, int devR, int devG, int devB) {
        int r = clamp(base.getRed()   + rnd.nextInt(2 * devR + 1) - devR);
        int g = clamp(base.getGreen() + rnd.nextInt(2 * devG + 1) - devG);
        int b = clamp(base.getBlue()  + rnd.nextInt(2 * devB + 1) - devB);
        return new Color(r, g, b);
    }

    private int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public void draw(){
        long startTime = System.currentTimeMillis();
//        for (int i = 0; i < 50; i++) {
//            iterationMap.iterate();
//            step();
//        }
        iterationMap.tileIterate();
        drawImage();
        log.info("time to compute = {} ms", System.currentTimeMillis() - startTime);

    }
}
