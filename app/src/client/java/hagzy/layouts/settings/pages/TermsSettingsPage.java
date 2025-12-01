package hagzy.layouts.settings.pages;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

public class TermsSettingsPage extends ScrollView {

    private final Context context;

    public TermsSettingsPage(Context context) {
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
        content.setPadding(dp(20), dp(20), dp(20), dp(20));

        // Introduction
        content.addView(createTextBlock(
                t("settings.terms_intro_title"),
                t("settings.terms_intro_text"),
                true
        ));

        // Section 1: Acceptance
        content.addView(createTextBlock(
                t("settings.terms_acceptance_title"),
                t("settings.terms_acceptance_text"),
                false
        ));

        // Section 2: User Conduct
        content.addView(createTextBlock(
                t("settings.terms_conduct_title"),
                t("settings.terms_conduct_text"),
                false
        ));

        // Section 3: Content Rights
        content.addView(createTextBlock(
                t("settings.terms_content_title"),
                t("settings.terms_content_text"),
                false
        ));

        // Section 4: Privacy
        content.addView(createTextBlock(
                t("settings.terms_privacy_title"),
                t("settings.terms_privacy_text"),
                false
        ));

        // Section 5: Termination
        content.addView(createTextBlock(
                t("settings.terms_termination_title"),
                t("settings.terms_termination_text"),
                false
        ));

        // Section 6: Disclaimer
        content.addView(createTextBlock(
                t("settings.terms_disclaimer_title"),
                t("settings.terms_disclaimer_text"),
                false
        ));

        // Last Updated
        TextView lastUpdated = new TextView(context);
        lastUpdated.setText(t("settings.terms_last_updated"));
        lastUpdated.setTextSize(13);
        lastUpdated.setTypeface(ThemeManager.fontSemiBold());
        lastUpdated.setTextColor(Color.parseColor("#999999"));
        lastUpdated.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams updatedParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        updatedParams.topMargin = dp(32);
        lastUpdated.setLayoutParams(updatedParams);
        content.addView(lastUpdated);

        addView(content);
    }

    private LinearLayout createTextBlock(String title, String text, boolean isFirst) {
        LinearLayout block = new LinearLayout(context);
        block.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        blockParams.topMargin = isFirst ? 0 : dp(24);
        block.setLayoutParams(blockParams);

        // Title
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(18);
        titleView.setTypeface(ThemeManager.fontBold());
        titleView.setTextColor(Color.parseColor("#1A1A1A"));
        titleView.setTranslationY(-dpf(1.5f));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(12);
        titleView.setLayoutParams(titleParams);
        block.addView(titleView);

        // Text
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(15);
        textView.setTypeface(ThemeManager.fontRegular());
        textView.setTextColor(Color.parseColor("#666666"));
        textView.setLineSpacing(dpf(4f), 1f);
        block.addView(textView);

        return block;
    }

    private int dp(int dpValue) {
        return UiHelper.dp(context, dpValue);
    }

    private float dpf(float value) {
        return UiHelper.dpFloat(context, value);
    }
}