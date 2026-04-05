package de.cm.mandelproto.gui;

import de.cm.mandelproto.I18n;
import de.cm.mandelproto.graphics.Palette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RunningColorsFrame extends JFrame {

    private final JCheckBox cb_active;
    private final JSlider sl_speed;
    private final JSlider sl_red;
    private final JSlider sl_green;
    private final JSlider sl_blue;

    private final JLabel lbl_active;
    private final JLabel lbl_speed;
    private final JLabel lbl_red;
    private final JLabel lbl_green;
    private final JLabel lbl_blue;
    private final JLabel lbl_direction;
    private final JRadioButton rb_forward;
    private final JRadioButton rb_backward;

    private final Runnable langListener = this::applyTexts;

    public RunningColorsFrame(Palette palette) {
        super();

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        lbl_active = new JLabel();
        cb_active = new JCheckBox();
        cb_active.addActionListener(e -> {
            if (cb_active.isSelected()) {
                palette.startCycling(getInterval());
            } else {
                palette.stopCycling();
            }
        });
        panel.add(lbl_active);
        panel.add(cb_active);

        lbl_speed = new JLabel();
        sl_speed = new JSlider(50, 500, 100);
        sl_speed.addChangeListener(e -> {
            if (!sl_speed.getValueIsAdjusting()) palette.setInterval(getInterval());
        });
        panel.add(lbl_speed);
        panel.add(sl_speed);

        lbl_red = new JLabel();
        sl_red = new JSlider(0, 255, 80);
        sl_red.addChangeListener(e -> {
            if (!sl_red.getValueIsAdjusting()) palette.setDeviationR(mapDeviation(sl_red));
        });
        panel.add(lbl_red);
        panel.add(sl_red);

        lbl_green = new JLabel();
        sl_green = new JSlider(0, 255, 80);
        sl_green.addChangeListener(e -> {
            if (!sl_green.getValueIsAdjusting()) palette.setDeviationG(mapDeviation(sl_green));
        });
        panel.add(lbl_green);
        panel.add(sl_green);

        lbl_blue = new JLabel();
        sl_blue = new JSlider(0, 255, 80);
        sl_blue.addChangeListener(e -> {
            if (!sl_blue.getValueIsAdjusting()) palette.setDeviationB(mapDeviation(sl_blue));
        });
        panel.add(lbl_blue);
        panel.add(sl_blue);

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
        panel.add(lbl_direction);
        panel.add(dirPanel);

        applyTexts();
        I18n.addListener(langListener);

        add(panel);
        pack();
        setResizable(false);
        setAlwaysOnTop(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                palette.stopCycling();
                I18n.removeListener(langListener);
            }
        });
    }

    private void applyTexts() {
        setTitle(I18n.get("frame.runningColors"));
        lbl_active.setText(I18n.get("label.active"));
        lbl_speed.setText(I18n.get("label.speed"));
        lbl_red.setText(I18n.get("label.deviationRed"));
        lbl_green.setText(I18n.get("label.deviationGreen"));
        lbl_blue.setText(I18n.get("label.deviationBlue"));
        lbl_direction.setText(I18n.get("label.direction"));
        rb_forward.setText(I18n.get("direction.inward"));
        rb_backward.setText(I18n.get("direction.outward"));
    }

    /** Slider-Wert invertiert: kleiner Wert = schnell (kleines Intervall). */
    private int getInterval() {
        return 550 - sl_speed.getValue();
    }

    /** Quadratische Kurve: slider 0–255 → deviation 0–50. */
    private int mapDeviation(JSlider sl) {
        double t = sl.getValue() / 255.0;
        return (int) Math.round(t * t * 50);
    }
}
