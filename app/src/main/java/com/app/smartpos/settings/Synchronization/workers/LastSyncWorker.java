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

import java.util.Collections;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LastSyncWorker extends Worker {
    int code=-5;
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
                add("apikey", SharedPrefUtils.getApiKey()).
                add("ecrCode", ecrCode).
                build();
        Request request = new Request.Builder()
                .url(urL) // Replace with your server's upload endpoint
                .get()
                .headers(headers)
                .build();
        JSONObject headersJson = new JSONObject();
        try {
            headersJson.put("Authorization", Collections.singletonList("......"));
            headersJson.put("apikey",Collections.singletonList("......"));
            headersJson.put("tenantId", tenantId);
            headersJson.put("ecrCode",ecrCode);
        } catch (JSONException e) {
            addToDatabase(e,"lastSync");
        }
        try (Response response = client.newCall(request).execute()) {
            code = response.code();
            JSONObject responseBody = new JSONObject();
            if (response.isSuccessful()) {
                assert response.body() != null;
                responseBody = new JSONObject(response.body().string());
                code = responseBody.getInt("code");
                JSONObject returnedObj = responseBody.getJSONObject("data").getJSONArray("returnedObj").getJSONObject(0);
                String invoiceBusinessId = returnedObj.getString("invoiceBusinessId");
                String shiftBusinessId = returnedObj.getString("shiftBusinessId");
                Data data = new Data.Builder().
                        putString("invoiceBusinessId", invoiceBusinessId).
                        putString("shiftBusinessId", shiftBusinessId).
                        putString("Authorization", authorization).
                        build();
                Utils.addRequestTracking(urL,"LastSyncWorker",headersJson.toString(),"",code + "\n" + responseBody);
                return Result.success(data); // Return success if the upload is successful
            } else {
                Utils.addRequestTracking(urL,"LastSyncWorker",headersJson.toString(),"",code+"\n"+responseBody);
                return Result.failure(); // Retry the work if the server returns an error
            }
        } catch (Exception e) {
            Utils.addRequestTracking(urL,"LastSyncWorker",headersJson.toString(),"",code+"\n"+e.getMessage());
            addToDatabase(e,"lastSyncApi-cannot-call-request");
            e.printStackTrace();
            return Result.failure(); // Return failure if there is an exception
        }
    }
}
