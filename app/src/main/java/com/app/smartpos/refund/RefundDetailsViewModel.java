package com.app.smartpos.refund;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.refund.Model.RefundModel;

import org.json.JSONException;
import org.json.JSONObject;

public class RefundDetailsViewModel extends ViewModel {

    MutableLiveData<RefundModel>liveData;

    public MutableLiveData<RefundModel> getLiveData(){
        if(liveData==null){
            liveData=new MutableLiveData<>();
        }
        return liveData;
    }

    public void start(String sequenceId, DatabaseAccess databaseAccess){
        AndroidNetworking.get("https://gateway-am-wso2-nonprod.apps.nt-non-ocp.neotek.sa/ecr/v1/invoice/refund/"+sequenceId)
                .addHeaders("apikey","eyJ4NXQiOiJPREUzWTJaaE1UQmpNRE00WlRCbU1qQXlZemxpWVRJMllqUmhZVFpsT0dJeVptVXhOV0UzWVE9PSIsImtpZCI6ImdhdGV3YXlfY2VydGlmaWNhdGVfYWxpYXMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZSI6bnVsbCwidGllciI6IlVubGltaXRlZCIsIm5hbWUiOiJFQ1JfQXBwbGljYXRpb24iLCJpZCI6MTE2LCJ1dWlkIjoiYTA2MGViNTgtN2Y5NC00YWRmLTk3YWMtZmMzZmRmOTUxNjIzIn0sImlzcyI6Imh0dHBzOlwvXC9hbS13c28yLW5vbnByb2QuYXBwcy5udC1ub24tb2NwLm5lb3Rlay5zYTo0NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6eyJVbmxpbWl0ZWQiOnsidGllclF1b3RhVHlwZSI6InJlcXVlc3RDb3VudCIsImdyYXBoUUxNYXhDb21wbGV4aXR5IjowLCJncmFwaFFMTWF4RGVwdGgiOjAsInN0b3BPblF1b3RhUmVhY2giOnRydWUsInNwaWtlQXJyZXN0TGltaXQiOjAsInNwaWtlQXJyZXN0VW5pdCI6bnVsbH19LCJrZXl0eXBlIjoiUFJPRFVDVElPTiIsInBlcm1pdHRlZFJlZmVyZXIiOiIiLCJzdWJzY3JpYmVkQVBJcyI6W3sic3Vic2NyaWJlclRlbmFudERvbWFpbiI6ImNhcmJvbi5zdXBlciIsIm5hbWUiOiJlY3IiLCJjb250ZXh0IjoiXC9lY3JcL3YxIiwicHVibGlzaGVyIjoiYWRtaW4iLCJ2ZXJzaW9uIjoidjEiLCJzdWJzY3JpcHRpb25UaWVyIjoiVW5saW1pdGVkIn1dLCJ0b2tlbl90eXBlIjoiYXBpS2V5IiwicGVybWl0dGVkSVAiOiIiLCJpYXQiOjE3MjUxOTY2NzgsImp0aSI6IjczZTFmOTkxLWMxZGUtNGUwMC1iNGY4LWM1ZmY2ZWZhYzZiOCJ9.EypVFJTQEzEZ8CF9Km4tq_yztgelhzrsij6_yErXr4IUSLFQAODImhkK4MC_yWkf9q6o72-lDLAXQVTjDdjgROrCG4es6T106IqIggQy1LPuRChiWxT0XhMZEQUk9fKrDuU3Gy3P-FEDkvsy6JAuhM3RO8SawLERyxSGvFexPKLPqZlBakDs91OWHPRsSsIKZ0zo4PZ7If2X4ZvJy5TUgxUjzt0GNDoG6OqzQr-0mRuiauvJtDgtoBXn2XzdugaBnj8Frjux6vjmSQmFWvVP4O4Hvv_vXdo8rZxNUVF5v86Huq7t0L3jJBKcH0DzL_EiF23XWPsCbxj-Z5HOKXwkPg==")
                .addHeaders("tenantId","cr1212121213")
                .addHeaders("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwMDAwMDAwLCJpYXQiOjE3MjU3MTQzNDF9.FnAhAvrOQ4ODkGVBpEaMK1VawZMItxKakDL5xcqfJ-I")
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("datadata",response.toString());
                        try {
                            liveData.postValue(new RefundModel(response.getJSONObject("data").getJSONArray("returnedObj").getJSONObject(0).toString(),databaseAccess));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i("datadata_error",anError.getMessage()+" ");
                    }
                });
    }
}
