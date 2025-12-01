package hagzy.config;

/**
 * ⚠️ هذا ملف نموذجي - للاستخدام:
 * 1. انسخ هذا الملف
 * 2. أعد تسميته إلى PaymentConfig.java
 * 3. املأ المعلومات الحقيقية
 * 4. لا ترفع PaymentConfig.java على GitHub
 */
public class PaymentConfig {

    // ════════════════════════════════════════════════════════════
    // 🔐 Paymob API Configuration
    // احصل على هذه المعلومات من Paymob Dashboard
    // ════════════════════════════════════════════════════════════

    public static final String PAYMENT_API_URL =
            "https://your-api.your-domain.workers.dev/createPaymobPayment";

    public static final String VERIFY_PAYMENT_URL =
            "https://your-api.your-domain.workers.dev/verifyPayment";

    public static final String PAYMOB_INTEGRATION_ID = "YOUR_INTEGRATION_ID_HERE";

    public static final String PAYMOB_API_KEY = "YOUR_API_KEY_HERE";

    public static final String PAYMOB_HMAC_SECRET = "YOUR_HMAC_SECRET_HERE";

    // ════════════════════════════════════════════════════════════
    // 💰 حدود المعاملات
    // يمكنك تعديل هذه القيم حسب احتياجك
    // ════════════════════════════════════════════════════════════

    public static final double MIN_DEPOSIT_AMOUNT = 10.0;
    public static final double MAX_DEPOSIT_AMOUNT = 10000.0;
    public static final double MIN_WITHDRAWAL_AMOUNT = 50.0;
    public static final double MAX_WITHDRAWAL_AMOUNT = 5000.0;

    // ════════════════════════════════════════════════════════════
    // 🌐 Firebase Configuration
    // ════════════════════════════════════════════════════════════

    public static final String FIREBASE_WALLETS_PATH = "wallets";
    public static final String FIREBASE_TRANSACTIONS_PATH = "transactions";
    public static final String FIREBASE_PENDING_PATH = "pending_transactions";

    // ════════════════════════════════════════════════════════════
    // 🎨 UI Configuration
    // ════════════════════════════════════════════════════════════

    public static final int TRANSACTIONS_PAGE_SIZE = 20;
    public static final int AUTO_REFRESH_INTERVAL = 30;
    public static final int TOAST_DURATION = 2000;

    // ════════════════════════════════════════════════════════════
    // 🔍 Development Mode
    // ════════════════════════════════════════════════════════════

    public static final boolean DEVELOPMENT_MODE = true;  // غيّر إلى false في الإنتاج
    public static final boolean ENABLE_LOGGING = true;    // غيّر إلى false في الإنتاج
    public static final boolean USE_SANDBOX = true;       // غيّر إلى false في الإنتاج
}