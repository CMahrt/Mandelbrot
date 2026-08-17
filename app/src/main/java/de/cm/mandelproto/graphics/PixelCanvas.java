package de.cm.mandelproto.graphics;

import de.cm.mandelproto.math.IterationMap;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

@Slf4j
public class PixelCanvas extends JComponent {

    private static final double SCALE_MIN = 0.1;
    private static final double SCALE_MAX = 4.0;

    private final BufferedImage image;
    private final IterationMap iterationMap;
    private final Palette palette;
    private final PaletteMapper paletteMapper;
    private final ChangeListener repaintListener;
    private Rectangle previewRect;
    private double viewScale = 1.0;

    public PixelCanvas(int width, int height, IterationMap iterationMap, Palette palette, PaletteMapper paletteMapper) {
        super();
        this.iterationMap = iterationMap;
        this.palette = palette;
        this.paletteMapper = paletteMapper;
        this.repaintListener = e -> drawImage();
        palette.addChangeListener(repaintListener);
        setDoubleBuffered(true);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        drawImage();
    }

    public double getViewScale() { return viewScale; }

    public void setViewScale(double scale) {
        viewScale = Math.max(SCALE_MIN, Math.min(SCALE_MAX, scale));
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                (int)(image.getWidth()  * viewScale),
                (int)(image.getHeight() * viewScale)
        );
    }

    @Override
    public void removeNotify() {
        palette.removeChangeListener(repaintListener);
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = (int)(image.getWidth()  * viewScale);
        int h = (int)(image.getHeight() * viewScale);
        g.drawImage(image, 0, 0, w, h, this);
        if (previewRect != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.RED);
            g2.drawRect(
                    (int)(previewRect.x      * viewScale),
                    (int)(previewRect.y      * viewScale),
                    (int)(previewRect.width  * viewScale),
                    (int)(previewRect.height * viewScale)
            );
        }
    }

    public void setPreviewRect(Rectangle previewRect) {
        this.previewRect = previewRect;
        repaint();
    }

    public void drawImage() {
        paletteMapper.configure(iterationMap.getMinIteration(), iterationMap.getMaxIterations(), palette.size());
        int width = image.getWidth();
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int iteration = iterationMap.getIterationForCoordinate(x, y);
                pixels[y * width + x] = palette.getColor(paletteMapper.map(iteration));
            }
        }
        repaint();
    }

}
