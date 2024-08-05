package com.app.smartpos.settings.end_shift;

public class ShiftDifferences {
    double real;
    double input;
    double diff;
    String type;

    public ShiftDifferences(double real, double input, double diff, String type) {
        this.real = real;
        this.input = input;
        this.diff = diff;
        this.type = type;
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

    public String getType() {
        return type;
    }
}
