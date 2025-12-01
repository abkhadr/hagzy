package com.bytepulse.hagzy.layouts.auth.pages;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
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
import com.bytepulse.hagzy.layouts.auth.components.AuthButton;
import com.bytepulse.hagzy.layouts.auth.components.AuthDivider;
import com.bytepulse.hagzy.layouts.auth.components.AuthInput;
import com.bytepulse.hagzy.layouts.auth.components.AuthTitle;
import com.bytepulse.hagzy.layouts.auth.utils.AuthCardAnimator;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * صفحة إنشاء حساب جديد
 */
public class SignupPage implements AuthPage {

    private final Context context;
    private final FirebaseAuth mAuth;
    private final GoogleSignInClient googleSignInClient;
    private final int RC_SIGN_IN;

    private final OnSignupSuccessListener onSignupSuccess;
    private final OnNavigateListener onNavigateToLogin;

    private FrameLayout rootView;
    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private LinearLayout signupButton;
    private TextView signupButtonText, errorText;
    private FrameLayout progressOverlay;
    private boolean isLoading = false;

    public interface OnSignupSuccessListener {
        void onSignupSuccess(FirebaseUser user);
    }

    public interface OnNavigateListener {
        void onNavigate();
    }

    public SignupPage(Context context,
                      FirebaseAuth mAuth,
                      GoogleSignInClient googleSignInClient,
                      int RC_SIGN_IN,
                      OnSignupSuccessListener onSignupSuccess,
                      OnNavigateListener onNavigateToLogin) {
        this.context = context;
        this.mAuth = mAuth;
        this.googleSignInClient = googleSignInClient;
        this.RC_SIGN_IN = RC_SIGN_IN;
        this.onSignupSuccess = onSignupSuccess;
        this.onNavigateToLogin = onNavigateToLogin;

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
        AuthTitle title = new AuthTitle(context, "إنشاء حساب", "انضم إلينا الآن");
        container.addView(title.getView());

        // رسالة الخطأ
        errorText = AuthInput.createErrorText(context);
        container.addView(errorText);

        // حقل الاسم
        container.addView(AuthInput.createLabel(context, "الاسم الكامل"));
        nameInput = AuthInput.createInput(context, "أدخل اسمك", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        container.addView(nameInput);

        // حقل البريد الإلكتروني
        container.addView(AuthInput.createLabel(context, "البريد الإلكتروني"));
        emailInput = AuthInput.createEmailInput(context);
        container.addView(emailInput);

        // حقل كلمة المرور
        container.addView(AuthInput.createLabel(context, "كلمة المرور"));
        passwordInput = AuthInput.createPasswordInput(context);
        container.addView(passwordInput);

        // حقل تأكيد كلمة المرور
        container.addView(AuthInput.createLabel(context, "تأكيد كلمة المرور"));
        confirmPasswordInput = AuthInput.createPasswordInput(context);
        LinearLayout.LayoutParams confirmParams = (LinearLayout.LayoutParams) confirmPasswordInput.getLayoutParams();
        confirmParams.bottomMargin = dp(24);
        confirmPasswordInput.setLayoutParams(confirmParams);
        container.addView(confirmPasswordInput);

        // زر إنشاء الحساب
        signupButton = AuthButton.createPrimaryButton(context, "إنشاء حساب");
        signupButtonText = (TextView) signupButton.getChildAt(0);
        AuthCardAnimator.applyAnimation(signupButton, this::handleSignup);
        container.addView(signupButton);

        // رابط تسجيل الدخول
        LinearLayout loginContainer = AuthInput.createLinkContainer(
                context,
                "لديك حساب بالفعل؟ ",
                "تسجيل الدخول",
                () -> {
                    if (onNavigateToLogin != null) {
                        onNavigateToLogin.onNavigate();
                    }
                }
        );
        container.addView(loginContainer);

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

    private void handleSignup() {
        if (isLoading) return;

        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // التحقق من الحقول
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("يرجى ملء جميع الحقول");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("البريد الإلكتروني غير صحيح");
            return;
        }

        if (password.length() < 6) {
            showError("كلمة المرور يجب أن تكون 6 أحرف على الأقل");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("كلمة المرور غير متطابقة");
            return;
        }

        hideError();
        setLoading(true);

        // إنشاء حساب جديد
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // تحديث الاسم
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        setLoading(false);
                                        if (onSignupSuccess != null) {
                                            onSignupSuccess.onSignupSuccess(user);
                                        }
                                    });
                        } else {
                            setLoading(false);
                        }
                    } else {
                        setLoading(false);
                        String errorMessage = "فشل إنشاء الحساب";

                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error != null && error.contains("email address is already in use")) {
                                errorMessage = "البريد الإلكتروني مستخدم بالفعل";
                            }
                        }

                        showError(errorMessage);
                    }
                });
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
            signupButtonText.setText("");
            progressOverlay.setVisibility(View.VISIBLE);
            signupButton.setEnabled(false);
        } else {
            signupButtonText.setText("إنشاء حساب");
            progressOverlay.setVisibility(View.GONE);
            signupButton.setEnabled(true);
        }
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}