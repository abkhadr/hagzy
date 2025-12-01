package hagzy.layouts.wallet;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hagzy.activities.MainActivity;
import hagzy.config.PaymentConfig;
import hagzy.layouts.wallet.components.ActionButtons;
import hagzy.layouts.wallet.components.BalanceCard;
import hagzy.layouts.wallet.components.WalletHeader;
import hagzy.layouts.wallet.components.TransactionsSection;
import hagzy.layouts.wallet.models.TransactionItem;
import hagzy.layouts.wallet.dialogs.DepositDialog;
import hagzy.layouts.wallet.dialogs.TransactionDetailsDialog;
import hagzy.layouts.wallet.network.PaymobService;
import hagzy.layouts.wallet.utils.WalletAnimator;

/**
 * نظام المحفظة - الإصدار 3.0
 *
 * معمارية نظيفة + فصل المسؤوليات + أمان محسّن
 */
public class WalletLayout {

    private static final String TAG = "WalletLayout";

    // ════════════════════════════════════════════════════════════
    // 🎨 UI Components
    // ════════════════════════════════════════════════════════════

    private final Context context;
    private FrameLayout root;
    private ScrollView mainScrollView;
    private LinearLayout contentContainer;

    private WalletHeader header;
    private BalanceCard balanceCard;
    private ActionButtons actionButtons;
    private TransactionsSection transactionsSection;

    private FrameLayout progressOverlay;
    private ProgressBar progressBar;
    private TextView errorText;

    // ════════════════════════════════════════════════════════════
    // 🔧 Services & Data
    // ════════════════════════════════════════════════════════════

    private DatabaseReference realtimeDB;
    private FirebaseAuth auth;
    private PaymobService paymobService;

    private ValueEventListener balanceListener;
    private ValueEventListener transactionsListener;

    private boolean isLoading = false;

    // ════════════════════════════════════════════════════════════
    // 🏗️ البناء
    // ════════════════════════════════════════════════════════════

