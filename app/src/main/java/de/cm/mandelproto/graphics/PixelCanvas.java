package de.cm.mandelproto.graphics;

import de.cm.mandelproto.math.IterationMap;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class PixelCanvas extends JComponent {

    private final BufferedImage image;
    private final IterationMap iterationMap;
    private final Palette palette;
    private final PaletteMapper paletteMapper;
    private final ChangeListener repaintListener;
    private Rectangle previewRect;

    public PixelCanvas(int width, int height, IterationMap iterationMap, Palette palette, PaletteMapper paletteMapper) {
        super();
        this.iterationMap = iterationMap;
        this.palette = palette;
        this.paletteMapper = paletteMapper;
        this.repaintListener = e -> drawImage();
        palette.addChangeListener(repaintListener);
        setSize(width, height);
        setDoubleBuffered(true);
        image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        drawImage();
    }

    @Override
    public void removeNotify() {
        palette.removeChangeListener(repaintListener);
        super.removeNotify();
    }

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

    public void drawImage() {
        paletteMapper.configure(iterationMap.getMaxIterations(), palette.size());
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int iteration = iterationMap.getIterationForCoordinate(x, y);
                image.setRGB(x, y, palette.getColor(paletteMapper.map(iteration)));
            }
        }
        repaint();
    }

}
