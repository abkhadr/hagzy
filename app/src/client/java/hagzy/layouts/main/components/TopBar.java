package hagzy.layouts.main.components;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.UiHelper;

/**
 * شريط العنوان العلوي
 * يحتوي على: العنوان + أزرار الإجراءات
 */
public class TopBar {

    private Context context;
    private LinearLayout container;
    private TextView titleText;
    private LinearLayout actionsContainer;

    private LinearLayout notifBtn;
    private LinearLayout menuBtn;

    private OnActionClickListener listener;

    public TopBar(Context context, String title) {
        this.context = context;
        buildTopBar(title);
    }

    private void buildTopBar(String title) {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(dp(20), 0, dp(20), dp(12));
        container.setBackgroundColor(Color.WHITE);

        // العنوان
        titleText = UiHelper.createText(context, title, 28, "#000000", 3);
        titleText.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

        // Actions Container
        actionsContainer = new LinearLayout(context);
        actionsContainer.setOrientation(LinearLayout.HORIZONTAL);
        actionsContainer.setGravity(Gravity.CENTER_VERTICAL);

        // زر الإشعارات
        notifBtn = UiHelper.createIconButton(
                context, R.drawable.inbox, "#000000", "#000000"
        );
        notifBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick();
            }
        });

        // زر القائمة
        menuBtn = UiHelper.createIconButton(
                context, R.drawable.bars_3, "#000000", "#000000"
        );
        menuBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuClick();
            }
        });

        actionsContainer.addView(notifBtn);
        actionsContainer.addView(menuBtn);

        container.addView(titleText);
        container.addView(actionsContainer);
    }

    /**
     * تحديث العنوان
     */
    public void setTitle(String title) {
        if (titleText != null) {
            titleText.setText(title);
        }
    }

    /**
     * الحصول على الـ View
     */
    public LinearLayout getView() {
        return container;
    }

    /**
     * Listener للأزرار
     */
    public void setOnActionClickListener(OnActionClickListener listener) {
        this.listener = listener;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }

    public interface OnActionClickListener {
        void onNotificationClick();
        void onMenuClick();
    }

    public View getMenuButton() {
        return menuBtn;
    }

    public View getNotificationButton() {
        return notifBtn;
    }
}