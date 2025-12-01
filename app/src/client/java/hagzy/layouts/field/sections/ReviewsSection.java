package hagzy.layouts.field.sections;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.field.utils.FieldDataParser.FieldData;

/**
 * ReviewsSection - قسم التقييمات والمراجعات
 */
public class ReviewsSection implements FieldSection {

    private Context context;
    private LinearLayout container;
    private TextView reviewsText;

    public ReviewsSection(Context context) {
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
        TextView title = UiHelper.createText(context, "التقييمات", 18, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        title.setLayoutParams(titleParams);
        container.addView(title);

        // Reviews placeholder
        reviewsText = UiHelper.createText(context,
                "لا توجد مراجعات بعد. كن أول من يقيّم هذا الملعب!",
                14, "#999999", 1);
        reviewsText.setGravity(Gravity.CENTER);
        reviewsText.setPadding(0, dp(24), 0, dp(24));
        container.addView(reviewsText);
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(FieldData data) {
        // TODO: Load actual reviews from Firestore
        // For now, just show placeholder
        if (data.totalReviews > 0) {
            reviewsText.setText(String.format("يوجد %d تقييم لهذا الملعب", data.totalReviews));
        }
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}