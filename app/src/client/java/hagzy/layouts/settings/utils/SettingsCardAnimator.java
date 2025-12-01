package hagzy.layouts.settings.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Provides interactive touch animations for views, similar to the
 * soft press-and-release behavior seen in modern mobile UI.
 * This class adds smooth scaling and fading when the user presses or releases a view.
 */
public class SettingsCardAnimator {

    private static final float SCALE_PRESSED = 0.96f;
    private static final float ALPHA_PRESSED = 0.7f;
    private static final int ANIMATION_DURATION = 150;

    /**
     * Attaches a touch animation to the specified view. When pressed, the view
     * scales down slightly and fades; when released, it smoothly returns to
     * its original size and opacity. An optional callback is executed after the
     * release animation completes.
     *
     * @param view    The view to animate.
     * @param onClick A callback executed after the release animation (optional).
     */
    public static void applyAnimation(View view, Runnable onClick) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animatePress(v);
                    return true;

                case MotionEvent.ACTION_UP:
                    animateRelease(v);
                    v.performClick();
                    if (onClick != null) {
                        v.postDelayed(onClick, ANIMATION_DURATION / 2);
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    animateRelease(v);
                    return true;
            }
            return false;
        });
    }

    /**
     * Applies the "pressed" animation: the view smoothly scales down and lowers
     * its opacity, giving a soft tactile feedback effect.
     *
     * @param view The view to animate.
     */
    private static void animatePress(View view) {
        view.animate()
                .scaleX(SCALE_PRESSED)
                .scaleY(SCALE_PRESSED)
                .alpha(ALPHA_PRESSED)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Restores the view to its default scale and opacity when the touch
     * interaction ends. This animation runs when the user lifts their finger.
     *
     * @param view The view to animate.
     */
    private static void animateRelease(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

}