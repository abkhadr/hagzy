package hagzy.layouts.main;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.CoachMarkHelper;
import com.bytepulse.hagzy.helpers.StatusBarHelper;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import hagzy.Controller.SlideMenuController;
import hagzy.activities.InboxActivity;
import hagzy.activities.MainActivity;
import hagzy.fragments.tabs.FieldsFragment;
import hagzy.fragments.tabs.LobbyFragment;
import hagzy.fragments.tabs.PlayerProfileFragment;
import hagzy.layouts.main.components.PagerContainer;
import hagzy.layouts.main.components.TabsContainer;
import hagzy.layouts.main.components.TopBar;
import hagzy.layouts.main.models.TabData;
import hagzy.layouts.main.utils.VibrationHelper;

public class MainLayout {

    private Context context;
    private FrameLayout root;
    private LinearLayout content;

    private TopBar topBar;
    private TabsContainer tabsContainer;
    private PagerContainer pagerContainer;
    private SlideMenuController slideMenu;
    public TabData currentTab;

    public MainLayout(Context context) {
        this.context = context;
        buildUI();
        setupStatusBar();
        showCoachMarksIfNeeded();
    }

    private void buildUI() {
        root = new FrameLayout(context);
        root.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        root.setBackgroundColor(Color.WHITE);

        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        content.setBackgroundColor(Color.WHITE);

        buildTopBar();
        buildTabs();
        buildPager();

        root.addView(content);
        setupSlideMenu();
        applyInsets();
    }

    private void buildTopBar() {
        topBar = new TopBar(context, t("app.name"));
        topBar.setOnActionClickListener(new TopBar.OnActionClickListener() {
            @Override
            public void onNotificationClick() {
                context.startActivity(new Intent(context, InboxActivity.class));
            }

            @Override
            public void onMenuClick() {
                if (slideMenu != null) {
                    slideMenu.toggle();
                }
            }
        });

        content.addView(topBar.getView());
    }

    private void buildTabs() {
        tabsContainer = new TabsContainer(context);
        PlayerProfileFragment profileFragment = new PlayerProfileFragment();
        Bundle a_args = new Bundle();
        a_args.putInt("tab_id", 0);
        profileFragment.setArguments(a_args);

        LobbyFragment LobbyFragment = new LobbyFragment();
        Bundle b_args = new Bundle();
        b_args.putInt("tab_id", 1);
        profileFragment.setArguments(b_args);

        FieldsFragment FieldsFragment = new FieldsFragment();
        Bundle args = new Bundle();
        args.putInt("tab_id", 2);
        profileFragment.setArguments(args);
        List<TabData> tabs = Arrays.asList(
                new TabData(0,t("field.my_profile"), profileFragment),
                new TabData(1,t("field.matches"), LobbyFragment),
                new TabData(2,t("field.stadiums"), FieldsFragment)
        );

        tabsContainer.setTabs(tabs);

        tabsContainer.setOnTabSelectedListener(position -> {
            pagerContainer.setCurrentPage(position, true);
            VibrationHelper.vibrateLight(context);
        });

        content.addView(tabsContainer.getView());
    }

