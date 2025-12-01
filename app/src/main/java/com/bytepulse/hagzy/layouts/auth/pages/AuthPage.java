package com.bytepulse.hagzy.layouts.auth.pages;

import android.view.View;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * واجهة أساسية لجميع صفحات المصادقة
 */
public interface AuthPage {

    /**
     * الحصول على View الصفحة
     */
    View getView();

    /**
     * معالجة نتيجة تسجيل الدخول بـ Google
     */
    default void onGoogleSignInResult(GoogleSignInAccount account) {

    }

    /**
     * معالجة فشل تسجيل الدخول بـ Google
     */
    default void onGoogleSignInError() {

    }
}