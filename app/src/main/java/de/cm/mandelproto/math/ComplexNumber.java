package de.cm.mandelproto.math;

import lombok.Getter;

@Getter
public class ComplexNumber {

    private final double real;
    private final double imag;

    public final static ComplexNumber NaN = new ComplexNumber(Double.NaN,Double.NaN);
    public final static ComplexNumber ZERO = new ComplexNumber(0d,0d);

    public ComplexNumber(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    public static ComplexNumber add(ComplexNumber c1, ComplexNumber c2) {
        return new ComplexNumber(c1.real + c2.real, c1.imag + c2.imag);
    }

    public static ComplexNumber minus(ComplexNumber c1, ComplexNumber c2) {
        return new ComplexNumber(c1.real - c2.real, c1.imag - c2.imag);
    }

    public static ComplexNumber mult(ComplexNumber c1, ComplexNumber c2) {
        return new ComplexNumber(
                (c1.real * c2.real) - (c1.imag * c2.imag),
                (c1.real * c2.imag) + (c1.imag * c2.real)
        );
    }
    public static ComplexNumber division(ComplexNumber c1, ComplexNumber c2) {
        if(c2.isZero()){
            return ComplexNumber.NaN;
        }
        return mult(c1,conjugate(c2)).scale(1/c2.sqrAbs());
    }

    public static ComplexNumber invert(ComplexNumber c){
        if(c.real == 0 && c.imag == 0){
            return new ComplexNumber(Double.NaN, Double.NaN);
        }
        double sqrAbs= c.sqrAbs();
        return new ComplexNumber(c.real,-c.imag).scale(sqrAbs);
    }
    public static ComplexNumber conjugate(ComplexNumber c){
        return new ComplexNumber(c.real,-c.imag);
    }

    public double sqrAbs(){
        return (real*real) + (imag*imag);
    }

    public double abs() {
        return Math.sqrt((real*real) + (imag*imag));
    }

    public ComplexNumber scale(double scale){
        return new ComplexNumber(this.real*scale,this.imag*scale);
    }

    public boolean isZero(){
        return real == 0 && imag == 0;
    }
    public boolean isNaN(){
        return Double.valueOf(real).equals(Double.NaN) || Double.valueOf(imag).equals(Double.NaN);
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof ComplexNumber) {
            ComplexNumber another = (ComplexNumber) o;
            return this.real == another.real && this.imag == another.imag;
        }
        return false;
    }
    @Override
    public int hashCode() {
        if (isNaN()) {
            return 7;
        }
        return 37 * (17 * Double.valueOf(imag).hashCode() + Double.valueOf(real).hashCode());
    }



}
