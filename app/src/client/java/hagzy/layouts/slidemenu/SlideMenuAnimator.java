package hagzy.layouts.slidemenu;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import hagzy.layouts.slidemenu.models.MenuConfig;
import hagzy.layouts.slidemenu.utils.MenuRTLHelper;
import static com.bytepulse.hagzy.helpers.TranslationManager.isRTL;


/**
 *
 */
public class SlideMenuAnimator {

    private final Context context;
    private final MenuConfig config;
    private final View menuContainer;
    private final View overlay;

    private boolean isOpen = false;
    private boolean isAnimating = false;
    private final boolean isRTL = isRTL();
    private ValueAnimator currentAnimator;

    public SlideMenuAnimator(Context context, MenuConfig config,
                             View menuContainer, View overlay) {
        this.context = context;
        this.config = config;
        this.menuContainer = menuContainer;
        this.overlay = overlay;
    }

    /**
     * فتح القائمة
     */
    public void open(Runnable onComplete) {
        if (isOpen || isAnimating) return;

        isAnimating = true;

        // إظهار الـ Overlay
        overlay.setVisibility(View.VISIBLE);

        // حساب المواضع
        int menuWidth = dpToPx(config.getMenuWidth());
        float startX = MenuRTLHelper.getMenuClosedX(context, menuWidth);
        float endX = MenuRTLHelper.getMenuOpenX();

        // أنيميشن الحركة
        ValueAnimator animator = ValueAnimator.ofFloat(startX, endX);
        animator.setDuration(config.getAnimationDuration());
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            menuContainer.setTranslationX(isRTL ? value : -value);

            // أنيميشن الـ Overlay
            float progress = animation.getAnimatedFraction();
            overlay.setAlpha(progress);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                isOpen = true;
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });

        currentAnimator = animator;
        animator.start();
    }

    /**
     * إغلاق القائمة
     */
    public void close(Runnable onComplete) {
        if (!isOpen || isAnimating) return;

        isAnimating = true;

        // حساب المواضع
        int menuWidth = dpToPx(config.getMenuWidth());
        float startX = MenuRTLHelper.getMenuOpenX();
        float endX = MenuRTLHelper.getMenuClosedX(context, menuWidth);

        ValueAnimator animator = ValueAnimator.ofFloat(startX, endX);
        animator.setDuration(config.getAnimationDuration());
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            menuContainer.setTranslationX(isRTL ? value * 1.2f : -value * 1.2f);

            float progress = 1f - animation.getAnimatedFraction();
            overlay.setAlpha(progress);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                isOpen = false;
                overlay.setVisibility(View.GONE);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });

        currentAnimator = animator;
        animator.start();
    }

    /**
     * تبديل حالة القائمة
     */
    public void toggle(Runnable onComplete) {
        if (isOpen) {
            close(onComplete);
        } else {
            open(onComplete);
        }
    }

    /**
     * أنيميشن السحب (Swipe)
     * @param progress قيمة من 0 إلى 1
     */
    public void updateSwipeProgress(float progress) {
        if (isAnimating) return;

        int menuWidth = dpToPx(config.getMenuWidth());
        float closedX = MenuRTLHelper.getMenuClosedX(context, menuWidth);
        float openX = MenuRTLHelper.getMenuOpenX();

        // حساب الموضع الحالي
        float currentX = closedX + (openX - closedX) * progress;
        menuContainer.setTranslationX(currentX);

        // تحديث الـ Overlay
        overlay.setAlpha(progress);

        if (progress > 0 && overlay.getVisibility() != View.VISIBLE) {
            overlay.setVisibility(View.VISIBLE);
        }
    }

    /**
     * إنهاء السحب
     * @param velocity سرعة السحب
     */
    public void finishSwipe(float velocity) {
        float threshold = config.getSwipeThreshold();
        int menuWidth = dpToPx(config.getMenuWidth());
        float currentX = menuContainer.getTranslationX();
        float closedX = MenuRTLHelper.getMenuClosedX(context, menuWidth);
        float openX = MenuRTLHelper.getMenuOpenX();

        // حساب النسبة المئوية
        float progress = Math.abs(currentX - closedX) / Math.abs(openX - closedX);

        // تحديد الوجهة بناءً على السرعة أو المسافة
        boolean shouldOpen = velocity > 1000 || progress > threshold;

        if (shouldOpen) {
            open(null);
        } else {
            close(null);
        }
    }

    /**
     * إلغاء الأنيميشن الحالي
     */
    public void cancel() {
        if (currentAnimator != null && currentAnimator.isRunning()) {
            currentAnimator.cancel();
        }
        isAnimating = false;
    }

    /**
     * التحقق من حالة القائمة
     */
    public boolean isOpen() {
        return isOpen;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    /**
     * تحويل dp إلى px
     */
    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}