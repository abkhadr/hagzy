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
 * FeaturesSection - قسم المميزات والخدمات
 */
public class FeaturesSection implements FieldSection {

    private Context context;
    private LinearLayout container;
    private LinearLayout featuresContainer;

    public FeaturesSection(Context context) {
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
        TextView title = UiHelper.createText(context, "المميزات والخدمات", 18, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        title.setLayoutParams(titleParams);
        container.addView(title);

        // Features Container
        featuresContainer = new LinearLayout(context);
        featuresContainer.setOrientation(LinearLayout.VERTICAL);
        container.addView(featuresContainer);
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(FieldData data) {
        featuresContainer.removeAllViews();

        if (data.features == null || data.features.isEmpty()) {
            TextView emptyText = UiHelper.createText(context,
                    "لا توجد مميزات محددة", 14, "#999999", 1);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, dp(16), 0, dp(16));
            featuresContainer.addView(emptyText);
            return;
        }

        // Create feature rows (2 columns)
        LinearLayout currentRow = null;
        for (int i = 0; i < data.features.size(); i++) {
            if (i % 2 == 0) {
                currentRow = createFeatureRow();
                featuresContainer.addView(currentRow);
            }

            String feature = data.features.get(i);
            currentRow.addView(createFeatureItem(feature));
        }
    }

    private LinearLayout createFeatureRow() {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(12);
        row.setLayoutParams(params);
        return row;
    }

    private View createFeatureItem(String feature) {
        LinearLayout item = new LinearLayout(context);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(16), dp(12), dp(16), dp(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(10));
        item.setBackground(bg);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        itemParams.setMarginEnd(dp(8));
        item.setLayoutParams(itemParams);

        // Icon
        String icon = getFeatureIcon(feature);
        TextView iconText = UiHelper.createText(context, icon, 18, "#000000", 1);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        iconParams.setMarginEnd(dp(10));
        iconText.setLayoutParams(iconParams);
        item.addView(iconText);

        // Label
        TextView label = UiHelper.createText(context,
                getFeatureLabel(feature), 14, "#000000", 1);
        item.addView(label);

        return item;
    }

    private String getFeatureIcon(String feature) {
        String lower = feature.toLowerCase();
        if (lower.contains("parking") || lower.contains("موقف")) return "🅿️";
        if (lower.contains("shower") || lower.contains("دش")) return "🚿";
        if (lower.contains("locker") || lower.contains("خزانة")) return "🔐";
        if (lower.contains("cafeteria") || lower.contains("كافيتيريا")) return "☕";
        if (lower.contains("wifi") || lower.contains("واي فاي")) return "📶";
        if (lower.contains("ac") || lower.contains("تكييف")) return "❄️";
        if (lower.contains("light") || lower.contains("إضاءة")) return "💡";
        if (lower.contains("water") || lower.contains("مياه")) return "💧";
        if (lower.contains("first") || lower.contains("إسعاف")) return "🏥";
        if (lower.contains("sound") || lower.contains("صوت")) return "🔊";
        return "✅";
    }

    private String getFeatureLabel(String feature) {
        String lower = feature.toLowerCase();
        if (lower.contains("parking")) return "موقف سيارات";
        if (lower.contains("shower")) return "دُش";
        if (lower.contains("locker")) return "خزائن";
        if (lower.contains("cafeteria")) return "كافيتيريا";
        if (lower.contains("wifi")) return "واي فاي";
        if (lower.contains("ac")) return "تكييف";
        if (lower.contains("light")) return "إضاءة";
        if (lower.contains("water")) return "مياه";
        if (lower.contains("first")) return "إسعافات أولية";
        if (lower.contains("sound")) return "نظام صوتي";
        return feature;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}