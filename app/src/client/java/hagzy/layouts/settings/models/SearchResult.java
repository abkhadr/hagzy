package hagzy.layouts.settings.models;

/**
 * Model for search results in settings
 */
public class SearchResult {

    public final String pageId;          // e.g., "main", "language", "notifications"
    public final String title;           // Title of the setting
    public final String description;     // Description of the setting
    public final String matchedText;     // The text that matched the query
    public final int iconRes;            // Icon resource
    public final Runnable action;        // Action to perform when clicked

    public SearchResult(String pageId, String title, String description,
                        String matchedText, int iconRes, Runnable action) {
        this.pageId = pageId;
        this.title = title;
        this.description = description;
        this.matchedText = matchedText;
        this.iconRes = iconRes;
        this.action = action;
    }

    /**
     * Check if this result matches the search query
     */
    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        String lowerQuery = query.toLowerCase().trim();

        return (title != null && title.toLowerCase().contains(lowerQuery)) ||
                (description != null && description.toLowerCase().contains(lowerQuery)) ||
                (matchedText != null && matchedText.toLowerCase().contains(lowerQuery));
    }
}