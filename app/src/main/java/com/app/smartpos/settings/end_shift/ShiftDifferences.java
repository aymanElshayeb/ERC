package com.app.smartpos.settings.end_shift;

import java.io.Serializable;

public class ShiftDifferences implements Serializable {
    double real;
    double input;
    double diff;
    String code;

    public ShiftDifferences(double real, double input, double diff, String code) {
        this.real = real;
        this.input = input;
        this.diff = diff;
        this.code = code;
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

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "ShiftDifferences{" +
                "real=" + real +
                ", input=" + input +
                ", diff=" + diff +
                ", code='" + code + '\'' +
                '}';
    }
}
