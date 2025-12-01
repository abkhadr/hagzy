package hagzy.layouts.slidemenu.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.Locale;

/**
 * مساعد لمعالجة RTL/LTR بشكل صحيح
 * يوفر دوال للتحقق من الاتجاه وضبط المواضع
 */
public class MenuRTLHelper {

    /**
     * التحقق من اللغة RTL
     */
    public static boolean isRTL(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        int direction = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return direction == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                direction == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    /**
     * الحصول على Gravity المناسب للقائمة
     */
    public static int getMenuGravity(Context context) {
        return isRTL(context) ? Gravity.END : Gravity.START;
    }

    /**
     * الحصول على Gravity المعاكس (للأيقونات)
     */
    public static int getOppositeGravity(Context context) {
        return isRTL(context) ? Gravity.START : Gravity.END;
    }

    /**
     * ضبط اتجاه LinearLayout حسب اللغة
     */
    public static void setLayoutDirection(View view, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setLayoutDirection(isRTL(context) ?
                    View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
    }

    /**
     * الحصول على موضع القائمة (X) حسب الاتجاه
     * @param menuWidth عرض القائمة
     */
    public static float getMenuClosedX(Context context, int menuWidth) {
        return isRTL(context) ? menuWidth : -menuWidth;
    }

    /**
     * الحصول على موضع القائمة (X) عند الفتح
     */
    public static float getMenuOpenX() {
        return 0f;
    }

    /**
     * ضبط Padding للعناصر حسب الاتجاه
     */
    public static void setPaddingRTL(View view, int horizontal, int vertical) {
        if (isRTL(view.getContext())) {
            view.setPadding(horizontal, vertical, horizontal, vertical);
        } else {
            view.setPadding(horizontal, vertical, horizontal, vertical);
        }
    }

    /**
     * ضبط Margin للعناصر حسب الاتجاه
     */
    public static void setMarginRTL(View view, int start, int top, int end, int bottom) {
        if (view.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.setMarginStart(start);
                params.setMarginEnd(end);
            } else {
                if (isRTL(view.getContext())) {
                    params.setMargins(end, top, start, bottom);
                } else {
                    params.setMargins(start, top, end, bottom);
                }
            }
            view.setLayoutParams(params);
        } else if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.setMarginStart(start);
                params.setMarginEnd(end);
            } else {
                if (isRTL(view.getContext())) {
                    params.setMargins(end, top, start, bottom);
                } else {
                    params.setMargins(start, top, end, bottom);
                }
            }
            view.setLayoutParams(params);
        }
    }

    /**
     * الحصول على أيقونة السهم المناسبة حسب الاتجاه
     */
    public static int getArrowIcon(Context context, int ltrIcon, int rtlIcon) {
        return isRTL(context) ? rtlIcon : ltrIcon;
    }

    /**
     * ضبط اتجاه النص والأيقونات في العنصر
     */
    public static void configureMenuItemDirection(LinearLayout container, Context context) {
        if (isRTL(context)) {
            // في RTL: أيقونة - نص - سهم
            container.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            // في LTR: أيقونة - نص - سهم
            container.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    /**
     * ضبط Gravity للنصوص حسب الاتجاه
     */
    public static int getTextGravity(Context context) {
        return isRTL(context) ? Gravity.END : Gravity.START;
    }

    /**
     * الحصول على Drawable المناسب للخلفية حسب الاتجاه
     * مفيد للعناصر التي لها shadow أو gradient
     */
    public static int getStartAlignedGravity(Context context) {
        return isRTL(context) ? Gravity.END : Gravity.START;
    }

    /**
     * ضبط TranslationX للأنيميشن حسب الاتجاه
     */
    public static float getSlideInTranslation(Context context, int distance) {
        return isRTL(context) ? distance : -distance;
    }

    /**
     * ضبط TranslationX للأنيميشن حسب الاتجاه (للخروج)
     */
    public static float getSlideOutTranslation(Context context, int distance) {
        return isRTL(context) ? -distance : distance;
    }

    /**
     * تطبيق الاتجاه على كامل Activity أو Fragment
     */
    public static void applyRTL(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = context.getResources().getConfiguration();
            if (isRTL(context)) {
                config.setLayoutDirection(new Locale("ar"));
            } else {
                config.setLayoutDirection(Locale.ENGLISH);
            }
        }
    }

    /**
     * الحصول على موضع البداية للعنصر (Start)
     */
    public static int getStartPosition(Context context, int totalWidth, int itemWidth) {
        if (isRTL(context)) {
            return totalWidth - itemWidth;
        } else {
            return 0;
        }
    }

    /**
     * الحصول على موضع النهاية للعنصر (End)
     */
    public static int getEndPosition(Context context, int totalWidth, int itemWidth) {
        if (isRTL(context)) {
            return 0;
        } else {
            return totalWidth - itemWidth;
        }
    }
}