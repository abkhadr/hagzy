package hagzy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bytepulse.hagzy.helpers.DirectionHelper;
import com.bytepulse.hagzy.helpers.LocaleManager;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.TranslationManager;
import hagzy.fragments.settings.EditProfileFragment;
import hagzy.fragments.settings.LanguageFragment;
import hagzy.fragments.settings.MainSettingsFragment;
import hagzy.fragments.settings.NotificationsFragment;
import hagzy.fragments.settings.PrivacyFragment;
import hagzy.fragments.settings.TermsFragment;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FrameLayout fragmentContainer;

    private void setupInit() {
        LocaleManager.applyLocale(this);
        ThemeManager.setDarkMode(this, false);
        ThemeManager.init(this);
        DirectionHelper.applyDirection(this, LocaleManager.getSavedLanguage(this));
        ThemeManager.applySystemBars(this);
        TranslationManager.load(this, LocaleManager.getSavedLanguage(this));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupInit();

        mAuth = FirebaseAuth.getInstance();

        setContentView(buildMainLayout());

        // Load main settings
        showMainSettings();
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 Layout الرئيسي
    // ════════════════════════════════════════════════════════════

    private View buildMainLayout() {
        fragmentContainer = new FrameLayout(this);
        fragmentContainer.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        fragmentContainer.setId(View.generateViewId());
        fragmentContainer.setBackgroundColor(Color.WHITE);

        return fragmentContainer;
    }

    // ════════════════════════════════════════════════════════════
    // 📱 Fragment Navigation
    // ════════════════════════════════════════════════════════════

    private void showMainSettings() {
        MainSettingsFragment fragment = new MainSettingsFragment();
        fragment.setOnNavigationListener(new MainSettingsFragment.OnNavigationListener() {
            @Override
            public void onEditProfile() {
                showEditProfile();
            }

            @Override
            public void onLanguage() {
                showLanguage();
            }

            @Override
            public void onNotifications() {
                showNotifications();
            }

            @Override
            public void onPrivacy() {
                showPrivacy();
            }

            @Override
            public void onTerms() {
                showTerms();
            }

            @Override
            public void onBack() {
                finish();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(fragmentContainer.getId(), fragment)
                .commit();
    }

    private void showEditProfile() {
        EditProfileFragment fragment = new EditProfileFragment();
        fragment.setOnBackListener(() -> onBackPressed());

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .replace(fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showLanguage() {
        LanguageFragment fragment = new LanguageFragment();
        fragment.setOnLanguageChangedListener(new LanguageFragment.OnLanguageChangedListener() {
            @Override
            public void onLanguageChanged(String languageCode) {
                // Restart activity to apply language
                recreate();
            }

            @Override
            public void onBack() {
                onBackPressed();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .replace(fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showNotifications() {
        NotificationsFragment fragment = new NotificationsFragment();
        fragment.setOnBackListener(() -> onBackPressed());

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .replace(fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showPrivacy() {
        PrivacyFragment fragment = new PrivacyFragment();
        fragment.setOnBackListener(() -> onBackPressed());

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .replace(fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showTerms() {
        TermsFragment fragment = new TermsFragment();
        fragment.setOnBackListener(() -> onBackPressed());

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .replace(fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}