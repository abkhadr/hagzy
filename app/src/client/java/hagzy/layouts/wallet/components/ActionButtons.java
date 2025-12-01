package hagzy.layouts.wallet.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.ThemeManager;

/**
 * أزرار الإجراءات (إيداع - سحب)
 */
public class ActionButtons {

    private final Context context;
    private final LinearLayout root;

    private OnActionListener onActionListener;

    // ════════════════════════════════════════════════════════════
    // 🌐 واجهة الأحداث
    // ════════════════════════════════════════════════════════════

    public interface OnActionListener {
        void onDepositClick();
        void onWithdrawClick();
    }

    // ════════════════════════════════════════════════════════════
    // 🏗️ البناء
    // ════════════════════════════════════════════════════════════

    public ActionButtons(Context context) {
        this.context = context;

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(0), dp(24), dp(24));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        root.setLayoutParams(params);

        // زر الإيداع
        LinearLayout depositBtn = createActionButton("إيداع", "#000000", "#FFFFFF", true);
        root.addView(depositBtn);

        // زر السحب
        LinearLayout withdrawBtn = createActionButton("سحب", "#F5F5F5", "#000000", false);
        root.addView(withdrawBtn);
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 بناء المكونات
    // ════════════════════════════════════════════════════════════

    private LinearLayout createActionButton(
            String text,
            String bgColor,
            String textColor,
            boolean isPrimary
    ) {
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

        TextView buttonText = new TextView(context);
        buttonText.setText(text);
        buttonText.setTextSize(16);
        buttonText.setTextColor(Color.parseColor(textColor));
        buttonText.setTypeface(ThemeManager.fontBold());

        button.addView(buttonText);

        // إضافة حدث الضغط
        button.setOnClickListener(v -> {
            if (onActionListener != null) {
                if (isPrimary) {
                    onActionListener.onDepositClick();
                } else {
                    onActionListener.onWithdrawClick();
                }
            }
        });

        // إضافة تأثير الضغط
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
    // 🔧 Methods
    // ════════════════════════════════════════════════════════════

    public void setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
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