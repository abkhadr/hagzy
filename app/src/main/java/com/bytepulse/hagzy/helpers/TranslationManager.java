package com.bytepulse.hagzy.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * TranslationManager - نظام إدارة الترجمة المحسّن
 * يدعم التخزين المؤقت والتحميل الديناميكي للغات
 */
public class TranslationManager {

    private static final String TAG = "TranslationManager";
    private static final String PREF_NAME = "translation_prefs";
    private static final String KEY_LANG = "current_language";
    private static final String DEFAULT_LANG = "en_US";

    private static JSONObject translations;
    private static String currentLang = DEFAULT_LANG;
    private static final Map<String, JSONObject> translationsCache = new HashMap<>();

    /**
     * تهيئة المدير - يتم استدعاؤها مرة واحدة عند بدء التطبيق
     */
    public static void init(Context context) {
        String savedLang = getSavedLang(context);

        if (savedLang == null) {
            savedLang = DEFAULT_LANG;
            saveLang(context, savedLang);
        }

        load(context, savedLang);
        Log.d(TAG, "Initialized with language: " + savedLang);
    }

    /**
     * تحميل ملف اللغة
     */
    public static void load(Context context, String langCode) {
        if (langCode == null || langCode.isEmpty()) {
            langCode = DEFAULT_LANG;
        }

        // التحقق من الكاش أولاً
        if (translationsCache.containsKey(langCode)) {
            translations = translationsCache.get(langCode);
            currentLang = langCode;
            saveLang(context, langCode);
            Log.d(TAG, "Loaded from cache: " + langCode);
            return;
        }

        // تحميل من الملف
        try {
            InputStream is = context.getAssets().open("langs/" + langCode + ".json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            JSONObject loadedTranslations = new JSONObject(new String(buffer, "UTF-8"));

            // حفظ في الكاش
            translationsCache.put(langCode, loadedTranslations);
            translations = loadedTranslations;
            currentLang = langCode;
            saveLang(context, langCode);

            Log.d(TAG, "Successfully loaded language file: " + langCode);

        } catch (Exception e) {
            Log.e(TAG, "Error loading language file: " + langCode, e);
            translations = new JSONObject();

            // محاولة تحميل اللغة الافتراضية
            if (!langCode.equals(DEFAULT_LANG)) {
                Log.d(TAG, "Falling back to default language: " + DEFAULT_LANG);
                load(context, DEFAULT_LANG);
            }
        }
    }

    /**
     * الحصول على الترجمة - يدعم المفاتيح المتداخلة
     * مثال: t("settings.language.title")
     */
    public static String t(String key) {
        if (translations == null || key == null || key.isEmpty()) {
            return key;
        }

        try {
            String[] keys = key.split("\\.");
            JSONObject current = translations;

            for (int i = 0; i < keys.length - 1; i++) {
                if (current.has(keys[i])) {
                    Object value = current.get(keys[i]);
                    if (value instanceof JSONObject) {
                        current = (JSONObject) value;
                    } else {
                        return key; // المسار غير صحيح
                    }
                } else {
                    return key; // المفتاح غير موجود
                }
            }

            // الحصول على القيمة النهائية
            String lastKey = keys[keys.length - 1];
            if (current.has(lastKey)) {
                return current.getString(lastKey);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting translation for key: " + key, e);
        }

        return key;
    }

    /**
     * الترجمة مع متغيرات - يدعم String.format
     * مثال: tf("settings.welcome", userName)
     */
    public static String tf(String key, Object... args) {
        String translation = t(key);
        try {
            return String.format(translation, args);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting translation: " + key, e);
            return translation;
        }
    }

    /**
     * الحصول على اللغة الحالية
     */
    public static String getCurrentLang() {
        return currentLang;
    }

    public static boolean isRTL() {
        if (currentLang == null) {
            return false;
        }
        // You can add other RTL language codes here if you support them
        return currentLang.startsWith("ar");
    }

    /**
     * التحقق من وجود ترجمة لمفتاح معين
     */
    public static boolean hasTranslation(String key) {
        if (translations == null || key == null) {
            return false;
        }

        try {
            String[] keys = key.split("\\.");
            JSONObject current = translations;

            for (String k : keys) {
                if (!current.has(k)) {
                    return false;
                }
                Object value = current.get(k);
                if (value instanceof JSONObject) {
                    current = (JSONObject) value;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * مسح الكاش - مفيد للتطوير
     */
    public static void clearCache() {
        translationsCache.clear();
        Log.d(TAG, "Translation cache cleared");
    }

    /**
     * الحصول على اللغات المتاحة
     */
    public static String[] getAvailableLanguages() {
        return new String[]{"ar_AR", "en_US"};
    }

    // ========== SharedPreferences Operations ==========

    private static void saveLang(Context context, String lang) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_LANG, lang).apply();
            Log.d(TAG, "Language saved: " + lang);
        } catch (Exception e) {
            Log.e(TAG, "Error saving language preference", e);
        }
    }

    public static String getSavedLang(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getString(KEY_LANG, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting saved language", e);
            return null;
        }
    }
}