package de.cm.mandelproto.math;

public class MandelbrotPoint implements IterablePoint {

    private ComplexNumber result;
    private int iteration;
    private final ComplexNumber start;

    public MandelbrotPoint(ComplexNumber start){
        result = new ComplexNumber(0,0);
        iteration = 0;
        this.start = start;
    }

    @Override
    public boolean iterate() {
        if(result.abs()<=2){
                iteration++;
                result = ComplexNumber.add(ComplexNumber.mult(result,result),start);
                return true;
        }
        return false;
    }

    @Override
    public int getIteration() {
        return iteration;
    }

    @Override
    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    @Override
    public void reset() {
        result = new ComplexNumber(0, 0);
        iteration = 0;
    }
}
