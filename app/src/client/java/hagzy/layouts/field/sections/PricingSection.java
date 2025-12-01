package hagzy.layouts.field.sections;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.field.utils.FieldDataParser.FieldData;

/**
 * PricingSection - قسم الأسعار والباقات
 */
public class PricingSection implements FieldSection {

    private Context context;
    private LinearLayout container;
    private LinearLayout pricesContainer;

    public PricingSection(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(Color.WHITE);
        container.setPadding(dp(24), dp(24), dp(24), dp(24));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(1);
        container.setLayoutParams(params);

        // Title
        TextView title = UiHelper.createText(context, "الأسعار", 18, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        title.setLayoutParams(titleParams);
        container.addView(title);

        // Prices Container
        pricesContainer = new LinearLayout(context);
        pricesContainer.setOrientation(LinearLayout.VERTICAL);
        container.addView(pricesContainer);
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(FieldData data) {
        pricesContainer.removeAllViews();

        String currency = data.currency != null ? data.currency : "ريال";

        // Morning Price
        if (data.morningPrice > 0) {
            pricesContainer.addView(createPriceCard(
                    "🌅 الفترة الصباحية",
                    "6:00 ص - 12:00 م",
                    data.morningPrice,
                    currency,
                    "#FFF3E0"
            ));
        }

        // Afternoon Price
        if (data.afternoonPrice > 0) {
            pricesContainer.addView(createPriceCard(
                    "☀️ الفترة المسائية",
                    "12:00 م - 5:00 م",
                    data.afternoonPrice,
                    currency,
                    "#E3F2FD"
            ));
        }

        // Evening Price
        if (data.eveningPrice > 0) {
            pricesContainer.addView(createPriceCard(
                    "🌙 الفترة الليلية",
                    "5:00 م - 12:00 ص",
                    data.eveningPrice,
                    currency,
                    "#F3E5F5"
            ));
        }

        // Default hourly rate if no time slots
        if (data.morningPrice == 0 && data.afternoonPrice == 0 && data.eveningPrice == 0) {
            pricesContainer.addView(createPriceCard(
                    "💰 السعر بالساعة",
                    "جميع الأوقات",
                    data.hourlyRate,
                    currency,
                    "#E8F5E9"
            ));
        }
    }

    private View createPriceCard(String title, String time, int price, String currency, String bgColor) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(20), dp(16), dp(20), dp(16));
        card.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(dp(12));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        // Left: Info
        LinearLayout infoContainer = new LinearLayout(context);
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        infoContainer.setLayoutParams(infoParams);

        TextView titleText = UiHelper.createText(context, title, 16, "#000000", 3);
        infoContainer.addView(titleText);

        TextView timeText = UiHelper.createText(context, time, 13, "#666666", 1);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        timeParams.topMargin = dp(4);
        timeText.setLayoutParams(timeParams);
        infoContainer.addView(timeText);

        card.addView(infoContainer);

        // Right: Price
        LinearLayout priceContainer = new LinearLayout(context);
        priceContainer.setOrientation(LinearLayout.VERTICAL);
        priceContainer.setGravity(Gravity.END);

        TextView priceText = UiHelper.createText(context, price + "", 24, "#000000", 3);
        priceContainer.addView(priceText);

        TextView currencyText = UiHelper.createText(context, currency + "/ساعة", 13, "#666666", 1);
        LinearLayout.LayoutParams currencyParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        currencyParams.topMargin = dp(2);
        currencyText.setLayoutParams(currencyParams);
        priceContainer.addView(currencyText);

        card.addView(priceContainer);

        return card;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}