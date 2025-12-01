    package hagzy.activities;

    import static com.bytepulse.hagzy.helpers.TranslationManager.t;

    import android.annotation.SuppressLint;
    import android.graphics.Color;
    import android.os.Bundle;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.FrameLayout;
    import android.widget.LinearLayout;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.fragment.app.Fragment;
    import androidx.fragment.app.FragmentActivity;
    import androidx.viewpager2.adapter.FragmentStateAdapter;
    import androidx.viewpager2.widget.ViewPager2;
    import hagzy.fragments.old.HomeFragment;
    import hagzy.layouts.settings.SettingsLayout;
    import hagzy.layouts.support.SupportLayout;
    import hagzy.layouts.wallet.WalletLayout;

    public class MainActivity extends AppCompatActivity {
        private ViewPager2 pager;
        public DynamicFragment dynamicFragment = new DynamicFragment();
        public WalletLayout walletLayout;
        public SupportLayout supportLayout;
        public SettingsLayout settingsLayout;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            walletLayout = new WalletLayout(this);
            dynamicFragment.setWalletLayout(walletLayout);

            supportLayout = new SupportLayout(this);
            dynamicFragment.setSupportLayout(supportLayout);

            settingsLayout = new SettingsLayout(this);
            dynamicFragment.setSettingsLayout(settingsLayout);

            pager = new ViewPager2(this);
            pager.setId(View.generateViewId());
            pager.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
            setContentView(pager);
            pager.setAdapter(new MainPagerAdapter(this));
            pager.setUserInputEnabled(false);
        }

        public void openPage(String type){
            dynamicFragment.setPage(type);
            pager.setCurrentItem(1, true);
        }

        @SuppressLint("GestureBackNavigation")
        @Override
        public void onBackPressed() {
            int currentItem = pager.getCurrentItem();

            if (currentItem == 1 && dynamicFragment != null) {
                boolean handled = dynamicFragment.onPressBack();
                if (!handled) {
                    pager.setCurrentItem(0, true);
                }
            } else {
                super.onBackPressed();
            }
        }

        public class MainPagerAdapter extends FragmentStateAdapter{
            public MainPagerAdapter(@NonNull FragmentActivity fa){super(fa);}
            @NonNull
            @Override
            public Fragment createFragment(int position){
                Fragment fragment;

                switch (position) {
                    case 1:
                        dynamicFragment.setWalletLayout(walletLayout);
                        fragment = dynamicFragment;
                        break;

                    case 2:
                        dynamicFragment.setSupportLayout(supportLayout);
                        fragment = dynamicFragment;
                        break;

                    case 3:
                        dynamicFragment.setSettingsLayout(settingsLayout);
                        fragment = dynamicFragment;
                        break;

                    default:
                        fragment = new HomeFragment();
                        break;
                }
                return fragment;
            }
            @Override
            public int getItemCount(){return 2;}
        }




        public static class DynamicFragment extends Fragment{
            private String pageType="none";
            private FrameLayout root;

            private WalletLayout walletLayout;
            private SupportLayout supportLayout;
            private SettingsLayout settingsLayout;
            private WindowInsetsCompat lastInsets;

            public void setWalletLayout(WalletLayout layout){
                this.walletLayout = layout;
            }
            public void setSupportLayout(SupportLayout layout){
                this.supportLayout = layout;
            }
            public void setSettingsLayout(SettingsLayout layout){
                this.settingsLayout = layout;
            }


            @Nullable
            @Override
            public View onCreateView(@NonNull android.view.LayoutInflater inflater,@Nullable android.view.ViewGroup container,@Nullable Bundle savedInstanceState){
                root=new FrameLayout(requireContext());
                root.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
                if (walletLayout == null) {
                    walletLayout = new WalletLayout(getContext());
                }
                if (supportLayout == null) {
                    supportLayout = new SupportLayout(getContext());
                }
                if (settingsLayout == null) {
                    settingsLayout = new SettingsLayout(getContext());
                }
                root.setBackgroundColor(Color.TRANSPARENT);
                ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                    lastInsets = insets;

                    applyInsetsToHeader();

                    return insets;
                });

                render();
                return root;
            }

            private void render() {
                if (root == null) return;

                root.removeAllViews();

                switch (pageType) {
                    case "wallet":
                        root.addView(walletLayout.getView());
                        break;
                    case "support":
                        root.addView(supportLayout.getView());
                        break;
                    case "settings":
                        root.addView(settingsLayout.getView());
                        break;
                }
                applyInsetsToHeader();
                ViewCompat.requestApplyInsets(root);
            }

            private void applyInsetsToHeader() {
                if (lastInsets == null) return;

                int top = lastInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

                View header = null;
                switch (pageType) {
                    case "wallet":
                        header = walletLayout.getHeader();
                        break;
                    case "support":
                        header = supportLayout.getHeader();
                        break;
                    case "settings":
                        header = settingsLayout.getHeader();
                        break;
                }

                if (header != null) {
                    ViewGroup.MarginLayoutParams params =
                            (ViewGroup.MarginLayoutParams) header.getLayoutParams();

                    params.topMargin = top;
                    header.setLayoutParams(params);
                }
            }



            public void setPage(String type){
                pageType=type;
                render();
            }

            public String getPageType() {
                return pageType;
            }

            @Override
            public void onDestroyView() {
                super.onDestroyView();
                if (root != null) {
                    root.removeAllViews();
                    root = null;
                }
            }

            /**
             * onPressBack
             * @return true if handled, false otherwise
             */
            @SuppressLint("GestureBackNavigation")
            public boolean onPressBack() {
                if (settingsLayout != null && settingsLayout.getPagesManager() != null) {
                    settingsLayout.header.setTitle(t("settings.title"));
                    return settingsLayout.getPagesManager().onPressBack();
                }
                return false;
            }
        }
    }
