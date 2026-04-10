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
    private JScrollPane scrollPane;
    private JPanel centerer;

    private Point dragStart;
    private Rectangle draftRect;
    private boolean selectionMode = false;

    public ImageFrame(String title, FractalSnapshot snapshot, MainFrame mainFrame) {
        super(title);
        this.mainFrame = mainFrame;
        PaletteMapper paletteMapper = new PaletteMapper();
        palette      = new Palette(snapshot.palette());
        iterationMap = MandelbrotPointMap.fromData(snapshot.params(), snapshot.iterations(), snapshot.minIteration());
        pixelCanvas  = new PixelCanvas(iterationMap.getCols(), iterationMap.getRows(), iterationMap, palette, paletteMapper);
        inspector    = new InspectorFrame(iterationMap, palette, paletteMapper, this);
        configureWindow();
        registerListeners();
        setVisible(true);
        drawImage();
        scrollToCenter();
    }

    public ImageFrame(String title, RenderParameters params, MainFrame mainFrame) {
        super(title);
        this.mainFrame = mainFrame;
        PaletteMapper paletteMapper = new PaletteMapper();
        palette      = new Palette(PaletteLibrary.byName("Graustufen", 256));
        iterationMap = new MandelbrotPointMap(params);
        pixelCanvas  = new PixelCanvas(iterationMap.getCols(), iterationMap.getRows(), iterationMap, palette, paletteMapper);
        inspector    = new InspectorFrame(iterationMap, palette, paletteMapper, this);
        configureWindow();
        registerListeners();
        setVisible(true);
        startRendering();
    }

    private void configureWindow() {
        setResizable(true);
        centerer = new JPanel(null); // null-Layout: manuelle Positionierung von pixelCanvas
        centerer.add(pixelCanvas);
        // Initiale Bounds setzen, damit pixelCanvas vor scrollToCenter() sichtbar ist
        Dimension canvas = pixelCanvas.getPreferredSize();
        pixelCanvas.setBounds(0, 0, canvas.width, canvas.height);
        centerer.setPreferredSize(canvas);
        scrollPane = new JScrollPane(centerer);
        add(scrollPane);
        pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int maxW = (int)(screen.width  * 0.9);
        int maxH = (int)(screen.height * 0.9);
        if (getWidth() > maxW || getHeight() > maxH) {
            setSize(Math.min(getWidth(), maxW), Math.min(getHeight(), maxH));
        }
    }

    /**
     * Positioniert pixelCanvas innerhalb des Centerers und setzt die Scrollposition.
     * canvasX/canvasY: Position von pixelCanvas in Centerer-Koordinaten (≥ 0).
     * scrollX/scrollY: Scroll-Offset des Viewports im Centerer (≥ 0).
     * Invariante: canvasX - scrollX = gewünschte Canvas-Position im Viewport.
     */
    private void placeCanvas(int canvasX, int canvasY, int scrollX, int scrollY) {
        Dimension canvas = pixelCanvas.getPreferredSize();
        Dimension vp     = scrollPane.getViewport().getSize();
        centerer.setPreferredSize(new Dimension(
                Math.max(vp.width,  canvasX + canvas.width),
                Math.max(vp.height, canvasY + canvas.height)
        ));
        pixelCanvas.setBounds(canvasX, canvasY, canvas.width, canvas.height);
        // Synchrones validate() statt revalidate(): Layout läuft sofort durch,
        // ScrollPaneLayout passt Scroll-Bars an — danach setzen wir die Position,
        // ohne dass ein späterer Layout-Pass sie wieder überschreiben kann.
        scrollPane.validate();
        scrollPane.getViewport().setViewPosition(new Point(scrollX, scrollY));
    }

    private void scrollToCenter() {
        SwingUtilities.invokeLater(() -> {
            Dimension canvas = pixelCanvas.getPreferredSize();
            Dimension vp     = scrollPane.getViewport().getSize();
            // Kleines Bild: Canvas mittig im Centerer, kein Scrollen
            // Großes Bild: Canvas bei (0,0), Scroll auf Bildmitte
            int canvasX = Math.max(0, (vp.width  - canvas.width)  / 2);
            int canvasY = Math.max(0, (vp.height - canvas.height) / 2);
            int scrollX = Math.max(0, (canvas.width  - vp.width)  / 2);
            int scrollY = Math.max(0, (canvas.height - vp.height) / 2);
            placeCanvas(canvasX, canvasY, scrollX, scrollY);
        });
    }

    private void registerListeners() {
        pixelCanvas.addMouseListener(this);
        pixelCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart == null) return;
                int x = (int)(e.getX() / pixelCanvas.getViewScale());
                int y = (int)(e.getY() / pixelCanvas.getViewScale());
                int left   = Math.min(dragStart.x, x);
                int top    = Math.min(dragStart.y, y);
                int width  = Math.abs(x - dragStart.x);
                int height = Math.abs(y - dragStart.y);
                draftRect = new Rectangle(left, top, width, height);
                pixelCanvas.setPreviewRect(new Rectangle(draftRect));
            }
        });
        pixelCanvas.addMouseWheelListener(e -> {
            double oldScale = pixelCanvas.getViewScale();
            double factor   = Math.pow(1.05, -e.getPreciseWheelRotation());
            double newScale = Math.max(0.1, Math.min(4.0, oldScale * factor));
            if (newScale == oldScale) { e.consume(); return; }

            // Mausposition im Viewport-Anzeigebereich (vor dem Zoom)
            Point mouseInVP = SwingUtilities.convertPoint(pixelCanvas, e.getPoint(), scrollPane.getViewport());
            // Nativer Bildpunkt unter dem Zeiger
            double imgX = e.getX() / oldScale;
            double imgY = e.getY() / oldScale;

            pixelCanvas.setViewScale(newScale);

            // Gewünschte Canvas-Oberkante im Viewport nach dem Zoom:
            // Der native Punkt (imgX, imgY) liegt jetzt bei Canvas-Pos (imgX*newScale, imgY*newScale)
            // und soll weiterhin bei mouseInVP im Viewport erscheinen.
            double canvasPosX = mouseInVP.x - imgX * newScale;
            double canvasPosY = mouseInVP.y - imgY * newScale;

            // Aufteilen in Canvas-Position im Centerer (≥0) + Scroll-Offset (≥0)
            int canvasX = (int) Math.max(0, Math.round(canvasPosX));
            int canvasY = (int) Math.max(0, Math.round(canvasPosY));
            int scrollX = (int) Math.max(0, Math.round(-canvasPosX));
            int scrollY = (int) Math.max(0, Math.round(-canvasPosY));
            placeCanvas(canvasX, canvasY, scrollX, scrollY);
            e.consume();
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
        setBusy(true);
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
                setBusy(false);
                drawImage();
                inspector.updateParams(iterationMap);
                scrollToCenter();
                toFront();
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        pixelCanvas.setCursor(busy
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor());
    }

    public void drawImage() {
        pixelCanvas.drawImage();
    }

    /** Versetzt dieses Fenster in den Selektionsmodus (aufgerufen vom InspectorFrame). */
    public void enterSelectionMode() {
        log.debug("Selektionsmodus gestartet: {}", getTitle());
        selectionMode = true;
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
        selectionMode = false;
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
        if (!inspector.isVisible()) inspector.setVisible(true);
        if (!selectionMode) return;
        int x = (int)(e.getX() / pixelCanvas.getViewScale());
        int y = (int)(e.getY() / pixelCanvas.getViewScale());
        log.debug("mousePressed on {}, {} (native: {}, {})", e.getX(), e.getY(), x, y);
        dragStart = new Point(x, y);
        draftRect = null;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        log.debug("released on {}, {}", e.getX(), e.getY());
        dragStart = null;
        if (draftRect != null && draftRect.width >= 5 && draftRect.height >= 5) {
            openParameterDialogForSelection();
        } else {
            selectionMode = false;
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
        setBusy(true);
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
                setBusy(false);
                drawImage();
                inspector.updateParams(iterationMap);
                inspector.setRefineEnabled(true);
            }
        }.execute();
    }

    private static int suggestMaxIterations(double complexWidth) {
        return Math.max(100, (int) (150 * Math.log10(38.4 / complexWidth)));
    }
}
