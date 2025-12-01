package com.bytepulse.hagzy.layouts.auth.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * يوفر أنيميشن تفاعلية للـ Views، مشابهة لسلوك الضغط والإفلات
 * الناعم الموجود في واجهات الجوال الحديثة.
 * تضيف هذه الكلاس تكبير وتصغير ناعم عند الضغط أو الإفلات.
 */
public class AuthCardAnimator {

    private static final float SCALE_PRESSED = 0.96f;
    private static final float ALPHA_PRESSED = 0.7f;
    private static final int ANIMATION_DURATION = 150;

    private static final float SCALE_LIGHT_PRESSED = 0.98f;
    private static final float ALPHA_LIGHT_PRESSED = 0.85f;
    private static final int ANIMATION_LIGHT_DURATION = 100;

    /**
     * يضيف أنيميشن ضغط للـ View المحدد. عند الضغط، يتقلص الـ View قليلاً ويتلاشى؛
     * عند الإفلات، يعود بسلاسة إلى حجمه الأصلي وشفافيته.
     * يتم تنفيذ callback اختياري بعد اكتمال أنيميشن الإفلات.
     *
     * @param view    الـ View المراد تحريكه
     * @param onClick callback يُنفذ بعد أنيميشن الإفلات (اختياري)
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
     * أنيميشن خفيف للأزرار الصغيرة والروابط
     */
    public static void applyLightAnimation(View view, Runnable onClick) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animateLightPress(v);
                    return true;

                case MotionEvent.ACTION_UP:
                    animateLightRelease(v);
                    v.performClick();
                    if (onClick != null) {
                        v.postDelayed(onClick, ANIMATION_LIGHT_DURATION / 2);
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    animateLightRelease(v);
                    return true;
            }
            return false;
        });
    }

    /**
     * يطبق أنيميشن "الضغط": الـ View يتقلص بسلاسة ويقل وضوحه،
     * مما يعطي تأثير ردة فعل لمسية ناعمة.
     *
     * @param view الـ View المراد تحريكه
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
     * يعيد الـ View إلى حجمه وشفافيته الافتراضية عند انتهاء التفاعل اللمسي.
     * يعمل هذا الأنيميشن عندما يرفع المستخدم إصبعه.
     *
     * @param view الـ View المراد تحريكه
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

    /**
     * أنيميشن ضغط خفيف للأزرار الصغيرة
     */
    private static void animateLightPress(View view) {
        view.animate()
                .scaleX(SCALE_LIGHT_PRESSED)
                .scaleY(SCALE_LIGHT_PRESSED)
                .alpha(ALPHA_LIGHT_PRESSED)
                .setDuration(ANIMATION_LIGHT_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * أنيميشن إفلات خفيف للأزرار الصغيرة
     */
    private static void animateLightRelease(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(ANIMATION_LIGHT_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}