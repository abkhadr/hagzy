package hagzy.layouts.settings.models;

public class NotificationItem {
    public final String title;
    public final String description;
    public final boolean enabled;

    public NotificationItem(String title, String description, boolean enabled) {
        this.title = title;
        this.description = description;
        this.enabled = enabled;
    }
}