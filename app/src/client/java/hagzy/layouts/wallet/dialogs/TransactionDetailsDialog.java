package hagzy.layouts.wallet.dialogs;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bytepulse.hagzy.helpers.ThemeManager;

import java.util.Locale;

import hagzy.layouts.wallet.models.TransactionItem;

/**
 * حوار تفاصيل العملية
 */
public class TransactionDetailsDialog {

    private final Context context;
    private final TransactionItem transaction;
    private AlertDialog dialog;

    // ════════════════════════════════════════════════════════════
    // 🏗️ البناء
    // ════════════════════════════════════════════════════════════

    public TransactionDetailsDialog(Context context, TransactionItem transaction) {
        this.context = context;
        this.transaction = transaction;
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 بناء الحوار
    // ════════════════════════════════════════════════════════════

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);

        LinearLayout dialogLayout = createDialogLayout();
        scrollView.addView(dialogLayout);

        builder.setView(scrollView);

        dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    private LinearLayout createDialogLayout() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(24), dp(24), dp(24));

        // خلفية
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));
        bg.setColor(Color.WHITE);
        layout.setBackground(bg);

        // الهيدر
        layout.addView(createHeader());

        // الفاصل
        layout.addView(createDivider());

        // المعلومات
        layout.addView(createInfoSection("نوع العملية", getTypeText()));
        layout.addView(createInfoSection("المبلغ", formatAmount()));
        layout.addView(createInfoSection("الحالة", transaction.getStatusText()));
        layout.addView(createInfoSection("التاريخ", transaction.date));
        layout.addView(createInfoSection("رقم العملية", transaction.id));

        // زر الإغلاق
        layout.addView(createCloseButton());

        return layout;
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 0, 0, dp(24));

        // الأيقونة
        LinearLayout iconContainer = new LinearLayout(context);
        iconContainer.setOrientation(LinearLayout.VERTICAL);
        iconContainer.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(
                dp(80), dp(80)
        );
        iconContainerParams.bottomMargin = dp(16);
        iconContainer.setLayoutParams(iconContainerParams);

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setCornerRadius(dp(40));
        iconBg.setColor(Color.parseColor(transaction.getIconBackgroundColor()));
        iconContainer.setBackground(iconBg);

        TextView icon = new TextView(context);
        icon.setText(transaction.getIcon());
        icon.setTextSize(36);
        iconContainer.addView(icon);

        // العنوان
        TextView title = new TextView(context);
        title.setText(transaction.title != null ?
                transaction.title : transaction.getDefaultTitle());
        title.setTextSize(20);
        title.setTextColor(Color.parseColor("#000000"));
        title.setTypeface(ThemeManager.fontBold());
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-2, -2);
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);

        // المبلغ الكبير
        TextView amountLarge = new TextView(context);
        amountLarge.setText(formatAmount());
        amountLarge.setTextSize(32);
        amountLarge.setTextColor(Color.parseColor(transaction.getAmountColor()));
        amountLarge.setTypeface(ThemeManager.fontBold());
        amountLarge.setGravity(Gravity.CENTER);

        header.addView(iconContainer);
        header.addView(title);
        header.addView(amountLarge);

        return header;
    }

    private android.view.View createDivider() {
        android.view.View divider = new android.view.View(context);
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(1));
        params.setMargins(0, dp(16), 0, dp(16));
        divider.setLayoutParams(params);

        return divider;
    }

    private LinearLayout createInfoSection(String label, String value) {
        LinearLayout section = new LinearLayout(context);
        section.setOrientation(LinearLayout.HORIZONTAL);
        section.setPadding(0, dp(12), 0, dp(12));

        // التسمية
        TextView labelText = new TextView(context);
        labelText.setText(label);
        labelText.setTextSize(14);
        labelText.setTextColor(Color.parseColor("#666666"));
        labelText.setTypeface(ThemeManager.fontRegular());

        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, -2, 1f);
        labelText.setLayoutParams(labelParams);

        // القيمة
        TextView valueText = new TextView(context);
        valueText.setText(value);
        valueText.setTextSize(15);
        valueText.setTextColor(Color.parseColor("#000000"));
        valueText.setTypeface(ThemeManager.fontBold());
        valueText.setGravity(Gravity.END);

        // نسخ رقم العملية عند الضغط
        if ("رقم العملية".equals(label)) {
            valueText.setOnClickListener(v -> copyToClipboard(value));
            valueText.setTextColor(Color.parseColor("#2196F3"));
        }

        section.addView(labelText);
        section.addView(valueText);

        return section;
    }

    private LinearLayout createCloseButton() {
        LinearLayout button = new LinearLayout(context);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);
        button.setPadding(0, dp(24), 0, 0);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(12));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(48));
        button.setLayoutParams(params);

        TextView buttonText = new TextView(context);
        buttonText.setText("إغلاق");
        buttonText.setTextSize(16);
        buttonText.setTextColor(Color.parseColor("#000000"));
        buttonText.setTypeface(ThemeManager.fontBold());

        button.addView(buttonText);

        button.setOnClickListener(v -> dismiss());

        // تأثير الضغط
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.7f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1f);
                    break;
            }
            return false;
        });

        return button;
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    private String getTypeText() {
        switch (transaction.type) {
            case "deposit": return "إيداع";
            case "withdrawal": return "سحب";
            case "booking_payment": return "دفع حجز";
            case "refund": return "استرجاع";
            default: return "عملية";
        }
    }

    private String formatAmount() {
        String sign = transaction.isIncome ? "+ " : "- ";
        return sign + String.format(Locale.getDefault(), "%.2f ج.م", transaction.amount);
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText("Transaction ID", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, "تم النسخ ✓", Toast.LENGTH_SHORT).show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}