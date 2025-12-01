package com.bytepulse.hagzy.layouts.auth;

import static com.bytepulse.hagzy.helpers.TranslationManager.isRTL;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.bytepulse.hagzy.layouts.auth.ForgotPasswordFragment;
import com.bytepulse.hagzy.layouts.auth.SignupFragment;
import com.bytepulse.hagzy.layouts.auth.pages.AuthPage;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * مدير التنقل بين صفحات المصادقة
 * يتولى إدارة الأنيميشن والتحولات بين الصفحات
 */
public class AuthPagesManager {

    private final Context context;
    private final FrameLayout container;
    private final Map<String, AuthPage> pages;
    private final Stack<String> navigationStack;
    private String currentPageId;
    private final boolean isRTL = isRTL();

    private static final int ANIMATION_DURATION = 200;

    public AuthPagesManager(Context context, FrameLayout container) {
        this.context = context;
        this.container = container;
        this.pages = new HashMap<>();
        this.navigationStack = new Stack<>();
    }

    public void addPage(String id, AuthPage page) {
        pages.put(id, page);
    }

    public void showPage(String pageId) {
        if (!pages.containsKey(pageId)) return;

        AuthPage page = pages.get(pageId);
        View pageView = page.getView();

        container.removeAllViews();
        container.addView(pageView);

        currentPageId = pageId;
        if (navigationStack.isEmpty() || !navigationStack.peek().equals(pageId)) {
            navigationStack.push(pageId);
        }
    }

    /**
     * التنقل مع أنيميشن ناعم
     */
    public void navigateTo(String pageId) {
        if (!pages.containsKey(pageId) || pageId.equals(currentPageId)) return;

        AuthPage newPage = pages.get(pageId);
        View newView = newPage.getView();

        // إضافة الـ View الجديد
        newView.setTranslationX(container.getWidth());
        container.addView(newView);

        // أنيميشن الانتقال
        animatePageTransition(newView, () -> {
            // إزالة الـ View القديم
            if (container.getChildCount() > 1) {
                container.removeViewAt(0);
            }
            currentPageId = pageId;
            navigationStack.push(pageId);
        });
    }

    /**
     * أنيميشن حركة الصفحة - ناعم وسلس
     */
    private void animatePageTransition(View newView, Runnable onComplete) {
        // الصفحة الحالية تخرج من اليسار
        int containerWidth = container.getWidth();
        int startX, endX;

        if (isRTL) { // RTL
            startX = -containerWidth;
            endX = containerWidth;
        } else {   // LTR
            startX = containerWidth;
            endX = -containerWidth;
        }
        View currentView = container.getChildAt(0);
        if (currentView != null) {
            currentView.animate()
                    .translationX(startX)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // الصفحة الجديدة تدخل من اليمين
        newView.animate()
                .translationX(0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(onComplete)
                .start();
    }

    /**
     * الرجوع للصفحة السابقة
     */
    public boolean handleBackPress() {
        if (navigationStack.size() > 1) {
            navigationStack.pop();
            String previousPageId = navigationStack.peek();

            if (pages.containsKey(previousPageId)) {
                animateBackTransition(previousPageId);
                return true;
            }
        }
        return false;
    }

    /**
     * أنيميشن الرجوع - معكوس
     */
    private void animateBackTransition(String previousPageId) {
        AuthPage previousPage = pages.get(previousPageId);
        View previousView = previousPage.getView();



        int containerWidth = container.getWidth();
        int startX, endX;

        if (isRTL) { // RTL
            startX = -containerWidth;
            endX = containerWidth;
        } else {   // LTR
            startX = containerWidth;
            endX = -containerWidth;
        }

        previousView.setTranslationX(startX);
        container.addView(previousView, 0);
        View currentView = container.getChildAt(1);

        currentView.animate()
                .translationX(endX)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        previousView.animate()
                .translationX(0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    container.removeView(currentView);
                    currentPageId = previousPageId;
                })
                .start();
    }

    /**
     * معالجة نتيجة تسجيل الدخول بـ Google
     */
    public void handleGoogleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        Log.d("GoogleSignIn", "handleSignInResult: " + task.isSuccessful());
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (pages.containsKey(currentPageId)) {
                pages.get(currentPageId).onGoogleSignInResult(account);
            }
        } catch (ApiException e) {
            if (pages.containsKey(currentPageId)) {
                pages.get(currentPageId).onGoogleSignInError();
            }
        }
    }
}