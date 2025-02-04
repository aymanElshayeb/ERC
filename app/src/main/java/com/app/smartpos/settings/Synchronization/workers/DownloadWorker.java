package com.app.smartpos.settings.Synchronization.workers;


import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.common.Utils;
import com.app.smartpos.utils.SSLUtils;
import com.app.smartpos.utils.SharedPrefUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class DownloadWorker extends Worker {

    int statusCode=-5;
    HttpURLConnection connection;
    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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
        Utils.addLog("datadata_download",getInputData().getKeyValueMap().toString());
        if (urlString == null || fileName == null) {
            return Result.failure();
        }
        SSLUtils.trustAllCertificates();
        String requestHeaders = "";
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setHostnameVerifier((hostname, session) -> true);
            }
            String api_key=SharedPrefUtils.getApiKey();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("tenantId", tenantId);
            connection.setRequestProperty("Authorization", authorization);
            connection.setRequestProperty("apikey", api_key);
            connection.setRequestProperty("ecrCode", ecrCode);
            JSONObject headersJson = new JSONObject();
            headersJson.put("tenantId", tenantId);
            headersJson.put("Authorization", "......");
            headersJson.put("apikey",".....");
            headersJson.put("ecrCode", ecrCode);
            requestHeaders = headersJson.toString();
            Utils.addLog("datadata_download",tenantId);
            Utils.addLog("datadata_download",api_key);
            Utils.addLog("datadata_download",ecrCode);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                statusCode=connection.getResponseCode();
                File outputFile = new File(getApplicationContext().getCacheDir().getAbsolutePath(), fileName);
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                downloadFile(connection.getInputStream(), outputFile);
                Utils.addRequestTracking(urlString,"DownloadWorker",requestHeaders,"",statusCode+"");
                connection.disconnect();
                return Result.success();
            } else {
                statusCode=connection.getResponseCode();
                Utils.addLog("datadata_download", "disconnected " + connection.getResponseCode() + " " + connection.getResponseMessage());
                Utils.addRequestTracking(urlString,"DownloadWorker","",requestHeaders,statusCode+"\n"+connection.getResponseMessage());

                // Handle error response
                connection.disconnect();
                return Result.failure();
            }
        } catch (Exception e) {
            Utils.addRequestTracking(urlString,"DownloadWorker",requestHeaders,"",statusCode+"\n"+e.getMessage());
            addToDatabase(e,"downloadWorkerApi-cannot-call-request");
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
            addToDatabase(e,"downloadFile-downloadWorker");
            e.printStackTrace();
            return false;
        }
    }
}
