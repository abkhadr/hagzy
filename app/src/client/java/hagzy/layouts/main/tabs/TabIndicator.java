package hagzy.layouts.main.tabs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.FrameLayout;
import com.bytepulse.hagzy.helpers.UiHelper;
import hagzy.layouts.main.utils.ColorAnimator;
import static com.bytepulse.hagzy.helpers.TranslationManager.isRTL;

/**
 * المؤشر المتحرك أسفل التبويبات
 */
public class TabIndicator {

    private Context context;
    private View indicator;
    private GradientDrawable background;
    private int currentColor;
    private boolean isPulsing = false;

    // الألوان
    private static final int DEFAULT_COLOR = Color.parseColor("#FFFFFF");
    private static final int SELECTED_COLOR = Color.parseColor("#FFFFFF");
    private static final int BORDER_COLOR = Color.parseColor("#F5F5F5");

    public TabIndicator(Context context) {
        this.context = context;
        this.currentColor = DEFAULT_COLOR;
        buildIndicator();
    }

    private void buildIndicator() {
        indicator = new View(context);

        background = new GradientDrawable();
        background.setColor(currentColor);
        background.setCornerRadius(dp(56));
        background.setStroke(dp(4), BORDER_COLOR);

        indicator.setBackground(background);
        indicator.setLayoutParams(new FrameLayout.LayoutParams(0, -1));
    }

    /**
     * تحديث الموضع بناءً على الـ scroll
     */
    public void updatePosition(int position, float positionOffset, int indicatorWidth) {
        if (indicator != null && indicatorWidth > 0) {
            float translationX = isRTL() ? -(position + positionOffset) * indicatorWidth : (position + positionOffset) * indicatorWidth;
            indicator.setTranslationX(translationX);
        }
    }

    /**
     * تحديث اللون للتبويب المحدد
     */
    public void updateColor(int targetColor) {
        ColorAnimator.animateBackgroundColor(
                background,
                currentColor,
                targetColor,
                220,
                color -> currentColor = color
        );
    }

    /**
     * تحديث اللون للتبويب المحدد (افتراضي)
     */
    public void setSelected() {
        updateColor(SELECTED_COLOR);
    }

    /**
     * أنيميشن النبض
     */
    public void pulse() {
        pulse(1.1f, 1f, 140);
    }

    public void pulse(float scaleX, float scaleY, long duration) {
        if (indicator == null || indicator.getWidth() == 0 || isPulsing) return;

        isPulsing = true;
        indicator.animate().cancel();

        // تحديد نقطة المحور
        indicator.setPivotX(indicator.getWidth() / 2f);
        indicator.setPivotY(indicator.getHeight() / 2f);

        // أنيميشن التكبير
        indicator.animate()
                .scaleX(scaleX)
                .scaleY(scaleY)
                .setDuration(duration)
                .withEndAction(() -> {
                    // أنيميشن الرجوع
                    indicator.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(duration)
                            .withEndAction(() -> isPulsing = false)
                            .start();
                })
                .start();
    }

    /**
     * تحديث العرض
     */
    public void setWidth(int width) {
        if (indicator != null) {
            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) indicator.getLayoutParams();
            params.width = width;
            indicator.setLayoutParams(params);
        }
    }

    /**
     * الموضع الأولي
     */
    public void setInitialPosition(int tabWidth) {
        if (indicator != null) {
            indicator.setTranslationX(-tabWidth);
        }
    }

    public View getView() {
        return indicator;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}