package com.bytepulse.hagzy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bytepulse.hagzy.helpers.DirectionHelper;
import com.bytepulse.hagzy.helpers.LocaleManager;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.TranslationManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import hagzy.MainActivity;

public class RootActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private void setupInit() {
        LocaleManager.setLocale(this, "ar_AR");
        LocaleManager.applyLocale(this);
        ThemeManager.setDarkMode(this, false);
        ThemeManager.init(this);
        DirectionHelper.applyDirection(this, LocaleManager.getSavedLanguage(this));
        ThemeManager.applySystemBars(this);
        TranslationManager.load(this, LocaleManager.getSavedLanguage(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupInit();
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            if (BuildConfig.APP_MODE.equals("CLIENT")) {
                startActivity(new Intent(RootActivity.this, MainActivity.class));
            }else{
                startActivity(new Intent(RootActivity.this, MainActivity.class));
            }
        } else {
            startActivity(new Intent(RootActivity.this, AuthActivity.class));
        }

        finish();
    }
}
