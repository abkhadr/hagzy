package hagzy.layouts.settings.interfaces;

import java.util.List;

import hagzy.layouts.settings.models.SearchResult;

/**
 * Interface for searchable settings pages
 */
public interface Searchable {

    /**
     * Search for items in the page
     * @param query The search query
     * @return List of search results
     */
    List<SearchResult> search(String query);

    /**
     * Highlight or navigate to a specific result
     * @param result The search result to highlight
     */
    void highlightResult(SearchResult result);

    /**
     * Clear all highlights
     */
    void clearHighlights();
}