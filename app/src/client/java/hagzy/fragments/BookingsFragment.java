package hagzy.fragments;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import hagzy.FieldActivity;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.R;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BookingsFragment extends Fragment {
    private String fieldId;
    private DatabaseReference realtimeDB;
    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private BookingsAdapter adapter;
    private List<BookingItem> bookingsList = new ArrayList<>();
    private LinearLayout layout;

    public static BookingsFragment newInstance(String fieldId) {
        BookingsFragment fragment = new BookingsFragment();
        Bundle args = new Bundle();
        args.putString("newfieldId", fieldId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup containerParent, Bundle savedInstanceState) {
        if (getArguments() != null) {
            fieldId = getArguments().getString("newfieldId");
        }

        ThemeManager.setDarkMode(getContext(), false);

        realtimeDB = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        ScrollView scroll = new ScrollView(getContext());
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.WHITE);

        layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        buildHeader();

        // RecyclerView
        recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setPadding(dp(16), dp(8), dp(16), dp(16));

        adapter = new BookingsAdapter(bookingsList);
        recyclerView.setAdapter(adapter);

        layout.addView(recyclerView);
        scroll.addView(layout);

        ViewCompat.setOnApplyWindowInsetsListener(layout, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            layout.setPadding(0, top, 0, bottom);
            return insets;
        });

        loadData();

        return scroll;
    }

    public void loadData() {
        if (fieldId == null) return;

        showSkeletonLoading();

        // جلب الحجوزات من Realtime Database
        DatabaseReference bookingsRef = realtimeDB.child("fields").child(fieldId).child("bookings");

        bookingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // حذف skeleton
                int skeletonCount = bookingsList.size();
                bookingsList.clear();
                adapter.notifyItemRangeRemoved(0, skeletonCount);

                if (!snapshot.exists()) {
                    // لا يوجد حجوزات
                    return;
                }

                for (DataSnapshot bookingSnap : snapshot.getChildren()) {
                    Boolean available = bookingSnap.child("available").getValue(Boolean.class);
                    if (available == null || !available) continue;

                    String bookingDocId = bookingSnap.getKey();
                    String name = bookingSnap.child("name").getValue(String.class);
                    String category = bookingSnap.child("category").getValue(String.class);
                    Double price = bookingSnap.child("price").getValue(Double.class);
                    Long quantity = bookingSnap.child("quantity").getValue(Long.class);
                    String description = bookingSnap.child("description").getValue(String.class);

                    // جلب تفاصيل إضافية من Firestore
                    loadBookingDetailsFromFirestore(bookingDocId, name, category, price, quantity, description);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RealtimeDB", "خطأ أثناء جلب البيانات: " + error.getMessage());
                int skeletonCount = bookingsList.size();
                bookingsList.clear();
                adapter.notifyItemRangeRemoved(0, skeletonCount);
            }
        });
    }

    private void loadBookingDetailsFromFirestore(String bookingDocId, String name, String category,
                                                 Double price, Long quantity, String description) {
        firestore.collection("fields")
                .document(fieldId)
                .collection("bookings")
                .document(bookingDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    BookingStatus status = BookingStatus.AVAILABLE;
                    String bookedBy = null;
                    String bookedByName = null;
                    Date startTime = null;
                    Date endTime = null;
                    String imageUrl = null;

                    if (doc.exists()) {
                        String statusStr = doc.getString("status");
                        bookedBy = doc.getString("bookedBy");
                        bookedByName = doc.getString("bookedByName");
                        imageUrl = doc.getString("imageUrl");

                        com.google.firebase.Timestamp startTS = doc.getTimestamp("startTime");
                        com.google.firebase.Timestamp endTS = doc.getTimestamp("endTime");

                        if (startTS != null) startTime = startTS.toDate();
                        if (endTS != null) endTime = endTS.toDate();

                        if ("booked".equals(statusStr)) {
                            // التحقق من انتهاء الوقت
                            if (endTime != null && endTime.before(new Date())) {
                                status = BookingStatus.COMPLETED;
                                releaseBooking(bookingDocId);
                            } else {
                                status = BookingStatus.BOOKED;
                            }
                        } else if ("completed".equals(statusStr)) {
                            status = BookingStatus.COMPLETED;
                        }
                    }

                    // إضافة الحجز للقائمة
                    BookingItem item = new BookingItem(
                            bookingDocId, name, category, price, quantity,
                            description, status, bookedBy, bookedByName,
                            startTime, endTime, imageUrl, false
                    );

                    bookingsList.add(item);
                    adapter.notifyItemInserted(bookingsList.size() - 1);
                })
                .addOnFailureListener(e -> {
                    // إذا فشل جلب من Firestore، أضف البيانات الأساسية
                    BookingItem item = new BookingItem(
                            bookingDocId, name, category, price, quantity,
                            description, BookingStatus.AVAILABLE, null, null,
                            null, null, null, false
                    );

                    bookingsList.add(item);
                    adapter.notifyItemInserted(bookingsList.size() - 1);
                });
    }

    private void showSkeletonLoading() {
        if (!bookingsList.isEmpty()) {
            int oldSize = bookingsList.size();
            bookingsList.clear();
            adapter.notifyItemRangeRemoved(0, oldSize);
        }

        for (int i = 0; i < 3; i++) {
            bookingsList.add(new BookingItem(true));
            adapter.notifyItemInserted(i);
        }
    }

    private void performBooking(String bookingDocId, String name, String category,
                                Double price, Long quantity) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "يجب تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        String bookingId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        long endTime = now + (2 * 60 * 60 * 1000); // 2 hours

        // Generate activation codes
        String userActivationCode = generateActivationCode(bookingId, user.getUid(), "user");
        String providerActivationCode = generateActivationCode(bookingId, user.getUid(), "provider");

        Map<String, Object> firestoreBookingData = new HashMap<>();
        firestoreBookingData.put("status", "booked");
        firestoreBookingData.put("bookedBy", user.getUid());
        firestoreBookingData.put("bookedByName", user.getDisplayName() != null ?
                user.getDisplayName() : "مستخدم");
        firestoreBookingData.put("startTime", new com.google.firebase.Timestamp(new Date(now)));
        firestoreBookingData.put("endTime", new com.google.firebase.Timestamp(new Date(endTime)));
        firestoreBookingData.put("bookingDate", com.google.firebase.firestore.FieldValue.serverTimestamp());

        // Update in Firestore - حالة الحجز في الملعب
        firestore.collection("fields")
                .document(fieldId)
                .collection("bookings")
                .document(bookingDocId)
                .set(firestoreBookingData)
                .addOnSuccessListener(aVoid -> {
                    // Create booking record in user's bookings (Realtime DB)
                    Map<String, Object> userBooking = new HashMap<>();
                    userBooking.put("bookingId", bookingId);
                    userBooking.put("bookingDocId", bookingDocId);
                    userBooking.put("fieldId", fieldId);
                    userBooking.put("userId", user.getUid());
                    userBooking.put("bookingName", name);
                    userBooking.put("category", category);
                    userBooking.put("price", price);
                    userBooking.put("bookingDate", ServerValue.TIMESTAMP);
                    userBooking.put("userActivationCode", userActivationCode);
                    userBooking.put("providerActivationCode", providerActivationCode);
                    userBooking.put("activationStatus", "pending");

                    realtimeDB.child("bookings")
                            .child(user.getUid())
                            .child(bookingId)
                            .setValue(userBooking);

                    // Create duration info
                    Map<String, Object> durationData = new HashMap<>();
                    Map<String, Object> startEnd = new HashMap<>();
                    startEnd.put("start", now);
                    startEnd.put("end", endTime);
                    durationData.put("startEnd", startEnd);

                    realtimeDB.child("bookings")
                            .child(user.getUid())
                            .child(bookingId)
                            .child("duration")
                            .setValue(durationData);

                    // Send notification
                    sendBookingNotification(user.getUid(), bookingId, name, "booking_confirmed");

                    Toast.makeText(getContext(), "✅ تم الحجز بنجاح!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ فشل الحجز: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e("Booking", "Error", e);
                });
    }

    private void cancelBooking(String bookingDocId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "available");
        updates.put("bookedBy", null);
        updates.put("bookedByName", null);
        updates.put("startTime", null);
        updates.put("endTime", null);

        firestore.collection("fields")
                .document(fieldId)
                .collection("bookings")
                .document(bookingDocId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "تم إلغاء الحجز", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "فشل إلغاء الحجز", Toast.LENGTH_SHORT).show();
                });
    }

    private void releaseBooking(String bookingDocId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "completed");
        updates.put("bookedBy", null);
        updates.put("bookedByName", null);

        firestore.collection("fields")
                .document(fieldId)
                .collection("bookings")
                .document(bookingDocId)
                .update(updates);
    }

    private void sendBookingNotification(String userId, String bookingId,
                                         String bookingName, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("fieldId", fieldId);
        notification.put("bookingId", bookingId);
        notification.put("title", "تم تأكيد حجزك");
        notification.put("message", "تم تأكيد حجز " + bookingName + " بنجاح");
        notification.put("type", type);
        notification.put("isRead", false);
        notification.put("timestamp", ServerValue.TIMESTAMP);

        String notificationId = realtimeDB.child("inbox").push().getKey();
        if (notificationId != null) {
            realtimeDB.child("inbox")
                    .child(notificationId)
                    .setValue(notification);
        }
    }

    private String generateActivationCode(String bookingId, String userId, String salt) {
        try {
            String data = bookingId + userId + salt + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
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
            if (getContext() instanceof FieldActivity) {
                ((FieldActivity) getContext()).onBackPressed();
            }
        });

        TextView title = new TextView(getContext());
        title.setText("الحجوزات المتاحة");
        title.setTextSize(18);
        title.setTypeface(ThemeManager.fontBold());
        title.setTextColor(Color.parseColor("#4B463D"));
        title.setTranslationY(-dpf(1.5f));

        header.addView(backBtn);
        header.addView(title);

        header.setElevation(dp(4));

        layout.addView(header);
    }

    private int dp(int value) {
        return (int) (value * getContext().getResources().getDisplayMetrics().density);
    }

    private float dpf(float value) {
        return value * getContext().getResources().getDisplayMetrics().density;
    }

    // ============================
    // BookingItem Model
    // ============================
    public static class BookingItem {
        public String bookingDocId;
        public String name;
        public String category;
        public Double price;
        public Long quantity;
        public String description;
        public BookingStatus status;
        public String bookedBy;
        public String bookedByName;
        public Date startTime;
        public Date endTime;
        public String imageUrl;
        public boolean isSkeleton;

        public BookingItem(boolean isSkeleton) {
            this.isSkeleton = isSkeleton;
        }

        public BookingItem(String bookingDocId, String name, String category,
                           Double price, Long quantity, String description,
                           BookingStatus status, String bookedBy, String bookedByName,
                           Date startTime, Date endTime, String imageUrl, boolean isSkeleton) {
            this.bookingDocId = bookingDocId;
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.description = description;
            this.status = status;
            this.bookedBy = bookedBy;
            this.bookedByName = bookedByName;
            this.startTime = startTime;
            this.endTime = endTime;
            this.imageUrl = imageUrl;
            this.isSkeleton = isSkeleton;
        }
    }

    // ============================
    // RecyclerView Adapter
    // ============================
    private class BookingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_SKELETON = 0;
        private final int VIEW_TYPE_BOOKING = 1;

        private List<BookingItem> items;

        public BookingsAdapter(List<BookingItem> items) {
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
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(16));
            bg.setColor(Color.parseColor("#F0F0F0"));
            card.setBackground(bg);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, dp(150));
            params.setMargins(0, dp(4), 0, dp(4));
            card.setLayoutParams(params);

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
            TextView nameText, categoryText, priceText, quantityText, descText;
            TextView bookedByText, timeText;
            LinearLayout bookingInfoBox, actionBtn, detailsBox;
            LinearLayout statusBadge;

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

                // Header
                LinearLayout headerRow = new LinearLayout(getContext());
                headerRow.setOrientation(LinearLayout.HORIZONTAL);
                headerRow.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                headerParams.bottomMargin = dp(12);
                headerRow.setLayoutParams(headerParams);

                nameText = new TextView(getContext());
                nameText.setTextSize(17);
                nameText.setTypeface(ThemeManager.fontBold());
                nameText.setTextColor(Color.parseColor("#4B463D"));
                nameText.setTranslationY(-dpf(1.5f));
                nameText.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

                statusBadge = new LinearLayout(getContext());
                headerRow.addView(nameText);
                headerRow.addView(statusBadge);

                content.addView(headerRow);

                // Category
                categoryText = new TextView(getContext());
                categoryText.setTextSize(14);
                categoryText.setTypeface(ThemeManager.fontSemiBold());
                categoryText.setTextColor(Color.parseColor("#804B463D"));
                categoryText.setTranslationY(-dpf(1.5f));
                LinearLayout.LayoutParams catParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                catParams.bottomMargin = dp(8);
                categoryText.setLayoutParams(catParams);
                content.addView(categoryText);

                // Description
                descText = new TextView(getContext());
                descText.setTextSize(13);
                descText.setTypeface(ThemeManager.fontSemiBold());
                descText.setTextColor(Color.parseColor("#60000000"));
                descText.setTranslationY(-dpf(1f));
                descText.setMaxLines(2);
                LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                descParams.bottomMargin = dp(12);
                descText.setLayoutParams(descParams);
                content.addView(descText);

                // Details box
                detailsBox = new LinearLayout(getContext());
                detailsBox.setOrientation(LinearLayout.VERTICAL);
                detailsBox.setPadding(dp(12), dp(12), dp(12), dp(12));
                GradientDrawable detailsBg = new GradientDrawable();
                detailsBg.setCornerRadius(dp(12));
                detailsBg.setColor(Color.parseColor("#F8F8F8"));
                detailsBox.setBackground(detailsBg);
                LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                detailsParams.bottomMargin = dp(12);
                detailsBox.setLayoutParams(detailsParams);

                priceText = new TextView(getContext());
                quantityText = new TextView(getContext());

                content.addView(detailsBox);

                // Booking info box
                bookingInfoBox = new LinearLayout(getContext());
                bookingInfoBox.setOrientation(LinearLayout.VERTICAL);
                bookingInfoBox.setPadding(dp(12), dp(12), dp(12), dp(12));
                LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                infoParams.bottomMargin = dp(12);
                bookingInfoBox.setLayoutParams(infoParams);

                bookedByText = new TextView(getContext());
                timeText = new TextView(getContext());

                content.addView(bookingInfoBox);

                // Action button
                actionBtn = new LinearLayout(getContext());
                actionBtn.setOrientation(LinearLayout.HORIZONTAL);
                actionBtn.setGravity(Gravity.CENTER);
                actionBtn.setPadding(0, dp(12), 0, dp(12));

                content.addView(actionBtn);

                card.addView(content);
            }

            public void bind(BookingItem item) {
                nameText.setText(item.name != null ? item.name : "حجز");

                // Status badge
                statusBadge.removeAllViews();
                statusBadge.addView(createStatusBadge(item.status));

                // Category
                if (item.category != null && !item.category.isEmpty()) {
                    categoryText.setVisibility(View.VISIBLE);
                    categoryText.setText("📋 " + item.category);
                } else {
                    categoryText.setVisibility(View.GONE);
                }

                // Description
                if (item.description != null && !item.description.isEmpty()) {
                    descText.setVisibility(View.VISIBLE);
                    descText.setText(item.description);
                } else {
                    descText.setVisibility(View.GONE);
                }

                // Details
                detailsBox.removeAllViews();
                detailsBox.addView(createDetailRow("💰 السعر",
                        String.format(Locale.getDefault(), "%.0f ج.م", item.price != null ? item.price : 0)));
                detailsBox.addView(createDetailRow("📦 المتاح",
                        String.valueOf(item.quantity != null ? item.quantity : 0)));

                // Booking info
                if (item.status == BookingStatus.BOOKED) {
                    bookingInfoBox.setVisibility(View.VISIBLE);
                    bookingInfoBox.removeAllViews();

                    GradientDrawable infoBg = new GradientDrawable();
                    infoBg.setCornerRadius(dp(12));
                    infoBg.setColor(Color.parseColor("#FFF8E1"));
                    infoBg.setStroke(dp(1), Color.parseColor("#FFD54F"));
                    bookingInfoBox.setBackground(infoBg);

                    TextView title = new TextView(getContext());
                    title.setText("📌 معلومات الحجز");
                    title.setTextSize(13);
                    title.setTypeface(ThemeManager.fontBold());
                    title.setTextColor(Color.parseColor("#F57F17"));
                    title.setTranslationY(-dpf(1.5f));
                    LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                    titleParams.bottomMargin = dp(8);
                    title.setLayoutParams(titleParams);
                    bookingInfoBox.addView(title);

                    if (item.bookedByName != null) {
                        TextView bookedText = new TextView(getContext());
                        bookedText.setText("👤 محجوز بواسطة: " + item.bookedByName);
                        bookedText.setTextSize(12);
                        bookedText.setTypeface(ThemeManager.fontSemiBold());
                        bookedText.setTextColor(Color.parseColor("#804B463D"));
                        bookedText.setTranslationY(-dpf(1f));
                        bookingInfoBox.addView(bookedText);
                    }

                    if (item.startTime != null && item.endTime != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("d MMM, hh:mm a", new Locale("ar"));
                        TextView timeText = new TextView(getContext());
                        timeText.setText("🕐 من: " + sdf.format(item.startTime) + "\n🕐 إلى: " + sdf.format(item.endTime));
                        timeText.setTextSize(12);
                        timeText.setTypeface(ThemeManager.fontSemiBold());
                        timeText.setTextColor(Color.parseColor("#804B463D"));
                        timeText.setTranslationY(-dpf(1f));
                        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                        timeParams.topMargin = dp(4);
                        timeText.setLayoutParams(timeParams);
                        bookingInfoBox.addView(timeText);
                    }
                } else {
                    bookingInfoBox.setVisibility(View.GONE);
                }

                // Action button
                actionBtn.removeAllViews();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                boolean isOwner = currentUser != null && item.bookedBy != null &&
                        item.bookedBy.equals(currentUser.getUid());

                GradientDrawable btnBg = new GradientDrawable();
                btnBg.setCornerRadius(dp(12));

                TextView btnText = new TextView(getContext());
                btnText.setTextSize(15);
                btnText.setTypeface(ThemeManager.fontBold());
                btnText.setTranslationY(-dpf(1.5f));

                if (item.status == BookingStatus.AVAILABLE) {
                    btnBg.setColor(Color.parseColor("#4B463D"));
                    btnText.setText("احجز الآن");
                    btnText.setTextColor(Color.WHITE);
                    actionBtn.setOnClickListener(v -> performBooking(
                            item.bookingDocId, item.name, item.category,
                            item.price, item.quantity
                    ));
                } else if (item.status == BookingStatus.BOOKED && isOwner) {
                    btnBg.setColor(Color.parseColor("#F44336"));
                    btnText.setText("إلغاء الحجز");
                    btnText.setTextColor(Color.WHITE);
                    actionBtn.setOnClickListener(v -> cancelBooking(item.bookingDocId));
                } else if (item.status == BookingStatus.BOOKED) {
                    btnBg.setColor(Color.parseColor("#E0E0E0"));
                    btnText.setText("محجوز حالياً");
                    btnText.setTextColor(Color.parseColor("#757575"));
                    actionBtn.setEnabled(false);
                } else {
                    btnBg.setColor(Color.parseColor("#E0E0E0"));
                    btnText.setText("غير متاح");
                    btnText.setTextColor(Color.parseColor("#757575"));
                    actionBtn.setEnabled(false);
                }

                actionBtn.setBackground(btnBg);
                actionBtn.addView(btnText);
            }

            private LinearLayout createStatusBadge(BookingStatus status) {
                LinearLayout badge = new LinearLayout(getContext());
                badge.setOrientation(LinearLayout.HORIZONTAL);
                badge.setGravity(Gravity.CENTER);
                badge.setPadding(dp(10), dp(5), dp(10), dp(5));

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(16));

                String text;
                int bgColor;
                int textColor;

                switch (status) {
                    case AVAILABLE:
                        text = "متاح";
                        bgColor = Color.parseColor("#E8F5E9");
                        textColor = Color.parseColor("#2E7D32");
                        break;
                    case BOOKED:
                        text = "محجوز";
                        bgColor = Color.parseColor("#FFF3E0");
                        textColor = Color.parseColor("#E65100");
                        break;
                    case COMPLETED:
                        text = "مكتمل";
                        bgColor = Color.parseColor("#F5F5F5");
                        textColor = Color.parseColor("#757575");
                        break;
                    default:
                        text = "—";
                        bgColor = Color.parseColor("#F5F5F5");
                        textColor = Color.parseColor("#757575");
                }

                bg.setColor(bgColor);
                badge.setBackground(bg);

                TextView badgeText = new TextView(getContext());
                badgeText.setText(text);
                badgeText.setTextSize(12);
                badgeText.setTypeface(ThemeManager.fontBold());
                badgeText.setTextColor(textColor);
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
                labelText.setTextSize(14);
                labelText.setTypeface(ThemeManager.fontSemiBold());
                labelText.setTextColor(Color.parseColor("#804B463D"));
                labelText.setTranslationY(-dpf(1.5f));
                labelText.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

                TextView valueText = new TextView(getContext());
                valueText.setText(value);
                valueText.setTextSize(14);
                valueText.setTypeface(ThemeManager.fontBold());
                valueText.setTextColor(Color.parseColor("#4B463D"));
                valueText.setGravity(Gravity.END);
                valueText.setTranslationY(-dpf(1.5f));
                valueText.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

                row.addView(labelText);
                row.addView(valueText);

                return row;
            }
        }
    }

    enum BookingStatus {
        AVAILABLE,
        BOOKED,
        COMPLETED
    }
}