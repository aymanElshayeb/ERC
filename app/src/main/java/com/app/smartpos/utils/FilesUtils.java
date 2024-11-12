package com.app.smartpos.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class FilesUtils {

    static public void generateNoteOnSD(String sFileName, StackTraceElement[] sBody, DatabaseAccess databaseAccess) {
        try {
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ECR-Error-Logs");
            if (!root.exists()) {
                Utils.addLog("datadata_dir",root.mkdir()+"");
            }
            HashMap<String, String> configuration = databaseAccess.getConfiguration();
            String ecr_code = configuration.isEmpty() ? "" : configuration.get("ecr_code");
            String merchant_id = configuration.isEmpty() ? "" : configuration.get("merchant_id");
            databaseAccess.open();

            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy-MM-dd--HH-mm");
            File gpxfile = new File(root, sFileName+"-"+simpleDateFormat.format(new Date())+".txt");

            FileWriter writer = new FileWriter(gpxfile);
            writer.append(ecr_code);
            writer.append(merchant_id);
            for(int i=0;i<sBody.length;i++) {
                writer.append(sBody[i].toString()+"\n");
            }
            writer.flush();
            writer.close();
            //Utils.addLog("datadata_dir",root.mkdir()+"");
            //Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
