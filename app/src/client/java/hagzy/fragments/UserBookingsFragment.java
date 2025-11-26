package hagzy.fragments;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
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
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment لعرض وإدارة حجوزات المستخدم
 * يعرض الحجوزات النشطة والمكتملة والملغاة
 * مع إمكانية عرض أكواد التفعيل QR
 */
public class UserBookingsFragment extends Fragment {

    private DatabaseReference realtimeDB;
    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private UserBookingsAdapter adapter;
    private List<UserBooking> bookingsList = new ArrayList<>();
    private LinearLayout layout;
    private LinearLayout statsContainer;

    // Statistics
    private int activeBookingsCount = 0;
    private int completedBookingsCount = 0;
    private int cancelledBookingsCount = 0;
    private double totalSpent = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        realtimeDB = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        ScrollView scroll = new ScrollView(getContext());
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#F8F8F8"));

        layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        buildHeader();
        buildStatsSection();

        // RecyclerView
        recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setPadding(dp(16), dp(8), dp(16), dp(16));
        recyclerView.setClipToPadding(false);

        adapter = new UserBookingsAdapter(bookingsList);
        recyclerView.setAdapter(adapter);

        layout.addView(recyclerView);
        scroll.addView(layout);

        ViewCompat.setOnApplyWindowInsetsListener(layout, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            layout.setPadding(0, top, 0, bottom);
            return insets;
        });

        loadUserBookings();

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

        TextView title = new TextView(getContext());
        title.setText("حجوزاتي");
        title.setTextSize(18);
        title.setTypeface(ThemeManager.fontBold());
        title.setTextColor(Color.parseColor("#4B463D"));
        title.setTranslationY(-dpf(1.5f));

        header.addView(backBtn);
        header.addView(title);
        header.setElevation(dp(4));

        layout.addView(header);
    }

    private void buildStatsSection() {
        statsContainer = new LinearLayout(getContext());
        statsContainer.setOrientation(LinearLayout.VERTICAL);
        statsContainer.setPadding(dp(16), dp(16), dp(16), dp(8));
        statsContainer.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        containerParams.bottomMargin = dp(8);
        statsContainer.setLayoutParams(containerParams);

        TextView statsTitle = new TextView(getContext());
        statsTitle.setText("📊 إحصائيات الحجوزات");
        statsTitle.setTextSize(16);
        statsTitle.setTypeface(ThemeManager.fontBold());
        statsTitle.setTextColor(Color.parseColor("#4B463D"));
        statsTitle.setTranslationY(-dpf(1.5f));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        titleParams.bottomMargin = dp(12);
        statsTitle.setLayoutParams(titleParams);
        statsContainer.addView(statsTitle);

        // Stats cards container
        LinearLayout statsCardsRow = new LinearLayout(getContext());
        statsCardsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsCardsRow.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        statsContainer.addView(statsCardsRow);

        layout.addView(statsContainer);
    }

    private void updateStatsUI() {
        LinearLayout statsCardsRow = (LinearLayout) statsContainer.getChildAt(1);
        statsCardsRow.removeAllViews();

        statsCardsRow.addView(createStatCard("🟢 نشط", activeBookingsCount, "#4CAF50"));
        statsCardsRow.addView(createStatCard("✅ مكتمل", completedBookingsCount, "#2196F3"));
        statsCardsRow.addView(createStatCard("❌ ملغي", cancelledBookingsCount, "#F44336"));

        // Total spent card (full width)
        LinearLayout totalSpentCard = createTotalSpentCard();
        LinearLayout.LayoutParams spentParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        spentParams.topMargin = dp(8);
        totalSpentCard.setLayoutParams(spentParams);
        statsContainer.addView(totalSpentCard);
    }

    private LinearLayout createStatCard(String label, int value, String color) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        params.setMarginEnd(dp(8));
        card.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(Color.parseColor(color + "15"));
        bg.setStroke(dp(2), Color.parseColor(color + "30"));
        card.setBackground(bg);

        TextView valueText = new TextView(getContext());
        valueText.setText(String.valueOf(value));
        valueText.setTextSize(24);
        valueText.setTypeface(ThemeManager.fontBold());
        valueText.setTextColor(Color.parseColor(color));
        valueText.setTranslationY(-dpf(2f));

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(12);
        labelText.setTypeface(ThemeManager.fontSemiBold());
        labelText.setTextColor(Color.parseColor("#804B463D"));
        labelText.setTranslationY(-dpf(1f));

        card.addView(valueText);
        card.addView(labelText);

        return card;
    }

    private LinearLayout createTotalSpentCard() {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(Color.parseColor("#FFF3E0"));
        bg.setStroke(dp(2), Color.parseColor("#FFB74D"));
        card.setBackground(bg);

        TextView icon = new TextView(getContext());
        icon.setText("💰");
        icon.setTextSize(24);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        iconParams.setMarginEnd(dp(12));
        icon.setLayoutParams(iconParams);

        LinearLayout textContainer = new LinearLayout(getContext());
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

        TextView label = new TextView(getContext());
        label.setText("إجمالي المصروفات");
        label.setTextSize(13);
        label.setTypeface(ThemeManager.fontSemiBold());
        label.setTextColor(Color.parseColor("#804B463D"));
        label.setTranslationY(-dpf(1f));

        TextView value = new TextView(getContext());
        value.setText(String.format(Locale.getDefault(), "%.2f ج.م", totalSpent));
        value.setTextSize(18);
        value.setTypeface(ThemeManager.fontBold());
        value.setTextColor(Color.parseColor("#E65100"));
        value.setTranslationY(-dpf(1.5f));

        textContainer.addView(label);
        textContainer.addView(value);

        card.addView(icon);
        card.addView(textContainer);

        return card;
    }

    private void loadUserBookings() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "يجب تسجيل الدخول", Toast.LENGTH_SHORT).show();
            return;
        }

        showSkeletonLoading();

        // Reset statistics
        activeBookingsCount = 0;
        completedBookingsCount = 0;
        cancelledBookingsCount = 0;
        totalSpent = 0;

        DatabaseReference userBookingsRef = realtimeDB.child("bookings").child(user.getUid());

        userBookingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clearSkeletons();

                if (!snapshot.exists()) {
                    showEmptyState();
                    return;
                }

                for (DataSnapshot bookingSnap : snapshot.getChildren()) {
                    String bookingId = bookingSnap.getKey();
                    String bookingDocId = bookingSnap.child("bookingDocId").getValue(String.class);
                    String fieldId = bookingSnap.child("fieldId").getValue(String.class);
                    String userId = bookingSnap.child("userId").getValue(String.class);
                    String bookingName = bookingSnap.child("bookingName").getValue(String.class);
                    String category = bookingSnap.child("category").getValue(String.class);
                    Double price = bookingSnap.child("price").getValue(Double.class);
                    Long bookingDate = bookingSnap.child("bookingDate").getValue(Long.class);
                    String userActivationCode = bookingSnap.child("userActivationCode").getValue(String.class);
                    String providerActivationCode = bookingSnap.child("providerActivationCode").getValue(String.class);
                    String activationStatus = bookingSnap.child("activationStatus").getValue(String.class);

                    // Duration
                    Long startTime = null;
                    Long endTime = null;
                    if (bookingSnap.hasChild("duration") && bookingSnap.child("duration").hasChild("startEnd")) {
                        startTime = bookingSnap.child("duration").child("startEnd").child("start").getValue(Long.class);
                        endTime = bookingSnap.child("duration").child("startEnd").child("end").getValue(Long.class);
                    }

                    Long finalStartTime = startTime;
                    Long finalEndTime = endTime;

                    // Load field and booking details from Firestore
                    loadBookingDetails(bookingId, bookingDocId, fieldId, userId, bookingName,
                            category, price, bookingDate, userActivationCode,
                            providerActivationCode, activationStatus,
                            finalStartTime, finalEndTime);
                }

                updateStatsUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                clearSkeletons();
                Toast.makeText(getContext(), "خطأ في تحميل البيانات", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBookingDetails(String bookingId, String bookingDocId, String fieldId,
                                    String userId, String bookingName, String category,
                                    Double price, Long bookingDate, String userActivationCode,
                                    String providerActivationCode, String activationStatus,
                                    Long startTime, Long endTime) {

        // First get field name
        firestore.collection("fields").document(fieldId).get()
                .addOnSuccessListener(fieldDoc -> {
                    String fieldName = "ملعب غير معروف";
                    String fieldImageUrl = null;

                    if (fieldDoc.exists()) {
                        fieldName = fieldDoc.getString("name");
                        fieldImageUrl = fieldDoc.getString("imageUrl");
                    }

                    String finalFieldName = fieldName;
                    String finalFieldImageUrl = fieldImageUrl;

                    // Then get booking status from field's bookings
                    firestore.collection("fields")
                            .document(fieldId)
                            .collection("bookings")
                            .document(bookingDocId)
                            .get()
                            .addOnSuccessListener(bookingDoc -> {
                                BookingStatus status = BookingStatus.PENDING;

                                if (bookingDoc.exists()) {
                                    String statusStr = bookingDoc.getString("status");

                                    if ("booked".equals(statusStr)) {
                                        // Check if expired
                                        if (endTime != null && endTime < System.currentTimeMillis()) {
                                            status = BookingStatus.COMPLETED;
                                        } else {
                                            status = BookingStatus.ACTIVE;
                                        }
                                    } else if ("completed".equals(statusStr)) {
                                        status = BookingStatus.COMPLETED;
                                    } else if ("cancelled".equals(statusStr)) {
                                        status = BookingStatus.CANCELLED;
                                    } else if ("available".equals(statusStr)) {
                                        status = BookingStatus.CANCELLED;
                                    }
                                }

                                // Update statistics
                                if (status == BookingStatus.ACTIVE) {
                                    activeBookingsCount++;
                                    if (price != null) totalSpent += price;
                                } else if (status == BookingStatus.COMPLETED) {
                                    completedBookingsCount++;
                                    if (price != null) totalSpent += price;
                                } else if (status == BookingStatus.CANCELLED) {
                                    cancelledBookingsCount++;
                                }

                                UserBooking userBooking = new UserBooking(
                                        bookingId, bookingDocId, fieldId, userId,
                                        bookingName, category, price, bookingDate,
                                        userActivationCode, providerActivationCode,
                                        activationStatus, startTime, endTime,
                                        finalFieldName, finalFieldImageUrl, status, false
                                );

                                bookingsList.add(userBooking);
                                adapter.notifyItemInserted(bookingsList.size() - 1);
                                updateStatsUI();
                            })
                            .addOnFailureListener(e -> {
                                // Add with default status
                                UserBooking userBooking = new UserBooking(
                                        bookingId, bookingDocId, fieldId, userId,
                                        bookingName, category, price, bookingDate,
                                        userActivationCode, providerActivationCode,
                                        activationStatus, startTime, endTime,
                                        finalFieldName, finalFieldImageUrl, BookingStatus.PENDING, false
                                );

                                bookingsList.add(userBooking);
                                adapter.notifyItemInserted(bookingsList.size() - 1);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("UserBookings", "Error loading field details", e);
                });
    }

    private void showSkeletonLoading() {
        clearSkeletons();
        for (int i = 0; i < 3; i++) {
            bookingsList.add(new UserBooking(true));
            adapter.notifyItemInserted(i);
        }
    }

    private void clearSkeletons() {
        if (!bookingsList.isEmpty()) {
            int oldSize = bookingsList.size();
            bookingsList.clear();
            adapter.notifyItemRangeRemoved(0, oldSize);
        }
    }

    private void showEmptyState() {
        // Add empty state view
        LinearLayout emptyState = new LinearLayout(getContext());
        emptyState.setOrientation(LinearLayout.VERTICAL);
        emptyState.setGravity(Gravity.CENTER);
        emptyState.setPadding(dp(32), dp(64), dp(32), dp(64));

        TextView emptyIcon = new TextView(getContext());
        emptyIcon.setText("📋");
        emptyIcon.setTextSize(48);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        iconParams.bottomMargin = dp(16);
        emptyIcon.setLayoutParams(iconParams);

        TextView emptyText = new TextView(getContext());
        emptyText.setText("لا توجد حجوزات حالياً");
        emptyText.setTextSize(16);
        emptyText.setTypeface(ThemeManager.fontBold());
        emptyText.setTextColor(Color.parseColor("#804B463D"));
        emptyText.setTranslationY(-dpf(1.5f));

        emptyState.addView(emptyIcon);
        emptyState.addView(emptyText);

        layout.addView(emptyState);
    }

    private void cancelBooking(UserBooking booking) {
        // Update in Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "cancelled");
        updates.put("bookedBy", null);
        updates.put("bookedByName", null);

        firestore.collection("fields")
                .document(booking.fieldId)
                .collection("bookings")
                .document(booking.bookingDocId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Delete from user's bookings in Realtime DB
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        realtimeDB.child("bookings")
                                .child(user.getUid())
                                .child(booking.bookingId)
                                .removeValue();
                    }

                    Toast.makeText(getContext(), "تم إلغاء الحجز بنجاح", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "فشل إلغاء الحجز", Toast.LENGTH_SHORT).show();
                });
    }

    private void showActivationCodes(UserBooking booking) {
        // Create dialog to show QR codes
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());

        LinearLayout dialogLayout = new LinearLayout(getContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(dp(24), dp(24), dp(24), dp(24));

        TextView title = new TextView(getContext());
        title.setText("🔐 أكواد التفعيل");
        title.setTextSize(18);
        title.setTypeface(ThemeManager.fontBold());
        title.setTextColor(Color.parseColor("#4B463D"));
        title.setTranslationY(-dpf(1.5f));
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        titleParams.bottomMargin = dp(16);
        title.setLayoutParams(titleParams);
        dialogLayout.addView(title);

        // User QR Code
        dialogLayout.addView(createQRSection("كود المستخدم", booking.userActivationCode));

        // Provider QR Code
        dialogLayout.addView(createQRSection("كود مقدم الخدمة", booking.providerActivationCode));

        builder.setView(dialogLayout);
        builder.setPositiveButton("إغلاق", null);
        builder.show();
    }

    private LinearLayout createQRSection(String label, String code) {
        LinearLayout section = new LinearLayout(getContext());
        section.setOrientation(LinearLayout.VERTICAL);
        section.setGravity(Gravity.CENTER);
        section.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        sectionParams.bottomMargin = dp(16);
        section.setLayoutParams(sectionParams);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        bg.setColor(Color.parseColor("#F8F8F8"));
        section.setBackground(bg);

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(14);
        labelText.setTypeface(ThemeManager.fontBold());
        labelText.setTextColor(Color.parseColor("#4B463D"));
        labelText.setTranslationY(-dpf(1.5f));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        labelParams.bottomMargin = dp(12);
        labelText.setLayoutParams(labelParams);

        ImageView qrImage = new ImageView(getContext());
        qrImage.setLayoutParams(new LinearLayout.LayoutParams(dp(200), dp(200)));

        try {
            Bitmap qrBitmap = generateQRCode(code);
            qrImage.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            qrImage.setImageResource(R.drawable.bars_3);
        }

        TextView codeText = new TextView(getContext());
        codeText.setText(code.substring(0, Math.min(16, code.length())) + "...");
        codeText.setTextSize(10);
        codeText.setTypeface(ThemeManager.fontMedium());
        codeText.setTextColor(Color.parseColor("#804B463D"));
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        codeParams.topMargin = dp(8);
        codeText.setLayoutParams(codeParams);

        section.addView(labelText);
        section.addView(qrImage);
        section.addView(codeText);

        return section;
    }

    private Bitmap generateQRCode(String data) throws Exception {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }

    private int dp(int value) {
        return (int) (value * getContext().getResources().getDisplayMetrics().density);
    }

    private float dpf(float value) {
        return value * getContext().getResources().getDisplayMetrics().density;
    }

    // ============================
    // UserBooking Model
    // ============================
    public static class UserBooking {
        public String bookingId;
        public String bookingDocId;
        public String fieldId;
        public String userId;
        public String bookingName;
        public String category;
        public Double price;
        public Long bookingDate;
        public String userActivationCode;
        public String providerActivationCode;
        public String activationStatus;
        public Long startTime;
        public Long endTime;
        public String fieldName;
        public String fieldImageUrl;
        public BookingStatus status;
        public boolean isSkeleton;

        public UserBooking(boolean isSkeleton) {
            this.isSkeleton = isSkeleton;
        }

        public UserBooking(String bookingId, String bookingDocId, String fieldId, String userId,
                           String bookingName, String category, Double price, Long bookingDate,
                           String userActivationCode, String providerActivationCode,
                           String activationStatus, Long startTime, Long endTime,
                           String fieldName, String fieldImageUrl, BookingStatus status,
                           boolean isSkeleton) {
            this.bookingId = bookingId;
            this.bookingDocId = bookingDocId;
            this.fieldId = fieldId;
            this.userId = userId;
            this.bookingName = bookingName;
            this.category = category;
            this.price = price;
            this.bookingDate = bookingDate;
            this.userActivationCode = userActivationCode;
            this.providerActivationCode = providerActivationCode;
            this.activationStatus = activationStatus;
            this.startTime = startTime;
            this.endTime = endTime;
            this.fieldName = fieldName;
            this.fieldImageUrl = fieldImageUrl;
            this.status = status;
            this.isSkeleton = isSkeleton;
        }
    }

    // ============================
    // RecyclerView Adapter
    // ============================
    private class UserBookingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_SKELETON = 0;
        private final int VIEW_TYPE_BOOKING = 1;

        private List<UserBooking> items;

        public UserBookingsAdapter(List<UserBooking> items) {
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).isSkeleton ? VIEW_TYPE_SKELETON : VIEW_TYPE_BOOKING;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_SKELETON) {
                return new SkeletonViewHolder(createSkeletonView());
            } else {
                return new BookingViewHolder(createBookingCard());
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof BookingViewHolder) {
                ((BookingViewHolder) holder).bind(items.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private View createSkeletonView() {
            CardView card = new CardView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, dp(180));
            params.setMargins(0, dp(4), 0, dp(4));
            card.setLayoutParams(params);
            card.setRadius(dp(16));

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(16));
            bg.setColor(Color.parseColor("#F0F0F0"));
            card.setBackground(bg);

            return card;
        }

        private CardView createBookingCard() {
            CardView card = new CardView(getContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            cardParams.setMargins(0, dp(5), 0, dp(5));
            card.setLayoutParams(cardParams);
            card.setRadius(dp(16));
            card.setCardElevation(0);

            return card;
        }

        class SkeletonViewHolder extends RecyclerView.ViewHolder {
            public SkeletonViewHolder(@NonNull View itemView) {
                super(itemView);

                ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(itemView, "alpha", 0.5f, 1f, 0.5f);
                fadeAnim.setDuration(1200);
                fadeAnim.setRepeatCount(ValueAnimator.INFINITE);
                fadeAnim.setRepeatMode(ValueAnimator.REVERSE);
                fadeAnim.start();
            }
        }

        class BookingViewHolder extends RecyclerView.ViewHolder {
            LinearLayout statusBadge;
            TextView bookingNameText, fieldNameText, categoryText, priceText;
            TextView bookingDateText, durationText;
            LinearLayout actionsContainer;

            public BookingViewHolder(@NonNull View itemView) {
                super(itemView);

                CardView card = (CardView) itemView;

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(16));
                bg.setStroke(dp(2), Color.parseColor("#EFEDE9"));
                bg.setColor(Color.WHITE);
                card.setBackground(bg);

                LinearLayout content = new LinearLayout(getContext());
                content.setOrientation(LinearLayout.VERTICAL);
                content.setPadding(dp(16), dp(16), dp(16), dp(16));

                // Header Row
                LinearLayout headerRow = new LinearLayout(getContext());
                headerRow.setOrientation(LinearLayout.HORIZONTAL);
                headerRow.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                headerParams.bottomMargin = dp(4);
                headerRow.setLayoutParams(headerParams);

                bookingNameText = new TextView(getContext());
                bookingNameText.setTextSize(17);
                bookingNameText.setTypeface(ThemeManager.fontBold());
                bookingNameText.setTextColor(Color.parseColor("#4B463D"));
                bookingNameText.setTranslationY(-dpf(1.5f));
                bookingNameText.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

                statusBadge = new LinearLayout(getContext());
                headerRow.addView(bookingNameText);
                headerRow.addView(statusBadge);

                content.addView(headerRow);

                // Field name
                fieldNameText = new TextView(getContext());
                fieldNameText.setTextSize(14);
                fieldNameText.setTypeface(ThemeManager.fontSemiBold());
                fieldNameText.setTextColor(Color.parseColor("#804B463D"));
                fieldNameText.setTranslationY(-dpf(1f));
                LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                fieldParams.bottomMargin = dp(12);
                fieldNameText.setLayoutParams(fieldParams);
                content.addView(fieldNameText);

                // Details Box
                LinearLayout detailsBox = new LinearLayout(getContext());
                detailsBox.setOrientation(LinearLayout.VERTICAL);
                detailsBox.setPadding(dp(12), dp(12), dp(12), dp(12));
                GradientDrawable detailsBg = new GradientDrawable();
                detailsBg.setCornerRadius(dp(12));
                detailsBg.setColor(Color.parseColor("#F8F8F8"));
                detailsBox.setBackground(detailsBg);
                LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                detailsParams.bottomMargin = dp(12);
                detailsBox.setLayoutParams(detailsParams);

                categoryText = new TextView(getContext());
                priceText = new TextView(getContext());
                bookingDateText = new TextView(getContext());
                durationText = new TextView(getContext());

                content.addView(detailsBox);

                // Actions Container
                actionsContainer = new LinearLayout(getContext());
                actionsContainer.setOrientation(LinearLayout.HORIZONTAL);
                actionsContainer.setGravity(Gravity.CENTER);
                actionsContainer.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

                content.addView(actionsContainer);

                card.addView(content);
            }

            public void bind(UserBooking booking) {
                bookingNameText.setText(booking.bookingName != null ? booking.bookingName : "حجز");
                fieldNameText.setText("🏟️ " + (booking.fieldName != null ? booking.fieldName : "ملعب"));

                // Status badge
                statusBadge.removeAllViews();
                statusBadge.addView(createStatusBadge(booking.status));

                // Details
                LinearLayout detailsBox = (LinearLayout) ((LinearLayout) itemView).getChildAt(0);
                detailsBox = (LinearLayout) detailsBox.getChildAt(2);
                detailsBox.removeAllViews();

                if (booking.category != null && !booking.category.isEmpty()) {
                    detailsBox.addView(createDetailRow("📋 الفئة", booking.category));
                }

                if (booking.price != null) {
                    detailsBox.addView(createDetailRow("💰 السعر",
                            String.format(Locale.getDefault(), "%.0f ج.م", booking.price)));
                }

                if (booking.bookingDate != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy, hh:mm a", new Locale("ar"));
                    detailsBox.addView(createDetailRow("📅 تاريخ الحجز",
                            sdf.format(new Date(booking.bookingDate))));
                }

                if (booking.startTime != null && booking.endTime != null) {
                    SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a", new Locale("ar"));
                    String duration = timeSdf.format(new Date(booking.startTime)) + " - " +
                            timeSdf.format(new Date(booking.endTime));
                    detailsBox.addView(createDetailRow("⏱️ المدة", duration));
                }

                // Actions
                actionsContainer.removeAllViews();

                if (booking.status == BookingStatus.ACTIVE) {
                    // Show QR code button
                    actionsContainer.addView(createActionButton("🔐 عرض الأكواد", "#4CAF50",
                            v -> showActivationCodes(booking)));

                    // Cancel button
                    actionsContainer.addView(createActionButton("❌ إلغاء", "#F44336",
                            v -> {
                                new android.app.AlertDialog.Builder(getContext())
                                        .setTitle("تأكيد الإلغاء")
                                        .setMessage("هل تريد إلغاء هذا الحجز؟")
                                        .setPositiveButton("نعم", (dialog, which) -> cancelBooking(booking))
                                        .setNegativeButton("لا", null)
                                        .show();
                            }));
                } else if (booking.status == BookingStatus.COMPLETED) {
                    actionsContainer.addView(createInfoButton("✅ تم إكمال الحجز", "#2196F3"));
                } else if (booking.status == BookingStatus.CANCELLED) {
                    actionsContainer.addView(createInfoButton("❌ تم إلغاء الحجز", "#757575"));
                } else {
                    actionsContainer.addView(createInfoButton("⏳ قيد الانتظار", "#FF9800"));
                }
            }

            private LinearLayout createStatusBadge(BookingStatus status) {
                LinearLayout badge = new LinearLayout(getContext());
                badge.setOrientation(LinearLayout.HORIZONTAL);
                badge.setGravity(Gravity.CENTER);
                badge.setPadding(dp(10), dp(5), dp(10), dp(5));

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(16));

                String text;
                String bgColor;
                String textColor;

                switch (status) {
                    case ACTIVE:
                        text = "🟢 نشط";
                        bgColor = "#E8F5E9";
                        textColor = "#2E7D32";
                        break;
                    case COMPLETED:
                        text = "✅ مكتمل";
                        bgColor = "#E3F2FD";
                        textColor = "#1565C0";
                        break;
                    case CANCELLED:
                        text = "❌ ملغي";
                        bgColor = "#FFEBEE";
                        textColor = "#C62828";
                        break;
                    case PENDING:
                    default:
                        text = "⏳ انتظار";
                        bgColor = "#FFF3E0";
                        textColor = "#E65100";
                }

                bg.setColor(Color.parseColor(bgColor));
                badge.setBackground(bg);

                TextView badgeText = new TextView(getContext());
                badgeText.setText(text);
                badgeText.setTextSize(12);
                badgeText.setTypeface(ThemeManager.fontBold());
                badgeText.setTextColor(Color.parseColor(textColor));
                badgeText.setTranslationY(-dpf(1f));

                badge.addView(badgeText);
                return badge;
            }

            private LinearLayout createDetailRow(String label, String value) {
                LinearLayout row = new LinearLayout(getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, dp(4), 0, dp(4));

                TextView labelText = new TextView(getContext());
                labelText.setText(label);
                labelText.setTextSize(13);
                labelText.setTypeface(ThemeManager.fontSemiBold());
                labelText.setTextColor(Color.parseColor("#804B463D"));
                labelText.setTranslationY(-dpf(1f));
                labelText.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

                TextView valueText = new TextView(getContext());
                valueText.setText(value);
                valueText.setTextSize(13);
                valueText.setTypeface(ThemeManager.fontBold());
                valueText.setTextColor(Color.parseColor("#4B463D"));
                valueText.setGravity(Gravity.END);
                valueText.setTranslationY(-dpf(1f));
                valueText.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

                row.addView(labelText);
                row.addView(valueText);

                return row;
            }

            private LinearLayout createActionButton(String text, String color, View.OnClickListener listener) {
                LinearLayout btn = new LinearLayout(getContext());
                btn.setOrientation(LinearLayout.HORIZONTAL);
                btn.setGravity(Gravity.CENTER);
                btn.setPadding(dp(16), dp(10), dp(16), dp(10));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
                params.setMarginEnd(dp(8));
                btn.setLayoutParams(params);

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(12));
                bg.setColor(Color.parseColor(color));
                btn.setBackground(bg);

                TextView btnText = new TextView(getContext());
                btnText.setText(text);
                btnText.setTextSize(13);
                btnText.setTypeface(ThemeManager.fontBold());
                btnText.setTextColor(Color.WHITE);
                btnText.setTranslationY(-dpf(1f));

                btn.addView(btnText);
                btn.setOnClickListener(listener);

                return btn;
            }

            private LinearLayout createInfoButton(String text, String color) {
                LinearLayout btn = new LinearLayout(getContext());
                btn.setOrientation(LinearLayout.HORIZONTAL);
                btn.setGravity(Gravity.CENTER);
                btn.setPadding(dp(16), dp(10), dp(16), dp(10));
                btn.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(12));
                bg.setColor(Color.parseColor(color + "20"));
                bg.setStroke(dp(2), Color.parseColor(color));
                btn.setBackground(bg);

                TextView btnText = new TextView(getContext());
                btnText.setText(text);
                btnText.setTextSize(13);
                btnText.setTypeface(ThemeManager.fontBold());
                btnText.setTextColor(Color.parseColor(color));
                btnText.setTranslationY(-dpf(1f));

                btn.addView(btnText);

                return btn;
            }
        }
    }

    enum BookingStatus {
        PENDING,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }
}