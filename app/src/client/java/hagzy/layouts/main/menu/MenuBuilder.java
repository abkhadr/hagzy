package hagzy.layouts.main.menu;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.UiHelper;

import java.util.List;

import hagzy.Controller.SlideMenuController;
import hagzy.layouts.main.models.MenuItemData;
import hagzy.layouts.main.models.MenuSectionData;

/**
 * بناء القائمة الجانبية
 */
public class MenuBuilder {

    private Context context;
    private LinearLayout menuLayout;
    private SlideMenuController menuController;

    public MenuBuilder(Context context, SlideMenuController menuController) {
        this.context = context;
        this.menuController = menuController;
        buildMenu();
    }

    private void buildMenu() {
        menuLayout = new LinearLayout(context);
        menuLayout.setPadding(dp(16), dp(24), dp(16), dp(16));
        menuLayout.setOrientation(LinearLayout.VERTICAL);

        menuController.addMenuItem(menuLayout);
    }

    /**
     * إضافة البروفايل
     */
    public MenuBuilder addProfile() {
        MenuProfile profile = new MenuProfile(context);
        menuLayout.addView(profile.getView());
        menuLayout.addView(createDivider());
        return this;
    }

    /**
     * إضافة قسم
     */
    public MenuBuilder addSection(MenuSectionData section) {
        // عنوان القسم
        if (!section.getTitle().isEmpty()) {
            TextView title = createSectionTitle(section.getTitle());
            menuLayout.addView(title);
        }

        // عناصر القسم
        for (MenuItemData item : section.getItems()) {
            MenuItemView itemView = new MenuItemView(
                    context,
                    item.getIcon(),
                    item.getLabel(),
                    () -> {
                        if (item.getOnClick() != null) {
                            item.getOnClick().run();
                        }
                        // إغلاق القائمة بعد الضغط
                        if (menuController != null) {
                            menuLayout.postDelayed(() -> menuController.toggle(), 200);
                        }
                    }
            );
            menuLayout.addView(itemView.getView());
        }

        return this;
    }

    /**
     * إضافة عدة أقسام
     */
    public MenuBuilder addSections(List<MenuSectionData> sections) {
        for (MenuSectionData section : sections) {
            addSection(section);
        }
        return this;
    }

    /**
     * إضافة فاصل
     */
    public MenuBuilder addDivider() {
        menuLayout.addView(createDivider());
        return this;
    }

    /**
     * إنشاء عنوان القسم
     */
    private TextView createSectionTitle(String title) {
        TextView text = UiHelper.createText(context, title, 12, "#999999", 3);
        text.setPadding(dp(12), dp(16), dp(12), dp(8));
        return text;
    }

    /**
     * إنشاء فاصل
     */
    private View createDivider() {
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(1));
        params.bottomMargin = dp(4);
        divider.setLayoutParams(params);

        return divider;
    }

    /**
     * البناء النهائي
     */
    public LinearLayout build() {
        return menuLayout;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}