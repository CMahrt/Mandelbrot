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
