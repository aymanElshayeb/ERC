package com.app.smartpos.pos;

import static com.app.smartpos.common.Utils.trimLongDouble;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.smartpos.Constant;
import com.app.smartpos.R;
import com.app.smartpos.adapter.CartAdapter;
import com.app.smartpos.common.DeviceFactory.Device;
import com.app.smartpos.common.DeviceFactory.DeviceFactory;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.orders.OrdersActivity;
import com.app.smartpos.utils.BaseActivity;

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

public class ProductCart extends BaseActivity {



    private RecyclerView recyclerView;
    CartAdapter productCartAdapter;
    ImageView imgNoProduct;
    Button btnSubmitOrder;
    TextView txt_no_product,txt_total_price;
    LinearLayout linearLayout;
    List<String> customerNames,orderTypeNames,paymentMethodNames;
    ArrayAdapter<String>  customerAdapter, orderTypeAdapter,paymentMethodAdapter;

    String currency;
    DatabaseAccess databaseAccess;


    String dialogOrderType;
    String dialogOrderPaymentMethod;
    String customerName;
    String dialogDiscount;
    double total_tax;
    AlertDialog alertDialog;
    Device device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_cart);
        device=DeviceFactory.getDevice();
        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setTitle(R.string.product_cart);
        recyclerView = findViewById(R.id.cart_recyclerview);
        imgNoProduct = findViewById(R.id.image_no_product);
        btnSubmitOrder=findViewById(R.id.btn_submit_order);
        txt_no_product=findViewById(R.id.txt_no_product);
        linearLayout=findViewById(R.id.linear_layout);
        txt_total_price=findViewById(R.id.txt_total_price);

        txt_no_product.setVisibility(View.GONE);


        // set a GridLayoutManager with default vertical orientation and 3 number of columns
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView


        recyclerView.setHasFixedSize(true);


        databaseAccess = DatabaseAccess.getInstance(ProductCart.this);
        databaseAccess.open();
        currency = databaseAccess.getCurrency();

        //get data from local database
        databaseAccess.open();
        List<HashMap<String, String>> cartProductList;
        cartProductList = databaseAccess.getCartProduct();



        if (cartProductList.isEmpty()) {

            imgNoProduct.setImageResource(R.drawable.empty_cart);
            imgNoProduct.setVisibility(View.VISIBLE);
            txt_no_product.setVisibility(View.VISIBLE);
            btnSubmitOrder.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);
            txt_total_price.setVisibility(View.GONE);
        } else {


            imgNoProduct.setVisibility(View.GONE);
            //productCartAdapter = new CartAdapter(ProductCart.this, cartProductList,txt_total_price,btnSubmitOrder,imgNoProduct,txt_no_product);

            recyclerView.setAdapter(productCartAdapter);


        }




        btnSubmitOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog();


            }
        });

    }




    public void proceedOrder(String type,String payment_method,String customer_name,double calculated_tax,String discount,String card_type_code,String approval_code,double total) throws JSONException {

        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(ProductCart.this);
        databaseAccess.open();

        int itemCount = databaseAccess.getCartItemCount();

        if (itemCount>0) {



            databaseAccess.open();
            //get data from local database
            final List<HashMap<String, String>> lines;
            lines = databaseAccess.getCartProduct();

            if (lines.isEmpty()) {
                Toasty.error(ProductCart.this, R.string.no_product_found, Toast.LENGTH_SHORT).show();
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
                    obj.put("original_order_id", null);
                    obj.put("card_type_code", card_type_code);
                    obj.put("approval_code", approval_code);
                    databaseAccess.open();
                    HashMap<String,String> configuration = databaseAccess.getConfiguration();
                    String ecr_code= configuration.isEmpty() ? "" :configuration.get("ecr_code");
                    obj.put("ecr_code", ecr_code);

                    databaseAccess.open();
                    double totalPriceWithTax=databaseAccess.getTotalPriceWithTax();
                    obj.put("in_tax_total", totalPriceWithTax);

                    databaseAccess.open();
                    obj.put("ex_tax_total", databaseAccess.getTotalPriceWithoutTax());

                    obj.put("paid_amount", total==0?totalPriceWithTax:total);
                    obj.put("change_amount", 0);

                    String tax_number= configuration.get("merchant_tax_number");
                    obj.put("tax_number", tax_number);

                    databaseAccess.open();
                    HashMap<String,String> sequenceMap = databaseAccess.getSequence(1,ecr_code);
                    obj.put("sequence_text", sequenceMap.get("sequence_id"));

                    obj.put("tax", calculated_tax);
                    obj.put("discount", discount);



                    JSONArray array = new JSONArray();


                    for (int i = 0; i < lines.size(); i++) {

                        databaseAccess.open();
                        String product_id=lines.get(i).get("product_id");

                        ArrayList<HashMap<String, String>> product =databaseAccess.getProductsInfo(product_id);

                        databaseAccess.open();
                        String weight_unit=databaseAccess.getWeightUnitName(product.get(0).get("product_weight_unit_id"));


                        JSONObject objp = new JSONObject();
                        objp.put("product_uuid", product.get(0).get("product_uuid"));
                        objp.put("product_name_en", product.get(0).get("product_name_en"));
                        objp.put("product_name_ar", product.get(0).get("product_name_ar"));
                        objp.put("product_weight", lines.get(i).get("product_weight")+" "+weight_unit);
                        objp.put("product_qty", lines.get(i).get("product_qty"));
                        objp.put("stock", lines.get(i).get("stock")==null ? Integer.MAX_VALUE:lines.get(i).get("stock"));
                        objp.put("product_price", lines.get(i).get("product_price"));
                        objp.put("product_image", product.get(0).get("product_image") == null? "" : product.get(0).get("product_image"));
                        objp.put("product_order_date", currentDate);
                        objp.put("product_description", lines.get(i).get("product_description"));

                        array.put(objp);

                    }
                    obj.put("lines", array);




                } catch (JSONException e) {
                    e.printStackTrace();
                }

                saveOrderInOfflineDb(obj);




            }

        }
        else {
            Toasty.error(ProductCart.this, R.string.no_product_in_cart, Toast.LENGTH_SHORT).show();
        }
    }




    //for save data in offline
    private void saveOrderInOfflineDb(final JSONObject obj) throws JSONException {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(ProductCart.this);

        databaseAccess.open();
        HashMap<String, String> sequenceMap = databaseAccess.getSequence(Constant.INVOICE_SEQ_ID, obj.getString("ecr_code"));

        databaseAccess.open();
        databaseAccess.updateSequence(Integer.parseInt(sequenceMap.get("next_value")),Integer.parseInt(sequenceMap.get("sequence_id")));

        databaseAccess.open();
        databaseAccess.insertOrder(sequenceMap.get("sequence"),obj,ProductCart.this);


        Toasty.success(this, R.string.order_done_successful, Toast.LENGTH_SHORT).show();

        Intent intent=new Intent(ProductCart.this, OrdersActivity.class);

        startActivity(intent);
        finish();


    }




    //dialog for taking otp code
    public void dialog() {


        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(ProductCart.this);
        databaseAccess.open();
        //get data from local database
        List<HashMap<String, String>> shopData;
        shopData = databaseAccess.getShopInformation();
        String shop_currency = shopData.get(0).get("shop_currency")+" ";

        AlertDialog.Builder dialog = new AlertDialog.Builder(ProductCart.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_payment, null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);

        final Button dialog_btn_submit = dialogView.findViewById(R.id.btn_submit);
        final ImageButton dialog_btn_close = dialogView.findViewById(R.id.btn_close);
        final TextView dialog_order_payment_method =  dialogView.findViewById(R.id.dialog_order_status);
        final TextView dialog_order_type=  dialogView.findViewById(R.id.dialog_order_type);
        final TextView dialog_customer=  dialogView.findViewById(R.id.dialog_customer);

        final TextView dialog_txt_total=  dialogView.findViewById(R.id.dialog_txt_total);
        final TextView dialog_txt_total_tax=  dialogView.findViewById(R.id.dialog_txt_total_tax);
        final TextView dialog_txt_level_tax=  dialogView.findViewById(R.id.dialog_level_tax);
        final TextView dialog_txt_total_cost=  dialogView.findViewById(R.id.dialog_txt_total_cost);
        final EditText dialog_etxt_discount=  dialogView.findViewById(R.id.etxt_dialog_discount);


        final ImageButton dialog_img_customer = dialogView.findViewById(R.id.img_select_customer);
        final ImageButton dialog_img_order_payment_method = dialogView.findViewById(R.id.img_order_payment_method);
        final ImageButton dialog_img_order_type = dialogView.findViewById(R.id.img_order_type);

        databaseAccess.open();
        double total_cost=databaseAccess.getTotalPriceWithTax();
        databaseAccess.open();
        double total_cost_without_tax=databaseAccess.getTotalPriceWithoutTax();
        total_tax= total_cost - total_cost_without_tax;

        dialog_txt_total.setText(shop_currency+trimLongDouble(total_cost_without_tax));
        dialog_txt_total_tax.setText(shop_currency+trimLongDouble(total_tax));
        //dialog_txt_level_tax.setText(getString(R.string.total_tax)+"( "+total_tax+") : ");

        double discount=0;
        dialog_txt_total_cost.setText(shop_currency+trimLongDouble(total_cost));



        dialog_etxt_discount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                double discount=0;
                String get_discount=s.toString();
                if (!get_discount.isEmpty() && !get_discount.equals("."))
                {
                    double calculated_total_cost=total_cost+total_tax;
                    discount=Double.parseDouble(get_discount);
                    if(discount>calculated_total_cost)
                    {
                        dialog_etxt_discount.setError(getString(R.string.discount_cant_be_greater_than_total_price));
                        dialog_etxt_discount.requestFocus();

                        dialog_btn_submit.setVisibility(View.INVISIBLE);
                        }
                    else {

                        dialog_btn_submit.setVisibility(View.VISIBLE);
                        calculated_total_cost = total_cost + total_tax - discount;
                        dialog_txt_total_cost.setText(shop_currency + trimLongDouble(calculated_total_cost));
                    }
                }
                else
                {

                    double calculated_total_cost=total_cost+total_tax-discount;
                    dialog_txt_total_cost.setText(shop_currency+trimLongDouble(calculated_total_cost));
                }



            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




        customerNames = new ArrayList<>();


        databaseAccess.open();

        //get data from local database
        final List<HashMap<String, String>> customer;
        customer = databaseAccess.getCustomers();

        for (int i=0;  i<customer.size();  i++) {

            // Get the ID of selected Country
            customerNames.add(customer.get(i).get("customer_name"));

        }


        orderTypeNames = new ArrayList<>();
        databaseAccess.open();

        //get data from local database
        final List<HashMap<String, String>> order_type;
        order_type = databaseAccess.getOrderType();

        for (int i=0;  i<order_type.size();  i++) {

            // Get the ID of selected Country
            orderTypeNames.add(order_type.get(i).get("order_type_name"));

        }




        //payment methods
        paymentMethodNames = new ArrayList<>();
        databaseAccess.open();

        //get data from local database
        final List<HashMap<String, String>> payment_method;
        payment_method = databaseAccess.getPaymentMethod(true);

        for (int i=0;  i<payment_method.size();  i++) {

            // Get the ID of selected Country
            paymentMethodNames.add(payment_method.get(i).get("payment_method_name"));

        }





        dialog_img_order_payment_method.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               paymentMethodAdapter = new ArrayAdapter<>(ProductCart.this, android.R.layout.simple_list_item_1);
               paymentMethodAdapter.addAll(paymentMethodNames);

                AlertDialog.Builder dialog = new AlertDialog.Builder(ProductCart.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_list_search, null);
                dialog.setView(dialogView);
                dialog.setCancelable(false);

                Button dialog_button  = (Button) dialogView.findViewById(R.id.dialog_button);
                EditText dialog_input = (EditText) dialogView.findViewById(R.id.dialog_input);
                TextView dialog_title = (TextView) dialogView.findViewById(R.id.dialog_title);
                ListView dialog_list = (ListView) dialogView.findViewById(R.id.dialog_list);

//                dialog_title.setText(getString(R.string.zone));
                dialog_title.setText(R.string.select_payment_method);
                dialog_list.setVerticalScrollBarEnabled(true);
                dialog_list.setAdapter(paymentMethodAdapter);

                dialog_input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        paymentMethodAdapter.getFilter().filter(charSequence);
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });


                final AlertDialog alertDialog = dialog.create();

                dialog_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();



                dialog_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        alertDialog.dismiss();
                        String selectedItem = paymentMethodAdapter.getItem(position);


                        dialog_order_payment_method.setText(selectedItem);


                    }
                });
            }


        });


        dialog_img_order_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                orderTypeAdapter = new ArrayAdapter<String>(ProductCart.this, android.R.layout.simple_list_item_1);
                orderTypeAdapter.addAll(orderTypeNames);

                AlertDialog.Builder dialog = new AlertDialog.Builder(ProductCart.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_list_search, null);
                dialog.setView(dialogView);
                dialog.setCancelable(false);

                Button dialog_button  = (Button) dialogView.findViewById(R.id.dialog_button);
                EditText dialog_input = (EditText) dialogView.findViewById(R.id.dialog_input);
                TextView dialog_title = (TextView) dialogView.findViewById(R.id.dialog_title);
                ListView dialog_list = (ListView) dialogView.findViewById(R.id.dialog_list);

