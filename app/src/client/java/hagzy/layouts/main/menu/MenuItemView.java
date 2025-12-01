package hagzy.layouts.main.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.UiHelper;

/**
 * عنصر قائمة واحد
 */
public class MenuItemView {

    private Context context;
    private LinearLayout container;
    private int iconRes;
    private String label;
    private Runnable onClick;

    public MenuItemView(Context context, int iconRes, String label, Runnable onClick) {
        this.context = context;
        this.iconRes = iconRes;
        this.label = label;
        this.onClick = onClick;
        buildItem();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void buildItem() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(56));
        params.bottomMargin = dp(4);
        container.setLayoutParams(params);

        // الخلفية
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#F5F5F5"));
        background.setCornerRadius(dp(12));
        container.setBackground(background);

        // الأيقونة
        ImageView icon = createIcon();
        container.addView(icon);

        // النص
        TextView labelText = createLabel();
        container.addView(labelText);

        // السهم
        ImageView arrow = createArrow();
        container.addView(arrow);

        // Touch Animation
        container.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.7f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1f);
                    break;
            }
            return false;
        });

        // Click
        container.setOnClickListener(v -> {
            if (onClick != null) {
                onClick.run();
            }
        });
    }

    private ImageView createIcon() {
        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(22), dp(22));
        params.setMarginEnd(dp(12));
        icon.setLayoutParams(params);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Color.parseColor("#000000"));
        return icon;
    }

    private TextView createLabel() {
        TextView text = UiHelper.createText(context, label, 15, "#000000", 3);
        text.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        return text;
    }

    private ImageView createArrow() {
        ImageView arrow = new ImageView(context);
        arrow.setLayoutParams(new LinearLayout.LayoutParams(dp(18), dp(18)));
        arrow.setImageResource(R.drawable.chevron_right);
        arrow.setColorFilter(Color.parseColor("#999999"));
        return arrow;
    }

    public LinearLayout getView() {
        return container;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}