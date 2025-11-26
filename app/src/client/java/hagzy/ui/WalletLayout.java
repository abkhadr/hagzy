
package hagzy.ui;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hagzy.MainActivity;

public class WalletLayout {

    private final Context context;
    private FrameLayout root;
    private ScrollView mainScrollView;
    private LinearLayout contentContainer;

    private TextView balanceAmount;
    private LinearLayout transactionsContainer;
    private FrameLayout progressOverlay;
    private ProgressBar progressBar;
    private TextView errorText;

    private DatabaseReference realtimeDB;
    private FirebaseAuth mAuth;
    private FirebaseFunctions functions;
    private ValueEventListener walletListener;
    private ValueEventListener transactionsListener;

    private double currentBalance = 0.0;
    private boolean isLoading = false;

    public static class TransactionItem {
        public String id;
        public String type;
        public String title;
        public String date;
        public double amount;
        public boolean isIncome;
        public String status;
        public long timestamp;

        public TransactionItem(String id, String type, String title, String date,
                               double amount, boolean isIncome, String status, long timestamp) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.date = date;
            this.amount = amount;
            this.isIncome = isIncome;
            this.status = status;
            this.timestamp = timestamp;
        }
    }

    public WalletLayout(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        realtimeDB = FirebaseDatabase.getInstance().getReference();
        functions = FirebaseFunctions.getInstance();
        buildLayout();
        loadWalletData();
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 Layout الرئيسي
    // ════════════════════════════════════════════════════════════

    private void buildLayout() {
        root = new FrameLayout(context);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(Color.WHITE);

        mainScrollView = new ScrollView(context);
        mainScrollView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        mainScrollView.setFillViewport(true);
        mainScrollView.setVerticalScrollBarEnabled(false);

        contentContainer = new LinearLayout(context);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        mainScrollView.addView(contentContainer);
        root.addView(mainScrollView);

        buildHeader();
        buildBalanceCard();
        buildActionButtons();
        buildTransactionsSection();
        buildProgressOverlay();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            mainScrollView.setPadding(0, top, 0, bottom);
            return insets;
        });
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 Header
    // ════════════════════════════════════════════════════════════

    private void buildHeader() {
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(16), dp(16), dp(16));
        header.setBackgroundColor(Color.WHITE);

        ImageView backBtn = new ImageView(context);
        backBtn.setImageResource(R.drawable.chevron_right);
        backBtn.setRotation(180);
        backBtn.setColorFilter(Color.parseColor("#000000"));
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dp(32), dp(32));
        backParams.setMarginEnd(dp(16));
        backBtn.setLayoutParams(backParams);
        backBtn.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).onBackPressed();
            }
        });

        TextView title = createText("المحفظة", 20, "#000000", true);

        header.addView(backBtn);
        header.addView(title);

        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        header.setLayoutParams(headerParams);
        header.setElevation(dp(2));

        contentContainer.addView(header);
    }

    // ════════════════════════════════════════════════════════════
    // 💳 Balance Card
    // ════════════════════════════════════════════════════════════

    private void buildBalanceCard() {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(32), dp(40), dp(32), dp(40));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));
        bg.setColor(Color.parseColor("#000000"));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(dp(24), dp(24), dp(24), dp(16));
        card.setLayoutParams(cardParams);

        TextView balanceLabel = createText("الرصيد المتاح", 14, "#999999", false);
        balanceLabel.setGravity(Gravity.CENTER);

        balanceAmount = createText("0.00 ج.م", 36, "#FFFFFF", true);
        balanceAmount.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        amountParams.topMargin = dp(12);
        balanceAmount.setLayoutParams(amountParams);

        card.addView(balanceLabel);
        card.addView(balanceAmount);

        contentContainer.addView(card);
    }

    // ════════════════════════════════════════════════════════════
    // 🔘 Action Buttons
    // ════════════════════════════════════════════════════════════

    private void buildActionButtons() {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(dp(24), dp(0), dp(24), dp(24));

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        container.setLayoutParams(containerParams);

        container.addView(createActionButton("إيداع", "#000000", true));
        container.addView(createActionButton("سحب", "#F5F5F5", false));

        contentContainer.addView(container);
    }

    private LinearLayout createActionButton(String text, String bgColor, boolean isPrimary) {
        LinearLayout button = new LinearLayout(context);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(dp(12));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, dp(56), 1f
        );
        params.setMargins(dp(4), 0, dp(4), 0);
        button.setLayoutParams(params);

        TextView buttonText = createText(text, 16, isPrimary ? "#FFFFFF" : "#000000", true);
        button.addView(buttonText);

        button.setOnClickListener(v -> {
            if (isPrimary) {
                showDepositDialog();
            } else {
                Toast.makeText(context, "قريباً", Toast.LENGTH_SHORT).show();
            }
        });

        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1f);
            }
            return false;
        });

        return button;
    }

    // ════════════════════════════════════════════════════════════
    // 📋 Transactions Section
    // ════════════════════════════════════════════════════════════

    private void buildTransactionsSection() {
        LinearLayout section = new LinearLayout(context);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(dp(24), dp(0), dp(24), dp(24));

        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        section.setLayoutParams(sectionParams);

        TextView sectionTitle = createText("العمليات الأخيرة", 18, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        sectionTitle.setLayoutParams(titleParams);

        errorText = createText("", 14, "#E53935", false);
        errorText.setGravity(Gravity.CENTER);
        errorText.setVisibility(View.GONE);
        LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        errorParams.bottomMargin = dp(16);
        errorText.setLayoutParams(errorParams);

        transactionsContainer = new LinearLayout(context);
        transactionsContainer.setOrientation(LinearLayout.VERTICAL);
        transactionsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        section.addView(sectionTitle);
        section.addView(errorText);
        section.addView(transactionsContainer);

        contentContainer.addView(section);
    }

    // ════════════════════════════════════════════════════════════
    // ⏳ Progress Overlay
    // ════════════════════════════════════════════════════════════

    private void buildProgressOverlay() {
        progressOverlay = new FrameLayout(context);
        progressOverlay.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        progressOverlay.setBackgroundColor(Color.parseColor("#80000000"));
        progressOverlay.setVisibility(View.GONE);
        progressOverlay.setClickable(true);

        progressBar = new ProgressBar(context);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                dp(50), dp(50), Gravity.CENTER
        );
        progressBar.setLayoutParams(progressParams);
        progressBar.getIndeterminateDrawable().setColorFilter(
                Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN
        );
        progressOverlay.addView(progressBar);

        root.addView(progressOverlay);
    }

    // ════════════════════════════════════════════════════════════
    // 💰 Deposit Dialog
    // ════════════════════════════════════════════════════════════

    private void showDepositDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LinearLayout dialogLayout = new LinearLayout(context);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(dp(24), dp(24), dp(24), dp(16));

        TextView title = createText("إيداع في المحفظة", 20, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);

        TextView subtitle = createText("أدخل المبلغ المراد إيداعه", 14, "#666666", false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.bottomMargin = dp(24);
        subtitle.setLayoutParams(subtitleParams);

        TextView label = createText("المبلغ (ج.م)", 14, "#000000", true);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        labelParams.bottomMargin = dp(8);
        label.setLayoutParams(labelParams);

        EditText amountInput = createInput("100",
                android.text.InputType.TYPE_CLASS_NUMBER |
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        TextView note = createText("الحد الأدنى: 10 ج.م", 12, "#999999", false);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        noteParams.topMargin = dp(8);
        noteParams.bottomMargin = dp(24);
        note.setLayoutParams(noteParams);

        dialogLayout.addView(title);
        dialogLayout.addView(subtitle);
        dialogLayout.addView(label);
        dialogLayout.addView(amountInput);
        dialogLayout.addView(note);

        builder.setView(dialogLayout);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        LinearLayout buttonContainer = new LinearLayout(context);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setGravity(Gravity.CENTER);
        buttonContainer.setPadding(0, 0, 0, dp(8));

        LinearLayout cancelBtn = createDialogButton("إلغاء", "#F5F5F5", "#000000");
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        LinearLayout confirmBtn = createDialogButton("متابعة", "#000000", "#FFFFFF");
        confirmBtn.setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString().trim();
            if (amountStr.isEmpty()) {
                showError("يرجى إدخال المبلغ");
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount < 10) {
                    showError("الحد الأدنى 10 ج.م");
                    return;
                }
                dialog.dismiss();
                initiatePaymobDeposit(amount);
            } catch (NumberFormatException e) {
                showError("مبلغ غير صحيح");
            }
        });

        buttonContainer.addView(cancelBtn);
        buttonContainer.addView(confirmBtn);
        dialogLayout.addView(buttonContainer);

        dialog.show();
    }

    private LinearLayout createDialogButton(String text, String bgColor, String textColor) {
        LinearLayout button = new LinearLayout(context);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(dp(12));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, dp(48), 1f
        );
        params.setMargins(dp(4), 0, dp(4), 0);
        button.setLayoutParams(params);

        TextView buttonText = createText(text, 15, textColor, true);
        button.addView(buttonText);

        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1f);
            }
            return false;
        });

        return button;
    }

    // ════════════════════════════════════════════════════════════
    // 💳 Paymob Integration
    // ════════════════════════════════════════════════════════════


    private void initiatePaymobDeposit(double amount) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showError("يجب تسجيل الدخول");
            return;
        }

        hideError();
        setLoading(true);

        // بدل Firebase Functions، استخدم HTTP Request مباشر
        String apiUrl = "https://hagzy-wallet-api.hagzy-pro.workers.dev/createPaymobPayment";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("amount", amount);
            requestBody.put("userId", user.getUid());
            requestBody.put("userEmail", user.getEmail() != null ? user.getEmail() : "");
            requestBody.put("userName", user.getDisplayName() != null ? user.getDisplayName() : "مستخدم");
            requestBody.put("userPhone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Wallet", "Error creating JSON request: " + e.getMessage());
        }

        // استخدم OkHttp أو أي HTTP client
        makeHttpRequest(apiUrl, requestBody.toString(), new HttpCallback() {
            @Override
            public void onSuccess(String response) {
                setLoading(false);
                try {
                    JSONObject json = new JSONObject(response);
                    String paymentUrl = json.getString("payment_url");
                    String transactionId = json.getString("transaction_id");

                    savePendingTransaction(transactionId, amount);

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                    context.startActivity(browserIntent);

                    Toast.makeText(context, "✓ تم فتح صفحة الدفع", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    showError("خطأ في معالجة الاستجابة");
                }
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                showError("حدث خطأ: " + error);
            }
        });
    }

    // HTTP Request Helper
    private void makeHttpRequest(String url, String jsonBody, HttpCallback callback) {
        new Thread(() -> {
            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream())
                    );
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Run on UI thread
                    ((Activity) context).runOnUiThread(() ->
                            callback.onSuccess(response.toString())
                    );
                } else {
                    ((Activity) context).runOnUiThread(() ->
                            callback.onError("HTTP Error: " + responseCode)
                    );
                }
            } catch (Exception e) {
                ((Activity) context).runOnUiThread(() ->
                        callback.onError(e.getMessage())
                );
            }
        }).start();
    }

    interface HttpCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    private void savePendingTransaction(String transactionId, double amount) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("type", "deposit");
        transaction.put("amount", amount);
        transaction.put("status", "pending");
        transaction.put("transactionId", transactionId);
        transaction.put("timestamp", ServerValue.TIMESTAMP);
        transaction.put("title", "إيداع في المحفظة");

        realtimeDB.child("wallets")
                .child(user.getUid())
                .child("transactions")
                .push()
                .setValue(transaction);
    }

    // ════════════════════════════════════════════════════════════
    // 📊 Load & Display Data
    // ════════════════════════════════════════════════════════════

    private void loadWalletData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            displayEmptyState("يجب تسجيل الدخول لعرض المحفظة");
            return;
        }

        String userId = user.getUid();

        walletListener = realtimeDB.child("wallets")
                .child(userId)
                .child("balance")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Double balance = snapshot.getValue(Double.class);
                        currentBalance = balance != null ? balance : 0.0;
                        updateBalanceUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("WalletLayout", "Error loading balance", error.toException());
                        showError("فشل تحميل الرصيد");
                    }
                });

        transactionsListener = realtimeDB.child("wallets")
                .child(userId)
                .child("transactions")
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<TransactionItem> transactions = new ArrayList<>();

                        for (DataSnapshot txSnapshot : snapshot.getChildren()) {
                            String id = txSnapshot.getKey();
                            String type = txSnapshot.child("type").getValue(String.class);
                            Double amount = txSnapshot.child("amount").getValue(Double.class);
                            String status = txSnapshot.child("status").getValue(String.class);
                            Long timestamp = txSnapshot.child("timestamp").getValue(Long.class);
                            String title = txSnapshot.child("title").getValue(String.class);

                            if (amount != null && timestamp != null) {
                                boolean isIncome = "deposit".equals(type) || "refund".equals(type);

                                SimpleDateFormat sdf = new SimpleDateFormat(
                                        "d MMM yyyy, hh:mm a", new Locale("ar")
                                );
                                String date = sdf.format(new Date(timestamp));

                                transactions.add(new TransactionItem(
                                        id, type, title, date, amount, isIncome, status, timestamp
                                ));
                            }
                        }

                        transactions.sort((t1, t2) -> Long.compare(t2.timestamp, t1.timestamp));
                        displayTransactions(transactions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("WalletLayout", "Error loading transactions", error.toException());
                        showError("فشل تحميل العمليات");
                    }
                });
    }

    private void updateBalanceUI() {
        balanceAmount.setText(String.format(Locale.getDefault(), "%.2f ج.م", currentBalance));
    }

    private void displayTransactions(List<TransactionItem> transactions) {
        transactionsContainer.removeAllViews();

        if (transactions.isEmpty()) {
            displayEmptyState("لا توجد عمليات بعد");
            return;
        }

        for (TransactionItem item : transactions) {
            transactionsContainer.addView(createTransactionCard(item));
        }
    }

    private LinearLayout createTransactionCard(TransactionItem item) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(Color.parseColor("#F5F5F5"));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(8);
        card.setLayoutParams(cardParams);

        LinearLayout iconContainer = new LinearLayout(context);
        iconContainer.setOrientation(LinearLayout.VERTICAL);
        iconContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        iconContainerParams.setMarginEnd(dp(16));
        iconContainer.setLayoutParams(iconContainerParams);

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setCornerRadius(dp(24));
        iconBg.setColor(item.isIncome ?
                Color.parseColor("#E8F5E9") : Color.parseColor("#FFEBEE"));
        iconContainer.setBackground(iconBg);

        TextView icon = new TextView(context);
        icon.setText(getTransactionIcon(item.type));
        icon.setTextSize(20);
        iconContainer.addView(icon);

        LinearLayout details = new LinearLayout(context);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ));

        TextView title = createText(
                item.title != null ? item.title : getTransactionTitle(item.type),
                15, "#000000", true
        );

        TextView date = createText(item.date, 13, "#666666", false);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dateParams.topMargin = dp(4);
        date.setLayoutParams(dateParams);

        details.addView(title);
        details.addView(date);

        LinearLayout amountContainer = new LinearLayout(context);
        amountContainer.setOrientation(LinearLayout.VERTICAL);
        amountContainer.setGravity(Gravity.END);

        TextView amount = createText(
                (item.isIncome ? "+ " : "- ") +
                        String.format(Locale.getDefault(), "%.2f ج.م", item.amount),
                16,
                item.isIncome ? "#4CAF50" : "#F44336",
                true
        );

        amountContainer.addView(amount);

        if ("pending".equals(item.status)) {
            TextView statusBadge = createText("⏳ قيد المعالجة", 11, "#FF9800", true);
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            statusParams.topMargin = dp(4);
            statusBadge.setLayoutParams(statusParams);
            amountContainer.addView(statusBadge);
        }

        card.addView(iconContainer);
        card.addView(details);
        card.addView(amountContainer);

        return card;
    }

    private void displayEmptyState(String message) {
        transactionsContainer.removeAllViews();

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(dp(32), dp(64), dp(32), dp(64));

        TextView emptyIcon = new TextView(context);
        emptyIcon.setText("💳");
        emptyIcon.setTextSize(48);
        emptyIcon.setGravity(Gravity.CENTER);

        TextView emptyText = createText(message, 16, "#999999", false);
        emptyText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = dp(16);
        emptyText.setLayoutParams(textParams);

        container.addView(emptyIcon);
        container.addView(emptyText);

        transactionsContainer.addView(container);
    }
