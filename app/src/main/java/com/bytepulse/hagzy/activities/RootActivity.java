package com.bytepulse.hagzy.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.bytepulse.hagzy.activities.AuthActivity;
import com.bytepulse.hagzy.BuildConfig;
import com.bytepulse.hagzy.helpers.DirectionHelper;
import com.bytepulse.hagzy.helpers.LocaleManager;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.TranslationManager;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hagzy.activities.MainActivity;

public class RootActivity extends AppCompatActivity {

    private static final String TAG = "RootActivity";
    private static final long NAVIGATION_DELAY = 0; // بدون تأخير

    private FirebaseAuth mAuth;

    // ════════════════════════════════════════════════════════════
    // 🎬 Lifecycle
    // ════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 2. تهيئة Firebase
        initializeFirebase();

        // 3. التحقق من المستخدم والتوجيه
        loadDataAsync(this::checkUserAndNavigate);

    }

    private void loadDataAsync(Runnable onComplete) {
        new Thread(() -> {
            runOnUiThread(onComplete);
        }).start();
    }

    private void initializeFirebase() {
        // تهيئة Firebase
        FirebaseApp.initializeApp(this);

        // الحصول على مثيل Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    // ════════════════════════════════════════════════════════════
    // 🚀 Navigation Logic
    // ════════════════════════════════════════════════════════════

    private void checkUserAndNavigate() {
        // الحصول على المستخدم الحالي
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // التوجيه بناءً على حالة تسجيل الدخول
        if (currentUser != null) {
            // المستخدم مسجل دخول → الذهاب للصفحة الرئيسية
            navigateToMain();
        } else {
            // المستخدم غير مسجل دخول → الذهاب لشاشة المصادقة
            navigateToAuth();
        }
    }

    private void navigateToMain() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
        }, NAVIGATION_DELAY);
    }

    private void navigateToAuth() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
        }, NAVIGATION_DELAY);
    }

    // ════════════════════════════════════════════════════════════
    // ⬅️ Back Press (منع الرجوع)
    // ════════════════════════════════════════════════════════════

    @SuppressLint({"MissingSuperCall", "GestureBackNavigation"})
    @Override
    public void onBackPressed() {}
}