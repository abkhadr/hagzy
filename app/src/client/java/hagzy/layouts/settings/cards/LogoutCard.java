package hagzy.layouts.settings.cards;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

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

import hagzy.layouts.settings.utils.SettingsCardAnimator;

public class LogoutCard {

    private final Context context;
    private final Runnable onLogout;
    private CardView card;

    public LogoutCard(Context context, Runnable onLogout) {
        this.context = context;
        this.onLogout = onLogout;
        buildCard();
    }

    private void buildCard() {
        card = new CardView(context);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(dp(20), 0, dp(20), 0);
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
        icon.setImageResource(R.drawable.cog_8);
        icon.setColorFilter(Color.parseColor("#DC2626"));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMarginEnd(dp(16));
        icon.setLayoutParams(iconParams);

        // Title
        TextView titleText = new TextView(context);
        titleText.setText(t("settings.logout"));
        titleText.setTextSize(15);
        titleText.setTypeface(ThemeManager.fontBold());
        titleText.setTextColor(Color.parseColor("#DC2626"));
        titleText.setTranslationY(-dpf(1.5f));
        titleText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // Arrow
        ImageView arrow = new ImageView(context);
        arrow.setImageResource(R.drawable.chevron_right);
        arrow.setColorFilter(Color.parseColor("#DC2626"));
        arrow.setLayoutParams(new LinearLayout.LayoutParams(dp(16), dp(16)));

        content.addView(icon);
        content.addView(titleText);
        content.addView(arrow);

        card.addView(content);

        // Apply animation
        SettingsCardAnimator.applyAnimation(card, onLogout);
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