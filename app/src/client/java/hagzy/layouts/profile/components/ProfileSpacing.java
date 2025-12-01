package hagzy.layouts.profile.components;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bytepulse.hagzy.helpers.UiHelper;

/**
 * ProfileSpacing - فواصل بين الأقسام
 */
public class ProfileSpacing {

    public static View create(Context context, int dpHeight) {
        View spacing = new View(context);
        spacing.setBackgroundColor(Color.parseColor("#F0F0F0"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiHelper.dp(context, dpHeight)
        );
        spacing.setLayoutParams(params);
        return spacing;
    }
}