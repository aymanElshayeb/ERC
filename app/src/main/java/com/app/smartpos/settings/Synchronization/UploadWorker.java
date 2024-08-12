package com.app.smartpos.settings.Synchronization;

import static com.app.smartpos.Constant.API_KEY;
import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadWorker extends Worker {

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get the file path from input data
        String filePath = getInputData().getString("fileNameGzip");
        String uri = getInputData().getString("url");
        String tenantId= getInputData().getString("tenantId");
        String authorization= getInputData().getString("Authorization");
        String ecrCode= getInputData().getString("ecrCode");
        if (filePath == null || uri==null || tenantId==null || authorization==null || ecrCode==null) {
            return Result.failure();
        }

        // Step 1: Get the file from the download directory
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File fileToUpload = new File(downloadDir,filePath ); // Replace with your actual file name

        if (!fileToUpload.exists()) {
            return Result.failure(); // Return failure if the file does not exist
        }

        // Step 2: Build the request to send the file
        OkHttpClient client = getUnsafeOkHttpClient();


        RequestBody fileBody = RequestBody.create(fileToUpload,MediaType.parse("application/octet-stream"));

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileToUpload.getName(), fileBody)
                .build();
        Headers headers=new Headers.Builder().
                add("tenantId", tenantId).
                add("Authorization",authorization).
                add("apikey",API_KEY).
                add("ecrCode",ecrCode).
                build();
        Request request = new Request.Builder()
                .url(uri) // Replace with your server's upload endpoint
                .post(requestBody)
                .headers(headers)
                .build();

        // Step 3: Execute the request and handle the response
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return Result.success(); // Return success if the upload is successful
            } else {
                return Result.failure(); // Retry the work if the server returns an error
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure(); // Return failure if there is an exception
        }
    }
}
