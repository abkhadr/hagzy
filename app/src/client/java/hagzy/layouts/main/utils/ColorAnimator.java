package hagzy.layouts.main.utils;

import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.ThemeManager;

/**
 * مساعد لأنيميشن الألوان
 */
public class ColorAnimator {

    private static final int DEFAULT_DURATION = 200;

    /**
     * تحريك لون النص
     */
    public static void animateTextColor(TextView textView, int targetColor) {
        animateTextColor(textView, targetColor, DEFAULT_DURATION);
    }

    public static void animateTextColor(TextView textView, int targetColor, int duration) {
        if (textView == null) return;

        int startColor = textView.getCurrentTextColor();
        if (startColor == targetColor) return;

        ValueAnimator animator = ValueAnimator.ofArgb(startColor, targetColor);
        animator.setDuration(duration);
        animator.addUpdateListener(a ->
                textView.setTextColor((int) a.getAnimatedValue())
        );
        animator.start();
    }

    /**
     * تحريك لون الخلفية
     */
    public static void animateBackgroundColor(GradientDrawable drawable,
                                              int startColor,
                                              int targetColor,
                                              int duration,
                                              OnColorChangeListener listener) {
        if (drawable == null) return;

        ValueAnimator animator = ValueAnimator.ofArgb(startColor, targetColor);
        animator.setDuration(duration);
        animator.addUpdateListener(a -> {
            int currentColor = (int) a.getAnimatedValue();
            drawable.setColor(currentColor);
            if (listener != null) {
                listener.onColorChanged(currentColor);
            }
        });
        animator.start();
    }

    public interface OnColorChangeListener {
        void onColorChanged(int color);
    }
}