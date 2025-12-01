package hagzy.layouts.support;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

import java.util.Arrays;
import java.util.List;

public class SupportLayout {

    private Context context;
    private FrameLayout root;
    private LinearLayout content;
    private LinearLayout header;
    private ScrollView scrollView;
    private LinearLayout itemsContainer;

    public SupportLayout(Context context) {
        this.context = context;
        build();
    }

    private void build() {
        root = new FrameLayout(context);
        root.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        root.setBackgroundColor(Color.parseColor("#F5F5F5"));

        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

        buildHeader();
        buildContent();

        root.addView(content);
        applyInsets();
    }

    private void buildHeader() {
        header = new LinearLayout(context);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundColor(Color.WHITE);
        header.setPadding(dp(20), dp(16), dp(20), dp(20));

        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

        LinearLayout backBtn = createIconButton(R.drawable.chevron_right, "#F5F5F5");
        backBtn.setOnClickListener(v -> {});

        TextView title = txt("الدعم الفني", 24, "#000000", true);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

        topRow.addView(backBtn);
        topRow.addView(title);

        TextView subtitle = txt("كيف يمكننا مساعدتك؟", 15, "#666666", false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(8);
        subtitle.setLayoutParams(subtitleParams);

        header.addView(topRow);
        header.addView(subtitle);
        header.addView(createDivider());

        content.addView(header);
    }

    private void buildContent() {
        scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1));
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        itemsContainer = new LinearLayout(context);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        itemsContainer.setPadding(dp(16), dp(16), dp(16), dp(16));

        buildQuickHelp();
        buildContactSection();
        buildFAQSection();

