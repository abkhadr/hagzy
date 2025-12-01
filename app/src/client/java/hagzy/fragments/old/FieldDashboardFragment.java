/*
package hagzy.fragments.old;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import hagzy.activities.FieldActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

*/
/**
 * Fragment لإدارة الملعب وعرض التحليلات والإحصائيات
 * يعرض معلومات شاملة عن الحجوزات والإيرادات والمستخدمين
 *//*

public class FieldDashboardFragment extends Fragment {

    private String fieldId;
    private DatabaseReference realtimeDB;
    private FirebaseFirestore firestore;
    private LinearLayout layout;

    // Analytics Data
    private int totalBookings = 0;
    private int activeBookings = 0;
    private int completedBookings = 0;
    private int cancelledBookings = 0;
    private double totalRevenue = 0;
    private double todayRevenue = 0;
    private double weekRevenue = 0;
    private double monthRevenue = 0;
    private int todayBookings = 0;
    private int weekBookings = 0;
    private int monthBookings = 0;

    // Chart Data
    private Map<String, Integer> dailyBookingsMap = new HashMap<>();
    private Map<String, Double> dailyRevenueMap = new HashMap<>();
    private List<BookingItem> recentBookings = new ArrayList<>();

    // UI Components
    private LinearLayout statsContainer;
    private LinearLayout chartContainer;
    private LinearLayout recentBookingsContainer;

    public static FieldDashboardFragment newInstance(String fieldId) {
        FieldDashboardFragment fragment = new FieldDashboardFragment();
        Bundle args = new Bundle();
        args.putString("fieldId", fieldId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            fieldId = getArguments().getString("fieldId");
        }

        realtimeDB = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        ScrollView scroll = new ScrollView(getContext());
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#F8F8F8"));

        layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        buildHeader();
        buildOverviewStats();
        buildRevenueStats();
        buildTimeBasedStats();
        buildBookingsChart();
        buildRecentBookings();
        buildQuickActions();

        scroll.addView(layout);

        ViewCompat.setOnApplyWindowInsetsListener(layout, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            layout.setPadding(0, top, 0, bottom);
            return insets;
        });

        loadDashboardData();

        return scroll;
    }

    private void buildHeader() {
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(12), dp(16), dp(12));
        header.setBackgroundColor(Color.WHITE);

        ImageView backBtn = new ImageView(getContext());
        backBtn.setImageResource(R.drawable.arrow_left);
        backBtn.setColorFilter(Color.parseColor("#4B463D"), PorterDuff.Mode.SRC_IN);
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dp(32), dp(32));
        backParams.setMarginEnd(dp(12));
        backBtn.setLayoutParams(backParams);
        backBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        LinearLayout titleContainer = new LinearLayout(getContext());
        titleContainer.setOrientation(LinearLayout.VERTICAL);
        titleContainer.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

        TextView title = new TextView(getContext());
        title.setText("📊 لوحة التحكم");
        title.setTextSize(18);
        title.setTypeface(ThemeManager.fontBold());
        title.setTextColor(Color.parseColor("#4B463D"));
        title.setTranslationY(-dpf(1.5f));

        TextView subtitle = new TextView(getContext());
        subtitle.setText("إحصائيات وتحليلات الملعب");
        subtitle.setTextSize(12);
        subtitle.setTypeface(ThemeManager.fontSemiBold());
        subtitle.setTextColor(Color.parseColor("#804B463D"));
        subtitle.setTranslationY(-dpf(1f));

        titleContainer.addView(title);
        titleContainer.addView(subtitle);

        header.addView(backBtn);
        header.addView(titleContainer);
        header.setElevation(dp(4));

        layout.addView(header);
    }

    private void buildOverviewStats() {
        statsContainer = createSection("📈 نظرة عامة");

        LinearLayout statsGrid = new LinearLayout(getContext());
        statsGrid.setOrientation(LinearLayout.VERTICAL);
        statsGrid.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        // First row
        LinearLayout row1 = new LinearLayout(getContext());
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        row1.addView(createStatCard("إجمالي الحجوزات", "0", "📋", "#2196F3", "totalBookings"));
        row1.addView(createStatCard("الحجوزات النشطة", "0", "🟢", "#4CAF50", "activeBookings"));

        // Second row
        LinearLayout row2 = new LinearLayout(getContext());
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams row2Params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        row2Params.topMargin = dp(8);
        row2.setLayoutParams(row2Params);

        row2.addView(createStatCard("حجوزات مكتملة", "0", "✅", "#FF9800", "completedBookings"));
        row2.addView(createStatCard("حجوزات ملغاة", "0", "❌", "#F44336", "cancelledBookings"));

        statsGrid.addView(row1);
        statsGrid.addView(row2);

        statsContainer.addView(statsGrid);
        layout.addView(statsContainer);
    }

    private void buildRevenueStats() {
        LinearLayout revenueSection = createSection("💰 الإيرادات");

        // Total revenue card (highlighted)
        LinearLayout totalRevenueCard = new LinearLayout(getContext());
        totalRevenueCard.setOrientation(LinearLayout.VERTICAL);
        totalRevenueCard.setGravity(Gravity.CENTER);
        totalRevenueCard.setPadding(dp(20), dp(20), dp(20), dp(20));

        GradientDrawable totalBg = new GradientDrawable();
        totalBg.setCornerRadius(dp(16));
        totalBg.setColors(new int[]{Color.parseColor("#4CAF50"), Color.parseColor("#45a049")});
        totalBg.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        totalRevenueCard.setBackground(totalBg);

        LinearLayout.LayoutParams totalParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        totalParams.bottomMargin = dp(12);
        totalRevenueCard.setLayoutParams(totalParams);

        TextView totalLabel = new TextView(getContext());
        totalLabel.setText("💎 إجمالي الإيرادات");
        totalLabel.setTextSize(14);
        totalLabel.setTypeface(ThemeManager.fontSemiBold());
        totalLabel.setTextColor(Color.WHITE);
        totalLabel.setTranslationY(-dpf(1f));

        TextView totalValue = new TextView(getContext());
        totalValue.setText("0.00 ج.م");
        totalValue.setTextSize(32);
        totalValue.setTypeface(ThemeManager.fontBold());
        totalValue.setTextColor(Color.WHITE);
        totalValue.setTranslationY(-dpf(2f));
        totalValue.setTag("totalRevenue");

        totalRevenueCard.addView(totalLabel);
        totalRevenueCard.addView(totalValue);

        // Period revenues
        LinearLayout periodsRow = new LinearLayout(getContext());
        periodsRow.setOrientation(LinearLayout.HORIZONTAL);
        periodsRow.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        periodsRow.addView(createRevenueCard("اليوم", "0", "📅", "#FF9800", "todayRevenue"));
        periodsRow.addView(createRevenueCard("الأسبوع", "0", "📆", "#9C27B0", "weekRevenue"));
        periodsRow.addView(createRevenueCard("الشهر", "0", "📊", "#3F51B5", "monthRevenue"));

        revenueSection.addView(totalRevenueCard);
        revenueSection.addView(periodsRow);
        layout.addView(revenueSection);
    }

    private void buildTimeBasedStats() {
        LinearLayout timeSection = createSection("⏰ حجوزات الفترات");

        LinearLayout timeRow = new LinearLayout(getContext());
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        timeRow.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        timeRow.addView(createTimeStatCard("اليوم", "0", "🌅", "#FF5722", "todayBookings"));
        timeRow.addView(createTimeStatCard("الأسبوع", "0", "🗓️", "#00BCD4", "weekBookings"));
        timeRow.addView(createTimeStatCard("الشهر", "0", "📅", "#673AB7", "monthBookings"));

        timeSection.addView(timeRow);
        layout.addView(timeSection);
    }

    private void buildBookingsChart() {
        chartContainer = createSection("📊 الرسم البياني - آخر 7 أيام");

        LinearLayout chartContent = new LinearLayout(getContext());
        chartContent.setOrientation(LinearLayout.VERTICAL);
        chartContent.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        chartContent.setTag("chartContent");

        chartContainer.addView(chartContent);
        layout.addView(chartContainer);
    }

    private void buildRecentBookings() {
        recentBookingsContainer = createSection("📋 آخر الحجوزات");

        LinearLayout bookingsList = new LinearLayout(getContext());
        bookingsList.setOrientation(LinearLayout.VERTICAL);
        bookingsList.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        bookingsList.setTag("bookingsList");

        recentBookingsContainer.addView(bookingsList);
        layout.addView(recentBookingsContainer);
    }

    private void buildQuickActions() {
        LinearLayout actionsSection = createSection("⚡ إجراءات سريعة");

        LinearLayout actionsGrid = new LinearLayout(getContext());
        actionsGrid.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row1 = new LinearLayout(getContext());
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        row1.addView(createActionCard("➕ إضافة حجز", "#4CAF50", v -> addNewBooking()));
        row1.addView(createActionCard("📝 إدارة الحجوزات", "#2196F3", v -> manageBookings()));

        LinearLayout row2 = new LinearLayout(getContext());
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams row2Params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        row2Params.topMargin = dp(8);
        row2.setLayoutParams(row2Params);

        row2.addView(createActionCard("📊 تقرير مفصل", "#FF9800", v -> showDetailedReport()));
        row2.addView(createActionCard("⚙️ الإعدادات", "#607D8B", v -> openSettings()));

        actionsGrid.addView(row1);
        actionsGrid.addView(row2);

        actionsSection.addView(actionsGrid);
        layout.addView(actionsSection);
    }

    private LinearLayout createSection(String title) {
        LinearLayout section = new LinearLayout(getContext());
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(dp(16), dp(12), dp(16), dp(12));
        section.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        sectionParams.topMargin = dp(8);
        section.setLayoutParams(sectionParams);

        TextView sectionTitle = new TextView(getContext());
        sectionTitle.setText(title);
        sectionTitle.setTextSize(16);
        sectionTitle.setTypeface(ThemeManager.fontBold());
        sectionTitle.setTextColor(Color.parseColor("#4B463D"));
        sectionTitle.setTranslationY(-dpf(1.5f));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        titleParams.bottomMargin = dp(12);
        sectionTitle.setLayoutParams(titleParams);

        section.addView(sectionTitle);
        return section;
    }

    private LinearLayout createStatCard(String label, String value, String icon, String color, String tag) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        params.setMarginEnd(dp(8));
        card.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));
        bg.setColor(Color.parseColor(color + "15"));
        bg.setStroke(dp(2), Color.parseColor(color + "40"));
        card.setBackground(bg);

        TextView iconText = new TextView(getContext());
        iconText.setText(icon);
        iconText.setTextSize(32);

        TextView valueText = new TextView(getContext());
        valueText.setText(value);
        valueText.setTextSize(24);
        valueText.setTypeface(ThemeManager.fontBold());
        valueText.setTextColor(Color.parseColor(color));
        valueText.setTranslationY(-dpf(2f));
        valueText.setTag(tag);

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(12);
        labelText.setTypeface(ThemeManager.fontSemiBold());
        labelText.setTextColor(Color.parseColor("#804B463D"));
        labelText.setTranslationY(-dpf(1f));
        labelText.setGravity(Gravity.CENTER);

        card.addView(iconText);
        card.addView(valueText);
        card.addView(labelText);

        return card;
    }

    private LinearLayout createRevenueCard(String label, String value, String icon, String color, String tag) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        params.setMarginEnd(dp(6));
        card.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(Color.parseColor(color + "15"));
        bg.setStroke(dp(2), Color.parseColor(color));
        card.setBackground(bg);

        TextView iconText = new TextView(getContext());
        iconText.setText(icon);
        iconText.setTextSize(24);

        TextView valueText = new TextView(getContext());
        valueText.setText(value + " ج.م");
        valueText.setTextSize(16);
        valueText.setTypeface(ThemeManager.fontBold());
        valueText.setTextColor(Color.parseColor(color));
        valueText.setTranslationY(-dpf(1.5f));
        valueText.setTag(tag);

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(11);
        labelText.setTypeface(ThemeManager.fontSemiBold());
        labelText.setTextColor(Color.parseColor("#804B463D"));
        labelText.setTranslationY(-dpf(0.5f));

        card.addView(iconText);
        card.addView(valueText);
        card.addView(labelText);

        return card;
    }

    private LinearLayout createTimeStatCard(String label, String value, String icon, String color, String tag) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        params.setMarginEnd(dp(6));
        card.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(Color.parseColor(color + "20"));
        card.setBackground(bg);

        TextView iconText = new TextView(getContext());
        iconText.setText(icon);
        iconText.setTextSize(28);

        TextView valueText = new TextView(getContext());
        valueText.setText(value);
        valueText.setTextSize(20);
        valueText.setTypeface(ThemeManager.fontBold());
        valueText.setTextColor(Color.parseColor(color));
        valueText.setTranslationY(-dpf(1.5f));
        valueText.setTag(tag);

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(11);
        labelText.setTypeface(ThemeManager.fontSemiBold());
        labelText.setTextColor(Color.parseColor("#804B463D"));
        labelText.setTranslationY(-dpf(0.5f));

        card.addView(iconText);
        card.addView(valueText);
        card.addView(labelText);

        return card;
    }

    private LinearLayout createActionCard(String label, String color, View.OnClickListener listener) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        params.setMarginEnd(dp(8));
        card.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(Color.parseColor(color));
        card.setBackground(bg);

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(13);
        labelText.setTypeface(ThemeManager.fontBold());
        labelText.setTextColor(Color.WHITE);
        labelText.setTranslationY(-dpf(1f));
        labelText.setGravity(Gravity.CENTER);

        card.addView(labelText);
        card.setOnClickListener(listener);

        return card;
    }

    private void loadDashboardData() {
        if (fieldId == null) return;

        // Initialize last 7 days
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("d MMM", new Locale("ar"));

        for (int i = 6; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            String dayKey = dayFormat.format(calendar.getTime());
            dailyBookingsMap.put(dayKey, 0);
            dailyRevenueMap.put(dayKey, 0.0);
        }

        // Load all bookings from Realtime DB
        realtimeDB.child("fields").child(fieldId).child("bookings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        resetStats();

                        for (DataSnapshot bookingSnap : snapshot.getChildren()) {
                            String bookingDocId = bookingSnap.getKey();
                            processBookingData(bookingDocId, bookingSnap);
                        }

                        updateAllUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Dashboard", "Error loading data", error.toException());
                    }
                });
    }

    private void processBookingData(String bookingDocId, DataSnapshot bookingSnap) {
        Boolean available = bookingSnap.child("available").getValue(Boolean.class);
        Double price = bookingSnap.child("price").getValue(Double.class);
        String name = bookingSnap.child("name").getValue(String.class);

        // Get booking details from Firestore
        firestore.collection("fields")
                .document(fieldId)
                .collection("bookings")
                .document(bookingDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String status = doc.getString("status");
                        com.google.firebase.Timestamp bookingTS = doc.getTimestamp("bookingDate");
                        com.google.firebase.Timestamp endTS = doc.getTimestamp("endTime");
                        String bookedByName = doc.getString("bookedByName");

                        // Update stats
                        totalBookings++;

                        if ("booked".equals(status)) {
                            // Check if still active
                            if (endTS != null && endTS.toDate().after(new Date())) {
                                activeBookings++;
                            } else {
                                completedBookings++;
                            }
                        } else if ("completed".equals(status)) {
                            completedBookings++;
                        } else if ("cancelled".equals(status)) {
                            cancelledBookings++;
                        }

                        // Revenue calculation
                        if (price != null && ("booked".equals(status) || "completed".equals(status))) {
                            totalRevenue += price;

                            // Time-based revenue
                            if (bookingTS != null) {
                                Date bookingDate = bookingTS.toDate();
                                long daysDiff = (System.currentTimeMillis() - bookingDate.getTime()) / (1000 * 60 * 60 * 24);

                                if (daysDiff == 0) {
                                    todayRevenue += price;
                                    todayBookings++;
                                }
                                if (daysDiff < 7) {
                                    weekRevenue += price;
                                    weekBookings++;
                                }
                                if (daysDiff < 30) {
                                    monthRevenue += price;
                                    monthBookings++;
                                }

                                // Chart data
                                SimpleDateFormat dayFormat = new SimpleDateFormat("d MMM", new Locale("ar"));
                                String dayKey = dayFormat.format(bookingDate);
                                if (dailyBookingsMap.containsKey(dayKey)) {
                                    dailyBookingsMap.put(dayKey, dailyBookingsMap.get(dayKey) + 1);
                                    dailyRevenueMap.put(dayKey, dailyRevenueMap.get(dayKey) + price);
                                }
                            }
                        }

                        // Add to recent bookings
                        if (recentBookings.size() < 5) {
                            recentBookings.add(new BookingItem(bookingDocId, name, price,
                                    bookedByName, bookingTS != null ? bookingTS.toDate() : null, status));
                        }

                        updateAllUI();
                    }
                });
    }

    private void resetStats() {
        totalBookings = 0;
        activeBookings = 0;
        completedBookings = 0;
        cancelledBookings = 0;
        totalRevenue = 0;
        todayRevenue = 0;
        weekRevenue = 0;
        monthRevenue = 0;
        todayBookings = 0;
        weekBookings = 0;
        monthBookings = 0;
        recentBookings.clear();

        // Reset daily maps
        for (String key : dailyBookingsMap.keySet()) {
            dailyBookingsMap.put(key, 0);
        }
        for (String key : dailyRevenueMap.keySet()) {
            dailyRevenueMap.put(key, 0.0);
        }
    }

    private void updateAllUI() {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            // Update stats
            updateTextViewByTag("totalBookings", String.valueOf(totalBookings));
            updateTextViewByTag("activeBookings", String.valueOf(activeBookings));
            updateTextViewByTag("completedBookings", String.valueOf(completedBookings));
            updateTextViewByTag("cancelledBookings", String.valueOf(cancelledBookings));

            // Update revenue
            updateTextViewByTag("totalRevenue", String.format(Locale.getDefault(), "%.2f ج.م", totalRevenue));
            updateTextViewByTag("todayRevenue", String.format(Locale.getDefault(), "%.0f", todayRevenue));
            updateTextViewByTag("weekRevenue", String.format(Locale.getDefault(), "%.0f", weekRevenue));
            updateTextViewByTag("monthRevenue", String.format(Locale.getDefault(), "%.0f", monthRevenue));

            // Update time-based
            updateTextViewByTag("todayBookings", String.valueOf(todayBookings));
            updateTextViewByTag("weekBookings", String.valueOf(weekBookings));
            updateTextViewByTag("monthBookings", String.valueOf(monthBookings));

            // Update chart
            updateChart();

            // Update recent bookings
            updateRecentBookings();
        });
    }

    private void updateTextViewByTag(String tag, String value) {
        View view = layout.findViewWithTag(tag);
        if (view instanceof TextView) {
            ((TextView) view).setText(value);
        }
    }

    private void updateChart() {
        LinearLayout chartContent = layout.findViewWithTag("chartContent");
        if (chartContent == null) return;

        chartContent.removeAllViews();

        // Find max value for scaling
        int maxBookings = 1;
        for (Integer count : dailyBookingsMap.values()) {
            maxBookings = Math.max(maxBookings, count);
        }

        // Create bars
        for (Map.Entry<String, Integer> entry : dailyBookingsMap.entrySet()) {
            chartContent.addView(createChartBar(entry.getKey(), entry.getValue(), maxBookings));
        }
    }

    private LinearLayout createChartBar(String label, int value, int maxValue) {
        LinearLayout barContainer = new LinearLayout(getContext());
        barContainer.setOrientation(LinearLayout.VERTICAL);
        barContainer.setGravity(Gravity.BOTTOM);
        barContainer.setPadding(dp(4), 0, dp(4), 0);
        barContainer.setLayoutParams(new LinearLayout.LayoutParams(0, dp(150), 1f));

        // Value text
        TextView valueText = new TextView(getContext());
        valueText.setText(String.valueOf(value));
        valueText.setTextSize(11);
        valueText.setTypeface(ThemeManager.fontBold());
        valueText.setTextColor(Color.parseColor("#4B463D"));
        valueText.setGravity(Gravity.CENTER);
        valueText.setTranslationY(-dpf(0.5f));
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        valueParams.bottomMargin = dp(4);
        valueText.setLayoutParams(valueParams);

        // Bar
        View bar = new View(getContext());
        int barHeight = maxValue > 0 ? (int) ((value / (float) maxValue) * dp(80)) : dp(5);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(MATCH_PARENT, barHeight);
        barParams.bottomMargin = dp(4);
        bar.setLayoutParams(barParams);

        GradientDrawable barBg = new GradientDrawable();
        barBg.setCornerRadius(dp(8));
        barBg.setColors(new int[]{Color.parseColor("#4CAF50"), Color.parseColor("#81C784")});
        barBg.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        bar.setBackground(barBg);

        // Label
        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(10);
        labelText.setTypeface(ThemeManager.fontSemiBold());
        labelText.setTextColor(Color.parseColor("#804B463D"));
        labelText.setGravity(Gravity.CENTER);
        labelText.setTranslationY(-dpf(0.5f));

        barContainer.addView(valueText);
        barContainer.addView(bar);
        barContainer.addView(labelText);

        return barContainer;
    }

    private void updateRecentBookings() {
        LinearLayout bookingsList = layout.findViewWithTag("bookingsList");
        if (bookingsList == null) return;

        bookingsList.removeAllViews();

        if (recentBookings.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("لا توجد حجوزات حديثة");
            emptyText.setTextSize(14);
            emptyText.setTypeface(ThemeManager.fontSemiBold());
            emptyText.setTextColor(Color.parseColor("#804B463D"));
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(dp(16), dp(32), dp(16), dp(32));
            bookingsList.addView(emptyText);
            return;
        }

        for (BookingItem booking : recentBookings) {
            bookingsList.addView(createBookingItem(booking));
        }
    }

    private LinearLayout createBookingItem(BookingItem booking) {
        LinearLayout item = new LinearLayout(getContext());
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        itemParams.bottomMargin = dp(8);
        item.setLayoutParams(itemParams);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(Color.parseColor("#F8F8F8"));
        bg.setStroke(dp(1), Color.parseColor("#E0E0E0"));
        item.setBackground(bg);

        // Icon
        TextView icon = new TextView(getContext());
        String statusIcon = "booked".equals(booking.status) ? "🟢" :
                "completed".equals(booking.status) ? "✅" :
                        "cancelled".equals(booking.status) ? "❌" : "📋";
        icon.setText(statusIcon);
        icon.setTextSize(24);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        iconParams.setMarginEnd(dp(12));
        icon.setLayoutParams(iconParams);
        icon.setGravity(Gravity.CENTER);

        // Info container
        LinearLayout infoContainer = new LinearLayout(getContext());
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        infoContainer.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

        TextView nameText = new TextView(getContext());
        nameText.setText(booking.name != null ? booking.name : "حجز");
        nameText.setTextSize(14);
        nameText.setTypeface(ThemeManager.fontBold());
        nameText.setTextColor(Color.parseColor("#4B463D"));
        nameText.setTranslationY(-dpf(1f));

        TextView detailsText = new TextView(getContext());
        String details = "";
        if (booking.bookedByName != null) {
            details += "👤 " + booking.bookedByName;
        }
        if (booking.bookingDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM, hh:mm a", new Locale("ar"));
            if (!details.isEmpty()) details += " • ";
            details += "📅 " + sdf.format(booking.bookingDate);
        }
        detailsText.setText(details);
        detailsText.setTextSize(11);
        detailsText.setTypeface(ThemeManager.fontSemiBold());
        detailsText.setTextColor(Color.parseColor("#804B463D"));
        detailsText.setTranslationY(-dpf(0.5f));

        infoContainer.addView(nameText);
        infoContainer.addView(detailsText);

        // Price
        TextView priceText = new TextView(getContext());
        priceText.setText(String.format(Locale.getDefault(), "%.0f ج.م",
                booking.price != null ? booking.price : 0));
        priceText.setTextSize(14);
        priceText.setTypeface(ThemeManager.fontBold());
        priceText.setTextColor(Color.parseColor("#4CAF50"));
        priceText.setTranslationY(-dpf(1f));

        item.addView(icon);
        item.addView(infoContainer);
        item.addView(priceText);

        return item;
    }

    // Quick Actions
    private void addNewBooking() {
        // Show dialog to add new booking type
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());

        LinearLayout dialogLayout = new LinearLayout(getContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(dp(24), dp(24), dp(24), dp(24));

        TextView title = new TextView(getContext());
        title.setText("➕ إضافة نوع حجز جديد");
        title.setTextSize(18);
        title.setTypeface(ThemeManager.fontBold());
        title.setTextColor(Color.parseColor("#4B463D"));
        title.setTranslationY(-dpf(1.5f));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        titleParams.bottomMargin = dp(16);
        title.setLayoutParams(titleParams);
        dialogLayout.addView(title);

        // Name input
        android.widget.EditText nameInput = new android.widget.EditText(getContext());
        nameInput.setHint("اسم الحجز (مثال: ملعب 5، صالة كبيرة)");
        nameInput.setPadding(dp(16), dp(12), dp(16), dp(12));
        GradientDrawable nameBg = new GradientDrawable();
        nameBg.setCornerRadius(dp(8));
        nameBg.setStroke(dp(2), Color.parseColor("#E0E0E0"));
        nameBg.setColor(Color.WHITE);
        nameInput.setBackground(nameBg);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        nameParams.bottomMargin = dp(12);
        nameInput.setLayoutParams(nameParams);
        dialogLayout.addView(nameInput);

        // Category input
        android.widget.EditText categoryInput = new android.widget.EditText(getContext());
        categoryInput.setHint("الفئة (مثال: ملاعب، قاعات)");
        categoryInput.setPadding(dp(16), dp(12), dp(16), dp(12));
        GradientDrawable catBg = new GradientDrawable();
        catBg.setCornerRadius(dp(8));
        catBg.setStroke(dp(2), Color.parseColor("#E0E0E0"));
        catBg.setColor(Color.WHITE);
        categoryInput.setBackground(catBg);
        LinearLayout.LayoutParams catParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        catParams.bottomMargin = dp(12);
        categoryInput.setLayoutParams(catParams);
        dialogLayout.addView(categoryInput);

        // Price input
        android.widget.EditText priceInput = new android.widget.EditText(getContext());
        priceInput.setHint("السعر (ج.م)");
        priceInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        priceInput.setPadding(dp(16), dp(12), dp(16), dp(12));
        GradientDrawable priceBg = new GradientDrawable();
        priceBg.setCornerRadius(dp(8));
        priceBg.setStroke(dp(2), Color.parseColor("#E0E0E0"));
        priceBg.setColor(Color.WHITE);
        priceInput.setBackground(priceBg);
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        priceParams.bottomMargin = dp(12);
        priceInput.setLayoutParams(priceParams);
        dialogLayout.addView(priceInput);

        // Quantity input
        android.widget.EditText quantityInput = new android.widget.EditText(getContext());
        quantityInput.setHint("الكمية المتاحة");
        quantityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        quantityInput.setPadding(dp(16), dp(12), dp(16), dp(12));
        GradientDrawable qtyBg = new GradientDrawable();
        qtyBg.setCornerRadius(dp(8));
        qtyBg.setStroke(dp(2), Color.parseColor("#E0E0E0"));
        qtyBg.setColor(Color.WHITE);
        quantityInput.setBackground(qtyBg);
        LinearLayout.LayoutParams qtyParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        qtyParams.bottomMargin = dp(12);
        quantityInput.setLayoutParams(qtyParams);
        dialogLayout.addView(quantityInput);

        // Description input
        android.widget.EditText descInput = new android.widget.EditText(getContext());
        descInput.setHint("الوصف (اختياري)");
        descInput.setPadding(dp(16), dp(12), dp(16), dp(12));
        descInput.setMinLines(3);
        GradientDrawable descBg = new GradientDrawable();
        descBg.setCornerRadius(dp(8));
        descBg.setStroke(dp(2), Color.parseColor("#E0E0E0"));
        descBg.setColor(Color.WHITE);
        descInput.setBackground(descBg);
        dialogLayout.addView(descInput);

        builder.setView(dialogLayout);
        builder.setPositiveButton("إضافة", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString().trim();
            String description = descInput.getText().toString().trim();

            if (name.isEmpty() || category.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
                Toast.makeText(getContext(), "يرجى ملء جميع الحقول المطلوبة", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int quantity = Integer.parseInt(quantityStr);

                // Add to Realtime DB
                String bookingId = realtimeDB.child("fields").child(fieldId).child("bookings").push().getKey();

                Map<String, Object> bookingData = new HashMap<>();
                bookingData.put("name", name);
                bookingData.put("category", category);
                bookingData.put("price", price);
                bookingData.put("quantity", quantity);
                bookingData.put("description", description);
                bookingData.put("available", true);

                realtimeDB.child("fields").child(fieldId).child("bookings")
                        .child(bookingId)
                        .setValue(bookingData)
                        .addOnSuccessListener(aVoid -> {
                            // Add to Firestore
                            Map<String, Object> firestoreData = new HashMap<>();
                            firestoreData.put("status", "available");

                            firestore.collection("fields")
                                    .document(fieldId)
                                    .collection("bookings")
                                    .document(bookingId)
                                    .set(firestoreData);

                            Toast.makeText(getContext(), "✅ تم إضافة نوع الحجز بنجاح", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "❌ فشل في الإضافة", Toast.LENGTH_SHORT).show();
                        });

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "قيمة السعر أو الكمية غير صحيحة", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

*/
/*    private void manageBookings() {
        // Navigate to bookings management
        if (getActivity() != null && getActivity() instanceof FieldActivity) {
            ((FieldActivity) getActivity()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, BookingsFragment.newInstance(fieldId))
                    .addToBackStack(null)
                    .commit();
        }
    }*//*


    private void showDetailedReport() {
        // Create detailed report dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());

        LinearLayout reportLayout = new LinearLayout(getContext());
        reportLayout.setOrientation(LinearLayout.VERTICAL);
        reportLayout.setPadding(dp(24), dp(24), dp(24), dp(24));

        TextView title = new TextView(getContext());
        title.setText("📊 تقرير تفصيلي");
        title.setTextSize(18);
        title.setTypeface(ThemeManager.fontBold());
        title.setTextColor(Color.parseColor("#4B463D"));
        title.setTranslationY(-dpf(1.5f));
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        titleParams.bottomMargin = dp(16);
        title.setLayoutParams(titleParams);
        reportLayout.addView(title);

        // Report content
        reportLayout.addView(createReportRow("إجمالي الحجوزات", String.valueOf(totalBookings)));
        reportLayout.addView(createReportRow("الحجوزات النشطة", String.valueOf(activeBookings)));
        reportLayout.addView(createReportRow("الحجوزات المكتملة", String.valueOf(completedBookings)));
        reportLayout.addView(createReportRow("الحجوزات الملغاة", String.valueOf(cancelledBookings)));
        reportLayout.addView(createDivider());
        reportLayout.addView(createReportRow("إجمالي الإيرادات",
                String.format(Locale.getDefault(), "%.2f ج.م", totalRevenue)));
        reportLayout.addView(createReportRow("إيرادات اليوم",
                String.format(Locale.getDefault(), "%.2f ج.م", todayRevenue)));
        reportLayout.addView(createReportRow("إيرادات الأسبوع",
                String.format(Locale.getDefault(), "%.2f ج.م", weekRevenue)));
        reportLayout.addView(createReportRow("إيرادات الشهر",
                String.format(Locale.getDefault(), "%.2f ج.م", monthRevenue)));
        reportLayout.addView(createDivider());
        reportLayout.addView(createReportRow("حجوزات اليوم", String.valueOf(todayBookings)));
        reportLayout.addView(createReportRow("حجوزات الأسبوع", String.valueOf(weekBookings)));
        reportLayout.addView(createReportRow("حجوزات الشهر", String.valueOf(monthBookings)));

        // Calculations
        double avgBookingValue = totalBookings > 0 ? totalRevenue / totalBookings : 0;
        double completionRate = totalBookings > 0 ? (completedBookings * 100.0 / totalBookings) : 0;
        double cancellationRate = totalBookings > 0 ? (cancelledBookings * 100.0 / totalBookings) : 0;

        reportLayout.addView(createDivider());
        reportLayout.addView(createReportRow("متوسط قيمة الحجز",
                String.format(Locale.getDefault(), "%.2f ج.م", avgBookingValue)));
        reportLayout.addView(createReportRow("معدل الإكمال",
                String.format(Locale.getDefault(), "%.1f%%", completionRate)));
        reportLayout.addView(createReportRow("معدل الإلغاء",
                String.format(Locale.getDefault(), "%.1f%%", cancellationRate)));

        builder.setView(reportLayout);
        builder.setPositiveButton("إغلاق", null);
        builder.show();
    }

    private LinearLayout createReportRow(String label, String value) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(14);
        labelText.setTypeface(ThemeManager.fontSemiBold());
        labelText.setTextColor(Color.parseColor("#804B463D"));
        labelText.setTranslationY(-dpf(1f));
        labelText.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

        TextView valueText = new TextView(getContext());
        valueText.setText(value);
        valueText.setTextSize(14);
        valueText.setTypeface(ThemeManager.fontBold());
        valueText.setTextColor(Color.parseColor("#4B463D"));
        valueText.setGravity(Gravity.END);
        valueText.setTranslationY(-dpf(1f));

        row.addView(labelText);
        row.addView(valueText);

        return row;
    }

    private View createDivider() {
        View divider = new View(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, dp(1));
        params.setMargins(0, dp(8), 0, dp(8));
        divider.setLayoutParams(params);
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
        return divider;
    }

    private void openSettings() {
        Toast.makeText(getContext(), "⚙️ الإعدادات قريباً", Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return UiHelper.dp(requireContext(), value);
    }

    private float dpf(float value) {
        return UiHelper.dpFloat(requireContext(), value);
    }

    // ============================
    // BookingItem Model for Dashboard
    // ============================
    private static class BookingItem {
        String bookingDocId;
        String name;
        Double price;
        String bookedByName;
        Date bookingDate;
        String status;

        BookingItem(String bookingDocId, String name, Double price, String bookedByName,
                    Date bookingDate, String status) {
            this.bookingDocId = bookingDocId;
            this.name = name;
            this.price = price;
            this.bookedByName = bookedByName;
            this.bookingDate = bookingDate;
            this.status = status;
        }
    }
}*/
