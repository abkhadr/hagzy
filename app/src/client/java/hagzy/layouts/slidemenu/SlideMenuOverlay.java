package hagzy.layouts.slidemenu;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import hagzy.layouts.slidemenu.models.MenuConfig;

/**
 * الخلفية المعتمة للقائمة
 * تغطي الشاشة عند فتح القائمة
 */
public class SlideMenuOverlay {

    private Context context;
    private MenuConfig config;
    private View overlay;
    private Runnable onOverlayClick;

    public SlideMenuOverlay(Context context, MenuConfig config) {
        this.context = context;
        this.config = config;
        buildOverlay();
    }

    private void buildOverlay() {
        overlay = new View(context);
        overlay.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        overlay.setBackgroundColor(config.getOverlayColor());
        overlay.setVisibility(View.GONE);
        overlay.setAlpha(0f);

        // الضغط على الـ Overlay يغلق القائمة
        overlay.setOnClickListener(v -> {
            if (onOverlayClick != null) {
                onOverlayClick.run();
            }
        });
    }

    public void setOnOverlayClick(Runnable onClick) {
        this.onOverlayClick = onClick;
    }

    public View getView() {
        return overlay;
    }
}