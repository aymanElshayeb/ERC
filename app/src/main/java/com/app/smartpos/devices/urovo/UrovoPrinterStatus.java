package com.app.smartpos.devices.urovo;

public enum UrovoPrinterStatus {
    OK(0,0),
    OUT_OF_PAPER(240,-1),
    OVER_HEAT(243,-2),
    UNDER_VOLTAGE(225,-3),
    BUSY(247,-4)
    ;

    UrovoPrinterStatus(int key, int value) {
        this.key = key;
        this.value = value;
    }
    private int key;
    private int value;

    public int getKey() {
        return key;
    }
}
