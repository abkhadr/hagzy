package com.bytepulse.hagzy.layouts.auth.components;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.ThemeManager;

/**
 * عنوان الصفحة مع عنوان فرعي
 */
public class AuthTitle {
    private final LinearLayout container;

    public AuthTitle(Context context, String title, String subtitle) {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(context, 40);
        container.setLayoutParams(params);

        // العنوان الرئيسي
        TextView titleView = createText(context, title, 32, "#000000", true);
        titleView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(context, 8);
        titleView.setLayoutParams(titleParams);
        container.addView(titleView);

        // العنوان الفرعي
        TextView subtitleView = createText(context, subtitle, 16, "#666666", false);
        subtitleView.setGravity(Gravity.CENTER);
        container.addView(subtitleView);
    }

    public View getView() {
        return container;
    }

    private TextView createText(Context context, String text, int size, String color, boolean bold) {
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

    private int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}