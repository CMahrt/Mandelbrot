package de.cm.mandelproto.graphics;

import java.awt.*;

public class Palette {

    public static final String[] NAMES = {"Graustufen", "Feuer", "Regenbogen", "Ozean"};

    public static Color[] byName(String name) {
        return switch (name) {
            case "Feuer"      -> fire();
            case "Regenbogen" -> rainbow();
            case "Ozean"      -> ocean();
            default           -> grayscale();
        };
    }

    public static Color[] grayscale() {
        Color[] p = new Color[256];
        for (int i = 0; i < 256; i++) p[i] = new Color(i, i, i);
        return p;
    }

    public static Color[] fire() {
        // schwarz → rot → orange → gelb → weiß
        Color[] keyColors = {
            new Color(0, 0, 0),
            new Color(180, 0, 0),
            new Color(255, 100, 0),
            new Color(255, 220, 0),
            new Color(255, 255, 255)
        };
        return interpolate(keyColors, 256);
    }

    public static Color[] rainbow() {
        Color[] p = new Color[256];
        for (int i = 0; i < 256; i++) {
            p[i] = Color.getHSBColor(i / 256f, 1f, 1f);
        }
        return p;
    }

    public static Color[] ocean() {
        // schwarz → dunkelblau → cyan → weiß
        Color[] keyColors = {
            new Color(0, 0, 0),
            new Color(0, 0, 128),
            new Color(0, 128, 255),
            new Color(0, 220, 220),
            new Color(255, 255, 255)
        };
        return interpolate(keyColors, 256);
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
