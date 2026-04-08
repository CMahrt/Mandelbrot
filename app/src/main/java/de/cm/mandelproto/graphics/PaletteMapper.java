package de.cm.mandelproto.graphics;

@lombok.Getter @lombok.Setter
public class PaletteMapper {

    public enum Curve { LINEAR, SQRT, LOG }

    private Curve curve = Curve.LINEAR;
    private int maxIterations = 1;
    private int paletteSize   = 256;

    public void configure(int maxIterations, int paletteSize) {
        this.maxIterations = maxIterations;
        this.paletteSize   = paletteSize;
    }

    /**
     * Mappt eine rohe Iterationszahl auf einen Palettenindex.
     * Innen-Punkte  (>= maxIterations)   → paletteSize-1  (reservierter letzter Eintrag, schwarz)
     * Außen-Punkte  (0..maxIterations-1) → 0..paletteSize-2 je nach Kurve
     */
    public int map(int iteration) {
        if (iteration >= maxIterations) return paletteSize - 1;
        double t = (double) iteration / maxIterations;
        double mapped = switch (curve) {
            case LINEAR -> t;
            case SQRT   -> Math.sqrt(t);
            case LOG    -> Math.log1p(t * (Math.E - 1));
        };
        return (int) (mapped * (paletteSize - 1));  // Bereich 0..paletteSize-2
    }
}
