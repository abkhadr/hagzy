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
 * فاصل "أو" بين الأزرار
 */
public class AuthDivider {
    private final LinearLayout container;

    public AuthDivider(Context context) {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(context, 8);
        params.bottomMargin = dp(context, 16);
        container.setLayoutParams(params);

        View line1 = new View(context);
        LinearLayout.LayoutParams lineParams1 = new LinearLayout.LayoutParams(0, dp(context, 1), 1);
        line1.setLayoutParams(lineParams1);
        line1.setBackgroundColor(Color.parseColor("#E0E0E0"));

        TextView orText = createText(context, "  أو  ", 14, "#999999", false);

        View line2 = new View(context);
        LinearLayout.LayoutParams lineParams2 = new LinearLayout.LayoutParams(0, dp(context, 1), 1);
        line2.setLayoutParams(lineParams2);
        line2.setBackgroundColor(Color.parseColor("#E0E0E0"));

        container.addView(line1);
        container.addView(orText);
        container.addView(line2);
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