package de.cm.mandelproto.math;

public record RenderParameters(
        ComplexNumber center,
        double complexWidth,
        double complexHeight,
        int pixelWidth,
        int maxIterations
) {
    public int pixelHeight() {
        return (int) ((complexHeight / complexWidth) * pixelWidth);
    }
}
