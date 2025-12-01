package com.bytepulse.hagzy.layouts.auth.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.ThemeManager;

/**
 * الأزرار - زر رئيسي وزر Social
 */
public class AuthButton {

    public static LinearLayout createPrimaryButton(Context context, String text) {
        LinearLayout button = new LinearLayout(context);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#000000"));
        bg.setCornerRadius(dp(context, 12));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, 56)
        );
        params.bottomMargin = dp(context, 16);
        button.setLayoutParams(params);

        TextView buttonText = createText(context, text, 16, "#FFFFFF", true);
        button.addView(buttonText);

        return button;
    }

    public static LinearLayout createSocialButton(Context context, int iconRes, String text) {
        LinearLayout button = new LinearLayout(context);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#FFFFFF"));
        bg.setCornerRadius(dp(context, 12));
        bg.setStroke(dp(context, 1), Color.parseColor("#E0E0E0"));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, 56)
        );
        params.bottomMargin = dp(context, 12);
        button.setLayoutParams(params);

        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp(context, 24),
                dp(context, 24)
        );
        iconParams.setMarginEnd(dp(context, 12));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(iconRes);
        button.addView(icon);

        TextView buttonText = createText(context, text, 16, "#000000", true);
        button.addView(buttonText);

        return button;
    }

    private static TextView createText(Context context, String text, int size, String color, boolean bold) {
        TextView tv = new TextView(context);
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

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}