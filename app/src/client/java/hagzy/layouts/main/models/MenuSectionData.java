package hagzy.layouts.main.models;

import java.util.ArrayList;
import java.util.List;

/**
 * نموذج بيانات قسم القائمة
 */
public class MenuSectionData {

    private String title;
    private List<MenuItemData> items;

    public MenuSectionData(String title, List<MenuItemData> items) {
        this.title = title;
        this.items = items != null ? items : new ArrayList<>();
    }

    public MenuSectionData(List<MenuItemData> items) {
        this("", items);
    }

    public String getTitle() {
        return title;
    }

    public List<MenuItemData> getItems() {
        return items;
    }

    public void addItem(MenuItemData item) {
        items.add(item);
    }
}