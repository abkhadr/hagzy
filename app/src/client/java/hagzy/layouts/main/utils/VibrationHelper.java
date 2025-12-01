package hagzy.layouts.main.utils;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

/**
 * مساعد للاهتزاز
 */
public class VibrationHelper {

    public static final int LIGHT_VIBRATION = 50;
    public static final int MEDIUM_VIBRATION = 100;
    public static final int HEAVY_VIBRATION = 200;

    /**
     * اهتزاز خفيف
     */
    public static void vibrateLight(Context context) {
        vibrate(context, LIGHT_VIBRATION);
    }

    /**
     * اهتزاز متوسط
     */
    public static void vibrateMedium(Context context) {
        vibrate(context, MEDIUM_VIBRATION);
    }

    /**
     * اهتزاز قوي
     */
    public static void vibrateHeavy(Context context) {
        vibrate(context, HEAVY_VIBRATION);
    }

    /**
     * اهتزاز بمدة مخصصة
     */
    public static void vibrate(Context context, long duration) {
        if (context == null) return;

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(
                    VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            );
        }
    }

    /**
     * اهتزاز متعدد (Pattern)
     */
    public static void vibratePattern(Context context, long[] pattern) {
        if (context == null) return;

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, -1)
            );
        }
    }
}