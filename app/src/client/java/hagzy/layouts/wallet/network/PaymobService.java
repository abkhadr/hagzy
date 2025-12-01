package hagzy.layouts.wallet.network;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import hagzy.config.PaymentConfig;

/**
 * خدمة التعامل مع Paymob
 */
public class PaymobService {

    private final Context context;
    private final FirebaseAuth auth;

    // ════════════════════════════════════════════════════════════
    // 🌐 واجهة الاستجابة
    // ════════════════════════════════════════════════════════════

    public interface PaymentCallback {
        void onSuccess(String paymentUrl, String transactionId);
        void onError(String error);
    }

    // ════════════════════════════════════════════════════════════
    // 🏗️ البناء
    // ════════════════════════════════════════════════════════════

    public PaymobService(Context context) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
    }

    // ════════════════════════════════════════════════════════════
    // 💳 إنشاء عملية دفع
    // ════════════════════════════════════════════════════════════

    public void createPayment(double amount, PaymentCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("يجب تسجيل الدخول");
            return;
        }

        // التحقق من الحدود
        if (!validateAmount(amount)) {
            callback.onError(String.format(
                    "المبلغ يجب أن يكون بين %.0f و %.0f ج.م",
                    PaymentConfig.MIN_DEPOSIT_AMOUNT,
                    PaymentConfig.MAX_DEPOSIT_AMOUNT
            ));
            return;
        }

        // بناء الطلب
        JSONObject requestBody = buildPaymentRequest(user, amount);

        // إرسال الطلب
        HttpHelper.post(
                context,
                PaymentConfig.PAYMENT_API_URL,
                requestBody.toString(),
                new HttpHelper.Callback() {
                    @Override
                    public void onSuccess(String response) {
                        handlePaymentResponse(response, callback);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError("فشل الاتصال بخادم الدفع: " + error);
                    }
                }
        );
    }

    // ════════════════════════════════════════════════════════════
    // 🔍 التحقق من حالة الدفع
    // ════════════════════════════════════════════════════════════

    public void verifyPayment(String transactionId, HttpHelper.Callback callback) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("transactionId", transactionId);
        } catch (JSONException e) {
            callback.onError("خطأ في بناء الطلب");
            return;
        }

        HttpHelper.post(
                context,
                PaymentConfig.VERIFY_PAYMENT_URL,
                requestBody.toString(),
                callback
        );
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    private boolean validateAmount(double amount) {
        return amount >= PaymentConfig.MIN_DEPOSIT_AMOUNT &&
                amount <= PaymentConfig.MAX_DEPOSIT_AMOUNT;
    }

    private JSONObject buildPaymentRequest(FirebaseUser user, double amount) {
        JSONObject request = new JSONObject();
        try {
            request.put("amount", amount);
            request.put("userId", user.getUid());
            request.put("userEmail", user.getEmail() != null ? user.getEmail() : "");
            request.put("userName", user.getDisplayName() != null ? user.getDisplayName() : "مستخدم");
            request.put("userPhone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");

            if (PaymentConfig.USE_SANDBOX) {
                request.put("sandbox", true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return request;
    }

    private void handlePaymentResponse(String response, PaymentCallback callback) {
        try {
            JSONObject json = new JSONObject(response);

            if (json.has("error")) {
                callback.onError(json.getString("error"));
                return;
            }

            String paymentUrl = json.getString("payment_url");
            String transactionId = json.getString("transaction_id");

            callback.onSuccess(paymentUrl, transactionId);

        } catch (JSONException e) {
            callback.onError("خطأ في معالجة استجابة الخادم");
        }
    }
}