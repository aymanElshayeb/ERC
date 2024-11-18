package com.app.smartpos.settings.Synchronization.workers;

import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;
import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.common.Utils;
import com.app.smartpos.utils.SharedPrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadWorker extends Worker {

    int code=-5;
    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get the file path from input data
        String filePath = getInputData().getString("fileNameGzip");

        String uri = getInputData().getString("url");
        String tenantId = getInputData().getString("tenantId");
        String authorization = getInputData().getString("Authorization");
        String ecrCode = getInputData().getString("ecrCode");
        if (filePath == null || uri == null || tenantId == null || authorization == null || ecrCode == null) {
            return Result.failure();
        }

        // Step 1: Get the file from the download directory
        File fileToUpload = new File(getApplicationContext().getCacheDir().getAbsolutePath(), filePath); // Replace with your actual file name

        if (!fileToUpload.exists()) {
            return Result.failure(); // Return failure if the file does not exist
        }

        // Step 2: Build the request to send the file
        OkHttpClient client = getUnsafeOkHttpClient();


        RequestBody fileBody = RequestBody.create(fileToUpload, MediaType.parse("application/octet-stream"));

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileToUpload.getName(), fileBody)
                .build();
        Headers headers = new Headers.Builder().
                add("tenantId", tenantId).
                add("Authorization", authorization).
                add("apikey", SharedPrefUtils.getApiKey()).
                add("ecrCode", ecrCode).
                build();
        Request request = new Request.Builder()
                .url(uri) // Replace with your server's upload endpoint
                .post(requestBody)
                .headers(headers)
                .build();
        JSONObject headersJson = new JSONObject();
        try {
            headersJson.put("Authorization", Collections.singletonList("......"));
            headersJson.put("apikey",Collections.singletonList("......"));
            headersJson.put("tenantId", tenantId);
            headersJson.put("ecrCode", ecrCode);
        } catch (JSONException e) {
            addToDatabase(e,"productImageSize");
        }

        Data outputData = new Data.Builder().putString("Authorization", authorization).build();
        // Step 3: Execute the request and handle the response
        try (Response response = client.newCall(request).execute()) {
            code=response.code();
            if (response.isSuccessful()) {
                assert response.body() != null;
                Utils.addRequestTracking(uri,"UploadWorker",headersJson.toString(),requestBody.toString(),code + "\n" + response.body().string());
                return Result.success(outputData);
            } else {
                Utils.addRequestTracking(uri,"UploadWorker",headersJson.toString(),requestBody.toString(),code+ "\n" + response.body().string());

                return Result.failure(); // Retry the work if the server returns an error
            }
        } catch (IOException e) {
            Utils.addRequestTracking(uri,"UploadWorker",headersJson.toString(),requestBody.toString(), code+ "\n" +e.getMessage());

            addToDatabase(e,"uploadWorkerApi-cannot-call-request");
            e.printStackTrace();
            return Result.failure(); // Return failure if there is an exception
        }
    }
}
