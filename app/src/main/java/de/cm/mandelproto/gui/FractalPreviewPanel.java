package de.cm.mandelproto.gui;

import de.cm.mandelproto.I18n;
import de.cm.mandelproto.graphics.PaletteMapper;
import de.cm.mandelproto.io.FractalIO;
import de.cm.mandelproto.io.FractalSnapshot;
import de.cm.mandelproto.math.RenderParameters;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class FractalPreviewPanel extends JPanel implements PropertyChangeListener {

    private static final int MAX_PREVIEW = 200;
    private static final int PANEL_WIDTH  = 230;

    private final JLabel imageLabel;
    private final JLabel valCenterReal;
    private final JLabel valCenterImag;
    private final JLabel valComplexWidth;
    private final JLabel valMaxIterations;
    private final JLabel valResolution;

    private final AtomicInteger generation = new AtomicInteger(0);
    private SwingWorker<PreviewResult, Void> currentWorker;

    private record PreviewResult(BufferedImage image, RenderParameters params) {}

    public FractalPreviewPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 4));
        setPreferredSize(new Dimension(PANEL_WIDTH, 360));

        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(MAX_PREVIEW, MAX_PREVIEW));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        valCenterReal    = new JLabel();
        valCenterImag    = new JLabel();
        valComplexWidth  = new JLabel();
        valMaxIterations = new JLabel();
        valResolution    = new JLabel();

        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 4, 2));
        infoPanel.add(new JLabel(I18n.get("field.centerReal") + ":"));
        infoPanel.add(valCenterReal);
        infoPanel.add(new JLabel(I18n.get("field.centerImag") + ":"));
        infoPanel.add(valCenterImag);
        infoPanel.add(new JLabel(I18n.get("field.complexWidth") + ":"));
        infoPanel.add(valComplexWidth);
        infoPanel.add(new JLabel(I18n.get("field.maxIterations") + ":"));
        infoPanel.add(valMaxIterations);
        infoPanel.add(new JLabel(I18n.get("preview.resolution") + ":"));
        infoPanel.add(valResolution);

        add(imageLabel, BorderLayout.NORTH);
        add(infoPanel,  BorderLayout.CENTER);

        showPlaceholder();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) return;
        File file = (File) evt.getNewValue();
        if (file == null || !file.getName().endsWith(".mfrac")) {
            cancelWorker();
            showPlaceholder();
            return;
        }
        loadPreview(file);
    }

    private void loadPreview(File file) {
        cancelWorker();
        int myGeneration = generation.incrementAndGet();
        showLoading();

        SwingWorker<PreviewResult, Void> worker = new SwingWorker<>() {
            @Override
            protected PreviewResult doInBackground() throws Exception {
                FractalSnapshot snap = FractalIO.load(file);
                BufferedImage preview = buildPreviewImage(snap);
                return new PreviewResult(preview, snap.params());
            }

            @Override
            protected void done() {
                if (isCancelled() || generation.get() != myGeneration) return;
                try {
                    PreviewResult result = get();
                    showPreview(result.image(), result.params());
                } catch (Exception ex) {
                    showError();
                }
            }
        };
        currentWorker = worker;
        worker.execute();
    }

    private void cancelWorker() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
        currentWorker = null;
    }

    private void showPlaceholder() {
        imageLabel.setIcon(null);
        imageLabel.setText(I18n.get("preview.selectFile"));
        clearParams();
    }

    private void showLoading() {
        imageLabel.setIcon(null);
        imageLabel.setText(I18n.get("preview.loading"));
        clearParams();
    }

    private void showError() {
        imageLabel.setIcon(null);
        imageLabel.setText(I18n.get("preview.error"));
        clearParams();
    }

    private void showPreview(BufferedImage img, RenderParameters params) {
        imageLabel.setIcon(new ImageIcon(img));
        imageLabel.setText(null);
        valCenterReal   .setText(String.format("%.8f", params.center().getReal()));
        valCenterImag   .setText(String.format("%.8f", params.center().getImag()));
        valComplexWidth .setText(String.format("%.4e", params.complexWidth()));
        valMaxIterations.setText(String.valueOf(params.maxIterations()));
        valResolution   .setText(params.pixelWidth() + " \u00d7 " + params.pixelHeight());
    }

    private void clearParams() {
        valCenterReal   .setText("");
        valCenterImag   .setText("");
        valComplexWidth .setText("");
        valMaxIterations.setText("");
        valResolution   .setText("");
    }

    private static BufferedImage buildPreviewImage(FractalSnapshot snap) {
        RenderParameters params  = snap.params();
        int[][]          iters   = snap.iterations();   // col-major: [col][row]
        Color[]          palette = snap.palette();

        int srcW = params.pixelWidth();
        int srcH = params.pixelHeight();
        double scale = Math.min((double) MAX_PREVIEW / srcW, (double) MAX_PREVIEW / srcH);
        int dstW = Math.max(1, (int) (srcW * scale));
        int dstH = Math.max(1, (int) (srcH * scale));

        PaletteMapper mapper = new PaletteMapper();
        mapper.setCurve(PaletteMapper.Curve.SQRT);
        mapper.configure(params.maxIterations(), palette.length);

        BufferedImage img = new BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_RGB);
        for (int dstX = 0; dstX < dstW; dstX++) {
            int srcX = Math.min((int) ((dstX + 0.5) * srcW / dstW), srcW - 1);
            for (int dstY = 0; dstY < dstH; dstY++) {
                int srcY = Math.min((int) ((dstY + 0.5) * srcH / dstH), srcH - 1);
                img.setRGB(dstX, dstY, palette[mapper.map(iters[srcX][srcY])].getRGB());
            }
        }
        return img;
    }
}
