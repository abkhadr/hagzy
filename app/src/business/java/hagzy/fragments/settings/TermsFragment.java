package hagzy.fragments.settings;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.graphics.Color;
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
import com.bytepulse.hagzy.helpers.ThemeManager;

public class TermsFragment extends Fragment {

    private OnBackListener onBackListener;

    public interface OnBackListener {
        void onBack();
    }

    public void setOnBackListener(OnBackListener listener) {
        this.onBackListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return buildUI();
    }

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

        mainContainer.addView(createBackButton());
        mainContainer.addView(createHeader());

        TextView content = createText(
                "الشروط والأحكام\n\n" +
                        "مرحباً بك في تطبيق حجز الملاعب. باستخدامك لهذا التطبيق، فإنك توافق على الشروط والأحكام التالية.\n\n" +
                        "قبول الشروط\n" +
                        "باستخدام التطبيق، فإنك توافق على الالتزام بهذه الشروط والأحكام. إذا لم توافق على أي جزء من هذه الشروط، يرجى عدم استخدام التطبيق.\n\n" +
                        "استخدام التطبيق\n" +
                        "• يحق لك استخدام التطبيق للأغراض الشخصية فقط\n" +
                        "• يجب أن تكون على الأقل 18 عاماً لاستخدام التطبيق\n" +
                        "• يجب تقديم معلومات صحيحة ودقيقة\n" +
                        "• لا يجوز استخدام التطبيق لأغراض غير قانونية\n\n" +
                        "الحجوزات والدفع\n" +
                        "• جميع الحجوزات تخضع للتوافر\n" +
                        "• يجب إتمام الدفع لتأكيد الحجز\n" +
                        "• سياسة الإلغاء تختلف حسب الملعب\n" +
                        "• قد تطبق رسوم إلغاء في بعض الحالات\n\n" +
                        "المسؤولية\n" +
                        "التطبيق يعمل كمنصة وساطة بين المستخدمين وأصحاب الملاعب. نحن غير مسؤولين عن أي أضرار أو خسائر قد تنتج عن استخدام الملاعب.\n\n" +
                        "التعديلات\n" +
                        "نحتفظ بالحق في تعديل هذه الشروط في أي وقت. سيتم إخطارك بأي تغييرات جوهرية.\n\n" +
                        "آخر تحديث: يناير 2025",
                14,
                "#333333",
                false
        );
        content.setLineSpacing(dp(6), 1.0f);
        mainContainer.addView(content);

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

        TextView title = createText(t("settings.terms"), 28, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        header.addView(title);

        TextView subtitle = createText("شروط استخدام التطبيق", 16, "#666666", false);
        header.addView(subtitle);

        return header;
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