package hagzy.layouts.slidemenu.models;

import android.graphics.Color;

/**
 * إعدادات القائمة الجانبية
 * يحتوي على كل الإعدادات القابلة للتخصيص
 */
public class MenuConfig {

    // الأبعاد
    private int menuWidth = 335; // dp
    private int overlayColor = Color.parseColor("#80000000");

    // الألوان
    private int backgroundColor = Color.WHITE;
    private int headerBackgroundColor = Color.parseColor("#2196F3");
    private int itemTextColor = Color.parseColor("#212121");
    private int itemIconColor = Color.parseColor("#757575");
    private int selectedItemColor = Color.parseColor("#2196F3");
    private int dividerColor = Color.parseColor("#E0E0E0");

    // الأنيميشن
    private int animationDuration = 200; // ms
    private boolean enableSwipeGesture = true;
    private float swipeThreshold = 0.2f; // 30% من عرض الشاشة

    // المظهر
    private boolean showShadow = true;
    private int shadowColor = Color.parseColor("#40000000");
    private int cornerRadius = 0; // dp
    private int itemHeight = 56; // dp
    private int itemPadding = 16; // dp

    // الهيدر
    private boolean showHeader = true;
    private int headerHeight = 180; // dp

    // الفوتر
    private boolean showFooter = false;
    private int footerHeight = 80; // dp

    // Builder Pattern
    public static class Builder {
        private MenuConfig config = new MenuConfig();

        public Builder menuWidth(int width) {
            config.menuWidth = width;
            return this;
        }

        public Builder overlayColor(int color) {
            config.overlayColor = color;
            return this;
        }

        public Builder backgroundColor(int color) {
            config.backgroundColor = color;
            return this;
        }

        public Builder headerBackgroundColor(int color) {
            config.headerBackgroundColor = color;
            return this;
        }

        public Builder itemTextColor(int color) {
            config.itemTextColor = color;
            return this;
        }

        public Builder selectedItemColor(int color) {
            config.selectedItemColor = color;
            return this;
        }

        public Builder animationDuration(int duration) {
            config.animationDuration = duration;
            return this;
        }

        public Builder enableSwipeGesture(boolean enable) {
            config.enableSwipeGesture = enable;
            return this;
        }

        public Builder showShadow(boolean show) {
            config.showShadow = show;
            return this;
        }

        public Builder cornerRadius(int radius) {
            config.cornerRadius = radius;
            return this;
        }

        public Builder showHeader(boolean show) {
            config.showHeader = show;
            return this;
        }

        public Builder showFooter(boolean show) {
            config.showFooter = show;
            return this;
        }

        public MenuConfig build() {
            return config;
        }
    }

    // Getters
    public int getMenuWidth() { return menuWidth; }
    public int getOverlayColor() { return overlayColor; }
    public int getBackgroundColor() { return backgroundColor; }
    public int getHeaderBackgroundColor() { return headerBackgroundColor; }
    public int getItemTextColor() { return itemTextColor; }
    public int getItemIconColor() { return itemIconColor; }
    public int getSelectedItemColor() { return selectedItemColor; }
    public int getDividerColor() { return dividerColor; }
    public int getAnimationDuration() { return animationDuration; }
    public boolean isSwipeGestureEnabled() { return enableSwipeGesture; }
    public float getSwipeThreshold() { return swipeThreshold; }
    public boolean shouldShowShadow() { return showShadow; }
    public int getShadowColor() { return shadowColor; }
    public int getCornerRadius() { return cornerRadius; }
    public int getItemHeight() { return itemHeight; }
    public int getItemPadding() { return itemPadding; }
    public boolean shouldShowHeader() { return showHeader; }
    public int getHeaderHeight() { return headerHeight; }
    public boolean shouldShowFooter() { return showFooter; }
    public int getFooterHeight() { return footerHeight; }

    // Theme Presets
    public static MenuConfig defaultLight() {
        return new Builder()
                .backgroundColor(Color.WHITE)
                .itemTextColor(Color.parseColor("#212121"))
                .selectedItemColor(Color.parseColor("#2196F3"))
                .build();
    }

    public static MenuConfig defaultDark() {
        return new Builder()
                .backgroundColor(Color.parseColor("#1E1E1E"))
                .headerBackgroundColor(Color.parseColor("#2C2C2C"))
                .itemTextColor(Color.parseColor("#E0E0E0"))
                .selectedItemColor(Color.parseColor("#42A5F5"))
                .build();
    }
}