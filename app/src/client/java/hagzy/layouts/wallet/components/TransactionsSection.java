package hagzy.layouts.wallet.components;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.ThemeManager;

import java.util.List;

import hagzy.layouts.wallet.models.TransactionItem;

/**
 * قسم عرض العمليات
 */
public class TransactionsSection {

    private final Context context;
    private final LinearLayout root;
    private final TextView sectionTitle;
    private final LinearLayout transactionsContainer;

    private OnTransactionClickListener onTransactionClickListener;

    // ════════════════════════════════════════════════════════════
    // 🌐 واجهة الأحداث
    // ════════════════════════════════════════════════════════════

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionItem transaction);
    }

    // ════════════════════════════════════════════════════════════
    // 🏗️ البناء
    // ════════════════════════════════════════════════════════════

    public TransactionsSection(Context context) {
        this.context = context;

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(0), dp(24), dp(24));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        root.setLayoutParams(params);

        // عنوان القسم
        sectionTitle = createSectionTitle();
        root.addView(sectionTitle);

        // حاوية العمليات
        transactionsContainer = new LinearLayout(context);
        transactionsContainer.setOrientation(LinearLayout.VERTICAL);
        transactionsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        root.addView(transactionsContainer);
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 بناء المكونات
    // ════════════════════════════════════════════════════════════

    private TextView createSectionTitle() {
        TextView tv = new TextView(context);
        tv.setText("العمليات الأخيرة");
        tv.setTextSize(18);
        tv.setTextColor(Color.parseColor("#000000"));
        tv.setTypeface(ThemeManager.fontBold());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(16);
        tv.setLayoutParams(params);

        return tv;
    }

    // ════════════════════════════════════════════════════════════
    // 📊 إدارة البيانات
    // ════════════════════════════════════════════════════════════

    /**
     * تعيين قائمة العمليات
     */
    public void setTransactions(List<TransactionItem> transactions) {
        transactionsContainer.removeAllViews();

        if (transactions == null || transactions.isEmpty()) {
            showEmptyState();
            return;
        }

        for (TransactionItem item : transactions) {
            TransactionCard card = new TransactionCard(context, item);

            // إضافة حدث الضغط
            if (onTransactionClickListener != null) {
                card.setOnClickListener(() ->
                        onTransactionClickListener.onTransactionClick(item)
                );
            }

            transactionsContainer.addView(card.getView());
        }
    }

    /**
     * إضافة عملية واحدة في البداية
     */
    public void addTransaction(TransactionItem transaction) {
        TransactionCard card = new TransactionCard(context, transaction);

        if (onTransactionClickListener != null) {
            card.setOnClickListener(() ->
                    onTransactionClickListener.onTransactionClick(transaction)
            );
        }

        // إضافة في البداية
        transactionsContainer.addView(card.getView(), 0);

        // إزالة Empty State إذا كان موجوداً
        if (transactionsContainer.getChildCount() > 1) {
            View firstChild = transactionsContainer.getChildAt(1);
            if (firstChild.getTag() != null && "empty_state".equals(firstChild.getTag())) {
                transactionsContainer.removeView(firstChild);
            }
        }
    }

    /**
     * مسح جميع العمليات
     */
    public void clear() {
        transactionsContainer.removeAllViews();
        showEmptyState();
    }

    /**
     * الحصول على عدد العمليات
     */
    public int getTransactionsCount() {
        int count = transactionsContainer.getChildCount();
        // استثناء Empty State
        if (count == 1 && transactionsContainer.getChildAt(0).getTag() != null) {
            return 0;
        }
        return count;
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 Empty State
    // ════════════════════════════════════════════════════════════

    private void showEmptyState() {
        LinearLayout emptyState = createEmptyState();
        emptyState.setTag("empty_state");
        transactionsContainer.addView(emptyState);
    }

    private LinearLayout createEmptyState() {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(dp(32), dp(64), dp(32), dp(64));

        // أيقونة
        TextView icon = new TextView(context);
        icon.setText("💳");
        icon.setTextSize(48);
        icon.setGravity(Gravity.CENTER);

        // نص
        TextView text = new TextView(context);
        text.setText("لا توجد عمليات بعد");
        text.setTextSize(16);
        text.setTextColor(Color.parseColor("#999999"));
        text.setTypeface(ThemeManager.fontRegular());
        text.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = dp(16);
        text.setLayoutParams(textParams);

        // نص فرعي
        TextView subtitle = new TextView(context);
        subtitle.setText("ابدأ بإيداع أول مبلغ في محفظتك");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.parseColor("#CCCCCC"));
        subtitle.setTypeface(ThemeManager.fontRegular());
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(8);
        subtitle.setLayoutParams(subtitleParams);

        container.addView(icon);
        container.addView(text);
        container.addView(subtitle);

        return container;
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Methods
    // ════════════════════════════════════════════════════════════

    /**
     * تغيير عنوان القسم
     */
    public void setTitle(String title) {
        sectionTitle.setText(title);
    }

    /**
     * تعيين مستمع الضغط على العمليات
     */
    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.onTransactionClickListener = listener;
    }

    /**
     * إظهار/إخفاء القسم
     */
    public void setVisible(boolean visible) {
        root.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public LinearLayout getView() {
        return root;
    }

    // ════════════════════════════════════════════════════════════
    // 🛠️ Utilities
    // ════════════════════════════════════════════════════════════

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}