package de.cm.mandelproto.graphics;

@lombok.Getter @lombok.Setter
public class PaletteMapper {

    public enum Curve { LINEAR, SQRT, LOG }

    private Curve curve = Curve.SQRT;
    private int minIterations = 0;
    private int maxIterations = 1;
    private int paletteSize   = 256;

    public void configure(int minIterations, int maxIterations, int paletteSize) {
        this.minIterations = minIterations;
        this.maxIterations = maxIterations;
        this.paletteSize   = paletteSize;
    }

    /**
     * Mappt eine rohe Iterationszahl auf einen Palettenindex.
     * Innen-Punkte  (>= maxIterations)        → paletteSize-1  (reservierter letzter Eintrag, schwarz)
     * Außen-Punkte  (minIterations..maxIter-1) → 0..paletteSize-2 je nach Kurve
     */
    public int map(int iteration) {
        if (iteration >= maxIterations) return paletteSize - 1;
        int range = maxIterations - minIterations;
        if (range <= 0) return 0;
        double t = (double) Math.max(0, iteration - minIterations) / range;
        double mapped = switch (curve) {
            case LINEAR -> t;
            case SQRT   -> Math.sqrt(t);
            case LOG    -> Math.log1p(t * (Math.E - 1));
        };
        return (int) (mapped * (paletteSize - 1));  // Bereich 0..paletteSize-2
    }
}
