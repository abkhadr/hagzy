package hagzy.fragments.settings;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private OnBackListener onBackListener;

    private EditText nameInput, emailInput, phoneInput;
    private LinearLayout saveButton;
    private TextView saveButtonText, errorText;
    private ProgressBar progressBar;
    private FrameLayout progressOverlay;
    private ImageView profileImage;
    private boolean isLoading = false;

    public interface OnBackListener {
        void onBack();
    }

    public void setOnBackListener(OnBackListener listener) {
        this.onBackListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
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

        // Profile Image Section
        container.addView(createProfileImageSection());

        // Form Fields
        container.addView(createLabel("الاسم الكامل"));
        nameInput = createInput(
                currentUser != null && currentUser.getDisplayName() != null ?
                        currentUser.getDisplayName() : "",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        );
        container.addView(nameInput);

        container.addView(createLabel("البريد الإلكتروني"));
        emailInput = createInput(
                currentUser != null && currentUser.getEmail() != null ?
                        currentUser.getEmail() : "",
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );
        emailInput.setEnabled(false); // Email cannot be changed easily
        emailInput.setAlpha(0.6f);
        container.addView(emailInput);

        container.addView(createLabel("رقم الهاتف"));
        phoneInput = createInput(
                currentUser != null && currentUser.getPhoneNumber() != null ?
                        currentUser.getPhoneNumber() : "",
                InputType.TYPE_CLASS_PHONE
        );
        container.addView(phoneInput);

        // Info Note
        TextView infoNote = createText(
                "ملاحظة: لا يمكن تعديل البريد الإلكتروني بعد التسجيل",
                12,
                "#666666",
                false
        );
        infoNote.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        noteParams.topMargin = dp(8);
        noteParams.bottomMargin = dp(24);
        infoNote.setLayoutParams(noteParams);
        container.addView(infoNote);

        // Save Button
        saveButton = createPrimaryButton("حفظ التعديلات", "#000000");
        saveButton.setOnClickListener(v -> handleSaveProfile());
        container.addView(saveButton);

        // Cancel Button
        LinearLayout cancelButton = createSecondaryButton("إلغاء");
        cancelButton.setOnClickListener(v -> {
            if (onBackListener != null) {
                onBackListener.onBack();
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
            if (onBackListener != null) {
                onBackListener.onBack();
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

        TextView title = createText("تعديل الملف الشخصي", 28, "#000000", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(8);
        title.setLayoutParams(titleParams);
        header.addView(title);

        TextView subtitle = createText("قم بتحديث معلوماتك الشخصية", 16, "#666666", false);
        header.addView(subtitle);

        return header;
    }

    private LinearLayout createProfileImageSection() {
        LinearLayout section = new LinearLayout(requireContext());
        section.setOrientation(LinearLayout.VERTICAL);
        section.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sectionParams.bottomMargin = dp(24);
        section.setLayoutParams(sectionParams);

        // Profile Image Container
        FrameLayout imageContainer = new FrameLayout(requireContext());
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(dp(100), dp(100));
        containerParams.bottomMargin = dp(12);
        imageContainer.setLayoutParams(containerParams);

        profileImage = new ImageView(requireContext());
        FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        profileImage.setLayoutParams(imgParams);
        profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        profileImage.setClipToOutline(true);

        GradientDrawable imgBg = new GradientDrawable();
        imgBg.setShape(GradientDrawable.OVAL);
        imgBg.setColor(Color.parseColor("#F5F5F5"));
        profileImage.setBackground(imgBg);

        if (currentUser != null) {
            Uri photoUri = currentUser.getPhotoUrl();
            if (photoUri != null) {
                Glide.with(requireContext()).load(photoUri).into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.arrow_left);
            }
        }

        // Edit Icon Overlay
        ImageView editIcon = new ImageView(requireContext());
        FrameLayout.LayoutParams editParams = new FrameLayout.LayoutParams(dp(32), dp(32));
        editParams.gravity = Gravity.BOTTOM | Gravity.END;
        editIcon.setLayoutParams(editParams);
        editIcon.setImageResource(R.drawable.cog_8);
        editIcon.setPadding(dp(6), dp(6), dp(6), dp(6));

        GradientDrawable editBg = new GradientDrawable();
        editBg.setShape(GradientDrawable.OVAL);
        editBg.setColor(Color.parseColor("#000000"));
        editIcon.setBackground(editBg);
        editIcon.setColorFilter(Color.WHITE);

        imageContainer.addView(profileImage);
        imageContainer.addView(editIcon);

        TextView changePhotoText = createText("تغيير الصورة", 14, "#1976D2", true);
        changePhotoText.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "قريباً...", Toast.LENGTH_SHORT).show();
        });

        section.addView(imageContainer);
        section.addView(changePhotoText);

        return section;
    }

    // ════════════════════════════════════════════════════════════
    // 💾 Save Profile
    // ════════════════════════════════════════════════════════════

    private void handleSaveProfile() {
        if (isLoading || currentUser == null) return;

        String name = nameInput.getText().toString().trim();

        if (name.isEmpty()) {
            showError("يرجى إدخال الاسم");
            nameInput.requestFocus();
            return;
        }

        hideError();
        setLoading(true);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "تم تحديث الملف الشخصي بنجاح", Toast.LENGTH_SHORT).show();
                        if (onBackListener != null) {
                            onBackListener.onBack();
                        }
                    } else {
                        showError("فشل تحديث الملف الشخصي");
                    }
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

    private EditText createInput(String initialValue, int inputType) {
        EditText input = new EditText(requireContext());
        input.setText(initialValue);
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
}