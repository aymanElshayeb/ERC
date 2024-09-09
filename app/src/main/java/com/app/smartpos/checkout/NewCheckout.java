package com.app.smartpos.checkout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.Constant;
import com.app.smartpos.R;
import com.app.smartpos.adapter.CartPaymentMethodAdapter;
import com.app.smartpos.common.DeviceFactory.Device;
import com.app.smartpos.common.DeviceFactory.DeviceFactory;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;

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

public class NewCheckout extends AppCompatActivity {

    TextView totalAmountWithoutVatTv;
    TextView totalAmountTv;
    TextView totalVatTv;
    List<HashMap<String, String>> cartProductList;
    List<HashMap<String, String>> paymentMethodData;

    double totalAmount = 0;
    String currency;
    DatabaseAccess databaseAccess;

    CartPaymentMethodAdapter adapter;
    String paymentType = "";
    Device device;
    boolean fromQuickBill;
    private double totalTax;
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), launcherResult -> {
        if (launcherResult.getResultCode() == Activity.RESULT_OK) {
            if (launcherResult.getData() != null) {
                try {
                    String jsonActivityResult = device.jsonActivityResult();
                    String amountString = device.amountString();

                    JSONObject response = new JSONObject(launcherResult.getData().getStringExtra(jsonActivityResult));
                    String statusCode = "";
                    try {
                        JSONObject result = response.getJSONObject(device.resultHeader());
                        statusCode = result.getString("StatusCode");
                        String resultStatus = result.getJSONObject("Result").getString("English");
                        if (resultStatus.equals("APPROVED")) {
                            String code = result.getJSONObject("CardScheme").getString("ID");
                            String name = result.getJSONObject("CardScheme").getString("English");

                            String PurchaseAmount = result.getJSONObject(amountString).getString("PurchaseAmount");
                            String ApprovalCode = result.getString("ApprovalCode");
                            Log.i("datadata", name + " " + code);
//                            databaseAccess.open();
                            //long id=databaseAccess.insertCardDetails(name,code);
                            //Log.i("datadata",id+"");
                            proceedOrder("", "CARD", "", totalTax, "0", code, ApprovalCode, Double.parseDouble(PurchaseAmount), 0);

                        } else if (resultStatus.equals("Declined")) {
                            Toast.makeText(this, "Transaction Declined", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        if (statusCode.equals(Constant.REJECTED_STATUS_CODE))
                            Toast.makeText(this, response.getString("ErrorMsg"), Toast.LENGTH_LONG).show();
                        else
                            e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_new_checkout);

        device = DeviceFactory.getDevice();

        RecyclerView recyclerView = findViewById(R.id.recycler);
        ImageView backIm = findViewById(R.id.back_im);
        totalAmountWithoutVatTv = findViewById(R.id.total_amount_without_vat_tv);
        totalAmountTv = findViewById(R.id.total_amount_tv);
        totalVatTv = findViewById(R.id.total_vat_tv);
        TextView addItemsTv = findViewById(R.id.add_items_tv);
        TextView checkoutTv = findViewById(R.id.checkout_tv);

        GridLayoutManager manager = new GridLayoutManager(getApplicationContext(), 2, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(manager); // set LayoutManager to RecyclerView

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        currency = databaseAccess.getCurrency();

        databaseAccess.open();
        //get data from local database
        fromQuickBill = getIntent().getBooleanExtra("fromQuickBill", false);
        if (!fromQuickBill) {
            cartProductList = databaseAccess.getCartProduct();
        } else {
            cartProductList = new ArrayList<>();
            cartProductList.add(addCustomItem(getIntent().getStringExtra("amount"),getIntent().getStringExtra("description")));
        }
        List<HashMap<String, String>> paymentMethodData;
        databaseAccess.open();
        paymentMethodData = databaseAccess.getPaymentMethod(true);


        adapter = new CartPaymentMethodAdapter(this, paymentMethodData);
        recyclerView.setAdapter(adapter);

        updateTotalPrice(cartProductList);

        addItemsTv.setOnClickListener(view -> finish());
        backIm.setOnClickListener(view -> finish());
        checkoutTv.setOnClickListener(view -> {
            if (paymentType.equals("CASH")) {
                startActivityForResult(new Intent(this, CashPricing.class).putExtra("total_amount", totalAmount), 12);
            } else if (paymentType.equals("CARD")) {
                Intent intent = device.pay((long) totalAmount);
                launcher.launch(intent);
            } else {
                Toasty.info(this, "Please select Payment Method",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public void updateTotalPrice(List<HashMap<String, String>> list) {
        Log.i("datadata_size", list.size() + "");
        double totalWithoutTax = 0;

        for (int i = 0; i < list.size(); i++) {
            String productId = list.get(i).get("product_id");
            double productPrice = Double.parseDouble(list.get(i).get("product_price"));
            double productCount = Double.parseDouble(list.get(i).get("product_qty"));
            databaseAccess.open();
            double productTax = 1 + databaseAccess.getProductTax(productId) / 100;
            totalWithoutTax += (productPrice / productTax) * productCount;
            totalAmount += productPrice * productCount;
        }
        totalTax = totalAmount - totalWithoutTax;
        totalAmountWithoutVatTv.setText(Utils.trimLongDouble(totalWithoutTax) + " " + currency);
        totalVatTv.setText(Utils.trimLongDouble(totalTax) + " " + currency);
        totalAmountTv.setText(Utils.trimLongDouble(totalAmount) + " " + currency);
    }

    public void proceedOrder(String type, String payment_method, String customer_name, double calculated_tax, String discount, String card_type_code, String approval_code, double total, double change) throws JSONException {

        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);

//        if (fromQuickBill) {
//            databaseAccess.open();
//            databaseAccess.addToCart(cartProductList.get(0).get("product_id"), "1", cartProductList.get(0).get("weight_unit_id"), cartProductList.get(0).get("product_price"), 1, cartProductList.get(0).get("product_stock"), cartProductList.get(0).get("product_uuid"),cartProductList.get(0).get("product_description"));
//        }

        databaseAccess.open();

        int itemCount = cartProductList.size();

        if (itemCount > 0) {


            databaseAccess.open();
            //get data from local database
            final List<HashMap<String, String>> lines;
            lines = cartProductList;

            if (lines.isEmpty()) {
                Toasty.error(this, R.string.no_product_found, Toast.LENGTH_SHORT).show();
            } else {


                //get current timestamp

                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
                //H denote 24 hours and h denote 12 hour hour format
                String currentTime = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date()); //HH:mm:ss a

                //timestamp use for invoice id for unique
                Long tsLong = System.currentTimeMillis() / 1000;
                String timeStamp = tsLong.toString();
                Log.d("Time", timeStamp);

                final JSONObject obj = new JSONObject();
                try {


                    obj.put("order_date", currentDate);
                    obj.put("order_time", currentTime);
                    obj.put("order_timestamp", new Timestamp(System.currentTimeMillis()).getTime());
                    obj.put("order_type", type);
                    obj.put("order_payment_method", payment_method);
                    obj.put("customer_name", customer_name);
                    obj.put("order_status", Constant.COMPLETED);
                    obj.put("operation_type", "invoice");
                    obj.put("original_order_id", null);
                    obj.put("card_type_code", card_type_code);
                    obj.put("approval_code", approval_code);
                    obj.put("operation_sub_type", fromQuickBill?"freeText":"product");

                    databaseAccess.open();
                    HashMap<String, String> configuration = databaseAccess.getConfiguration();
                    String ecr_code = configuration.isEmpty() ? "" : configuration.get("ecr_code");
                    obj.put("ecr_code", ecr_code);

                    databaseAccess.open();
                    double totalPriceWithTax = databaseAccess.getTotalPriceWithTax();
                    if(fromQuickBill){
                        totalPriceWithTax=Double.parseDouble(cartProductList.get(0).get("product_price"));
                    }
                    obj.put("in_tax_total", totalPriceWithTax);

                    databaseAccess.open();
                    double totalPriceWithoutTax = databaseAccess.getTotalPriceWithoutTax();
                    if(fromQuickBill){
                        databaseAccess.open();
                        totalPriceWithoutTax=totalPriceWithTax/(1.0+databaseAccess.getShopTax()/100.0);
                    }
                    obj.put("ex_tax_total", totalPriceWithoutTax);

                    obj.put("paid_amount", total == 0 ? totalPriceWithTax : total);
                    obj.put("change_amount", change);

                    String tax_number = configuration.get("merchant_tax_number");
                    obj.put("tax_number", tax_number);

                    databaseAccess.open();
                    HashMap<String, String> sequenceMap = databaseAccess.getSequence(1, ecr_code);
                    obj.put("sequence_text", sequenceMap.get("sequence_id"));

                    obj.put("tax", calculated_tax);
                    obj.put("discount", discount);


                    JSONArray array = new JSONArray();


                    for (int i = 0; i < lines.size(); i++) {

                        databaseAccess.open();
                        String product_id = lines.get(i).get("product_id");

                        ArrayList<HashMap<String, String>> product = databaseAccess.getProductsInfo(product_id);

                        databaseAccess.open();
                        String weight_unit = databaseAccess.getWeightUnitName(product.get(0).get("product_weight_unit_id"));


                        JSONObject objp = new JSONObject();
                        objp.put("product_uuid", product.get(0).get("product_uuid"));
                        String englishName=product.get(0).get("product_name_en");
                        String arabicName=product.get(0).get("product_name_ar");
                        if(!lines.get(i).get("product_description").isEmpty()){
                            englishName=lines.get(i).get("product_description");
                            arabicName=lines.get(i).get("product_description");
                        }
                        objp.put("product_name_en", englishName);
                        objp.put("product_name_ar", arabicName);
                        objp.put("product_weight", lines.get(i).get("product_weight") + " " + weight_unit);
                        objp.put("product_qty", lines.get(i).get("product_qty"));
                        objp.put("stock", lines.get(i).get("stock") == null ? Integer.MAX_VALUE : lines.get(i).get("stock"));
                        objp.put("product_price", lines.get(i).get("product_price"));
                        objp.put("product_image", product.get(0).get("product_image") == null ? "" : product.get(0).get("product_image"));
                        objp.put("product_order_date", currentDate);
                        objp.put("product_description", lines.get(i).get("product_description"));

                        array.put(objp);

                    }
                    obj.put("lines", array);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.i("datadata", obj.toString());
                saveOrderInOfflineDb(obj);


            }

        } else {
            Toasty.error(this, R.string.no_product_in_cart, Toast.LENGTH_SHORT).show();
        }
    }

    //for save data in offline
    private void saveOrderInOfflineDb(final JSONObject obj) throws JSONException {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);

        databaseAccess.open();
        HashMap<String, String> sequenceMap = databaseAccess.getSequence(Constant.INVOICE_SEQ_ID, obj.getString("ecr_code"));

        databaseAccess.open();
        databaseAccess.updateSequence(Integer.parseInt(sequenceMap.get("next_value")), Integer.parseInt(sequenceMap.get("sequence_id")));

        String orderId = sequenceMap.get("sequence");
        databaseAccess.open();
        databaseAccess.insertOrder(orderId, obj, this,!fromQuickBill,databaseAccess);


        Toasty.success(this, R.string.order_done_successful, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, SuccessfulPayment.class).putExtra("amount", totalAmount + " " + currency).putExtra("id", orderId).putExtra("printType","invoice");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();


    }

    public HashMap<String, String> addCustomItem(String amount,String description) {
        databaseAccess.open();
        HashMap<String, String> product = databaseAccess.getCustomProduct();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("product_id", product.get("product_id"));
        map.put("product_price", amount);
        map.put("product_description", description);
        map.put("product_qty", "1");
        map.put("weight_unit_id",product.get("product_weight"));
        map.put("product_stock",product.get("product_stock"));
        map.put("product_weight_unit_id",product.get("product_weight_unit_id"));
        map.put("product_uuid", product.get("product_uuid"));
        return map;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12 && resultCode == Activity.RESULT_OK) {
            double change = data.getDoubleExtra("change", 0);
             Log.i("datadata",change+" "+totalAmount);
            try {
                proceedOrder("", "CASH", "", totalTax, "0", "", "", totalAmount, change);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}