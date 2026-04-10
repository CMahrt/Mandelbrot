package de.cm.mandelproto.gui;

import de.cm.mandelproto.I18n;
import de.cm.mandelproto.graphics.Palette;
import de.cm.mandelproto.graphics.PaletteLibrary;
import de.cm.mandelproto.graphics.PaletteMapper;
import de.cm.mandelproto.graphics.PixelCanvas;
import de.cm.mandelproto.io.FractalIO;
import de.cm.mandelproto.io.FractalSnapshot;
import de.cm.mandelproto.math.ComplexNumber;
import de.cm.mandelproto.math.IterationMap;
import de.cm.mandelproto.math.MandelbrotPointMap;
import de.cm.mandelproto.math.RenderParameters;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;

@Slf4j
public class ImageFrame extends JFrame implements MouseListener {

    private final IterationMap iterationMap;
    private final PixelCanvas pixelCanvas;
    private final MainFrame mainFrame;
    private final InspectorFrame inspector;
    private final Palette palette;

    private Point dragStart;
    private Rectangle draftRect;

    public ImageFrame(String title, FractalSnapshot snapshot, MainFrame mainFrame) {
        super(title);
        this.mainFrame = mainFrame;
        PaletteMapper paletteMapper = new PaletteMapper();
        palette      = new Palette(snapshot.palette());
        iterationMap = MandelbrotPointMap.fromData(snapshot.params(), snapshot.iterations());
        pixelCanvas  = new PixelCanvas(iterationMap.getCols(), iterationMap.getRows() + 40, iterationMap, palette, paletteMapper);
        inspector    = new InspectorFrame(snapshot.params(), palette, paletteMapper, this);
        configureWindow();
        registerListeners();
        setVisible(true);
        drawImage();
    }

    public ImageFrame(String title, RenderParameters params, MainFrame mainFrame) {
        super(title);
        this.mainFrame = mainFrame;
        PaletteMapper paletteMapper = new PaletteMapper();
        palette      = new Palette(PaletteLibrary.byName("Graustufen", 256));
        iterationMap = new MandelbrotPointMap(params);
        pixelCanvas  = new PixelCanvas(iterationMap.getCols(), iterationMap.getRows() + 40, iterationMap, palette, paletteMapper);
        inspector    = new InspectorFrame(params, palette, paletteMapper, this);
        configureWindow();
        registerListeners();
        setVisible(true);
        startRendering();
    }

    private void configureWindow() {
        setSize(iterationMap.getCols(), iterationMap.getRows() + 40);
        setResizable(false);
        add(pixelCanvas);
    }

    private void registerListeners() {
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
                inspector.updateParams(computeParamsForRect(draftRect));
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                palette.stopCycling();
                mainFrame.onImageFrameClosing(ImageFrame.this);
                inspector.dispose();
            }
        });
    }

    private void startRendering() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                long t = System.currentTimeMillis();
                iterationMap.tileIterate();
                log.info("tileIterate = {} ms", System.currentTimeMillis() - t);
                return null;
            }
            @Override
            protected void done() {
                drawImage();
                toFront();
            }
        }.execute();
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
        inspector.updateParams(params);
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
            mainFrame.openImage(confirmedParams.get());
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

    public void saveToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(I18n.get("dialog.saveFractal.title"));
        chooser.setFileFilter(new FileNameExtensionFilter(I18n.get("filefilter.mfrac.description"), "mfrac"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".mfrac")) {
            file = new File(file.getParentFile(), file.getName() + ".mfrac");
        }
        try {
            FractalIO.save(file, iterationMap.getRenderParameters(), iterationMap, palette);
        } catch (IOException ex) {
            log.error("Speichern fehlgeschlagen", ex);
            JOptionPane.showMessageDialog(this,
                    I18n.get("error.saveFailed") + "\n" + ex.getMessage(),
                    I18n.get("error.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void startRefine() {
        int currentMax = iterationMap.getMaxIterations();
        int suggested  = Math.max(currentMax + 100, currentMax * 3);
        Object input = JOptionPane.showInputDialog(
                this,
                MessageFormat.format(I18n.get("dialog.refine.prompt"), currentMax),
                I18n.get("dialog.refine.title"),
                JOptionPane.QUESTION_MESSAGE,
                null, null,
                suggested
        );
        if (input == null) return; // Abgebrochen
        int newMax;
        try {
            newMax = Integer.parseInt(input.toString().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    I18n.get("error.refine.tooLow"),
                    I18n.get("error.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newMax <= currentMax) {
            JOptionPane.showMessageDialog(this,
                    I18n.get("error.refine.tooLow"),
                    I18n.get("error.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        inspector.setRefineEnabled(false);
        final int finalNewMax = newMax;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                long t = System.currentTimeMillis();
                iterationMap.refine(finalNewMax);
                log.info("refine({}) = {} ms", finalNewMax, System.currentTimeMillis() - t);
                return null;
            }
            @Override
            protected void done() {
                drawImage();
                inspector.updateParams(iterationMap.getRenderParameters());
                inspector.setRefineEnabled(true);
            }
        }.execute();
    }

    private static int suggestMaxIterations(double complexWidth) {
        return Math.max(100, (int) (150 * Math.log10(38.4 / complexWidth)));
    }
}
