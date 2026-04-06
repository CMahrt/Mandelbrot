package de.cm.mandelproto.gui;

import de.cm.mandelproto.graphics.Palette;
import de.cm.mandelproto.graphics.PixelCanvas;
import de.cm.mandelproto.math.ComplexNumber;
import de.cm.mandelproto.math.IterationMap;
import de.cm.mandelproto.math.RenderParameters;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

@Slf4j
public class ImageFrame extends JFrame implements MouseListener {

    private final IterationMap iterationMap;
    private final PixelCanvas pixelCanvas;
    private final MainFrame mainFrame;
    @Setter
    private InspectorFrame inspector;

    private Point dragStart;
    private Rectangle draftRect;

    public ImageFrame(
            String title,
            IterationMap iterationMap,
            MainFrame mainFrame,
            Palette palette
    ) {
        super(title);
        log.debug("create ImageFrame");
        this.mainFrame = mainFrame;
        this.iterationMap = iterationMap;
        setSize(iterationMap.getCols(), iterationMap.getRows() + 40);
        setResizable(false);
        pixelCanvas = new PixelCanvas(getWidth(), getHeight(), iterationMap, palette);
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
                if (inspector != null) inspector.updateParams(computeParamsForRect(draftRect));
            }
        });
        add(pixelCanvas);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                palette.stopCycling();
                mainFrame.onImageFrameClosing(ImageFrame.this);
                if (inspector != null) inspector.dispose();
            }
        });
        setVisible(true);
        log.debug("created ImageFrame");
    }

    public void drawImage() {
        pixelCanvas.drawImage();
    }

    /** Versetzt dieses Fenster in den Selektionsmodus (aufgerufen vom InspectorFrame). */
    public void enterSelectionMode() {
        log.debug("Selektionsmodus gestartet: {}", getTitle());
        draftRect = null;
        pixelCanvas.setPreviewRect(null);
        pixelCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        toFront();
    }

    /**
     * Verschiebt oder vergrößert/verkleinert das Auswahl-Rechteck um einen Pixel.
     * Ohne Shift: Rechteck verschieben. Mit Shift: Breite/Höhe ändern.
     * @return die neu berechneten Parameter für das angepasste Rechteck
     */
    public RenderParameters adjustDraftRect(int dx, int dy, boolean shift) {
        if (draftRect == null) return null;
        if (shift) {
            draftRect.width  = Math.max(1, draftRect.width  + dx);
            draftRect.height = Math.max(1, draftRect.height + dy);
        } else {
            draftRect.x += dx;
            draftRect.y += dy;
        }
        pixelCanvas.setPreviewRect(new Rectangle(draftRect));
        RenderParameters params = computeParamsForRect(draftRect);
        if (inspector != null) inspector.updateParams(params);
        return params;
    }

    private void openParameterDialogForSelection() {
        RenderParameters selectionParams = computeParamsForRect(draftRect);
        log.debug("ParameterDialog öffnen: center={}, complexWidth={}",
                selectionParams.center(), selectionParams.complexWidth());
        Optional<RenderParameters> confirmedParams =
                ParameterDialog.requestParameters(
                        pixelCanvas,
                        selectionParams,
                        this::adjustDraftRect
                );
        pixelCanvas.setCursor(Cursor.getDefaultCursor());
        draftRect = null;
        pixelCanvas.setPreviewRect(null);
        if (confirmedParams.isPresent()) {
            log.debug("ParameterDialog bestätigt: pixelWidth={}, maxIterations={}",
                    confirmedParams.get().pixelWidth(), confirmedParams.get().maxIterations());
            mainFrame.createImageFramePair(confirmedParams.get());
        } else {
            log.debug("ParameterDialog abgebrochen");
        }
    }

    private RenderParameters computeParamsForRect(Rectangle rect) {
        double complexPerPixel = iterationMap.getWidth() / iterationMap.getCols();
        double mapLeft = iterationMap.getCenter().getReal() - iterationMap.getWidth()  / 2;
        double mapTop  = iterationMap.getCenter().getImag() + iterationMap.getHeight() / 2;

        double centerReal    = mapLeft + (rect.x + rect.width  / 2.0) * complexPerPixel;
        double centerImag    = mapTop  - (rect.y + rect.height / 2.0) * complexPerPixel;
        double complexWidth  = rect.width  * complexPerPixel;
        double complexHeight = rect.height * complexPerPixel;
        int    maxIter       = suggestMaxIterations(complexWidth);

        return new RenderParameters(
                new ComplexNumber(centerReal, centerImag),
                complexWidth,
                complexHeight,
                iterationMap.getCols(),
                maxIter
        );
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

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
            openParameterDialogForSelection();
        } else {
            pixelCanvas.setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    private static int suggestMaxIterations(double complexWidth) {
        return Math.max(100, (int) (150 * Math.log10(38.4 / complexWidth)));
    }
}
