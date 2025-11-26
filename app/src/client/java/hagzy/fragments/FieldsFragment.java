package hagzy.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.bytepulse.hagzy.helpers.ThemeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hagzy.FieldActivity;

public class FieldsFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationClient;
    private double userLat = 0.0;
    private double userLng = 0.0;

    private RecyclerView recyclerView;
    private FieldsAdapter adapter;
    private FirebaseFirestore db;
    private Context context;

    private List<FieldData> allFields = new ArrayList<>();
    private String currentSortMode = "distance";
    private boolean isLoading = false;
    private DocumentSnapshot lastDocument = null;
    private static final int PAGE_SIZE = 50;

    private TextView sortDistanceBtn;
    private TextView sortRatingBtn;
    private TextView sortVerifiedBtn;

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        context = getContext();

        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#FAFAFA"));

        // شريط الفرز
        LinearLayout sortBar = createSortBar();
        rootLayout.addView(sortBar);

        // RecyclerView
        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setPadding(dp(16), dp(8), dp(16), dp(16));
        rootLayout.addView(recyclerView);

        // Adapter
        adapter = new FieldsAdapter(allFields);
        recyclerView.setAdapter(adapter);

        // Pagination Listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                if (layoutManager != null && !isLoading) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 10) {
                        loadMoreFields();
                    }
                }
            }
        });

        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1001);
            return rootLayout;
        }

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc != null) {
                        userLat = loc.getLatitude();
                        userLng = loc.getLongitude();
                    }
                    loadInitialFields();
                })
                .addOnFailureListener(e -> {
                    Log.e("LOCATION", "فشل في تحديد الموقع: " + e.getMessage());
                    loadInitialFields();
                });

        return rootLayout;
    }

    private LinearLayout createSortBar() {
        LinearLayout sortBar = new LinearLayout(context);
        sortBar.setOrientation(LinearLayout.HORIZONTAL);
        sortBar.setGravity(Gravity.CENTER);
        sortBar.setPadding(dp(16), dp(16), dp(16), dp(12));
        sortBar.setBackgroundColor(Color.WHITE);

        GradientDrawable sortBarBg = new GradientDrawable();
        sortBarBg.setColor(Color.WHITE);
        sortBarBg.setCornerRadii(new float[]{0, 0, 0, 0, dp(12), dp(12), dp(12), dp(12)});
        sortBar.setBackground(sortBarBg);

        TextView label = new TextView(context);
        label.setText("الترتيب: ");
        label.setTextSize(15);
        label.setTextColor(Color.parseColor("#4B463D"));
        label.setTypeface(ThemeManager.fontBold());
        sortBar.addView(label);

        sortDistanceBtn = createSortButton("📍 الأقرب", true);
        sortDistanceBtn.setOnClickListener(v -> changeSortMode("distance"));
        sortBar.addView(sortDistanceBtn);

        sortRatingBtn = createSortButton("⭐ الأفضل", false);
        sortRatingBtn.setOnClickListener(v -> changeSortMode("rating"));
        sortBar.addView(sortRatingBtn);

        sortVerifiedBtn = createSortButton("✓ موثق", false);
        sortVerifiedBtn.setOnClickListener(v -> changeSortMode("verified"));
        sortBar.addView(sortVerifiedBtn);

        return sortBar;
    }

    private TextView createSortButton(String text, boolean active) {
        TextView btn = new TextView(context);
        btn.setText(text);
        btn.setTextSize(13);
        btn.setPadding(dp(14), dp(8), dp(14), dp(8));
        btn.setGravity(Gravity.CENTER);
        btn.setTypeface(ThemeManager.fontMedium());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(6), 0, dp(6), 0);
        btn.setLayoutParams(params);

        updateButtonStyle(btn, active);
        return btn;
    }

    private void updateButtonStyle(TextView btn, boolean active) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(24));

        if (active) {
            bg.setColor(Color.parseColor("#4B463D"));
            btn.setTextColor(Color.WHITE);
        } else {
            bg.setColor(Color.parseColor("#F5F5F5"));
            btn.setTextColor(Color.parseColor("#666666"));
        }

        btn.setBackground(bg);
    }

    private void changeSortMode(String mode) {
        currentSortMode = mode;
        updateButtonStyle(sortDistanceBtn, mode.equals("distance"));
        updateButtonStyle(sortRatingBtn, mode.equals("rating"));
        updateButtonStyle(sortVerifiedBtn, mode.equals("verified"));
        sortAndDisplayFields();
    }

    private void sortAndDisplayFields() {
        Collections.sort(allFields, new Comparator<FieldData>() {
            @Override
            public int compare(FieldData f1, FieldData f2) {
                switch (currentSortMode) {
                    case "distance":
                        return Double.compare(f1.distance, f2.distance);
                    case "rating":
                        return Double.compare(f2.ratingAverage, f1.ratingAverage);
                    case "verified":
                        if (f1.verified != f2.verified) {
                            return f1.verified ? -1 : 1;
                        }
                        return Double.compare(f2.ratingAverage, f1.ratingAverage);
                    default:
                        return 0;
                }
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void loadInitialFields() {
        isLoading = false;
        allFields.clear();
        lastDocument = null;
        loadMoreFields();
    }

    private void loadMoreFields() {
        if (isLoading) return;
        isLoading = true;
        Log.d("FIELDS", "Loaded field: ");

        Query query = db.collection("fields")
                .whereEqualTo("available", true)
                .limit(PAGE_SIZE);

        if (lastDocument != null) {
            query = query.startAfter(lastDocument);
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        lastDocument = querySnapshot.getDocuments()
                                .get(querySnapshot.size() - 1);

                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            FieldData fieldData = parseFieldData(doc);
                            if (fieldData != null) {
                                allFields.add(fieldData);
                            }
                        }

                        sortAndDisplayFields();
                    }
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "حدث خطأ أثناء تحميل البيانات", Toast.LENGTH_SHORT).show();
                    Log.e("FIELDS", "Error loading fields", e);
                    isLoading = false;
                });
    }

    private FieldData parseFieldData(DocumentSnapshot doc) {
        try {
            FieldData fieldData = new FieldData();
            fieldData.docId = doc.getId();

            // البيانات الأساسية
            fieldData.name = doc.getString("name");
            fieldData.description = doc.getString("description");
            fieldData.category = doc.getString("category");
            fieldData.fieldType = doc.getString("fieldType");
            fieldData.verified = doc.getBoolean("verified") != null && doc.getBoolean("verified");
            fieldData.featured = doc.getBoolean("featured") != null && doc.getBoolean("featured");
            fieldData.status = doc.getString("status");
            fieldData.createdAt = doc.getTimestamp("createdAt");

            // الأسعار
            Map<String, Object> pricing = (Map<String, Object>) doc.get("pricing");
            if (pricing != null) {
                fieldData.hourlyPrice = pricing.get("hourly") != null ?
                        ((Number) pricing.get("hourly")).doubleValue() : 0;
                fieldData.currency = (String) pricing.get("currency");
            }

            // الموقع
            Map<String, Object> location = (Map<String, Object>) doc.get("location");
            if (location != null) {
                fieldData.address = (String) location.get("address");
                fieldData.city = (String) location.get("city");
                fieldData.area = (String) location.get("area");
                fieldData.lat = location.get("lat") != null ?
                        ((Number) location.get("lat")).doubleValue() : 0.0;
                fieldData.lng = location.get("lng") != null ?
                        ((Number) location.get("lng")).doubleValue() : 0.0;
            }

            // التقييم
            Map<String, Object> rating = (Map<String, Object>) doc.get("rating");
            if (rating != null) {
                fieldData.ratingAverage = rating.get("average") != null ?
                        ((Number) rating.get("average")).doubleValue() : 0.0;
                fieldData.ratingCount = rating.get("count") != null ?
                        ((Number) rating.get("count")).intValue() : 0;
            }

            // المميزات
            List<String> amenities = (List<String>) doc.get("amenities");
            if (amenities != null) {
                fieldData.amenities = amenities;
            }

            // المالك
            Map<String, Object> owner = (Map<String, Object>) doc.get("owner");
            if (owner != null) {
                fieldData.providerId = (String) owner.get("providerId");
                fieldData.ownerName = (String) owner.get("name");
            }

            // حساب المسافة
            fieldData.distance = calculateDistance(userLat, userLng, fieldData.lat, fieldData.lng);

            return fieldData;
        } catch (Exception e) {
            Log.e("FIELDS", "Error parsing field data: " + doc.getId(), e);
            return null;
        }
    }

    // RecyclerView Adapter
    private class FieldsAdapter extends RecyclerView.Adapter<FieldsAdapter.ViewHolder> {
        private List<FieldData> fields;

        FieldsAdapter(List<FieldData> fields) {
            this.fields = fields;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView card = new CardView(context);
            card.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) card.getLayoutParams();
            params.setMargins(0, dp(8), 0, dp(8));
            card.setLayoutParams(params);

            card.setCardElevation(dp(2));
            card.setRadius(dp(16));
            card.setCardBackgroundColor(Color.WHITE);
            card.setUseCompatPadding(false);

            return new ViewHolder(card);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FieldData field = fields.get(position);
            holder.bind(field);
        }

        @Override
        public int getItemCount() {
            return fields.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CardView card;
            ImageView imageView;
            TextView nameText;
            TextView verifiedBadge;
            TextView featuredBadge;
            TextView categoryText;
            TextView priceText;
            TextView distanceText;
            TextView walkTimeText;
            TextView ratingText;
            TextView addressText;
            LinearLayout amenitiesLayout;

            ViewHolder(CardView card) {
                super(card);
                this.card = card;
                setupViews();
            }

            private void setupViews() {
                LinearLayout mainLayout = new LinearLayout(context);
                mainLayout.setOrientation(LinearLayout.VERTICAL);
                mainLayout.setPadding(dp(14), dp(14), dp(14), dp(14));

                // الصف العلوي: صورة + معلومات
                LinearLayout topRow = new LinearLayout(context);
                topRow.setOrientation(LinearLayout.HORIZONTAL);
                topRow.setGravity(Gravity.TOP);

                // الصورة
                imageView = new ImageView(context);
                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dp(90), dp(90));
                imgParams.setMarginEnd(dp(14));
                imageView.setLayoutParams(imgParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                GradientDrawable imgBg = new GradientDrawable();
                imgBg.setCornerRadius(dp(12));
                imgBg.setColor(Color.parseColor("#F0F0F0"));
                imageView.setBackground(imgBg);
                imageView.setClipToOutline(true);
                topRow.addView(imageView);

                // عمود المعلومات
                LinearLayout infoColumn = new LinearLayout(context);
                infoColumn.setOrientation(LinearLayout.VERTICAL);
                infoColumn.setLayoutParams(new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                // صف الاسم + الشارات
                LinearLayout nameRow = new LinearLayout(context);
                nameRow.setOrientation(LinearLayout.HORIZONTAL);
                nameRow.setGravity(Gravity.CENTER_VERTICAL);

                nameText = new TextView(context);
                nameText.setTextSize(17);
                nameText.setTextColor(Color.parseColor("#2C2C2C"));
                nameText.setTypeface(ThemeManager.fontBold());
                nameText.setMaxLines(2);
                nameRow.addView(nameText);

                // علامة التوثيق
                verifiedBadge = new TextView(context);
                verifiedBadge.setText(" ✓");
                verifiedBadge.setTextSize(16);
                verifiedBadge.setTextColor(Color.parseColor("#4CAF50"));
                verifiedBadge.setTypeface(ThemeManager.fontBold());
                verifiedBadge.setVisibility(View.GONE);
                nameRow.addView(verifiedBadge);

                // علامة المميز
                featuredBadge = new TextView(context);
                featuredBadge.setText(" ⭐");
                featuredBadge.setTextSize(14);
                featuredBadge.setVisibility(View.GONE);
                nameRow.addView(featuredBadge);

                infoColumn.addView(nameRow);

                // الفئة ونوع الملعب
                categoryText = new TextView(context);
                categoryText.setTextSize(12);
                categoryText.setTextColor(Color.parseColor("#757575"));
                categoryText.setTypeface(ThemeManager.fontRegular());
                LinearLayout.LayoutParams catParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                catParams.setMargins(0, dp(2), 0, dp(4));
                categoryText.setLayoutParams(catParams);
                infoColumn.addView(categoryText);

                // التقييم
                ratingText = new TextView(context);
                ratingText.setTextSize(14);
                ratingText.setTextColor(Color.parseColor("#FFA726"));
                ratingText.setTypeface(ThemeManager.fontMedium());
                LinearLayout.LayoutParams ratingParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                ratingParams.setMargins(0, dp(2), 0, dp(6));
                ratingText.setLayoutParams(ratingParams);
                infoColumn.addView(ratingText);

                // السعر
                priceText = new TextView(context);
                priceText.setTextSize(16);
                priceText.setTextColor(Color.parseColor("#1976D2"));
                priceText.setTypeface(ThemeManager.fontBold());
                infoColumn.addView(priceText);

                topRow.addView(infoColumn);
                mainLayout.addView(topRow);

                // المسافة والوقت
                LinearLayout distanceRow = new LinearLayout(context);
                distanceRow.setOrientation(LinearLayout.HORIZONTAL);
                distanceRow.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams distRowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                distRowParams.setMargins(0, dp(10), 0, 0);
                distanceRow.setLayoutParams(distRowParams);

                distanceText = new TextView(context);
                distanceText.setTextSize(13);
                distanceText.setTextColor(Color.parseColor("#757575"));
                distanceText.setTypeface(ThemeManager.fontMedium());
                distanceRow.addView(distanceText);

                walkTimeText = new TextView(context);
                walkTimeText.setTextSize(13);
                walkTimeText.setTextColor(Color.parseColor("#9E9E9E"));
                walkTimeText.setTypeface(ThemeManager.fontRegular());
                distanceRow.addView(walkTimeText);

                mainLayout.addView(distanceRow);

                // العنوان
                addressText = new TextView(context);
                addressText.setTextSize(12);
                addressText.setTextColor(Color.parseColor("#9E9E9E"));
                addressText.setTypeface(ThemeManager.fontRegular());
                addressText.setMaxLines(2);
                LinearLayout.LayoutParams addressParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                addressParams.setMargins(0, dp(8), 0, 0);
                addressText.setLayoutParams(addressParams);

                GradientDrawable addressBg = new GradientDrawable();
                addressBg.setColor(Color.parseColor("#F9F9F9"));
                addressBg.setCornerRadius(dp(8));
                addressText.setBackground(addressBg);
                addressText.setPadding(dp(10), dp(6), dp(10), dp(6));
                mainLayout.addView(addressText);

                // المميزات
                amenitiesLayout = new LinearLayout(context);
                amenitiesLayout.setOrientation(LinearLayout.HORIZONTAL);
                amenitiesLayout.setGravity(Gravity.START);
                LinearLayout.LayoutParams amenitiesParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                amenitiesParams.setMargins(0, dp(8), 0, 0);
                amenitiesLayout.setLayoutParams(amenitiesParams);
                mainLayout.addView(amenitiesLayout);

                card.addView(mainLayout);
            }

            void bind(FieldData field) {
                nameText.setText(field.name != null ? field.name : "ملعب غير معروف");
                verifiedBadge.setVisibility(field.verified ? View.VISIBLE : View.GONE);
                featuredBadge.setVisibility(field.featured ? View.VISIBLE : View.GONE);

                // الفئة
                String categoryDisplay = getCategoryDisplay(field.category);
                String fieldTypeDisplay = field.fieldType != null ? field.fieldType : "";
                categoryText.setText(categoryDisplay + (fieldTypeDisplay.isEmpty() ? "" : " • " + fieldTypeDisplay));

                // السعر
                if (field.hourlyPrice > 0) {
                    priceText.setText(String.format(Locale.getDefault(), "💰 %.0f %s/ساعة",
                            field.hourlyPrice, field.currency != null ? field.currency : "ج.م"));
                } else {
                    priceText.setText("السعر غير متاح");
                }

                // التقييم
                if (field.ratingCount > 0) {
                    ratingText.setText(String.format(Locale.getDefault(), "⭐ %.1f (%d تقييم)",
                            field.ratingAverage, field.ratingCount));
                } else {
                    ratingText.setText("⭐ جديد - لا توجد تقييمات");
                }

                // المسافة
                if (field.distance > 0) {
                    distanceText.setText(String.format(Locale.getDefault(), "📍 %.1f كم", field.distance));
                    double walkTime = (field.distance / 5) * 60;
                    walkTimeText.setText(String.format(Locale.getDefault(), "  • %.0f دقيقة مشي", walkTime));
                } else {
                    distanceText.setText("");
                    walkTimeText.setText("");
                }

                // العنوان
                if (field.address != null && !field.address.isEmpty()) {
                    String fullAddress = "📍 " + field.address;
                    if (field.city != null) {
                        fullAddress += " - " + field.city;
                    }
                    addressText.setText(fullAddress);
                    addressText.setVisibility(View.VISIBLE);
                } else {
                    addressText.setVisibility(View.GONE);
                }

                // المميزات
                amenitiesLayout.removeAllViews();
                if (field.amenities != null && !field.amenities.isEmpty()) {
                    int maxAmenities = Math.min(4, field.amenities.size());
                    for (int i = 0; i < maxAmenities; i++) {
                        String amenity = field.amenities.get(i);
                        TextView amenityTag = createAmenityTag(getAmenityIcon(amenity));
                        amenitiesLayout.addView(amenityTag);
                    }

                    if (field.amenities.size() > 4) {
                        TextView moreTag = createAmenityTag("+" + (field.amenities.size() - 4));
                        amenitiesLayout.addView(moreTag);
                    }
                }

                card.setOnClickListener(v -> {
                    Intent intent = new Intent(context, FieldActivity.class);
                    intent.putExtra("fieldId", field.docId);
                    context.startActivity(intent);
                });

                // تحميل الصورة
                db.collection("fields")
                        .document(field.docId)
                        .collection("images")
                        .whereEqualTo("type", "cover")
                        .limit(1)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.isEmpty()) {
                                String url = snapshot.getDocuments().get(0).getString("url");
                                if (url != null) {
                                    Glide.with(context)
                                            .load(url.replace("\"", ""))
                                            .centerCrop()
                                            .into(imageView);
                                }
                            } else {
                                // محاولة جلب أي صورة
                                db.collection("fields")
                                        .document(field.docId)
                                        .collection("images")
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener(imgs -> {
                                            if (!imgs.isEmpty()) {
                                                String url = imgs.getDocuments().get(0).getString("url");
                                                if (url != null) {
                                                    Glide.with(context)
                                                            .load(url.replace("\"", ""))
                                                            .centerCrop()
                                                            .into(imageView);
                                                }
                                            }
                                        });
                            }
                        });
            }

            private TextView createAmenityTag(String text) {
                TextView tag = new TextView(context);
                tag.setText(text);
                tag.setTextSize(11);
                tag.setTextColor(Color.parseColor("#666666"));
                tag.setTypeface(ThemeManager.fontMedium());
                tag.setPadding(dp(8), dp(4), dp(8), dp(4));

                GradientDrawable tagBg = new GradientDrawable();
                tagBg.setCornerRadius(dp(12));
                tagBg.setColor(Color.parseColor("#F0F0F0"));
                tag.setBackground(tagBg);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, dp(6), 0);
                tag.setLayoutParams(params);

                return tag;
            }
        }
    }

    private static class FieldData {
        String docId;
        String name;
        String description;
        String category;
        String fieldType;
        String address;
        String city;
        String area;
        String providerId;
        String ownerName;
        String status;
        String currency;
        double hourlyPrice;
        double ratingAverage;
        int ratingCount;
        double lat;
        double lng;
        double distance;
        boolean verified;
        boolean featured;
        List<String> amenities;
        Timestamp createdAt;
    }

    private String getCategoryDisplay(String category) {
        if (category == null) return "";
        switch (category) {
            case "football": return "⚽ كرة القدم";
            case "basketball": return "🏀 كرة السلة";
            case "tennis": return "🎾 التنس";
            case "volleyball": return "🏐 الكرة الطائرة";
            case "gym": return "💪 صالة رياضية";
            default: return category;
        }
    }

    private String getAmenityIcon(String amenity) {
        if (amenity == null) return "";
        switch (amenity) {
            case "parking": return "🅿️";
            case "changing-rooms": return "👕";
            case "showers": return "🚿";
            case "cafeteria": return "☕";
            case "wifi": return "📶";
            case "lighting": return "💡";
            case "seating": return "🪑";
            case "lockers": return "🔐";
            case "first-aid": return "⚕️";
            case "air-conditioning": return "❄️";
            default: return amenity;
        }
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}