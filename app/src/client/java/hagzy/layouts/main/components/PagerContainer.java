package hagzy.layouts.main.components;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import hagzy.adapters.ViewPagerAdapter;
import hagzy.layouts.main.models.TabData;

/**
 * ViewPager Container
 * يدير الـ Fragments والتنقل بينها
 */
public class PagerContainer {

    private Context context;
    private ViewPager2 pager;
    private List<TabData> tabs = new ArrayList<>();

    private OnPageChangeListener listener;

    public PagerContainer(Context context) {
        this.context = context;
        buildPager();
    }

    private void buildPager() {
        pager = new ViewPager2(context);
        pager.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1));
        pager.setBackgroundColor(Color.parseColor("#F5F5F5"));
    }

    /**
     * تعيين التبويبات والـ Fragments
     */
    public void setTabs(List<TabData> tabsData) {
        this.tabs = tabsData;

        List<Fragment> fragments = new ArrayList<>();
        for (TabData tab : tabs) {
            fragments.add(tab.getFragment());
        }

        if (context instanceof FragmentActivity) {
            pager.setAdapter(new ViewPagerAdapter((FragmentActivity) context, fragments));
        }
    }

    /**
     * تعيين الصفحة الحالية
     */
    public void setCurrentPage(int position, boolean smooth) {
        if (pager != null) {
            pager.setCurrentItem(position, smooth);
        }
    }

    /**
     * تسجيل Callback للتغيير
     */
    public void setupPageChangeCallback() {
        pager.post(() -> {
            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    if (listener != null) {
                        listener.onPageSelected(position);
                    }
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (listener != null) {
                        listener.onPageScrolled(position, positionOffset);
                    }
                }
            });
        });
    }

    /**
     * Listener للتغيير
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.listener = listener;
    }

    public ViewPager2 getView() {
        return pager;
    }

    public interface OnPageChangeListener {
        void onPageSelected(int position);
        void onPageScrolled(int position, float offset);
    }
}