package hagzy.layouts.main.models;

/**
 * نموذج بيانات عنصر القائمة
 */
public class MenuItemData {

    private int icon;
    private String label;
    private Runnable onClick;

    public MenuItemData(int icon, String label) {
        this.icon = icon;
        this.label = label;
    }

    public MenuItemData(int icon, String label, Runnable onClick) {
        this.icon = icon;
        this.label = label;
        this.onClick = onClick;
    }

    public int getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public Runnable getOnClick() {
        return onClick;
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }
}