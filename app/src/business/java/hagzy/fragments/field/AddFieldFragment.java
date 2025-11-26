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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddFieldFragment extends Fragment {

    private FirebaseFirestore db;
    private String providerId;
    private OnFieldAddedListener onFieldAddedListener;

    private EditText fieldNameInput, addressInput, latInput, lonInput, priceInput, commissionInput;
    private Switch activeSwitch, autoApproveSwitch;
    private LinearLayout saveButton;
    private TextView saveButtonText, errorText;
    private ProgressBar progressBar;
    private FrameLayout progressOverlay;
    private boolean isLoading = false;

    public interface OnFieldAddedListener {
        void onFieldAdded();
        void onCancel();
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setOnFieldAddedListener(OnFieldAddedListener listener) {
        this.onFieldAddedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        return buildUI();
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

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(60), dp(24), dp(40));

        // Back Button
        container.addView(createBackButton());

        // Header
        container.addView(createHeader());

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
        container.addView(errorText);

        // Form Section - Basic Info
        container.addView(createSectionTitle("المعلومات الأساسية"));
        container.addView(createLabel("اسم الملعب"));
        fieldNameInput = createInput("مثال: ملعب الأهلي", InputType.TYPE_CLASS_TEXT);
        container.addView(fieldNameInput);

        // Form Section - Location
        container.addView(createSectionTitle("الموقع"));
        container.addView(createLabel("العنوان"));
        addressInput = createInput("مثال: المعادي، القاهرة", InputType.TYPE_CLASS_TEXT);
        container.addView(addressInput);

        LinearLayout coordsRow = new LinearLayout(requireContext());
        coordsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        coordsRow.setLayoutParams(rowParams);

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
        container.addView(coordsRow);

        // Form Section - Pricing
        container.addView(createSectionTitle("الأسعار"));
        container.addView(createLabel("سعر الساعة (جنيه)"));
        priceInput = createInput("150", InputType.TYPE_CLASS_NUMBER);
        container.addView(priceInput);

        container.addView(createLabel("عمولة المنصة (%)"));
        commissionInput = createInput("10", InputType.TYPE_CLASS_NUMBER);
        container.addView(commissionInput);

        // Form Section - Settings
        container.addView(createSectionTitle("الإعدادات"));

        LinearLayout activeCard = createSwitchCard(
                "تفعيل الملعب",
                "يظهر الملعب للعملاء",
                true
        );
        activeSwitch = (Switch) ((LinearLayout) activeCard.getChildAt(0)).getChildAt(1);
        container.addView(activeCard);

        LinearLayout autoApproveCard = createSwitchCard(
                "موافقة تلقائية",
                "قبول الحجوزات تلقائياً بدون مراجعة",
                false
        );
        autoApproveSwitch = (Switch) ((LinearLayout) autoApproveCard.getChildAt(0)).getChildAt(1);
        container.addView(autoApproveCard);

        // Action Buttons
        saveButton = createPrimaryButton("حفظ الملعب", "#000000");
        saveButton.setOnClickListener(v -> handleSaveField());
        container.addView(saveButton);

        LinearLayout cancelButton = createSecondaryButton("إلغاء");
        cancelButton.setOnClickListener(v -> {
            if (onFieldAddedListener != null) {
                onFieldAddedListener.onCancel();
            }
        });
        container.addView(cancelButton);

        scrollView.addView(container);
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
            if (onFieldAddedListener != null) {
                onFieldAddedListener.onCancel();
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

        TextView title = createText("إضافة ملعب جديد", 28, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        header.addView(title);

        TextView subtitle = createText("املأ البيانات التالية لإضافة ملعب جديد", 16, "#666666", false);
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
    // 💾 Save Field
    // ════════════════════════════════════════════════════════════

    private void handleSaveField() {
        if (isLoading) return;

        // Validation
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
            commission = commissionStr.isEmpty() ? 10 : Integer.parseInt(commissionStr);
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

        // Create field data
        Map<String, Object> fieldData = new HashMap<>();
        fieldData.put("fieldsId", ""); // Will be set after creation
        fieldData.put("providorId", providerId);
        fieldData.put("nameField", fieldName);

        Map<String, Object> location = new HashMap<>();
        location.put("lat", lat);
        location.put("lon", lon);
        location.put("address", address);
        fieldData.put("location", location);

        fieldData.put("pricePerHour", price);
        fieldData.put("platformCommission", commission);
        fieldData.put("autoApprove", autoApproveSwitch.isChecked());
        fieldData.put("active", activeSwitch.isChecked());
        fieldData.put("CreatedAt", Timestamp.now());

        // Save to Firestore
        db.collection("fields")
                .add(fieldData)
                .addOnSuccessListener(documentReference -> {
                    // Update fieldsId with document ID
                    documentReference.update("fieldsId", documentReference.getId())
                            .addOnSuccessListener(aVoid -> {
                                setLoading(false);
                                Toast.makeText(requireContext(), "تم إضافة الملعب بنجاح", Toast.LENGTH_SHORT).show();
                                if (onFieldAddedListener != null) {
                                    onFieldAddedListener.onFieldAdded();
                                }
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                showError("تم الإضافة ولكن حدث خطأ في التحديث");
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("فشل في إضافة الملعب: " + e.getMessage());
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
            saveButtonText.setText("حفظ الملعب");
            progressOverlay.setVisibility(View.GONE);
            saveButton.setEnabled(true);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}