package hagzy.layouts.settings.pages;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bytepulse.hagzy.BuildConfig;
import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.activities.RootActivity;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hagzy.layouts.settings.cards.LogoutCard;
import hagzy.layouts.settings.cards.SettingCard;
import hagzy.layouts.settings.interfaces.Searchable;
import hagzy.layouts.settings.models.SearchResult;
import hagzy.layouts.settings.models.SettingItem;

public class MainSettingsPage extends ScrollView implements Searchable {

    private final Context context;
    private final String userName;
    private final String userEmail;
    private final String photoUrl;

    private final Runnable onLanguageClick;
    private final Runnable onNotificationsClick;
    private final Runnable onPrivacyClick;
    private final Runnable onTermsClick;

    private List<SettingItem> allSettings;
    private TextView largeTitleText;
    private OnScrollListener scrollListener;

    public MainSettingsPage(Context context, String userName, String userEmail, String photoUrl,
                            Runnable onLanguageClick, Runnable onNotificationsClick,
                            Runnable onPrivacyClick, Runnable onTermsClick) {
        super(context);
        this.context = context;
        this.userName = userName;
        this.userEmail = userEmail;
        this.photoUrl = photoUrl;
        this.onLanguageClick = onLanguageClick;
        this.onNotificationsClick = onNotificationsClick;
        this.onPrivacyClick = onPrivacyClick;
        this.onTermsClick = onTermsClick;

        initializeSettings();
        buildPage();
        setupScrollListener();
    }

    public interface OnScrollListener {
        void onScroll(float scrollProgress);
    }

    public void setOnScrollListener(OnScrollListener listener) {
        this.scrollListener = listener;
    }

    private void initializeSettings() {
        allSettings = new ArrayList<>();

        // Account settings
        allSettings.add(new SettingItem(
                t("settings.edit_profile"),
                t("settings.edit_profile_desc"),
                R.drawable.cog_8,
                () -> showToast("Edit Profile")
        ));

        // Preferences
        allSettings.add(new SettingItem(
                t("settings.language"),
                t("settings.language_desc"),
                R.drawable.cog_8,
                onLanguageClick
        ));

        allSettings.add(new SettingItem(
                t("settings.notifications"),
                t("settings.notifications_desc"),
                R.drawable.cog_8,
                onNotificationsClick
        ));

        // About
        allSettings.add(new SettingItem(
                t("settings.privacy_policy"),
                t("settings.privacy_policy_desc"),
                R.drawable.cog_8,
                onPrivacyClick
        ));

        allSettings.add(new SettingItem(
                t("settings.terms"),
                t("settings.terms_desc"),
                R.drawable.cog_8,
                onTermsClick
        ));
    }

    private void buildPage() {
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setVerticalScrollBarEnabled(false);
        setBackgroundColor(Color.parseColor("#FAFAFA"));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        content.setPadding(0, 0, 0, dp(20));

        // Large Title at top
        largeTitleText = new TextView(context);
        largeTitleText.setText(t("settings.title"));
        largeTitleText.setTextSize(34);
        largeTitleText.setTypeface(ThemeManager.fontBold());
        largeTitleText.setTextColor(Color.parseColor("#1A1A1A"));
        largeTitleText.setTranslationY(-dpf(2f));
        LinearLayout.LayoutParams largeTitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        largeTitleParams.setMargins(dp(20), dp(20), dp(20), dp(16));
        largeTitleText.setLayoutParams(largeTitleParams);
        content.addView(largeTitleText);

        // Account Section
        content.addView(createSection(
                t("settings.account"),
                Arrays.asList(allSettings.get(0))
        ));

        // Preferences Section
        content.addView(createSection(
                t("settings.preferences"),
                Arrays.asList(allSettings.get(1), allSettings.get(2))
        ));

        // About Section
        content.addView(createSection(
                t("settings.about"),
                Arrays.asList(allSettings.get(3), allSettings.get(4))
        ));

        // Account Actions
        content.addView(createSection(t("settings.account_actions"), Arrays.asList()));
        content.addView(new LogoutCard(context, this::performLogout).getView());

        // Version
        TextView versionText = new TextView(context);
        String appInfo = String.format(t("settings.version_label"),
                BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE);
        versionText.setText(appInfo);
        versionText.setTextSize(13);
        versionText.setTypeface(ThemeManager.fontSemiBold());
        versionText.setTextColor(Color.parseColor("#999999"));
        versionText.setGravity(Gravity.CENTER);
        versionText.setPadding(dp(20), dp(32), dp(20), dp(20));
        versionText.setTranslationY(-dpf(1f));
        content.addView(versionText);

        addView(content);
    }

