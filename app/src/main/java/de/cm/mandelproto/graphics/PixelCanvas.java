package de.cm.mandelproto.graphics;

import de.cm.mandelproto.math.IterationMap;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

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
