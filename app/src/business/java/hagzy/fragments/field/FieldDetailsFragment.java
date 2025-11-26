package hagzy.fragments.field;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FieldDetailsFragment extends Fragment {

    private FirebaseFirestore db;
    private String fieldId;
    private OnBackListener onBackListener;
    private OnEditClickListener onEditClickListener;

    private LinearLayout contentContainer;
    private ProgressBar progressBar;
    private TextView errorText;

    private FieldData fieldData;

    private Switch activeSwitch;
    private Switch autoApproveSwitch;

    public interface OnBackListener {
        void onBack();
    }

    public interface OnEditClickListener {
        void onEditClick(String fieldId);
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public void setOnBackListener(OnBackListener listener) {
        this.onBackListener = listener;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        return buildUI();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadFieldDetails();
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 UI Building
    // ════════════════════════════════════════════════════════════

    private View buildUI() {
        FrameLayout root = new FrameLayout(requireContext());
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(Color.WHITE);

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout mainContainer = new LinearLayout(requireContext());
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(dp(24), dp(60), dp(24), dp(40));

        // Back Button
        mainContainer.addView(createBackButton());

        // Content Container
        contentContainer = new LinearLayout(requireContext());
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setVisibility(View.GONE);
        mainContainer.addView(contentContainer);

        // Progress Bar
        progressBar = new ProgressBar(requireContext());
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                dp(40), dp(40)
        );
        progressParams.gravity = Gravity.CENTER;
        progressParams.topMargin = dp(60);
        progressBar.setLayoutParams(progressParams);
        mainContainer.addView(progressBar);

        // Error Text
        errorText = createText("حدث خطأ في تحميل البيانات", 16, "#E53935", false);
        errorText.setGravity(Gravity.CENTER);
        errorText.setVisibility(View.GONE);
        LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        errorParams.topMargin = dp(60);
        errorText.setLayoutParams(errorParams);
        mainContainer.addView(errorText);

        scrollView.addView(mainContainer);
        root.addView(scrollView);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            scrollView.setPadding(0, top, 0, bottom);
            return insets;
        });

        return root;
    }

    private LinearLayout createBackButton() {
        LinearLayout backButton = new LinearLayout(requireContext());
        backButton.setOrientation(LinearLayout.HORIZONTAL);
        backButton.setGravity(Gravity.CENTER_VERTICAL);
        backButton.setPadding(0, 0, 0, dp(24));

        ImageView backIcon = new ImageView(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMarginEnd(dp(8));
        backIcon.setLayoutParams(iconParams);
        backIcon.setImageResource(R.drawable.arrow_left);
        backIcon.setColorFilter(Color.parseColor("#000000"));
        backButton.addView(backIcon);

        TextView backText = createText("رجوع", 16, "#000000", true);
        backButton.addView(backText);

        backButton.setOnClickListener(v -> {
            if (onBackListener != null) {
                onBackListener.onBack();
            }
        });

        return backButton;
    }

    // ════════════════════════════════════════════════════════════
    // 📊 Load Data
    // ════════════════════════════════════════════════════════════

    private void loadFieldDetails() {
        progressBar.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);

        db.collection("fields")
                .document(fieldId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        fieldData = parseFieldData(documentSnapshot);
                        displayFieldDetails();
                        contentContainer.setVisibility(View.VISIBLE);
                    } else {
                        errorText.setText("لم يتم العثور على الملعب");
                        errorText.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                });
    }

    private FieldData parseFieldData(DocumentSnapshot doc) {
        FieldData data = new FieldData();
        data.fieldId = doc.getId();
        data.nameField = doc.getString("nameField");
        data.address = doc.contains("location") ? doc.get("location.address", String.class) : "";
        data.lat = doc.contains("location.lat") ? doc.getDouble("location.lat") : 0.0;
        data.lon = doc.contains("location.lon") ? doc.getDouble("location.lon") : 0.0;
        data.pricePerHour = doc.contains("pricePerHour") ? doc.getLong("pricePerHour").intValue() : 0;
        data.platformCommission = doc.contains("platformCommission") ? doc.getLong("platformCommission").intValue() : 10;
        data.autoApprove = doc.getBoolean("autoApprove") != null ? doc.getBoolean("autoApprove") : false;
        data.active = doc.getBoolean("active") != null ? doc.getBoolean("active") : true;
        return data;
    }

    private void displayFieldDetails() {
        contentContainer.removeAllViews();

        // Header
        contentContainer.addView(createHeader());

        // Status Card
        contentContainer.addView(createStatusCard());

        // Basic Info Card
        contentContainer.addView(createInfoCard());

        // Location Card
        contentContainer.addView(createLocationCard());

        // Pricing Card
        contentContainer.addView(createPricingCard());

        // Settings Card
        contentContainer.addView(createSettingsCard());

        // Action Buttons
        contentContainer.addView(createActionButtons());
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 UI Components
    // ════════════════════════════════════════════════════════════

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headerParams.bottomMargin = dp(24);
        header.setLayoutParams(headerParams);

        TextView title = createText(fieldData.nameField, 28, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        header.addView(title);

        TextView subtitle = createText("معلومات الملعب التفصيلية", 16, "#666666", false);
        header.addView(subtitle);

        return header;
    }

    private LinearLayout createStatusCard() {
        LinearLayout card = createCard();

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView label = createText("حالة الملعب", 16, "#000000", true);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        label.setLayoutParams(labelParams);
        row.addView(label);

        activeSwitch = new Switch(requireContext());
        activeSwitch.setChecked(fieldData.active);
        activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateFieldStatus(isChecked));
        row.addView(activeSwitch);

        card.addView(row);
        return card;
    }

    private LinearLayout createInfoCard() {
        LinearLayout card = createCard();

        TextView cardTitle = createText("المعلومات الأساسية", 18, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        cardTitle.setLayoutParams(titleParams);
        card.addView(cardTitle);

        card.addView(createInfoRow("اسم الملعب", fieldData.nameField));
        card.addView(createInfoRow("رقم الملعب", fieldData.fieldId));

        return card;
    }

    private LinearLayout createLocationCard() {
        LinearLayout card = createCard();

        TextView cardTitle = createText("الموقع", 18, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        cardTitle.setLayoutParams(titleParams);
        card.addView(cardTitle);

        card.addView(createInfoRow("العنوان", fieldData.address));
        card.addView(createInfoRow("الإحداثيات",
                String.format("%.4f, %.4f", fieldData.lat, fieldData.lon)));

        return card;
    }

    private LinearLayout createPricingCard() {
        LinearLayout card = createCard();

        TextView cardTitle = createText("الأسعار", 18, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        cardTitle.setLayoutParams(titleParams);
        card.addView(cardTitle);

        card.addView(createInfoRow("سعر الساعة", fieldData.pricePerHour + " جنيه"));
        card.addView(createInfoRow("عمولة المنصة", fieldData.platformCommission + "%"));

        int netPrice = fieldData.pricePerHour - (fieldData.pricePerHour * fieldData.platformCommission / 100);
        card.addView(createInfoRow("صافي الربح/ساعة", netPrice + " جنيه"));

        return card;
    }

    private LinearLayout createSettingsCard() {
        LinearLayout card = createCard();

        TextView cardTitle = createText("الإعدادات", 18, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        cardTitle.setLayoutParams(titleParams);
        card.addView(cardTitle);

        // Auto Approve Row
        LinearLayout autoApproveRow = new LinearLayout(requireContext());
        autoApproveRow.setOrientation(LinearLayout.HORIZONTAL);
        autoApproveRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(8);
        autoApproveRow.setLayoutParams(rowParams);

        LinearLayout textContainer = new LinearLayout(requireContext());
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        textContainer.setLayoutParams(textParams);

        TextView approveLabel = createText("موافقة تلقائية", 16, "#000000", true);
        textContainer.addView(approveLabel);

        TextView approveDesc = createText("قبول الحجوزات تلقائياً", 12, "#666666", false);
        textContainer.addView(approveDesc);

        autoApproveRow.addView(textContainer);

        autoApproveSwitch = new Switch(requireContext());
        autoApproveSwitch.setChecked(fieldData.autoApprove);
        autoApproveSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAutoApprove(isChecked));
        autoApproveRow.addView(autoApproveSwitch);

        card.addView(autoApproveRow);

        return card;
    }

    private LinearLayout createActionButtons() {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(8);
        container.setLayoutParams(params);

        LinearLayout editButton = createActionButton("تعديل الملعب", "#000000", "#FFFFFF");
        editButton.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(fieldId);
            }
        });
        container.addView(editButton);

        LinearLayout deleteButton = createActionButton("حذف الملعب", "#FFFFFF", "#E53935");
        deleteButton.setOnClickListener(v -> {
            // TODO: Show confirmation dialog
            Toast.makeText(requireContext(), "قريباً...", Toast.LENGTH_SHORT).show();
        });
        container.addView(deleteButton);

        return container;
    }

    // ════════════════════════════════════════════════════════════
    // 🔄 Update Methods
    // ════════════════════════════════════════════════════════════

    private void updateFieldStatus(boolean active) {
        db.collection("fields")
                .document(fieldId)
                .update("active", active)
                .addOnSuccessListener(aVoid -> {
                    fieldData.active = active;
                    Toast.makeText(requireContext(),
                            active ? "تم تفعيل الملعب" : "تم تعطيل الملعب",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    activeSwitch.setChecked(!active);
                    Toast.makeText(requireContext(), "فشل التحديث", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateAutoApprove(boolean autoApprove) {
        db.collection("fields")
                .document(fieldId)
                .update("autoApprove", autoApprove)
                .addOnSuccessListener(aVoid -> {
                    fieldData.autoApprove = autoApprove;
                    Toast.makeText(requireContext(), "تم تحديث الإعدادات", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    autoApproveSwitch.setChecked(!autoApprove);
                    Toast.makeText(requireContext(), "فشل التحديث", Toast.LENGTH_SHORT).show();
                });
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Color.parseColor("#E0E0E0"));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(16);
        card.setLayoutParams(cardParams);

        return card;
    }

    private LinearLayout createInfoRow(String label, String value) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(12);
        row.setLayoutParams(rowParams);

        TextView labelText = createText(label + ":", 14, "#666666", false);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        labelText.setLayoutParams(labelParams);
        row.addView(labelText);

        TextView valueText = createText(value, 14, "#000000", true);
        valueText.setGravity(Gravity.END);
        row.addView(valueText);

        return row;
    }

    private LinearLayout createActionButton(String text, String bgColor, String textColor) {
        LinearLayout button = new LinearLayout(requireContext());
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(dp(12));
        if (bgColor.equals("#FFFFFF")) {
            bg.setStroke(dp(1), Color.parseColor(textColor));
        }
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        params.bottomMargin = dp(12);
        button.setLayoutParams(params);

        TextView buttonText = createText(text, 16, textColor, true);
        button.addView(buttonText);

        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1f);
            }
            return false;
        });

        return button;
    }

    private TextView createText(String text, int size, String color, boolean bold) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        tv.setTextColor(Color.parseColor(color));
        if (bold) {
            tv.setTypeface(ThemeManager.fontBold());
        } else {
            tv.setTypeface(ThemeManager.fontRegular());
        }
        return tv;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    // ════════════════════════════════════════════════════════════
    // 📦 Data Model
    // ════════════════════════════════════════════════════════════

    private static class FieldData {
        String fieldId;
        String nameField;
        String address;
        double lat;
        double lon;
        int pricePerHour;
        int platformCommission;
        boolean autoApprove;
        boolean active;
    }
}