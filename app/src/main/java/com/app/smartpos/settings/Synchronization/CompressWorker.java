package com.app.smartpos.settings.Synchronization;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class CompressWorker extends Worker {

    public CompressWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        String fileName = getInputData().getString("fileName");
        if (fileName == null) {
            return Result.failure();
        }

        File inputFile = new File(getApplicationContext().getCacheDir().getAbsolutePath(), fileName);
        File gzipFile = new File(getApplicationContext().getCacheDir().getAbsolutePath(), fileName+".gz");
        if (compressGzip(inputFile,gzipFile)) {
            Data outputData = new Data.Builder().putString("fileNameGzip",gzipFile.getName())
                    .putString("Authorization",getInputData().getString("Authorization"))
                    .build();
            return Result.success(outputData);
        } else {
            return Result.failure();
        }
    }
    private  boolean compressGzip(File inputFilePath, File outputFilePath)  {
        try (FileInputStream fis = new FileInputStream(inputFilePath);
             FileOutputStream fos = new FileOutputStream(outputFilePath);
             GZIPOutputStream gos = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                gos.write(buffer, 0, length);
            }
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
