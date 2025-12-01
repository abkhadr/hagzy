package hagzy.layouts.profile.sections;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.profile.utils.PlayerDataParser.PlayerData;

/**
 * LevelSection - قسم المستوى والتقدم
 */
public class LevelSection implements ProfileSection {

    private Context context;
    private LinearLayout container;
    private TextView titleValue;
    private TextView xpLabel;
    private TextView levelText;
    private View progressFill;
    private TextView emptyMessage;

    public LevelSection(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(20), dp(24), dp(20));
        container.setBackgroundColor(Color.WHITE);

        // Section Title
        TextView sectionTitle = UiHelper.createText(context, "المستوى", 16, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        sectionTitle.setLayoutParams(titleParams);
        container.addView(sectionTitle);

        // Content Row
        LinearLayout contentRow = new LinearLayout(context);
        contentRow.setOrientation(LinearLayout.HORIZONTAL);
        contentRow.setGravity(Gravity.CENTER_VERTICAL);

        // Left Section
        LinearLayout leftSection = new LinearLayout(context);
        leftSection.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        leftSection.setLayoutParams(leftParams);

        titleValue = UiHelper.createText(context, "", 18, "#000000", 3);
        leftSection.addView(titleValue);

        xpLabel = UiHelper.createText(context, "", 13, "#666666", 1);
        LinearLayout.LayoutParams xpParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        xpParams.topMargin = dp(4);
        xpLabel.setLayoutParams(xpParams);
        leftSection.addView(xpLabel);

        contentRow.addView(leftSection);

        // Level Badge
        FrameLayout levelBadge = new FrameLayout(context);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(56), dp(56));
        levelBadge.setLayoutParams(badgeParams);

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setShape(GradientDrawable.OVAL);
        badgeBg.setColor(Color.parseColor("#000000"));
        levelBadge.setBackground(badgeBg);

        levelText = UiHelper.createText(context, "1", 24, "#FFFFFF", 3);
        levelText.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams levelTextParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        levelText.setLayoutParams(levelTextParams);
        levelBadge.addView(levelText);

        contentRow.addView(levelBadge);
        container.addView(contentRow);

        // Progress Bar
        container.addView(createProgressBar());

        // Empty Message (Hidden by default)
        emptyMessage = UiHelper.createText(context,
                "🎮 العب مباريات للحصول على XP وترتفع في المستويات!",
                13, "#FF9800", 1);
        emptyMessage.setGravity(Gravity.CENTER);
        emptyMessage.setPadding(dp(16), dp(12), dp(16), dp(12));
        emptyMessage.setVisibility(View.GONE);

        GradientDrawable msgBg = new GradientDrawable();
        msgBg.setColor(Color.parseColor("#FFF3E0"));
        msgBg.setCornerRadius(dp(8));
        emptyMessage.setBackground(msgBg);

        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        msgParams.topMargin = dp(12);
        emptyMessage.setLayoutParams(msgParams);
        container.addView(emptyMessage);
    }

    private View createProgressBar() {
        FrameLayout progressContainer = new FrameLayout(context);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(6)
        );
        progressParams.topMargin = dp(16);
        progressContainer.setLayoutParams(progressParams);

        // Background
        GradientDrawable progressBgDrawable = new GradientDrawable();
        progressBgDrawable.setColor(Color.parseColor("#F0F0F0"));
        progressBgDrawable.setCornerRadius(dp(3));

        View progressBg = new View(context);
        progressBg.setBackground(progressBgDrawable);
        FrameLayout.LayoutParams bgParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        progressBg.setLayoutParams(bgParams);
        progressContainer.addView(progressBg);

        // Fill
        GradientDrawable progressFillDrawable = new GradientDrawable();
        progressFillDrawable.setColor(Color.parseColor("#000000"));
        progressFillDrawable.setCornerRadius(dp(3));

        progressFill = new View(context);
        progressFill.setBackground(progressFillDrawable);
        FrameLayout.LayoutParams fillParams = new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        progressFill.setLayoutParams(fillParams);
        progressContainer.addView(progressFill);

        return progressContainer;
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(PlayerData data) {
        // Set level
        levelText.setText(String.valueOf(data.level));

        // Set title
        titleValue.setText(data.title);

        // Set XP
        xpLabel.setText(String.format("%d / %d XP", data.xp, data.xpToNextLevel));

        // Animate progress
        float progress = Math.min(1.0f, (float) data.xp / data.xpToNextLevel);
        animateProgress(progress);

        // Show empty message for new users
        if (!data.hasData || data.totalMatches == 0) {
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            emptyMessage.setVisibility(View.GONE);
        }
    }

    private void animateProgress(float progress) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int targetWidth = (int) (progress * (screenWidth - dp(48)));

        ValueAnimator animator = ValueAnimator.ofInt(0, targetWidth);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) progressFill.getLayoutParams();
            params.width = (int) animation.getAnimatedValue();
            progressFill.setLayoutParams(params);
        });
        animator.start();
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}