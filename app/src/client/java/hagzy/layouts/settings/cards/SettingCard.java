package hagzy.layouts.settings.cards;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;
import com.bytepulse.hagzy.R;

import hagzy.layouts.settings.models.SettingItem;
import hagzy.layouts.settings.utils.SettingsCardAnimator;

public class SettingCard {

    private final Context context;
    private final SettingItem item;
    private CardView card;

    public SettingCard(Context context, SettingItem item) {
        this.context = context;
        this.item = item;
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
        card.setRadius(dp(24));
        card.setCardElevation(0);
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(16));

        // Icon
        ImageView icon = new ImageView(context);
        icon.setImageResource(item.iconRes);
        icon.setColorFilter(Color.parseColor("#1A1A1A"));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMarginEnd(dp(16));
        icon.setLayoutParams(iconParams);

        // Text Column
        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView titleText = new TextView(context);
        titleText.setText(item.title);
        titleText.setTextSize(15);
        titleText.setTypeface(ThemeManager.fontBold());
        titleText.setTextColor(Color.parseColor("#1A1A1A"));
        titleText.setTranslationY(-dpf(1.5f));

        TextView descText = new TextView(context);
        descText.setText(item.description);
        descText.setTextSize(13);
        descText.setTypeface(ThemeManager.fontSemiBold());
        descText.setTextColor(Color.parseColor("#999999"));
        descText.setTranslationY(-dpf(1f));
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        descParams.topMargin = dp(2);
        descText.setLayoutParams(descParams);

        textColumn.addView(titleText);
        textColumn.addView(descText);

        // Arrow
        ImageView arrow = new ImageView(context);
        arrow.setImageResource(R.drawable.chevron_right);
        arrow.setColorFilter(Color.parseColor("#CCCCCC"));
        arrow.setLayoutParams(new LinearLayout.LayoutParams(dp(16), dp(16)));

        content.addView(icon);
        content.addView(textColumn);
        content.addView(arrow);

        card.addView(content);

        // Apply Uber-style animation
        SettingsCardAnimator.applyAnimation(card, item.onClick);
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