//                dialog_title.setText(getString(R.string.zone));
                dialog_title.setText(R.string.select_order_type);
                dialog_list.setVerticalScrollBarEnabled(true);
                dialog_list.setAdapter(orderTypeAdapter);

                dialog_input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        orderTypeAdapter.getFilter().filter(charSequence);
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });


                final AlertDialog alertDialog = dialog.create();

                dialog_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();



                dialog_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        alertDialog.dismiss();
                        String selectedItem = orderTypeAdapter.getItem(position);


                        dialog_order_type.setText(selectedItem);


                    }
                });
            }


        });



        dialog_img_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerAdapter = new ArrayAdapter<String>(ProductCart.this, android.R.layout.simple_list_item_1);
                customerAdapter.addAll(customerNames);

                AlertDialog.Builder dialog = new AlertDialog.Builder(ProductCart.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_list_search, null);
                dialog.setView(dialogView);
                dialog.setCancelable(false);

                Button dialog_button  = (Button) dialogView.findViewById(R.id.dialog_button);
                EditText dialog_input = (EditText) dialogView.findViewById(R.id.dialog_input);
                TextView dialog_title = (TextView) dialogView.findViewById(R.id.dialog_title);
                ListView dialog_list = (ListView) dialogView.findViewById(R.id.dialog_list);

