package hagzy.layouts.settings.models;

import java.util.function.Consumer;

public class ToggleItem {
    public final String title;
    public final String description;
    public final int iconRes;
    public final boolean initialState;
    public final Consumer<Boolean> onToggle;

    public ToggleItem(String title, String description, int iconRes,
                      boolean initialState, Consumer<Boolean> onToggle) {
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.initialState = initialState;
        this.onToggle = onToggle;
    }
}