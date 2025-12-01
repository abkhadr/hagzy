package hagzy.layouts.settings;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

public class SettingsHeader {

    private final Context context;
    private LinearLayout header;
    private TextView titleText;
    private ImageView backButton;
    private ImageView searchButton;

    // Search Mode Components
    private EditText searchEditText;
    private ImageView clearSearchButton;

    private boolean isSearchMode = false;
    private boolean showHeaderTitle = false;
    private Runnable onBackClickListener;
    private OnSearchListener onSearchListener;

    public interface OnSearchListener {
        void onSearchQuery(String query);
    }

    public SettingsHeader(Context context) {
        this.context = context;
        buildHeader();
    }

    private void buildHeader() {
        header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(16), dp(20), dp(16));
        header.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Back Button
        backButton = new ImageView(context);
        backButton.setImageResource(R.drawable.chevron_left);
        backButton.setColorFilter(Color.parseColor("#1A1A1A"));
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        backParams.setMarginEnd(dp(16));
        backButton.setLayoutParams(backParams);
        backButton.setOnClickListener(v -> {
            if (isSearchMode) {
                exitSearchMode();
            } else if (onBackClickListener != null) {
                onBackClickListener.run();
            }
        });

        // Title
        titleText = new TextView(context);
        titleText.setText(t("settings.title"));
        titleText.setTextSize(20);
        titleText.setTypeface(ThemeManager.fontBold());
        titleText.setTextColor(Color.parseColor("#1A1A1A"));
        titleText.setTranslationY(-dpf(1.5f));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        titleText.setLayoutParams(titleParams);
        titleText.setAlpha(0f); // Start hidden
        titleText.setScaleX(0.9f);
        titleText.setScaleY(0.9f);

        // Search EditText (initially hidden)
        searchEditText = new EditText(context);
        searchEditText.setHint(t("settings.search_hint"));
        searchEditText.setTextSize(16);
        searchEditText.setTypeface(ThemeManager.fontSemiBold());
        searchEditText.setTextColor(Color.parseColor("#1A1A1A"));
        searchEditText.setHintTextColor(Color.parseColor("#999999"));
        searchEditText.setBackgroundColor(Color.TRANSPARENT);
        searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchEditText.setSingleLine(true);
        searchEditText.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        searchEditText.setLayoutParams(searchParams);
        searchEditText.setVisibility(View.GONE);
        searchEditText.setAlpha(0f);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (onSearchListener != null) {
                    onSearchListener.onSearchQuery(s.toString());
                }
                // Show/hide clear button
                clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });

        // Clear Search Button
        clearSearchButton = new ImageView(context);
        clearSearchButton.setImageResource(R.drawable.bars_3); // استخدم أيقونة X
        clearSearchButton.setColorFilter(Color.parseColor("#999999"));
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        clearParams.setMarginEnd(dp(12));
        clearSearchButton.setLayoutParams(clearParams);
        clearSearchButton.setVisibility(View.GONE);
        clearSearchButton.setAlpha(0f);
        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            searchEditText.requestFocus();
            showKeyboard();
        });

        // Search Button
        searchButton = new ImageView(context);
        searchButton.setImageResource(R.drawable.magnifying_glass); // أيقونة البحث
        searchButton.setColorFilter(Color.parseColor("#1A1A1A"));
        LinearLayout.LayoutParams searchBtnParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        searchButton.setLayoutParams(searchBtnParams);
        searchButton.setOnClickListener(v -> enterSearchMode());

        header.addView(backButton);
        header.addView(titleText);
        header.addView(searchEditText);
        header.addView(clearSearchButton);
        header.addView(searchButton);
    }

    private void enterSearchMode() {
        if (isSearchMode) return;
        isSearchMode = true;

        // Animation duration
        int duration = 250;

        // Fade out title and search button
        titleText.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> titleText.setVisibility(View.GONE))
                .start();

        searchButton.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> searchButton.setVisibility(View.GONE))
                .start();

        // Fade in search edittext
        searchEditText.setVisibility(View.VISIBLE);
        searchEditText.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    searchEditText.requestFocus();
                    showKeyboard();
                })
                .start();

        // Animate back button icon change (optional)
        animateBackButtonRotation(0f, 180f);
    }

    public void exitSearchMode() {
        exitSearchMode(0f); // Default to 0 if not specified
    }

    public void exitSearchMode(float scrollProgress) {
        if (!isSearchMode) return;
        isSearchMode = false;

        hideKeyboard();
        searchEditText.setText("");

        // Animation duration
        int duration = 250;

        // Fade out search components
        searchEditText.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    searchEditText.setVisibility(View.GONE);
                    searchEditText.clearFocus();
                })
                .start();

        if (clearSearchButton.getVisibility() == View.VISIBLE) {
            clearSearchButton.animate()
                    .alpha(0f)
                    .setDuration(duration)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> clearSearchButton.setVisibility(View.GONE))
                    .start();
        }

        // Fade in title with current scroll progress
        titleText.setVisibility(View.VISIBLE);
        titleText.setAlpha(scrollProgress); // Set based on scroll
        titleText.setScaleX(0.9f + (scrollProgress * 0.1f));
        titleText.setScaleY(0.9f + (scrollProgress * 0.1f));

        // Fade in search button
        searchButton.setVisibility(View.VISIBLE);
        searchButton.setScaleX(0.8f);
        searchButton.setScaleY(0.8f);
        searchButton.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animate back button rotation
        animateBackButtonRotation(180f, 0f);
    }

    private void animateBackButtonRotation(float from, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(250);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            backButton.setRotation(value);
        });
        animator.start();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        }
    }

    public void setTitle(String title) {
        if (titleText != null) {
            titleText.setText(title);
        }
    }

    public void setOnBackClickListener(Runnable listener) {
        this.onBackClickListener = listener;
    }

    public void setOnSearchListener(OnSearchListener listener) {
        this.onSearchListener = listener;
    }

    public boolean isSearchMode() {
        return isSearchMode;
    }

    public void clearSearch() {
        searchEditText.setText("");
    }

    public void setScrollProgress(float progress) {
        if (!isSearchMode) {
            int duration = 200;

            if (progress >= 1f && titleText.getAlpha() < 0.5f) {
                // Show header title
                titleText.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(duration)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            } else if (progress < 1f && titleText.getAlpha() > 0.5f) {
                // Hide header title
                titleText.animate()
                        .alpha(0f)
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(duration)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        }
    }

    public void showTitle(boolean animated) {
        if (animated) {
            titleText.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            titleText.setAlpha(1f);
            titleText.setScaleX(1f);
            titleText.setScaleY(1f);
        }
    }

    public LinearLayout getView() {
        return header;
    }

    private int dp(int dpValue) {
        return UiHelper.dp(context, dpValue);
    }

    private float dpf(float value) {
        return UiHelper.dpFloat(context, value);
    }
}