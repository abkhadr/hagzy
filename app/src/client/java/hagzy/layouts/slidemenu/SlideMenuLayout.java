package hagzy.layouts.slidemenu;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import static com.bytepulse.hagzy.helpers.TranslationManager.isRTL;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.slidemenu.items.MenuItem;
import hagzy.layouts.slidemenu.models.MenuConfig;
import hagzy.layouts.slidemenu.models.MenuItemData;
import hagzy.layouts.slidemenu.sections.MenuFooter;
import hagzy.layouts.slidemenu.sections.MenuHeader;

import java.util.ArrayList;
import java.util.List;

public class SlideMenuLayout {
    private final Context context;
    private final FrameLayout root;
    private final LinearLayout contentLayout;
    private View overlay;
    private LinearLayout slideMenu;
    private ScrollView scrollView;
    private LinearLayout itemsContainer;
    private MenuHeader header;
    private MenuFooter footer;
    private boolean isMenuOpen = false;
    private boolean isRTL = isRTL();
    private final int menuWidthDp;
    private final float contentShiftFactor = 0.3f;
    private MenuConfig config;
    private List<MenuItem> menuItems = new ArrayList<>();
    private String selectedItemId = "";
    private OnMenuItemClickListener onItemClickListener;

    public interface OnMenuItemClickListener {
        void onItemClick(String itemId);
    }

    public SlideMenuLayout(Context context, FrameLayout root, LinearLayout contentLayout) {
        this(context, root, contentLayout, MenuConfig.defaultLight());
    }

    public SlideMenuLayout(Context context, FrameLayout root, LinearLayout contentLayout, MenuConfig config) {
        this.context = context;
        this.root = root;
        this.contentLayout = contentLayout;
        this.config = config;
        this.menuWidthDp = config.getMenuWidth();
        initOverlay();
        initSlideMenu();
    }

    private void initOverlay() {
        overlay = new View(context);
        overlay.setBackgroundColor(config.getOverlayColor());
        overlay.setAlpha(0f);
        overlay.setVisibility(View.GONE);
        root.addView(overlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        overlay.setOnClickListener(v -> toggleMenu(false));
    }

    private void initSlideMenu() {
        slideMenu = new LinearLayout(context);
        slideMenu.setOrientation(LinearLayout.VERTICAL);
        slideMenu.setBackgroundColor(config.getBackgroundColor());

        int menuPx = dp(menuWidthDp);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(menuPx, FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = isRTL ? Gravity.END : Gravity.START;
        slideMenu.setLayoutParams(params);

        root.post(() -> {
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(root);
            if (insets != null) {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                slideMenu.setPadding(0, top, 0, bottom);
            }
        });

        if (config.shouldShowHeader()) {
            header = new MenuHeader(context, config);
            slideMenu.addView(header.getView());
        }

        scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scrollView.setVerticalScrollBarEnabled(false);

        itemsContainer = new LinearLayout(context);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        itemsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        itemsContainer.setPadding(0, dp(8), 0, dp(8));

        scrollView.addView(itemsContainer);
        slideMenu.addView(scrollView);

        if (config.shouldShowFooter()) {
            footer = new MenuFooter(context, config);
            slideMenu.addView(footer.getView());
        }

        slideMenu.setTranslationX(isRTL ? dp(menuWidthDp) : -dp(menuWidthDp));
        root.addView(slideMenu);
        slideMenu.setOnTouchListener((v, e) -> true);
    }

    private void toggleMenu(boolean open) {
        if (slideMenu == null || contentLayout == null) return;

        final float menuPx = dp(menuWidthDp);
        float start = slideMenu.getTranslationX();
        float end = open ? 0f : (isRTL ? menuPx : -menuPx);

        overlay.setVisibility(View.VISIBLE);

        ValueAnimator anim = ValueAnimator.ofFloat(start, end);
        anim.setDuration(config.getAnimationDuration());
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());

        anim.addUpdateListener(a -> {
            float value = (float) a.getAnimatedValue();
            float fraction = a.getAnimatedFraction();

            slideMenu.setTranslationX(value);

            float shift = (menuPx - Math.abs(value)) * contentShiftFactor;
            float contentTranslation = (isRTL ? -1f : 1f) * shift;
            contentLayout.setTranslationX(contentTranslation);

            overlay.setAlpha(open ? fraction : 1f - fraction);
        });

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (!open) overlay.setVisibility(View.GONE);
                isMenuOpen = open;
            }
        });

        anim.start();
    }

    public void toggle() {
        toggleMenu(!isMenuOpen);
    }

    public void open() { toggleMenu(true); }
    public void close() { toggleMenu(false); }
    public boolean isOpen() { return isMenuOpen; }

    public void setDirectionRTL(boolean rtl) {
        this.isRTL = rtl;
        if (slideMenu == null) return;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideMenu.getLayoutParams();
        params.gravity = rtl ? Gravity.END : Gravity.START;
        slideMenu.setLayoutParams(params);
        slideMenu.setTranslationX(rtl ? dp(menuWidthDp) : -dp(menuWidthDp));
    }

    public SlideMenuLayout addMenuItem(MenuItemData data) {
        MenuItem item = new MenuItem(context, config, data);
        menuItems.add(item);

        item.getView().setOnClickListener(v -> {
            if (!data.hasToggle()) {
                setSelectedItem(data.getId());
            }

            if (data.getOnClick() != null) {
                data.getOnClick().run();
            }

            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(data.getId());
            }

            if (!data.hasToggle()) {
                close();
            }
        });

        itemsContainer.addView(item.getView());
        return this;
    }

    public SlideMenuLayout addDivider() {
        View divider = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
        );
        params.setMargins(dp(config.getItemPadding()), dp(8), dp(config.getItemPadding()), dp(8));
        divider.setLayoutParams(params);
        divider.setBackgroundColor(config.getDividerColor());
        itemsContainer.addView(divider);
        return this;
    }

    public void setSelectedItem(String id) {
        this.selectedItemId = id;
        for (MenuItem item : menuItems) {
            item.setSelected(item.getData().getId().equals(id));
        }
    }

    public String getSelectedItemId() {
        return selectedItemId;
    }

    public void clearItems() {
        menuItems.clear();
        itemsContainer.removeAllViews();
    }

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

    public void removeItem(String id) {
        for (int i = 0; i < menuItems.size(); i++) {
            if (menuItems.get(i).getData().getId().equals(id)) {
                itemsContainer.removeViewAt(i);
                menuItems.remove(i);
                break;
            }
        }
    }

    public SlideMenuLayout setUserData(String name, String email, int imageRes) {
        if (header != null) {
            header.setUserData(name, email, imageRes);
        }
        return this;
    }

    public SlideMenuLayout setVersionText(String text) {
        if (footer != null) {
            footer.setVersionText(text);
        }
        return this;
    }

    public MenuHeader getHeader() {
        return header;
    }

    public MenuFooter getFooter() {
        return footer;
    }

    public SlideMenuLayout setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.onItemClickListener = listener;
        return this;
    }

    public void applyTheme(MenuConfig newConfig) {
        this.config = newConfig;
    }

    public void destroy() {
        clearItems();
    }

    private int dp(int v) {
        return UiHelper.dp(context, v);
    }
}