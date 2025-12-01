package hagzy.layouts.field.components;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.UiHelper;

/**
 * FieldRating - عرض التقييم والنجوم
 */
public class FieldRating {

    private Context context;
    private LinearLayout container;
    private TextView ratingText;
    private TextView starsText;
    private TextView reviewsText;

    public FieldRating(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);

        // Stars
        starsText = UiHelper.createText(context, "", 20, "#FFD700", 1);
        LinearLayout.LayoutParams starsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        starsParams.setMarginEnd(dp(8));
        starsText.setLayoutParams(starsParams);
        container.addView(starsText);

        // Rating Value
        ratingText = UiHelper.createText(context, "0.0", 20, "#000000", 3);
        LinearLayout.LayoutParams ratingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ratingParams.setMarginEnd(dp(8));
        ratingText.setLayoutParams(ratingParams);
        container.addView(ratingText);

        // Reviews Count
        reviewsText = UiHelper.createText(context, "(0)", 16, "#999999", 1);
        container.addView(reviewsText);
    }

    public View getView() {
        return container;
    }

    public void setRating(double rating, int totalReviews) {
        // Set rating value
        ratingText.setText(String.format("%.1f", rating));

        // Set stars
        starsText.setText(getStarsString(rating));

        // Set reviews count
        reviewsText.setText(String.format("(%d)", totalReviews));
    }

    private String getStarsString(double rating) {
        int fullStars = (int) rating;
        boolean hasHalfStar = (rating - fullStars) >= 0.5;
        int emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);

        StringBuilder stars = new StringBuilder();

        // Full stars
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }

        // Half star
        if (hasHalfStar) {
            stars.append("⯨");
        }

        // Empty stars
        for (int i = 0; i < emptyStars; i++) {
            stars.append("☆");
        }

        return stars.toString();
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}