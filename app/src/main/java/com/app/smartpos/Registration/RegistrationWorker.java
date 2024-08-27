package com.app.smartpos.Registration;

import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.customers.AddCustomersActivity;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.baseDto.ServiceRequest;
import com.app.smartpos.utils.baseDto.ServiceResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistrationWorker extends Worker {
    public RegistrationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String username = getInputData().getString("username");
        String password = getInputData().getString("password");
        String tenantId= getInputData().getString("tenantId");
        String deviceId= getInputData().getString("deviceId");
        String urlString=getInputData().getString("url");
        if (username == null || password == null || tenantId==null || deviceId==null) {
            return Result.failure();
        }
        // Perform registration logic here
        OkHttpClient client = getUnsafeOkHttpClient();
        Headers headers=new Headers.Builder().
                add("tenantId", tenantId).
                add("apikey", "eyJ4NXQiOiJPREUzWTJaaE1UQmpNRE00WlRCbU1qQXlZemxpWVRJMllqUmhZVFpsT0dJeVptVXhOV0UzWVE9PSIsImtpZCI6ImdhdGV3YXlfY2VydGlmaWNhdGVfYWxpYXMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZSI6bnVsbCwidGllciI6IlVubGltaXRlZCIsIm5hbWUiOiJFQ1JfQXBwbGljYXRpb24iLCJpZCI6MTE2LCJ1dWlkIjoiYTA2MGViNTgtN2Y5NC00YWRmLTk3YWMtZmMzZmRmOTUxNjIzIn0sImlzcyI6Imh0dHBzOlwvXC9hbS13c28yLW5vbnByb2QuYXBwcy5udC1ub24tb2NwLm5lb3Rlay5zYTo0NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6eyJVbmxpbWl0ZWQiOnsidGllclF1b3RhVHlwZSI6InJlcXVlc3RDb3VudCIsImdyYXBoUUxNYXhDb21wbGV4aXR5IjowLCJncmFwaFFMTWF4RGVwdGgiOjAsInN0b3BPblF1b3RhUmVhY2giOnRydWUsInNwaWtlQXJyZXN0TGltaXQiOjAsInNwaWtlQXJyZXN0VW5pdCI6bnVsbH19LCJrZXl0eXBlIjoiUFJPRFVDVElPTiIsInBlcm1pdHRlZFJlZmVyZXIiOiIiLCJzdWJzY3JpYmVkQVBJcyI6W3sic3Vic2NyaWJlclRlbmFudERvbWFpbiI6ImNhcmJvbi5zdXBlciIsIm5hbWUiOiJlY3IiLCJjb250ZXh0IjoiXC9lY3JcL3YxIiwicHVibGlzaGVyIjoiYWRtaW4iLCJ2ZXJzaW9uIjoidjEiLCJzdWJzY3JpcHRpb25UaWVyIjoiVW5saW1pdGVkIn1dLCJ0b2tlbl90eXBlIjoiYXBpS2V5IiwicGVybWl0dGVkSVAiOiIiLCJpYXQiOjE3MjQ2Njc0NzUsImp0aSI6IjFkOWE2YTYyLTAyN2QtNGQwYi1hOGVmLWRmZWI4YTgyNTA2NCJ9.OxuvQ32HnBlo4QWe0Y7ealgR6eag_Uw5_WPKGzR4ZHswj6asZTZmrPQFCuAK4TtCGf5TmRvxNbX7naMB9cBwIIUqBmGjaEmYdeYyc_MQ6Cu6n9uHT7TnGVCzJZldaPdtVhDRhYKVTydekPDD5GYWxduI14huMJcUo6FQp9P1D1W0s6nsTtyTyWxxCQsp0aCQhB6Twr8KhX4qCJ_5l15sZPdyVh4LPihkk_thYbvNCVSuBQ4PbHYEjqio08L5LtSHu-GpuID6C7h93ED5ORFC9cojubMOxj8sS1OD-LnJPXnodE9-sz7k5xA_gGz6jMHbBz8DegvQoGS8q-73f79xEw==").
                build();
        //prepare the dto class
        RegistrationRequestDto registrationRequestDto =new RegistrationRequestDto();
        registrationRequestDto.setUsername(username);
        registrationRequestDto.setPassword(password);
        registrationRequestDto.setDeviceId(deviceId);
        ServiceRequest<RegistrationRequestDto> serviceRequest=ServiceRequest.constructServiceRequest(registrationRequestDto);
        //prepare the request
        Gson gson = new Gson();
        String jsonBody = gson.toJson(serviceRequest);
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(urlString)
                .post(body)
                .headers(headers)
                .build();
        try(Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                ServiceResult<RegistrationResponseDto> result=gson.fromJson(responseBody, new TypeToken<ServiceResult<RegistrationResponseDto>>(){}.getType());
                if(result.getCode()!=200){
                    Data outputData = new Data.Builder().putString("errorMessage", "FAILED TO REGISTER").build();
                    return Result.failure(outputData);
                }
                RegistrationResponseDto registrationResponseDto=result.getData().getReturnedObj().get(0);
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
                databaseAccess.open();
                databaseAccess.addConfiguration(registrationResponseDto);
                String authorization=registrationResponseDto.getToken();
                Data outputData = new Data.Builder().
                        putString("Authorization", authorization).
                        putString("ecrCode",registrationResponseDto.getEcrCode()).
                        build();
                return Result.success(outputData);
            } else {
                Data outputData = new Data.Builder().putString("errorMessage", "FAILED TO REGISTER").build();
                return Result.failure(outputData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
