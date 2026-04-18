package de.cm.mandelproto.gui;

public class Format {

    private Format() {}

    public static String zoom(double zoom) {
        if (zoom >= 1_000_000) return String.format("%.3e×", zoom);
        if (zoom >= 1_000)     return String.format("%.1f×", zoom);
        return String.format("%.2f×", zoom);
    }
}
