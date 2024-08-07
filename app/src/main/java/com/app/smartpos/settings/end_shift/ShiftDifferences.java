package com.app.smartpos.settings.end_shift;

public class ShiftDifferences {
    double real;
    double input;
    double diff;

    public ShiftDifferences(double real, double input, double diff) {
        this.real = real;
        this.input = input;
        this.diff = diff;
    }

    public double getReal() {
        return real;
    }

    public double getInput() {
        return input;
    }

    public double getDiff() {
        return diff;
    }

    @Override
    public String toString() {
        return "ShiftDifferences{" +
                "real=" + real +
                ", input=" + input +
                ", diff=" + diff +
                '}';
    }
}
