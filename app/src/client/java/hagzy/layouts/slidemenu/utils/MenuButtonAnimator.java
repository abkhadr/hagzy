package hagzy.layouts.slidemenu.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * أنيميشن احترافي لأزرار القائمة الجانبية
 * يدعم RTL/LTR مع تأثيرات Uber-style
 */
public class MenuButtonAnimator {

    // معاملات الأنيميشن
    private static final float SCALE_PRESSED = 0.96f;
    private static final float ALPHA_PRESSED = 0.7f;
    private static final int PRESS_DURATION = 150;
    private static final int RELEASE_DURATION = 200;

    // تأثير الارتداد للعناصر المهمة
    private static final float BOUNCE_SCALE = 1.05f;
    private static final int BOUNCE_DURATION = 300;

    /**
     * أنيميشن قياسي للأزرار العادية
     * مناسب لمعظم عناصر القائمة
     */
    public static void applyStandardAnimation(View view, Runnable onClick) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animatePress(v, false);
                    return true;

                case MotionEvent.ACTION_UP:
                    animateRelease(v, false);
                    v.performClick();
                    if (onClick != null) {
                        v.postDelayed(onClick, PRESS_DURATION);
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    animateRelease(v, false);
                    return true;
            }
            return false;
        });
    }

    /**
     * أنيميشن مع تأثير ارتداد
     * مناسب للعناصر المهمة (Profile, Settings, Premium)
     */
    public static void applyBounceAnimation(View view, Runnable onClick) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animatePress(v, true);
                    return true;

                case MotionEvent.ACTION_UP:
                    animateRelease(v, true);
                    v.performClick();
                    if (onClick != null) {
                        v.postDelayed(onClick, PRESS_DURATION);
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    animateRelease(v, false);
                    return true;
            }
            return false;
        });
    }

    /**
     * أنيميشن خفيف للعناصر الصغيرة
     * مناسب للأيقونات والبادجات
     */
    public static void applyLightAnimation(View view, Runnable onClick) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.92f)
                            .scaleY(0.92f)
                            .alpha(0.6f)
                            .setDuration(100)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    return true;

                case MotionEvent.ACTION_UP:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(150)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    v.performClick();
                    if (onClick != null) {
                        v.postDelayed(onClick, 100);
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                    return true;
            }
            return false;
        });
    }

    /**
     * أنيميشن الضغط
     */
    private static void animatePress(View view, boolean withBounce) {
        view.animate()
                .scaleX(SCALE_PRESSED)
                .scaleY(SCALE_PRESSED)
                .alpha(ALPHA_PRESSED)
                .setDuration(PRESS_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * أنيميشن التحرير
     */
    private static void animateRelease(View view, boolean withBounce) {
        if (withBounce) {
            // أنيميشن مع ارتداد
            view.animate()
                    .scaleX(BOUNCE_SCALE)
                    .scaleY(BOUNCE_SCALE)
                    .alpha(1f)
                    .setDuration(RELEASE_DURATION / 2)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(RELEASE_DURATION / 2)
                                .setInterpolator(new OvershootInterpolator())
                                .start();
                    })
                    .start();
        } else {
            // أنيميشن عادي
            view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(RELEASE_DURATION)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    /**
     * أنيميشن ظهور العنصر (عند فتح القائمة)
     * مع تأخير تدريجي لكل عنصر
     */
    public static void animateItemEntrance(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationX(MenuRTLHelper.isRTL(view.getContext()) ? 50 : -50);

        view.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(300)
                .setStartDelay(position * 50L) // تأخير تدريجي
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * أنيميشن اختفاء العنصر (عند إغلاق القائمة)
     */
    public static void animateItemExit(View view, int position) {
        float translationX = MenuRTLHelper.isRTL(view.getContext()) ? 50 : -50;

        view.animate()
                .alpha(0f)
                .translationX(translationX)
                .setDuration(250)
                .setStartDelay(position * 30L)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * تأثير Ripple للعناصر (بديل لـ RippleDrawable)
     */
    public static void createRippleEffect(View view, int color) {
        // يمكن إضافة تأثير Ripple مخصص هنا
        // أو استخدام RippleDrawable إذا كان متاح
    }

    /**
     * أنيميشن Badge (للإشعارات)
     */
    public static void animateBadge(View badge) {
        badge.setScaleX(0f);
        badge.setScaleY(0f);

        badge.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    /**
     * أنيميشن نبضي للعناصر المهمة
     * مثل زر Premium أو الإشعارات الجديدة
     */
    public static void applyPulseAnimation(View view) {
        view.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(800)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(800)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(() -> applyPulseAnimation(view))
                            .start();
                })
                .start();
    }
}