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

import hagzy.layouts.field.components.FieldRating;
import hagzy.layouts.field.utils.FieldDataParser.FieldData;

/**
 * InfoSection - قسم المعلومات الأساسية
 */
public class InfoSection implements FieldSection {

    private Context context;
    private LinearLayout container;
    private FieldRating ratingView;
    private TextView descriptionText;
    private LinearLayout contactsContainer;
    private LinearLayout detailsContainer;

    public InfoSection(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(Color.WHITE);
        container.setPadding(dp(24), dp(24), dp(24), dp(24));

        // Rating
        ratingView = new FieldRating(context);
        LinearLayout.LayoutParams ratingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ratingParams.bottomMargin = dp(20);
        ratingView.getView().setLayoutParams(ratingParams);
        container.addView(ratingView.getView());

        // Description Section
        TextView descTitle = UiHelper.createText(context, "نبذة عن الملعب", 18, "#000000", 3);
        LinearLayout.LayoutParams descTitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        descTitleParams.bottomMargin = dp(12);
        descTitle.setLayoutParams(descTitleParams);
        container.addView(descTitle);

        descriptionText = UiHelper.createText(context, "", 15, "#666666", 1);
        descriptionText.setLineSpacing(dp(4), 1.0f);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        descParams.bottomMargin = dp(24);
        descriptionText.setLayoutParams(descParams);
        container.addView(descriptionText);

        // Details Section
        TextView detailsTitle = UiHelper.createText(context, "تفاصيل الملعب", 18, "#000000", 3);
        LinearLayout.LayoutParams detailsTitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        detailsTitleParams.bottomMargin = dp(12);
        detailsTitle.setLayoutParams(detailsTitleParams);
        container.addView(detailsTitle);

        detailsContainer = new LinearLayout(context);
        detailsContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        detailsParams.bottomMargin = dp(24);
        detailsContainer.setLayoutParams(detailsParams);
        container.addView(detailsContainer);

        // Contact Section
        TextView contactTitle = UiHelper.createText(context, "معلومات التواصل", 18, "#000000", 3);
        LinearLayout.LayoutParams contactTitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        contactTitleParams.bottomMargin = dp(12);
        contactTitle.setLayoutParams(contactTitleParams);
        container.addView(contactTitle);

        contactsContainer = new LinearLayout(context);
        contactsContainer.setOrientation(LinearLayout.VERTICAL);
        container.addView(contactsContainer);
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(FieldData data) {
        // Set rating
        ratingView.setRating(data.rating, data.totalReviews);

        // Set description
        if (data.description != null && !data.description.isEmpty()) {
            descriptionText.setText(data.description);
        } else {
            descriptionText.setText("لا يوجد وصف متاح");
        }

        // Set details
        detailsContainer.removeAllViews();
        detailsContainer.addView(createDetailRow("⏰", "ساعات العمل",
                data.openTime + " - " + data.closeTime));
        detailsContainer.addView(createDetailRow("👥", "السعة",
                data.minPlayers + " - " + data.maxPlayers + " لاعب"));
        detailsContainer.addView(createDetailRow("🏟️", "نوع الملعب",
                getFieldTypeName(data.fieldType)));
        detailsContainer.addView(createDetailRow("🌱", "نوع الأرضية",
                getSurfaceTypeName(data.surfaceType)));

        // Set contacts
        contactsContainer.removeAllViews();
        if (data.phone != null && !data.phone.isEmpty()) {
            contactsContainer.addView(createContactButton(
                    R.drawable.bars_3, "الاتصال", data.phone,
                    () -> callPhone(data.phone)
            ));
        }
        if (data.email != null && !data.email.isEmpty()) {
            contactsContainer.addView(createContactButton(
                    R.drawable.bars_3, "البريد الإلكتروني", data.email,
                    () -> sendEmail(data.email)
            ));
        }
        if (data.website != null && !data.website.isEmpty()) {
            contactsContainer.addView(createContactButton(
                    R.drawable.bars_3, "الموقع الإلكتروني", data.website,
                    () -> openWebsite(data.website)
            ));
        }
    }

    private View createDetailRow(String icon, String label, String value) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(12));
        row.setBackground(bg);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(8);
        row.setLayoutParams(rowParams);

        // Icon
        TextView iconText = UiHelper.createText(context, icon, 20, "#000000", 1);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        iconParams.setMarginEnd(dp(12));
        iconText.setLayoutParams(iconParams);
        row.addView(iconText);

        // Label & Value
        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        textContainer.setLayoutParams(textParams);

        TextView labelText = UiHelper.createText(context, label, 13, "#999999", 1);
        textContainer.addView(labelText);

        TextView valueText = UiHelper.createText(context, value, 15, "#000000", 3);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        valueParams.topMargin = dp(2);
        valueText.setLayoutParams(valueParams);
        textContainer.addView(valueText);

        row.addView(textContainer);

        return row;
    }

    private View createContactButton(int iconRes, String label, String value, Runnable action) {
        LinearLayout button = new LinearLayout(context);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(dp(16), dp(14), dp(16), dp(14));
        button.setClickable(true);
        button.setFocusable(true);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(2), Color.parseColor("#E0E0E0"));
        button.setBackground(bg);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnParams.bottomMargin = dp(8);
        button.setLayoutParams(btnParams);

        // Icon
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Color.parseColor("#000000"));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp(24), dp(24)
        );
        iconParams.setMarginEnd(dp(12));
        icon.setLayoutParams(iconParams);
        button.addView(icon);

        // Text
        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        textContainer.setLayoutParams(textParams);

        TextView labelText = UiHelper.createText(context, label, 13, "#999999", 1);
        textContainer.addView(labelText);

        TextView valueText = UiHelper.createText(context, value, 15, "#000000", 1);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        valueParams.topMargin = dp(2);
        valueText.setLayoutParams(valueParams);
        textContainer.addView(valueText);

        button.addView(textContainer);

        // Arrow
        ImageView arrow = new ImageView(context);
        arrow.setImageResource(R.drawable.arrow_left);
        arrow.setColorFilter(Color.parseColor("#CCCCCC"));
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(
                dp(20), dp(20)
        );
        arrow.setLayoutParams(arrowParams);
        button.addView(arrow);

        button.setOnClickListener(v -> action.run());

        return button;
    }

    private void callPhone(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        context.startActivity(intent);
    }

    private void sendEmail(String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        context.startActivity(intent);
    }

    private void openWebsite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    private String getFieldTypeName(String type) {
        if (type == null) return "غير محدد";
        switch (type.toLowerCase()) {
            case "indoor": return "داخلي";
            case "outdoor": return "خارجي";
            case "5v5": return "خماسي";
            case "7v7": return "سباعي";
            case "11v11": return "كامل";
            default: return type;
        }
    }

    private String getSurfaceTypeName(String type) {
        if (type == null) return "غير محدد";
        switch (type.toLowerCase()) {
            case "artificial": return "نجيل صناعي";
            case "natural": return "نجيل طبيعي";
            case "concrete": return "خرسانة";
            case "rubber": return "مطاط";
            default: return type;
        }
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}