    private void setupScrollListener() {
        setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Calculate scroll progress (0 to 1)
            // Animation triggers at 60dp instead of gradual
            int triggerPoint = dp(60);
            float progress = scrollY >= triggerPoint ? 1f : 0f;

            // Animate with duration instead of instant change
            if (scrollY >= triggerPoint && largeTitleText.getAlpha() > 0.5f) {
                // Scrolled down - hide large title
                animateLargeTitleVisibility(false);
            } else if (scrollY < triggerPoint && largeTitleText.getAlpha() < 0.5f) {
                // Scrolled up - show large title
                animateLargeTitleVisibility(true);
            }

            // Notify listener for header title
            if (scrollListener != null) {
                scrollListener.onScroll(progress);
            }
        });
    }

    private void animateLargeTitleVisibility(boolean show) {
        int duration = 200;

        if (show) {
            // Show animation
            largeTitleText.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(-dpf(2f))
                    .setDuration(duration)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            // Change text color back to original
            largeTitleText.setTextColor(Color.parseColor("#1A1A1A"));
        } else {
            // Hide animation
            largeTitleText.animate()
                    .alpha(0f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .translationY(-dpf(2f) - dp(10))
                    .setDuration(duration)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            // Fade color to background
            largeTitleText.setTextColor(Color.parseColor("#FAFAFA"));
        }
    }

    private int blendColors(int color1, int color2, float ratio) {
        float inverseRatio = 1f - ratio;

        int r = (int) (Color.red(color1) * inverseRatio + Color.red(color2) * ratio);
        int g = (int) (Color.green(color1) * inverseRatio + Color.green(color2) * ratio);
        int b = (int) (Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio);

        return Color.rgb(r, g, b);
    }

    public float getCurrentScrollProgress() {
        int triggerPoint = dp(60);
        return getScrollY() >= triggerPoint ? 1f : 0f;
    }

    private LinearLayout createSection(String title, java.util.List<SettingItem> items) {
        LinearLayout section = new LinearLayout(context);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(dp(20), 0, dp(20), 0);

        if (title != null && !title.isEmpty()) {
            TextView sectionTitle = new TextView(context);
            sectionTitle.setText(title);
            sectionTitle.setTextSize(13);
            sectionTitle.setTypeface(ThemeManager.fontBold());
            sectionTitle.setTextColor(Color.parseColor("#999999"));
            sectionTitle.setTranslationY(-dpf(1f));
            sectionTitle.setAllCaps(true);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            titleParams.topMargin = dp(24);
            titleParams.bottomMargin = dp(12);
            sectionTitle.setLayoutParams(titleParams);
            section.addView(sectionTitle);
        }

        for (SettingItem item : items) {
            section.addView(new SettingCard(context, item).getView());
        }

        return section;
    }

    @Override
    public List<SearchResult> search(String query) {
        List<SearchResult> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String lowerQuery = query.toLowerCase().trim();

        for (SettingItem item : allSettings) {
            if (item.title.toLowerCase().contains(lowerQuery) ||
                    item.description.toLowerCase().contains(lowerQuery)) {

                results.add(new SearchResult(
                        "main",
                        item.title,
                        item.description,
                        item.title,
                        item.iconRes,
                        item.onClick
                ));
            }
        }

        return results;
    }

    @Override
    public void highlightResult(SearchResult result) {
        if (result.action != null) {
            result.action.run();
        }
    }

    @Override
    public void clearHighlights() {
        // Clear any highlights
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();

        GoogleSignIn.getClient(
                context,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut().addOnCompleteListener(task -> {
            Intent intent = new Intent(context, RootActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private int dp(int dpValue) {
        return UiHelper.dp(context, dpValue);
    }

    private float dpf(float value) {
        return UiHelper.dpFloat(context, value);
    }
}