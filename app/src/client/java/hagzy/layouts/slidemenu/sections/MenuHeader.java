package hagzy.layouts.slidemenu.sections;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import hagzy.layouts.slidemenu.models.MenuConfig;
import hagzy.layouts.slidemenu.utils.MenuButtonAnimator;
import hagzy.layouts.slidemenu.utils.MenuRTLHelper;

/**
 * هيدر القائمة الجانبية
 * يحتوي على صورة المستخدم والاسم والبريد
 */
public class MenuHeader {

    private Context context;
    private MenuConfig config;
    private LinearLayout headerContainer;

    // البيانات
    private String userName = "";
    private String userEmail = "";
    private int userImageRes = 0;
    private Runnable onHeaderClick;

    public MenuHeader(Context context, MenuConfig config) {
        this.context = context;
        this.config = config;
        buildHeader();
    }

    private void buildHeader() {
        headerContainer = new LinearLayout(context);
        headerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(config.getHeaderHeight())
        ));
        headerContainer.setOrientation(LinearLayout.VERTICAL);
        headerContainer.setGravity(Gravity.CENTER);
        headerContainer.setBackgroundColor(config.getHeaderBackgroundColor());
        headerContainer.setPadding(
                dpToPx(config.getItemPadding()),
                dpToPx(24),
                dpToPx(config.getItemPadding()),
                dpToPx(16)
        );

        // ضبط الاتجاه
        MenuRTLHelper.setLayoutDirection(headerContainer, context);

        // صورة المستخدم
        ImageView userImage = createUserImage();
        headerContainer.addView(userImage);

        // اسم المستخدم
        TextView nameText = createNameText();
        headerContainer.addView(nameText);

        // البريد الإلكتروني
        TextView emailText = createEmailText();
        headerContainer.addView(emailText);

        // أنيميشن الضغط
        MenuButtonAnimator.applyBounceAnimation(headerContainer, () -> {
            if (onHeaderClick != null) {
                onHeaderClick.run();
            }
        });
    }

    private ImageView createUserImage() {
        ImageView imageView = new ImageView(context);
        int size = dpToPx(80);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(0, 0, 0, dpToPx(12));
        imageView.setLayoutParams(params);

        // صورة دائرية
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.WHITE);
        imageView.setBackground(drawable);

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));

        if (userImageRes != 0) {
            imageView.setImageResource(userImageRes);
        }

        return imageView;
    }

    private TextView createNameText() {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(4));
        textView.setLayoutParams(params);

        textView.setText(userName.isEmpty() ? "User Name" : userName);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        textView.setGravity(Gravity.CENTER);

        return textView;
    }

    private TextView createEmailText() {
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        textView.setText(userEmail.isEmpty() ? "user@example.com" : userEmail);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setTextColor(Color.parseColor("#E0E0E0"));
        textView.setGravity(Gravity.CENTER);

        return textView;
    }

    /**
     * تحديث بيانات المستخدم
     */
    public void setUserData(String name, String email, int imageRes) {
        this.userName = name;
        this.userEmail = email;
        this.userImageRes = imageRes;
        buildHeader(); // إعادة بناء الهيدر
    }

    public void setUserName(String name) {
        this.userName = name;
        ((TextView) headerContainer.getChildAt(1)).setText(name);
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
        ((TextView) headerContainer.getChildAt(2)).setText(email);
    }

    public void setUserImage(int imageRes) {
        this.userImageRes = imageRes;
        ((ImageView) headerContainer.getChildAt(0)).setImageResource(imageRes);
    }

    public void setOnHeaderClick(Runnable onClick) {
        this.onHeaderClick = onClick;
    }

    public View getView() {
        return headerContainer;
    }

    /**
     * تطبيق ثيم مخصص
     */
    public void applyCustomTheme(int backgroundColor, int textColor) {
        headerContainer.setBackgroundColor(backgroundColor);
        ((TextView) headerContainer.getChildAt(1)).setTextColor(textColor);
        ((TextView) headerContainer.getChildAt(2)).setTextColor(
                adjustAlpha(textColor, 0.7f)
        );
    }

    /**
     * إضافة تدرج لوني للخلفية
     */
    public void setGradientBackground(int startColor, int endColor) {
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{startColor, endColor}
        );
        headerContainer.setBackground(gradient);
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