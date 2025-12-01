package hagzy.layouts.slidemenu.items;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import hagzy.layouts.slidemenu.models.MenuConfig;

/**
 * فاصل بين عناصر القائمة
 */
public class MenuDivider {

    private Context context;
    private MenuConfig config;
    private View divider;

    public MenuDivider(Context context, MenuConfig config) {
        this.context = context;
        this.config = config;
        buildDivider();
    }

    private void buildDivider() {
        divider = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(1)
        );
        params.setMargins(
                dpToPx(config.getItemPadding()),
                dpToPx(8),
                dpToPx(config.getItemPadding()),
                dpToPx(8)
        );
        divider.setLayoutParams(params);
        divider.setBackgroundColor(config.getDividerColor());
    }

    public View getView() {
        return divider;
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}