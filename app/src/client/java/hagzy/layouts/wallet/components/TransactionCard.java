package hagzy.layouts.wallet.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.ThemeManager;

import java.util.Locale;

import hagzy.layouts.wallet.models.TransactionItem;

/**
 * كارد عرض عملية واحدة
 */
public class TransactionCard {

    private final Context context;
    private final LinearLayout root;
    private final TransactionItem transaction;

    private Runnable onClickListener;

    // ════════════════════════════════════════════════════════════
    // 🏗️ البناء
    // ════════════════════════════════════════════════════════════

    public TransactionCard(Context context, TransactionItem transaction) {
        this.context = context;
        this.transaction = transaction;

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));

        // خلفية الكارد
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(Color.parseColor("#F5F5F5"));
        root.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(8);
        root.setLayoutParams(params);

        // بناء المكونات
        root.addView(createIconContainer());
        root.addView(createDetailsSection());
        root.addView(createAmountSection());

        // إضافة تأثير الضغط
        setupClickEffect();
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 بناء المكونات
    // ════════════════════════════════════════════════════════════

    private LinearLayout createIconContainer() {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp(48), dp(48)
        );
        params.setMarginEnd(dp(16));
        container.setLayoutParams(params);

        // خلفية الأيقونة
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(24));
        bg.setColor(Color.parseColor(transaction.getIconBackgroundColor()));
        container.setBackground(bg);

        // الأيقونة
        TextView icon = new TextView(context);
        icon.setText(transaction.getIcon());
        icon.setTextSize(20);
        icon.setGravity(Gravity.CENTER);

        container.addView(icon);

        return container;
    }

    private LinearLayout createDetailsSection() {
        LinearLayout details = new LinearLayout(context);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        // العنوان
        TextView title = new TextView(context);
        title.setText(transaction.title != null ?
                transaction.title : transaction.getDefaultTitle());
        title.setTextSize(15);
        title.setTextColor(Color.parseColor("#000000"));
        title.setTypeface(ThemeManager.fontBold());

        // التاريخ
        TextView date = new TextView(context);
        date.setText(transaction.date);
        date.setTextSize(13);
        date.setTextColor(Color.parseColor("#666666"));
        date.setTypeface(ThemeManager.fontRegular());

        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dateParams.topMargin = dp(4);
        date.setLayoutParams(dateParams);

        details.addView(title);
        details.addView(date);

        return details;
    }

    private LinearLayout createAmountSection() {
        LinearLayout amountContainer = new LinearLayout(context);
        amountContainer.setOrientation(LinearLayout.VERTICAL);
        amountContainer.setGravity(Gravity.END);

        // المبلغ
        TextView amount = new TextView(context);
        amount.setText(formatAmount());
        amount.setTextSize(16);
        amount.setTextColor(Color.parseColor(transaction.getAmountColor()));
        amount.setTypeface(ThemeManager.fontBold());
        amount.setGravity(Gravity.END);

        amountContainer.addView(amount);

        // شارة الحالة (إذا كانت pending)
        if (transaction.isPending()) {
            TextView statusBadge = createStatusBadge();
            amountContainer.addView(statusBadge);
        }

        return amountContainer;
    }

    private TextView createStatusBadge() {
        TextView badge = new TextView(context);
        badge.setText(transaction.getStatusText());
        badge.setTextSize(11);
        badge.setTextColor(Color.parseColor(transaction.getStatusColor()));
        badge.setTypeface(ThemeManager.fontBold());
        badge.setGravity(Gravity.END);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(4);
        badge.setLayoutParams(params);

        return badge;
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 التفاعلات
    // ════════════════════════════════════════════════════════════

    private void setupClickEffect() {
        root.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.run();
            }
        });

        root.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.7f);
                    v.setScaleX(0.98f);
                    v.setScaleY(0.98f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1f);
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                    break;
            }
            return false;
        });
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    private String formatAmount() {
        String sign = transaction.isIncome ? "+ " : "- ";
        return sign + String.format(Locale.getDefault(), "%.2f ج.م", transaction.amount);
    }

    /**
     * تعيين مستمع الضغط
     */
    public void setOnClickListener(Runnable listener) {
        this.onClickListener = listener;
    }

    /**
     * تمييز الكارد (للبحث مثلاً)
     */
    public void highlight(boolean highlight) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));

        if (highlight) {
            bg.setColor(Color.parseColor("#FFF9C4")); // أصفر فاتح
            bg.setStroke(dp(2), Color.parseColor("#FBC02D"));
        } else {
            bg.setColor(Color.parseColor("#F5F5F5"));
        }

        root.setBackground(bg);
    }

    /**
     * تحديث حالة العملية
     */
    public void updateStatus(String newStatus) {
        // TODO: إعادة بناء قسم المبلغ
    }

    public LinearLayout getView() {
        return root;
    }

    public TransactionItem getTransaction() {
        return transaction;
    }

    // ════════════════════════════════════════════════════════════
    // 🛠️ Utilities
    // ════════════════════════════════════════════════════════════

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}