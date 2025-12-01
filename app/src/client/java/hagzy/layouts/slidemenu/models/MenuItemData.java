package hagzy.layouts.slidemenu.models;

/**
 * نموذج بيانات عنصر القائمة
 * يحتوي على كل المعلومات اللازمة لعرض العنصر
 */
public class MenuItemData {

    // المعلومات الأساسية
    private String id;
    private String title;
    private String subtitle;
    private int iconRes;

    // الحالة والمظهر
    private boolean enabled = true;
    private boolean selected = false;
    private boolean showArrow = true;

    // Badge والإشعارات
    private boolean hasBadge = false;
    private String badgeText = "";
    private int badgeColor = 0xFFFF4444;

    // Toggle/Switch
    private boolean hasToggle = false;
    private boolean toggleState = false;

    // الأنيميشن
    private AnimationType animationType = AnimationType.STANDARD;

    // الـ Callback
    private Runnable onClick;
    private OnToggleChangeListener onToggleChange;

    // أنواع الأنيميشن المتاحة
    public enum AnimationType {
        STANDARD,      // أنيميشن عادي
        BOUNCE,        // أنيميشن مع ارتداد
        LIGHT,         // أنيميشن خفيف
        PULSE          // أنيميشن نبضي
    }

    // Interface للـ Toggle
    public interface OnToggleChangeListener {
        void onToggleChanged(boolean isChecked);
    }

    // Constructor كامل
    public MenuItemData(String id, String title, String subtitle, int iconRes) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.iconRes = iconRes;
    }

    // Constructor بسيط
    public MenuItemData(String id, String title, int iconRes) {
        this(id, title, "", iconRes);
    }

    // Getters & Setters

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public MenuItemData setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public MenuItemData setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public int getIconRes() {
        return iconRes;
    }

    public MenuItemData setIconRes(int iconRes) {
        this.iconRes = iconRes;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public MenuItemData setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isSelected() {
        return selected;
    }

    public MenuItemData setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public boolean shouldShowArrow() {
        return showArrow;
    }

    public MenuItemData setShowArrow(boolean showArrow) {
        this.showArrow = showArrow;
        return this;
    }

    public boolean hasBadge() {
        return hasBadge;
    }

    public MenuItemData setBadge(boolean hasBadge, String text, int color) {
        this.hasBadge = hasBadge;
        this.badgeText = text;
        this.badgeColor = color;
        return this;
    }

    public MenuItemData setBadge(boolean hasBadge, String text) {
        return setBadge(hasBadge, text, 0xFFFF4444);
    }

    public String getBadgeText() {
        return badgeText;
    }

    public int getBadgeColor() {
        return badgeColor;
    }

    public boolean hasToggle() {
        return hasToggle;
    }

    public MenuItemData setToggle(boolean hasToggle, boolean initialState, OnToggleChangeListener listener) {
        this.hasToggle = hasToggle;
        this.toggleState = initialState;
        this.onToggleChange = listener;
        this.showArrow = false; // إخفاء السهم عند وجود Toggle
        return this;
    }

    public boolean getToggleState() {
        return toggleState;
    }

    public void setToggleState(boolean state) {
        this.toggleState = state;
        if (onToggleChange != null) {
            onToggleChange.onToggleChanged(state);
        }
    }

    public OnToggleChangeListener getOnToggleChange() {
        return onToggleChange;
    }

    public AnimationType getAnimationType() {
        return animationType;
    }

    public MenuItemData setAnimationType(AnimationType type) {
        this.animationType = type;
        return this;
    }

    public Runnable getOnClick() {
        return onClick;
    }

    public MenuItemData setOnClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    // Builder Pattern (اختياري)
    public static class Builder {
        private MenuItemData item;

        public Builder(String id, String title, int iconRes) {
            item = new MenuItemData(id, title, iconRes);
        }

        public Builder subtitle(String subtitle) {
            item.setSubtitle(subtitle);
            return this;
        }

        public Builder enabled(boolean enabled) {
            item.setEnabled(enabled);
            return this;
        }

        public Builder selected(boolean selected) {
            item.setSelected(selected);
            return this;
        }

        public Builder showArrow(boolean show) {
            item.setShowArrow(show);
            return this;
        }

        public Builder badge(String text, int color) {
            item.setBadge(true, text, color);
            return this;
        }

        public Builder badge(String text) {
            item.setBadge(true, text);
            return this;
        }

        public Builder toggle(boolean initialState, OnToggleChangeListener listener) {
            item.setToggle(true, initialState, listener);
            return this;
        }

        public Builder animation(AnimationType type) {
            item.setAnimationType(type);
            return this;
        }

        public Builder onClick(Runnable onClick) {
            item.setOnClick(onClick);
            return this;
        }

        public MenuItemData build() {
            return item;
        }
    }
}