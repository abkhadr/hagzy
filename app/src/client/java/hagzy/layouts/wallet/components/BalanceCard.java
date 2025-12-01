package hagzy.layouts.wallet.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.ThemeManager;

import java.util.Locale;

/**
 * كارد عرض الرصيد
 */
public class BalanceCard {

    private final Context context;
    private final LinearLayout root;
    private final TextView balanceAmount;

    private double currentBalance = 0.0;

    // ════════════════════════════════════════════════════════════
    // 🏗️ البناء
    // ════════════════════════════════════════════════════════════

    public BalanceCard(Context context) {
        this.context = context;

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(32), dp(40), dp(32), dp(40));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));
        bg.setColor(Color.parseColor("#000000"));
        root.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dp(24), dp(24), dp(24), dp(16));
        root.setLayoutParams(params);

        // تسمية الرصيد
        TextView label = createLabel();
        root.addView(label);

        // قيمة الرصيد
        balanceAmount = createAmount();
        root.addView(balanceAmount);
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 بناء المكونات
    // ════════════════════════════════════════════════════════════

    private TextView createLabel() {
        TextView tv = new TextView(context);
        tv.setText("الرصيد المتاح");
        tv.setTextSize(14);
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setTypeface(ThemeManager.fontRegular());
        tv.setGravity(Gravity.CENTER);
        return tv;
    }

    private TextView createAmount() {
        TextView tv = new TextView(context);
        tv.setText("0.00 ج.م");
        tv.setTextSize(36);
        tv.setTextColor(Color.parseColor("#FFFFFF"));
        tv.setTypeface(ThemeManager.fontBold());
        tv.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(12);
        tv.setLayoutParams(params);

        return tv;
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Methods
    // ════════════════════════════════════════════════════════════

    /**
     * تحديث الرصيد مع أنيميشن
     */
    public void updateBalance(double newBalance) {
        updateBalance(newBalance, true);
    }

    /**
     * تحديث الرصيد
     */
    public void updateBalance(double newBalance, boolean animate) {
        if (animate) {
            animateBalanceChange(currentBalance, newBalance);
        } else {
            currentBalance = newBalance;
            setBalanceText(newBalance);
        }
    }

    /**
     * الحصول على الرصيد الحالي
     */
    public double getCurrentBalance() {
        return currentBalance;
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 Animations
    // ════════════════════════════════════════════════════════════

    private void animateBalanceChange(double from, double to) {
        ValueAnimator animator = ValueAnimator.ofFloat((float) from, (float) to);
        animator.setDuration(600);

        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            setBalanceText(value);
        });

        animator.start();
        currentBalance = to;
    }

    private void setBalanceText(double balance) {
        balanceAmount.setText(String.format(Locale.getDefault(), "%.2f ج.م", balance));
    }

    // ════════════════════════════════════════════════════════════
    // 🔄 Getters
    // ════════════════════════════════════════════════════════════

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