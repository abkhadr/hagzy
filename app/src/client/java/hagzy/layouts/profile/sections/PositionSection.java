package hagzy.layouts.profile.sections;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.profile.utils.PlayerDataParser.PlayerData;

/**
 * LevelSection - قسم المستوى والتقدم
 */
public class PositionSection implements ProfileSection {

    private Context context;
    private LinearLayout container;
    private TextView positionText;
    private TextView emptyMessage;

    public PositionSection(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(20), dp(24), dp(20));
        container.setBackgroundColor(Color.WHITE);

        // Section Title
        TextView sectionTitle = UiHelper.createText(context, "المركز", 16, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        sectionTitle.setLayoutParams(titleParams);
        container.addView(sectionTitle);

        // Position Badge
        positionText = UiHelper.createText(context, "", 14, "#000000", 3);
        LinearLayout.LayoutParams posParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        posParams.topMargin = dp(4);
        positionText.setLayoutParams(posParams);
        positionText.setPadding(dp(12), dp(4), dp(12), dp(4));
        positionText.setAllCaps(true);
        GradientDrawable positionBg = new GradientDrawable();
        positionBg.setColor(Color.parseColor("#1A000000"));
        positionBg.setCornerRadius(dp(999));
        positionText.setBackground(positionBg);
        container.addView(positionText);
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(PlayerData data) {
        // Set position
        String positionName = getPositionName(data.preferredPosition);
        positionText.setText(positionName);
    }

    private String getPositionName(String position) {
        if (position == null || position.isEmpty()) return "غير محدد";

        switch (position.toLowerCase()) {
            case "goalkeeper": return "حارس مرمى";
            case "defender": return "مدافع";
            case "midfielder": return "خط وسط";
            case "forward": return "مهاجم";
            default: return "غير محدد";
        }
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}