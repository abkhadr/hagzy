package hagzy.layouts.settings.cards;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.settings.models.ToggleItem;

public class ToggleCard {

    private final Context context;
    private final ToggleItem item;
    private CardView card;
    private LinearLayout toggle;
    private boolean isEnabled;

    public ToggleCard(Context context, ToggleItem item) {
        this.context = context;
        this.item = item;
        this.isEnabled = item.initialState;
        buildCard();
    }

    private void buildCard() {
        card = new CardView(context);
        card.setCardElevation(0);
        card.setRadius(dp(24));
        card.setCardBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.topMargin = dp(6);
        cardParams.bottomMargin = dp(6);
        card.setLayoutParams(cardParams);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(dp(20), dp(16), dp(20), dp(16));

//        // Icon
//        ImageView icon = new ImageView(context);
//        icon.setImageResource(item.iconRes);
//        icon.setColorFilter(Color.parseColor("#1A1A1A"));
//        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
//        iconParams.setMarginEnd(dp(16));
//        icon.setLayoutParams(iconParams);

        // Text Container
        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        textContainer.setLayoutParams(textParams);

        // Title
        TextView title = new TextView(context);
        title.setText(item.title);
        title.setTextSize(15);
        title.setTypeface(ThemeManager.fontBold());
        title.setTextColor(Color.parseColor("#1A1A1A"));
        title.setTranslationY(-dpf(1f));

        // Description
        TextView description = new TextView(context);
        description.setText(item.description);
        description.setTextSize(13);
        description.setTypeface(ThemeManager.fontSemiBold());
        description.setTextColor(Color.parseColor("#999999"));
        description.setTranslationY(-dpf(0.5f));
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        descParams.topMargin = dp(4);
        description.setLayoutParams(descParams);

        textContainer.addView(title);
        textContainer.addView(description);

        // Toggle Switch
        toggle = createToggle();

//        container.addView(icon);
        container.addView(textContainer);
        container.addView(toggle);

        card.addView(container);

        // Click listener
        card.setOnClickListener(v -> {
            isEnabled = !isEnabled;
            animateToggle(isEnabled);
            if (item.onToggle != null) {
                item.onToggle.accept(isEnabled);
            }
        });
    }

    private LinearLayout createToggle() {
        LinearLayout toggleContainer = new LinearLayout(context);
        toggleContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(
                dp(50),
                dp(30)
        );
        toggleContainer.setLayoutParams(toggleParams);

        // Background
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dp(15));
        background.setColor(isEnabled ? Color.parseColor("#4CAF50") : Color.parseColor("#E0E0E0"));
        toggleContainer.setBackground(background);

        // Thumb
        GradientDrawable thumb = new GradientDrawable();
        thumb.setShape(GradientDrawable.OVAL);
        thumb.setColor(Color.WHITE);

        LinearLayout thumbView = new LinearLayout(context);
        LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        thumbParams.setMargins(
                isEnabled ? dp(23) : dp(3),
                dp(3),
                dp(3),
                dp(3)
        );
        thumbView.setLayoutParams(thumbParams);
        thumbView.setBackground(thumb);

        toggleContainer.addView(thumbView);
        toggleContainer.setTag(thumbView); // Store reference for animation

        return toggleContainer;
    }

    private void animateToggle(boolean enabled) {
        LinearLayout thumbView = (LinearLayout) toggle.getTag();
        GradientDrawable background = (GradientDrawable) toggle.getBackground();

        // Animate thumb position
        LinearLayout.LayoutParams thumbParams = (LinearLayout.LayoutParams) thumbView.getLayoutParams();
        int startMargin = thumbParams.leftMargin;
        int endMargin = enabled ? dp(23) : dp(3);

        ValueAnimator positionAnimator = ValueAnimator.ofInt(startMargin, endMargin);
        positionAnimator.setDuration(200);
        positionAnimator.setInterpolator(new DecelerateInterpolator());
        positionAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            thumbParams.leftMargin = value;
            thumbView.setLayoutParams(thumbParams);
        });

        // Animate background color
        int startColor = enabled ? Color.parseColor("#E0E0E0") : Color.parseColor("#4CAF50");
        int endColor = enabled ? Color.parseColor("#4CAF50") : Color.parseColor("#E0E0E0");

        ValueAnimator colorAnimator = ValueAnimator.ofArgb(startColor, endColor);
        colorAnimator.setDuration(200);
        colorAnimator.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            background.setColor(color);
        });

        positionAnimator.start();
        colorAnimator.start();
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