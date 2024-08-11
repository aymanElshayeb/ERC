package com.app.smartpos;

public class Constant {

    Constant() {
        //write your action here if need
    }


    public final static String PACKAGE_UROVO  = "com.neoleap.urovo.launcher";
    public final static String CARD_ACTION_UROVO_PURCHASE = "com.urovo.neoleap.launcher.PURCHASE";
    public final static String CARD_ACTION_UROVO_REFUND = "com.urovo.neoleap.launcher.REFUND";
    public final static String CARD_ACTION_UROVO_RECON = "com.urovo.neoleap.launcher.RECONCILIATION";

    public final static String CARD_ACTION_UROVO_TERMINAL_INFO = "com.urovo.neoleap.launcher.TERMINALINFO";
    public final static String CARD_ACTION_UROVO_ENABEL_MENU = "com.urovo.neoleap.launcher.ENABLEMENU";
    public final static String CARD_ACTION_UROVO_LAST_TRX= "com.urovo.neoleap.launcher.LASTTRXN";

    public final static String CARD_ACTION_UROVO_LAST_RECON= "com.urovo.neoleap.launcher.LASTRECON";

    public final static  String SALE = "Sale";
    public final static  String GET_ALL_TRX_BY_DATE = "Get All Transactions";
    public final static  String REFUND = "Refund";
    public final static  String RECONCILIATION = "Reconciliation";
    public final static  String TERMINAL_INFO = "Terminal Information";
    public final static  String LAST_TRANSACTION = "Get Last Transaction";
    public final static String RECON_RESULT = "Get Recon Result";
    public final static String ENABLE= "Enable Main Menu";
    public final static String DISABLE = "Disable Main Menu";
    public final static String DONATION = "Donation";

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
