package de.cm.mandelproto.gui;

import de.cm.mandelproto.I18n;
import de.cm.mandelproto.graphics.Palette;
import de.cm.mandelproto.graphics.PaletteLibrary;
import de.cm.mandelproto.graphics.PaletteMapper;
import de.cm.mandelproto.math.RenderParameters;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class InspectorFrame extends JDialog {

    private final Palette palette;
    private final PaletteMapper paletteMapper;
    private final ImageFrame imageFrame;
    private final Runnable langListener = this::applyTexts;

    // Info section
    private JLabel lbl_centerRealKey, lbl_centerRealVal;
    private JLabel lbl_centerImagKey, lbl_centerImagVal;
    private JLabel lbl_complexWidthKey, lbl_complexWidthVal;
    private JLabel lbl_complexHeightKey, lbl_complexHeightVal;
    private JLabel lbl_pixelWidthKey, lbl_pixelWidthVal;
    private JLabel lbl_pixelHeightKey, lbl_pixelHeightVal;
    private JLabel lbl_maxIterationsKey, lbl_maxIterationsVal;

    // Color section
    private JLabel lbl_palette;
    private JComboBox<String> cb_palette;
    private JLabel lbl_paletteSize;
    private JComboBox<Integer> cb_paletteSize;
    private JLabel lbl_curve;
    private JComboBox<PaletteMapper.Curve> cb_curve;
    private JButton btn_openRunningColors;

    // Action
    private JButton btn_newSelection;
    private JButton btn_save;

    public InspectorFrame(RenderParameters params, Palette palette, PaletteMapper paletteMapper, ImageFrame imageFrame) {
        super(imageFrame, false);  // imageFrame als Owner, nicht modal
        this.palette = palette;
        this.paletteMapper = paletteMapper;
        this.imageFrame = imageFrame;
        buildUI();
        updateParams(params);
        applyTexts();
        I18n.addListener(langListener);
        pack();
        setResizable(false);
        setVisible(true);
    }

    // ── UI-Aufbau ────────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        main.add(buildInfoPanel());
        main.add(Box.createVerticalStrut(8));
        main.add(buildColorPanel());
        main.add(Box.createVerticalStrut(8));
        main.add(buildActionPanel());
        add(main);
    }

    private JPanel buildInfoPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 2));

        lbl_centerRealKey    = new JLabel(); lbl_centerRealVal    = new JLabel();
        lbl_centerImagKey    = new JLabel(); lbl_centerImagVal    = new JLabel();
        lbl_complexWidthKey  = new JLabel(); lbl_complexWidthVal  = new JLabel();
        lbl_complexHeightKey = new JLabel(); lbl_complexHeightVal = new JLabel();
        lbl_pixelWidthKey    = new JLabel(); lbl_pixelWidthVal    = new JLabel();
        lbl_pixelHeightKey   = new JLabel(); lbl_pixelHeightVal   = new JLabel();
        lbl_maxIterationsKey = new JLabel(); lbl_maxIterationsVal = new JLabel();

        for (JLabel val : new JLabel[]{lbl_centerRealVal, lbl_centerImagVal,
                lbl_complexWidthVal, lbl_complexHeightVal,
                lbl_pixelWidthVal, lbl_pixelHeightVal, lbl_maxIterationsVal}) {
            val.setHorizontalAlignment(JLabel.RIGHT);
        }

        p.add(lbl_centerRealKey);    p.add(lbl_centerRealVal);
        p.add(lbl_centerImagKey);    p.add(lbl_centerImagVal);
        p.add(lbl_complexWidthKey);  p.add(lbl_complexWidthVal);
        p.add(lbl_complexHeightKey); p.add(lbl_complexHeightVal);
        p.add(lbl_pixelWidthKey);    p.add(lbl_pixelWidthVal);
        p.add(lbl_pixelHeightKey);   p.add(lbl_pixelHeightVal);
        p.add(lbl_maxIterationsKey); p.add(lbl_maxIterationsVal);
        return p;
    }

    private JPanel buildColorPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 4));
        addPaletteChooser(p);
        addPaletteSizeChooser(p);
        addCurveChooser(p);
        addRunningColorsButton(p);
        return p;
    }

    private JPanel buildActionPanel() {
        btn_newSelection = new JButton();
        btn_newSelection.addActionListener(e -> imageFrame.enterSelectionMode());
        btn_save = new JButton();
        btn_save.addActionListener(e -> imageFrame.saveToFile());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(btn_newSelection);
        p.add(btn_save);
        return p;
    }

    // ── Einzelne Controls ────────────────────────────────────────────────────

    private void addPaletteChooser(JPanel p) {
        lbl_palette = new JLabel();
        cb_palette = new JComboBox<>(PaletteLibrary.NAMES);
        cb_palette.addActionListener(e -> regeneratePalette());
        p.add(lbl_palette);
        p.add(cb_palette);
    }

    private void addPaletteSizeChooser(JPanel p) {
        lbl_paletteSize = new JLabel();
        cb_paletteSize = new JComboBox<>(new Integer[]{4, 8, 16, 32, 64, 128, 256, 512, 1024});
        cb_paletteSize.setSelectedItem(256);
        cb_paletteSize.addActionListener(e -> regeneratePalette());
        p.add(lbl_paletteSize);
        p.add(cb_paletteSize);
    }

    private void addCurveChooser(JPanel p) {
        lbl_curve = new JLabel();
        cb_curve = new JComboBox<>(PaletteMapper.Curve.values());
        cb_curve.setSelectedItem(paletteMapper.getCurve());
        cb_curve.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value != null ? I18n.get("curve." + value.name()) : "");
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });
        cb_curve.addActionListener(e -> {
            paletteMapper.setCurve((PaletteMapper.Curve) cb_curve.getSelectedItem());
            imageFrame.drawImage();
        });
        p.add(lbl_curve);
        p.add(cb_curve);
    }

    private void addRunningColorsButton(JPanel p) {
        btn_openRunningColors = new JButton();
        btn_openRunningColors.setEnabled(false);
        btn_openRunningColors.addActionListener(e -> openRunningColorsDialog());
        p.add(new JLabel());
        p.add(btn_openRunningColors);
    }

    // ── Running Colors ───────────────────────────────────────────────────────

    private void openRunningColorsDialog() {
        new RunningColorsDialog(getOwner(), palette);
        // modal — setzt hier fort, sobald der Dialog geschlossen wurde
        updateRunningColorsState();
    }

    private void updateRunningColorsState() {
        boolean cycling = palette.isCycling();
        cb_palette.setEnabled(!cycling);
        cb_curve.setEnabled(!cycling);
        // cb_paletteSize bleibt disabled, solange "Running Colors" in der ComboBox steht
    }

    // ── Öffentliche API ──────────────────────────────────────────────────────

    public void updateParams(RenderParameters params) {
        lbl_centerRealVal.setText(String.format("%.6f", params.center().getReal()));
        lbl_centerImagVal.setText(String.format("%.6f", params.center().getImag()));
        lbl_complexWidthVal.setText(String.format("%.8f", params.complexWidth()));
        lbl_complexHeightVal.setText(String.format("%.8f", params.complexHeight()));
        lbl_pixelWidthVal.setText(String.valueOf(params.pixelWidth()));
        lbl_pixelHeightVal.setText(String.valueOf(params.pixelHeight()));
        lbl_maxIterationsVal.setText(String.valueOf(params.maxIterations()));
    }

    private void applyTexts() {
        setTitle(I18n.get("inspector.title"));
        lbl_centerRealKey.setText(I18n.get("field.centerReal"));
        lbl_centerImagKey.setText(I18n.get("field.centerImag"));
        lbl_complexWidthKey.setText(I18n.get("field.complexWidth"));
        lbl_complexHeightKey.setText(I18n.get("field.complexHeight"));
        lbl_pixelWidthKey.setText(I18n.get("field.pixelWidth"));
        lbl_pixelHeightKey.setText(I18n.get("field.pixelHeight"));
        lbl_maxIterationsKey.setText(I18n.get("field.maxIterations"));
        lbl_palette.setText(I18n.get("field.palette"));
        lbl_paletteSize.setText(I18n.get("label.paletteSize"));
        lbl_curve.setText(I18n.get("label.iterationCurve"));
        cb_curve.repaint();
        btn_openRunningColors.setText(I18n.get("button.configureRunningColors"));
        btn_newSelection.setText(I18n.get("button.newSelection"));
        btn_save.setText(I18n.get("button.saveFractal"));
        pack();
    }

    @Override
    public void removeNotify() {
        I18n.removeListener(langListener);
        palette.stopCycling();
        super.removeNotify();
    }

    // ── Hilfsmethoden ────────────────────────────────────────────────────────

    private void regeneratePalette() {
        String name  = (String)  cb_palette.getSelectedItem();
        Integer size = (Integer) cb_paletteSize.getSelectedItem();
        if (name == null || size == null) return;

        if ("Running Colors".equals(name)) {
            log.debug("Running Colors ausgewählt — Dialog öffnet");
            cb_paletteSize.setEnabled(false);
            btn_openRunningColors.setEnabled(true);
            openRunningColorsDialog();
        } else {
            log.debug("Palette neu generiert: name={}, size={}", name, size);
            palette.stopCycling();
            btn_openRunningColors.setEnabled(false);
            cb_palette.setEnabled(true);
            cb_paletteSize.setEnabled(true);
            cb_curve.setEnabled(true);
            palette.loadColors(PaletteLibrary.byName(name, size));
        }
    }
}
