package de.cm.mandelproto.gui;

import de.cm.mandelproto.I18n;
import de.cm.mandelproto.graphics.Palette;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

@Slf4j
public class RunningColorsDialog extends JDialog {

    private final Palette palette;
    private final Runnable langListener = this::applyTexts;

    private JButton btn_cycling;
    private JLabel lbl_speed;
    private JSlider sl_speed;
    private JLabel lbl_red, lbl_green, lbl_blue;
    private JSlider sl_red, sl_green, sl_blue;
    private JLabel lbl_direction;
    private JRadioButton rb_forward, rb_backward;
    private JLabel lbl_excludeInner;
    private JCheckBox cb_excludeInner;
    private JButton btn_close;

    public RunningColorsDialog(Window owner, Palette palette) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.palette = palette;
        buildUI();
        syncFromPalette();
        applyTexts();
        I18n.addListener(langListener);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    // ── UI-Aufbau ────────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btn_cycling = new JButton();
        btn_cycling.addActionListener(e -> {
            if (palette.isCycling()) {
                log.debug("Running Colors gestoppt");
                palette.stopCycling();
            } else {
                log.debug("Running Colors gestartet: interval={}ms", getInterval());
                palette.startCycling(getInterval());
            }
            updateCyclingButton();
        });
        btnPanel.add(btn_cycling);

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 4));
        addSpeedSlider(grid);
        addDeviationControls(grid);
        addDirectionControls(grid);
        addModeControls(grid);

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btn_close = new JButton();
        btn_close.addActionListener(e -> dispose());
        closePanel.add(btn_close);

        main.add(btnPanel);
        main.add(Box.createVerticalStrut(8));
        main.add(grid);
        main.add(Box.createVerticalStrut(8));
        main.add(closePanel);
        add(main);
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

    private void addDeviationControls(JPanel p) {
        lbl_red   = new JLabel(); sl_red   = addDeviationSlider(p, lbl_red,   palette::setDeviationR);
        lbl_green = new JLabel(); sl_green = addDeviationSlider(p, lbl_green, palette::setDeviationG);
        lbl_blue  = new JLabel(); sl_blue  = addDeviationSlider(p, lbl_blue,  palette::setDeviationB);
    }

    private JSlider addDeviationSlider(JPanel p, JLabel label, Consumer<Integer> setter) {
        JSlider sl = new JSlider(0, 255, 80);
        sl.addChangeListener(e -> {
            if (!sl.getValueIsAdjusting()) setter.accept(mapDeviation(sl));
        });
        p.add(label);
        p.add(sl);
        return sl;
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

    private void addModeControls(JPanel p) {
        lbl_excludeInner = new JLabel();
        cb_excludeInner  = new JCheckBox();
        cb_excludeInner.addActionListener(e -> palette.setExcludeInner(cb_excludeInner.isSelected()));
        p.add(lbl_excludeInner);
        p.add(cb_excludeInner);
    }

    // ── Texte & Cleanup ──────────────────────────────────────────────────────

    private void applyTexts() {
        setTitle(I18n.get("dialog.runningColors.title"));
        updateCyclingButton();
        lbl_speed.setText(I18n.get("label.speed"));
        lbl_red.setText(I18n.get("label.deviationRed"));
        lbl_green.setText(I18n.get("label.deviationGreen"));
        lbl_blue.setText(I18n.get("label.deviationBlue"));
        lbl_direction.setText(I18n.get("label.direction"));
        rb_forward.setText(I18n.get("direction.inward"));
        rb_backward.setText(I18n.get("direction.outward"));
        btn_close.setText(I18n.get("button.close"));
        lbl_excludeInner.setText(I18n.get("label.excludeInner"));
        pack();
    }

    private void updateCyclingButton() {
        btn_cycling.setText(I18n.get(palette.isCycling() ? "button.stopCycling" : "button.startCycling"));
    }

    @Override
    public void dispose() {
        I18n.removeListener(langListener);
        super.dispose();
    }

    // ── Hilfsmethoden ────────────────────────────────────────────────────────

    private void syncFromPalette() {
        sl_speed.setValue(550 - palette.getIntervalMs());
        sl_red.setValue(unmapDeviation(palette.getDeviationR()));
        sl_green.setValue(unmapDeviation(palette.getDeviationG()));
        sl_blue.setValue(unmapDeviation(palette.getDeviationB()));
        if (palette.isForward()) rb_forward.setSelected(true);
        else                     rb_backward.setSelected(true);
        cb_excludeInner.setSelected(palette.isExcludeInner());
    }

    private int getInterval() {
        return 550 - sl_speed.getValue();
    }

    private int mapDeviation(JSlider sl) {
        double t = sl.getValue() / 255.0;
        return (int) Math.round(t * t * 50);
    }

    private int unmapDeviation(int deviation) {
        return (int) (Math.sqrt(deviation / 50.0) * 255);
    }
}
