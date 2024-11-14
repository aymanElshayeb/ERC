package com.app.smartpos.refund;

import static com.app.smartpos.Constant.REFUND_URL;
import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;
import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.refund.Model.RefundModel;
import com.app.smartpos.utils.GsonUtils;
import com.app.smartpos.utils.SharedPrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RefundWorker extends Worker {

    int code=-5;
    public RefundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public Result doWork() {
        OkHttpClient client = getUnsafeOkHttpClient();
        Headers headers = new Headers.Builder().
                add("tenantId", Objects.requireNonNull(getInputData().getString("tenantId"))).
                add("apikey", Objects.requireNonNull(getInputData().getString("apikey"))).
                add("Authorization", Objects.requireNonNull(getInputData().getString("Authorization"))).
                build();
        Request request = new Request.Builder()
                .url(REFUND_URL + getInputData().getString("sequenceId"))
                .get()
                .headers(headers)
                .build();
        try (Response response = client.newCall(request).execute()) {
            Utils.addLog("datadata", response.toString());
            code = response.code();
            if (response.isSuccessful()) {
                Data.Builder outputData = new Data.Builder();
                try {
                    assert response.body() != null;
                    JSONObject responseBody = new JSONObject(response.body().string());
                    int code = responseBody.getInt("code");
                    if (code == 200) {
                        Utils.addRequestTracking(REFUND_URL,"RefundWorker",headers.toString(),"",response.body().string());
                        GsonUtils gsonUtils = new GsonUtils();
                        outputData.put("refundModel", gsonUtils.serializeToJson(new RefundModel(responseBody.getJSONObject("data").getJSONArray("returnedObj").getJSONObject(0).toString())));
                    } else if (code == 404) {
                        Utils.addRequestTracking(REFUND_URL,"RefundWorker",headers.toString(),"",code+"");
                        outputData.put("refundModel",null);
                    }
                } catch (JSONException e) {
                    Utils.addRequestTracking(REFUND_URL,"RefundWorker",headers.toString(),"",code+" "+e.getMessage());
                    addToDatabase(e,"error-in-read-json-do-work-refundWorker");
                    e.printStackTrace();
                }
                return Result.success(outputData.build());
            }
        } catch (Exception e) {
            addToDatabase(e,"refundApi-cannot-call-request");
            e.printStackTrace();
        }
        return Result.failure();
    }
}
