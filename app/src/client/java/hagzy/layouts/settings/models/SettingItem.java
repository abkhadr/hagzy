package hagzy.layouts.settings.models;

public class SettingItem {
    public final String title;
    public final String description;
    public final int iconRes;
    public final Runnable onClick;

    public SettingItem(String title, String description, int iconRes, Runnable onClick) {
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.onClick = onClick;
    }
}