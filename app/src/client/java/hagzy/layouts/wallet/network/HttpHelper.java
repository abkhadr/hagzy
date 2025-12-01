package hagzy.layouts.wallet.network;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import hagzy.config.PaymentConfig;

/**
 * مساعد إرسال طلبات HTTP
 */
public class HttpHelper {

    private static final String TAG = "HttpHelper";

    // ════════════════════════════════════════════════════════════
    // 🌐 واجهة الاستجابة
    // ════════════════════════════════════════════════════════════

    public interface Callback {
        void onSuccess(String response);
        void onError(String error);
    }

    // ════════════════════════════════════════════════════════════
    // 📤 إرسال طلب POST
    // ════════════════════════════════════════════════════════════

    public static void post(
            Context context,
            String url,
            String jsonBody,
            Callback callback
    ) {
        new Thread(() -> {
            try {
                logIfEnabled("POST Request to: " + url);
                logIfEnabled("Request Body: " + jsonBody);

                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

                // إعداد الاتصال
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                // إرسال البيانات
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                // قراءة الاستجابة
                int responseCode = conn.getResponseCode();
                logIfEnabled("Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = readResponse(conn);
                    logIfEnabled("Response: " + response);

                    runOnUiThread(context, () -> callback.onSuccess(response));
                } else {
                    String errorBody = readErrorResponse(conn);
                    String error = "HTTP Error " + responseCode + ": " + errorBody;
                    logIfEnabled("Error: " + error);

                    runOnUiThread(context, () -> callback.onError(error));
                }

                conn.disconnect();

            } catch (Exception e) {
                String error = "Network Error: " + e.getMessage();
                logIfEnabled("Exception: " + error);
                e.printStackTrace();

                runOnUiThread(context, () -> callback.onError(error));
            }
        }).start();
    }

    // ════════════════════════════════════════════════════════════
    // 📥 قراءة الاستجابة
    // ════════════════════════════════════════════════════════════

    private static String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private static String readErrorResponse(HttpURLConnection conn) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            return "Unable to read error response";
        }
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    private static void runOnUiThread(Context context, Runnable action) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(action);
        }
    }

    private static void logIfEnabled(String message) {
        if (PaymentConfig.ENABLE_LOGGING) {
            Log.d(TAG, message);
        }
    }
}