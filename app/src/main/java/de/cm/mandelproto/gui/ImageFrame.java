package de.cm.mandelproto.gui;

import de.cm.mandelproto.graphics.PixelCanvas;
import de.cm.mandelproto.math.ComplexNumber;
import de.cm.mandelproto.math.IterationMap;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@Slf4j
public class ImageFrame extends JFrame implements MouseListener {

    private final IterationMap iterationMap;
    private final PixelCanvas pixelCanvas;
    private final MainFrame mainFrame;

    private Point dragStart;
    private Rectangle draftRect;

    public ImageFrame(IterationMap iterationMap, MainFrame mainFrame) {
        super();
        this.mainFrame = mainFrame;
        log.debug("create ImageFrame");
        this.iterationMap = iterationMap;
        setSize(iterationMap.getStepOnWidth(), iterationMap.getStepOnHeight() + 40);
        setResizable(false);
        pixelCanvas = new PixelCanvas(getWidth(), getHeight(), iterationMap);
        pixelCanvas.addMouseListener(this);
        pixelCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart == null) return;
                int left   = Math.min(dragStart.x, e.getX());
                int top    = Math.min(dragStart.y, e.getY());
                int width  = Math.abs(e.getX() - dragStart.x);
                int height = Math.abs(e.getY() - dragStart.y);
                draftRect = new Rectangle(left, top, width, height);
                pixelCanvas.setPreviewRect(new Rectangle(draftRect));
                syncDraftRectToPanel();
            }
        });
        pixelCanvas.setFocusable(true);
        pixelCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (draftRect == null) return;
                boolean shift = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT  -> { if (shift) draftRect.width = Math.max(1, draftRect.width - 1);  else draftRect.x--; }
                    case KeyEvent.VK_RIGHT -> { if (shift) draftRect.width++;                                    else draftRect.x++; }
                    case KeyEvent.VK_UP    -> { if (shift) draftRect.height++;                                    else draftRect.y--; }
                    case KeyEvent.VK_DOWN  -> { if (shift) draftRect.height = Math.max(1, draftRect.height - 1); else draftRect.y++; }
                    case KeyEvent.VK_ENTER -> { confirmSelection(); return; }
                    default -> { return; }
                }
                pixelCanvas.setPreviewRect(new Rectangle(draftRect));
                syncDraftRectToPanel();
            }
        });
        add(pixelCanvas);
        setVisible(true);
        log.debug("created ImageFrame");
    }

    public void draw() {
        pixelCanvas.draw();
        pixelCanvas.requestFocusInWindow();
        log.debug("drawed pixelCanvas");
    }

    public void updatePreviewRect(ComplexNumber previewCenter, double previewWidth, double previewHeight) {
        double complexPerPixel = iterationMap.getWidth() / iterationMap.getStepOnWidth();
        double mapLeft         = iterationMap.getCenter().getReal() - iterationMap.getWidth()  / 2;
        double mapTop          = iterationMap.getCenter().getImag() + iterationMap.getHeight() / 2;

        int previewLeft   = (int) ((previewCenter.getReal() - previewWidth  / 2 - mapLeft) / complexPerPixel);
        int previewTop    = (int) ((mapTop - (previewCenter.getImag() + previewHeight / 2)) / complexPerPixel);
        int previewRight  = (int) ((previewCenter.getReal() + previewWidth  / 2 - mapLeft) / complexPerPixel);
        int previewBottom = (int) ((mapTop - (previewCenter.getImag() - previewHeight / 2)) / complexPerPixel);

        pixelCanvas.setPreviewRect(
                new Rectangle(previewLeft, previewTop, previewRight - previewLeft, previewBottom - previewTop)
        );
    }

    private void syncDraftRectToPanel() {
        double complexPerPixel = iterationMap.getWidth() / iterationMap.getStepOnWidth();
        double mapLeft = iterationMap.getCenter().getReal() - iterationMap.getWidth()  / 2;
        double mapTop  = iterationMap.getCenter().getImag() + iterationMap.getHeight() / 2;

        double centerReal    = mapLeft + (draftRect.x + draftRect.width  / 2.0) * complexPerPixel;
        double centerImag    = mapTop  - (draftRect.y + draftRect.height / 2.0) * complexPerPixel;
        double complexWidth  = draftRect.width  * complexPerPixel;
        double complexHeight = draftRect.height * complexPerPixel;

        mainFrame.initNewMandelbrotMap(new ComplexNumber(centerReal, centerImag), complexWidth, complexHeight);
    }

    private void confirmSelection() {
        if (draftRect == null || draftRect.width < 5 || draftRect.height < 5) return;
        syncDraftRectToPanel();
        mainFrame.triggerRender();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Single click (no drag): Zoom zur Hälfte, zentriert auf Klickpunkt
        if (draftRect != null && draftRect.width >= 5 && draftRect.height >= 5) return;
        log.debug("clicked on {}, {}", e.getX(), e.getY());
        ComplexNumber clickedOn = iterationMap.getComplexNumberForCoordinate(e.getX(), e.getY());
        mainFrame.initNewMandelbrotMap(clickedOn, iterationMap.getWidth() / 2, iterationMap.getHeight() / 2);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        log.debug("mousePressed on {}, {}", e.getX(), e.getY());
        dragStart = new Point(e.getX(), e.getY());
        draftRect = null;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        log.debug("released on {}, {}", e.getX(), e.getY());
        dragStart = null;
        if (draftRect != null && draftRect.width >= 5 && draftRect.height >= 5) {
            pixelCanvas.requestFocusInWindow();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
