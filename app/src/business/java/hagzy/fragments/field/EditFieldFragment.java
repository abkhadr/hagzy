package hagzy.fragments.field;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

public class EditFieldFragment extends Fragment {

    private FirebaseFirestore db;
    private String fieldId;
    private OnFieldUpdatedListener onFieldUpdatedListener;

    private EditText fieldNameInput, addressInput, latInput, lonInput, priceInput, commissionInput;
    private Switch activeSwitch, autoApproveSwitch;
    private LinearLayout saveButton;
    private TextView saveButtonText, errorText;
    private ProgressBar progressBar, loadingProgress;
    private FrameLayout progressOverlay;
    private LinearLayout formContainer;
    private boolean isLoading = false;

    private FieldData originalData;

    public interface OnFieldUpdatedListener {
        void onFieldUpdated();
        void onCancel();
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public void setOnFieldUpdatedListener(OnFieldUpdatedListener listener) {
        this.onFieldUpdatedListener = listener;
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
        loadFieldData();
    }

    // ════════════════════════════════════════════════════════════
    // 📊 Load Data
    // ════════════════════════════════════════════════════════════

    private void loadFieldData() {
        loadingProgress.setVisibility(View.VISIBLE);
        formContainer.setVisibility(View.GONE);

        db.collection("fields")
                .document(fieldId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        originalData = parseFieldData(documentSnapshot);
                        populateForm();
                        formContainer.setVisibility(View.VISIBLE);
                    } else {
                        showError("لم يتم العثور على الملعب");
                    }
                })
                .addOnFailureListener(e -> {
                    loadingProgress.setVisibility(View.GONE);
                    showError("فشل تحميل البيانات");
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

    private void populateForm() {
        fieldNameInput.setText(originalData.nameField);
        addressInput.setText(originalData.address);
        latInput.setText(String.valueOf(originalData.lat));
        lonInput.setText(String.valueOf(originalData.lon));
        priceInput.setText(String.valueOf(originalData.pricePerHour));
        commissionInput.setText(String.valueOf(originalData.platformCommission));
        activeSwitch.setChecked(originalData.active);
        autoApproveSwitch.setChecked(originalData.autoApprove);
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
        scrollView.setFillViewport(true);

        LinearLayout mainContainer = new LinearLayout(requireContext());
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(dp(24), dp(60), dp(24), dp(40));

        // Back Button
        mainContainer.addView(createBackButton());

        // Header
        mainContainer.addView(createHeader());

        // Loading Progress
        loadingProgress = new ProgressBar(requireContext());
        LinearLayout.LayoutParams loadingParams = new LinearLayout.LayoutParams(
                dp(40), dp(40)
        );
        loadingParams.gravity = Gravity.CENTER;
        loadingParams.topMargin = dp(60);
        loadingProgress.setLayoutParams(loadingParams);
        mainContainer.addView(loadingProgress);

        // Form Container
        formContainer = new LinearLayout(requireContext());
        formContainer.setOrientation(LinearLayout.VERTICAL);
        formContainer.setVisibility(View.GONE);

        // Error Message
        errorText = createText("", 14, "#E53935", false);
        errorText.setGravity(Gravity.CENTER);
        errorText.setVisibility(View.GONE);
        LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        errorParams.bottomMargin = dp(16);
        errorText.setLayoutParams(errorParams);
        formContainer.addView(errorText);

        // Form Fields
        formContainer.addView(createSectionTitle("المعلومات الأساسية"));
        formContainer.addView(createLabel("اسم الملعب"));
        fieldNameInput = createInput("مثال: ملعب الأهلي", InputType.TYPE_CLASS_TEXT);
        formContainer.addView(fieldNameInput);

        formContainer.addView(createSectionTitle("الموقع"));
        formContainer.addView(createLabel("العنوان"));
        addressInput = createInput("مثال: المعادي، القاهرة", InputType.TYPE_CLASS_TEXT);
        formContainer.addView(addressInput);

        LinearLayout coordsRow = new LinearLayout(requireContext());
        coordsRow.setOrientation(LinearLayout.HORIZONTAL);
        coordsRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout latContainer = new LinearLayout(requireContext());
        latContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams latParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        latParams.setMarginEnd(dp(8));
        latContainer.setLayoutParams(latParams);
        latContainer.addView(createLabel("خط العرض"));
        latInput = createInput("30.0444", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        latContainer.addView(latInput);

        LinearLayout lonContainer = new LinearLayout(requireContext());
        lonContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lonParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        lonParams.setMarginStart(dp(8));
        lonContainer.setLayoutParams(lonParams);
        lonContainer.addView(createLabel("خط الطول"));
        lonInput = createInput("31.2357", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        lonContainer.addView(lonInput);

        coordsRow.addView(latContainer);
        coordsRow.addView(lonContainer);
        formContainer.addView(coordsRow);

        formContainer.addView(createSectionTitle("الأسعار"));
        formContainer.addView(createLabel("سعر الساعة (جنيه)"));
        priceInput = createInput("150", InputType.TYPE_CLASS_NUMBER);
        formContainer.addView(priceInput);

        formContainer.addView(createLabel("عمولة المنصة (%)"));
        commissionInput = createInput("10", InputType.TYPE_CLASS_NUMBER);
        formContainer.addView(commissionInput);

        formContainer.addView(createSectionTitle("الإعدادات"));

        LinearLayout activeCard = createSwitchCard("تفعيل الملعب", "يظهر الملعب للعملاء", true);
        activeSwitch = (Switch) ((LinearLayout) activeCard.getChildAt(0)).getChildAt(1);
        formContainer.addView(activeCard);

        LinearLayout autoApproveCard = createSwitchCard("موافقة تلقائية", "قبول الحجوزات تلقائياً بدون مراجعة", false);
        autoApproveSwitch = (Switch) ((LinearLayout) autoApproveCard.getChildAt(0)).getChildAt(1);
        formContainer.addView(autoApproveCard);

        saveButton = createPrimaryButton("حفظ التعديلات", "#000000");
        saveButton.setOnClickListener(v -> handleUpdateField());
        formContainer.addView(saveButton);

        LinearLayout cancelButton = createSecondaryButton("إلغاء");
        cancelButton.setOnClickListener(v -> {
            if (onFieldUpdatedListener != null) {
                onFieldUpdatedListener.onCancel();
            }
        });
        formContainer.addView(cancelButton);

        mainContainer.addView(formContainer);

        scrollView.addView(mainContainer);
        root.addView(scrollView);

        // Progress Overlay
        progressOverlay = new FrameLayout(requireContext());
        progressOverlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        progressOverlay.setBackgroundColor(Color.parseColor("#80000000"));
        progressOverlay.setVisibility(View.GONE);
        progressOverlay.setClickable(true);

        progressBar = new ProgressBar(requireContext());
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                dp(50), dp(50), Gravity.CENTER
        );
        progressBar.setLayoutParams(progressParams);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressOverlay.addView(progressBar);

        root.addView(progressOverlay);

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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(24);
        backButton.setLayoutParams(params);

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
            if (onFieldUpdatedListener != null) {
                onFieldUpdatedListener.onCancel();
            }
        });

        return backButton;
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headerParams.bottomMargin = dp(32);
        header.setLayoutParams(headerParams);

        TextView title = createText("تعديل الملعب", 28, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        header.addView(title);

        TextView subtitle = createText("تحديث معلومات الملعب", 16, "#666666", false);
        header.addView(subtitle);

        return header;
    }

    private TextView createSectionTitle(String text) {
        TextView title = createText(text, 20, "#000000", true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(24);
        params.bottomMargin = dp(16);
        title.setLayoutParams(params);
        return title;
    }

    private LinearLayout createSwitchCard(String title, String description, boolean defaultValue) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(12));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textContainer = new LinearLayout(requireContext());
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        textContainer.setLayoutParams(textParams);

        TextView titleText = createText(title, 16, "#000000", true);
        textContainer.addView(titleText);

        TextView descText = createText(description, 12, "#666666", false);
        textContainer.addView(descText);

        row.addView(textContainer);

        Switch switchView = new Switch(requireContext());
        switchView.setChecked(defaultValue);
        row.addView(switchView);

        card.addView(row);

        return card;
    }

    // ════════════════════════════════════════════════════════════
    // 💾 Update Field
    // ════════════════════════════════════════════════════════════

    private void handleUpdateField() {
        if (isLoading) return;

        String fieldName = fieldNameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String latStr = latInput.getText().toString().trim();
        String lonStr = lonInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String commissionStr = commissionInput.getText().toString().trim();

        if (fieldName.isEmpty()) {
            showError("يرجى إدخال اسم الملعب");
            fieldNameInput.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            showError("يرجى إدخال العنوان");
            addressInput.requestFocus();
            return;
        }

        if (latStr.isEmpty() || lonStr.isEmpty()) {
            showError("يرجى إدخال الإحداثيات");
            return;
        }

        if (priceStr.isEmpty()) {
            showError("يرجى إدخال سعر الساعة");
            priceInput.requestFocus();
            return;
        }

        double lat, lon;
        int price, commission;

        try {
            lat = Double.parseDouble(latStr);
            lon = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            showError("الإحداثيات غير صحيحة");
            return;
        }

        try {
            price = Integer.parseInt(priceStr);
            if (price <= 0) {
                showError("السعر يجب أن يكون أكبر من صفر");
                return;
            }
        } catch (NumberFormatException e) {
            showError("السعر غير صحيح");
            return;
        }

        try {
            commission = Integer.parseInt(commissionStr);
            if (commission < 0 || commission > 100) {
                showError("العمولة يجب أن تكون بين 0 و 100");
                return;
            }
        } catch (NumberFormatException e) {
            showError("العمولة غير صحيحة");
            return;
        }

        hideError();
        setLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("nameField", fieldName);

        Map<String, Object> location = new HashMap<>();
        location.put("lat", lat);
        location.put("lon", lon);
        location.put("address", address);
        updates.put("location", location);

        updates.put("pricePerHour", price);
        updates.put("platformCommission", commission);
        updates.put("autoApprove", autoApproveSwitch.isChecked());
        updates.put("active", activeSwitch.isChecked());

        db.collection("fields")
                .document(fieldId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(requireContext(), "تم تحديث الملعب بنجاح", Toast.LENGTH_SHORT).show();
                    if (onFieldUpdatedListener != null) {
                        onFieldUpdatedListener.onFieldUpdated();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("فشل في تحديث الملعب: " + e.getMessage());
                });
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

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

    private TextView createLabel(String text) {
        TextView label = createText(text, 14, "#000000", true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(8);
        label.setLayoutParams(params);
        return label;
    }

    private EditText createInput(String hint, int inputType) {
        EditText input = new EditText(requireContext());
        input.setHint(hint);
        input.setInputType(inputType);
        input.setTextSize(16);
        input.setTextColor(Color.parseColor("#000000"));
        input.setHintTextColor(Color.parseColor("#999999"));
        input.setPadding(dp(16), dp(16), dp(16), dp(16));
        input.setTypeface(ThemeManager.fontRegular());

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(12));
        input.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        params.bottomMargin = dp(16);
        input.setLayoutParams(params);

        return input;
    }

    private LinearLayout createPrimaryButton(String text, String bgColor) {
        LinearLayout button = new LinearLayout(requireContext());
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(dp(12));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        params.topMargin = dp(8);
        params.bottomMargin = dp(12);
        button.setLayoutParams(params);

        saveButtonText = createText(text, 16, "#FFFFFF", true);
        button.addView(saveButtonText);

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

    private LinearLayout createSecondaryButton(String text) {
        LinearLayout button = new LinearLayout(requireContext());
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(1), Color.parseColor("#E0E0E0"));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        params.bottomMargin = dp(16);
        button.setLayoutParams(params);

        TextView buttonText = createText(text, 16, "#000000", true);
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

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);

        ObjectAnimator shake = ObjectAnimator.ofFloat(errorText, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(500);
        shake.start();
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        if (loading) {
            saveButtonText.setText("");
            progressOverlay.setVisibility(View.VISIBLE);
            saveButton.setEnabled(false);
        } else {
            saveButtonText.setText("حفظ التعديلات");
            progressOverlay.setVisibility(View.GONE);
            saveButton.setEnabled(true);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

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