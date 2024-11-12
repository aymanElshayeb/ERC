package com.app.smartpos.devices.newland;

public enum NewLandPrinterStatus {
    OUT_OF_PAPER("out of paper"),
    OVER_HEAT("over heat"),
    UNDER_VOLTAGE("under voltage"),
    BUSY("busy")
    ;

    NewLandPrinterStatus(String key) {
        this.key = key;
    }
    private String key;

    public String getKey() {
        return key;
    }

    public static NewLandPrinterStatus getStatus(String status) {
        status = status.toLowerCase().trim().replace(".","");
        for (NewLandPrinterStatus b : NewLandPrinterStatus.values()) {
            if (status.contains(String.valueOf(b.key).toLowerCase())) {
                return b;
            }
        }
        return null;
    }
}
