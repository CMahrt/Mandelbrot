package de.cm.mandelproto.math;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class IterationMap {

    @Getter protected ComplexNumber center;
    @Getter protected double width;
    @Getter protected double height;

    @Getter protected int cols;
    @Getter protected int rows;
    @Getter protected int maxIterations;

    protected IterablePoint[][] points;

    private int iterations;

    protected IterationMap(RenderParameters params) {
        this.center = params.center();
        this.width = params.complexWidth();
        this.height = params.complexHeight();
        this.cols = params.pixelWidth();
        this.rows = params.pixelHeight();
        this.maxIterations = params.maxIterations();
        points = new IterablePoint[cols][rows];
        iterations = 0;
        init();
    }

    @SuppressWarnings("unused")
    public boolean iterate() {
        boolean result = false;
        if (iterations < maxIterations) {
            iterations++;
            for (int i = 0; i < cols; i++)
                for (int j = 0; j < rows; j++) {
                    result |= points[i][j].iterate();
                }
        }
        return result;
    }

    public void tileIterate() {
        tileIterate(0, 0, cols, rows);
    }

    private void tileIterate(int left, int top, int right, int bottom) {
        boolean floodTile = true;
        log.trace("tileIterate {}, {} to {}, {}", left, top, right, bottom);
        for (int i = left; i < right; i++) {
            iterateFully(points[i][top]);
            floodTile &= points[i][top].getIteration() == maxIterations;
            iterateFully(points[i][bottom - 1]);
            floodTile &= points[i][bottom - 1].getIteration() == maxIterations;
        }
        for (int i = top; i < bottom; i++) {
            iterateFully(points[left][i]);
            floodTile &= points[left][i].getIteration() == maxIterations;
            iterateFully(points[right - 1][i]);
            floodTile &= points[right - 1][i].getIteration() == maxIterations;
        }
        if (floodTile) {
            log.trace("flood {}, {} to {}, {}", left, top, right, bottom);
            for (int i = left + 1; i < right - 1; i++) {
                for (int j = top; j < bottom - 1; j++) {
                    points[i][j].setIteration(maxIterations);
                }
            }
        } else {
            int widthMiddle = (left + right) / 2;
            int heightMiddle = (top + bottom) / 2;
            log.trace("tile {}, {}", widthMiddle, heightMiddle);
            tileIterate(left + 1, top + 1, widthMiddle, heightMiddle);
            tileIterate(widthMiddle, top + 1, right - 1, heightMiddle);
            tileIterate(left + 1, heightMiddle, widthMiddle, bottom - 1);
            tileIterate(widthMiddle, heightMiddle, right - 1, bottom - 1);
        }
    }

    private void iterateFully(IterablePoint point) {
        while (point.getIteration() < maxIterations) {
            if (!point.iterate()) break;
        }
    }

    public abstract void init();

    public int getIterationForCoordinate(int x, int y) {
        if (x >= cols || y >= rows) return 0;
        return points[x][y].getIteration();
    }

    abstract public ComplexNumber getComplexNumberForCoordinate(int x, int y);
}
