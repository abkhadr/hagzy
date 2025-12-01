package hagzy.layouts.main.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.bytepulse.hagzy.helpers.UiHelper;
import java.util.ArrayList;
import java.util.List;
import hagzy.layouts.main.models.TabData;
import hagzy.layouts.main.tabs.TabIndicator;
import hagzy.layouts.main.tabs.TabItem;
import static com.bytepulse.hagzy.helpers.TranslationManager.isRTL;

public class TabsContainer {

    private Context context;
    private FrameLayout container;
    private LinearLayout tabsLayout;
    private TabIndicator indicator;
    private List<TabItem> tabs = new ArrayList<>();

    private OnTabSelectedListener listener;

    public TabsContainer(Context context) {
        this.context = context;
        buildContainer();
    }

    private void buildContainer() {
        container = new FrameLayout(context);
        container.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(56)));
        container.setPadding(dp(16), dp(6), dp(16), dp(6));
        container.setBackgroundColor(Color.WHITE);

        // Background
        LinearLayout background = createBackground();
        container.addView(background);

        // Indicator
        indicator = new TabIndicator(context);
        container.addView(indicator.getView());

        // Tabs Layout
        tabsLayout = new LinearLayout(context);
        tabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabsLayout.setGravity(Gravity.CENTER);
        tabsLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

        container.addView(tabsLayout);
    }

    private LinearLayout createBackground() {
        LinearLayout background = new LinearLayout(context);
        background.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        background.setClipToOutline(true);

        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor("#F5F5F5"));
        shape.setCornerRadius(dp(56));
        background.setBackground(shape);

        return background;
    }

    /**
     * إضافة تبويبات
     */
    public void setTabs(List<TabData> tabsData) {
        tabs.clear();
        tabsLayout.removeAllViews();

        for (int i = 0; i < tabsData.size(); i++) {
            final int index = i;
            TabData data = tabsData.get(i);

            TabItem tab = new TabItem(context, data, () -> {
                if (listener != null) {
                    listener.onTabSelected(index);
                }
            }, i == 1);

            tabs.add(tab);
            tabsLayout.addView(tab.getView());
        }

        // تحديث المؤشر بعد البناء
        tabsLayout.post(() -> {
            if (!tabs.isEmpty()) {
                int tabWidth = tabsLayout.getWidth() / tabs.size();
                indicator.setWidth(tabWidth);
                indicator.setInitialPosition(isRTL() ? tabWidth : -tabWidth);
            }
        });
    }

    /**
     * تحديث التبويب المحدد
     */
    public void selectTab(int position) {
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setSelected(i == position);
        }
        indicator.setSelected();
    }

    /**
     * تحديث موضع المؤشر أثناء الـ scroll
     */
    public void updateIndicatorPosition(int position, float offset) {
        if (!tabs.isEmpty()) {
            int tabWidth = tabsLayout.getWidth() / tabs.size();
            indicator.updatePosition(position, offset, tabWidth);
        }
    }

    /**
     * أنيميشن النبض للمؤشر
     */
    public void pulseIndicator() {
        indicator.pulse();
    }

    /**
     * Listener للتبويب المحدد
     */
    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public FrameLayout getView() {
        return container;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }

    public interface OnTabSelectedListener {
        void onTabSelected(int position);
    }

    public View getTabView(int position) {
        if (position >= 0 && position < tabsLayout.getChildCount()) {
            return tabsLayout.getChildAt(position);
        }
        return null;
    }
}