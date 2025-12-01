package com.bytepulse.hagzy.layouts.auth.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.layouts.auth.utils.AuthCardAnimator;

/**
 * حقول الإدخال والروابط
 */
public class AuthInput {

    public static TextView createLabel(Context context, String text) {
        TextView label = createText(context, text, 14, "#000000", true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(context, 8);
        label.setLayoutParams(params);
        return label;
    }

    public static EditText createEmailInput(Context context) {
        return createInput(context, "name@example.com", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    public static EditText createPasswordInput(Context context) {
        return createInput(context, "••••••••", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    public static EditText createInput(Context context, String hint, int inputType) {
        EditText input = new EditText(context);
        input.setHint(hint);
        input.setInputType(inputType);
        input.setTextSize(16);
        input.setTextColor(Color.parseColor("#000000"));
        input.setHintTextColor(Color.parseColor("#999999"));
        input.setPadding(dp(context, 16), 0, dp(context, 16), 0);
        input.setTypeface(ThemeManager.fontRegular());

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(context, 12));
        input.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, 56)
        );
        params.bottomMargin = dp(context, 16);
        input.setLayoutParams(params);

        return input;
    }

    public static TextView createErrorText(Context context) {
        TextView errorText = createText(context, "", 14, "#E53935", false);
        errorText.setGravity(Gravity.CENTER);
        errorText.setVisibility(View.GONE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(context, 16);
        errorText.setLayoutParams(params);
        return errorText;
    }

    public static TextView createLinkText(Context context, String text) {
        return createText(context, text, 14, "#1976D2", true);
    }

    public static LinearLayout createLinkContainer(Context context, String normalText, String linkText, Runnable onClick) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(context, 24);
        container.setLayoutParams(params);

        TextView normal = createText(context, normalText, 14, "#666666", false);
        TextView link = createText(context, linkText, 14, "#1976D2", true);

        AuthCardAnimator.applyLightAnimation(link, onClick);

        container.addView(normal);
        container.addView(link);

        return container;
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