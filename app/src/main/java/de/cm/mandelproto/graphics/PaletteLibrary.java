package de.cm.mandelproto.graphics;

import java.awt.*;

public class PaletteLibrary {

    public static final String[] NAMES = {"Graustufen", "Feuer", "Regenbogen", "Ozean", "Running Colors"};

    public static Color[] byName(String name, int size) {
        Color[] colors = switch (name) {
            case "Feuer"      -> fire(size);
            case "Regenbogen" -> rainbow(size);
            case "Ozean"      -> ocean(size);
            default           -> grayscale(size);
        };
        colors[colors.length - 1] = new Color(0, 0, 0);  // reservierter Innen-Eintrag
        return colors;
    }

    public static Color[] grayscale(int size) {
        Color[] p = new Color[size];
        for (int i = 0; i < size; i++) p[i] = new Color(i * 255 / (size - 1), i * 255 / (size - 1), i * 255 / (size - 1));
        return p;
    }

    public static Color[] fire(int size) {
        // schwarz → rot → orange → gelb → weiß
        Color[] keyColors = {
            new Color(0, 0, 0),
            new Color(180, 0, 0),
            new Color(255, 100, 0),
            new Color(255, 220, 0),
            new Color(255, 255, 255)
        };
        return interpolate(keyColors, size);
    }

    public static Color[] rainbow(int size) {
        Color[] p = new Color[size];
        for (int i = 0; i < size; i++) {
            p[i] = Color.getHSBColor(i / (float) size, 1f, 1f);
        }
        return p;
    }

    public static Color[] ocean(int size) {
        // schwarz → dunkelblau → cyan → weiß
        Color[] keyColors = {
            new Color(0, 0, 0),
            new Color(0, 0, 128),
            new Color(0, 128, 255),
            new Color(0, 220, 220),
            new Color(255, 255, 255)
        };
        return interpolate(keyColors, size);
    }

    private static Color[] interpolate(Color[] keys, int size) {
        Color[] result = new Color[size];
        int segments = keys.length - 1;
        for (int i = 0; i < size; i++) {
            double t = (double) i / (size - 1) * segments;
            int seg = Math.min((int) t, segments - 1);
            double f = t - seg;
            Color a = keys[seg];
            Color b = keys[seg + 1];
            result[i] = new Color(
                (int) (a.getRed()   + f * (b.getRed()   - a.getRed())),
                (int) (a.getGreen() + f * (b.getGreen() - a.getGreen())),
                (int) (a.getBlue()  + f * (b.getBlue()  - a.getBlue()))
            );
        }
        return result;
    }
}
