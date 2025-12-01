package com.bytepulse.hagzy.layouts.auth.pages;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.activities.AuthActivity;
import com.bytepulse.hagzy.layouts.auth.components.AuthButton;
import com.bytepulse.hagzy.layouts.auth.components.AuthDivider;
import com.bytepulse.hagzy.layouts.auth.components.AuthInput;
import com.bytepulse.hagzy.layouts.auth.components.AuthTitle;
import com.bytepulse.hagzy.layouts.auth.utils.AuthCardAnimator;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * صفحة تسجيل الدخول
 */
public class LoginPage implements AuthPage {

    private static final String TAG = "LoginPage";

    private final Context context;
    private final FirebaseAuth mAuth;
    private final GoogleSignInClient googleSignInClient;
    private final int RC_SIGN_IN;

    private final OnLoginSuccessListener onLoginSuccess;
    private final OnNavigateListener onNavigateToSignup;
    private final OnNavigateListener onNavigateToForgotPassword;

    private FrameLayout rootView;
    private EditText emailInput, passwordInput;
    private LinearLayout loginButton, googleButton;
    private TextView loginButtonText, errorText;
    private FrameLayout progressOverlay;
    private boolean isLoading = false;

    public interface OnLoginSuccessListener {
        void onLoginSuccess(FirebaseUser user);
    }

    public interface OnNavigateListener {
        void onNavigate();
    }

    public LoginPage(Context context,
                     FirebaseAuth mAuth,
                     GoogleSignInClient googleSignInClient,
                     int RC_SIGN_IN,
                     OnLoginSuccessListener onLoginSuccess,
                     OnNavigateListener onNavigateToSignup,
                     OnNavigateListener onNavigateToForgotPassword) {
        this.context = context;
        this.mAuth = mAuth;
        this.googleSignInClient = googleSignInClient;
        this.RC_SIGN_IN = RC_SIGN_IN;
        this.onLoginSuccess = onLoginSuccess;
        this.onNavigateToSignup = onNavigateToSignup;
        this.onNavigateToForgotPassword = onNavigateToForgotPassword;

        buildUI();
    }

    @Override
    public View getView() {
        return rootView;
    }

    private void buildUI() {
        rootView = new FrameLayout(context);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rootView.setBackgroundColor(Color.WHITE);

        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        scrollView.setFillViewport(true);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(60), dp(24), dp(40));

        // العنوان
        AuthTitle title = new AuthTitle(context, "مرحباً بك", "سجل دخولك للمتابعة");
        container.addView(title.getView());

        // رسالة الخطأ
        errorText = AuthInput.createErrorText(context);
        container.addView(errorText);

        // زر Google
        googleButton = AuthButton.createSocialButton(
                context,
                R.drawable.google,
                "المتابعة مع Google"
        );
        AuthCardAnimator.applyAnimation(googleButton, this::signInWithGoogle);
        container.addView(googleButton);

        // الفاصل
        container.addView(new AuthDivider(context).getView());

        // حقل البريد الإلكتروني
        container.addView(AuthInput.createLabel(context, "البريد الإلكتروني"));
        emailInput = AuthInput.createEmailInput(context);
        container.addView(emailInput);

        // حقل كلمة المرور
        container.addView(AuthInput.createLabel(context, "كلمة المرور"));
        passwordInput = AuthInput.createPasswordInput(context);
        container.addView(passwordInput);

        // نسيت كلمة المرور
        TextView forgotPassword = AuthInput.createLinkText(context, "نسيت كلمة المرور؟");
        forgotPassword.setGravity(Gravity.END);
        LinearLayout.LayoutParams forgotParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        forgotParams.topMargin = dp(8);
        forgotParams.bottomMargin = dp(24);
        forgotPassword.setLayoutParams(forgotParams);
        AuthCardAnimator.applyLightAnimation(forgotPassword, () -> {
            if (onNavigateToForgotPassword != null) {
                onNavigateToForgotPassword.onNavigate();
            }
        });
        container.addView(forgotPassword);

        // زر تسجيل الدخول
        loginButton = AuthButton.createPrimaryButton(context, "تسجيل الدخول");
        loginButtonText = (TextView) loginButton.getChildAt(0);
        AuthCardAnimator.applyAnimation(loginButton, this::handleLogin);
        container.addView(loginButton);

