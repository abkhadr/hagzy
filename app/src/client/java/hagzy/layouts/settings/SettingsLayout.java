package hagzy.layouts.settings;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import hagzy.activities.MainActivity;
import hagzy.helpers.SettingsPagesManager;
import hagzy.layouts.settings.interfaces.Searchable;
import hagzy.layouts.settings.models.SearchResult;
import hagzy.layouts.settings.pages.*;

public class SettingsLayout {

    private final Context context;
    private FrameLayout root;
    private LinearLayout contentLayout;
    private FrameLayout pagesContainer;

    public SettingsHeader header;
    private SettingsPagesManager pagesManager;

    private MainSettingsPage mainPage;
    private SearchResultsPage searchResultsPage;
    private boolean isSearching = false;

    public SettingsLayout(Context context) {
        this.context = context;
        buildLayout();
    }

    private void buildLayout() {
        root = new FrameLayout(context);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(Color.parseColor("#FAFAFA"));

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        buildHeader();
        buildPagesContainer();

        root.addView(contentLayout);
    }

    private void buildHeader() {
        header = new SettingsHeader(context);
        header.setOnBackClickListener(this::handleBackPress);
        header.setOnSearchListener(this::handleSearch);
        contentLayout.addView(header.getView());
    }

    private void buildPagesContainer() {
        pagesContainer = new FrameLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        pagesContainer.setLayoutParams(params);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String name = currentUser != null ? currentUser.getDisplayName() : null;
        String email = currentUser != null ? currentUser.getEmail() : null;
        Uri photoUri = currentUser != null ? currentUser.getPhotoUrl() : null;
        String photoUrl = (photoUri != null) ? photoUri.toString() : null;

        pagesManager = new SettingsPagesManager(context, pagesContainer);
        pagesManager.setOnNavigationListener(title -> {
            if (!header.isSearchMode()) {
                header.setTitle(title);
            }
        });

        // Create pages
        mainPage = new MainSettingsPage(context, name, email, photoUrl,
                this::navigateToLanguage,
                this::navigateToNotifications,
                this::navigateToPrivacy,
                this::navigateToTerms);

        // Setup scroll listener for large title animation
        mainPage.setOnScrollListener(progress -> {
            if (!header.isSearchMode()) {
                header.setScrollProgress(progress);
            }
        });

        searchResultsPage = new SearchResultsPage(context);

        pagesManager.addPage("main", mainPage);
        pagesManager.addPage("search", searchResultsPage);
        pagesManager.addPage("language", new LanguageSettingsPage(context));
        pagesManager.addPage("notifications", new NotificationsSettingsPage(context));
        pagesManager.addPage("privacy", new PrivacySettingsPage(context));
        pagesManager.addPage("terms", new TermsSettingsPage(context));

        pagesManager.showPage("main");
        contentLayout.addView(pagesContainer);
    }

    private void handleSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Return to main page if search is empty
            if (isSearching) {
                pagesManager.showPage("main");
                isSearching = false;
            }
            return;
        }

        // Perform search
        List<SearchResult> allResults = new ArrayList<>();

        // Search in main page
        if (mainPage instanceof Searchable) {
            allResults.addAll(((Searchable) mainPage).search(query));
        }

        // Show results
        searchResultsPage.showResults(allResults, query);

        if (!isSearching) {
            pagesManager.showPage("search");
            isSearching = true;
        }
    }

    public void handleBackPress() {
        if (header.isSearchMode()) {
            // Exit search mode with current scroll progress
            float scrollProgress = mainPage.getCurrentScrollProgress();
            header.exitSearchMode(scrollProgress);
            if (isSearching) {
                pagesManager.showPage("main");
                isSearching = false;
            }
        } else if (pagesManager.isOnMainPage()) {
            if (context instanceof MainActivity) {
                ((MainActivity) context).onBackPressed();
            }
        } else {
            pagesManager.navigateBack();
            header.setTitle(t("settings.title"));
        }
    }

    private void navigateToLanguage() {
        exitSearchMode();
        pagesManager.navigateTo("language");
        header.setTitle(t("settings.language"));
        header.showTitle(true); // Show title when entering subsection
    }

    private void navigateToNotifications() {
        exitSearchMode();
        pagesManager.navigateTo("notifications");
        header.setTitle(t("settings.notifications"));
        header.showTitle(true);
    }

    private void navigateToPrivacy() {
        exitSearchMode();
        pagesManager.navigateTo("privacy");
        header.setTitle(t("settings.privacy_policy"));
        header.showTitle(true);
    }

    private void navigateToTerms() {
        exitSearchMode();
        pagesManager.navigateTo("terms");
        header.setTitle(t("settings.terms"));
        header.showTitle(true);
    }

    private void exitSearchMode() {
        if (header.isSearchMode()) {
            float scrollProgress = mainPage.getCurrentScrollProgress();
            header.exitSearchMode(scrollProgress);
        }
        if (isSearching) {
            pagesManager.showPage("main");
            isSearching = false;
        }
    }

    public SettingsPagesManager getPagesManager() {
        return pagesManager;
    }

    public FrameLayout getView() {
        return root;
    }

    public View getHeader() { return header.getView(); }
}