package com.app.smartpos.settings.Synchronization.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class DecompressWorker extends Worker {

    public DecompressWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String fileName = getInputData().getString("fileName");
        if (fileName == null) {
            return Result.failure();
        }

        String path = getApplicationContext().getCacheDir().getAbsolutePath();
        File gzipFile = new File(path, fileName);
        File outputFile = new File(path, removeGzipExtension(fileName));
        if (outputFile.exists()) {
            outputFile.delete();
        }
        if (decompressGzip(gzipFile, outputFile)) {
            return Result.success();
        } else {
            return Result.failure();
        }
    }

    private boolean decompressGzip(File gzipFile, File outputFile) {
        try (FileInputStream fis = new FileInputStream(gzipFile);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String removeGzipExtension(String fileName) {
        if (fileName != null && fileName.endsWith(".gz")) {
            return fileName.substring(0, fileName.length() - 3);
        }
        return fileName;
    }
}
