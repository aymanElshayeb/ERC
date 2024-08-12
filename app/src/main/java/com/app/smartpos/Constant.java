package com.app.smartpos;

public class Constant {

    Constant() {
        //write your action here if need
    }
    // API KEY
    public static final String API_KEY="eyJ4NXQiOiJPREUzWTJaaE1UQmpNRE00WlRCbU1qQXlZemxpWVRJMllqUmhZVFpsT0dJeVptVXhOV0UzWVE9PSIsImtpZCI6ImdhdGV3YXlfY2VydGlmaWNhdGVfYWxpYXMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZSI6bnVsbCwidGllciI6IlVubGltaXRlZCIsIm5hbWUiOiJFQ1JfQXBwbGljYXRpb24iLCJpZCI6MTE2LCJ1dWlkIjoiYTA2MGViNTgtN2Y5NC00YWRmLTk3YWMtZmMzZmRmOTUxNjIzIn0sImlzcyI6Imh0dHBzOlwvXC9hbS13c28yLW5vbnByb2QuYXBwcy5udC1ub24tb2NwLm5lb3Rlay5zYTo0NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6eyJVbmxpbWl0ZWQiOnsidGllclF1b3RhVHlwZSI6InJlcXVlc3RDb3VudCIsImdyYXBoUUxNYXhDb21wbGV4aXR5IjowLCJncmFwaFFMTWF4RGVwdGgiOjAsInN0b3BPblF1b3RhUmVhY2giOnRydWUsInNwaWtlQXJyZXN0TGltaXQiOjAsInNwaWtlQXJyZXN0VW5pdCI6bnVsbH19LCJrZXl0eXBlIjoiUFJPRFVDVElPTiIsInBlcm1pdHRlZFJlZmVyZXIiOiIiLCJzdWJzY3JpYmVkQVBJcyI6W3sic3Vic2NyaWJlclRlbmFudERvbWFpbiI6ImNhcmJvbi5zdXBlciIsIm5hbWUiOiJlY3IiLCJjb250ZXh0IjoiXC9lY3JcL3YxIiwicHVibGlzaGVyIjoiYWRtaW4iLCJ2ZXJzaW9uIjoidjEiLCJzdWJzY3JpcHRpb25UaWVyIjoiVW5saW1pdGVkIn1dLCJ0b2tlbl90eXBlIjoiYXBpS2V5IiwicGVybWl0dGVkSVAiOiIiLCJpYXQiOjE3MjM0MTY0MTIsImp0aSI6ImY5YmVhNjljLTM5MWEtNDM2Yi04Y2RkLWJiZDJjMWRhYjNhYSJ9.HcqqFFTyci2POp7LH6cN_jOmspJye4oors-zAOX9Tj0QFMwtIdRzuxpnpV0FHncK9y38XEw86fdauwBKtpMEV-KwQ046lXPxySFZ41Tf_KtFVKUxU5_OrSSCfBemgnkHCRDIaNhjBVZyYvDfpauZAxQux3JIQ4g48GnxErK4YiWcdnW7avbReXIKzHQ9HC_4YIb2aKC-Z14whTKWld3DO3zJReUaP3tfqkJdmHCRoSzyOvJjX07GELDN1a_1bqBBHpx7Kcr5SZGrhXWv08V4q6YLhMKAx6H_nR3hCFhb_yLXYcL7l49RDwlGCUrjYoR1Avp82y6Xiy6bz6s7H2FxsA==";
    public static final String BASE_URL = "https://gateway-am-wso2-nonprod.apps.nt-non-ocp.neotek.sa/ecr/v1";
    public static final String LOGIN_URL = BASE_URL + "/auth/user";
    public static final String SYNC_URL=BASE_URL + "/sync";
    public static final String LAST_SYNC_URL=BASE_URL + "/sync/last";
    public static final String REGISTER_DEVICE_URL=BASE_URL + "/organization/unit/device/register";

    //File names
    public static final String DOWNLOAD_FILE_NAME_GZIP = "download.db.gz";
    public static final String DOWNLOAD_FILE_NAME = "download.db";
    public static final String UPLOAD_FILE_NAME = "upload.db";
    public static final String UPLOAD_FILE_NAME_GZIP = "upload.db.gz";


    //We will use this to store the user token number into shared preference
    public static final String SHARED_PREF_NAME = "com.app.smartpos"; //pcakage name+ id


    public static final String ORDER_STATUS = "order_status";


    //order status
    public static final String PENDING = "Pending";
    public static final String PROCESSING = "Processing";
    public static final String COMPLETED = "Completed";
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



}
