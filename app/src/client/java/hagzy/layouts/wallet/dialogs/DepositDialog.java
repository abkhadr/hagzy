package hagzy.layouts.wallet.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.ThemeManager;

import hagzy.config.PaymentConfig;

/**
 * حوار إيداع الأموال
 */
public class DepositDialog {

    private final Context context;
    private AlertDialog dialog;
    private EditText amountInput;
    private TextView errorText;

    private OnDepositListener onDepositListener;

    // ════════════════════════════════════════════════════════════
    // 🌐 واجهة الأحداث
    // ════════════════════════════════════════════════════════════

    public interface OnDepositListener {
        void onDeposit(double amount);
        void onCancel();
    }

    // ════════════════════════════════════════════════════════════
    // 🏗️ البناء
    // ════════════════════════════════════════════════════════════

    public DepositDialog(Context context) {
        this.context = context;
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 بناء الحوار
    // ════════════════════════════════════════════════════════════

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LinearLayout dialogLayout = createDialogLayout();
        builder.setView(dialogLayout);

        dialog = builder.create();

        // خلفية شفافة
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    private LinearLayout createDialogLayout() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(24), dp(24), dp(16));

        // خلفية الحوار
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));
        bg.setColor(Color.WHITE);
        layout.setBackground(bg);

        // العنوان
        layout.addView(createTitle());

        // الوصف
        layout.addView(createSubtitle());

        // تسمية المبلغ
        layout.addView(createLabel());

        // حقل الإدخال
        amountInput = createAmountInput();
        layout.addView(amountInput);

        // نص الخطأ
        errorText = createErrorText();
        layout.addView(errorText);

        // ملاحظة
        layout.addView(createNote());

        // الأزرار
        layout.addView(createButtons());

        return layout;
    }

    private TextView createTitle() {
        TextView tv = new TextView(context);
        tv.setText("إيداع في المحفظة");
        tv.setTextSize(20);
        tv.setTextColor(Color.parseColor("#000000"));
        tv.setTypeface(ThemeManager.fontBold());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(8);
        tv.setLayoutParams(params);

        return tv;
    }

    private TextView createSubtitle() {
        TextView tv = new TextView(context);
        tv.setText("أدخل المبلغ المراد إيداعه في محفظتك");
        tv.setTextSize(14);
        tv.setTextColor(Color.parseColor("#666666"));
        tv.setTypeface(ThemeManager.fontRegular());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(24);
        tv.setLayoutParams(params);

        return tv;
    }

    private TextView createLabel() {
        TextView tv = new TextView(context);
        tv.setText("المبلغ (ج.م)");
        tv.setTextSize(14);
        tv.setTextColor(Color.parseColor("#000000"));
        tv.setTypeface(ThemeManager.fontBold());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(8);
        tv.setLayoutParams(params);

        return tv;
    }

    private EditText createAmountInput() {
        EditText input = new EditText(context);
        input.setHint("100");
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setTextSize(16);
        input.setTextColor(Color.parseColor("#000000"));
        input.setHintTextColor(Color.parseColor("#999999"));
        input.setPadding(dp(16), dp(16), dp(16), dp(16));
        input.setTypeface(ThemeManager.fontRegular());

        // خلفية
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(12));
        input.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        input.setLayoutParams(params);

        // إزالة الخطأ عند الكتابة
        input.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        return input;
    }

    private TextView createErrorText() {
        TextView tv = new TextView(context);
        tv.setTextSize(13);
        tv.setTextColor(Color.parseColor("#E53935"));
        tv.setTypeface(ThemeManager.fontRegular());
        tv.setVisibility(android.view.View.GONE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(8);
        tv.setLayoutParams(params);

        return tv;
    }

    private TextView createNote() {
        TextView tv = new TextView(context);
        tv.setText(String.format(
                "الحد الأدنى: %.0f ج.م | الحد الأقصى: %.0f ج.م",
                PaymentConfig.MIN_DEPOSIT_AMOUNT,
                PaymentConfig.MAX_DEPOSIT_AMOUNT
        ));
        tv.setTextSize(12);
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setTypeface(ThemeManager.fontRegular());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(8);
        params.bottomMargin = dp(24);
        tv.setLayoutParams(params);

        return tv;
    }

    private LinearLayout createButtons() {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(0, 0, 0, dp(8));

        // زر الإلغاء
        LinearLayout cancelBtn = createButton("إلغاء", "#F5F5F5", "#000000", false);
        cancelBtn.setOnClickListener(v -> {
            if (onDepositListener != null) {
                onDepositListener.onCancel();
            }
            dismiss();
        });

        // زر المتابعة
        LinearLayout confirmBtn = createButton("متابعة", "#000000", "#FFFFFF", true);
        confirmBtn.setOnClickListener(v -> handleDeposit());

        container.addView(cancelBtn);
        container.addView(confirmBtn);

        return container;
    }

    private LinearLayout createButton(String text, String bgColor, String textColor, boolean primary) {
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

        TextView buttonText = new TextView(context);
        buttonText.setText(text);
        buttonText.setTextSize(15);
        buttonText.setTextColor(Color.parseColor(textColor));
        buttonText.setTypeface(ThemeManager.fontBold());

        button.addView(buttonText);

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
    // 🔧 معالجة الإيداع
    // ════════════════════════════════════════════════════════════

    private void handleDeposit() {
        String amountStr = amountInput.getText().toString().trim();

        // التحقق من الإدخال
        if (amountStr.isEmpty()) {
            showError("يرجى إدخال المبلغ");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            showError("مبلغ غير صحيح");
            return;
        }

        // التحقق من الحدود
        if (amount < PaymentConfig.MIN_DEPOSIT_AMOUNT) {
            showError(String.format(
                    "الحد الأدنى %.0f ج.م",
                    PaymentConfig.MIN_DEPOSIT_AMOUNT
            ));
            return;
        }

        if (amount > PaymentConfig.MAX_DEPOSIT_AMOUNT) {
            showError(String.format(
                    "الحد الأقصى %.0f ج.م",
                    PaymentConfig.MAX_DEPOSIT_AMOUNT
            ));
            return;
        }

        // إغلاق الحوار
        dismiss();

        // استدعاء المستمع
        if (onDepositListener != null) {
            onDepositListener.onDeposit(amount);
        }
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 إدارة الأخطاء
    // ════════════════════════════════════════════════════════════

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(android.view.View.VISIBLE);

        // هز حقل الإدخال
        android.animation.ObjectAnimator shake = android.animation.ObjectAnimator.ofFloat(
                amountInput, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0
        );
        shake.setDuration(500);
        shake.start();
    }

    private void hideError() {
        errorText.setVisibility(android.view.View.GONE);
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Public Methods
    // ════════════════════════════════════════════════════════════

    /**
     * تعيين مستمع الأحداث
     */
    public void setOnDepositListener(OnDepositListener listener) {
        this.onDepositListener = listener;
    }

    /**
     * تعيين مبلغ افتراضي
     */
    public void setDefaultAmount(double amount) {
        if (amountInput != null) {
            amountInput.setText(String.valueOf((int) amount));
        }
    }

    /**
     * إغلاق الحوار
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * التحقق من ظهور الحوار
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    // ════════════════════════════════════════════════════════════
    // 🛠️ Utilities
    // ════════════════════════════════════════════════════════════

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}