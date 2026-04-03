package de.cm.mandelproto.gui;

import de.cm.mandelproto.graphics.Palette;

import javax.swing.*;
import java.awt.*;

public class RunningColorsFrame extends JFrame {

    private final JCheckBox cb_active;
    private final JSlider sl_speed;
    private final JSlider sl_red;
    private final JSlider sl_green;
    private final JSlider sl_blue;

    public RunningColorsFrame(Palette palette) {
        super("Running Colors");

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Zeile 1: Aktiv-Checkbox
        panel.add(new JLabel("Aktiv"));
        cb_active = new JCheckBox();
        cb_active.addActionListener(e -> {
            if (cb_active.isSelected()) {
                palette.startCycling(getInterval());
            } else {
                palette.stopCycling();
            }
        });
        panel.add(cb_active);

        // Zeile 2: Geschwindigkeit (50–500ms, invertiert: links = schnell)
        panel.add(new JLabel("Geschwindigkeit"));
        sl_speed = new JSlider(50, 500, 100);
        sl_speed.addChangeListener(e -> {
            if (!sl_speed.getValueIsAdjusting()) palette.setInterval(getInterval());
        });
        panel.add(sl_speed);

        // Zeilen 3–5: Abweichung pro Kanal (quadratisch gemappt)
        panel.add(new JLabel("Abweichung Rot"));
        sl_red = new JSlider(0, 255, 80);
        sl_red.addChangeListener(e -> {
            if (!sl_red.getValueIsAdjusting()) palette.setDeviationR(mapDeviation(sl_red));
        });
        panel.add(sl_red);

        panel.add(new JLabel("Abweichung Grün"));
        sl_green = new JSlider(0, 255, 80);
        sl_green.addChangeListener(e -> {
            if (!sl_green.getValueIsAdjusting()) palette.setDeviationG(mapDeviation(sl_green));
        });
        panel.add(sl_green);

        panel.add(new JLabel("Abweichung Blau"));
        sl_blue = new JSlider(0, 255, 80);
        sl_blue.addChangeListener(e -> {
        if (!sl_blue.getValueIsAdjusting()) palette.setDeviationB(mapDeviation(sl_blue));
        });
        panel.add(sl_blue);

        // Zeile 6: Richtung
        panel.add(new JLabel("Richtung"));
        panel.add(createDirectionPanel(palette));

        add(panel);
        pack();
        setResizable(false);
        setAlwaysOnTop(true);
    }

    private JPanel createDirectionPanel(Palette palette) {
        JPanel dirPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JRadioButton rb_forward  = new JRadioButton("innen → außen", true);
        JRadioButton rb_backward = new JRadioButton("außen → innen");
        ButtonGroup dirGroup = new ButtonGroup();
        dirGroup.add(rb_forward);
        dirGroup.add(rb_backward);
        rb_forward.addActionListener(e  -> palette.setForward(true));
        rb_backward.addActionListener(e -> palette.setForward(false));
        dirPanel.add(rb_forward);
        dirPanel.add(rb_backward);
        return dirPanel;
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
