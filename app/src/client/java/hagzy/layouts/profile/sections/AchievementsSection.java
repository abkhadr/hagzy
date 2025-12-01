package hagzy.layouts.profile.sections;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.profile.utils.PlayerDataParser.PlayerData;
import hagzy.layouts.profile.utils.PlayerDataParser.Achievement;

/**
 * AchievementsSection - قسم الإنجازات
 */
public class AchievementsSection implements ProfileSection {

    private Context context;
    private LinearLayout container;
    private LinearLayout achievementsContainer;

    public AchievementsSection(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(20), dp(24), dp(20));
        container.setBackgroundColor(Color.WHITE);

        // Section Title
        TextView sectionTitle = UiHelper.createText(context, "الإنجازات", 16, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        sectionTitle.setLayoutParams(titleParams);
        container.addView(sectionTitle);

        // Achievements Container
        achievementsContainer = new LinearLayout(context);
        achievementsContainer.setOrientation(LinearLayout.VERTICAL);
        container.addView(achievementsContainer);
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(PlayerData data) {
        achievementsContainer.removeAllViews();

        if (!data.hasData || data.achievements == null || data.achievements.isEmpty()) {
            // رسالة للمستخدمين الجدد
            achievementsContainer.addView(createEmptyMessage(
                    "🏆 لا يوجد إنجازات بعد\n\nابدأ اللعب واحصل على إنجازاتك الأولى!",
                    "#9C27B0",
                    "#F3E5F5"
            ));
        } else {
            // عرض الإنجازات المفتوحة
            boolean hasUnlocked = false;
            for (Achievement achievement : data.achievements) {
                if (achievement.unlocked) {
                    achievementsContainer.addView(createAchievementItem(achievement));
                    hasUnlocked = true;
                }
            }

            if (!hasUnlocked) {
                achievementsContainer.addView(createEmptyMessage(
                        "🔒 جميع الإنجازات مقفلة\n\nالعب المزيد لفتح الإنجازات!",
                        "#9C27B0",
                        "#F3E5F5"
                ));
            }
        }
    }

    private View createEmptyMessage(String text, String textColor, String bgColor) {
        TextView message = UiHelper.createText(context, text, 13, textColor, 1);
        message.setGravity(Gravity.CENTER);
        message.setPadding(dp(16), dp(20), dp(16), dp(20));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(dp(12));
        message.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        message.setLayoutParams(params);

        return message;
    }

    private View createAchievementItem(Achievement achievement) {
        LinearLayout item = new LinearLayout(context);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(dp(16), dp(14), dp(16), dp(14));
        item.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        itemParams.bottomMargin = dp(8);
        item.setLayoutParams(itemParams);

        GradientDrawable itemBg = new GradientDrawable();
        itemBg.setColor(Color.parseColor("#FAFAFA"));
        itemBg.setCornerRadius(dp(12));
        item.setBackground(itemBg);

        // Icon Container
        FrameLayout iconContainer = new FrameLayout(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        iconParams.setMarginEnd(dp(12));
        iconContainer.setLayoutParams(iconParams);

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        iconBg.setColor(Color.parseColor("#000000"));
        iconContainer.setBackground(iconBg);

        TextView iconText = UiHelper.createText(context, "★", 18, "#FFFFFF", 3);
        iconText.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams iconTextParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        iconText.setLayoutParams(iconTextParams);
        iconContainer.addView(iconText);

        item.addView(iconContainer);

        // Text Container
        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        textParams.leftMargin = dp(12);
        textContainer.setLayoutParams(textParams);

        TextView nameText = UiHelper.createText(context, achievement.name, 14, "#000000", 3);
        textContainer.addView(nameText);

        TextView descText = UiHelper.createText(context, achievement.description, 12, "#666666", 1);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        descParams.topMargin = dp(2);
        descText.setLayoutParams(descParams);
        textContainer.addView(descText);

        item.addView(textContainer);

        return item;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}