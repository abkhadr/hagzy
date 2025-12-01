package hagzy.helpers;

import android.content.Context;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import static com.bytepulse.hagzy.helpers.TranslationManager.isRTL;
public class SettingsPagesManager {

    private final Context context;
    private final FrameLayout container;
    private final Map<String, View> pages;
    private final Stack<String> navigationStack;
    private String currentPageId;
    private final boolean isRTL = isRTL();

    public Context getContext() {
        return context;
    }

    private OnNavigationListener navigationListener;

    public void setNavigationListener(OnNavigationListener navigationListener) {
        this.navigationListener = navigationListener;
    }

    public interface OnNavigationListener {
        void onNavigate(String title);
    }

    public SettingsPagesManager(Context context, FrameLayout container) {
        this.context = context;
        this.container = container;
        this.pages = new HashMap<>();
        this.navigationStack = new Stack<>();
    }

    public void addPage(String id, View page) {
        pages.put(id, page);
        page.setVisibility(View.GONE);
        container.addView(page);
    }

    public void showPage(String pageId) {
        if (pages.containsKey(pageId)) {
            for (Map.Entry<String, View> entry : pages.entrySet()) {
                entry.getValue().setVisibility(
                        entry.getKey().equals(pageId) ? View.VISIBLE : View.GONE
                );
            }
            currentPageId = pageId;
            navigationStack.clear();
            navigationStack.push(pageId);
        }
    }

    public void navigateTo(String pageId) {
        if (!pages.containsKey(pageId) || pageId.equals(currentPageId)) {
            return;
        }

        View currentView = pages.get(currentPageId);
        View targetView = pages.get(pageId);

        int containerWidth = container.getWidth();
        int startX, endX;

        if (isRTL) { // RTL
            startX = -containerWidth;
            endX = containerWidth;
        } else {   // LTR
            startX = containerWidth;
            endX = -containerWidth;
        }

        assert targetView != null;
        targetView.setTranslationX(startX);
        targetView.setVisibility(View.VISIBLE);
        targetView.setAlpha(1f);

        assert currentView != null;
        currentView.animate()
                .translationX(endX)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    currentView.setVisibility(View.GONE);
                    currentView.setTranslationX(0);
                    currentView.setAlpha(1f);
                })
                .start();

        targetView.animate()
                .translationX(0)
                .alpha(1f)
                .setDuration(200)
                .start();

        navigationStack.push(pageId);
        currentPageId = pageId;
    }

    public void navigateBack() {
        if (navigationStack.size() <= 1) {
            return;
        }

        navigationStack.pop();
        String previousPageId = navigationStack.peek();

        View currentView = pages.get(currentPageId);
        View targetView = pages.get(previousPageId);

        int startX, endX;

        if (isRTL) {
            startX = container.getWidth();
            endX = -container.getWidth();
        } else {
            startX = -container.getWidth();
            endX = container.getWidth();
        }

        assert targetView != null;
        targetView.setTranslationX(startX);
        targetView.setVisibility(View.VISIBLE);
        targetView.setAlpha(1f);

        assert currentView != null;
        currentView.animate()
                .translationX(endX)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    currentView.setVisibility(View.GONE);
                    currentView.setTranslationX(0);
                    currentView.setAlpha(1f);
                })
                .start();

        targetView.animate()
                .translationX(0)
                .setDuration(200)
                .start();

        currentPageId = previousPageId;
    }

    public boolean isOnMainPage() {
        return navigationStack.size() <= 1;
    }

    public void setOnNavigationListener(OnNavigationListener listener) {
        this.navigationListener = listener;
    }

    /**
     * Handles back press.
     * @return true if the navigation stack changed (handled), false if already on main page
     */
    public boolean onPressBack() {
        if (isOnMainPage()) {
            if (navigationStack.size() <= 1) {
                return false;
            }
        }
        navigateBack();
        return true;
    }
}