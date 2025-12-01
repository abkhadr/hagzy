package hagzy.layouts.slidemenu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

import hagzy.layouts.slidemenu.items.MenuDivider;
import hagzy.layouts.slidemenu.items.MenuItem;
import hagzy.layouts.slidemenu.models.MenuConfig;
import hagzy.layouts.slidemenu.models.MenuItemData;
import hagzy.layouts.slidemenu.sections.MenuFooter;
import hagzy.layouts.slidemenu.sections.MenuHeader;
import hagzy.layouts.slidemenu.utils.MenuButtonAnimator;
import hagzy.layouts.slidemenu.utils.MenuRTLHelper;
import static com.bytepulse.hagzy.helpers.TranslationManager.isRTL;

/**
 * Container الرئيسي للقائمة
 * يحتوي على Header + Items + Footer
 */
public class SlideMenuContainer {

    private Context context;
    private MenuConfig config;
    private FrameLayout container;

    private MenuHeader header;
    private MenuFooter footer;
    private LinearLayout itemsContainer;
    private List<MenuItem> menuItems = new ArrayList<>();
    private final boolean isRTL = isRTL();


    public SlideMenuContainer(Context context, MenuConfig config) {
        this.context = context;
        this.config = config;
        buildContainer();
    }

    private void buildContainer() {
        container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                dpToPx(config.getMenuWidth()),
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        params.gravity = MenuRTLHelper.getMenuGravity(context);
        container.setLayoutParams(params);
        container.setBackgroundColor(config.getBackgroundColor());

        // Shadow
        if (config.shouldShowShadow()) {
            container.setElevation(dpToPx(8));
        }

        // Corner Radius
        if (config.getCornerRadius() > 0) {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(config.getBackgroundColor());
            bg.setCornerRadius(dpToPx(config.getCornerRadius()));
            container.setBackground(bg);
        }

        // Main Layout (Vertical)
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Header
        if (config.shouldShowHeader()) {
            header = new MenuHeader(context, config);
            mainLayout.addView(header.getView());
        }

        // Items Container with ScrollView
        ScrollView scrollView = new ScrollView(context);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        scrollView.setLayoutParams(scrollParams);
        scrollView.setVerticalScrollBarEnabled(false);

        itemsContainer = new LinearLayout(context);
        itemsContainer.setLayoutParams(new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        itemsContainer.setPadding(0, dpToPx(8), 0, dpToPx(8));

        scrollView.addView(itemsContainer);
        mainLayout.addView(scrollView);

        // Footer
        if (config.shouldShowFooter()) {
            footer = new MenuFooter(context, config);
            mainLayout.addView(footer.getView());
        }

        container.addView(mainLayout);

        // ضبط الموضع الأولي (خارج الشاشة)
        int menuWidth = dpToPx(config.getMenuWidth());
        container.setTranslationX(isRTL ? -menuWidth * 1.2f : menuWidth * 1.2f);
    }

    /**
     * إضافة عنصر قائمة
     */
    public void addItem(MenuItemData data) {
        MenuItem item = new MenuItem(context, config, data);
        menuItems.add(item);
        itemsContainer.addView(item.getView());

        // أنيميشن الظهور
        int position = menuItems.size() - 1;
        MenuButtonAnimator.animateItemEntrance(item.getView(), position);
    }

    /**
     * إضافة عدة عناصر
     */
    public void addItems(List<MenuItemData> items) {
        for (MenuItemData data : items) {
            addItem(data);
        }
    }

    /**
     * إضافة فاصل
     */
    public void addDivider() {
        MenuDivider divider = new MenuDivider(context, config);
        itemsContainer.addView(divider.getView());
    }

    /**
     * مسح كل العناصر
     */
    public void clearItems() {
        menuItems.clear();
        itemsContainer.removeAllViews();
    }

    /**
     * تحديث عنصر معين
     */
    public void updateItem(String id, MenuItemData newData) {
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);
            if (item.getData().getId().equals(id)) {
                menuItems.set(i, new MenuItem(context, config, newData));
                itemsContainer.removeViewAt(i);
                itemsContainer.addView(menuItems.get(i).getView(), i);
                break;
            }
        }
    }

    /**
     * حذف عنصر
     */
    public void removeItem(String id) {
        for (int i = 0; i < menuItems.size(); i++) {
            if (menuItems.get(i).getData().getId().equals(id)) {
                itemsContainer.removeViewAt(i);
                menuItems.remove(i);
                break;
            }
        }
    }

    /**
     * الحصول على عنصر
     */
    public MenuItem getItem(String id) {
        for (MenuItem item : menuItems) {
            if (item.getData().getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /**
     * تحديد عنصر معين
     */
    public void setSelectedItem(String id) {
        for (MenuItem item : menuItems) {
            item.setSelected(item.getData().getId().equals(id));
        }
    }

    /**
     * الوصول إلى الـ Header
     */
    public MenuHeader getHeader() {
        return header;
    }

    /**
     * الوصول إلى الـ Footer
     */
    public MenuFooter getFooter() {
        return footer;
    }

    public View getView() {
        return container;
    }

    /**
     * تطبيق ثيم مخصص
     */
    public void applyTheme(MenuConfig newConfig) {
        this.config = newConfig;
        buildContainer(); // إعادة البناء بالثيم الجديد
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}