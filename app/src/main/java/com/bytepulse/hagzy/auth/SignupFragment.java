package com.bytepulse.hagzy.auth;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bytepulse.hagzy.helpers.ThemeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SignupFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private LinearLayout signupButton;
    private TextView signupButtonText, errorText;
    private ProgressBar progressBar;
    private FrameLayout progressOverlay;
    private boolean isLoading = false;

    public interface OnSignupListener {
        void onSignupSuccess();
        void onBackToLogin();
    }

    private OnSignupListener listener;

    public void setOnSignupListener(OnSignupListener listener) {
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

        // ────────── العنوان ──────────
        TextView title = createText("إنشاء حساب جديد", 32, "#000000", true);
        title.setGravity(Gravity.START);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        container.addView(title);

        TextView subtitle = createText("أدخل بياناتك لإنشاء حساب جديد", 16, "#666666", false);
        subtitle.setGravity(Gravity.START);
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

        // ────────── حقل الاسم ──────────
        container.addView(createLabel("الاسم الكامل"));
        nameInput = createInput("أدخل اسمك الكامل", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        container.addView(nameInput);

        // ────────── حقل البريد الإلكتروني ──────────
        container.addView(createLabel("البريد الإلكتروني"));
        emailInput = createInput("name@example.com", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        container.addView(emailInput);

        // ────────── حقل كلمة المرور ──────────
        container.addView(createLabel("كلمة المرور"));
        passwordInput = createInput("••••••••", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(passwordInput);

        // ────────── تأكيد كلمة المرور ──────────
        container.addView(createLabel("تأكيد كلمة المرور"));
        confirmPasswordInput = createInput("••••••••", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(confirmPasswordInput);

        // ────────── شروط الاستخدام ──────────
        LinearLayout termsContainer = new LinearLayout(requireContext());
        termsContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams termsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        termsParams.topMargin = dp(8);
        termsParams.bottomMargin = dp(24);
        termsContainer.setLayoutParams(termsParams);

        TextView termsText1 = createText("بالمتابعة، أنت توافق على ", 12, "#666666", false);
        TextView termsLink = createText("الشروط والأحكام", 12, "#1976D2", true);
        termsLink.setOnClickListener(v -> {
            // TODO: فتح صفحة الشروط والأحكام
        });

        termsContainer.addView(termsText1);
        termsContainer.addView(termsLink);
        container.addView(termsContainer);

        // ────────── زر إنشاء الحساب ──────────
        signupButton = createPrimaryButton("إنشاء حساب", "#000000");
        signupButton.setOnClickListener(v -> handleSignup());
        container.addView(signupButton);

        // ────────── لديك حساب؟ ──────────
        LinearLayout loginContainer = new LinearLayout(requireContext());
        loginContainer.setOrientation(LinearLayout.HORIZONTAL);
        loginContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams loginParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        loginParams.topMargin = dp(24);
        loginContainer.setLayoutParams(loginParams);

        TextView loginText = createText("لديك حساب بالفعل؟ ", 14, "#666666", false);
        TextView loginLink = createText("تسجيل الدخول", 14, "#1976D2", true);
        loginLink.setOnClickListener(v -> {
            if (listener != null) listener.onBackToLogin();
        });

        loginContainer.addView(loginText);
        loginContainer.addView(loginLink);
        container.addView(loginContainer);

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

    private void handleSignup() {
        if (isLoading) return;

        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("يرجى ملء جميع الحقول");
            return;
        }

        if (name.length() < 3) {
            showError("الاسم يجب أن يكون 3 أحرف على الأقل");
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
            showError("كلمة المرور وتأكيد كلمة المرور غير متطابقين");
            return;
        }

        hideError();
        setLoading(true);

        // ✅ استخدام Firebase لإنشاء الحساب
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // تحديث الاسم
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
                            setLoading(false);
                            if (updateTask.isSuccessful()) {
                                if (listener != null) listener.onSignupSuccess();
                            } else {
                                showError("تم إنشاء الحساب ولكن فشل تحديث الاسم");
                            }
                        });
                    } else {
                        setLoading(false);
                        String errorMessage = "فشل إنشاء الحساب";
                        if (task.getException() != null) {
                            String exception = task.getException().getMessage();
                            if (exception.contains("already in use")) {
                                errorMessage = "البريد الإلكتروني مستخدم بالفعل";
                            } else if (exception.contains("weak password")) {
                                errorMessage = "كلمة المرور ضعيفة جداً";
                            }
                        }
                        showError(errorMessage);
                    }
                });

        // 📡 أو استخدام API الخاص بك
        // signupWithAPI(name, email, password);
    }

    // ════════════════════════════════════════════════════════════
    // 📡 API Integration (مثال)
    // ════════════════════════════════════════════════════════════

    private void signupWithAPI(String name, String email, String password) {
        new Thread(() -> {
            try {
                URL url = new URL("https://your-api.com/api/auth/signup");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("name", name);
                jsonBody.put("email", email);
                jsonBody.put("password", password);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());

                    requireActivity().runOnUiThread(() -> {
                        setLoading(false);
                        if (listener != null) listener.onSignupSuccess();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        setLoading(false);
                        showError("فشل إنشاء الحساب");
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
        params.bottomMargin = dp(16);
        button.setLayoutParams(params);

        signupButtonText = createText(text, 16, "#FFFFFF", true);
        button.addView(signupButtonText);

        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1f);
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
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}