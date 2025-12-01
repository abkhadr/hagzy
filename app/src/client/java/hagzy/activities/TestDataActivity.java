package hagzy.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;
import com.bytepulse.hagzy.utils.FirebaseSeedData;

/**
 * Activity for quickly seeding test data to Firestore
 *
 * Usage: Add this activity to your AndroidManifest.xml temporarily for testing:
 *
 * <activity
 *     android:name="hagzy.activities.TestDataActivity"
 *     android:exported="true">
 *     <intent-filter>
 *         <action android:name="android.intent.action.VIEW" />
 *         <category android:name="android.intent.category.DEFAULT" />
 *     </intent-filter>
 * </activity>
 *
 * Then launch it from your app or add a navigation button to it.
 */
public class TestDataActivity extends AppCompatActivity {

    private FirebaseSeedData seedData;
    private ProgressBar progressBar;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        seedData = new FirebaseSeedData();
        setContentView(buildUI());
    }

    private LinearLayout buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#F5F5F5"));
        root.setPadding(dp(24), dp(24), dp(24), dp(24));

        ScrollView scrollView = new ScrollView(this);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        // Title
        TextView title = createText("🧪 إدارة البيانات التجريبية", 24, "#000000", 3);
        title.setGravity(Gravity.CENTER);
        container.addView(title);

        // Subtitle
        TextView subtitle = createText("استخدم هذه الواجهة لإضافة أو حذف البيانات التجريبية", 14, "#666666", 1);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(8);
        subtitleParams.bottomMargin = dp(32);
        subtitle.setLayoutParams(subtitleParams);
        container.addView(subtitle);

        // Seed All Button
        container.addView(createButton(
                "📊 إضافة جميع البيانات",
                "إضافة: اللاعب الحالي + الملاعب + لاعبين تجريبيين",
                "#4CAF50",
                v -> seedAllData()
        ));

        // Seed Current User Only
        container.addView(createButton(
                "👤 إضافة اللاعب الحالي فقط",
                "إنشاء ملف شخصي كامل للمستخدم الحالي",
                "#2196F3",
                v -> seedCurrentUser()
        ));

        // Seed Fields Only
        container.addView(createButton(
                "🏟️ إضافة الملاعب فقط",
                "إضافة 5 ملاعب تجريبية مع بيانات كاملة",
                "#FF9800",
                v -> seedFields()
        ));

        // Seed Players Only
        container.addView(createButton(
                "👥 إضافة لاعبين تجريبيين",
                "إضافة 10 لاعبين تجريبيين للاختبار",
                "#9C27B0",
                v -> seedPlayers()
        ));

        // Clear All Button
        container.addView(createButton(
                "🗑️ حذف جميع البيانات التجريبية",
                "حذف الملاعب واللاعبين التجريبيين فقط",
                "#F44336",
                v -> clearAllData()
        ));

        // Status Text
        statusText = createText("", 14, "#666666", 1);
        statusText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        statusParams.topMargin = dp(32);
        statusText.setLayoutParams(statusParams);
        container.addView(statusText);

        // Progress Bar
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(android.view.View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                dp(40), dp(40)
        );
        progressParams.gravity = Gravity.CENTER;
        progressParams.topMargin = dp(16);
        progressBar.setLayoutParams(progressParams);
        container.addView(progressBar);

        // Warning
        LinearLayout warningBox = new LinearLayout(this);
        warningBox.setOrientation(LinearLayout.VERTICAL);
        warningBox.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable warningBg = new GradientDrawable();
        warningBg.setColor(Color.parseColor("#FFF3E0"));
        warningBg.setCornerRadius(dp(12));
        warningBg.setStroke(dp(2), Color.parseColor("#FF9800"));
        warningBox.setBackground(warningBg);

        LinearLayout.LayoutParams warningParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        warningParams.topMargin = dp(32);
        warningBox.setLayoutParams(warningParams);

        TextView warningTitle = createText("⚠️ تحذير", 16, "#FF9800", 3);
        warningBox.addView(warningTitle);

        TextView warningText = createText(
                "• هذه الواجهة للاختبار فقط\n" +
                        "• لا تستخدمها في الإنتاج\n" +
                        "• البيانات المضافة ستكون ظاهرة لجميع المستخدمين\n" +
                        "• تأكد من حذف البيانات التجريبية بعد الانتهاء",
                12,
                "#F57C00",
                1
        );
        LinearLayout.LayoutParams warningTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        warningTextParams.topMargin = dp(8);
        warningText.setLayoutParams(warningTextParams);
        warningBox.addView(warningText);

        container.addView(warningBox);

        scrollView.addView(container);
        root.addView(scrollView);

        return root;
    }

    // ════════════════════════════════════════════════════════════
    // 🎯 Action Methods
    // ════════════════════════════════════════════════════════════

    private void seedAllData() {
        showLoading("جاري إضافة جميع البيانات...");

        seedData.seedAllData(new FirebaseSeedData.SeedCallback() {
            @Override
            public void onComplete() {
                runOnUiThread(() -> {
                    hideLoading();
                    showStatus("✅ تم إضافة جميع البيانات بنجاح!");
                    Toast.makeText(TestDataActivity.this,
                            "تمت إضافة البيانات بنجاح",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    showStatus("❌ حدث خطأ: " + error);
                    Toast.makeText(TestDataActivity.this,
                            "خطأ: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void seedCurrentUser() {
        showLoading("جاري إضافة بيانات اللاعب الحالي...");

        seedData.seedCurrentUser(
                () -> runOnUiThread(() -> {
                    hideLoading();
                    showStatus("✅ تم إضافة بيانات اللاعب بنجاح!");
                    Toast.makeText(TestDataActivity.this,
                            "تم إنشاء الملف الشخصي بنجاح",
                            Toast.LENGTH_SHORT).show();
                }),
                error -> runOnUiThread(() -> {
                    hideLoading();
                    showStatus("❌ خطأ: " + error);
                    Toast.makeText(TestDataActivity.this,
                            "خطأ: " + error,
                            Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void seedFields() {
        showLoading("جاري إضافة الملاعب...");

        seedData.seedFields(
                () -> runOnUiThread(() -> {
                    hideLoading();
                    showStatus("✅ تم إضافة 5 ملاعب بنجاح!");
                    Toast.makeText(TestDataActivity.this,
                            "تمت إضافة الملاعب بنجاح",
                            Toast.LENGTH_SHORT).show();
                }),
                error -> runOnUiThread(() -> {
                    hideLoading();
                    showStatus("❌ خطأ: " + error);
                    Toast.makeText(TestDataActivity.this,
                            "خطأ: " + error,
                            Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void seedPlayers() {
        showLoading("جاري إضافة اللاعبين التجريبيين...");

        seedData.seedPlayers(
                () -> runOnUiThread(() -> {
                    hideLoading();
                    showStatus("✅ تم إضافة 10 لاعبين بنجاح!");
                    Toast.makeText(TestDataActivity.this,
                            "تمت إضافة اللاعبين بنجاح",
                            Toast.LENGTH_SHORT).show();
                }),
                error -> runOnUiThread(() -> {
                    hideLoading();
                    showStatus("❌ خطأ: " + error);
                    Toast.makeText(TestDataActivity.this,
                            "خطأ: " + error,
                            Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void clearAllData() {
        showLoading("جاري حذف البيانات التجريبية...");

        seedData.clearAllData(
                () -> runOnUiThread(() -> {
                    hideLoading();
                    showStatus("✅ تم حذف جميع البيانات التجريبية!");
                    Toast.makeText(TestDataActivity.this,
                            "تم الحذف بنجاح",
                            Toast.LENGTH_SHORT).show();
                }),
                error -> runOnUiThread(() -> {
                    hideLoading();
                    showStatus("❌ خطأ: " + error);
                    Toast.makeText(TestDataActivity.this,
                            "خطأ: " + error,
                            Toast.LENGTH_SHORT).show();
                })
        );
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 UI Helpers
    // ════════════════════════════════════════════════════════════

    private LinearLayout createButton(String title, String subtitle, String color, android.view.View.OnClickListener listener) {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.VERTICAL);
        button.setPadding(dp(20), dp(16), dp(20), dp(16));
        button.setOnClickListener(listener);
        button.setClickable(true);
        button.setFocusable(true);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(2), Color.parseColor(color));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(16);
        button.setLayoutParams(params);

        TextView titleText = createText(title, 16, color, 3);
        button.addView(titleText);

        TextView subtitleText = createText(subtitle, 12, "#666666", 1);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(4);
        subtitleText.setLayoutParams(subtitleParams);
        button.addView(subtitleText);

        // Add press effect
        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1.0f);
            }
            return false;
        });

        return button;
    }

    private void showLoading(String message) {
        progressBar.setVisibility(android.view.View.VISIBLE);
        statusText.setText(message);
        statusText.setTextColor(Color.parseColor("#2196F3"));
    }

    private void hideLoading() {
        progressBar.setVisibility(android.view.View.GONE);
    }

    private void showStatus(String message) {
        statusText.setText(message);
        if (message.contains("✅")) {
            statusText.setTextColor(Color.parseColor("#4CAF50"));
        } else if (message.contains("❌")) {
            statusText.setTextColor(Color.parseColor("#F44336"));
        } else {
            statusText.setTextColor(Color.parseColor("#666666"));
        }
    }

    private TextView createText(String text, int size, String color, int weight) {
        return UiHelper.createText(this, text, size, color, weight);
    }

    private int dp(int value) {
        return UiHelper.dp(this, value);
    }
}