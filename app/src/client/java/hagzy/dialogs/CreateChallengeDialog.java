    package hagzy.dialogs;

    import android.app.Dialog;
    import android.graphics.Color;
    import android.graphics.drawable.GradientDrawable;
    import android.os.Bundle;
    import android.util.Log;
    import android.util.TypedValue;
    import android.view.Gravity;
    import android.view.View;
    import android.view.ViewGroup;
    import android.view.Window;
    import android.widget.ArrayAdapter;
    import android.widget.CheckBox;
    import android.widget.FrameLayout;
    import android.widget.LinearLayout;
    import android.widget.ProgressBar;
    import android.widget.Spinner;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.fragment.app.DialogFragment;

    import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.List;
    import java.util.Locale;
    import java.util.Map;

    public class CreateChallengeDialog extends DialogFragment {

        private static final String TAG = "CreateChallengeDialog";

        private FirebaseFirestore db;
        private FirebaseAuth mAuth;

        private Spinner fieldSpinner;
        private Spinner typeSpinner;
        private Spinner dateSpinner;
        private Spinner timeSpinner;
        private CheckBox openModeCheckbox;
        private ProgressBar progressBar;
        private TextView createButton;

        private OnCreateListener listener;
        private List<FieldData> fieldsList = new ArrayList<>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new Dialog(requireContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(buildUI());

            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }

            loadFields();

            return dialog;
        }

        // ════════════════════════════════════════════════════════════
        // 🎨 UI Building
        // ════════════════════════════════════════════════════════════

        private View buildUI() {
            FrameLayout root = new FrameLayout(requireContext());
            root.setPadding(dp(24), dp(24), dp(24), dp(24));

            LinearLayout container = new LinearLayout(requireContext());
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(dp(24), dp(24), dp(24), dp(24));

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.WHITE);
            bg.setCornerRadius(dp(20));
            container.setBackground(bg);

            // Title
            TextView title = createText("⚔️ إنشاء تحدي جديد", 22, "#000000", 3);
            title.setGravity(Gravity.CENTER);
            container.addView(title);

            TextView subtitle = createText("اختر الملعب والتفاصيل للبدء", 14, "#666666", 1);
            subtitle.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            subParams.topMargin = dp(4);
            subParams.bottomMargin = dp(20);
            subtitle.setLayoutParams(subParams);
            container.addView(subtitle);

            // Field Spinner
            container.addView(createLabel("🏟️ الملعب"));
            fieldSpinner = createSpinner(new String[]{"جاري التحميل..."});
            container.addView(fieldSpinner);

            // Type Spinner
            container.addView(createLabel("⚽ نوع المباراة"));
            typeSpinner = createSpinner(new String[]{"2v2", "4v4", "5v5", "11v11"});
            container.addView(typeSpinner);

            // Date Spinner
            container.addView(createLabel("📅 التاريخ"));
            dateSpinner = createSpinner(getNextDays(7));
            container.addView(dateSpinner);

            // Time Spinner
            container.addView(createLabel("🕐 الوقت"));
            timeSpinner = createSpinner(getTimeSlots());
            container.addView(timeSpinner);

            // Open Mode Checkbox
            LinearLayout checkboxRow = new LinearLayout(requireContext());
            checkboxRow.setOrientation(LinearLayout.HORIZONTAL);
            checkboxRow.setGravity(Gravity.CENTER_VERTICAL);
            checkboxRow.setPadding(dp(4), dp(16), dp(4), dp(4));

            openModeCheckbox = new CheckBox(requireContext());
            openModeCheckbox.setChecked(true);
            checkboxRow.addView(openModeCheckbox);

            TextView checkboxLabel = createText("تحدي مفتوح (يمكن لأي شخص الانضمام)", 14, "#666666", 1);
            LinearLayout.LayoutParams checkLabelParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            checkLabelParams.leftMargin = dp(8);
            checkboxLabel.setLayoutParams(checkLabelParams);
            checkboxRow.addView(checkboxLabel);

            container.addView(checkboxRow);

            // Progress Bar
            progressBar = new ProgressBar(requireContext());
            progressBar.setVisibility(View.GONE);
            LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                    dp(40), dp(40)
            );
            progressParams.gravity = Gravity.CENTER;
            progressParams.topMargin = dp(16);
            progressBar.setLayoutParams(progressParams);
            container.addView(progressBar);

            // Buttons
            LinearLayout buttonsRow = new LinearLayout(requireContext());
            buttonsRow.setOrientation(LinearLayout.HORIZONTAL);
            buttonsRow.setWeightSum(2);
            LinearLayout.LayoutParams buttonsParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            buttonsParams.topMargin = dp(24);
            buttonsRow.setLayoutParams(buttonsParams);

            // Cancel Button
            TextView cancelButton = createButton("إلغاء", "#E0E0E0", "#000000");
            LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
            );
            cancelParams.setMargins(0, 0, dp(8), 0);
            cancelButton.setLayoutParams(cancelParams);
            cancelButton.setOnClickListener(v -> dismiss());
            buttonsRow.addView(cancelButton);

            // Create Button
            createButton = createButton("إنشاء التحدي", "#667eea", "#FFFFFF");
            LinearLayout.LayoutParams createParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
            );
            createParams.setMargins(dp(8), 0, 0, 0);
            createButton.setLayoutParams(createParams);
            createButton.setOnClickListener(v -> createChallenge());
            buttonsRow.addView(createButton);

            container.addView(buttonsRow);

            root.addView(container);

            return root;
        }

        private TextView createLabel(String text) {
            TextView label = createText(text, 14, "#666666", 3);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = dp(16);
            label.setLayoutParams(params);
            return label;
        }

        private Spinner createSpinner(String[] items) {
            Spinner spinner = new Spinner(requireContext());

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    items
            ) {
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    view.setTypeface(ThemeManager.fontRegular());
                    view.setPadding(dp(16), dp(12), dp(16), dp(12));
                    return view;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setTypeface(ThemeManager.fontRegular());
                    view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    view.setPadding(dp(16), dp(12), dp(16), dp(12));
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = dp(8);
            spinner.setLayoutParams(params);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.parseColor("#F5F5F5"));
            bg.setCornerRadius(dp(12));
            bg.setStroke(dp(1), Color.parseColor("#E0E0E0"));
            spinner.setBackground(bg);

            return spinner;
        }

        private TextView createButton(String text, String bgColor, String textColor) {
            TextView button = createText(text, 16, textColor, 3);
            button.setGravity(Gravity.CENTER);
            button.setPadding(dp(16), dp(14), dp(16), dp(14));

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.parseColor(bgColor));
            bg.setCornerRadius(dp(12));
            button.setBackground(bg);

            button.setClickable(true);
            button.setFocusable(true);

            return button;
        }

        // ════════════════════════════════════════════════════════════
        // 📊 Load Fields
        // ════════════════════════════════════════════════════════════

        private void loadFields() {
            db.collection("fields")
                    .whereEqualTo("isActive", true)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        fieldsList.clear();

                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            FieldData field = new FieldData();
                            field.id = doc.getId();
                            field.name = doc.getString("name");
                            fieldsList.add(field);
                        }

                        updateFieldSpinner();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading fields", e);
                        Toast.makeText(requireContext(),
                                "فشل تحميل الملاعب",
                                Toast.LENGTH_SHORT).show();
                    });
        }

        private void updateFieldSpinner() {
            if (fieldsList.isEmpty()) {
                String[] items = {"لا توجد ملاعب متاحة"};
                updateSpinnerAdapter(fieldSpinner, items);
                return;
            }

            String[] items = new String[fieldsList.size()];
            for (int i = 0; i < fieldsList.size(); i++) {
                items[i] = fieldsList.get(i).name;
            }

            updateSpinnerAdapter(fieldSpinner, items);
        }

        private void updateSpinnerAdapter(Spinner spinner, String[] items) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    items
            ) {
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    view.setTypeface(ThemeManager.fontRegular());
                    view.setPadding(dp(16), dp(12), dp(16), dp(12));
                    return view;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setTypeface(ThemeManager.fontRegular());
                    view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    view.setPadding(dp(16), dp(12), dp(16), dp(12));
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

        // ════════════════════════════════════════════════════════════
        // 🎮 Create Challenge
        // ════════════════════════════════════════════════════════════

        private void createChallenge() {
            if (fieldsList.isEmpty()) {
                Toast.makeText(requireContext(),
                        "الرجاء اختيار ملعب",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser() != null ?
                    mAuth.getCurrentUser().getUid() : null;

            if (userId == null) {
                Toast.makeText(requireContext(),
                        "يجب تسجيل الدخول أولاً",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading
            createButton.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            // Get selected values
            int fieldIndex = fieldSpinner.getSelectedItemPosition();
            if (fieldIndex < 0 || fieldIndex >= fieldsList.size()) {
                hideLoading();
                Toast.makeText(requireContext(),
                        "الرجاء اختيار ملعب صحيح",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            FieldData selectedField = fieldsList.get(fieldIndex);
            String type = typeSpinner.getSelectedItem().toString();
            boolean openMode = openModeCheckbox.isChecked();
            long dateTime = getSelectedDateTime();

            // Verify player profile exists
            db.collection("players")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            createPlayerProfile(userId, () -> {
                                notifyCreate(selectedField.id, selectedField.name, type, openMode, dateTime);
                            });
                        } else {
                            Map<String, Object> profile = (Map<String, Object>) documentSnapshot.get("profile");

                            if (profile == null || !isProfileComplete(profile)) {
                                updatePlayerProfile(userId, documentSnapshot, () -> {
                                    notifyCreate(selectedField.id, selectedField.name, type, openMode, dateTime);
                                });
                            } else {
                                notifyCreate(selectedField.id, selectedField.name, type, openMode, dateTime);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking player profile", e);
                        hideLoading();
                        Toast.makeText(requireContext(),
                                "حدث خطأ في التحقق من الملف الشخصي",
                                Toast.LENGTH_SHORT).show();
                    });
        }

        private void notifyCreate(String fieldId, String fieldName, String type, boolean openMode, long dateTime) {
            hideLoading();

            if (listener != null) {
                listener.onCreate(fieldId, fieldName, type, openMode, dateTime);
            }

            dismiss();
        }

        private boolean isProfileComplete(Map<String, Object> profile) {
            return profile.containsKey("level") &&
                    profile.containsKey("rating") &&
                    profile.containsKey("preferredPosition");
        }

        private void createPlayerProfile(String userId, Runnable onSuccess) {
            Log.d(TAG, "Creating player profile for user: " + userId);

            Map<String, Object> profile = new java.util.HashMap<>();
            profile.put("level", 1);
            profile.put("xp", 0);
            profile.put("xpToNextLevel", 100);
            profile.put("title", "مبتدئ");
            profile.put("titleIcon", "🌟");
            profile.put("rating", 0.0);
            profile.put("preferredPosition", "midfielder");
            profile.put("playStyle", "balanced");
            profile.put("strength", 50);
            profile.put("speed", 50);
            profile.put("stamina", 50);
            profile.put("technique", 50);
            profile.put("teamwork", 50);

            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalMatches", 0);
            stats.put("wins", 0);
            stats.put("losses", 0);
            stats.put("draws", 0);
            stats.put("goals", 0);
            stats.put("assists", 0);
            stats.put("winRate", 0.0);

            Map<String, Object> player = new java.util.HashMap<>();
            player.put("userId", userId);
            player.put("name", mAuth.getCurrentUser().getDisplayName() != null ?
                    mAuth.getCurrentUser().getDisplayName() : "لاعب");
            player.put("email", mAuth.getCurrentUser().getEmail());
            player.put("profile", profile);
            player.put("stats", stats);
            player.put("createdAt", System.currentTimeMillis());
            player.put("updatedAt", System.currentTimeMillis());

            db.collection("players")
                    .document(userId)
                    .set(player)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Player profile created successfully");
                        onSuccess.run();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creating player profile", e);
                        hideLoading();
                        Toast.makeText(requireContext(),
                                "حدث خطأ في إنشاء الملف الشخصي",
                                Toast.LENGTH_SHORT).show();
                    });
        }

        private void updatePlayerProfile(String userId, DocumentSnapshot doc, Runnable onSuccess) {
            Log.d(TAG, "Updating player profile for user: " + userId);

            Map<String, Object> updates = new java.util.HashMap<>();

            Map<String, Object> existingProfile = (Map<String, Object>) doc.get("profile");
            if (existingProfile == null) {
                existingProfile = new java.util.HashMap<>();
            }

            if (!existingProfile.containsKey("level")) {
                updates.put("profile.level", 1);
            }
            if (!existingProfile.containsKey("rating")) {
                updates.put("profile.rating", 0.0);
            }
            if (!existingProfile.containsKey("preferredPosition")) {
                updates.put("profile.preferredPosition", "midfielder");
            }

            if (updates.isEmpty()) {
                onSuccess.run();
                return;
            }

            db.collection("players")
                    .document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Player profile updated successfully");
                        onSuccess.run();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating player profile", e);
                        hideLoading();
                        Toast.makeText(requireContext(),
                                "حدث خطأ في تحديث الملف الشخصي",
                                Toast.LENGTH_SHORT).show();
                    });
        }

        private void hideLoading() {
            createButton.setEnabled(true);
            progressBar.setVisibility(View.GONE);
        }

        // ════════════════════════════════════════════════════════════
        // 🔧 Helper Methods
        // ════════════════════════════════════════════════════════════

        private long getSelectedDateTime() {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, dateSpinner.getSelectedItemPosition());

            String timeSlot = timeSpinner.getSelectedItem().toString();
            String[] parts = timeSlot.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            return calendar.getTimeInMillis();
        }

        private String[] getNextDays(int count) {
            String[] days = new String[count];
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE، dd MMMM", new Locale("ar"));

            for (int i = 0; i < count; i++) {
                days[i] = sdf.format(calendar.getTime());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            return days;
        }

        private String[] getTimeSlots() {
            return new String[]{
                    "09:00", "10:00", "11:00", "12:00",
                    "13:00", "14:00", "15:00", "16:00",
                    "17:00", "18:00", "19:00", "20:00",
                    "21:00", "22:00", "23:00"
            };
        }

        private TextView createText(String text, int size, String color, int weight) {
            return UiHelper.createText(requireContext(), text, size, color, weight);
        }

        private int dp(int value) {
            return UiHelper.dp(requireContext(), value);
        }

        // ════════════════════════════════════════════════════════════
        // 📦 Listener & Data Models
        // ════════════════════════════════════════════════════════════

        public void setOnCreateListener(OnCreateListener listener) {
            this.listener = listener;
        }

        public interface OnCreateListener {
            void onCreate(String fieldId, String fieldName, String type, boolean openMode, long dateTime);
        }

        private static class FieldData {
            String id;
            String name;
        }
    }