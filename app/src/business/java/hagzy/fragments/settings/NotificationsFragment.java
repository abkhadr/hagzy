package hagzy.fragments.settings;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.content.SharedPreferences;
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
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;

public class NotificationsFragment extends Fragment {

    private OnBackListener onBackListener;
    private SharedPreferences prefs;

    public interface OnBackListener {
        void onBack();
    }

    public void setOnBackListener(OnBackListener listener) {
        this.onBackListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        prefs = requireContext().getSharedPreferences("notifications_prefs", 0);
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

        // Booking Notifications Section
        mainContainer.addView(createSectionTitle("إشعارات الحجز"));
        mainContainer.addView(createToggleCard(
                "تأكيد الحجز",
                "احصل على إشعار عند تأكيد حجزك",
                "booking_confirmation",
                prefs.getBoolean("booking_confirmation", true)
        ));
        mainContainer.addView(createToggleCard(
                "تذكير الحجز",
                "تذكير قبل موعد الحجز بساعة",
                "booking_reminder",
                prefs.getBoolean("booking_reminder", true)
        ));
        mainContainer.addView(createToggleCard(
                "إلغاء الحجز",
                "إشعار عند إلغاء الحجز",
                "booking_cancellation",
                prefs.getBoolean("booking_cancellation", true)
        ));

        // App Notifications Section
        mainContainer.addView(createSectionTitle("إشعارات التطبيق"));
        mainContainer.addView(createToggleCard(
                "العروض الخاصة",
                "احصل على إشعارات العروض والخصومات",
                "special_offers",
                prefs.getBoolean("special_offers", false)
        ));
        mainContainer.addView(createToggleCard(
                "التحديثات",
                "أخبار وتحديثات التطبيق",
                "app_updates",
                prefs.getBoolean("app_updates", true)
        ));
        mainContainer.addView(createToggleCard(
                "تقييم التطبيق",
                "تذكير بتقييم التطبيق",
                "app_rating",
                prefs.getBoolean("app_rating", false)
        ));

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
            if (onBackListener != null) {
                onBackListener.onBack();
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

        TextView title = createText(t("settings.notifications"), 28, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        header.addView(title);

        TextView subtitle = createText("إدارة الإشعارات والتنبيهات", 16, "#666666", false);
        header.addView(subtitle);

        return header;
    }

    private TextView createSectionTitle(String title) {
        TextView section = new TextView(requireContext());
        section.setText(title);
        section.setTextSize(20);
        section.setTypeface(ThemeManager.fontBold());
        section.setTextColor(Color.parseColor("#000000"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(8);
        params.bottomMargin = dp(16);
        section.setLayoutParams(params);
        return section;
    }

    private LinearLayout createToggleCard(String title, String description, String prefKey, boolean defaultValue) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Color.parseColor("#E0E0E0"));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        // Text Column
        LinearLayout textColumn = new LinearLayout(requireContext());
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        textColumn.setLayoutParams(textParams);

        TextView titleText = createText(title, 16, "#000000", true);
        textColumn.addView(titleText);

        TextView descText = createText(description, 12, "#666666", false);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        descParams.topMargin = dp(2);
        descText.setLayoutParams(descParams);
        textColumn.addView(descText);

        card.addView(textColumn);

        // Switch
        Switch switchView = new Switch(requireContext());
        switchView.setChecked(defaultValue);
        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(prefKey, isChecked).apply();
        });
        card.addView(switchView);

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