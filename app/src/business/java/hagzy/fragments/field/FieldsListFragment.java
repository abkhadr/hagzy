package hagzy.fragments.field;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import hagzy.SettingsActivity;

public class FieldsListFragment extends Fragment {

    private FirebaseFirestore db;
    private String providerId;
    private OnFieldClickListener onFieldClickListener;
    private OnAddFieldClickListener onAddFieldClickListener;

    private LinearLayout fieldsContainer;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private LinearLayout settingsButton, addFieldButton;

    private List<FieldData> fieldsList = new ArrayList<>();

    public interface OnFieldClickListener {
        void onFieldClick(String fieldId);
    }

    public interface OnAddFieldClickListener {
        void onAddFieldClick();
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setOnFieldClickListener(OnFieldClickListener listener) {
        this.onFieldClickListener = listener;
    }

    public void setOnAddFieldClickListener(OnAddFieldClickListener listener) {
        this.onAddFieldClickListener = listener;
    }

    public void refreshFields() {
        loadFields();
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
        loadFields();
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

        // Add Field Button
        settingsButton = creaeteSettingButton();
        mainContainer.addView(settingsButton);

        // Header
        mainContainer.addView(createHeader());

        // Add Field Button
        addFieldButton = createAddFieldButton();
        mainContainer.addView(addFieldButton);

        // Fields Container
        fieldsContainer = new LinearLayout(requireContext());
        fieldsContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.addView(fieldsContainer);

        // Empty State
        emptyStateText = createText("لا توجد ملاعب مضافة بعد", 16, "#999999", false);
        emptyStateText.setGravity(Gravity.CENTER);
        emptyStateText.setVisibility(View.GONE);
        LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        emptyParams.topMargin = dp(60);
        emptyStateText.setLayoutParams(emptyParams);
        mainContainer.addView(emptyStateText);

        // Progress Bar
        progressBar = new ProgressBar(requireContext());
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                dp(40), dp(40)
        );
        progressParams.gravity = Gravity.CENTER;
        progressParams.topMargin = dp(60);
        progressBar.setLayoutParams(progressParams);
        mainContainer.addView(progressBar);

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

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headerParams.bottomMargin = dp(24);
        header.setLayoutParams(headerParams);

        TextView title = createText("الملاعب", 28, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        header.addView(title);

        TextView subtitle = createText("إدارة ملاعبك المضافة", 16, "#666666", false);
        header.addView(subtitle);

        return header;
    }

    @SuppressLint("ClickableViewAccessibility")
    private LinearLayout creaeteSettingButton() {
        LinearLayout button = new LinearLayout(requireContext());
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#000000"));
        bg.setCornerRadius(dp(12));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        params.bottomMargin = dp(24);
        button.setLayoutParams(params);

        ImageView icon = new ImageView(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMarginEnd(dp(8));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.arrow_left); // تأكد من وجود الأيقونة
        icon.setColorFilter(Color.WHITE);
        button.addView(icon);

        TextView buttonText = createText("الإعدادات", 16, "#FFFFFF", true);
        button.addView(buttonText);

        button.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        });

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

