package hagzy;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import hagzy.fragments.field.AddFieldFragment;
import hagzy.fragments.field.EditFieldFragment;
import hagzy.fragments.field.FieldDetailsFragment;
import hagzy.fragments.field.FieldsListFragment;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.DirectionHelper;
import com.bytepulse.hagzy.helpers.LocaleManager;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.TranslationManager;
import com.bytepulse.hagzy.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    private FrameLayout fragmentContainer;
    private View dashboardView;
    private LinearLayout mainContainer;

    private TextView errorText;
    private ProgressBar progressBar;
    private FrameLayout progressOverlay;
    private boolean isLoading = false;

    private String providerId;
    private boolean hasPermission = false;

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
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        providerId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        setContentView(buildMainLayout());

        checkPermissionAndLoadData();
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

        // Container للـ Dashboard View
        dashboardView = buildDashboardUI();
        root.addView(dashboardView);

        // Container للـ Fragments
        fragmentContainer = new FrameLayout(this);
        fragmentContainer.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        fragmentContainer.setId(View.generateViewId());
        fragmentContainer.setVisibility(View.GONE);
        root.addView(fragmentContainer);

        // Progress Overlay
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

        return root;
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 واجهة Dashboard الرئيسية
    // ════════════════════════════════════════════════════════════

    private View buildDashboardUI() {
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

        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(dp(24), dp(60), dp(24), dp(40));
        mainContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Header
        LinearLayout header = createHeader();
        mainContainer.addView(header);

        // Error Message
        errorText = createText("", 14, "#E53935", false);
        errorText.setGravity(Gravity.CENTER);
        errorText.setVisibility(View.GONE);
        LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        errorParams.bottomMargin = dp(16);
        errorText.setLayoutParams(errorParams);
        mainContainer.addView(errorText);

        scrollView.addView(mainContainer);
        root.addView(scrollView);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            scrollView.setPadding(0, top, 0, bottom);
            return insets;
        });

        return root;
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headerParams.bottomMargin = dp(32);
        header.setLayoutParams(headerParams);

        TextView title = createText("لوحة التحكم", 32, "#000000", true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        header.addView(title);

        TextView subtitle = createText("إدارة الملاعب والحجوزات", 16, "#666666", false);
        subtitle.setGravity(Gravity.CENTER);
        header.addView(subtitle);

        return header;
    }

    // ════════════════════════════════════════════════════════════
    // 🔐 Permission Check & Load Data
    // ════════════════════════════════════════════════════════════

    private void checkPermissionAndLoadData() {
        if (providerId.isEmpty()) {
            showError("خطأ في تحميل البيانات");
            showNoPermissionView();
            return;
        }

        setLoading(true);

        // التحقق من وجود صلاحيات المزود
        db.collection("providers")
                .document(providerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        hasPermission = true;
                        showFieldsListView();
                    } else {
                        hasPermission = false;
                        showNoPermissionView();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("فشل التحقق من الصلاحيات");
                    showNoPermissionView();
                });
    }

    private void showNoPermissionView() {
        mainContainer.removeAllViews();

        LinearLayout header = createHeader();
        mainContainer.addView(header);
        mainContainer.addView(errorText);

        // Icon
        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(120), dp(120));
        iconParams.gravity = Gravity.CENTER;
        iconParams.topMargin = dp(60);
        iconParams.bottomMargin = dp(24);
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.arrow_left); // تأكد من وجود الأيقونة
        icon.setColorFilter(Color.parseColor("#BDBDBD"));
        mainContainer.addView(icon);

        TextView noPermissionTitle = createText("لا توجد صلاحيات", 24, "#000000", true);
        noPermissionTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams noPermTitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        noPermTitleParams.bottomMargin = dp(12);
        noPermissionTitle.setLayoutParams(noPermTitleParams);
        mainContainer.addView(noPermissionTitle);

        TextView noPermissionSubtitle = createText("يجب أن يكون لديك حساب مزود لإدارة الملاعب", 16, "#666666", false);
        noPermissionSubtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams noPermSubParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        noPermSubParams.bottomMargin = dp(32);
        noPermSubParams.setMargins(dp(40), 0, dp(40), dp(32));
        noPermissionSubtitle.setLayoutParams(noPermSubParams);
        mainContainer.addView(noPermissionSubtitle);

        LinearLayout contactButton = createPrimaryButton("تواصل معنا", "#000000");
        contactButton.setOnClickListener(v -> {
            // فتح صفحة التواصل أو البريد
            showError("يرجى التواصل مع الإدارة");
        });
        mainContainer.addView(contactButton);
    }

    private void showFieldsListView() {
        FieldsListFragment fragment = new FieldsListFragment();
        fragment.setProviderId(providerId);
        fragment.setOnFieldClickListener(fieldId -> showFieldDetails(fieldId));
        fragment.setOnAddFieldClickListener(() -> showAddField());

        dashboardView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction()
                .replace(fragmentContainer.getId(), fragment)
                .commit();
    }

    // ════════════════════════════════════════════════════════════
    // 📱 Fragment Navigation
    // ════════════════════════════════════════════════════════════

    private void showAddField() {
        AddFieldFragment fragment = new AddFieldFragment();
        fragment.setProviderId(providerId);
        fragment.setOnFieldAddedListener(new AddFieldFragment.OnFieldAddedListener() {
            @Override
            public void onFieldAdded() {
                // Refresh the fields list
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(fragmentContainer.getId());
                getSupportFragmentManager().popBackStack();

                // Reload fields list
                showFieldsListView();
            }

            @Override
            public void onCancel() {
                getSupportFragmentManager().popBackStack();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .replace(fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showFieldDetails(String fieldId) {
        FieldDetailsFragment fragment = new FieldDetailsFragment();
        fragment.setFieldId(fieldId);
        fragment.setOnBackListener(() -> showFieldsListView());
        fragment.setOnEditClickListener(id -> showEditField(id));

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .replace(fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showEditField(String fieldId) {
        EditFieldFragment fragment = new EditFieldFragment();
        fragment.setFieldId(fieldId);
        fragment.setOnFieldUpdatedListener(new EditFieldFragment.OnFieldUpdatedListener() {
            @Override
            public void onFieldUpdated() {
                // Go back and refresh
                getSupportFragmentManager().popBackStack();
                // Reload details
                showFieldDetails(fieldId);
            }

            @Override
            public void onCancel() {
                getSupportFragmentManager().popBackStack();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .replace(fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showDashboard() {
        fragmentContainer.setVisibility(View.GONE);
        dashboardView.setVisibility(View.VISIBLE);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(fragmentContainer.getId());
        if (currentFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }
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

        TextView buttonText = createText(text, 16, "#FFFFFF", true);
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
        progressOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(fragmentContainer.getId());
            if (currentFragment instanceof FieldsListFragment) {
                showDashboard();
                checkPermissionAndLoadData();
            } else if (currentFragment != null) {
                showFieldsListView();
            } else {
                super.onBackPressed();
            }
        }
    }
}