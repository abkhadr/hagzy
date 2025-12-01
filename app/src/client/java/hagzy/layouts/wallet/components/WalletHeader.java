package hagzy.layouts.wallet.components;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;

/**
 * شريط عنوان المحفظة
 */
public class WalletHeader {

    private final Context context;
    private final LinearLayout root;
    private final ImageView backButton;
    private final TextView titleText;

    private OnBackClickListener onBackClickListener;

    // ════════════════════════════════════════════════════════════
    // 🌐 واجهة الأحداث
    // ════════════════════════════════════════════════════════════

    public interface OnBackClickListener {
        void onBackClick();
    }

    // ════════════════════════════════════════════════════════════
    // 🏗️ البناء
    // ════════════════════════════════════════════════════════════

    public WalletHeader(Context context, String title) {
        this.context = context;

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));
        root.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        root.setLayoutParams(params);

        // زر الرجوع
        backButton = createBackButton();
        root.addView(backButton);

        // العنوان
        titleText = createTitle(title);
        root.addView(titleText);
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 بناء المكونات
    // ════════════════════════════════════════════════════════════

    private ImageView createBackButton() {
        ImageView btn = new ImageView(context);
        btn.setImageResource(R.drawable.chevron_right);
        btn.setRotation(180);
        btn.setColorFilter(Color.parseColor("#000000"));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(32), dp(32));
        params.setMarginEnd(dp(16));
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> {
            if (onBackClickListener != null) {
                onBackClickListener.onBackClick();
            }
        });

        // إضافة تأثير الضغط
        btn.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.5f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1f);
                    break;
            }
            return false;
        });

        return btn;
    }

    private TextView createTitle(String title) {
        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setTextSize(20);
        tv.setTextColor(Color.parseColor("#000000"));
        tv.setTypeface(ThemeManager.fontBold());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        tv.setLayoutParams(params);

        return tv;
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Methods
    // ════════════════════════════════════════════════════════════

    public void setTitle(String title) {
        titleText.setText(title);
    }

    public void setOnBackClickListener(OnBackClickListener listener) {
        this.onBackClickListener = listener;
    }

    public LinearLayout getView() {
        return root;
    }

    public ImageView getBackButton() {
        return backButton;
    }

    // ════════════════════════════════════════════════════════════
    // 🛠️ Utilities
    // ════════════════════════════════════════════════════════════

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}