package com.app.smartpos.settings.Synchronization.workers;


import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.common.Utils;
import com.app.smartpos.utils.SSLUtils;
import com.app.smartpos.utils.SharedPrefUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ProductImagesWorker extends Worker {

    public ProductImagesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Utils.addLog("datadata_download", "start");
        String urlString = getInputData().getString("url");
        String fileName = getInputData().getString("fileName");
        String authorization = getInputData().getString("Authorization");
        String tenantId = getInputData().getString("tenantId");
        String ecrCode = getInputData().getString("ecrCode");
        if (urlString == null || fileName == null) {
            Utils.addLog("datadata_download", "failed 1");
            return Result.failure();
        }
        SSLUtils.trustAllCertificates();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setHostnameVerifier((hostname, session) -> true);
            }
            connection.setRequestMethod("POST");
            connection.setRequestProperty("tenantId", tenantId);
            connection.setRequestProperty("Authorization", authorization);
            connection.setRequestProperty("apikey", SharedPrefUtils.getApiKey());
            connection.setRequestProperty("ecrCode", ecrCode);
            Utils.addLog("datadata_download", "request");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Utils.addLog("datadata_download", "loading");
                File outputFile = new File(getApplicationContext().getCacheDir().getAbsolutePath(), fileName);
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                Utils.addLog("datadata_download", "delete");
                downloadFile(connection.getInputStream(), outputFile);

                connection.disconnect();
                return Result.success();
            } else {
                Utils.addLog("datadata_download", "disconnected " + connection.getResponseCode() + " " + connection.getResponseMessage());
                // Handle error response
                connection.disconnect();
                return Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private boolean downloadFile(InputStream inputStream, File outputFile) {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                Utils.addLog("datadata_download", "downloading");
                fos.write(buffer, 0, length);
            }

            return true;
        } catch (IOException e) {
            addToDatabase(e,"downloadFile-productImagesWorker");
            e.printStackTrace();
            return false;
        }
    }
}
