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
 * SkillsSection - قسم المهارات
 */
public class SkillsSection implements ProfileSection {

    private Context context;
    private LinearLayout container;

    public SkillsSection(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(20), dp(24), dp(20));
        container.setBackgroundColor(Color.WHITE);

        // Section Title
        TextView sectionTitle = UiHelper.createText(context, "المهارات", 16, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        sectionTitle.setLayoutParams(titleParams);
        container.addView(sectionTitle);
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(PlayerData data) {
        // Remove old skills (keep only title)
        while (container.getChildCount() > 1) {
            container.removeViewAt(1);
        }

        // Add skills
        container.addView(createSkillRow("القوة", data.strength));
        container.addView(createSkillRow("السرعة", data.speed));
        container.addView(createSkillRow("التحمل", data.stamina));
        container.addView(createSkillRow("المهارة", data.technique));
        container.addView(createSkillRow("العمل الجماعي", data.teamwork));
    }

    private LinearLayout createSkillRow(String name, int value) {
        LinearLayout skillContainer = new LinearLayout(context);
        skillContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.bottomMargin = dp(16);
        skillContainer.setLayoutParams(containerParams);

        // Label Row
        LinearLayout labelRow = new LinearLayout(context);
        labelRow.setOrientation(LinearLayout.HORIZONTAL);
        labelRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView nameText = UiHelper.createText(context, name, 14, "#000000", 1);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        nameText.setLayoutParams(nameParams);
        labelRow.addView(nameText);

        TextView valueText = UiHelper.createText(context, value + "/100", 14, "#666666", 1);
        labelRow.addView(valueText);

        skillContainer.addView(labelRow);

        // Progress Bar
        skillContainer.addView(createProgressBar(value));

        return skillContainer;
    }

    private View createProgressBar(int value) {
        FrameLayout progressContainer = new FrameLayout(context);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(6)
        );
        progressParams.topMargin = dp(8);
        progressContainer.setLayoutParams(progressParams);

        // Background
        GradientDrawable bgDrawable = new GradientDrawable();
        bgDrawable.setColor(Color.parseColor("#F0F0F0"));
        bgDrawable.setCornerRadius(dp(3));

        View progressBg = new View(context);
        progressBg.setBackground(bgDrawable);
        FrameLayout.LayoutParams bgParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        progressBg.setLayoutParams(bgParams);
        progressContainer.addView(progressBg);

        // Fill
        GradientDrawable fillDrawable = new GradientDrawable();
        fillDrawable.setColor(Color.parseColor("#000000"));
        fillDrawable.setCornerRadius(dp(3));

        View progressFill = new View(context);
        progressFill.setBackground(fillDrawable);
        FrameLayout.LayoutParams fillParams = new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        progressFill.setLayoutParams(fillParams);
        progressContainer.addView(progressFill);

        // Animate
        animateProgress(progressFill, value / 100f);

        return progressContainer;
    }

    private void animateProgress(View view, float progress) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int targetWidth = (int) (progress * (screenWidth - dp(48)));

        ValueAnimator animator = ValueAnimator.ofInt(0, targetWidth);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            params.width = (int) animation.getAnimatedValue();
            view.setLayoutParams(params);
        });
        animator.start();
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}