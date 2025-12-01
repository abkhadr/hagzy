package com.bytepulse.hagzy.layouts.auth.pages;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
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

import com.bytepulse.hagzy.layouts.auth.components.AuthButton;
import com.bytepulse.hagzy.layouts.auth.components.AuthInput;
import com.bytepulse.hagzy.layouts.auth.components.AuthTitle;
import com.bytepulse.hagzy.layouts.auth.utils.AuthCardAnimator;
import com.google.firebase.auth.FirebaseAuth;

/**
 * صفحة استرجاع كلمة المرور
 */
public class ForgotPasswordPage implements AuthPage {

    private final Context context;
    private final FirebaseAuth mAuth;
    private final OnNavigateListener onNavigateToLogin;

    private FrameLayout rootView;
    private EditText emailInput;
    private LinearLayout resetButton;
    private TextView resetButtonText, errorText, successText;
    private FrameLayout progressOverlay;
    private boolean isLoading = false;

    public interface OnNavigateListener {
        void onNavigate();
    }

    public ForgotPasswordPage(Context context,
                              FirebaseAuth mAuth,
                              OnNavigateListener onNavigateToLogin) {
        this.context = context;
        this.mAuth = mAuth;
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
        container.setPadding(dp(24), dp(80), dp(24), dp(40));

        // العنوان
        AuthTitle title = new AuthTitle(context, "نسيت كلمة المرور؟", "أدخل بريدك الإلكتروني لاسترجاع حسابك");
        container.addView(title.getView());

        // رسالة الخطأ
        errorText = AuthInput.createErrorText(context);
        container.addView(errorText);

        // رسالة النجاح
        successText = createSuccessText();
        container.addView(successText);

        // حقل البريد الإلكتروني
        container.addView(AuthInput.createLabel(context, "البريد الإلكتروني"));
        emailInput = AuthInput.createEmailInput(context);
        LinearLayout.LayoutParams emailParams = (LinearLayout.LayoutParams) emailInput.getLayoutParams();
        emailParams.bottomMargin = dp(24);
        emailInput.setLayoutParams(emailParams);
        container.addView(emailInput);

        // زر إرسال رابط الاسترجاع
        resetButton = AuthButton.createPrimaryButton(context, "إرسال رابط الاسترجاع");
        resetButtonText = (TextView) resetButton.getChildAt(0);
        AuthCardAnimator.applyAnimation(resetButton, this::handlePasswordReset);
        container.addView(resetButton);

        // رابط الرجوع لتسجيل الدخول
        LinearLayout loginContainer = AuthInput.createLinkContainer(
                context,
                "تذكرت كلمة المرور؟ ",
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
    // 🔐 Password Reset Method
    // ════════════════════════════════════════════════════════════

    private void handlePasswordReset() {
        if (isLoading) return;

        String email = emailInput.getText().toString().trim();

        // التحقق من البريد الإلكتروني
        if (email.isEmpty()) {
            showError("يرجى إدخال البريد الإلكتروني");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("البريد الإلكتروني غير صحيح");
            return;
        }

        hideError();
        hideSuccess();
        setLoading(true);

        // إرسال رابط استرجاع كلمة المرور
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        showSuccess("تم إرسال رابط استرجاع كلمة المرور إلى بريدك الإلكتروني");
                        emailInput.setText("");
                    } else {
                        String errorMessage = "فشل إرسال الرابط";

                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error != null && error.contains("no user record")) {
                                errorMessage = "البريد الإلكتروني غير مسجل";
                            }
                        }

                        showError(errorMessage);
                    }
                });
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    private TextView createSuccessText() {
        TextView successText = new TextView(context);
        successText.setTextSize(14);
        successText.setTextColor(Color.parseColor("#4CAF50"));
        successText.setGravity(Gravity.CENTER);
        successText.setVisibility(View.GONE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(16);
        successText.setLayoutParams(params);
        return successText;
    }

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
        hideSuccess();
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

    private void showSuccess(String message) {
        hideError();
        successText.setText(message);
        successText.setVisibility(View.VISIBLE);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(successText, "alpha", 0f, 1f);
        fadeIn.setDuration(300);
        fadeIn.start();
    }

    private void hideSuccess() {
        successText.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        if (loading) {
            resetButtonText.setText("");
            progressOverlay.setVisibility(View.VISIBLE);
            resetButton.setEnabled(false);
        } else {
            resetButtonText.setText("إرسال رابط الاسترجاع");
            progressOverlay.setVisibility(View.GONE);
            resetButton.setEnabled(true);
        }
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}