package com.bytepulse.hagzy;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bytepulse.hagzy.auth.ForgotPasswordFragment;
import com.bytepulse.hagzy.auth.SignupFragment;
import com.bytepulse.hagzy.helpers.DirectionHelper;
import com.bytepulse.hagzy.helpers.LocaleManager;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.TranslationManager;
import com.bytepulse.hagzy.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.recaptcha.Recaptcha;
import com.google.android.recaptcha.RecaptchaTasksClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import hagzy.MainActivity;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    private FrameLayout fragmentContainer;
    private View loginView;

    private EditText emailInput, passwordInput;
    private LinearLayout loginButton, googleButton;
    private TextView loginButtonText, errorText;
    private ProgressBar progressBar;
    private FrameLayout progressOverlay;
    private boolean isLoading = false;
    private RecaptchaTasksClient recaptchaClient;

    private void setupInit() {
        LocaleManager.setLocale(this, "ar_AR");
        LocaleManager.applyLocale(this);
        ThemeManager.setDarkMode(this, false);
        ThemeManager.init(this);
        DirectionHelper.applyDirection(this, LocaleManager.getSavedLanguage(this));
        ThemeManager.applySystemBars(this);
        TranslationManager.load(this, LocaleManager.getSavedLanguage(this));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupInit();

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        setContentView(buildMainLayout());
    }
    // ════════════════════════════════════════════════════════════
    // 🎨 Layout الرئيسي
    // ════════════════════════════════════════════════════════════

    private View buildMainLayout() {
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(Color.WHITE);

        // Container للـ Login View
        loginView = buildLoginUI();
        root.addView(loginView);

        // Container للـ Fragments
        fragmentContainer = new FrameLayout(this);
        fragmentContainer.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        fragmentContainer.setId(View.generateViewId());
        fragmentContainer.setVisibility(View.GONE);
        root.addView(fragmentContainer);

        return root;
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 واجهة تسجيل الدخول
    // ════════════════════════════════════════════════════════════

    private View buildLoginUI() {
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        scrollView.setFillViewport(true);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(60), dp(24), dp(40));
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView title = createText("مرحباً بك", 32, "#000000", true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        container.addView(title);

        TextView subtitle = createText("سجل دخولك للمتابعة", 16, "#666666", false);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.bottomMargin = dp(40);
        subtitle.setLayoutParams(subtitleParams);
        container.addView(subtitle);

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

        googleButton = createSocialButton(R.drawable.google, "المتابعة مع Google", "#FFFFFF", "#000000");
        googleButton.setOnClickListener(v -> signInWithGoogle());
        container.addView(googleButton);

        container.addView(createDivider());

        container.addView(createLabel("البريد الإلكتروني"));
        emailInput = createInput("name@example.com", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        container.addView(emailInput);

        container.addView(createLabel("كلمة المرور"));
        passwordInput = createInput("••••••••", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(passwordInput);

        TextView forgotPassword = createText("نسيت كلمة المرور؟", 14, "#1976D2", true);
        forgotPassword.setGravity(Gravity.END);
        LinearLayout.LayoutParams forgotParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        forgotParams.topMargin = dp(8);
        forgotParams.bottomMargin = dp(24);
        forgotPassword.setLayoutParams(forgotParams);
        forgotPassword.setOnClickListener(v -> showForgotPassword());
        container.addView(forgotPassword);

        loginButton = createPrimaryButton("تسجيل الدخول", "#000000");
        loginButton.setOnClickListener(v -> handleLogin());
        container.addView(loginButton);

        LinearLayout signupContainer = new LinearLayout(this);
        signupContainer.setOrientation(LinearLayout.HORIZONTAL);
        signupContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams signupParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        signupParams.topMargin = dp(24);
        signupContainer.setLayoutParams(signupParams);

        TextView signupText = createText("ليس لديك حساب؟ ", 14, "#666666", false);
        TextView signupLink = createText("إنشاء حساب", 14, "#1976D2", true);
        signupLink.setOnClickListener(v -> showSignup());

        signupContainer.addView(signupText);
        signupContainer.addView(signupLink);
        container.addView(signupContainer);

        scrollView.addView(container);
        root.addView(scrollView);

        progressOverlay = new FrameLayout(this);
        progressOverlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        progressOverlay.setBackgroundColor(Color.parseColor("#80000000"));
        progressOverlay.setVisibility(View.GONE);
        progressOverlay.setClickable(true);

        progressBar = new ProgressBar(this);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                dp(50), dp(50), Gravity.CENTER
        );
        progressBar.setLayoutParams(progressParams);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressOverlay.addView(progressBar);

        root.addView(progressOverlay);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            scrollView.setPadding(0, top, 0, bottom);
            return insets;
        });

        return root;
    }

    // ════════════════════════════════════════════════════════════
    // 📱 Fragment Navigation
    // ════════════════════════════════════════════════════════════

    private void showSignup() {
        SignupFragment fragment = new SignupFragment();
        fragment.setOnSignupListener(new SignupFragment.OnSignupListener() {
            @Override
            public void onSignupSuccess() {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    saveUserSession(user);
                    navigateToMain();
                }
            }

            @Override
            public void onBackToLogin() {
                showLogin();
            }
        });

        loginView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .replace(fragmentContainer.getId(), fragment)
                .commit();
    }

    private void showForgotPassword() {
        ForgotPasswordFragment fragment = new ForgotPasswordFragment();
        fragment.setOnForgotPasswordListener(() -> showLogin());

        loginView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .replace(fragmentContainer.getId(), fragment)
                .commit();
    }

    private void showLogin() {
        fragmentContainer.setVisibility(View.GONE);
        loginView.setVisibility(View.VISIBLE);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(fragmentContainer.getId());
        if (currentFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }
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
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserSession(user);
                        navigateToMain();
                    } else {
                        showError("البريد الإلكتروني أو كلمة المرور غير صحيحة");
                    }
                });
    }

    private void signInWithGoogle() {
        if (isLoading) return;
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                showError("فشل تسجيل الدخول مع Google");
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserSession(user);
                        navigateToMain();
                    } else {
                        showError("فشل المصادقة");
                    }
                });
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    private TextView createText(String text, int size, String color, boolean bold) {
        TextView tv = new TextView(this);
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
        EditText input = new EditText(this);
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
        LinearLayout button = new LinearLayout(this);
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

        loginButtonText = createText(text, 16, "#FFFFFF", true);
        button.addView(loginButtonText);

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

    private LinearLayout createSocialButton(int iconRes, String text, String bgColor, String textColor) {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(1), Color.parseColor("#E0E0E0"));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        params.bottomMargin = dp(12);
        button.setLayoutParams(params);

        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMarginEnd(dp(12));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(iconRes);
        button.addView(icon);

        TextView buttonText = createText(text, 16, textColor, true);
        button.addView(buttonText);

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

    private LinearLayout createDivider() {
        LinearLayout dividerContainer = new LinearLayout(this);
        dividerContainer.setOrientation(LinearLayout.HORIZONTAL);
        dividerContainer.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.topMargin = dp(8);
        containerParams.bottomMargin = dp(16);
        dividerContainer.setLayoutParams(containerParams);

        View line1 = new View(this);
        LinearLayout.LayoutParams lineParams1 = new LinearLayout.LayoutParams(0, dp(1), 1);
        line1.setLayoutParams(lineParams1);
        line1.setBackgroundColor(Color.parseColor("#E0E0E0"));

        TextView orText = createText("  أو  ", 14, "#999999", false);

        View line2 = new View(this);
        LinearLayout.LayoutParams lineParams2 = new LinearLayout.LayoutParams(0, dp(1), 1);
        line2.setLayoutParams(lineParams2);
        line2.setBackgroundColor(Color.parseColor("#E0E0E0"));

        dividerContainer.addView(line1);
        dividerContainer.addView(orText);
        dividerContainer.addView(line2);

        return dividerContainer;
    }

    private void saveUserSession(FirebaseUser user) {
        SessionManager session = new SessionManager(this);
        session.saveUser(
                user.getUid(),
                user.getDisplayName() != null ? user.getDisplayName() : "مستخدم",
                user.getEmail() != null ? user.getEmail() : "",
                user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : ""
        );
    }

    private void navigateToMain() {
        startActivity(new Intent(AuthActivity.this, MainActivity.class));
        finish();
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
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(fragmentContainer.getId());
        if (currentFragment != null) {
            showLogin();
        } else {
            super.onBackPressed();
        }
    }
}