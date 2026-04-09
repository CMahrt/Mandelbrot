package de.cm.mandelproto.math;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MandelbrotPointMap extends IterationMap {
    double step ;
    double left ;
    double top ;

    public MandelbrotPointMap(RenderParameters params) {
        super(params);
    }

    /**
     * Erzeugt eine MandelbrotPointMap aus vorberechneten Iterationsdaten (z.B. aus einer .mfrac-Datei).
     * Die Punkte werden normal initialisiert (korrekte komplexe Koordinaten), nur die Iterationswerte
     * werden aus {@code data} übernommen statt berechnet.
     *
     * @param data col-major: {@code data[col][row]}
     */
    public static MandelbrotPointMap fromData(RenderParameters params, int[][] data) {
        MandelbrotPointMap map = new MandelbrotPointMap(params);
        for (int col = 0; col < map.getCols(); col++)
            for (int row = 0; row < map.getRows(); row++)
                map.points[col][row].setIteration(data[col][row]);
        return map;
    }

    @Override
    public void init() {
        step = width / cols;
        left = center.getReal() - width / 2;
        top = center.getImag() + height / 2;
        for (int i = 0; i < cols; i++)
            for (int j = 0; j < rows; j++) {
                points[i][j] =
                        new MandelbrotPoint(
                                new ComplexNumber(
                                        left + i * step,
                                        top - j * step
                                )
                        );
            }

        log.debug("rows = {}", rows);
        log.debug("cols = {}", cols);
        log.debug("top , bottom = {},{}", top, (top - rows * step));
        log.debug("left , right = {},{}", left, (left + cols * step));
    }

    @Override
    public ComplexNumber getComplexNumberForCoordinate(int x, int y) {
        return new ComplexNumber(left + x * step, top - y * step);
    }
}
