package com.bytepulse.hagzy.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.DirectionHelper;
import com.bytepulse.hagzy.helpers.LocaleManager;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.TranslationManager;
import com.bytepulse.hagzy.layouts.auth.AuthPagesManager;
import com.bytepulse.hagzy.layouts.auth.pages.LoginPage;
import com.bytepulse.hagzy.layouts.auth.pages.SignupPage;
import com.bytepulse.hagzy.layouts.auth.pages.ForgotPasswordPage;
import com.bytepulse.hagzy.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hagzy.activities.MainActivity;

/**
 * Activity رئيسية لإدارة جميع صفحات المصادقة
 * تدمج AuthLayout داخل Activity
 */
public class AuthActivity extends AppCompatActivity {

    // Firebase & Google Sign In
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    // UI Components
    private AuthPagesManager pagesManager;
    private FrameLayout rootContainer;

    // ════════════════════════════════════════════════════════════
    // 🎬 Lifecycle Methods
    // ════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. التهيئة الأساسية
        setupInit();

        // 2. تهيئة Firebase
        setupFirebase();

        // 3. بناء الواجهة
        setupUI();
    }

    // ════════════════════════════════════════════════════════════
    // ⚙️ Setup Methods
    // ════════════════════════════════════════════════════════════

    /**
     * التهيئة الأساسية للتطبيق
     */
    private void setupInit() {
        // تعيين اللغة
        LocaleManager.setLocale(this, "ar_AR");
        LocaleManager.applyLocale(this);

        // تطبيق الثيم
        ThemeManager.setDarkMode(this, false);
        ThemeManager.init(this);

        // اتجاه النص
        DirectionHelper.applyDirection(this, LocaleManager.getSavedLanguage(this));

        // شريط الحالة والتنقل
        ThemeManager.applySystemBars(this);

        // تحميل الترجمات
        TranslationManager.load(this, LocaleManager.getSavedLanguage(this));
    }

    /**
     * تهيئة Firebase و Google Sign In
     */
    private void setupFirebase() {
        // Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Google Sign In Configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * بناء الواجهة وإضافة الصفحات
     */
    private void setupUI() {
        // إنشاء الحاوية الرئيسية
        rootContainer = new FrameLayout(this);
        rootContainer.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // إنشاء مدير الصفحات
        pagesManager = new AuthPagesManager(this, rootContainer);

        // إضافة صفحة تسجيل الدخول
        addLoginPage();

        // إضافة صفحة إنشاء حساب
        addSignupPage();

        // إضافة صفحة نسيت كلمة المرور
        addForgotPasswordPage();

        // عرض صفحة تسجيل الدخول أولاً
        pagesManager.showPage("login");

        // تعيين الواجهة
        setContentView(rootContainer);
    }

    // ════════════════════════════════════════════════════════════
    // 📄 Pages Setup
    // ════════════════════════════════════════════════════════════

    /**
     * إضافة صفحة تسجيل الدخول
     */
    private void addLoginPage() {
        LoginPage loginPage = new LoginPage(
                this,
                mAuth,
                mGoogleSignInClient,
                RC_SIGN_IN,
                this::onLoginSuccess,
                this::showSignup,
                this::showForgotPassword
        );

        pagesManager.addPage("login", loginPage);
    }

    /**
     * إضافة صفحة إنشاء حساب
     */
    private void addSignupPage() {
        SignupPage signupPage = new SignupPage(
                this,
                mAuth,
                mGoogleSignInClient,
                RC_SIGN_IN,
                this::onSignupSuccess,
                this::showLogin
        );

        pagesManager.addPage("signup", signupPage);
    }

    /**
     * إضافة صفحة نسيت كلمة المرور
     */
    private void addForgotPasswordPage() {
        ForgotPasswordPage forgotPasswordPage = new ForgotPasswordPage(
                this,
                mAuth,
                this::showLogin
        );

        pagesManager.addPage("forgot_password", forgotPasswordPage);
    }

    // ════════════════════════════════════════════════════════════
    // 📱 Navigation Callbacks
    // ════════════════════════════════════════════════════════════

    /**
     * الانتقال إلى صفحة تسجيل الدخول
     */
    private void showLogin() {
        pagesManager.navigateTo("login");
    }

    /**
     * الانتقال إلى صفحة إنشاء حساب
     */
    private void showSignup() {
        pagesManager.navigateTo("signup");
    }

    /**
     * الانتقال إلى صفحة نسيت كلمة المرور
     */
    private void showForgotPassword() {
        pagesManager.navigateTo("forgot_password");
    }

    // ════════════════════════════════════════════════════════════
    // ✅ Success Callbacks
    // ════════════════════════════════════════════════════════════

    /**
     * عند نجاح تسجيل الدخول
     */
    private void onLoginSuccess(FirebaseUser user) {
        saveUserSession(user);
        navigateToMain();
    }

    /**
     * عند نجاح إنشاء الحساب
     */
    private void onSignupSuccess(FirebaseUser user) {
        saveUserSession(user);
        navigateToMain();
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    /**
     * حفظ بيانات المستخدم في Session
     */
    private void saveUserSession(FirebaseUser user) {
        SessionManager session = new SessionManager(this);
        session.saveUser(
                user.getUid(),
                user.getDisplayName() != null ? user.getDisplayName() : "مستخدم",
                user.getEmail() != null ? user.getEmail() : "",
                user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : ""
        );
    }

    /**
     * الانتقال إلى الصفحة الرئيسية
     */
    private void navigateToMain() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);



        finish();
    }

    // ════════════════════════════════════════════════════════════
    // 🔄 Activity Results
    // ════════════════════════════════════════════════════════════

    /**
     * معالجة نتائج تسجيل الدخول بـ Google
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            pagesManager.handleGoogleSignInResult(data);
        }
    }

    // ════════════════════════════════════════════════════════════
    // ⬅️ Back Press Handling
    // ════════════════════════════════════════════════════════════

    /**
     * معالجة زر الرجوع
     */
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // إذا كان في navigation stack، ارجع للصفحة السابقة
        if (!pagesManager.handleBackPress()) {
            // إذا كان في أول صفحة، اخرج من التطبيق
            super.onBackPressed();
        }
    }
}