    private void buildPager() {
        pagerContainer = new PagerContainer(context);

        List<TabData> tabs = Arrays.asList(
                new TabData(0,t("field.my_profile"), new PlayerProfileFragment()),
                new TabData(1,t("field.matches"), new LobbyFragment()),
                new TabData(2,t("field.stadium"), new FieldsFragment())
        );

        pagerContainer.setTabs(tabs);
        pagerContainer.setCurrentPage(1, false);

        pagerContainer.setOnPageChangeListener(new PagerContainer.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentTab = tabs.get(position);
                tabsContainer.selectTab(position);
                tabsContainer.pulseIndicator();
                VibrationHelper.vibrateLight(context);
            }

            @Override
            public void onPageScrolled(int position, float offset) {
                tabsContainer.updateIndicatorPosition(position, offset);
            }
        });

        pagerContainer.setupPageChangeCallback();

        content.addView(pagerContainer.getView());
    }

    private void setupSlideMenu() {
        slideMenu = new SlideMenuController(context, root, content);
        addMenuItems();
    }

    private void addMenuItems() {
        LinearLayout menuLayout = new LinearLayout(context);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setPadding(dp(16), 0, dp(16), 0);
        slideMenu.addMenuItem(menuLayout);

//        menuLayout.addView(buildProfile());
//        menuLayout.addView(createDivider());

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
                TextView title = createText(sec.title, 12, "#999999", 3);
                title.setPadding(dp(12), dp(16), dp(12), dp(8));
                menuLayout.addView(title);
            }

            for (MenuItem item : sec.items) {
                final int i = idx++;
                menuLayout.addView(createMenuItem(item, i));
            }
        }
    }

    private LinearLayout buildProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userName = (user != null && user.getDisplayName() != null)
                ? user.getDisplayName()
                : "مستخدم";

        String userEmail = (user != null && user.getEmail() != null)
                ? user.getEmail()
                : "no-email";

        Uri photoUri = (user != null) ? user.getPhotoUrl() : null;

        LinearLayout profile = new LinearLayout(context);
        profile.setOrientation(LinearLayout.VERTICAL);
        profile.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(24);
        profile.setLayoutParams(params);

        ImageView img = new ImageView(context);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dp(80), dp(80));
        imgParams.bottomMargin = dp(12);
        img.setLayoutParams(imgParams);


        if (photoUri != null) {
            new Thread(() -> {
                try {
                    InputStream in = new java.net.URL(photoUri.toString()).openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(in);

                    img.post(() -> img.setImageBitmap(bitmap));

                } catch (Exception ignored) {}
            }).start();
        }

        img.setScaleType(ImageView.ScaleType.CENTER_CROP);


        GradientDrawable imgBg = new GradientDrawable();
        imgBg.setColor(Color.parseColor("#F5F5F5"));
        imgBg.setCornerRadius(dp(40));
        img.setBackground(imgBg);
        img.setClipToOutline(true);


        TextView name = createText(userName, 24, "#000000", 3);
        name.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(-2, -2);
        nameParams.bottomMargin = dp(4);
        name.setLayoutParams(nameParams);

        TextView email = createText(userEmail, 14, "#666666", 1);
        email.setGravity(Gravity.CENTER);

        profile.addView(img);
        profile.addView(name);
        profile.addView(email);

        return profile;
    }

    private LinearLayout createMenuItem(MenuItem item, int idx) {
        LinearLayout btn = new LinearLayout(context);
        btn.setOrientation(LinearLayout.HORIZONTAL);
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(56));
        params.bottomMargin = dp(4);
        btn.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#FAFAFA"));
        bg.setCornerRadius(dp(12));
        btn.setBackground(bg);

        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(22), dp(22));
        iconParams.setMarginEnd(dp(12));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(item.icon);
        icon.setColorFilter(Color.parseColor("#000000"));

        TextView label = createText(item.label, 15, "#000000", 3);
        label.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

        ImageView arrow = new ImageView(context);
        arrow.setLayoutParams(new LinearLayout.LayoutParams(dp(18), dp(18)));
        arrow.setImageResource(R.drawable.chevron_right);
        arrow.setColorFilter(Color.parseColor("#CCCCCC"));

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
                MainActivity activity = (MainActivity) context;
                switch (idx) {
                    case 0: activity.openPage("wallet"); break;
                    case 1: activity.openPage("support"); break;
                    case 2: activity.openPage("settings"); break;
                }
                v.postDelayed(() -> slideMenu.toggle(), 200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return btn;
    }

    private void showCoachMarksIfNeeded() {
        if (!(context instanceof Activity)) return;

        if (!CoachMarkHelper.shouldShow(context)) {
            return;
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startCoachMarks();
        }, 500);
    }

    private void startCoachMarks() {
        if (!(context instanceof Activity)) return;

        CoachMarkHelper coach = new CoachMarkHelper((Activity) context);

        View menuBtn = topBar.getMenuButton();
        if (menuBtn != null) {
            coach.addStep(
                    menuBtn,
                    "القائمة الجانبية",
                    "افتح القائمة للوصول إلى الإعدادات، الإشعارات، والدعم الفني",
                    0
            );
        }

        View notifBtn = topBar.getNotificationButton();
        if (notifBtn != null) {
            coach.addStep(
                    notifBtn,
                    "الإشعارات",
                    "تابع جميع التحديثات والرسائل الخاصة بحجوزاتك ومبارياتك من هنا",
                    0
            );
        }

        View tabsView = tabsContainer.getView();
        coach.addStep(
                tabsView,
                "التنقل بين الأقسام",
                "استخدم التبويبات للتنقل بين ملفك الشخصي، المباريات، والملاعب المتاحة",
                2
        );

        View fieldsTab = tabsContainer.getTabView(2);
        if (fieldsTab != null) {
            coach.addStep(
                    fieldsTab,
                    "اكتشف الملاعب",
                    "اضغط هنا لاستعراض جميع الملاعب القريبة منك مع التقييمات والأسعار",
                    2
            );
        }

        coach.setOnCompleteListener(() -> {});

        coach.start();
    }

    private void setupStatusBar() {
        if (context instanceof Activity) {
            StatusBarHelper.setTransparentLightStatusBar((Activity) context);
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

    public FrameLayout getView() {
        return root;
    }

    public void setTitle(String title) {
        if (topBar != null) {
            topBar.setTitle(title);
        }
    }

    public void navigateToTab(int position) {
        if (pagerContainer != null) {
            pagerContainer.setCurrentPage(position, true);
        }
    }

    public void toggleMenu() {
        if (slideMenu != null) {
            slideMenu.toggle();
        }
    }

    public SlideMenuController getSlideMenu() {
        return slideMenu;
    }

    public void resetAndShowCoachMarks() {
        CoachMarkHelper.markAsShown(context);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startCoachMarks();
        }, 300);
    }

    private TextView createText(String text, int size, String color, int weight) {
        return UiHelper.createText(context, text, size, color, weight);
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

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