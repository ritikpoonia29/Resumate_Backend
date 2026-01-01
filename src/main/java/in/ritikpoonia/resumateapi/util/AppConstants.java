package in.ritikpoonia.resumateapi.util;

public class AppConstants {

    //Authentication routes
    public static final String AUTH_CONTROLLER = "/api/auth";
    public static final String REGISTER = "/register";
    public static final String VERIFY_EMAIL = "/verify-email";
    public static final String UPLOAD_PROFILE = "/upload-image";
    public static final String LOGIN = "/login";
    public static final String RESEND_VERIFICATION = "/resend-verification";
    public static final String PROFILE = "/profile";

    //Templates routes
    public static final String RESUME = "/api/resumes";
    public static final String ID = "/{id}";
    public static final String UPLOAD_IMAGES = "/{id}/upload-images";
    public static final String PREMIUM = "premium";
    public static final String TEMPLATES = "/api/templates";

    //Email routes
    public static final String EMAIL = "/api/email";
    public static final String SEND_RESUME = "/send-resume";

    //Order routes
    public static final String PAYMENT = "/api/payment";
    public static final String CREATE_ORDER = "/create-order";
    public static final String VERIFY = "/verify";
    public static final String HISTORY = "/history";
    public static final String ORDER_DETAILS = "/order/{orderId}";
}
