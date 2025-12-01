package hagzy.layouts.profile.sections;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.profile.utils.PlayerDataParser.PlayerData;

/**
 * StatsSection - قسم الإحصائيات
 */
public class StatsSection implements ProfileSection {

    private Context context;
    private LinearLayout container;
    private LinearLayout statsContainer;
    private TextView emptyMessage;

    public StatsSection(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(20), dp(24), dp(20));
        container.setBackgroundColor(Color.WHITE);

        // Section Title
        TextView sectionTitle = UiHelper.createText(context, "الإحصائيات", 16, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        sectionTitle.setLayoutParams(titleParams);
        container.addView(sectionTitle);

        // Stats Container
        statsContainer = new LinearLayout(context);
        statsContainer.setOrientation(LinearLayout.VERTICAL);
        container.addView(statsContainer);

        // Empty Message
        emptyMessage = UiHelper.createText(context,
                "⚽ ابدأ اللعب لبناء سجلك الإحصائي!",
                13, "#2196F3", 1);
        emptyMessage.setGravity(Gravity.CENTER);
        emptyMessage.setPadding(dp(16), dp(12), dp(16), dp(12));
        emptyMessage.setVisibility(View.GONE);

        GradientDrawable msgBg = new GradientDrawable();
        msgBg.setColor(Color.parseColor("#E3F2FD"));
        msgBg.setCornerRadius(dp(8));
        emptyMessage.setBackground(msgBg);

        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        msgParams.topMargin = dp(8);
        emptyMessage.setLayoutParams(msgParams);
        container.addView(emptyMessage);
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(PlayerData data) {
        statsContainer.removeAllViews();

        // Show empty message for new users
        if (!data.hasData || data.totalMatches == 0) {
            emptyMessage.setVisibility(View.VISIBLE);

            // Still show zeros
            statsContainer.addView(createStatsRow("المباريات", "0", "الانتصارات", "0"));
            statsContainer.addView(createStatsRow("التعادلات", "0", "الخسائر", "0"));
            statsContainer.addView(createStatsRow("الأهداف", "0", "التمريرات", "0"));

            // Divider
            statsContainer.addView(createDivider());

            // Win Rate
            statsContainer.addView(createWinRateRow(0.0));
        } else {
            emptyMessage.setVisibility(View.GONE);

            // Show actual stats
            statsContainer.addView(createStatsRow(
                    "المباريات", String.valueOf(data.totalMatches),
                    "الانتصارات", String.valueOf(data.wins)
            ));
            statsContainer.addView(createStatsRow(
                    "التعادلات", String.valueOf(data.draws),
                    "الخسائر", String.valueOf(data.losses)
            ));
            statsContainer.addView(createStatsRow(
                    "الأهداف", String.valueOf(data.goals),
                    "التمريرات", String.valueOf(data.assists)
            ));

            // Divider
            statsContainer.addView(createDivider());

            // Win Rate
            statsContainer.addView(createWinRateRow(data.winRate));
        }
    }

    private LinearLayout createStatsRow(String label1, String value1, String label2, String value2) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(12);
        row.setLayoutParams(rowParams);

        // Left Item
        LinearLayout leftItem = new LinearLayout(context);
        leftItem.setOrientation(LinearLayout.HORIZONTAL);
        leftItem.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        leftItem.setLayoutParams(leftParams);

        TextView leftLabel = UiHelper.createText(context, label1, 14, "#666666", 1);
        LinearLayout.LayoutParams leftLabelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        leftLabel.setLayoutParams(leftLabelParams);
        leftItem.addView(leftLabel);

        TextView leftValue = UiHelper.createText(context, value1, 14, "#000000", 3);
        leftItem.addView(leftValue);

        row.addView(leftItem);

        // Spacer
        View spacer = new View(context);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                dp(16), ViewGroup.LayoutParams.MATCH_PARENT
        );
        spacer.setLayoutParams(spacerParams);
        row.addView(spacer);

        // Right Item
        LinearLayout rightItem = new LinearLayout(context);
        rightItem.setOrientation(LinearLayout.HORIZONTAL);
        rightItem.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        rightItem.setLayoutParams(rightParams);

        TextView rightLabel = UiHelper.createText(context, label2, 14, "#666666", 1);
        LinearLayout.LayoutParams rightLabelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        rightLabel.setLayoutParams(rightLabelParams);
        rightItem.addView(rightLabel);

        TextView rightValue = UiHelper.createText(context, value2, 14, "#000000", 3);
        rightItem.addView(rightValue);

        row.addView(rightItem);

        return row;
    }

    private View createDivider() {
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#F0F0F0"));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)
        );
        dividerParams.setMargins(0, dp(12), 0, dp(12));
        divider.setLayoutParams(dividerParams);
        return divider;
    }

    private LinearLayout createWinRateRow(double winRate) {
        LinearLayout winRateRow = new LinearLayout(context);
        winRateRow.setOrientation(LinearLayout.HORIZONTAL);
        winRateRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView winRateLabel = UiHelper.createText(context, "نسبة الفوز", 14, "#666666", 1);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        winRateLabel.setLayoutParams(labelParams);
        winRateRow.addView(winRateLabel);

        TextView winRateValue = UiHelper.createText(context,
                String.format("%.1f%%", winRate), 16, "#000000", 3);
        winRateRow.addView(winRateValue);

        return winRateRow;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}