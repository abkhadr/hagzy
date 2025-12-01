package hagzy.layouts.settings.pages;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bytepulse.hagzy.helpers.LocaleManager;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.TranslationManager;
import com.bytepulse.hagzy.helpers.UiHelper;

import java.util.Arrays;
import java.util.List;

import hagzy.activities.MainActivity;
import hagzy.layouts.settings.cards.LanguageCard;
import hagzy.layouts.settings.models.LanguageOption;

public class PrivacySettingsPage extends ScrollView {

    private final Context context;

    public PrivacySettingsPage(Context context) {
        super(context);
        this.context = context;
        buildPage();
    }

    private void buildPage() {
        setVerticalScrollBarEnabled(false);
        setBackgroundColor(Color.parseColor("#FAFAFA"));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(20));

        TextView subtitle = new TextView(context);
        subtitle.setText(t("settings.language_desc"));
        subtitle.setTextSize(14);
        subtitle.setTypeface(ThemeManager.fontSemiBold());
        subtitle.setTextColor(Color.parseColor("#666666"));
        subtitle.setPadding(0, 0, 0, dp(20));
        subtitle.setTranslationY(-dpf(1f));
        content.addView(subtitle);

        List<LanguageOption> options = Arrays.asList(
                new LanguageOption(t("settings.language_option.ar"), "ar_AR"),
                new LanguageOption(t("settings.language_option.en"), "en_US")
        );

        String currentLang = TranslationManager.getCurrentLang();
        for (LanguageOption option : options) {
            boolean isSelected = option.code.equalsIgnoreCase(currentLang);
            content.addView(new LanguageCard(context, option, isSelected,
                    () -> changeLanguage(option.code)).getView());
        }

        addView(content);
    }

    private void changeLanguage(String langCode) {
        String current = TranslationManager.getCurrentLang();
        if (current != null && current.equalsIgnoreCase(langCode)) {
            showToast(t("settings.language_already"));
            return;
        }

        TranslationManager.load(context, langCode);
        if (context instanceof Activity) {
            LocaleManager.setLocale((Activity) context, langCode);
        }

        showToast(t("settings.language_updated"));
        Intent restart = new Intent(context, MainActivity.class);
        restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(restart);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
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