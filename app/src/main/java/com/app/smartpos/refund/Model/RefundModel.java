package com.app.smartpos.refund.Model;

import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import com.app.smartpos.Constant;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.MultiLanguageApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class RefundModel implements Serializable {
    String order_id;
    String operation_sub_type;
    String order_payment_method;
    String operation_type;
    boolean printed;
    ArrayList<HashMap<String, String>> orderDetailsItems = new ArrayList<>();

    public RefundModel(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);

            order_id = json.getString("invoiceSeq");
            operation_type = json.getString("operationType");
            operation_sub_type = json.getString("operationSubType");
//            printed = json.getBoolean("printed");
            order_payment_method = json.getJSONObject("paymentMethod").getString("name");

            JSONArray invoiceLines = json.getJSONArray("invoiceLines");
            for (int i = 0; i < invoiceLines.length(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ex_tax_total", invoiceLines.getJSONObject(i).getString("exTaxTotal"));
                map.put("in_tax_total", invoiceLines.getJSONObject(i).getString("inTaxTotal"));
                map.put("invoice_id", order_id);
                map.put("order_status", Constant.COMPLETED);
                String uuid = invoiceLines.getJSONObject(i).getString("productUUID");
                map.put("product_uuid", uuid);
                String description = invoiceLines.getJSONObject(i).getString("description");
                map.put("product_description", description);
                if (uuid.equals("CUSTOM_ITEM")) {
                    map.put("product_image", "");
                    map.put("product_name_en", description);
                    map.put("product_name_ar", description);
                } else {
                    DatabaseAccess databaseAccess = DatabaseAccess.getInstance(MultiLanguageApp.getApp());
                    databaseAccess.open();
                    HashMap<String, String> map1 = databaseAccess.getProductsInfoFromUUID(uuid).get(0);
                    map.put("product_image", map1.get("product_image"));
                    map.put("product_name_en", map1.get("product_name_en"));
                    map.put("product_name_ar", map1.get("product_name_ar"));
                }

                int quantity = Integer.parseInt(invoiceLines.getJSONObject(i).getString("quantity"));
                map.put("product_order_date", json.getString("invoiceDate"));
                map.put("product_price", invoiceLines.getJSONObject(i).getString("unitPrice"));
                map.put("product_qty", String.valueOf(quantity));
                map.put("product_weight", "1");
                map.put("tax_amount", invoiceLines.getJSONObject(i).getString("taxAmount"));
                map.put("tax_percentage", invoiceLines.getJSONObject(i).getJSONObject("tax").getString("percentage"));
                map.put("item_checked", "0");
                map.put("refund_qty", "0");
                if (invoiceLines.getJSONObject(i).has("refundedQuantity")) {
                    map.put("product_original_refund_quantity", invoiceLines.getJSONObject(i).getString("refundedQuantity"));
                } else {
                    map.put("product_original_refund_quantity", "0");
                }
                Utils.addLog("datadata_map", map.toString());
                orderDetailsItems.add(map);
            }
        } catch (JSONException e) {
            addToDatabase(e,"error-in-read-json-refundModel");
            e.printStackTrace();
        }
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getOperation_sub_type() {
        return operation_sub_type;
    }

    public String getOrder_payment_method() {
        return order_payment_method;
    }

    public String getOperation_type() {
        return operation_type;
    }

    public boolean isPrinted() {
        return printed;
    }

    public ArrayList<HashMap<String, String>> getOrderDetailsItems() {
        return orderDetailsItems;
    }
}
