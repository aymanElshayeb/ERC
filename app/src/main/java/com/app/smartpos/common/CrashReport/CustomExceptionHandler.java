package com.app.smartpos.common.CrashReport;

import android.content.Context;

import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;

    private static Context context;
    static private DatabaseAccess databaseAccess;
    /*
     * if any of the parameters is null, the respective functionality
     * will not be used
     */
    public CustomExceptionHandler(Context context) {
        databaseAccess = DatabaseAccess.getInstance(context);
        this.context=context;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
        addToDatabase(e,"crash");
        defaultUEH.uncaughtException(t, e);
    }

    static public void addToDatabase(Throwable e,String toast){
        databaseAccess.open();
        HashMap<String,String> configuration = databaseAccess.getConfiguration();
        StackTraceElement[] elements = e.getStackTrace();
        String body="";
        int maxLength = elements.length>8 ? 8 : elements.length;
        for (int i=0;i<maxLength;i++) {
            body += elements[i].toString() + "\n";
        }
        Utils.addLog("datadata_crash", body);
        databaseAccess.open();
        boolean flag=databaseAccess.addReport(configuration.get("ecr_code"),Utils.getDeviceId(context),toast,body,e.getMessage()+"" );
        Utils.addLog("datadata_crash", ""+flag);
    }
}