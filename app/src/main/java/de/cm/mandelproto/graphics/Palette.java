package de.cm.mandelproto.graphics;

import lombok.Getter;
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
    @Getter private int intervalMs = 450;
    @Getter @Setter private int deviationR = 5;
    @Getter @Setter private int deviationG = 5;
    @Getter @Setter private int deviationB = 5;
    @Getter @Setter private boolean forward      = true;
    @Getter @Setter private boolean excludeInner = false;
    private int signR = 1, signG = 1, signB = 1;

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
        this.intervalMs = intervalMs;
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

    public boolean isCycling()       { return cycleTimer != null; }
    public void setInterval(int ms) { this.intervalMs = ms; if (cycleTimer != null) cycleTimer.setDelay(ms); }

    public void addChangeListener(ChangeListener l)    { listeners.add(l); }
    public void removeChangeListener(ChangeListener l) { listeners.remove(l); }

    private void rotatePalette() {
        int last     = colors.length - 1;
        int cycleEnd = excludeInner ? last - 1 : last;

        if (forward) {
            Color neighbor = colors[cycleEnd];
            System.arraycopy(colors, 1, colors, 0, cycleEnd);
            colors[cycleEnd] = nextColor(neighbor);
        } else {
            Color neighbor = colors[0];
            System.arraycopy(colors, 0, colors, 1, cycleEnd);
            colors[0] = nextColor(neighbor);
        }
    }

    private Color nextColor(Color base) {
        int r = Math.clamp(base.getRed()   + signR * (rnd.nextInt(deviationR) + 1), 0, 255);
        int g = Math.clamp(base.getGreen() + signG * (rnd.nextInt(deviationG) + 1), 0, 255);
        int b = Math.clamp(base.getBlue()  + signB * (rnd.nextInt(deviationB) + 1), 0, 255);
        if (r == 0 || r == 255) signR = -signR;
        if (g == 0 || g == 255) signG = -signG;
        if (b == 0 || b == 255) signB = -signB;
        return new Color(r, g, b);
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
