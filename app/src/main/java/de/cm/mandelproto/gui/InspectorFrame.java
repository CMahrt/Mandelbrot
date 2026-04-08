package de.cm.mandelproto.gui;

import de.cm.mandelproto.I18n;
import de.cm.mandelproto.graphics.Palette;
import de.cm.mandelproto.graphics.PaletteLibrary;
import de.cm.mandelproto.graphics.PaletteMapper;
import de.cm.mandelproto.math.RenderParameters;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

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
    private JLabel lbl_active;
    private JCheckBox cb_active;
    private JLabel lbl_speed;
    private JSlider sl_speed;
    private JLabel lbl_red, lbl_green, lbl_blue;
    private JLabel lbl_direction;
    private JRadioButton rb_forward, rb_backward;

    // Action
    private JButton btn_newSelection;

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

    // ── UI-Aufbau (Orchestratoren) ────────────────────────────────────────────

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
        addPaletteControls(p);
        addCyclingControls(p);
        addDeviationControls(p);
        addDirectionControls(p);
        return p;
    }

    private void addPaletteControls(JPanel p) {
        addPaletteChooser(p);
        addPaletteSizeChooser(p);
        addCurveChooser(p);
    }

    private void addCyclingControls(JPanel p) {
        addActiveCheckbox(p);
        addSpeedSlider(p);
    }

    private void addDeviationControls(JPanel p) {
        lbl_red   = new JLabel(); addDeviationSlider(p, lbl_red,   palette::setDeviationR);
        lbl_green = new JLabel(); addDeviationSlider(p, lbl_green, palette::setDeviationG);
        lbl_blue  = new JLabel(); addDeviationSlider(p, lbl_blue,  palette::setDeviationB);
    }

    private void addDirectionControls(JPanel p) {
        lbl_direction = new JLabel();
        rb_forward  = new JRadioButton("", true);
        rb_backward = new JRadioButton("");
        ButtonGroup dirGroup = new ButtonGroup();
        dirGroup.add(rb_forward);
        dirGroup.add(rb_backward);
        rb_forward.addActionListener(e  -> palette.setForward(true));
        rb_backward.addActionListener(e -> palette.setForward(false));
        JPanel dirPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dirPanel.add(rb_forward);
        dirPanel.add(rb_backward);
        p.add(lbl_direction);
        p.add(dirPanel);
    }

    private JPanel buildActionPanel() {
        btn_newSelection = new JButton();
        btn_newSelection.addActionListener(e -> imageFrame.enterSelectionMode());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(btn_newSelection);
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

    private void addActiveCheckbox(JPanel p) {
        lbl_active = new JLabel();
        cb_active = new JCheckBox();
        cb_active.addActionListener(e -> {
            if (cb_active.isSelected()) {
                log.debug("Running Colors gestartet: interval={}ms", getInterval());
                palette.startCycling(getInterval());
                cb_palette.setEnabled(false);
                cb_paletteSize.setEnabled(false);
            } else {
                log.debug("Running Colors gestoppt");
                palette.stopCycling();
                cb_palette.setEnabled(true);
                cb_paletteSize.setEnabled(true);
            }
        });
        p.add(lbl_active);
        p.add(cb_active);
    }

    private void addSpeedSlider(JPanel p) {
        lbl_speed = new JLabel();
        sl_speed = new JSlider(50, 500, 100);
        sl_speed.addChangeListener(e -> {
            if (!sl_speed.getValueIsAdjusting()) palette.setInterval(getInterval());
        });
        p.add(lbl_speed);
        p.add(sl_speed);
    }

    private void addDeviationSlider(JPanel p, JLabel label, Consumer<Integer> setter) {
        JSlider sl = new JSlider(0, 255, 80);
        sl.addChangeListener(e -> {
            if (!sl.getValueIsAdjusting()) setter.accept(mapDeviation(sl));
        });
        p.add(label);
        p.add(sl);
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
        lbl_active.setText(I18n.get("label.active"));
        lbl_speed.setText(I18n.get("label.speed"));
        lbl_red.setText(I18n.get("label.deviationRed"));
        lbl_green.setText(I18n.get("label.deviationGreen"));
        lbl_blue.setText(I18n.get("label.deviationBlue"));
        lbl_direction.setText(I18n.get("label.direction"));
        rb_forward.setText(I18n.get("direction.inward"));
        rb_backward.setText(I18n.get("direction.outward"));
        btn_newSelection.setText(I18n.get("button.newSelection"));
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
        String name   = (String)  cb_palette.getSelectedItem();
        Integer size  = (Integer) cb_paletteSize.getSelectedItem();
        if (name != null && size != null) {
            log.debug("Palette neu generiert: name={}, size={}", name, size);
            palette.loadColors(PaletteLibrary.byName(name, size));
        }
    }

    private int getInterval() {
        return 550 - sl_speed.getValue();
    }

    private int mapDeviation(JSlider sl) {
        double t = sl.getValue() / 255.0;
        return (int) Math.round(t * t * 50);
    }
}
