package com.app.smartpos.utils;

import com.app.smartpos.refund.Model.RefundModel;
import com.google.gson.Gson;

public class GsonUtils<T> {
    public GsonUtils() {
    }

    public String serializeToJson(T object) {
        Gson gson = new Gson();
        String j = gson.toJson(object);
        return j;
    }
    // Deserialize to single object.
    static public Object deserializeFromJson(String jsonString,Class objectClass) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, objectClass);
    }
}
