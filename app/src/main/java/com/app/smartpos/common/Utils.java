package com.app.smartpos.common;

import java.text.DecimalFormat;

public class Utils {

    public static String trimLongDouble(double value){
//        String stringValue=value+"";
//        String partOne=stringValue.split("\\.")[0];
//        String partTwo=stringValue.split("\\.")[1];
//        if(partTwo.length()<=2){
//            return stringValue;
//        }else{
//            return partOne+"."+partTwo.charAt(0)+partTwo.charAt(1);
//        }

        DecimalFormat f;
        f = new DecimalFormat("#.00");
        return value == 0 ? "0" : f.format(value);
    }

    public static String trimLongDouble(String value){
        double doubleValue=Double.parseDouble(value);
        DecimalFormat f;
        f = new DecimalFormat("#.00");
        return value.equals("0") ? "0" : f.format(doubleValue);
    }
}
