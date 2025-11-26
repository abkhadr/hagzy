package hagzy.fragments.settings;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.LocaleManager;
import com.bytepulse.hagzy.helpers.ThemeManager;

public class LanguageFragment extends Fragment {

    private OnLanguageChangedListener onLanguageChangedListener;
    private String currentLanguage;

    public interface OnLanguageChangedListener {
        void onLanguageChanged(String languageCode);
        void onBack();
    }

    public void setOnLanguageChangedListener(OnLanguageChangedListener listener) {
        this.onLanguageChangedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        currentLanguage = LocaleManager.getSavedLanguage(requireContext());
        return buildUI();
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 UI Building
    // ════════════════════════════════════════════════════════════

    private View buildUI() {
        FrameLayout root = new FrameLayout(requireContext());
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(Color.WHITE);

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        scrollView.setVerticalScrollBarEnabled(false);

        LinearLayout mainContainer = new LinearLayout(requireContext());
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(dp(24), dp(60), dp(24), dp(40));

        // Back Button
        mainContainer.addView(createBackButton());

        // Header
        mainContainer.addView(createHeader());

        // Description
        TextView description = createText(
                "اختر اللغة المفضلة للتطبيق",
                14,
                "#666666",
                false
        );
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        descParams.bottomMargin = dp(24);
        description.setLayoutParams(descParams);
        mainContainer.addView(description);

        // Languages
        mainContainer.addView(createLanguageOption("العربية", "ar_AR", "🇸🇦"));
        mainContainer.addView(createLanguageOption("English", "en_US", "🇺🇸"));
        mainContainer.addView(createLanguageOption("Français", "fr_FR", "🇫🇷"));
        mainContainer.addView(createLanguageOption("Español", "es_ES", "🇪🇸"));

        scrollView.addView(mainContainer);
        root.addView(scrollView);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            scrollView.setPadding(0, top, 0, bottom);
            return insets;
        });

        return root;
    }

    private LinearLayout createBackButton() {
        LinearLayout backButton = new LinearLayout(requireContext());
        backButton.setOrientation(LinearLayout.HORIZONTAL);
        backButton.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(24);
        backButton.setLayoutParams(params);

        ImageView backIcon = new ImageView(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMarginEnd(dp(8));
        backIcon.setLayoutParams(iconParams);
        backIcon.setImageResource(R.drawable.arrow_left);
        backIcon.setColorFilter(Color.parseColor("#000000"));
        backButton.addView(backIcon);

        TextView backText = createText("رجوع", 16, "#000000", true);
        backButton.addView(backText);

        backButton.setOnClickListener(v -> {
            if (onLanguageChangedListener != null) {
                onLanguageChangedListener.onBack();
            }
        });

        return backButton;
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headerParams.bottomMargin = dp(24);
        header.setLayoutParams(headerParams);

        TextView title = createText(t("field.language"), 28, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        header.addView(title);

        TextView subtitle = createText("Language / Langue / Idioma", 16, "#666666", false);
        header.addView(subtitle);

        return header;
    }

    @SuppressLint("ClickableViewAccessibility")
    private LinearLayout createLanguageOption(String languageName, String languageCode, String flag) {
        boolean isSelected = currentLanguage.equals(languageCode);

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));
        if (isSelected) {
            bg.setColor(Color.parseColor("#E8F5E9"));
            bg.setStroke(dp(2), Color.parseColor("#4CAF50"));
        } else {
            bg.setColor(Color.WHITE);
            bg.setStroke(dp(1), Color.parseColor("#E0E0E0"));
        }
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        // Flag
        TextView flagText = new TextView(requireContext());
        flagText.setText(flag);
        flagText.setTextSize(32);
        LinearLayout.LayoutParams flagParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        flagParams.setMarginEnd(dp(16));
        flagText.setLayoutParams(flagParams);
        card.addView(flagText);

        // Language Name
        TextView languageText = createText(
                languageName,
                18,
                isSelected ? "#4CAF50" : "#000000",
                true
        );
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        languageText.setLayoutParams(textParams);
        card.addView(languageText);

        // Check Icon
        if (isSelected) {
            ImageView checkIcon = new ImageView(requireContext());
            checkIcon.setImageResource(R.drawable.check_badge);
            checkIcon.setColorFilter(Color.parseColor("#4CAF50"));
            LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(dp(24), dp(24));
            checkIcon.setLayoutParams(checkParams);
            card.addView(checkIcon);
        }

        card.setOnClickListener(v -> {
            if (!currentLanguage.equals(languageCode)) {
                LocaleManager.setLocale((Activity) requireContext(), languageCode);
                if (onLanguageChangedListener != null) {
                    onLanguageChangedListener.onLanguageChanged(languageCode);
                }
            }
        });

        card.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1f);
            }
            return false;
        });

        return card;
    }

    private TextView createText(String text, int size, String color, boolean bold) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        tv.setTextColor(Color.parseColor(color));
        if (bold) {
            tv.setTypeface(ThemeManager.fontBold());
        } else {
            tv.setTypeface(ThemeManager.fontRegular());
        }
        return tv;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}