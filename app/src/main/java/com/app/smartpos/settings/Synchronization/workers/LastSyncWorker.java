package com.app.smartpos.settings.Synchronization.workers;

import static com.app.smartpos.Constant.API_KEY;
import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LastSyncWorker extends Worker {
    public LastSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String urL = getInputData().getString("url");
        String tenantId = getInputData().getString("tenantId");
        String authorization = getInputData().getString("Authorization");

        String ecrCode = getInputData().getString("ecrCode");
        if (urL == null || tenantId == null || authorization == null || ecrCode == null) {
            return Result.failure();
        }
        OkHttpClient client = getUnsafeOkHttpClient();
        Headers headers = new Headers.Builder().
                add("tenantId", tenantId).
                add("Authorization", authorization).
                add("apikey", API_KEY).
                add("ecrCode", ecrCode).
                build();
        Request request = new Request.Builder()
                .url(urL) // Replace with your server's upload endpoint
                .get()
                .headers(headers)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {

                //Gson gson=new Gson();
                //ServiceResult<LastSyncResponseDto> result=gson.fromJson(responseBody, new TypeToken<ServiceResult<LastSyncResponseDto>>(){}.getType());
                JSONObject responseBody = new JSONObject(response.body().string());
                JSONObject returnedObj = responseBody.getJSONObject("data").getJSONArray("returnedObj").getJSONObject(0);
                String invoiceBusinessId = returnedObj.getString("invoiceBusinessId");
                String shiftBusinessId = returnedObj.getString("shiftBusinessId");
                Data data = new Data.Builder().
                        putString("invoiceBusinessId", invoiceBusinessId).
                        putString("shiftBusinessId", shiftBusinessId).
                        putString("Authorization", authorization).
                        build();
                return Result.success(data); // Return success if the upload is successful
            } else {
                return Result.failure(); // Retry the work if the server returns an error
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(); // Return failure if there is an exception
        }
    }
}
