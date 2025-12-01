package hagzy.layouts.slidemenu.items;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import hagzy.layouts.slidemenu.models.MenuConfig;
import hagzy.layouts.slidemenu.models.MenuItemData;
import hagzy.layouts.slidemenu.utils.MenuButtonAnimator;
import hagzy.layouts.slidemenu.utils.MenuRTLHelper;

/**
 * عنصر القائمة الأساسي
 * يدعم: أيقونة، نص، نص فرعي، سهم، Badge، Toggle
 */
public class MenuItem {

    private Context context;
    private MenuConfig config;
    private MenuItemData data;
    private LinearLayout itemContainer;

    public MenuItem(Context context, MenuConfig config, MenuItemData data) {
        this.context = context;
        this.config = config;
        this.data = data;
        buildItem();
    }

    private void buildItem() {
        itemContainer = new LinearLayout(context);
        itemContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(config.getItemHeight())
        ));
        itemContainer.setOrientation(LinearLayout.HORIZONTAL);
        itemContainer.setGravity(Gravity.CENTER_VERTICAL);
        itemContainer.setPadding(
                dpToPx(config.getItemPadding()),
                dpToPx(8),
                dpToPx(config.getItemPadding()),
                dpToPx(8)
        );

        // ضبط الاتجاه
        MenuRTLHelper.setLayoutDirection(itemContainer, context);

        // الخلفية
        if (data.isSelected()) {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(adjustAlpha(config.getSelectedItemColor(), 0.1f));
            bg.setCornerRadius(dpToPx(8));
            itemContainer.setBackground(bg);
        }

        // الأيقونة
        ImageView icon = createIcon();
        itemContainer.addView(icon);

        // Container للنصوص
        LinearLayout textContainer = createTextContainer();
        itemContainer.addView(textContainer);

        // Badge أو Toggle أو سهم
        if (data.hasBadge()) {
            TextView badge = createBadge();
            itemContainer.addView(badge);
        } else if (data.hasToggle()) {
            Switch toggle = createToggle();
            itemContainer.addView(toggle);
        } else if (data.shouldShowArrow()) {
            ImageView arrow = createArrow();
            itemContainer.addView(arrow);
        }

        // تطبيق الأنيميشن
        applyAnimation();

        // تفعيل/تعطيل
        itemContainer.setAlpha(data.isEnabled() ? 1f : 0.5f);
        itemContainer.setEnabled(data.isEnabled());
    }

    private ImageView createIcon() {
        ImageView icon = new ImageView(context);
        int size = dpToPx(24);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        MenuRTLHelper.setMarginRTL(icon, 0, 0, 12, 0);
        icon.setLayoutParams(params);

        icon.setImageResource(data.getIconRes());
        icon.setColorFilter(data.isSelected() ?
                config.getSelectedItemColor() :
                config.getItemIconColor()
        );

        return icon;
    }

    private LinearLayout createTextContainer() {
        LinearLayout container = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        container.setLayoutParams(params);
        container.setOrientation(LinearLayout.VERTICAL);

        // النص الرئيسي
        TextView title = new TextView(context);
        title.setText(data.getTitle());
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTextColor(data.isSelected() ?
                config.getSelectedItemColor() :
                config.getItemTextColor()
        );
        container.addView(title);

        // النص الفرعي
        if (!data.getSubtitle().isEmpty()) {
            TextView subtitle = new TextView(context);
            subtitle.setText(data.getSubtitle());
            subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            subtitle.setTextColor(adjustAlpha(config.getItemTextColor(), 0.6f));
            LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            subtitleParams.topMargin = dpToPx(2);
            subtitle.setLayoutParams(subtitleParams);
            container.addView(subtitle);
        }

        return container;
    }

    private TextView createBadge() {
        TextView badge = new TextView(context);
        badge.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        badge.setText(data.getBadgeText());
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        badge.setTextColor(Color.WHITE);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));

        // خلفية دائرية
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(data.getBadgeColor());
        bg.setCornerRadius(dpToPx(12));
        badge.setBackground(bg);

        // أنيميشن Badge
        MenuButtonAnimator.animateBadge(badge);

        return badge;
    }

    private Switch createToggle() {
        Switch toggle = new Switch(context);
        toggle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        toggle.setChecked(data.getToggleState());
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            data.setToggleState(isChecked);
        });

        return toggle;
    }

    private ImageView createArrow() {
        ImageView arrow = new ImageView(context);
        int size = dpToPx(20);
        arrow.setLayoutParams(new LinearLayout.LayoutParams(size, size));

        // اختيار الأيقونة حسب الاتجاه
        int arrowRes = MenuRTLHelper.isRTL(context) ?
                android.R.drawable.arrow_down_float : // استبدل بأيقونتك
                android.R.drawable.arrow_down_float;  // استبدل بأيقونتك

        arrow.setImageResource(arrowRes);
        arrow.setColorFilter(adjustAlpha(config.getItemIconColor(), 0.5f));

        return arrow;
    }

    private void applyAnimation() {
        switch (data.getAnimationType()) {
            case BOUNCE:
                MenuButtonAnimator.applyBounceAnimation(itemContainer, data.getOnClick());
                break;
            case LIGHT:
                MenuButtonAnimator.applyLightAnimation(itemContainer, data.getOnClick());
                break;
            case PULSE:
                MenuButtonAnimator.applyPulseAnimation(itemContainer);
                MenuButtonAnimator.applyStandardAnimation(itemContainer, data.getOnClick());
                break;
            case STANDARD:
            default:
                MenuButtonAnimator.applyStandardAnimation(itemContainer, data.getOnClick());
                break;
        }
    }

    /**
     * تحديث حالة العنصر
     */
    public void setSelected(boolean selected) {
        data.setSelected(selected);
        buildItem(); // إعادة البناء
    }

    public void updateBadge(String text) {
        if (data.hasBadge()) {
            data.setBadge(true, text);
            buildItem();
        }
    }

    public void setEnabled(boolean enabled) {
        data.setEnabled(enabled);
        itemContainer.setAlpha(enabled ? 1f : 0.5f);
        itemContainer.setEnabled(enabled);
    }

    public View getView() {
        return itemContainer;
    }

    public MenuItemData getData() {
        return data;
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}