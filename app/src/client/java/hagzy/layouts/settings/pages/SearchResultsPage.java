package hagzy.layouts.settings.pages;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

import java.util.ArrayList;
import java.util.List;

import hagzy.layouts.settings.cards.SettingCard;
import hagzy.layouts.settings.models.SearchResult;
import hagzy.layouts.settings.models.SettingItem;

public class SearchResultsPage extends ScrollView {

    private final Context context;
    private LinearLayout content;
    private TextView emptyState;
    private LinearLayout resultsContainer;

    public SearchResultsPage(Context context) {
        super(context);
        this.context = context;
        buildPage();
    }

    private void buildPage() {
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setVerticalScrollBarEnabled(false);
        setBackgroundColor(Color.parseColor("#FAFAFA"));

        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        content.setPadding(dp(20), dp(20), dp(20), dp(20));

        // Empty state
        emptyState = new TextView(context);
        emptyState.setText(t("settings.search_empty"));
        emptyState.setTextSize(15);
        emptyState.setTypeface(ThemeManager.fontSemiBold());
        emptyState.setTextColor(Color.parseColor("#999999"));
        emptyState.setGravity(Gravity.CENTER);
        emptyState.setPadding(dp(20), dp(60), dp(20), dp(20));
        emptyState.setVisibility(VISIBLE);

        // Results container
        resultsContainer = new LinearLayout(context);
        resultsContainer.setOrientation(LinearLayout.VERTICAL);
        resultsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        resultsContainer.setVisibility(GONE);

        content.addView(emptyState);
        content.addView(resultsContainer);
        addView(content);
    }

    public void showResults(List<SearchResult> results, String query) {
        resultsContainer.removeAllViews();

        if (results == null || results.isEmpty()) {
            // Show "no results" message with the query
            String message = query != null && !query.trim().isEmpty()
                    ? t("settings.search_no_results_for") + " \"" + query + "\""
                    : t("settings.search_empty");
            emptyState.setText(message);
            emptyState.setVisibility(VISIBLE);
            resultsContainer.setVisibility(GONE);
            return;
        }

        emptyState.setVisibility(GONE);
        resultsContainer.setVisibility(VISIBLE);

        // Group results by page
        addSection(t("settings.search_results"), results);
    }

    private void addSection(String title, List<SearchResult> results) {
        // Section title
        TextView sectionTitle = new TextView(context);
        sectionTitle.setText(title + " (" + results.size() + ")");
        sectionTitle.setTextSize(13);
        sectionTitle.setTypeface(ThemeManager.fontBold());
        sectionTitle.setTextColor(Color.parseColor("#999999"));
        sectionTitle.setTranslationY(-dpf(1f));
        sectionTitle.setAllCaps(true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(12);
        sectionTitle.setLayoutParams(titleParams);
        resultsContainer.addView(sectionTitle);

        // Results
        for (SearchResult result : results) {
            SettingItem item = new SettingItem(
                    result.title,
                    result.description,
                    result.iconRes,
                    result.action
            );
            resultsContainer.addView(new SettingCard(context, item).getView());
        }
    }

    public void clear() {
        resultsContainer.removeAllViews();
        emptyState.setVisibility(VISIBLE);
        resultsContainer.setVisibility(GONE);
    }

    private int dp(int dpValue) {
        return UiHelper.dp(context, dpValue);
    }

    private float dpf(float value) {
        return UiHelper.dpFloat(context, value);
    }
}