    @SuppressLint("ClickableViewAccessibility")
    private LinearLayout createAddFieldButton() {
        LinearLayout button = new LinearLayout(requireContext());
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#000000"));
        bg.setCornerRadius(dp(12));
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        params.bottomMargin = dp(24);
        button.setLayoutParams(params);

        ImageView icon = new ImageView(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMarginEnd(dp(8));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.arrow_left); // تأكد من وجود الأيقونة
        icon.setColorFilter(Color.WHITE);
        button.addView(icon);

        TextView buttonText = createText("إضافة ملعب جديد", 16, "#FFFFFF", true);
        button.addView(buttonText);

        button.setOnClickListener(v -> {
            if (onAddFieldClickListener != null) {
                onAddFieldClickListener.onAddFieldClick();
            }
        });

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

    // ════════════════════════════════════════════════════════════
    // 📊 Load Data
    // ════════════════════════════════════════════════════════════

    private void loadFields() {
        progressBar.setVisibility(View.VISIBLE);
        fieldsContainer.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);

        db.collection("fields")
                .whereEqualTo("providorId", providerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    fieldsList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FieldData field = new FieldData();
                        field.fieldId = doc.getId();
                        field.nameField = doc.getString("nameField");
                        field.address = doc.contains("location") ?
                                doc.get("location.address", String.class) : "";
                        field.pricePerHour = doc.contains("pricePerHour") ?
                                doc.getLong("pricePerHour").intValue() : 0;
                        field.active = doc.getBoolean("active") != null ?
                                doc.getBoolean("active") : true;
                        field.autoApprove = doc.getBoolean("autoApprove") != null ?
                                doc.getBoolean("autoApprove") : false;

                        fieldsList.add(field);
                    }

                    if (fieldsList.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        fieldsContainer.setVisibility(View.VISIBLE);
                        displayFields();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyStateText.setText("حدث خطأ في تحميل البيانات");
                    emptyStateText.setVisibility(View.VISIBLE);
                });
    }

    private void displayFields() {
        fieldsContainer.removeAllViews();

        for (FieldData field : fieldsList) {
            fieldsContainer.addView(createFieldCard(field));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private View createFieldCard(FieldData field) {
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

        // Header Row
        LinearLayout headerRow = new LinearLayout(requireContext());
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headerParams.bottomMargin = dp(12);
        headerRow.setLayoutParams(headerParams);

        TextView fieldName = createText(field.nameField, 18, "#000000", true);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        fieldName.setLayoutParams(nameParams);
        headerRow.addView(fieldName);

        // Status Badge
        TextView statusBadge = createText(field.active ? "نشط" : "معطل", 12,
                field.active ? "#4CAF50" : "#757575", true);
        statusBadge.setPadding(dp(12), dp(6), dp(12), dp(6));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Color.parseColor(field.active ? "#E8F5E9" : "#F5F5F5"));
        badgeBg.setCornerRadius(dp(12));
        statusBadge.setBackground(badgeBg);
        headerRow.addView(statusBadge);

        card.addView(headerRow);

        // Location
        if (field.address != null && !field.address.isEmpty()) {
            LinearLayout locationRow = new LinearLayout(requireContext());
            locationRow.setOrientation(LinearLayout.HORIZONTAL);
            locationRow.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams locationParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            locationParams.bottomMargin = dp(8);
            locationRow.setLayoutParams(locationParams);

            ImageView locationIcon = new ImageView(requireContext());
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(16), dp(16));
            iconParams.setMarginEnd(dp(8));
            locationIcon.setLayoutParams(iconParams);
            locationIcon.setImageResource(R.drawable.arrow_left);
            locationIcon.setColorFilter(Color.parseColor("#757575"));
            locationRow.addView(locationIcon);

            TextView locationText = createText(field.address, 14, "#666666", false);
            locationRow.addView(locationText);

            card.addView(locationRow);
        }

        // Price
        LinearLayout priceRow = new LinearLayout(requireContext());
        priceRow.setOrientation(LinearLayout.HORIZONTAL);
        priceRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        priceParams.bottomMargin = dp(12);
        priceRow.setLayoutParams(priceParams);

        TextView priceLabel = createText("السعر: ", 14, "#666666", false);
        priceRow.addView(priceLabel);

        TextView priceValue = createText(field.pricePerHour + " جنيه/ساعة", 14, "#000000", true);
        priceRow.addView(priceValue);

        card.addView(priceRow);

        // Divider
        View divider = new View(requireContext());
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)
        );
        dividerParams.topMargin = dp(4);
        dividerParams.bottomMargin = dp(12);
        divider.setLayoutParams(dividerParams);
        card.addView(divider);

        // Action Buttons
        LinearLayout actionsRow = new LinearLayout(requireContext());
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView viewButton = createText("عرض التفاصيل", 14, "#1976D2", true);
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        viewButton.setLayoutParams(viewParams);
        viewButton.setGravity(Gravity.START);
        actionsRow.addView(viewButton);

        ImageView arrowIcon = new ImageView(requireContext());
        arrowIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        arrowIcon.setImageResource(R.drawable.arrow_left);
        arrowIcon.setColorFilter(Color.parseColor("#1976D2"));
        actionsRow.addView(arrowIcon);

        card.addView(actionsRow);

        card.setOnClickListener(v -> {
            if (onFieldClickListener != null) {
                onFieldClickListener.onFieldClick(field.fieldId);
            }
        });

        card.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1f);
            }
            return false;
        });

        return card;
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helpers
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
        int pricePerHour;
        boolean active;
        boolean autoApprove;
    }
}