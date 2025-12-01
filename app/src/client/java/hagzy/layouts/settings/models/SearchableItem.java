package hagzy.layouts.settings.models;

import java.util.ArrayList;
import java.util.List;

public class SearchableItem {
    public String title;
    public String description;
    public String category;
    public String pageId;
    public int iconRes;
    public Runnable onClick;
    public List<String> keywords; // كلمات بحث إضافية

    public SearchableItem(String title, String description, String category,
                          String pageId, int iconRes, Runnable onClick) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.pageId = pageId;
        this.iconRes = iconRes;
        this.onClick = onClick;
        this.keywords = new ArrayList<>();
    }

    public SearchableItem addKeyword(String keyword) {
        this.keywords.add(keyword.toLowerCase());
        return this;
    }

    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String lowerQuery = query.toLowerCase();

        // البحث في العنوان
        if (title.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // البحث في الوصف
        if (description != null && description.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // البحث في الفئة
        if (category != null && category.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // البحث في الكلمات المفتاحية
        for (String keyword : keywords) {
            if (keyword.contains(lowerQuery)) {
                return true;
            }
        }

        return false;
    }
}