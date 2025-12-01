package com.bytepulse.hagzy.activities;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.bytepulse.hagzy.BuildConfig;
import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.DirectionHelper;
import com.bytepulse.hagzy.helpers.LocaleManager;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.TranslationManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hagzy.activities.MainActivity;

/**
 * شاشة البداية مع أنيميشن Lottie
 * محسّنة للأداء والسرعة
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    // Cache للـ Composition لتسريع التشغيل
    private static LottieComposition cachedComposition;

    private LottieAnimationView lottieView;
    private boolean isAnimationComplete = false;
    private boolean isNavigating = false;

    // ════════════════════════════════════════════════════════════
    // 🎬 Lifecycle
    // ════════════════════════════════════════════════════════════


    private void initApp() {
        // لغة التطبيق
        LocaleManager.applyLocale(this);
        ThemeManager.setDarkMode(this, false);
        ThemeManager.init(this);

        // اتجاه النص
        DirectionHelper.applyDirection(this, LocaleManager.getSavedLanguage(this));

        // ألوان شريط الحالة
        ThemeManager.applySystemBars(this);

        // تحميل الترجمات
        TranslationManager.init(this);
        TranslationManager.load(this, LocaleManager.getSavedLanguage(this));
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
        FirebaseAuth.getInstance();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        setupFullScreen();

        setContentView(R.layout.activity_splash);

        lottieView = findViewById(R.id.lottieLogo);
        loadAndPlayAnimation();
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 Setup Methods
    // ════════════════════════════════════════════════════════════

    /**
     * إعداد وضع الشاشة الكاملة
     */
    private void setupFullScreen() {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    // ════════════════════════════════════════════════════════════
    // ⚙️ Initialization Methods
    // ════════════════════════════════════════════════════════════

    private void initializeApp() {
        // تطبيق اللغة
        LocaleManager.applyLocale(this);

        // تطبيق الثيم
        ThemeManager.setDarkMode(this, false);
        ThemeManager.init(this);

        // تطبيق اتجاه النص
        DirectionHelper.applyDirection(this, LocaleManager.getSavedLanguage(this));

        // تطبيق ألوان شريط الحالة
        ThemeManager.applySystemBars(this);

        // تحميل الترجمات
        TranslationManager.init(this);
        TranslationManager.load(this, LocaleManager.getSavedLanguage(this));
    }

    /**
     * تحميل وتشغيل أنيميشن Lottie
     */
    private void loadAndPlayAnimation() {
        // إذا كان موجود في الـ Cache، استخدمه مباشرة
        if (cachedComposition != null) {
            playAnimation(cachedComposition);
            return;
        }

        // تحديد ملف الأنيميشن حسب نوع التطبيق
        String animationFile = BuildConfig.APP_MODE.equals("BUSINESS")
                ? "hagzyBusiness.json"
                : "hagzy.json";

        // تحميل الأنيميشن
        LottieCompositionFactory.fromAsset(this, animationFile)
                .addListener(this::onCompositionLoaded)
                .addFailureListener(this::onCompositionFailed);
    }

    // ════════════════════════════════════════════════════════════
    // 🎭 Animation Callbacks
    // ════════════════════════════════════════════════════════════

    /**
     * عند نجاح تحميل الأنيميشن
     */
    private void onCompositionLoaded(LottieComposition composition) {
        cachedComposition = composition;
        playAnimation(composition);
    }

    /**
     * عند فشل تحميل الأنيميشن
     */
    private void onCompositionFailed(Throwable error) {
        Log.e(TAG, "Failed to load Lottie animation", error);
        // الانتقال مباشرة عند الفشل
        navigateToNext();
    }

    /**
     * تشغيل الأنيميشن
     */
    private void playAnimation(LottieComposition composition) {
        if (lottieView == null) return;

        lottieView.setComposition(composition);

        // تحسين الأداء
        lottieView.enableMergePathsForKitKatAndAbove(true);
        lottieView.setFailureListener(error ->
                Log.e(TAG, "Lottie playback error", error)
        );

        // مراقبة نهاية الأنيميشن
        lottieView.addAnimatorListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimationComplete = true;
                navigateToNext();
            }
        });

        // تشغيل الأنيميشن
        lottieView.playAnimation();
    }

    // ════════════════════════════════════════════════════════════
    // 🚀 Navigation
    // ════════════════════════════════════════════════════════════

    /**
     * الانتقال إلى الشاشة التالية
     */
    private void navigateToNext() {
        if (isNavigating) return;
        isNavigating = true;
        initApp();
        initFirebase();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = user != null
                ? new Intent(this, MainActivity.class)
                : new Intent(this, AuthActivity.class);

        startActivity(intent);
        finish();
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Classes
    // ════════════════════════════════════════════════════════════

    /**
     * Animator Listener مبسّط
     */
    private static abstract class SimpleAnimatorListener implements Animator.AnimatorListener {
        @Override public void onAnimationStart(Animator animation) {}
        @Override public void onAnimationRepeat(Animator animation) {}
        @Override public void onAnimationCancel(Animator animation) {}
    }

    // ════════════════════════════════════════════════════════════
    // ⬅️ Back Press (منع الرجوع)
    // ════════════════════════════════════════════════════════════

    @SuppressLint({"GestureBackNavigation", "MissingSuperCall"})
    @Override
    public void onBackPressed() {}
}