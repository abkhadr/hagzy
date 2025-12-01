package com.bytepulse.hagzy.helpers;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CoachMarkHelper {

    private static final String PREFS_NAME = "CoachMarkPrefs";
    private static final String KEY_MAIN_ONBOARDING = "main_onboarding_shown_v1";

    private Activity activity;
    private ViewGroup rootView;
    private FrameLayout overlayContainer;
    private List<CoachStep> steps = new ArrayList<>();
    private int currentStep = 0;
    private OnCompleteListener completeListener;

    public interface OnCompleteListener {
        void onComplete();
    }

    public static class CoachStep {
        View targetView;
        String title;
        String description;
        int position; // 0=auto, 1=force top, 2=force bottom
        boolean allowInteraction; // السماح بالتفاعل مع العنصر

        public CoachStep(View target, String title, String desc, int pos) {
            this.targetView = target;
            this.title = title;
            this.description = desc;
            this.position = pos;
            this.allowInteraction = false;
        }

        public CoachStep setAllowInteraction(boolean allow) {
            this.allowInteraction = allow;
            return this;
        }
    }

    public CoachMarkHelper(Activity activity) {
        this.activity = activity;
        this.rootView = (ViewGroup) activity.getWindow().getDecorView();
    }

    // ✅ استخدام SharedPreferences
    public static boolean shouldShow(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return !prefs.getBoolean(KEY_MAIN_ONBOARDING, false);
    }

    public static void markAsShown(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_MAIN_ONBOARDING, true).apply();
    }

    public static void resetOnboarding(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_MAIN_ONBOARDING, false).apply();
    }

    public CoachMarkHelper addStep(View target, String title, String desc, int position) {
        CoachStep step = new CoachStep(target, title, desc, position);
        steps.add(step);
        return this;
    }

    public CoachMarkHelper addStepWithInteraction(View target, String title, String desc, int position, boolean allowInteraction) {
        CoachStep step = new CoachStep(target, title, desc, position);
        step.setAllowInteraction(allowInteraction);
        steps.add(step);
        return this;
    }

    public CoachMarkHelper setOnCompleteListener(OnCompleteListener listener) {
        this.completeListener = listener;
        return this;
    }

    public void start() {
        if (steps.isEmpty()) return;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentStep = 0;
            showStep(currentStep);
        }, 100);
    }

    private void showStep(int index) {
        if (index >= steps.size()) {
            finish();
            return;
        }

        // إزالة الخطوة السابقة
        if (overlayContainer != null) {
            rootView.removeView(overlayContainer);
        }

        CoachStep step = steps.get(index);

        // إنشاء Overlay جديد
        overlayContainer = new FrameLayout(activity);
        overlayContainer.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlayContainer.setClickable(true);

        // Spotlight View مع نبض
        SpotlightView spotlight = new SpotlightView(activity, step.targetView, step.allowInteraction);
        overlayContainer.addView(spotlight);

        // Description Card
        LinearLayout card = createCard(step, index);
        overlayContainer.addView(card);

        rootView.addView(overlayContainer);

        // Fade in animation
        overlayContainer.setAlpha(0f);
        overlayContainer.animate().alpha(1f).setDuration(300).start();

        // ✅ تحريك الكارد من خارج الشاشة
        animateCardEntry(card, step);
    }

    private void animateCardEntry(LinearLayout card, CoachStep step) {
        // تحديد الاتجاه
        int[] location = new int[2];
        step.targetView.getLocationOnScreen(location);
        int targetY = location[1];
        int screenHeight = rootView.getHeight();

        boolean isCardAtBottom = (step.position == 2) ||
                (step.position == 0 && targetY < screenHeight / 2);

        if (isCardAtBottom) {
            // الكارد في الأسفل - يدخل من الأسفل
            card.setTranslationY(screenHeight);
            card.animate()
                    .translationY(0)
                    .setDuration(400)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        } else {
            // الكارد في الأعلى - يدخل من الأعلى
            card.setTranslationY(-screenHeight);
            card.animate()
                    .translationY(0)
                    .setDuration(400)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private LinearLayout createCard(CoachStep step, int index) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(24), dp(20), dp(24), dp(20));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // ✅ حساب موضع الكارد الذكي
        int[] location = new int[2];
        step.targetView.getLocationOnScreen(location);
        int targetY = location[1];
        int targetHeight = step.targetView.getHeight();
        int screenHeight = rootView.getHeight();

        boolean placeAtBottom;

        if (step.position == 1) {
            // إجباري في الأعلى
            placeAtBottom = false;
        } else if (step.position == 2) {
            // إجباري في الأسفل
            placeAtBottom = true;
        } else {
            // تلقائي: إذا العنصر في النصف العلوي، الكارد في الأسفل
            placeAtBottom = targetY < screenHeight / 2;
        }

        if (placeAtBottom) {
            // الكارد في الأسفل
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.bottomMargin = dp(40);
        } else {
            // الكارد في الأعلى
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.topMargin = dp(80);
        }

        params.leftMargin = dp(20);
        params.rightMargin = dp(20);
        card.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        card.setBackground(bg);
        card.setElevation(dp(8));

        // العنوان
        TextView title = UiHelper.createText(activity, step.title, 20, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        card.addView(title);

        // الوصف
        TextView desc = UiHelper.createText(activity, step.description, 16, "#666666", 1);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(-1, -2);
        descParams.bottomMargin = dp(20);
        desc.setLayoutParams(descParams);
        card.addView(desc);

        // Progress و Buttons
        LinearLayout footer = new LinearLayout(activity);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

        // Progress dots
        LinearLayout dotsContainer = new LinearLayout(activity);
        dotsContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams dotsParams = new LinearLayout.LayoutParams(0, -2, 1);
        dotsContainer.setLayoutParams(dotsParams);

        for (int i = 0; i < steps.size(); i++) {
            View dot = new View(activity);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(8), dp(8));
            if (i < steps.size() - 1) dotParams.rightMargin = dp(8);
            dot.setLayoutParams(dotParams);

            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(i == index ? Color.BLACK : Color.parseColor("#E0E0E0"));
            dot.setBackground(dotBg);

            dotsContainer.addView(dot);
        }
        footer.addView(dotsContainer);

        // Skip button
        if (index < steps.size() - 1) {
            TextView skip = UiHelper.createText(activity, "تخطي", 15, "#999999", 1);
            skip.setPadding(dp(12), dp(8), dp(12), dp(8));
            skip.setOnClickListener(v -> finish());
            footer.addView(skip);
        }

        // Next/Finish button
        TextView nextBtn = UiHelper.createText(activity,
                index == steps.size() - 1 ? "فهمت" : "التالي",
                14, "#FFFFFF", 3);
        nextBtn.setPadding(dp(20), dp(6), dp(20), dp(6));

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.BLACK);
        btnBg.setCornerRadius(dp(8));
        nextBtn.setBackground(btnBg);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(-2, -2);
        btnParams.setMarginEnd(dp(12));
        btnParams.gravity = Gravity.CENTER;
        nextBtn.setLayoutParams(btnParams);

        nextBtn.setOnClickListener(v -> {
            currentStep++;
            showStep(currentStep);
        });

        footer.addView(nextBtn);
        card.addView(footer);

        return card;
    }

    private void finish() {
        if (overlayContainer != null) {
            overlayContainer.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        rootView.removeView(overlayContainer);
                        overlayContainer = null;

                        markAsShown(activity);

                        if (completeListener != null) {
                            completeListener.onComplete();
                        }
                    })
                    .start();
        }
    }

    private int dp(int value) {
        return UiHelper.dp(activity, value);
    }

    // ✅ Spotlight View مع تأثير النبض النظيف
    private static class SpotlightView extends View {
        private Paint bgPaint;
        private Paint clearPaint;
        private Paint pulsePaint;
        private View targetView;
        private RectF spotRect;
        private float currentRadius = 0;
        private float targetRadius;
        private float pulseRadius = 0;
        private ValueAnimator pulseAnimator;

        public SpotlightView(Context context, View target, boolean allowInteraction) {
            super(context);
            this.targetView = target;

            bgPaint = new Paint();
            bgPaint.setColor(Color.parseColor("#CC000000"));

            clearPaint = new Paint();
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            clearPaint.setAntiAlias(true);

            pulsePaint = new Paint();
            pulsePaint.setStyle(Paint.Style.STROKE);
            pulsePaint.setStrokeWidth(0);
            pulsePaint.setAntiAlias(true);
            pulsePaint.setColor(Color.WHITE);

            setLayerType(LAYER_TYPE_SOFTWARE, null);

            calculateSpotlight();
            animateSpotlight();
            startPulseAnimation();
        }

        private void calculateSpotlight() {
            int[] location = new int[2];
            targetView.getLocationOnScreen(location);

            float centerX = location[0] + targetView.getWidth() / 2f;
            float centerY = location[1] + targetView.getHeight() / 2f;

            float width = targetView.getWidth();
            float height = targetView.getHeight();
            targetRadius = (float) Math.sqrt(width * width + height * height) / 2f + 30;

            spotRect = new RectF(
                    centerX - targetRadius,
                    centerY - targetRadius,
                    centerX + targetRadius,
                    centerY + targetRadius
            );
        }

        private void animateSpotlight() {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, targetRadius);
            animator.setDuration(400);
            animator.addUpdateListener(animation -> {
                currentRadius = (float) animation.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        // ✅ تأثير النبض النظيف
        private void startPulseAnimation() {
            pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
            pulseAnimator.setDuration(2000);
            pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
            pulseAnimator.setRepeatMode(ValueAnimator.RESTART);
            pulseAnimator.addUpdateListener(animation -> {
                float fraction = (float) animation.getAnimatedValue();

                // حجم النبضة
                pulseRadius = targetRadius + (fraction * 40);

                // شفافية تتلاشى
                int alpha = (int) ((1f - fraction) * 150);
                pulsePaint.setAlpha(alpha);
                int s = (int) ((1f - fraction) * 6);
                pulsePaint.setStrokeWidth(s);

                invalidate();
            });

            // تأخير بدء النبض
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (pulseAnimator != null) {
                    pulseAnimator.start();
                }
            }, 500);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // رسم الخلفية الداكنة
            canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

            if (spotRect != null) {
                // مسح دائرة الـ spotlight
                canvas.drawCircle(
                        spotRect.centerX(),
                        spotRect.centerY(),
                        currentRadius,
                        clearPaint
                );

                // رسم دائرة النبض
                if (pulseRadius > targetRadius) {
                    canvas.drawCircle(
                            spotRect.centerX(),
                            spotRect.centerY(),
                            pulseRadius,
                            pulsePaint
                    );
                }
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (pulseAnimator != null) {
                pulseAnimator.cancel();
                pulseAnimator = null;
            }
        }
    }
}