        // رابط إنشاء حساب
        LinearLayout signupContainer = AuthInput.createLinkContainer(
                context,
                "ليس لديك حساب؟ ",
                "إنشاء حساب",
                () -> {
                    if (onNavigateToSignup != null) {
                        onNavigateToSignup.onNavigate();
                    }
                }
        );
        container.addView(signupContainer);

        scrollView.addView(container);
        rootView.addView(scrollView);

        // Progress Overlay
        progressOverlay = createProgressOverlay();
        rootView.addView(progressOverlay);

        // Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            scrollView.setPadding(0, top, 0, bottom);
            return insets;
        });
    }

    // ════════════════════════════════════════════════════════════
    // 🔐 Authentication Methods
    // ════════════════════════════════════════════════════════════

    private void handleLogin() {
        if (isLoading) return;

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("يرجى ملء جميع الحقول");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("البريد الإلكتروني غير صحيح");
            return;
        }

        hideError();
        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (onLoginSuccess != null && user != null) {
                            onLoginSuccess.onLoginSuccess(user);
                        }
                    } else {
                        showError("البريد الإلكتروني أو كلمة المرور غير صحيحة");
                    }
                });
    }

    /**
     * تسجيل الدخول بـ Google - FIXED
     */
    private void signInWithGoogle() {
        if (isLoading) return;

        Log.d(TAG, "Starting Google Sign In...");

        try {
            // تأكد من أن Context هو AuthActivity
            if (!(context instanceof AuthActivity)) {
                Log.e(TAG, "Context is not AuthActivity!");
                showError("خطأ في تهيئة Google Sign In");
                return;
            }

            // استخدام GoogleSignInClient الممرر من AuthActivity
            // (الذي يحتوي على requestIdToken الصحيح)
            Intent signInIntent = googleSignInClient.getSignInIntent();

            // بدء Activity للحصول على النتيجة
            ((AuthActivity) context).startActivityForResult(signInIntent, RC_SIGN_IN);

            Log.d(TAG, "Google Sign In Intent started successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error starting Google Sign In", e);
            showError("فشل في بدء تسجيل الدخول مع Google");
        }
    }

    /**
     * معالجة نتيجة تسجيل الدخول بـ Google الناجحة
     */
    @Override
    public void onGoogleSignInResult(GoogleSignInAccount account) {
        Log.d(TAG, "Google Sign In Result received for: " + account.getEmail());

        setLoading(true);

        // الحصول على ID Token
        String idToken = account.getIdToken();

        if (idToken == null) {
            Log.e(TAG, "ID Token is null!");
            setLoading(false);
            showError("فشل الحصول على معلومات Google");
            return;
        }

        // إنشاء Firebase Credential
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        // تسجيل الدخول في Firebase
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase authentication successful");
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (onLoginSuccess != null && user != null) {
                            onLoginSuccess.onLoginSuccess(user);
                        }
                    } else {
                        Log.e(TAG, "Firebase authentication failed", task.getException());
                        showError("فشل المصادقة مع Firebase");
                    }
                });
    }

    /**
     * معالجة فشل تسجيل الدخول بـ Google
     */
    @Override
    public void onGoogleSignInError() {
        Log.e(TAG, "Google Sign In Error");
        showError("فشل تسجيل الدخول مع Google");
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    private FrameLayout createProgressOverlay() {
        FrameLayout overlay = new FrameLayout(context);
        overlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlay.setBackgroundColor(Color.parseColor("#80000000"));
        overlay.setVisibility(View.GONE);
        overlay.setClickable(true);

        ProgressBar progressBar = new ProgressBar(context);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                dp(50), dp(50), Gravity.CENTER
        );
        progressBar.setLayoutParams(progressParams);
        progressBar.getIndeterminateDrawable().setColorFilter(
                Color.WHITE,
                android.graphics.PorterDuff.Mode.SRC_IN
        );
        overlay.addView(progressBar);

        return overlay;
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);

        ObjectAnimator shake = ObjectAnimator.ofFloat(
                errorText,
                "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0
        );
        shake.setDuration(500);
        shake.start();
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        if (loading) {
            loginButtonText.setText("");
            progressOverlay.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            googleButton.setEnabled(false);
        } else {
            loginButtonText.setText("تسجيل الدخول");
            progressOverlay.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            googleButton.setEnabled(true);
        }
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}