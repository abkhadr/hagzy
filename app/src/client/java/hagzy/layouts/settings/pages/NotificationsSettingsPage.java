package hagzy.layouts.settings.pages;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

import java.util.Arrays;

import hagzy.layouts.settings.cards.ToggleCard;
import hagzy.layouts.settings.models.ToggleItem;

public class NotificationsSettingsPage extends ScrollView {

    private final Context context;

    public NotificationsSettingsPage(Context context) {
        super(context);
        this.context = context;
        buildPage();
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

        // General Notifications
        content.addView(createSection(
                t("settings.notifications_general"),
                Arrays.asList(
                        new ToggleItem(
                                t("settings.push_notifications"),
                                t("settings.push_notifications_desc"),
                                R.drawable.ellipsis_vertical,
                                true,
                                this::onPushNotificationsToggle
                        ),
                        new ToggleItem(
                                t("settings.email_notifications"),
                                t("settings.email_notifications_desc"),
                                R.drawable.ellipsis_vertical,
                                false,
                                this::onEmailNotificationsToggle
                        )
                )
        ));

        // Activity Notifications
        content.addView(createSection(
                t("settings.notifications_activity"),
                Arrays.asList(
                        new ToggleItem(
                                t("settings.likes_comments"),
                                t("settings.likes_comments_desc"),
                                R.drawable.ellipsis_vertical,
                                true,
                                this::onLikesCommentsToggle
                        ),
                        new ToggleItem(
                                t("settings.new_followers"),
                                t("settings.new_followers_desc"),
                                R.drawable.ellipsis_vertical,
                                true,
                                this::onNewFollowersToggle
                        ),
                        new ToggleItem(
                                t("settings.mentions"),
                                t("settings.mentions_desc"),
                                R.drawable.ellipsis_vertical,
                                true,
                                this::onMentionsToggle
                        )
                )
        ));

        // Updates & News
        content.addView(createSection(
                t("settings.notifications_updates"),
                Arrays.asList(
                        new ToggleItem(
                                t("settings.app_updates"),
                                t("settings.app_updates_desc"),
                                R.drawable.ellipsis_vertical,
                                true,
                                this::onAppUpdatesToggle
                        ),
                        new ToggleItem(
                                t("settings.news_features"),
                                t("settings.news_features_desc"),
                                R.drawable.ellipsis_vertical,
                                false,
                                this::onNewsFeaturesToggle
                        )
                )
        ));

        addView(content);
    }

    private LinearLayout createSection(String title, java.util.List<ToggleItem> items) {
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

        for (ToggleItem item : items) {
            section.addView(new ToggleCard(context, item).getView());
        }

        return section;
    }

    // Toggle callbacks
    private void onPushNotificationsToggle(boolean enabled) {
        // Handle push notifications toggle
    }

    private void onEmailNotificationsToggle(boolean enabled) {
        // Handle email notifications toggle
    }

    private void onLikesCommentsToggle(boolean enabled) {
        // Handle likes/comments toggle
    }

    private void onNewFollowersToggle(boolean enabled) {
        // Handle new followers toggle
    }

    private void onMentionsToggle(boolean enabled) {
        // Handle mentions toggle
    }

    private void onAppUpdatesToggle(boolean enabled) {
        // Handle app updates toggle
    }

    private void onNewsFeaturesToggle(boolean enabled) {
        // Handle news/features toggle
    }

    private int dp(int dpValue) {
        return UiHelper.dp(context, dpValue);
    }

    private float dpf(float value) {
        return UiHelper.dpFloat(context, value);
    }
}