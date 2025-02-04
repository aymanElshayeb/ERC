package com.app.smartpos.refund;

import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;

import com.app.smartpos.Constant;
import com.app.smartpos.R;
import com.app.smartpos.adapter.RefundOrOrderDetailsAdapter;
import com.app.smartpos.checkout.CheckoutOrderDetails;
import com.app.smartpos.checkout.SuccessfulPayment;
import com.app.smartpos.common.Utils;
import com.app.smartpos.common.WorkerActivity;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.refund.Model.RefundModel;
import com.app.smartpos.utils.SharedPrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class RefundOrOrderDetails extends WorkerActivity {

    DatabaseAccess databaseAccess;
    RefundOrOrderDetailsAdapter refundDetailsAdapter;
    String currency;
    TextView receipt_number_tv;
    TextView card_tv, cash_tv, refunded_tv, total_amount_tv;

    List<HashMap<String, String>> orderDetailsList;
    String orderId;
    String operation_sub_type;
    TextView refund_tv;
    LinearLayout loadingLl;
    boolean isRefund;
    private String order_payment_method;
    private String operation_type;
    private String refundSequence;
    boolean checkConnectionOnce = true;
    private RecyclerView recycler;
    boolean printedBefore;
    String orderPreviousState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_refund_details);

        isRefund = getIntent().getBooleanExtra("isRefund", false);

        TextView title_tv = findViewById(R.id.title_tv);
        TextView question_tv = findViewById(R.id.question_tv);
        TextView amount_tv = findViewById(R.id.amount_tv);
        LinearLayout btn_ll = findViewById(R.id.btn_ll);
        receipt_number_tv = findViewById(R.id.receipt_number_tv);
        recycler = findViewById(R.id.recycler);
        card_tv = findViewById(R.id.card_tv);
        cash_tv = findViewById(R.id.cash_tv);
        refunded_tv = findViewById(R.id.refunded_tv);
        total_amount_tv = findViewById(R.id.total_amount_tv);
        loadingLl = findViewById(R.id.loading_ll);

        refund_tv = findViewById(R.id.refund_tv);


        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        currency = databaseAccess.getCurrency();
        databaseAccess.open();
        //get data from local database
        if (!isRefund) {
            orderId = getIntent().getStringExtra("order_id");
            operation_sub_type = getIntent().getStringExtra("operation_sub_type");
            order_payment_method = getIntent().getStringExtra("order_payment_method");
            operation_type = getIntent().getStringExtra("operation_type");
            printedBefore = getIntent().getBooleanExtra("printed",false);
            orderDetailsList = databaseAccess.getOrderDetailsList(orderId);
            Utils.addLog("datadata_test", operation_sub_type + " " + operation_type);
            for (int i = 0; i < orderDetailsList.size(); i++) {
                orderDetailsList.get(i).put("refund_qty", "0");
                orderDetailsList.get(i).put("item_checked", "0");
                Utils.addLog("datadata", orderDetailsList.get(i).toString());
            }
        } else {
            RefundModel refundModel = (RefundModel) getIntent().getSerializableExtra("refundModel");
            orderId = refundModel.getOrder_id();
            operation_sub_type = refundModel.getOperation_sub_type();
            order_payment_method = refundModel.getOrder_payment_method();
            operation_type = refundModel.getOperation_type();
            orderDetailsList = refundModel.getOrderDetailsItems();
            printedBefore = getIntent().getBooleanExtra("printed",false);
        }


        receipt_number_tv.setText(orderId);

        card_tv.setVisibility(order_payment_method.equals("CARD") ? View.VISIBLE : View.GONE);
        cash_tv.setVisibility(order_payment_method.equals("CASH") ? View.VISIBLE : View.GONE);
        refunded_tv.setVisibility(operation_type.equals("refund") ? View.VISIBLE : View.GONE);


        refundDetailsAdapter = new RefundOrOrderDetailsAdapter(this, orderDetailsList);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        updateTotalAmount();
        if (!isRefund) {
            title_tv.setText(getString(R.string.order_details));
            question_tv.setVisibility(View.GONE);
            amount_tv.setText(getString(R.string.total_amount));
            refund_tv.setText(getString(R.string.invoice));
            refund_tv.setAlpha(1.0f);
            refund_tv.setEnabled(true);

        }

        refund_tv.setOnClickListener(view -> {
            if (!isRefund) {
                String type="";
                if(operation_type.equals("refund")){
                    if(printedBefore){
                       type = getString(R.string.receipt_refund_copy);
                    }else{
                        type= getString(R.string.refund);
                    }
                }else{
                    if(printedBefore){
                        type = getString(R.string.receipt_copy);
                    }else{
                        type= getString(R.string.simplified_tax_invoice);
                    }
                }
                startActivity(new Intent(this, CheckoutOrderDetails.class).putExtra("id", orderId).putExtra("printType",
                        type))
                ;
            } else {
                refundPressed();
            }
        });
        findViewById(R.id.back_im).setOnClickListener(view -> finish());
    }

    @Override
    public void connectionChanged(boolean state) {
        super.connectionChanged(state);
        Utils.addLog("datadata_connection", String.valueOf(state));
        setAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isConnected()) {
            setAdapter();
        }
    }

    private void setAdapter() {
        if (checkConnectionOnce) {
            checkConnectionOnce = false;
            runOnUiThread(() -> {
                recycler.setAdapter(refundDetailsAdapter);
            });
        }
    }

    public boolean isRefund() {
        return isRefund;
    }

    public String getCurrency() {
        return currency;
    }

    public void updateTotalAmount() {
        boolean canRefund = false;
        double total = 0;
        for (int i = 0; i < orderDetailsList.size(); i++) {
            double product_price = Double.parseDouble(orderDetailsList.get(i).get("product_price"));
            int refund_qty = Integer.parseInt(orderDetailsList.get(i).get("refund_qty"));
            double product_qty = Integer.parseInt(orderDetailsList.get(i).get("product_qty"));
            String item_checked = orderDetailsList.get(i).get("item_checked");

            if (!isRefund) {
                total += product_qty * product_price;
                total = Double.parseDouble(Utils.trimLongDouble(total));
                Utils.addLog("datadata_total", total + " " + (product_qty * product_price));
            } else {
                if (item_checked.equals("1") && refund_qty > 0) {
                    total += refund_qty * product_price;
                    canRefund = true;
                }
            }
        }
        refund_tv.setEnabled(canRefund);
        refund_tv.setAlpha(canRefund ? 1.0f : 0.5f);
        total_amount_tv.setText(Utils.trimLongDouble(total) + " " + currency);
    }

    private void refundPressed() {
        RefundConfirmationDialog dialog = new RefundConfirmationDialog();
        dialog.setData(this, total_amount_tv.getText().toString());
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    public void refundConfirmation() {
        boolean canRefund = false;
        double total_amount = 0;
        double total_tax = 0;
        for (int i = 0; i < orderDetailsList.size(); i++) {
            int refund_qty = Integer.parseInt(orderDetailsList.get(i).get("refund_qty"));
            String item_checked = orderDetailsList.get(i).get("item_checked");

            if (item_checked.equals("1") && refund_qty > 0) {
                canRefund = true;
                total_amount += Double.parseDouble(orderDetailsList.get(i).get("in_tax_total"));
                total_tax += Double.parseDouble(orderDetailsList.get(i).get("tax_amount"));

//                databaseAccess.open();
//                databaseAccess.updateOrderDetailsItem("product_qty", "" + (int) (product_qty - refund_qty), order_details_id);
            }
        }

        if (canRefund) {
            databaseAccess.open();
            HashMap<String,String>map=databaseAccess.getOrderListByOrderId(orderId);
            if (!map.isEmpty()) {
                if(orderPreviousState==null){
                    orderPreviousState = map.get("order_status");
                }
                databaseAccess.updateOrderListItem("order_status", Constant.REFUNDED, orderId);
            }
            try {
                if(isConnected()) {
                    refundSequence = proceedOrder("", "CASH", "", total_tax, "0", "", "", total_amount, 0);
//                DownloadDataDialog dialog = DownloadDataDialog.newInstance(DownloadDataDialog.OPERATION_UPLOAD);
//                dialog.show(getSupportFragmentManager(), "dialog");
                loadingLl.setVisibility(View.VISIBLE);
                enqueueCreateAndUploadWorkers();
//                redirectToSuccess();
//                SharedPrefUtils.resetAuthorization();
                }else{
                    Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                addToDatabase(e,"error-in-proceedOrder-refundOrOrderDetailsScreen");
                e.printStackTrace();
            }

        }
    }

    @Override
    public void handleWorkCompletion(WorkInfo workInfo) {
        super.handleWorkCompletion(workInfo);
        loadingLl.setVisibility(View.GONE);
        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
            // Work succeeded, handle success
            showMessage(getString(R.string.data_synced_successfully));
            redirectToSuccess();
            SharedPrefUtils.resetAuthorization();
        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
            // Work failed, handle failure
            showMessage(getString(R.string.error_in_syncing_refund));
            databaseAccess.deleteOrderListItemAndDetails(orderPreviousState,refundSequence);
        }

    }

    public void redirectToSuccess() {
        Intent intent = new Intent(this, SuccessfulPayment.class).putExtra("amount", total_amount_tv.getText().toString()).putExtra("order_id", refundSequence).putExtra("printType", getString(R.string.receipt_refunded));
        startActivity(intent);
        finish();
    }

    public String proceedOrder(String type, String payment_method, String customer_name, double calculated_tax, String discount, String card_type_code, String approval_code, double total, double change) throws JSONException {

        //boolean success=true;
        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        //get data from local database
        String sequence = null;
        if (orderDetailsList.isEmpty()) {
            Toasty.error(this, R.string.no_product_found, Toast.LENGTH_SHORT).show();
        } else {


            //get current timestamp

            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
            //H denote 24 hours and h denote 12 hour hour format
            String currentTime = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date()); //HH:mm:ss a

            //timestamp use for invoice id for unique
            Long tsLong = System.currentTimeMillis() / 1000;
            String timeStamp = tsLong.toString();


            final JSONObject obj = new JSONObject();
            try {


                obj.put("order_date", currentDate);
                obj.put("order_time", currentTime);
                obj.put("order_timestamp", new Timestamp(System.currentTimeMillis()).getTime());
                obj.put("order_type", type);
                obj.put("order_payment_method", payment_method);
                obj.put("customer_name", customer_name);
                obj.put("order_status", Constant.COMPLETED);
                obj.put("operation_type", "refund");
                obj.put("original_order_id", orderId);
                obj.put("card_type_code", card_type_code);
                obj.put("approval_code", approval_code);
                obj.put("operation_sub_type", operation_sub_type);
                obj.put("printed", false);
                databaseAccess.open();
                HashMap<String, String> configuration = databaseAccess.getConfiguration();
                String ecr_code = configuration.isEmpty() ? "" : configuration.get("ecr_code");
                obj.put("ecr_code", ecr_code);


                double totalPriceWithTax = 0;
                double total_tax = 0;
                for (int i = 0; i < orderDetailsList.size(); i++) {
                    int refund_qty = Integer.parseInt(orderDetailsList.get(i).get("refund_qty"));
                    String item_checked = orderDetailsList.get(i).get("item_checked");

                    if (item_checked.equals("1") && refund_qty > 0) {
                        totalPriceWithTax += Double.parseDouble(orderDetailsList.get(i).get("product_price")) * refund_qty;
                        //ToDo tax amount should be divided by the total qantity of the order line before multiplying
                        int quantity = Integer.parseInt(orderDetailsList.get(i).get("product_qty"));
                        Utils.addLog("datadata_qty", quantity + " " + orderDetailsList.get(i).get("tax_amount"));
                        total_tax += (Double.parseDouble(orderDetailsList.get(i).get("tax_amount")) / quantity) * refund_qty;
                        Utils.addLog("datadata_qty", quantity + " " + total_tax);
                    }
                }
                obj.put("in_tax_total", -totalPriceWithTax);
                obj.put("ex_tax_total", -(totalPriceWithTax - total_tax));

                obj.put("paid_amount", -totalPriceWithTax);
                obj.put("change_amount", change);

                String tax_number = configuration.get("merchant_tax_number");
                obj.put("tax_number", tax_number);

                databaseAccess.open();
                HashMap<String, String> sequenceMap = databaseAccess.getSequence(1, ecr_code);
                obj.put("sequence_text", sequenceMap.get("sequence_id"));

                obj.put("tax", -total_tax);
                obj.put("discount", discount);


                JSONArray array = new JSONArray();


                for (int i = 0; i < orderDetailsList.size(); i++) {
                    int refund_qty = Integer.parseInt(orderDetailsList.get(i).get("refund_qty"));
                    String item_checked = orderDetailsList.get(i).get("item_checked");

                    if (item_checked.equals("1") && refund_qty > 0) {
                        databaseAccess.open();
                        String product_uuid = orderDetailsList.get(i).get("product_uuid");

                        ArrayList<HashMap<String, String>> product = databaseAccess.getProductsInfoFromUUID(product_uuid);

                        databaseAccess.open();
                        String weight_unit = databaseAccess.getWeightUnitName(product.get(0).get("product_weight_unit_id"));


                        JSONObject objp = new JSONObject();
                        objp.put("product_uuid", product.get(0).get("product_uuid"));
                        String englishName = product.get(0).get("product_name_en");
                        String arabicName = product.get(0).get("product_name_ar");
                        if (product.get(0).get("product_uuid").equals("CUSTOM_ITEM") && !orderDetailsList.get(i).get("product_description").isEmpty()) {
                            englishName = orderDetailsList.get(i).get("product_description");
                            arabicName = orderDetailsList.get(i).get("product_description");
                        }
                        objp.put("product_name_en", englishName);
                        objp.put("product_name_ar", arabicName);
                        objp.put("product_uuid", product.get(0).get("product_uuid"));
                        objp.put("product_weight", orderDetailsList.get(i).get("product_weight") + " " + weight_unit);
                        //objp.put("product_qty", orderDetailsList.get(i).get("product_qty") + "");
                        objp.put("product_qty", String.valueOf(-refund_qty));
                        objp.put("stock", orderDetailsList.get(i).get("stock") == null ? Integer.MAX_VALUE : orderDetailsList.get(i).get("stock"));
                        objp.put("product_price", orderDetailsList.get(i).get("product_price"));
                        objp.put("product_description", orderDetailsList.get(i).get("product_description"));
                        objp.put("product_image", product.get(0).get("product_image") == null ? "" : product.get(0).get("product_image"));
                        objp.put("product_order_date", currentDate);

                        array.put(objp);
                    }

                }
                obj.put("lines", array);


            } catch (JSONException e) {
                addToDatabase(e,"error-in-proceedOrder-lines-refundOrderDetailsScreen");
                e.printStackTrace();
            }

            Utils.addLog("datadata", obj.toString());
            sequence = saveOrderInOfflineDb(obj);

        }
        return sequence;
    }

    //for save data in offline
    private String saveOrderInOfflineDb(final JSONObject obj) throws JSONException {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);

        databaseAccess.open();
        HashMap<String, String> sequenceMap = databaseAccess.getSequence(Constant.INVOICE_SEQ_ID, obj.getString("ecr_code"));

        databaseAccess.open();
        databaseAccess.updateSequence(Integer.parseInt(sequenceMap.get("next_value")), Integer.parseInt(sequenceMap.get("sequence_id")));

        String orderId = sequenceMap.get("sequence");
        databaseAccess.open();
        databaseAccess.insertOrder(orderId, obj, this, true, databaseAccess);


//        Toasty.success(this, R.string.order_done_successful, Toast.LENGTH_SHORT).show();
//
//        Intent intent = new Intent(this, SuccessfulPayment.class).putExtra("amount", totalAmount + " " + currency).putExtra("id", orderId);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();

        return orderId;
    }
}