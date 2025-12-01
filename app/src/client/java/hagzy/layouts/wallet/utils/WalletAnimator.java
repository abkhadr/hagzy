package hagzy.layouts.wallet.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * مساعد الأنيميشن للمحفظة
 */
public class WalletAnimator {

    // ════════════════════════════════════════════════════════════
    // 🎬 Card Animations
    // ════════════════════════════════════════════════════════════

    /**
     * أنيميشن Uber-style للكروت
     */
    public static void applyCardPressAnimation(View view, Runnable onClick) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    animatePress(v);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                    animateRelease(v);
                    if (onClick != null) {
                        v.postDelayed(onClick, 100);
                    }
                    break;
                case android.view.MotionEvent.ACTION_CANCEL:
                    animateRelease(v);
                    break;
            }
            return true;
        });
    }

    private static void animatePress(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.96f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.96f),
                ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f)
        );
        set.setDuration(150);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    private static void animateRelease(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 0.96f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 0.96f, 1f),
                ObjectAnimator.ofFloat(view, "alpha", 0.7f, 1f)
        );
        set.setDuration(150);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 Fade Animations
    // ════════════════════════════════════════════════════════════

    /**
     * إظهار تدريجي
     */
    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animator.setDuration(duration);
        animator.start();
    }

    /**
     * إخفاء تدريجي
     */
    public static void fadeOut(View view, long duration, Runnable onEnd) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (onEnd != null) {
                    onEnd.run();
                }
            }
        });
        animator.start();
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 Slide Animations
    // ════════════════════════════════════════════════════════════

    /**
     * انزلاق من الأسفل
     */
    public static void slideInFromBottom(View view, long duration) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0f);
        animator.setDuration(duration);
        animator.setInterpolator(new OvershootInterpolator(0.8f));
        animator.start();
    }

    /**
     * انزلاق إلى الأسفل
     */
    public static void slideOutToBottom(View view, long duration, Runnable onEnd) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0f, view.getHeight());
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (onEnd != null) {
                    onEnd.run();
                }
            }
        });
        animator.start();
    }

    /**
     * انزلاق من اليمين (للغة العربية)
     */
    public static void slideInFromRight(View view, long duration) {
        view.setTranslationX(view.getWidth());
        view.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", view.getWidth(), 0f);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    /**
     * انزلاق إلى اليمين
     */
    public static void slideOutToRight(View view, long duration, Runnable onEnd) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0f, view.getWidth());
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (onEnd != null) {
                    onEnd.run();
                }
            }
        });
        animator.start();
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 Scale Animations
    // ════════════════════════════════════════════════════════════

    /**
     * تكبير مع ظهور
     */
    public static void scaleIn(View view, long duration) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f),
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        );
        set.setDuration(duration);
        set.setInterpolator(new OvershootInterpolator(1.2f));
        set.start();
    }

    /**
     * تصغير مع اختفاء
     */
    public static void scaleOut(View view, long duration, Runnable onEnd) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f),
                ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        );
        set.setDuration(duration);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (onEnd != null) {
                    onEnd.run();
                }
            }
        });
        set.start();
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 Shake Animation
    // ════════════════════════════════════════════════════════════

    /**
     * هزّة (للأخطاء)
     */
    public static void shake(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                view, "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0
        );
        animator.setDuration(500);
        animator.start();
    }

    /**
     * هزّة عمودية
     */
    public static void shakeVertical(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                view, "translationY",
                0, 15, -15, 15, -15, 10, -10, 5, -5, 0
        );
        animator.setDuration(500);
        animator.start();
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 Rotation Animations
    // ════════════════════════════════════════════════════════════

    /**
     * دوران كامل
     */
    public static void rotate360(View view, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        animator.setDuration(duration);
        animator.start();
    }

    /**
     * دوران مع تكرار
     */
    public static ObjectAnimator rotateInfinite(View view, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        animator.setDuration(duration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.start();
        return animator;
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 Bounce Animation
    // ════════════════════════════════════════════════════════════

    /**
     * نط (bounce)
     */
    public static void bounce(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f).setDuration(100),
                ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 0.9f).setDuration(100),
                ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1.05f).setDuration(100),
                ObjectAnimator.ofFloat(view, "scaleX", 1.05f, 1f).setDuration(100)
        );

        AnimatorSet setY = new AnimatorSet();
        setY.playSequentially(
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f).setDuration(100),
                ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 0.9f).setDuration(100),
                ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1.05f).setDuration(100),
                ObjectAnimator.ofFloat(view, "scaleY", 1.05f, 1f).setDuration(100)
        );

        AnimatorSet combined = new AnimatorSet();
        combined.playTogether(set, setY);
        combined.start();
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 List Item Animations
    // ════════════════════════════════════════════════════════════

    /**
     * أنيميشن ظهور عنصر قائمة
     */
    public static void animateListItem(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationY(50f);

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(position * 50L)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    /**
     * أنيميشن اختفاء عنصر قائمة
     */
    public static void animateListItemRemoval(View view, Runnable onEnd) {
        view.animate()
                .alpha(0f)
                .translationX(view.getWidth())
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    if (onEnd != null) {
                        onEnd.run();
                    }
                })
                .start();
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 Success Animation
    // ════════════════════════════════════════════════════════════

    /**
     * أنيميشن نجاح (للعمليات الناجحة)
     */
    public static void successAnimation(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                // تصغير قليل
                createScaleAnimator(view, 1f, 0.9f, 150),
                // تكبير مع overshoot
                createScaleAnimator(view, 0.9f, 1.1f, 200),
                // عودة للحجم الطبيعي
                createScaleAnimator(view, 1.1f, 1f, 150)
        );
        set.start();
    }

    private static AnimatorSet createScaleAnimator(View view, float from, float to, long duration) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", from, to),
                ObjectAnimator.ofFloat(view, "scaleY", from, to)
        );
        set.setDuration(duration);
        return set;
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 Pulse Animation
    // ════════════════════════════════════════════════════════════

    /**
     * نبض مستمر
     */
    public static ObjectAnimator pulse(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f, 1f)
        );
        set.setDuration(1000);
        set.setTarget(ValueAnimator.INFINITE);
        set.start();

        return (ObjectAnimator) set.getChildAnimations().get(0);
    }

    // ════════════════════════════════════════════════════════════
    // 🎬 Number Counter Animation
    // ════════════════════════════════════════════════════════════

    /**
     * أنيميشن عداد الأرقام
     */
    public static void animateNumber(
            android.widget.TextView textView,
            double from,
            double to,
            long duration,
            String suffix
    ) {
        ValueAnimator animator = ValueAnimator.ofFloat((float) from, (float) to);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            textView.setText(String.format(java.util.Locale.getDefault(),
                    "%.2f %s", value, suffix));
        });
        animator.start();
    }
}