//                dialog_title.setText(getString(R.string.zone));
                dialog_title.setText(R.string.select_customer);
                dialog_list.setVerticalScrollBarEnabled(true);
                dialog_list.setAdapter(customerAdapter);

                dialog_input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        customerAdapter.getFilter().filter(charSequence);
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });


                final AlertDialog alertDialog = dialog.create();

                dialog_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();



                dialog_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        alertDialog.dismiss();
                        String selectedItem = customerAdapter.getItem(position);


                        dialog_customer.setText(selectedItem);


                    }
                });
            }
        });


        alertDialog = dialog.create();
        alertDialog.show();

        dialog_btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogOrderType = dialog_order_type.getText().toString().trim();
                dialogOrderPaymentMethod = dialog_order_payment_method.getText().toString().trim();
                customerName = dialog_customer.getText().toString().trim();

                dialogDiscount = dialog_etxt_discount.getText().toString().trim();
                if (dialogDiscount.isEmpty())
                {
                    dialogDiscount="0.00";
                }

                if(dialogOrderPaymentMethod.equals("CARD")) {
                    databaseAccess.open();
                    long totalPriceWithTax = (long) databaseAccess.getTotalPriceWithTax();

                    Intent intent=device.pay(totalPriceWithTax);
//                    if(){
//                        intent.setPackage(Consts.PACKAGE);
//                        intent.setAction(Consts.CARD_ACTION);
//                        intent.putExtra(ThirdTag.CHANNEL_ID, "acquire");
//                        intent.putExtra(ThirdTag.TRANS_TYPE, 2);
//                        intent.putExtra(ThirdTag.OUT_ORDERNO, "12345");
//                        intent.putExtra(ThirdTag.AMOUNT, totalPriceWithTax);
//                        intent.putExtra(ThirdTag.INSERT_SALE, true);
//                        intent.putExtra(ThirdTag.RF_FORCE_PSW, true);
//                    }
//                    else {
//                        intent.setPackage(Consts.PACKAGE_UROVO);
//                        intent.setAction(Consts.CARD_ACTION_UROVO_PURCHASE);
//                        intent.putExtra(ThirdTag.TRANS_TYPE, "2");
//                        intent.putExtra(ThirdTag.AMOUNT, String.valueOf(totalPriceWithTax));
//                        intent.putExtra(ThirdTag.IS_APP_2_APP, true);
//                    }
//                    intent.setPackage(Consts.PACKAGE_UROVO);
//                    intent.setAction(Consts.CARD_ACTION_UROVO_PURCHASE);
//                    intent.putExtra(ThirdTag.TRANS_TYPE, "2");
//                    intent.putExtra(ThirdTag.AMOUNT, String.valueOf(totalPriceWithTax/100));
//                    intent.putExtra(ThirdTag.IS_APP_2_APP, true);

//                    startActivityForResult(intent, 12);
                    launcher.launch(intent);
                }else {
                    try {
                        proceedOrder(dialogOrderType, dialogOrderPaymentMethod, customerName, total_tax, dialogDiscount,"","",0);
                        alertDialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }



        });


        dialog_btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
            }
        });



    }

    public void updateTotalPrice(){
        databaseAccess.open();
        double total_price = databaseAccess.getTotalPriceWithTax();
        txt_total_price.setText(getString(R.string.total_price) + currency + trimLongDouble(total_price));

    }

    //for back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                Intent intent=new Intent(ProductCart.this,PosActivity.class);
                startActivity(intent);
                this.finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
