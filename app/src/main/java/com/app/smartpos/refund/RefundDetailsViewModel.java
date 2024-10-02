package com.app.smartpos.refund;

import static com.app.smartpos.Constant.API_KEY;
import static com.app.smartpos.Constant.BASE_URL;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.refund.Model.RefundModel;
import com.app.smartpos.utils.SharedPrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class RefundDetailsViewModel extends ViewModel {

    MutableLiveData<RefundModel> liveData;

    public MutableLiveData<RefundModel> getLiveData() {
        if (liveData == null) {
            liveData = new MutableLiveData<>();
        }
        return liveData;
    }

    public void start(String sequenceId, DatabaseAccess databaseAccess) {
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();
        AndroidNetworking.get(BASE_URL + "/invoice/refund/" + sequenceId)
                .addHeaders("apikey", API_KEY)
                .addHeaders("tenantId", conf.get("merchant_id"))
                .addHeaders("Authorization", SharedPrefUtils.getAuthorization())
                .setTag("GET INVOICE DETAILS")
                .setPriority(Priority.HIGH)
                .build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("datadata", response.toString());
                        try {
                            if (response.getInt("code") == 200) {
                                liveData.postValue(new RefundModel(response.getJSONObject("data").getJSONArray("returnedObj").getJSONObject(0).toString(), databaseAccess));
                            } else if (response.getInt("code") == 404) {
                                liveData.postValue(null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i("datadata_error", anError.getMessage() + " "+anError.getErrorDetail()+" "+anError.getErrorCode());
                    }
                });

    }
}
