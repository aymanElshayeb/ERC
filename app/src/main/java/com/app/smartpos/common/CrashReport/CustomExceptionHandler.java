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

    static private DatabaseAccess databaseAccess;
    /*
     * if any of the parameters is null, the respective functionality
     * will not be used
     */
    public CustomExceptionHandler(Context context) {
        databaseAccess = DatabaseAccess.getInstance(context);
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
        for (StackTraceElement element : elements) {
            body += element.toString() + "\n";
        }
        Utils.addLog("datadata_crash", body);
        databaseAccess.open();
        boolean flag=databaseAccess.addReport(configuration.get("ecr_code"),configuration.get("merchant_id"),toast,body,e.getMessage()+"" );
        Utils.addLog("datadata_crash", ""+flag);
    }
}