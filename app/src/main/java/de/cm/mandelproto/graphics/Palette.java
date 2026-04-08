package de.cm.mandelproto.graphics;

import lombok.Setter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Palette {

    private Color[] colors;
    private final List<ChangeListener> listeners = new ArrayList<>();
    private final Random rnd = new Random();
    private Timer cycleTimer;
    @Setter private int deviationR = 5;
    @Setter private int deviationG = 5;
    @Setter private int deviationB = 5;
    @Setter private boolean forward = true;

    public Palette(Color[] initial) {
        this.colors = initial.clone();
    }

    public int getColor(int index) {
        return colors[index % colors.length].getRGB();
    }

    public int size() { return colors.length; }

    public void loadColors(Color[] newColors) {
        this.colors = newColors.clone();
        fireChange();
    }

    public void startCycling(int intervalMs) {
        if (cycleTimer != null) cycleTimer.stop();
        cycleTimer = new Timer(intervalMs, e -> { rotatePalette(); fireChange(); });
        cycleTimer.start();
    }

    public void stopCycling() {
        if (cycleTimer != null) {
            cycleTimer.stop();
            cycleTimer = null;
        }
    }

    public void setInterval(int ms) { if (cycleTimer != null) cycleTimer.setDelay(ms); }

    public void addChangeListener(ChangeListener l)    { listeners.add(l); }
    public void removeChangeListener(ChangeListener l) { listeners.remove(l); }

    private void rotatePalette() {
        Color neighbor = forward ? colors[colors.length - 1] : colors[0];
        if (forward) System.arraycopy(colors, 1, colors, 0, colors.length - 1);
        else         System.arraycopy(colors, 0, colors, 1, colors.length - 1);
        colors[forward ? colors.length - 1 : 0] = randomDeviation(neighbor);
    }

    private Color randomDeviation(Color base) {
        return new Color(
            Math.clamp(base.getRed()   + rnd.nextInt(2 * deviationR + 1) - deviationR, 0, 255),
            Math.clamp(base.getGreen() + rnd.nextInt(2 * deviationG + 1) - deviationG, 0, 255),
            Math.clamp(base.getBlue()  + rnd.nextInt(2 * deviationB + 1) - deviationB, 0, 255)
        );
    }

    private void fireChange() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::fireChange);
            return;
        }
        ChangeEvent evt = new ChangeEvent(this);
        List.copyOf(listeners).forEach(l -> l.stateChanged(evt));
    }
}
