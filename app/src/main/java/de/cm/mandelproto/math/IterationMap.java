package de.cm.mandelproto.math;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class IterationMap {

    protected ComplexNumber center;
    protected double width;
    protected double height;

    protected int stepOnWidth;
    protected int stepOnHeight;
    protected int maxIterations;

    protected Iterable[][] points;

    private int iterations;

    protected IterationMap(ComplexNumber center, double width, double height, int stepOnWidth, int maxIterations) {
        this.center = center;
        this.width = width;
        this.height = height;
        this.stepOnWidth = stepOnWidth;
        this.stepOnHeight = (int) ((height/width) * stepOnWidth);
        this.maxIterations = maxIterations;
        points = new Iterable[stepOnWidth][stepOnHeight];
        iterations = 0;
        init();
    }

    public boolean iterate() {
        boolean result = false;
        if (iterations < maxIterations) {
            iterations++;
            for (int i = 0; i < stepOnWidth; i++)
                for (int j = 0; j < stepOnHeight; j++) {
                    result |= points[i][j].iterate();
                }
        }
        return result;
    }

    public void tileIterate() {
        tileIterate(0, 0, stepOnWidth, stepOnHeight);
    }

    private void tileIterate(int left, int top, int right, int bottom) {
        boolean floodTile = true;
        log.trace("tileIterate {}, {} to {}, {}", left, top, right, bottom);
        for (int i = left; i < right; i++) {
            while (points[i][top].getIteration() < maxIterations && points[i][top].iterate())
            floodTile &= points[i][top].getIteration() == maxIterations;
            while (points[i][bottom - 1].getIteration() < maxIterations && points[i][bottom - 1].iterate()) ;
            floodTile &= points[i][bottom - 1].getIteration() == maxIterations;
        }
        for (int i = top; i < bottom; i++) {
            while (points[left][i].getIteration() < maxIterations && points[left][i].iterate()) ;
            floodTile &= points[left][i].getIteration() == maxIterations;
            while (points[right - 1][i].getIteration() < maxIterations && points[right - 1][i].iterate()) ;
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
            if (right <= left && bottom <= top) {
                log.trace("all done");
                return;
            }
            int widthMiddle = (left + right) / 2;
            int heightMiddle = (top + bottom) / 2;
            log.trace("tile {}, {}", widthMiddle, heightMiddle);
            tileIterate(left + 1, top + 1, widthMiddle, heightMiddle);
            tileIterate(widthMiddle, top + 1, right - 1, heightMiddle);
            tileIterate(left + 1, heightMiddle, widthMiddle, bottom - 1);
            tileIterate(widthMiddle, heightMiddle, right - 1, bottom - 1);
        }
    }

    public abstract void init();

    public int getIterationForCoordinate(int x, int y) {
        if (x >= stepOnWidth || y >= stepOnHeight) return 0;
        return points[x][y].getIteration();
    }

    abstract public ComplexNumber getComplexNumberForCoordinate(int x, int y);

    public int getStepOnWidth() {
        return stepOnWidth;
    }

    public int getStepOnHeight() {
        return stepOnHeight;
    }

    public ComplexNumber getCenter() {
        return center;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
