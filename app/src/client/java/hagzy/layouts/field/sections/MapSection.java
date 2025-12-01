package hagzy.layouts.field.sections;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.field.utils.FieldDataParser.FieldData;

/**
 * MapSection - قسم الموقع والخريطة
 */
public class MapSection implements FieldSection {

    private Context context;
    private LinearLayout container;
    private TextView addressText;
    private double latitude;
    private double longitude;

    public MapSection(Context context) {
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
        TextView title = UiHelper.createText(context, "الموقع", 18, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        title.setLayoutParams(titleParams);
        container.addView(title);

        // Address
        addressText = UiHelper.createText(context, "", 15, "#666666", 1);
        LinearLayout.LayoutParams addressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        addressParams.bottomMargin = dp(16);
        addressText.setLayoutParams(addressParams);
        container.addView(addressText);

        // Open in Maps Button
        container.addView(createMapButton());

        // Map Placeholder
        container.addView(createMapPlaceholder());
    }

    private View createMapButton() {
        LinearLayout button = new LinearLayout(context);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(20), dp(14), dp(20), dp(14));
        button.setClickable(true);
        button.setFocusable(true);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#000000"));
        bg.setCornerRadius(dp(12));
        button.setBackground(bg);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnParams.bottomMargin = dp(16);
        button.setLayoutParams(btnParams);

        // Icon
        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.bars_3);
        icon.setColorFilter(Color.WHITE);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp(20), dp(20)
        );
        iconParams.setMarginEnd(dp(8));
        icon.setLayoutParams(iconParams);
        button.addView(icon);

        // Text
        TextView text = UiHelper.createText(context, "فتح في خرائط جوجل", 16, "#FFFFFF", 3);
        button.addView(text);

        button.setOnClickListener(v -> openInGoogleMaps());

        return button;
    }

    private View createMapPlaceholder() {
        LinearLayout placeholder = new LinearLayout(context);
        placeholder.setOrientation(LinearLayout.VERTICAL);
        placeholder.setGravity(Gravity.CENTER);
        placeholder.setPadding(0, dp(40), 0, dp(40));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(12));
        placeholder.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(200)
        );
        placeholder.setLayoutParams(params);

        TextView iconText = UiHelper.createText(context, "🗺️", 48, "#CCCCCC", 1);
        placeholder.addView(iconText);

        TextView text = UiHelper.createText(context, "معاينة الخريطة", 14, "#999999", 1);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = dp(8);
        text.setLayoutParams(textParams);
        placeholder.addView(text);

        placeholder.setOnClickListener(v -> openInGoogleMaps());

        return placeholder;
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(FieldData data) {
        // Set address
        String fullAddress = data.address + ", " + data.district + ", " + data.city;
        addressText.setText(fullAddress);

        // Store coordinates
        this.latitude = data.latitude;
        this.longitude = data.longitude;
    }

    private void openInGoogleMaps() {
        if (latitude == 0 && longitude == 0) {
            return;
        }

        String uri = String.format("geo:%f,%f?q=%f,%f", latitude, longitude, latitude, longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            // Fallback to browser
            String url = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f",
                    latitude, longitude);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(browserIntent);
        }
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}