//
// ════════════════════════════════════════════════════════════
// 🔧Helper Methods
/// / ════════════════════════════════════════════════════════════
    private TextView createText(String text, int size, String color, boolean bold) {
    TextView tv = new TextView(context);
    tv.setText(text);
    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    tv.setTextColor(Color.parseColor(color));
    if (bold) {
        tv.setTypeface(ThemeManager.fontBold());
    } else {
        tv.setTypeface(ThemeManager.fontRegular());
    }
    return tv;
}private EditText createInput(String hint, int inputType) {
    EditText input = new EditText(context);
    input.setHint(hint);
    input.setInputType(inputType);
    input.setTextSize(16);
    input.setTextColor(Color.parseColor("#000000"));
    input.setHintTextColor(Color.parseColor("#999999"));
    input.setPadding(dp(16), dp(16), dp(16), dp(16));
    input.setTypeface(ThemeManager.fontRegular());    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#F5F5F5"));
    bg.setCornerRadius(dp(12));
    input.setBackground(bg);    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(56)
    );
    params.bottomMargin = dp(16);
    input.setLayoutParams(params);    return input;
}private String getTransactionIcon(String type) {
    switch (type) {
        case "deposit": return "💰";
        case "withdrawal": return "💸";
        case "booking_payment": return "🎫";
        case "refund": return "↩️";
        default: return "💳";
    }
}private String getTransactionTitle(String type) {
    switch (type) {
        case "deposit": return "إيداع في المحفظة";
        case "withdrawal": return "سحب من المحفظة";
        case "booking_payment": return "دفع حجز";
        case "refund": return "استرجاع مبلغ";
        default: return "عملية";
    }
}private void showError(String message) {
    errorText.setText(message);
    errorText.setVisibility(View.VISIBLE);    ObjectAnimator shake = ObjectAnimator.ofFloat(
            errorText, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0
    );
    shake.setDuration(500);
    shake.start();
}private void hideError() {
    errorText.setVisibility(View.GONE);
}private void setLoading(boolean loading) {
    isLoading = loading;
    progressOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
}private int dp(int value) {
    return (int) (value * context.getResources().getDisplayMetrics().density);
}public void cleanup() {
    FirebaseUser user = mAuth.getCurrentUser();
    if (user != null) {
        if (walletListener != null) {
            realtimeDB.child("wallets").child(user.getUid()).child("balance")
                    .removeEventListener(walletListener);
        }
        if (transactionsListener != null) {
            realtimeDB.child("wallets").child(user.getUid()).child("transactions")
                    .removeEventListener(transactionsListener);
        }
    }
}public FrameLayout getView() {
    return root;
}
}