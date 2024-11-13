package com.app.smartpos.common.CrashReport;

import com.app.smartpos.common.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;

    private String localPath;
    /*
     * if any of the parameters is null, the respective functionality
     * will not be used
     */
    public CustomExceptionHandler() {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
        StackTraceElement[] elements = e.getStackTrace();
        for(int i=0;i<elements.length;i++){
            Utils.addLog("datadata_crash",elements[i].toString());
        }
        defaultUEH.uncaughtException(t, e);
    }

}