        scrollView.addView(itemsContainer);
        content.addView(scrollView);
    }

    private void buildQuickHelp() {
        TextView sectionTitle = txt("المساعدة السريعة", 13, "#999999", true);
        sectionTitle.setPadding(dp(4), 0, 0, dp(12));
        itemsContainer.addView(sectionTitle);

        List<HelpItem> quickHelp = Arrays.asList(
                new HelpItem(R.drawable.inbox, "الدردشة المباشرة", "تواصل مع فريق الدعم الآن"),
                new HelpItem(R.drawable.inbox, "الاتصال الهاتفي", "نحن متاحون على مدار الساعة"),
                new HelpItem(R.drawable.inbox, "البريد الإلكتروني", "support@hagzy.com")
        );

        for (HelpItem item : quickHelp) {
            itemsContainer.addView(createHelpCard(item));
        }

        itemsContainer.addView(createSpace(16));
    }

    private void buildContactSection() {
        TextView sectionTitle = txt("تواصل معنا", 13, "#999999", true);
        sectionTitle.setPadding(dp(4), 0, 0, dp(12));
        itemsContainer.addView(sectionTitle);

        List<ContactItem> contacts = Arrays.asList(
                new ContactItem(R.drawable.inbox, "العنوان", "الرياض، المملكة العربية السعودية"),
                new ContactItem(R.drawable.inbox, "ساعات العمل", "24/7 متاح دائماً"),
                new ContactItem(R.drawable.inbox, "الموقع الإلكتروني", "www.hagzy.com")
        );

        LinearLayout contactCard = new LinearLayout(context);
        contactCard.setOrientation(LinearLayout.VERTICAL);
        contactCard.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dp(12);
        contactCard.setLayoutParams(cardParams);
        contactCard.setPadding(dp(16), dp(12), dp(16), dp(12));
        setCardBackground(contactCard);

        for (int i = 0; i < contacts.size(); i++) {
            ContactItem item = contacts.get(i);
            contactCard.addView(createContactRow(item));
            if (i < contacts.size() - 1) {
                contactCard.addView(createDivider());
            }
        }

        itemsContainer.addView(contactCard);
        itemsContainer.addView(createSpace(16));
    }

    private void buildFAQSection() {
        TextView sectionTitle = txt("الأسئلة الشائعة", 13, "#999999", true);
        sectionTitle.setPadding(dp(4), 0, 0, dp(12));
        itemsContainer.addView(sectionTitle);

        List<FAQItem> faqs = Arrays.asList(
                new FAQItem("كيف يمكنني حجز ملعب؟", "يمكنك حجز ملعب من خلال تصفح الملاعب المتاحة واختيار الوقت المناسب"),
                new FAQItem("ما هي طرق الدفع المتاحة؟", "نوفر الدفع الإلكتروني والدفع عند الاستلام"),
                new FAQItem("هل يمكنني إلغاء الحجز؟", "نعم، يمكنك إلغاء الحجز قبل 24 ساعة من موعد المباراة"),
                new FAQItem("كيف أتواصل مع اللاعبين؟", "يمكنك التواصل من خلال الدردشة داخل التطبيق")
        );

        for (FAQItem faq : faqs) {
            itemsContainer.addView(createFAQCard(faq));
        }
    }

    private LinearLayout createHelpCard(HelpItem item) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(80));
        params.bottomMargin = dp(12);
        card.setLayoutParams(params);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        setCardBackground(card);

        LinearLayout iconContainer = new LinearLayout(context);
        iconContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        iconContainerParams.setMarginEnd(dp(16));
        iconContainer.setLayoutParams(iconContainerParams);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setColor(Color.parseColor("#F5F5F5"));
        iconBg.setCornerRadius(dp(24));
        iconContainer.setBackground(iconBg);

        ImageView icon = new ImageView(context);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(24), dp(24)));
        icon.setImageResource(item.icon);
        icon.setColorFilter(Color.parseColor("#000000"));
        iconContainer.addView(icon);

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

        TextView title = txt(item.title, 16, "#000000", true);
        TextView subtitle = txt(item.subtitle, 14, "#666666", false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(4);
        subtitle.setLayoutParams(subtitleParams);

        textContainer.addView(title);
        textContainer.addView(subtitle);

        ImageView arrow = new ImageView(context);
        arrow.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        arrow.setImageResource(R.drawable.chevron_right);
        arrow.setColorFilter(Color.parseColor("#CCCCCC"));

        card.addView(iconContainer);
        card.addView(textContainer);
        card.addView(arrow);

        addTouchAnimation(card);

        return card;
    }

    private LinearLayout createContactRow(ContactItem item) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));

        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        iconParams.setMarginEnd(dp(12));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(item.icon);
        icon.setColorFilter(Color.parseColor("#666666"));

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

        TextView title = txt(item.title, 14, "#999999", false);
        TextView value = txt(item.value, 15, "#000000", true);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(-1, -2);
        valueParams.topMargin = dp(2);
        value.setLayoutParams(valueParams);

        textContainer.addView(title);
        textContainer.addView(value);

        row.addView(icon);
        row.addView(textContainer);

        return row;
    }

    private LinearLayout createFAQCard(FAQItem faq) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(12);
        card.setLayoutParams(params);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        setCardBackground(card);

        TextView question = txt(faq.question, 15, "#000000", true);
        TextView answer = txt(faq.answer, 14, "#666666", false);
        LinearLayout.LayoutParams answerParams = new LinearLayout.LayoutParams(-1, -2);
        answerParams.topMargin = dp(8);
        answer.setLayoutParams(answerParams);

        card.addView(question);
        card.addView(answer);

        addTouchAnimation(card);

        return card;
    }

    private LinearLayout createIconButton(int icon, String bgColor) {
        LinearLayout btn = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(40), dp(40));
        params.setMarginEnd(dp(12));
        btn.setLayoutParams(params);
        btn.setGravity(Gravity.CENTER);

        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor(bgColor));
        shape.setCornerRadius(dp(20));
        btn.setBackground(shape);

        ImageView img = new ImageView(context);
        img.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        img.setImageResource(icon);
        img.setColorFilter(Color.parseColor("#000000"));
        btn.addView(img);

        addTouchAnimation(btn);

        return btn;
    }

    private void setCardBackground(View view) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(12));
        view.setBackground(bg);
    }

    private void addTouchAnimation(View view) {
        view.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.7f);
                    v.setScaleX(0.98f);
                    v.setScaleY(0.98f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1f);
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                    break;
            }
            return false;
        });
    }

    private View createDivider() {
        View div = new View(context);
        div.setBackgroundColor(Color.parseColor("#E0E0E0"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(1));
        params.topMargin = dp(12);
        params.bottomMargin = dp(12);
        div.setLayoutParams(params);
        return div;
    }

    private View createSpace(int heightDp) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(heightDp)));
        return space;
    }

    private TextView txt(String text, int size, String color, boolean bold) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        tv.setTextColor(Color.parseColor(color));
        tv.setTypeface(bold ? ThemeManager.fontBold() : ThemeManager.fontRegular());
        return tv;
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            content.setPadding(0, top, 0, bottom);
            return insets;
        });
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }

    public FrameLayout getView() {
        return root;
    }

    private static class HelpItem {
        int icon;
        String title;
        String subtitle;
        HelpItem(int icon, String title, String subtitle) {
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    private static class ContactItem {
        int icon;
        String title;
        String value;
        ContactItem(int icon, String title, String value) {
            this.icon = icon;
            this.title = title;
            this.value = value;
        }
    }

    private static class FAQItem {
        String question;
        String answer;
        FAQItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    public View getHeader() { return header; }
}