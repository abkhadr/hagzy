package hagzy.fragments.settings;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bytepulse.hagzy.BuildConfig;
import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.RootActivity;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainSettingsFragment extends Fragment {

    private OnNavigationListener onNavigationListener;

    public interface OnNavigationListener {
        void onEditProfile();
        void onLanguage();
        void onNotifications();
        void onPrivacy();
        void onTerms();
        void onBack();
    }

    public void setOnNavigationListener(OnNavigationListener listener) {
        this.onNavigationListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return buildUI();
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 UI Building
    // ════════════════════════════════════════════════════════════

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
        scrollView.setVerticalScrollBarEnabled(false);

        LinearLayout mainContainer = new LinearLayout(requireContext());
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(dp(24), dp(60), dp(24), dp(40));

        // Back Button
        mainContainer.addView(createBackButton());

        // Header
        mainContainer.addView(createHeader());

        // User Profile Card
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri photoUri = currentUser.getPhotoUrl();
            String photoUrl = (photoUri != null) ? photoUri.toString() : null;
            mainContainer.addView(createProfileCard(name, email, photoUrl));
        }

        // Account Section
        mainContainer.addView(createSectionTitle(t("settings.account")));
        mainContainer.addView(createSettingCard(
                t("settings.edit_profile"),
                t("settings.edit_profile_desc"),
                R.drawable.cog_8,
                () -> {
                    if (onNavigationListener != null) {
                        onNavigationListener.onEditProfile();
                    }
                }
        ));

        // Preferences Section
        mainContainer.addView(createSectionTitle(t("settings.preferences")));
        mainContainer.addView(createSettingCard(
                t("field.language"),
                t("settings.language_desc"),
                R.drawable.cog_8,
                () -> {
                    if (onNavigationListener != null) {
                        onNavigationListener.onLanguage();
                    }
                }
        ));
        mainContainer.addView(createSettingCard(
                t("settings.notifications"),
                t("settings.notifications_desc"),
                R.drawable.cog_8,
                () -> {
                    if (onNavigationListener != null) {
                        onNavigationListener.onNotifications();
                    }
                }
        ));

        // About Section
        mainContainer.addView(createSectionTitle(t("settings.about")));
        mainContainer.addView(createSettingCard(
                t("settings.privacy_policy"),
                t("settings.privacy_policy_desc"),
                R.drawable.cog_8,
                () -> {
                    if (onNavigationListener != null) {
                        onNavigationListener.onPrivacy();
                    }
                }
        ));
        mainContainer.addView(createSettingCard(
                t("settings.terms"),
                t("settings.terms_desc"),
                R.drawable.cog_8,
                () -> {
                    if (onNavigationListener != null) {
                        onNavigationListener.onTerms();
                    }
                }
        ));

        // Account Actions
        mainContainer.addView(createSectionTitle(t("settings.account_actions")));
        mainContainer.addView(createLogoutCard());

        // Version Info
        String versionName = BuildConfig.VERSION_NAME;
        String buildType = BuildConfig.BUILD_TYPE;
        TextView versionText = new TextView(requireContext());
        String appInfo = "v" + versionName + " • " + buildType;
        versionText.setText(appInfo);
        versionText.setTextSize(14);
        versionText.setTypeface(ThemeManager.fontSemiBold());
        versionText.setTextColor(Color.parseColor("#99666666"));
        versionText.setGravity(Gravity.CENTER);
        versionText.setPadding(dp(16), dp(24), dp(16), dp(16));
        mainContainer.addView(versionText);

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

    private LinearLayout createBackButton() {
        LinearLayout backButton = new LinearLayout(requireContext());
        backButton.setOrientation(LinearLayout.HORIZONTAL);
        backButton.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(24);
        backButton.setLayoutParams(params);

        ImageView backIcon = new ImageView(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMarginEnd(dp(8));
        backIcon.setLayoutParams(iconParams);
        backIcon.setImageResource(R.drawable.arrow_left);
        backIcon.setColorFilter(Color.parseColor("#000000"));
        backButton.addView(backIcon);

        TextView backText = createText("رجوع", 16, "#000000", true);
        backButton.addView(backText);

        backButton.setOnClickListener(v -> {
            if (onNavigationListener != null) {
                onNavigationListener.onBack();
            }
        });

        return backButton;
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headerParams.bottomMargin = dp(24);
        header.setLayoutParams(headerParams);

        TextView title = createText(t("settings.title"), 28, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        header.addView(title);

        TextView subtitle = createText("إدارة الحساب والتفضيلات", 16, "#666666", false);
        header.addView(subtitle);

        return header;
    }

    private LinearLayout createProfileCard(String name, String email, String photoUrl) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(24), dp(24), dp(24), dp(24));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(16));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(24);
        card.setLayoutParams(cardParams);

        // Profile Image
        ImageView profileImg = new ImageView(requireContext());
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dp(80), dp(80));
        imgParams.bottomMargin = dp(12);
        profileImg.setLayoutParams(imgParams);

        GradientDrawable imgBg = new GradientDrawable();
        imgBg.setShape(GradientDrawable.OVAL);
        imgBg.setColor(Color.WHITE);
        profileImg.setBackground(imgBg);
        profileImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        profileImg.setClipToOutline(true);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(requireContext()).load(photoUrl).into(profileImg);
        } else {
            profileImg.setImageResource(R.drawable.arrow_left);
        }

        // User Name
        TextView userName = new TextView(requireContext());
        userName.setText(name != null ? name : "—");
        userName.setTextSize(24);
        userName.setTypeface(ThemeManager.fontBold());
        userName.setTextColor(Color.parseColor("#000000"));
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        nameParams.bottomMargin = dp(4);
        userName.setLayoutParams(nameParams);

        // User Email
        TextView userEmail = new TextView(requireContext());
        userEmail.setText(email != null ? email : "—");
        userEmail.setTextSize(14);
        userEmail.setTypeface(ThemeManager.fontRegular());
        userEmail.setTextColor(Color.parseColor("#666666"));

        card.addView(profileImg);
        card.addView(userName);
        card.addView(userEmail);

        return card;
    }

    private TextView createSectionTitle(String title) {
        TextView section = new TextView(requireContext());
        section.setText(title);
        section.setTextSize(20);
        section.setTypeface(ThemeManager.fontBold());
        section.setTextColor(Color.parseColor("#000000"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(8);
        params.bottomMargin = dp(16);
        section.setLayoutParams(params);
        return section;
    }

    private LinearLayout createSettingCard(String title, String description, int iconRes, Runnable onClick) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Color.parseColor("#E0E0E0"));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        // Icon
        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(Color.parseColor("#000000"));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMarginEnd(dp(12));
        icon.setLayoutParams(iconParams);
        card.addView(icon);

        // Text Column
        LinearLayout textColumn = new LinearLayout(requireContext());
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        textColumn.setLayoutParams(textParams);

        TextView titleText = createText(title, 16, "#000000", true);
        textColumn.addView(titleText);

        TextView descText = createText(description, 12, "#666666", false);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        descParams.topMargin = dp(2);
        descText.setLayoutParams(descParams);
        textColumn.addView(descText);

        card.addView(textColumn);

        // Arrow
        ImageView arrow = new ImageView(requireContext());
        arrow.setImageResource(R.drawable.arrow_left);
        arrow.setColorFilter(Color.parseColor("#666666"));
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        arrow.setLayoutParams(arrowParams);
        card.addView(arrow);

        card.setOnClickListener(v -> {
            if (onClick != null) onClick.run();
        });

        card.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1f);
            }
            return false;
        });

        return card;
    }

    @SuppressLint("ClickableViewAccessibility")
    private LinearLayout createLogoutCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#FFEBEE"));
        bg.setCornerRadius(dp(16));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        // Icon
        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.cog_8);
        icon.setColorFilter(Color.parseColor("#F44336"));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMarginEnd(dp(12));
        icon.setLayoutParams(iconParams);
        card.addView(icon);

        // Text
        TextView titleText = createText(t("field.logout"), 16, "#F44336", true);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        titleText.setLayoutParams(textParams);
        card.addView(titleText);

        // Arrow
        ImageView arrow = new ImageView(requireContext());
        arrow.setImageResource(R.drawable.arrow_left);
        arrow.setColorFilter(Color.parseColor("#F44336"));
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        arrow.setLayoutParams(arrowParams);
        card.addView(arrow);

        card.setOnClickListener(v -> handleLogout());

        card.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1f);
            }
            return false;
        });

        return card;
    }

    private void handleLogout() {
        // Firebase Logout
        FirebaseAuth.getInstance().signOut();

        // Google Logout
        GoogleSignIn.getClient(
                requireContext(),
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut().addOnCompleteListener(task -> {
            Intent intent = new Intent(requireContext(), RootActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}