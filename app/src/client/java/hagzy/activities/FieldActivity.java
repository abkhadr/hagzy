package hagzy.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.UiHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import hagzy.layouts.field.FieldDetailsManager;
import hagzy.layouts.field.utils.FieldDataParser;
import hagzy.layouts.field.utils.FieldDataParser.FieldData;

/**
 * FieldActivity - عرض تفاصيل الملعب
 *
 * الاستخدام:
 * Intent intent = new Intent(context, FieldActivity.class);
 * intent.putExtra("fieldId", fieldId);
 * startActivity(intent);
 */
public class FieldActivity extends AppCompatActivity {

    private static final String TAG = "FieldActivity";

    private FirebaseFirestore db;
    private String fieldId;

    private FieldDetailsManager detailsManager;
    private ProgressBar progressBar;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupInit();
        setupFirebase();
        setupUI();
        loadFieldData();
    }

    // ════════════════════════════════════════════════════════════
    // 🎯 Setup Methods
    // ════════════════════════════════════════════════════════════

    private void setupInit() {
        fieldId = getIntent().getStringExtra("fieldId");

        if (fieldId == null || fieldId.isEmpty()) {
            Toast.makeText(this, "خطأ: لم يتم تحديد الملعب", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupUI() {
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(Color.parseColor("#F5F5F5"));

        // ScrollView with Content
        ScrollView scrollView = new ScrollView(this);
        scrollView.setVerticalScrollBarEnabled(false);

        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);

        // Details Manager
        detailsManager = new FieldDetailsManager(this);
        detailsManager.getView().setVisibility(View.GONE);
        mainContainer.addView(detailsManager.getView());

        // Loading State
        progressBar = createProgressBar();
        mainContainer.addView(progressBar);

        // Error State
        errorText = createErrorText();
        mainContainer.addView(errorText);

        scrollView.addView(mainContainer);
        root.addView(scrollView);

        // Back Button
        root.addView(createBackButton());

        setContentView(root);
    }

    // ════════════════════════════════════════════════════════════
    // 📡 Data Loading
    // ════════════════════════════════════════════════════════════

    private void loadFieldData() {
        showLoading();

        db.collection("fields")
                .document(fieldId)
                .get()
                .addOnSuccessListener(this::onDataLoaded)
                .addOnFailureListener(this::onDataError);
    }

    private void onDataLoaded(DocumentSnapshot document) {
        if (!document.exists()) {
            showError("الملعب غير موجود");
            return;
        }

        try {
            FieldData fieldData = FieldDataParser.parse(document);
            displayFieldData(fieldData);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing field data", e);
            showError("خطأ في تحميل البيانات");
        }
    }

    private void onDataError(Exception e) {
        Log.e(TAG, "Error loading field data", e);
        showError("فشل تحميل البيانات");
    }

    private void displayFieldData(FieldData data) {
        hideLoading();
        detailsManager.displayField(data);
        detailsManager.getView().setVisibility(View.VISIBLE);
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 UI Components
    // ════════════════════════════════════════════════════════════

    private View createBackButton() {
        FrameLayout backButton = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                dp(44), dp(44)
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.topMargin = dp(16);
        params.leftMargin = dp(16);
        backButton.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.WHITE);
        backButton.setBackground(bg);
        backButton.setElevation(dp(4));

        ImageView icon = new ImageView(this);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                dp(24), dp(24)
        );
        iconParams.gravity = Gravity.CENTER;
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.arrow_left);
        icon.setColorFilter(Color.parseColor("#000000"));
        backButton.addView(icon);

        backButton.setOnClickListener(v -> finish());

        return backButton;
    }

    private ProgressBar createProgressBar() {
        ProgressBar progress = new ProgressBar(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp(40), dp(40)
        );
        params.gravity = Gravity.CENTER;
        params.topMargin = dp(100);
        progress.setLayoutParams(params);
        return progress;
    }

    private TextView createErrorText() {
        TextView text = UiHelper.createText(this, "", 15, "#999999", 1);
        text.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(100);
        text.setLayoutParams(params);
        text.setVisibility(View.GONE);
        return text;
    }

    // ════════════════════════════════════════════════════════════
    // 📊 State Management
    // ════════════════════════════════════════════════════════════

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);
        detailsManager.getView().setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
        detailsManager.getView().setVisibility(View.GONE);
    }

    // ════════════════════════════════════════════════════════════
    // 🛠️ Helpers
    // ════════════════════════════════════════════════════════════

    private int dp(int value) {
        return UiHelper.dp(this, value);
    }
}