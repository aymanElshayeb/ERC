package com.app.smartpos.common;

public class Utils {

    public static String trimLongDouble(double value){
        String stringValue=value+"";
        String partOne=stringValue.split("\\.")[0];
        String partTwo=stringValue.split("\\.")[1];
        if(partTwo.length()<=2){
            return stringValue;
        }else{
            return partOne+"."+partTwo.charAt(0)+partTwo.charAt(1);
        }
    }
}