////        data.getStringExtra("123");
////        Log.d("DEBUG","11111111111111111111111111111111111");
////        Log.i("datadata",data.getStringExtra(ThirdTag.JSON_DATA));
//
//        //tvResponse.setText("Sale Response："+data.getStringExtra(ThirdTag.JSON_DATA));
//        // tvResponse.setText("Response："+data.getExtras().toString());
//
//        try {
//            String jsonActivityResult = "";
//            if(Consts.MANUFACTURER.equalsIgnoreCase("newland"))
//                jsonActivityResult = ThirdTag.JSON_DATA;
//            else
//                jsonActivityResult = "result";
//            JSONObject json=new JSONObject(data.getStringExtra(jsonActivityResult));
//            JSONObject result=json.getJSONObject(data.getStringExtra(device.resultHeader()));
//            String resultStatus=result.getJSONObject("Result").getString("English");
//            if(resultStatus.equals("APPROVED")) {
//                String code=result.getJSONObject("CardScheme").getString("ID");
//                String name=result.getJSONObject("CardScheme").getString("English");
//
//                String PurchaseAmount=result.getJSONObject("Amounts").getString("PurchaseAmount");
//                String ApprovalCode=result.getString("ApprovalCode");
//                Log.i("datadata",name+" "+code);
//                databaseAccess.open();
//                //long id=databaseAccess.insertCardDetails(name,code);
//                //Log.i("datadata",id+"");
//                proceedOrder(dialogOrderType, dialogOrderPaymentMethod, customerName, total_tax, dialogDiscount, code,ApprovalCode,Double.parseDouble(PurchaseAmount));
//                alertDialog.dismiss();
//            }else{
//                Toast.makeText(this, resultStatus, Toast.LENGTH_SHORT).show();
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

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
                            proceedOrder(dialogOrderType, dialogOrderPaymentMethod, customerName, total_tax, dialogDiscount, code, ApprovalCode, Double.parseDouble(PurchaseAmount));
                            alertDialog.dismiss();
                        } else if(resultStatus.equals("Declined")) {
                            Toast.makeText(this, "Transaction Declined", Toast.LENGTH_LONG).show();
                        }
                    }
                    catch (Exception e){
                        if(statusCode.equals(Constant.REJECTED_STATUS_CODE))
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

}

