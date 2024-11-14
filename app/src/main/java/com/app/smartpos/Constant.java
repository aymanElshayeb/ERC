package com.app.smartpos;

public class Constant {

    Constant() {
        //write your action here if need
    }

    public static final String BASE_URL = "https://uat.qaema.com/ecr";
//    public static final String BASE_URL = "https://gateway-am-wso2-nonprod.apps.nt-non-ocp.neotek.sa/ecr/v1";
    public static final String LOGIN_URL = BASE_URL + "/auth/user";
    public static final String KEY_URL = BASE_URL + "/auth/validate";

    public static final String SYNC_URL = BASE_URL + "/sync";
    public static final String LAST_SYNC_URL = BASE_URL + "/sync/last";
    public static final String CRASH_REPORT_SYNC_URL = BASE_URL + "/track/error";
    public static final String REQUEST_TRACKING_SYNC_URL = BASE_URL + "/track/request";
    public static final String REGISTER_DEVICE_URL = BASE_URL + "/organization/unit/device/register";
    public static final String CHECK_COMPANY_URL = BASE_URL + "/user/companies";
    public static final String REFUND_URL = BASE_URL + "/invoice/refund/";

    public static final String PRODUCT_IMAGES_SIZE = BASE_URL + "/sync/products/images/size";
    public static final String PRODUCT_IMAGES = BASE_URL + "/sync/products/images";

    //File names
    public static final String DOWNLOAD_FILE_NAME_GZIP = "download.db.gz";
    public static final String PRODUCT_IMAGES_FILE_NAME_GZIP = "ecrcode_upload.db.gz";
    public static final String DOWNLOAD_FILE_NAME = "download.db";
    public static final String PRODUCT_IMAGES_FILE_NAME = "ecrcode_upload.db";
    public static final String UPLOAD_FILE_NAME = "upload.db";
    public static final String UPLOAD_FILE_NAME_GZIP = "upload.db.gz";


    //We will use this to store the user token number into shared preference
    public static final String SHARED_PREF_NAME = "com.app.smartpos"; //pcakage name+ id


    public static final String ORDER_STATUS = "order_status";


    //order status
    public static final String PENDING = "Pending";
    public static final String PROCESSING = "Processing";
    public static final String COMPLETED = "Completed";
    public static final String REFUNDED = "Refunded";
    public static final String CANCEL = "Cancel";


    //all table names
    public static String customers = "customers";
    public static String users = "users";
    public static String suppliers = "suppliers";
    public static String productCategory = "product_category";
    public static String products = "products";
    public static String paymentMethod = "payment_method";
    public static String expense = "expense";
    public static String productCart = "product_cart";
    public static String orderList = "order_list";
    public static String orderDetails = "order_details";


    public static int INVOICE_SEQ_ID = 1;
    public static int SHIFT_SEQ_ID = 2;

    public static int LEFT_ALIGNED = 0;
    public static int CENTER_ALIGNED = 1;
    public static int RIGHT_ALIGNED = 2;
    public static int FONT_SIZE_16 = 0;
    public static int FONT_SIZE_24 = 1;
    public static int FONT_SIZE_32 = 2;

    public static final String REPORT_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";


    public static final String ACCEPTED_STATUS_CODE = "00";
    public static final String DECLINED_STATUS_CODE = "01";
    public static final String REJECTED_STATUS_CODE = "02";

}
