package hagzy.ui;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import hagzy.Controller.SlideMenuController;
import com.bytepulse.hagzy.R;

import hagzy.InboxActivity;
import hagzy.MainActivity;
import hagzy.adapters.ViewPagerAdapter;
import hagzy.fragments.FieldsFragment;
import hagzy.fragments.MyBookingsFragment;
import hagzy.fragments.TournamentFragment;
import com.bytepulse.hagzy.helpers.ThemeManager;
import hagzy.models.TabItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainLayout {

    private final Context ctx;
    private FrameLayout root;
    private LinearLayout content;
    private LinearLayout tabLayout;
    private ViewPager2 pager;
    private TextView pageTitle;
    private View indicator;
    private final List<View> tabs = new ArrayList<>();

    public MainLayout(Context context) {
        this.ctx = context;
        build();
    }

    // ════════════════════════════════════════════════════════════
    // 🏗️ Build UI
    // ════════════════════════════════════════════════════════════

    private void build() {
        root = new FrameLayout(ctx);
        root.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        root.setBackgroundColor(Color.WHITE);

        content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        content.setBackgroundColor(Color.WHITE);

        content.addView(buildTopBar());
        content.addView(createDivider());
        content.addView(buildTabsContainer());
        content.addView(createDivider());
        content.addView(buildPager());

        root.addView(content);
        setupSlideMenu();
        applyInsets();
    }

    // ════════════════════════════════════════════════════════════
    // 📱 Top Bar
    // ════════════════════════════════════════════════════════════

    private LinearLayout buildTopBar() {
        LinearLayout bar = new LinearLayout(ctx);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(20), dp(12), dp(20), dp(12));
        bar.setBackgroundColor(Color.WHITE);

        pageTitle = txt("حجز", 28, "#000000", true);
        pageTitle.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout actions = new LinearLayout(ctx);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout notifBtn = iconBtn(R.drawable.inbox, "#F5F5F5", "#000000");
        notifBtn.setOnClickListener(v -> {
            ctx.startActivity(new Intent(ctx, InboxActivity.class));
        });

        LinearLayout menuBtn = iconBtn(R.drawable.bars_3, "#000000", "#FFFFFF");

        actions.addView(notifBtn);
        actions.addView(menuBtn);

        bar.addView(pageTitle);
        bar.addView(actions);

        // Setup menu after creation
        bar.post(() -> {
            SlideMenuController menu = new SlideMenuController(ctx, root, content);
            setupMenuContent(menu);
            menuBtn.setOnClickListener(v -> menu.toggle());
        });

        return bar;
    }

    // ════════════════════════════════════════════════════════════
    // 🔖 Tabs Container
    // ════════════════════════════════════════════════════════════

    private FrameLayout buildTabsContainer() {
        FrameLayout container = new FrameLayout(ctx);
        container.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(56)));
        container.setPadding(dp(16), dp(8), dp(16), dp(8));
        container.setBackgroundColor(Color.WHITE);

        // Background
        LinearLayout bgHolder = new LinearLayout(ctx);
        bgHolder.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setColor(Color.parseColor("#F5F5F5"));
        bgHolder.setClipToOutline(true);
        bgShape.setCornerRadius(dp(20));
        bgHolder.setBackground(bgShape);

        // Moving indicator
        indicator = new View(ctx);
        GradientDrawable indShape = new GradientDrawable();
        indShape.setColor(Color.parseColor("#000000"));
        indShape.setCornerRadius(dp(20));
        indicator.setBackground(indShape);
        FrameLayout.LayoutParams indParams = new FrameLayout.LayoutParams(0, -1);
        indicator.setLayoutParams(indParams);

        // Tabs
        tabLayout = new LinearLayout(ctx);
        tabLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabLayout.setGravity(Gravity.CENTER);
        tabLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

        List<String> titles = Arrays.asList(t("bookings"), "الملاعب", "المسابقات");
        for (int i = 0; i < titles.size(); i++) {
            final int idx = i;
            LinearLayout tab = new LinearLayout(ctx);
            tab.setOrientation(LinearLayout.VERTICAL);
            tab.setGravity(Gravity.CENTER);
            tab.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1));

            TextView label = txt(titles.get(i), 15, "#666666", true);
            label.setGravity(Gravity.CENTER);
            label.setTag("label");
            tab.addView(label);

            tab.setOnClickListener(v -> {
                pager.setCurrentItem(idx, true);
                vibrate();
            });

            tabs.add(tab);
            tabLayout.addView(tab);
        }

        container.addView(bgHolder);
        container.addView(indicator);
        container.addView(tabLayout);

        tabLayout.post(() -> {
            int w = tabLayout.getWidth() / titles.size();
            ViewGroup.LayoutParams lp = indicator.getLayoutParams();
            lp.width = w;
            indicator.setLayoutParams(lp);
            updateTabs(0);
        });

        return container;
    }

    // ════════════════════════════════════════════════════════════
    // 📄 ViewPager
    // ════════════════════════════════════════════════════════════

    private ViewPager2 buildPager() {
        pager = new ViewPager2(ctx);
        pager.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1));
        pager.setBackgroundColor(Color.parseColor("#F5F5F5"));

        List<Fragment> frags = Arrays.asList(
                new MyBookingsFragment(),
                new FieldsFragment(),
                new TournamentFragment()
        );

        pager.setAdapter(new ViewPagerAdapter(
                ((androidx.fragment.app.FragmentActivity) ctx), frags
        ));

        pager.post(() -> {
            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int pos) {
                    updateTabs(pos);
                    vibrate();
                }

                @Override
                public void onPageScrolled(int pos, float offset, int px) {
                    if (indicator.getWidth() > 0) {
                        indicator.setTranslationX(-(pos + offset) * indicator.getWidth());
                    }
                }
            });
        });

        return pager;
    }

    // ════════════════════════════════════════════════════════════
    // 📋 Slide Menu
    // ════════════════════════════════════════════════════════════

    private void setupSlideMenu() {
        // Menu setup is done in buildTopBar() post callback
    }

    private void setupMenuContent(SlideMenuController menu) {
        LinearLayout menuLayout = new LinearLayout(ctx);
        menuLayout.setPadding(dp(16), dp(24), dp(16), dp(16));
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menu.addMenuItem(menuLayout);

        // Profile
        menuLayout.addView(buildProfile());
        menuLayout.addView(createDivider());

        // Menu Items
        List<MenuSection> sections = Arrays.asList(
                new MenuSection("الأصول", Arrays.asList(
                        new MenuItem(R.drawable.wallet, "رصيد")
                )),
                new MenuSection("الدعم الفني & الإعدادات", Arrays.asList(
                        new MenuItem(R.drawable.adjustments_horizontal, "الدعم الفني")
                )),
                new MenuSection("", Arrays.asList(
                        new MenuItem(R.drawable.cog_8, t("settings.title"))
                ))
        );

        int idx = 0;
        for (MenuSection sec : sections) {
            if (!sec.title.isEmpty()) {
                TextView title = txt(sec.title, 12, "#999999", true);
                title.setPadding(dp(12), dp(16), dp(12), dp(8));
                menuLayout.addView(title);
            }

            for (MenuItem item : sec.items) {
                final int i = idx++;
                menuLayout.addView(createMenuItem(item, i, menu));
            }

//            if (!sec.title.isEmpty()) {
//                menuLayout.addView(createDivider());
//            }
        }
    }

    private LinearLayout buildProfile() {
        LinearLayout profile = new LinearLayout(ctx);
        profile.setOrientation(LinearLayout.VERTICAL);
        profile.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(24);
        profile.setLayoutParams(params);

        ImageView img = new ImageView(ctx);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dp(80), dp(80));
        imgParams.bottomMargin = dp(12);
        img.setLayoutParams(imgParams);
        img.setImageResource(R.drawable.bars_3);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable imgBg = new GradientDrawable();
        imgBg.setColor(Color.parseColor("#F5F5F5"));
        imgBg.setCornerRadius(dp(40));
        img.setBackground(imgBg);

        TextView name = txt("مستخدم", 20, "#000000", true);
        name.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(-2, -2);
        nameParams.bottomMargin = dp(4);
        name.setLayoutParams(nameParams);

        TextView email = txt("user@example.com", 14, "#666666", false);
        email.setGravity(Gravity.CENTER);

        profile.addView(img);
        profile.addView(name);
        profile.addView(email);

        return profile;
    }

    private LinearLayout createMenuItem(MenuItem item, int idx, SlideMenuController menu) {
        LinearLayout btn = new LinearLayout(ctx);
        btn.setOrientation(LinearLayout.HORIZONTAL);
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(56));
        params.bottomMargin = dp(4);
        btn.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(12));
        btn.setBackground(bg);

        ImageView icon = new ImageView(ctx);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(22), dp(22));
        iconParams.setMarginEnd(dp(12));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(item.icon);
        icon.setColorFilter(Color.parseColor("#000000"));

        TextView label = txt(item.label, 15, "#000000", true);
        label.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

        ImageView arrow = new ImageView(ctx);
        arrow.setLayoutParams(new LinearLayout.LayoutParams(dp(18), dp(18)));
        arrow.setImageResource(R.drawable.chevron_right);
        arrow.setColorFilter(Color.parseColor("#999999"));

        btn.addView(icon);
        btn.addView(label);
        btn.addView(arrow);

        btn.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.7f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1f);
                    break;
            }
            return false;
        });

        btn.setOnClickListener(v -> {
            try {
                MainActivity activity = (MainActivity) ctx;
                switch (idx) {
                    case 0: activity.openPage("wallet"); break;
                    case 1: break;
                    case 2: activity.openPage("settings"); break;
                }
                v.postDelayed(menu::toggle, 200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return btn;
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helpers
    // ════════════════════════════════════════════════════════════

    private LinearLayout iconBtn(int icon, String bg, String fg) {
        LinearLayout btn = new LinearLayout(ctx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(44), dp(44));
        params.setMarginStart(dp(8));
        btn.setLayoutParams(params);
        btn.setGravity(Gravity.CENTER);

        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor(bg));
        shape.setCornerRadius(dp(22));
        btn.setBackground(shape);

        ImageView img = new ImageView(ctx);
        img.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        img.setImageResource(icon);
        img.setColorFilter(Color.parseColor(fg));
        btn.addView(img);

        btn.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.setScaleX(0.95f);
                    v.setScaleY(0.95f);
                    v.setAlpha(0.7f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                    v.setAlpha(1f);
                    break;
            }
            return false;
        });

        return btn;
    }

    private View createDivider() {
        View div = new View(ctx);
        div.setBackgroundColor(Color.parseColor("#E0E0E0"));
        div.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(1)) {{bottomMargin = dp(4);}});
        return div;
    }

    private TextView txt(String text, int size, String color, boolean bold) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        tv.setTextColor(Color.parseColor(color));
        tv.setTypeface(bold ? ThemeManager.fontBold() : ThemeManager.fontRegular());
        return tv;
    }

    private void updateTabs(int selected) {
        for (int i = 0; i < tabs.size(); i++) {
            TextView label = (TextView) tabs.get(i).findViewWithTag("label");
            if (label != null) {
                label.setTextColor(Color.parseColor(i == selected ? "#FFFFFF" : "#666666"));
            }
        }
    }

    private void vibrate() {
        Vibrator vib = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        if (vib != null && vib.hasVibrator()) {
            vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            content.setPadding(0, top, 0, bottom);
            return insets;
        });
    }

    private int dp(int value) {
        return (int) (value * ctx.getResources().getDisplayMetrics().density);
    }

    public FrameLayout getView() {
        return root;
    }

    // ════════════════════════════════════════════════════════════
    // 📦 Models
    // ════════════════════════════════════════════════════════════

    private static class MenuItem {
        int icon;
        String label;
        MenuItem(int icon, String label) {
            this.icon = icon;
            this.label = label;
        }
    }

    private static class MenuSection {
        String title;
        List<MenuItem> items;
        MenuSection(String title, List<MenuItem> items) {
            this.title = title;
            this.items = items;
        }
    }
}