package de.cm.mandelproto.gui;

import javax.swing.*;
import java.awt.*;

public class RunningColorsFrame extends JFrame {

    private final MainFrame mainFrame;
    private final JCheckBox cb_active;
    private final JSlider sl_speed;
    private final JSlider sl_red;
    private final JSlider sl_green;
    private final JSlider sl_blue;
    private final JRadioButton rb_forward;
    private final JRadioButton rb_backward;

    public RunningColorsFrame(MainFrame mainFrame) {
        super("Running Colors");
        this.mainFrame = mainFrame;

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Zeile 1: Aktiv-Checkbox
        panel.add(new JLabel("Aktiv"));
        cb_active = new JCheckBox();
        cb_active.addActionListener(e -> {
            if (cb_active.isSelected()) {
                mainFrame.startCycling(getInterval(), getDeviationR(), getDeviationG(), getDeviationB());
            } else {
                mainFrame.stopCycling();
            }
        });
        panel.add(cb_active);

        // Zeile 2: Geschwindigkeit (50–500ms, invertiert: links = schnell)
        panel.add(new JLabel("Geschwindigkeit"));
        sl_speed = new JSlider(50, 500, 100);
        sl_speed.addChangeListener(e -> {
            if (!sl_speed.getValueIsAdjusting()) mainFrame.setCycleInterval(getInterval());
        });
        panel.add(sl_speed);

        // Zeile 3–5: Abweichung pro Kanal (0–255, quadratisch gemappt)
        panel.add(new JLabel("Abweichung Rot"));
        sl_red = new JSlider(0, 255, 80);
        sl_red.addChangeListener(e -> {
        if (!sl_red.getValueIsAdjusting()) mainFrame.setCycleDeviationR(getDeviationR());
        });
        panel.add(sl_red);

        panel.add(new JLabel("Abweichung Grün"));
        sl_green = new JSlider(0, 255, 80);
        sl_green.addChangeListener(e -> {
            if (!sl_green.getValueIsAdjusting()) mainFrame.setCycleDeviationG(getDeviationG());
        });
        panel.add(sl_green);

        panel.add(new JLabel("Abweichung Blau"));
        sl_blue = new JSlider(0, 255, 80);
        sl_blue.addChangeListener(e -> {
            if (!sl_blue.getValueIsAdjusting()) mainFrame.setCycleDeviationB(getDeviationB());
        });
        panel.add(sl_blue);

        // Zeile 6: Richtung
        panel.add(new JLabel("Richtung"));
        JPanel dirPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rb_forward  = new JRadioButton("innen → außen", true);
        rb_backward = new JRadioButton("außen → innen");
        ButtonGroup dirGroup = new ButtonGroup();
        dirGroup.add(rb_forward);
        dirGroup.add(rb_backward);
        rb_forward.addActionListener(e  -> mainFrame.setCycleDirection(true));
        rb_backward.addActionListener(e -> mainFrame.setCycleDirection(false));
        dirPanel.add(rb_forward);
        dirPanel.add(rb_backward);
        panel.add(dirPanel);

        add(panel);
        pack();
        setResizable(false);
        setAlwaysOnTop(true);
    }

    /** Wird von MainFrame nach jedem Rerender aufgerufen, um Cycling-Zustand wiederherzustellen. */
    public void applyCyclingState() {
        mainFrame.setCycleDirection(rb_forward.isSelected());
        if (cb_active.isSelected()) {
            mainFrame.startCycling(getInterval(), getDeviationR(), getDeviationG(), getDeviationB());
        }
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

    private int getDeviationR() { return mapDeviation(sl_red); }
    private int getDeviationG() { return mapDeviation(sl_green); }
    private int getDeviationB() { return mapDeviation(sl_blue); }
}
