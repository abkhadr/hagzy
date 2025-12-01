package hagzy.layouts.field.sections;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.field.utils.FieldDataParser.FieldData;

/**
 * HeroSection - قسم الصورة الرئيسية والعنوان
 */
public class HeroSection implements FieldSection {

    private Context context;
    private FrameLayout container;
    private ImageView coverImage;
    private TextView nameText;
    private TextView locationText;
    private LinearLayout badgesContainer;

    public HeroSection(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(300)
        );
        container.setLayoutParams(params);

        // Cover Image
        coverImage = new ImageView(context);
        coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        coverImage.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        container.addView(coverImage);

        // Gradient Overlay
        View gradient = createGradientOverlay();
        container.addView(gradient);

        // Bottom Info Container
        LinearLayout infoContainer = new LinearLayout(context);
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        infoContainer.setPadding(dp(24), dp(24), dp(24), dp(24));

        FrameLayout.LayoutParams infoParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        infoParams.gravity = Gravity.BOTTOM;
        infoContainer.setLayoutParams(infoParams);

        // Badges (Featured, etc.)
        badgesContainer = new LinearLayout(context);
        badgesContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams badgesParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        badgesParams.bottomMargin = dp(12);
        badgesContainer.setLayoutParams(badgesParams);
        infoContainer.addView(badgesContainer);

        // Name
        nameText = UiHelper.createText(context, "", 28, "#FFFFFF", 3);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        nameParams.bottomMargin = dp(8);
        nameText.setLayoutParams(nameParams);
        infoContainer.addView(nameText);

        // Location
        locationText = UiHelper.createText(context, "", 15, "#FFFFFF", 1);
        locationText.setAlpha(0.9f);
        infoContainer.addView(locationText);

        container.addView(infoContainer);
    }

    private View createGradientOverlay() {
        View overlay = new View(context);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        Color.parseColor("#00000000"),
                        Color.parseColor("#CC000000")
                }
        );
        overlay.setBackground(gradient);
        overlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return overlay;
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(FieldData data) {
        // Load cover image
        if (data.coverImage != null && !data.coverImage.isEmpty()) {
            Glide.with(context)
                    .load(data.coverImage)
                    .centerCrop()
                    .into(coverImage);
        }

        // Set name
        nameText.setText(data.name);

        // Set location
        String location = data.district + ", " + data.city;
        locationText.setText(location);

        // Add badges
        badgesContainer.removeAllViews();
        if (data.isFeatured) {
            badgesContainer.addView(createBadge("⭐ مميز", "#FFD700"));
        }
        if (data.rating >= 4.5) {
            badgesContainer.addView(createBadge("🏆 الأعلى تقييماً", "#4CAF50"));
        }
    }

    private View createBadge(String text, String color) {
        TextView badge = UiHelper.createText(context, text, 12, "#FFFFFF", 3);
        badge.setPadding(dp(12), dp(6), dp(12), dp(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(color));
        bg.setCornerRadius(dp(16));
        badge.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(dp(8));
        badge.setLayoutParams(params);

        return badge;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}