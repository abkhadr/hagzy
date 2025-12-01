package hagzy.layouts.slidemenu.sections;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import hagzy.layouts.slidemenu.models.MenuConfig;
import hagzy.layouts.slidemenu.utils.MenuRTLHelper;

/**
 * فوتر القائمة الجانبية
 * يحتوي على معلومات الإصدار والروابط
 */
public class MenuFooter {

    private Context context;
    private MenuConfig config;
    private LinearLayout footerContainer;

    private String versionText = "Version 1.0.0";
    private String copyrightText = "© 2024 Your App";

    public MenuFooter(Context context, MenuConfig config) {
        this.context = context;
        this.config = config;
        buildFooter();
    }

    private void buildFooter() {
        footerContainer = new LinearLayout(context);
        footerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(config.getFooterHeight())
        ));
        footerContainer.setOrientation(LinearLayout.VERTICAL);
        footerContainer.setGravity(Gravity.CENTER);
        footerContainer.setPadding(
                dpToPx(config.getItemPadding()),
                dpToPx(16),
                dpToPx(config.getItemPadding()),
                dpToPx(16)
        );

        // ضبط الاتجاه
        MenuRTLHelper.setLayoutDirection(footerContainer, context);

        // نص الإصدار
        TextView versionView = createVersionText();
        footerContainer.addView(versionView);

        // نص الحقوق
        TextView copyrightView = createCopyrightText();
        footerContainer.addView(copyrightView);
    }

    private TextView createVersionText() {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(4));
        textView.setLayoutParams(params);

        textView.setText(versionText);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        textView.setTextColor(adjustAlpha(config.getItemTextColor(), 0.6f));
        textView.setGravity(Gravity.CENTER);

        return textView;
    }

    private TextView createCopyrightText() {
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        textView.setText(copyrightText);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        textView.setTextColor(adjustAlpha(config.getItemTextColor(), 0.4f));
        textView.setGravity(Gravity.CENTER);

        return textView;
    }

    public void setVersionText(String text) {
        this.versionText = text;
        ((TextView) footerContainer.getChildAt(0)).setText(text);
    }

    public void setCopyrightText(String text) {
        this.copyrightText = text;
        ((TextView) footerContainer.getChildAt(1)).setText(text);
    }

    public View getView() {
        return footerContainer;
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}