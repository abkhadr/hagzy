package hagzy.layouts.main.tabs;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.main.models.TabData;
import hagzy.layouts.main.utils.ColorAnimator;

/**
 * عنصر تبويب واحد
 */
public class TabItem {

    private Context context;
    private LinearLayout container;
    private TextView label;
    private TabData data;

    // الألوان
    private static final int SELECTED_COLOR = Color.parseColor("#000000");
    private static final int UNSELECTED_COLOR = Color.parseColor("#666666");

    // Animation duration
    private static final int ANIMATION_DURATION = 100;

    public TabItem(Context context, TabData data, OnTabClickListener listener, boolean defaultIndex) {
        this.context = context;
        this.data = data;
        if(defaultIndex) {
            buildTab(listener, true);
        }else{
            buildTab(listener, false);
        }
    }

    private void buildTab(OnTabClickListener listener, boolean defaultIndex) {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1));

        // النص
        label = new TextView(context);
        label.setText(data.getTitle());
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        label.setTextColor(defaultIndex ? SELECTED_COLOR : UNSELECTED_COLOR);
        label.setGravity(Gravity.CENTER);
        label.setTranslationY(dpf(-1f));
        label.setTypeface(defaultIndex ? ThemeManager.fontBold() : ThemeManager.fontSemiBold());
        label.setTag("label");

        container.addView(label);

        // الضغط
        container.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTabClick();
            }
        });
    }

    /**
     * تحديد التبويب مع animation
     */
    public void setSelected(boolean selected) {
        int targetColor = selected ? SELECTED_COLOR : UNSELECTED_COLOR;
        ColorAnimator.animateTextColor(label, targetColor);
        animateFontWeight(selected);
    }

    /**
     * عمل animation للـ font weight بس
     */
    private void animateFontWeight(boolean toBold) {
        Typeface targetFont = toBold ? ThemeManager.fontBold() : ThemeManager.fontSemiBold();

        // Animation بسيط للـ font weight
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                label.setTypeface(targetFont);
            }
        });

        animator.start();
    }

    /**
     * الحصول على الـ View
     */
    public LinearLayout getView() {
        return container;
    }

    /**
     * الحصول على البيانات
     */
    public TabData getData() {
        return data;
    }

    /**
     * Callback للضغط
     */
    public interface OnTabClickListener {
        void onTabClick();
    }

    float dpf(float value) {
        return UiHelper.dpFloat(context, value);
    }
}