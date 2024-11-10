package com.app.smartpos.utils;

import com.app.smartpos.refund.Model.RefundModel;
import com.google.gson.Gson;

public class GsonUtils {

    static public String serializeToJson(RefundModel refundModel) {
        Gson gson = new Gson();
        String j = gson.toJson(refundModel);
        return j;
    }
    // Deserialize to single object.
    static public RefundModel deserializeFromJson(String jsonString) {
        Gson gson = new Gson();
        RefundModel refundModel = gson.fromJson(jsonString, RefundModel.class);
        return refundModel;
    }
}
