package com.bytepulse.hagzy.layouts.auth;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ForgotPasswordFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText emailInput;
    private LinearLayout resetButton;
    private TextView resetButtonText, errorText, successText;
    private ProgressBar progressBar;
    private FrameLayout progressOverlay;
    private boolean isLoading = false;
    private boolean emailSent = false;

    public interface OnForgotPasswordListener {
        void onBackToLogin();
    }

    private OnForgotPasswordListener listener;

    public void setOnForgotPasswordListener(OnForgotPasswordListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        return buildUI();
    }

    private View buildUI() {
        FrameLayout root = new FrameLayout(requireContext());
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(Color.WHITE);

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        scrollView.setFillViewport(true);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(40), dp(24), dp(40));
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // ────────── زر الرجوع ──────────
        LinearLayout backButton = new LinearLayout(requireContext());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        backParams.bottomMargin = dp(24);
        backButton.setLayoutParams(backParams);
        backButton.setGravity(Gravity.CENTER_VERTICAL);
        backButton.setOnClickListener(v -> {
            if (listener != null) listener.onBackToLogin();
        });

        TextView backArrow = createText("← ", 24, "#000000", true);
        TextView backText = createText("رجوع", 16, "#000000", false);
        backButton.addView(backArrow);
        backButton.addView(backText);
        container.addView(backButton);

        // ────────── الأيقونة ──────────
        FrameLayout iconContainer = new FrameLayout(requireContext());
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(
                dp(80), dp(80)
        );
        iconContainerParams.gravity = Gravity.CENTER_HORIZONTAL;
        iconContainerParams.bottomMargin = dp(24);
        iconContainer.setLayoutParams(iconContainerParams);

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        iconBg.setColor(Color.parseColor("#F5F5F5"));
        iconContainer.setBackground(iconBg);

        ImageView icon = new ImageView(requireContext());
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                dp(40), dp(40), Gravity.CENTER
        );
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.google); // استبدل بأيقونة القفل أو البريد
        icon.setColorFilter(Color.parseColor("#000000"));
        iconContainer.addView(icon);
        container.addView(iconContainer);

        // ────────── العنوان ──────────
        TextView title = createText("نسيت كلمة المرور؟", 32, "#000000", true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        container.addView(title);

        TextView subtitle = createText("لا تقلق! أدخل بريدك الإلكتروني وسنرسل لك رابط لإعادة تعيين كلمة المرور", 16, "#666666", false);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.bottomMargin = dp(40);
        subtitle.setLayoutParams(subtitleParams);
        container.addView(subtitle);

        // ────────── رسالة الخطأ ──────────
        errorText = createText("", 14, "#E53935", false);
        errorText.setGravity(Gravity.CENTER);
        errorText.setVisibility(View.GONE);
        LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        errorParams.bottomMargin = dp(16);
        errorText.setLayoutParams(errorParams);
        container.addView(errorText);

        // ────────── رسالة النجاح ──────────
        successText = createText("", 14, "#4CAF50", false);
        successText.setGravity(Gravity.CENTER);
        successText.setVisibility(View.GONE);
        LinearLayout.LayoutParams successParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        successParams.bottomMargin = dp(16);
        successText.setLayoutParams(successParams);
        container.addView(successText);

        // ────────── حقل البريد الإلكتروني ──────────
        container.addView(createLabel("البريد الإلكتروني"));
        emailInput = createInput("name@example.com", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        container.addView(emailInput);

        // ────────── زر إرسال الرابط ──────────
        resetButton = createPrimaryButton("إرسال رابط إعادة التعيين", "#000000");
        resetButton.setOnClickListener(v -> handleResetPassword());
        container.addView(resetButton);

        // ────────── معلومات إضافية ──────────
        LinearLayout infoContainer = new LinearLayout(requireContext());
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        infoContainer.setGravity(Gravity.CENTER);
        infoContainer.setPadding(dp(16), dp(24), dp(16), dp(16));
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        infoContainer.setLayoutParams(infoParams);

        GradientDrawable infoBg = new GradientDrawable();
        infoBg.setColor(Color.parseColor("#F5F5F5"));
        infoBg.setCornerRadius(dp(12));
        infoContainer.setBackground(infoBg);

        TextView infoTitle = createText("💡 نصيحة", 14, "#000000", true);
        infoTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams infoTitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        infoTitleParams.bottomMargin = dp(8);
        infoTitle.setLayoutParams(infoTitleParams);
        infoContainer.addView(infoTitle);

        TextView infoText = createText("تأكد من التحقق من مجلد الرسائل غير المرغوب فيها (Spam) إذا لم تجد البريد في صندوق الوارد", 12, "#666666", false);
        infoText.setGravity(Gravity.CENTER);
        infoContainer.addView(infoText);

        container.addView(infoContainer);

        scrollView.addView(container);
        root.addView(scrollView);

        // ────────── Progress Overlay ──────────
        progressOverlay = new FrameLayout(requireContext());
        progressOverlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        progressOverlay.setBackgroundColor(Color.parseColor("#80000000"));
        progressOverlay.setVisibility(View.GONE);
        progressOverlay.setClickable(true);

        progressBar = new ProgressBar(requireContext());
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                dp(50), dp(50), Gravity.CENTER
        );
        progressBar.setLayoutParams(progressParams);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressOverlay.addView(progressBar);

        root.addView(progressOverlay);

        return root;
    }

    // ════════════════════════════════════════════════════════════
    // 🔐 Authentication Methods
    // ════════════════════════════════════════════════════════════

    private void handleResetPassword() {
        if (isLoading) return;

        String email = emailInput.getText().toString().trim();

        // Validation
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

        // ✅ استخدام Firebase لإرسال رابط إعادة التعيين
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        emailSent = true;
                        showSuccess("تم إرسال رابط إعادة التعيين إلى " + email);

                        // تعطيل الحقل والزر
                        emailInput.setEnabled(false);
                        resetButton.setAlpha(0.5f);
                        resetButton.setEnabled(false);

                        // الرجوع للتسجيل بعد 3 ثواني
                        emailInput.postDelayed(() -> {
                            if (listener != null) listener.onBackToLogin();
                        }, 3000);
                    } else {
                        String errorMessage = "فشل إرسال البريد الإلكتروني";
                        if (task.getException() != null) {
                            String exception = task.getException().getMessage();
                            if (exception.contains("no user record")) {
                                errorMessage = "البريد الإلكتروني غير مسجل";
                            }
                        }
                        showError(errorMessage);
                    }
                });

        // 📡 أو استخدام API الخاص بك
        // resetPasswordWithAPI(email);
    }

    // ════════════════════════════════════════════════════════════
    // 📡 API Integration (مثال)
    // ════════════════════════════════════════════════════════════

    private void resetPasswordWithAPI(String email) {
        new Thread(() -> {
            try {
                URL url = new URL("https://your-api.com/api/auth/forgot-password");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    requireActivity().runOnUiThread(() -> {
                        setLoading(false);
                        emailSent = true;
                        showSuccess("تم إرسال رابط إعادة التعيين إلى " + email);

                        emailInput.setEnabled(false);
                        resetButton.setAlpha(0.5f);
                        resetButton.setEnabled(false);

                        emailInput.postDelayed(() -> {
                            if (listener != null) listener.onBackToLogin();
                        }, 3000);
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        setLoading(false);
                        showError("البريد الإلكتروني غير مسجل");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    showError("حدث خطأ في الاتصال");
                });
            }
        }).start();
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    private TextView createText(String text, int size, String color, boolean bold) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        tv.setTextColor(Color.parseColor(color));
        if (bold) {
            tv.setTypeface(ThemeManager.fontBold());
        } else {
            tv.setTypeface(ThemeManager.fontRegular());
        }
        return tv;
    }

    private TextView createLabel(String text) {
        TextView label = createText(text, 14, "#000000", true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(8);
        label.setLayoutParams(params);
        return label;
    }

    private EditText createInput(String hint, int inputType) {
        EditText input = new EditText(requireContext());
        input.setHint(hint);
        input.setInputType(inputType);
        input.setTextSize(16);
        input.setTextColor(Color.parseColor("#000000"));
        input.setHintTextColor(Color.parseColor("#999999"));
        input.setPadding(dp(16), dp(16), dp(16), dp(16));
        input.setTypeface(ThemeManager.fontRegular());

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(12));
        input.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        params.bottomMargin = dp(16);
        input.setLayoutParams(params);

        return input;
    }

    private LinearLayout createPrimaryButton(String text, String bgColor) {
        LinearLayout button = new LinearLayout(requireContext());
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(dp(12));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        params.bottomMargin = dp(24);
        button.setLayoutParams(params);

        resetButtonText = createText(text, 16, "#FFFFFF", true);
        button.addView(resetButtonText);

        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(emailSent ? 0.5f : 1f);
            }
            return false;
        });

        return button;
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);

        ObjectAnimator shake = ObjectAnimator.ofFloat(errorText, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(500);
        shake.start();
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }

    private void showSuccess(String message) {
        successText.setText("✓ " + message);
        successText.setVisibility(View.VISIBLE);
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
            resetButtonText.setText("إرسال رابط إعادة التعيين");
            progressOverlay.setVisibility(View.GONE);
            if (!emailSent) {
                resetButton.setEnabled(true);
            }
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}