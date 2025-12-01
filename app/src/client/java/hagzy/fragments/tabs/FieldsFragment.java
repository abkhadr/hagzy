package hagzy.fragments.tabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.CoachMarkHelper;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hagzy.activities.FieldActivity;

public class FieldsFragment extends Fragment {

    private static final String TAG = "FieldsFragment";

    private static final String COACH_KEY_FIELDS = "fields_onboarding_shown_v1";
    private View searchInputView;
    private View filterToggleView;
    private View firstFieldCard;

    private FirebaseFirestore db;
    private LinearLayout contentContainer;
    private ProgressBar progressBar;
    private TextView emptyText;
    private List<FieldData> fieldsList = new ArrayList<>();
    private List<FieldData> filteredFieldsList = new ArrayList<>();

    private String currentSortBy = "rating";
    private EditText searchInput;
    private LinearLayout filtersContainer;
    private boolean isFiltersExpanded = false;

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

        // إظهار Coach Marks بعد تحميل البيانات
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showCoachMarksIfNeeded();
        }, 800);
    }

    private View buildUI() {
        FrameLayout root = new FrameLayout(requireContext());
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(Color.parseColor("#FFFFFF"));

        // Fixed Search Section (Top)
        LinearLayout searchSection = new LinearLayout(requireContext());
        searchSection.setOrientation(LinearLayout.VERTICAL);
        searchSection.setPadding(dp(20), dp(8), dp(20), dp(8));
        searchSection.setBackgroundColor(Color.WHITE);

        FrameLayout.LayoutParams searchParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        searchParams.gravity = Gravity.TOP;
        searchSection.setLayoutParams(searchParams);

        // Search Container with Icon
        FrameLayout searchContainer = new FrameLayout(requireContext());

        // Search Input
        searchInput = createSearchInput();
        searchContainer.addView(searchInput);

        // Search Icon (Start)
        ImageView searchIcon = new ImageView(requireContext());
        FrameLayout.LayoutParams searchIconParams = new FrameLayout.LayoutParams(
                dp(24), dp(24)
        );
        searchIconParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        searchIconParams.setMarginStart(dp(16));
        searchIcon.setLayoutParams(searchIconParams);
        searchIcon.setImageResource(R.drawable.magnifying_glass); // Replace with search icon
        searchIcon.setColorFilter(Color.parseColor("#8E8E93"));
        searchContainer.addView(searchIcon);

        // Filter Toggle Button (End)
        ImageView filterToggle = new ImageView(requireContext());
        FrameLayout.LayoutParams filterIconParams = new FrameLayout.LayoutParams(
                dp(24), dp(24)
        );
        filterIconParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        filterIconParams.setMarginEnd(dp(16));
        filterToggle.setLayoutParams(filterIconParams);
        filterToggle.setImageResource(R.drawable.adjustments_horizontal); // Replace with filter icon
        filterToggle.setColorFilter(Color.parseColor("#000000"));
        filterToggle.setClickable(true);
        filterToggle.setFocusable(true);

        filterToggle.setOnClickListener(v -> {
            isFiltersExpanded = !isFiltersExpanded;

            if (isFiltersExpanded) {
                filtersContainer.setVisibility(View.VISIBLE);
            } else {
                filtersContainer.setVisibility(View.GONE);
            }
        });

        searchContainer.addView(filterToggle);
        filterToggleView = filterToggle;
        searchSection.addView(searchContainer);

        // Filters (Initially Hidden) - Above search
        filtersContainer = new LinearLayout(requireContext());
        filtersContainer.setOrientation(LinearLayout.HORIZONTAL);
        filtersContainer.setClipChildren(true);
        filtersContainer.setClipToPadding(true);
        LinearLayout.LayoutParams filtersParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        filtersParams.bottomMargin = dp(16);
        filtersContainer.setLayoutParams(filtersParams);
        filtersContainer.setVisibility(View.GONE);

        filtersContainer.addView(createFilterChip("الأعلى تقييمًا", "rating", true));
        filtersContainer.addView(createFilterChip("الأقرب", "distance", false));

        searchSection.addView(filtersContainer, 0); // Add at index 0 (before search)

        // Scrollable Content
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setFillViewport(true);

        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        scrollParams.topMargin = dp(52); // Reduced space since filters are collapsible
        scrollView.setLayoutParams(scrollParams);

        LinearLayout mainContainer = new LinearLayout(requireContext());
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(dp(20), dp(20), dp(20), dp(20));

        // Fields Container
        contentContainer = new LinearLayout(requireContext());
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setVisibility(View.GONE);
        mainContainer.addView(contentContainer);

        // Progress Bar
        progressBar = new ProgressBar(requireContext());
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                dp(36), dp(36)
        );
        progressParams.gravity = Gravity.CENTER;
        progressParams.topMargin = dp(60);
        progressBar.setLayoutParams(progressParams);
        mainContainer.addView(progressBar);

        // Empty Text
        emptyText = createText("لا توجد ملاعب متاحة", 15, "#999999", 1);
        emptyText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        emptyParams.topMargin = dp(60);
        emptyText.setLayoutParams(emptyParams);
        emptyText.setVisibility(View.GONE);
        mainContainer.addView(emptyText);

        scrollView.addView(mainContainer);

        // Add views to root
        root.addView(scrollView);
        root.addView(searchSection); // Add fixed search on top

        return root;
    }

    private EditText createSearchInput() {
        EditText input = new EditText(requireContext());
        input.setHint("بحث سريع");
        input.setTextSize(16);
        input.setTypeface(ThemeManager.fontRegular());
        input.setTextColor(Color.parseColor("#000000"));
        input.setHintTextColor(Color.parseColor("#8E8E93"));
        input.setPadding(dp(52), dp(8), dp(52), dp(8)); // Extra padding for icons
        input.setSingleLine(true);

        GradientDrawable bg = new GradientDrawable();
        bg.setStroke(dp(2), Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(12));
        input.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        input.setLayoutParams(params);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFields(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return input;
    }

    private View createFilterChip(String text, String sortKey, boolean selected) {
        LinearLayout chip = new LinearLayout(requireContext());
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(20), dp(10), dp(20), dp(10));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(dp(12));
        chip.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(24));
        if (selected) {
            bg.setColor(Color.parseColor("#000000"));
        } else {
            bg.setColor(Color.parseColor("#F5F5F5"));
        }
        chip.setBackground(bg);

        TextView label = createText(text, 14, selected ? "#FFFFFF" : "#000000", selected ? 3 : 1);
        chip.addView(label);

        chip.setTag(sortKey);
        chip.setOnClickListener(v -> {
            currentSortBy = sortKey;
            updateFilterChips();
            sortAndDisplayFields();
        });

        return chip;
    }

    private void updateFilterChips() {
        for (int i = 0; i < filtersContainer.getChildCount(); i++) {
            View child = filtersContainer.getChildAt(i);
            String tag = (String) child.getTag();
            boolean selected = tag != null && tag.equals(currentSortBy);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(24));
            if (selected) {
                bg.setColor(Color.parseColor("#000000"));
            } else {
                bg.setColor(Color.parseColor("#F5F5F5"));
            }
            child.setBackground(bg);

            TextView label = (TextView) ((ViewGroup) child).getChildAt(0);
            label.setTextColor(Color.parseColor(selected ? "#FFFFFF" : "#000000"));
            label.setTypeface( selected ? ThemeManager.fontBold() : ThemeManager.fontRegular());
        }
    }

    private void filterFields(String query) {
        filteredFieldsList.clear();

        if (query.isEmpty()) {
            filteredFieldsList.addAll(fieldsList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (FieldData field : fieldsList) {
                if (field.name.toLowerCase().contains(lowerQuery) ||
                        (field.district != null && field.district.toLowerCase().contains(lowerQuery)) ||
                        (field.city != null && field.city.toLowerCase().contains(lowerQuery))) {
                    filteredFieldsList.add(field);
                }
            }
        }

        sortAndDisplayFields();
    }

    private void loadFields() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        db.collection("fields")
                .whereEqualTo("isActive", true)
                .orderBy("rating.average", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (querySnapshot.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        return;
                    }

                    fieldsList.clear();
                    filteredFieldsList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        FieldData field = parseFieldData(doc);
                        if (field != null) {
                            fieldsList.add(field);
                            filteredFieldsList.add(field);
                        }
                    }

                    sortAndDisplayFields();
                    contentContainer.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error loading fields", e);
                });
    }

    private void sortAndDisplayFields() {
        if (currentSortBy.equals("distance")) {
            filteredFieldsList.sort((a, b) -> Integer.compare(a.distanceInMinutes, b.distanceInMinutes));
        } else if (currentSortBy.equals("rating")) {
            filteredFieldsList.sort((a, b) -> Double.compare(b.rating, a.rating));
        }

        displayFields();
    }

    private void displayFields() {
        contentContainer.removeAllViews();

        if (filteredFieldsList.isEmpty()) {
            emptyText.setText("لا توجد نتائج");
            emptyText.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.GONE);
            return;
        }

        emptyText.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);

        for (int i = 0; i < filteredFieldsList.size(); i++) {
            contentContainer.addView(createFieldCard(filteredFieldsList.get(i)));
        }
    }

    private View createFieldCard(FieldData field) {
        // Main Card LinearLayout
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(24), dp(16), dp(24), dp(16));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#FAFAFA"));
        cardBg.setCornerRadius(dp(24));
        card.setBackground(cardBg);

        card.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                        Log.d(TAG, "Opening field: " + field.name + " (ID: " + field.id + ")");

                        Intent intent = new Intent(requireContext(), FieldActivity.class);
                        intent.putExtra("fieldId", field.id);
                        startActivity(intent);
                    })
                    .start();
        });

        // Content Section
        LinearLayout contentSection = new LinearLayout(requireContext());
        contentSection.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        contentSection.setLayoutParams(contentParams);

        // Name
        TextView name = createText(field.name, 18, "#000000", 3);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        nameParams.bottomMargin = dp(6);
        name.setLayoutParams(nameParams);
        name.setMaxLines(1);
        contentSection.addView(name);

        // Rating Row
        LinearLayout ratingRow = new LinearLayout(requireContext());
        ratingRow.setOrientation(LinearLayout.HORIZONTAL);
        ratingRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams ratingRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ratingRowParams.bottomMargin = dp(8);
        ratingRow.setLayoutParams(ratingRowParams);

        // Star Icon
        TextView starIcon = createText("★", 14, "#000000", 1);
        ratingRow.addView(starIcon);

        // Rating Value
        TextView ratingText = createText(String.format("%.1f", field.rating), 14, "#000000", 2);
        LinearLayout.LayoutParams ratingTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ratingTextParams.setMarginStart(dp(4));
        ratingText.setLayoutParams(ratingTextParams);
        ratingRow.addView(ratingText);

        // Reviews Count
        TextView reviewsCount = createText("(" + field.totalReviews + ")", 14, "#888888", 2);
        LinearLayout.LayoutParams reviewsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        reviewsParams.setMarginStart(dp(4));
        reviewsCount.setLayoutParams(reviewsParams);
        ratingRow.addView(reviewsCount);

        contentSection.addView(ratingRow);

        // Location
        TextView location = createText(field.district + ", " + field.city, 14, "#666666", 2);
        LinearLayout.LayoutParams locParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        locParams.bottomMargin = dp(10);
        location.setLayoutParams(locParams);
        contentSection.addView(location);

        // Distance (Large and clear)
        @SuppressLint("DefaultLocale") TextView distance = createText(field.distanceInMinutes + " دقيقة • " +
                String.format("%.1f كم", field.distanceInKm), 16, "#000000", 3);
        contentSection.addView(distance);

        card.addView(contentSection);

        // Logo (Transparent background)
        ImageView logo = new ImageView(requireContext());
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(
                dp(72), dp(72)
        );
        logoParams.setMarginStart(dp(16));
        logo.setLayoutParams(logoParams);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Default placeholder URL
        Log.d(TAG, "field.coverImage: " + field.coverImage);
        String logoUrl = field.coverImage != null && !field.coverImage.isEmpty()
                ? field.coverImage
                : "https://via.placeholder.com/150/FFFFFF/CCCCCC?text=Logo";

        Glide.with(requireContext())
                .load(logoUrl)
                .centerInside()
                .into(logo);

        card.addView(logo);

        return card;
    }

    private FieldData parseFieldData(DocumentSnapshot doc) {
        try {
            FieldData data = new FieldData();

            data.id = doc.getId();
            data.name = doc.getString("name");
            data.description = doc.getString("description");
            data.coverImage = doc.getString("coverImage");
            data.isFeatured = doc.getBoolean("isFeatured") != null ?
                    doc.getBoolean("isFeatured") : false;

            Map<String, Object> location = (Map<String, Object>) doc.get("location");
            if (location != null) {
                data.address = (String) location.get("address");
                data.city = (String) location.get("city");
                data.district = (String) location.get("district");
            }

            Map<String, Object> pricing = (Map<String, Object>) doc.get("pricing");
            if (pricing != null) {
                data.hourlyRate = pricing.get("hourlyRate") != null ?
                        ((Long) pricing.get("hourlyRate")).intValue() : 0;
            }

            Map<String, Object> rating = (Map<String, Object>) doc.get("rating");
            if (rating != null) {
                data.rating = rating.get("average") != null ?
                        ((Number) rating.get("average")).doubleValue() : 0.0;
                data.totalReviews = rating.get("totalReviews") != null ?
                        ((Long) rating.get("totalReviews")).intValue() : 0;
            }

            // TODO: Calculate actual distance based on user location
            data.distanceInMinutes = 5 + (int)(Math.random() * 25);
            data.distanceInKm = data.distanceInMinutes * 0.8;

            return data;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing field data", e);
            return null;
        }
    }

    private void showCoachMarksIfNeeded() {
        if (getActivity() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("CoachMarkPrefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean(COACH_KEY_FIELDS, false)) {
            return;
        }

        startFieldsCoachMarks();
    }

    private void startFieldsCoachMarks() {
        if (getActivity() == null) return;

        CoachMarkHelper coach = new CoachMarkHelper(getActivity());

        // 1. شرح مربع البحث (يمكن التفاعل معه)
        if (searchInput != null) {
            coach.addStepWithInteraction(
                    searchInput,
                    "البحث السريع",
                    "ابحث عن الملاعب بالاسم أو المنطقة أو المدينة. جرب الكتابة الآن!",
                    0,
                    true  // السماح بالتفاعل
            );
        }

        // 2. شرح زر الفلتر (يمكن التفاعل معه)
        View filterToggle = findFilterToggle();
        if (filterToggle != null) {
            coach.addStepWithInteraction(
                    filterToggle,
                    "خيارات الفرز",
                    "اضغط هنا لفتح خيارات الفرز حسب التقييم أو المسافة. جرب الآن!",
                    2,
                    true  // السماح بالتفاعل
            );
        }

        // 3. شرح الفلاتر (إذا كانت ظاهرة)
        if (filtersContainer != null && filtersContainer.getVisibility() == View.VISIBLE) {
            coach.addStepWithInteraction(
                    filtersContainer,
                    "خيارات الترتيب",
                    "اختر كيف تريد ترتيب الملاعب: حسب التقييم أو حسب القرب منك",
                    2,
                    true  // السماح بالتفاعل
            );
        }

        // 4. شرح أول كارد ملعب
        if (contentContainer != null && contentContainer.getChildCount() > 0) {
            View firstCard = contentContainer.getChildAt(0);
            coach.addStepWithInteraction(
                    firstCard,
                    "بطاقة الملعب",
                    "كل بطاقة تعرض: اسم الملعب، التقييم، عدد المراجعات، الموقع، والمسافة. اضغط على أي بطاقة لعرض التفاصيل",
                    2,
                    true  // السماح بالتفاعل
            );
        }

        coach.setOnCompleteListener(() -> {
            // حفظ أنه تم عرض Coach Marks
            SharedPreferences prefs = requireContext().getSharedPreferences("CoachMarkPrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean(COACH_KEY_FIELDS, true).apply();
        });

        coach.start();
    }
    private View findFilterToggle() {
        // البحث عن زر الفلتر في الـ UI
        // يمكنك حفظه كـ member variable أثناء buildUI()
        return filterToggleView;
    }

    private TextView createText(String text, int size, String color, int weight) {
        return UiHelper.createText(requireContext(), text, size, color, weight);
    }

    private int dp(int value) {
        return UiHelper.dp(requireContext(), value);
    }

    private static class FieldData {
        String id;
        String name;
        String description;
        String coverImage;
        String address;
        String city;
        String district;
        int hourlyRate;
        double rating;
        int totalReviews;
        boolean isFeatured;
        int distanceInMinutes;
        double distanceInKm;
    }
}