    public WalletLayout(Context context) {
        this.context = context;

        // تهيئة الخدمات
        auth = FirebaseAuth.getInstance();
        realtimeDB = FirebaseDatabase.getInstance().getReference();
        paymobService = new PaymobService(context);

        // بناء الواجهة
        buildLayout();

        // تحميل البيانات
        loadWalletData();
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 بناء الواجهة
    // ════════════════════════════════════════════════════════════

    private void buildLayout() {
        // الحاوية الرئيسية
        root = new FrameLayout(context);
        root.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        root.setBackgroundColor(Color.WHITE);

        // ScrollView للمحتوى
        mainScrollView = new ScrollView(context);
        mainScrollView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        mainScrollView.setFillViewport(true);
        mainScrollView.setVerticalScrollBarEnabled(false);

        // حاوية المحتوى
        contentContainer = new LinearLayout(context);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

        mainScrollView.addView(contentContainer);
        root.addView(mainScrollView);

        // بناء المكونات
        buildHeader();
        buildBalanceCard();
        buildActionButtons();
        buildTransactionsSection();
        buildProgressOverlay();

        // تطبيق System Insets
        applyInsets();
    }

    private void buildHeader() {
        header = new WalletHeader(context, "المحفظة");
        header.setOnBackClickListener(() -> handleBackPress());
        contentContainer.addView(header.getView());
    }

    private void buildBalanceCard() {
        balanceCard = new BalanceCard(context);
        contentContainer.addView(balanceCard.getView());
    }

    private void buildActionButtons() {
        actionButtons = new ActionButtons(context);
        actionButtons.setOnActionListener(new ActionButtons.OnActionListener() {
            @Override
            public void onDepositClick() {
                handleDepositClick();
            }

            @Override
            public void onWithdrawClick() {
                handleWithdrawClick();
            }
        });
        contentContainer.addView(actionButtons.getView());
    }

    private void buildTransactionsSection() {
        // نص الخطأ
        errorText = createText("", 14, "#E53935", false);
        errorText.setGravity(Gravity.CENTER);
        errorText.setVisibility(View.GONE);
        LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams(-1, -2);
        errorParams.setMargins(dp(24), 0, dp(24), dp(16));
        errorText.setLayoutParams(errorParams);
        contentContainer.addView(errorText);

        // قسم العمليات
        transactionsSection = new TransactionsSection(context);
        transactionsSection.setOnTransactionClickListener(this::handleTransactionClick);
        contentContainer.addView(transactionsSection.getView());
    }

    private void buildProgressOverlay() {
        progressOverlay = new FrameLayout(context);
        progressOverlay.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
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
    // 📊 تحميل البيانات
    // ════════════════════════════════════════════════════════════

    private void loadWalletData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            //displayEmptyState("يجب تسجيل الدخول لعرض المحفظة");
            return;
        }

        String userId = user.getUid();

        // الاستماع للرصيد
        balanceListener = realtimeDB
                .child(PaymentConfig.FIREBASE_WALLETS_PATH)
                .child(userId)
                .child("balance")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Double balance = snapshot.getValue(Double.class);
                        double currentBalance = balance != null ? balance : 0.0;
                        balanceCard.updateBalance(currentBalance, true);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading balance", error.toException());
                        showError("فشل تحميل الرصيد");
                    }
                });

        // الاستماع للعمليات
        transactionsListener = realtimeDB
                .child(PaymentConfig.FIREBASE_WALLETS_PATH)
                .child(userId)
                .child(PaymentConfig.FIREBASE_TRANSACTIONS_PATH)
                .orderByChild("timestamp")
                .limitToLast(PaymentConfig.TRANSACTIONS_PAGE_SIZE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        handleTransactionsUpdate(snapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading transactions", error.toException());
                        showError("فشل تحميل العمليات");
                    }
                });
    }

    private void handleTransactionsUpdate(DataSnapshot snapshot) {
        List<TransactionItem> transactions = new ArrayList<>();

        for (DataSnapshot txSnapshot : snapshot.getChildren()) {
            TransactionItem item = parseTransaction(txSnapshot);
            if (item != null) {
                transactions.add(item);
            }
        }

        // ترتيب تنازلي حسب الوقت
        transactions.sort((t1, t2) -> Long.compare(t2.timestamp, t1.timestamp));

        displayTransactions(transactions);
    }

    private TransactionItem parseTransaction(DataSnapshot snapshot) {
        try {
            String id = snapshot.getKey();
            String type = snapshot.child("type").getValue(String.class);
            Double amount = snapshot.child("amount").getValue(Double.class);
            String status = snapshot.child("status").getValue(String.class);
            Long timestamp = snapshot.child("timestamp").getValue(Long.class);
            String title = snapshot.child("title").getValue(String.class);

            if (amount == null || timestamp == null) {
                return null;
            }

            boolean isIncome = "deposit".equals(type) || "refund".equals(type);

            SimpleDateFormat sdf = new SimpleDateFormat(
                    "d MMM yyyy, hh:mm a", new Locale("ar")
            );
            String date = sdf.format(new Date(timestamp));

            return new TransactionItem(
                    id, type, title, date, amount, isIncome, status, timestamp
            );
        } catch (Exception e) {
            Log.e(TAG, "Error parsing transaction", e);
            return null;
        }
    }

    // ════════════════════════════════════════════════════════════
    // 💰 معالجة الإجراءات
    // ════════════════════════════════════════════════════════════

    private void handleDepositClick() {
        DepositDialog dialog = new DepositDialog(context);
        dialog.setOnDepositListener(new DepositDialog.OnDepositListener() {
            @Override
            public void onDeposit(double amount) {
                initiateDeposit(amount);
            }

            @Override
            public void onCancel() {
                // المستخدم ألغى
            }
        });
        dialog.show();
    }

    private void handleWithdrawClick() {
        Toast.makeText(context, "السحب قريباً", Toast.LENGTH_SHORT).show();
    }

    private void initiateDeposit(double amount) {
        hideError();
        setLoading(true);

        paymobService.createPayment(amount, new PaymobService.PaymentCallback() {
            @Override
            public void onSuccess(String paymentUrl, String transactionId) {
                setLoading(false);

                // حفظ العملية كـ pending
                savePendingTransaction(transactionId, amount);

                // فتح صفحة الدفع
                openPaymentPage(paymentUrl);

                Toast.makeText(context, "✓ تم فتح صفحة الدفع", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                showError("حدث خطأ: " + error);
            }
        });
    }

    private void savePendingTransaction(String transactionId, double amount) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("type", "deposit");
        transaction.put("amount", amount);
        transaction.put("status", "pending");
        transaction.put("transactionId", transactionId);
        transaction.put("timestamp", ServerValue.TIMESTAMP);
        transaction.put("title", "إيداع في المحفظة");

        realtimeDB
                .child(PaymentConfig.FIREBASE_WALLETS_PATH)
                .child(user.getUid())
                .child(PaymentConfig.FIREBASE_TRANSACTIONS_PATH)
                .push()
                .setValue(transaction);
    }

    private void openPaymentPage(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    // ════════════════════════════════════════════════════════════
    // 🎴 عرض العمليات
    // ════════════════════════════════════════════════════════════

    private void displayTransactions(List<TransactionItem> transactions) {
        transactionsSection.setTransactions(transactions);
    }

    private void handleTransactionClick(TransactionItem transaction) {
        TransactionDetailsDialog dialog = new TransactionDetailsDialog(context, transaction);
        dialog.show();
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    private void handleBackPress() {
        if (context instanceof MainActivity) {
            ((MainActivity) context).onBackPressed();
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
        WalletAnimator.shake(errorText);
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            contentContainer.setPadding(0, top, 0, bottom);
            return insets;
        });
    }

    private TextView createText(String text, int size, String color, boolean bold) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(Color.parseColor(color));
        if (bold) {
            tv.setTypeface(com.bytepulse.hagzy.helpers.ThemeManager.fontBold());
        } else {
            tv.setTypeface(com.bytepulse.hagzy.helpers.ThemeManager.fontRegular());
        }
        return tv;
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    // ════════════════════════════════════════════════════════════
    // 🔄 Public Methods
    // ════════════════════════════════════════════════════════════

    public FrameLayout getView() {
        return root;
    }

    public View getHeader() {
        return header.getView();
    }

    /**
     * تنظيف الموارد عند الخروج
     */
    public void cleanup() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        if (balanceListener != null) {
            realtimeDB
                    .child(PaymentConfig.FIREBASE_WALLETS_PATH)
                    .child(userId)
                    .child("balance")
                    .removeEventListener(balanceListener);
        }

        if (transactionsListener != null) {
            realtimeDB
                    .child(PaymentConfig.FIREBASE_WALLETS_PATH)
                    .child(userId)
                    .child(PaymentConfig.FIREBASE_TRANSACTIONS_PATH)
                    .removeEventListener(transactionsListener);
        }
    }
}