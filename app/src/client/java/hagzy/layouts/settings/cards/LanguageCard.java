package hagzy.layouts.settings.cards;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.settings.models.LanguageOption;
import hagzy.layouts.settings.utils.SettingsCardAnimator;

public class LanguageCard {

    private final Context context;
    private final LanguageOption option;
    private final boolean selected;
    private final Runnable onClick;
    private CardView card;

    public LanguageCard(Context context, LanguageOption option, boolean selected, Runnable onClick) {
        this.context = context;
        this.option = option;
        this.selected = selected;
        this.onClick = onClick;
        buildCard();
    }

    private void buildCard() {
        card = new CardView(context);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(6);
        card.setLayoutParams(cardParams);
        card.setRadius(dp(12));
        card.setCardElevation(0);

        if (selected) {
            card.setCardBackgroundColor(Color.parseColor("#000000"));
        } else {
            card.setCardBackgroundColor(Color.WHITE);
        }

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(dp(20), dp(18), dp(20), dp(18));

        TextView languageText = new TextView(context);
        languageText.setText(option.label);
        languageText.setTextSize(16);
        languageText.setTypeface(ThemeManager.fontBold());
        languageText.setTextColor(selected ? Color.WHITE : Color.parseColor("#1A1A1A"));
        languageText.setTranslationY(-dpf(1.5f));
        languageText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        content.addView(languageText);

        if (selected) {
            ImageView checkIcon = new ImageView(context);
            checkIcon.setImageResource(R.drawable.check_badge);
            checkIcon.setColorFilter(Color.WHITE);
            checkIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
            content.addView(checkIcon);
        }

        card.addView(content);

        // Apply animation
        SettingsCardAnimator.applyAnimation(card, onClick);
    }

    public CardView getView() {
        return card;
    }

    private int dp(int dpValue) {
        return UiHelper.dp(context, dpValue);
    }

    private float dpf(float value) {
        return UiHelper.dpFloat(context, value);
    }
}