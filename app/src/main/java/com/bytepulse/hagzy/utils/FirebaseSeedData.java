package com.bytepulse.hagzy.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to add test data to Firestore
 * Use this for quick testing and development
 */
public class FirebaseSeedData {

    private static final String TAG = "FirebaseSeedData";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public FirebaseSeedData() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    // ════════════════════════════════════════════════════════════
    // 🚀 Main Seed Method
    // ════════════════════════════════════════════════════════════

    public void seedAllData(SeedCallback callback) {
        Log.d(TAG, "Starting data seeding...");

        seedCurrentUser(() -> {
            Log.d(TAG, "✓ Current user seeded");

            seedFields(() -> {
                Log.d(TAG, "✓ Fields seeded");

                seedPlayers(() -> {
                    Log.d(TAG, "✓ Players seeded");
                    Log.d(TAG, "✅ All data seeded successfully!");
                    callback.onComplete();
                }, error -> {
                    Log.e(TAG, "Error seeding players: " + error);
                    callback.onError(error);
                });
            }, error -> {
                Log.e(TAG, "Error seeding fields: " + error);
                callback.onError(error);
            });
        }, error -> {
            Log.e(TAG, "Error seeding current user: " + error);
            callback.onError(error);
        });
    }

    // ════════════════════════════════════════════════════════════
    // 👤 Seed Current User
    // ════════════════════════════════════════════════════════════

    public void seedCurrentUser(Runnable onSuccess, ErrorCallback onError) {
        String userId = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            onError.onError("No user logged in");
            return;
        }

        Map<String, Object> player = new HashMap<>();
        player.put("userId", userId);
        player.put("name", "أحمد محمد");
        player.put("email", mAuth.getCurrentUser().getEmail());
        player.put("phone", "+201234567890");
        player.put("avatar", "");
        player.put("createdAt", System.currentTimeMillis());
        player.put("updatedAt", System.currentTimeMillis());

        // Profile
        Map<String, Object> profile = new HashMap<>();
        profile.put("level", 5);
        profile.put("xp", 350);
        profile.put("xpToNextLevel", 500);
        profile.put("title", "لاعب محترف");
        profile.put("titleIcon", "⚡");
        profile.put("rating", 4.2);
        profile.put("preferredPosition", "midfielder");
        profile.put("playStyle", "balanced");
        profile.put("strength", 75);
        profile.put("speed", 82);
        profile.put("stamina", 78);
        profile.put("technique", 85);
        profile.put("teamwork", 90);
        player.put("profile", profile);

        // Stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMatches", 25);
        stats.put("wins", 15);
        stats.put("losses", 7);
        stats.put("draws", 3);
        stats.put("goals", 12);
        stats.put("assists", 8);
        stats.put("cleanSheets", 0);
        stats.put("yellowCards", 2);
        stats.put("redCards", 0);
        stats.put("winRate", 60.0);
        stats.put("averageRating", 4.2);
        player.put("stats", stats);

        // Achievements
        List<Map<String, Object>> achievements = new ArrayList<>();

        Map<String, Object> achievement1 = new HashMap<>();
        achievement1.put("id", "first_match");
        achievement1.put("name", "أول مباراة");
        achievement1.put("icon", "⚽");
        achievement1.put("description", "لعبت أول مباراة لك");
        achievement1.put("unlocked", true);
        achievement1.put("unlockedAt", System.currentTimeMillis());
        achievements.add(achievement1);

        Map<String, Object> achievement2 = new HashMap<>();
        achievement2.put("id", "hat_trick");
        achievement2.put("name", "هاتريك");
        achievement2.put("icon", "🎯");
        achievement2.put("description", "سجلت 3 أهداف في مباراة واحدة");
        achievement2.put("unlocked", true);
        achievement2.put("unlockedAt", System.currentTimeMillis());
        achievements.add(achievement2);

        player.put("achievements", achievements);

        // Favorites
        Map<String, Object> favorites = new HashMap<>();
        favorites.put("fields", new ArrayList<String>());
        favorites.put("positions", Arrays.asList("midfielder", "forward"));
        player.put("favorites", favorites);

        db.collection("players")
                .document(userId)
                .set(player)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.onError(e.getMessage()));
    }

    // ════════════════════════════════════════════════════════════
    // 🏟️ Seed Fields
    // ════════════════════════════════════════════════════════════

    public void seedFields(Runnable onSuccess, ErrorCallback onError) {
        List<Map<String, Object>> fields = createFieldsData();

        seedFieldsRecursive(fields, 0, onSuccess, onError);
    }

    private void seedFieldsRecursive(List<Map<String, Object>> fields, int index,
                                     Runnable onSuccess, ErrorCallback onError) {
        if (index >= fields.size()) {
            onSuccess.run();
            return;
        }

        Map<String, Object> field = fields.get(index);
        String fieldId = "field_" + (index + 1);

        db.collection("fields")
                .document(fieldId)
                .set(field)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Field " + fieldId + " seeded");
                    seedFieldsRecursive(fields, index + 1, onSuccess, onError);
                })
                .addOnFailureListener(e -> onError.onError(e.getMessage()));
    }

    private List<Map<String, Object>> createFieldsData() {
        List<Map<String, Object>> fields = new ArrayList<>();

        // Field 1: Champions Arena
        fields.add(createField(
                "ملعب الأبطال",
                "Champions Arena",
                "ملعب حديث بمواصفات عالمية",
                "مدينة نصر، القاهرة",
                "مدينة نصر",
                30.0505,
                31.3357,
                250,
                4.8,
                120,
                true
        ));

        // Field 2: Elite Sports
        fields.add(createField(
                "إيليت سبورتس",
                "Elite Sports",
                "ملعب متعدد الاستخدامات",
                "التجمع الخامس، القاهرة الجديدة",
                "التجمع الخامس",
                30.0131,
                31.4315,
                180,
                4.6,
                95,
                true
        ));

        // Field 3: Victory Stadium
        fields.add(createField(
                "ملعب النصر",
                "Victory Stadium",
                "ملعب مجهز بأحدث الإمكانيات",
                "المعادي، القاهرة",
                "المعادي",
                29.9602,
                31.2569,
                200,
                4.5,
                110,
                false
        ));

        // Field 4: Golden Field
        fields.add(createField(
                "الملعب الذهبي",
                "Golden Field",
                "ملعب عشب صناعي من الجيل الرابع",
                "مدينتي، القاهرة الجديدة",
                "مدينتي",
                30.0296,
                31.5114,
                150,
                4.7,
                88,
                true
        ));

        // Field 5: ProPlay Arena
        fields.add(createField(
                "برو بلاي أرينا",
                "ProPlay Arena",
                "مركز رياضي متكامل",
                "الشيخ زايد، الجيزة",
                "الشيخ زايد",
                30.0208,
                30.9767,
                220,
                4.4,
                130,
                false
        ));

        return fields;
    }

    private Map<String, Object> createField(
            String name, String nameEn, String description,
            String address, String district,
            double lat, double lng,
            int hourlyRate, double rating, int totalReviews,
            boolean isFeatured
    ) {
        Map<String, Object> field = new HashMap<>();

        field.put("name", name);
        field.put("nameEn", nameEn);
        field.put("description", description);
        field.put("images", Arrays.asList(
                "https://placeholder.com/800x600",
                "https://placeholder.com/800x600",
                "https://placeholder.com/800x600"
        ));
        field.put("coverImage", "https://placeholder.com/1200x400");

        // Location
        Map<String, Object> location = new HashMap<>();
        location.put("address", address);
        location.put("city", "القاهرة");
        location.put("district", district);

        Map<String, Object> coordinates = new HashMap<>();
        coordinates.put("lat", lat);
        coordinates.put("lng", lng);
        location.put("coordinates", coordinates);

        field.put("location", location);

        // Facilities
        Map<String, Object> facilities = new HashMap<>();
        facilities.put("hasParking", true);
        facilities.put("hasShowers", true);
        facilities.put("hasLockerRoom", true);
        facilities.put("hasCafeteria", true);
        facilities.put("hasFirstAid", true);
        facilities.put("hasLighting", true);
        field.put("facilities", facilities);

        // Surface
        Map<String, Object> surface = new HashMap<>();
        surface.put("type", "artificial_grass");
        surface.put("quality", "excellent");
        surface.put("lastMaintenance", System.currentTimeMillis());
        field.put("surface", surface);

        // Availability
        Map<String, Object> availability = new HashMap<>();
        availability.put("availableTypes", Arrays.asList("2v2", "4v4", "5v5", "11v11"));

        Map<String, Object> maxPlayers = new HashMap<>();
        maxPlayers.put("2v2", 2);
        maxPlayers.put("4v4", 4);
        maxPlayers.put("5v5", 5);
        maxPlayers.put("11v11", 11);
        availability.put("maxPlayersPerType", maxPlayers);

        field.put("availability", availability);

        // Pricing
        Map<String, Object> pricing = new HashMap<>();
        pricing.put("hourlyRate", hourlyRate);
        pricing.put("currency", "EGP");

        Map<String, Object> pricePerPlayer = new HashMap<>();
        pricePerPlayer.put("2v2", 25);
        pricePerPlayer.put("4v4", 25);
        pricePerPlayer.put("5v5", 20);
        pricePerPlayer.put("11v11", 18);
        pricing.put("pricePerPlayer", pricePerPlayer);

        Map<String, Object> discounts = new HashMap<>();
        discounts.put("weekday", 10);
        discounts.put("earlyBird", 15);
        discounts.put("bulkBooking", 20);
        pricing.put("discounts", discounts);

        field.put("pricing", pricing);

        // Rating
        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put("average", rating);
        ratingMap.put("totalReviews", totalReviews);

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("5", (int)(totalReviews * 0.6));
        breakdown.put("4", (int)(totalReviews * 0.25));
        breakdown.put("3", (int)(totalReviews * 0.1));
        breakdown.put("2", (int)(totalReviews * 0.03));
        breakdown.put("1", (int)(totalReviews * 0.02));
        ratingMap.put("breakdown", breakdown);

        field.put("rating", ratingMap);

        // Contact
        Map<String, Object> contact = new HashMap<>();
        contact.put("phone", "+201234567890");
        contact.put("whatsapp", "+201234567890");
        contact.put("email", "field@example.com");
        contact.put("facebook", "https://facebook.com");
        contact.put("instagram", "https://instagram.com");
        field.put("contact", contact);

        // Opening Hours
        Map<String, Object> hours = new HashMap<>();
        for (String day : Arrays.asList("monday", "tuesday", "wednesday", "thursday")) {
            Map<String, String> dayHours = new HashMap<>();
            dayHours.put("open", "09:00");
            dayHours.put("close", "23:00");
            hours.put(day, dayHours);
        }

        Map<String, String> friday = new HashMap<>();
        friday.put("open", "09:00");
        friday.put("close", "24:00");
        hours.put("friday", friday);

        Map<String, String> saturday = new HashMap<>();
        saturday.put("open", "08:00");
        saturday.put("close", "24:00");
        hours.put("saturday", saturday);

        Map<String, String> sunday = new HashMap<>();
        sunday.put("open", "08:00");
        sunday.put("close", "23:00");
        hours.put("sunday", sunday);

        field.put("openingHours", hours);

        // Stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookings", 500 + (int)(Math.random() * 500));
        stats.put("activeUsers", 250 + (int)(Math.random() * 250));
        stats.put("popularTimes", Arrays.asList("18:00-20:00", "20:00-22:00"));
        field.put("stats", stats);

        field.put("createdAt", System.currentTimeMillis());
        field.put("updatedAt", System.currentTimeMillis());
        field.put("isActive", true);
        field.put("isFeatured", isFeatured);

        return field;
    }

    // ════════════════════════════════════════════════════════════
    // 👥 Seed Test Players
    // ════════════════════════════════════════════════════════════

    public void seedPlayers(Runnable onSuccess, ErrorCallback onError) {
        List<Map<String, Object>> players = createPlayersData();

        seedPlayersRecursive(players, 0, onSuccess, onError);
    }

    private void seedPlayersRecursive(List<Map<String, Object>> players, int index,
                                      Runnable onSuccess, ErrorCallback onError) {
        if (index >= players.size()) {
            onSuccess.run();
            return;
        }

        Map<String, Object> player = players.get(index);
        String playerId = "test_player_" + (index + 1);

        db.collection("players")
                .document(playerId)
                .set(player)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Player " + playerId + " seeded");
                    seedPlayersRecursive(players, index + 1, onSuccess, onError);
                })
                .addOnFailureListener(e -> onError.onError(e.getMessage()));
    }

    private List<Map<String, Object>> createPlayersData() {
        List<Map<String, Object>> players = new ArrayList<>();

        String[] names = {
                "محمد علي", "أحمد حسن", "خالد محمود", "عمر سعيد", "يوسف إبراهيم",
                "كريم طارق", "حسام الدين", "مصطفى أحمد", "سامي خالد", "طارق عبدالله"
        };

        String[] positions = {"goalkeeper", "defender", "midfielder", "forward"};

        for (int i = 0; i < names.length; i++) {
            Map<String, Object> player = new HashMap<>();

            player.put("userId", "test_player_" + (i + 1));
            player.put("name", names[i]);
            player.put("email", "player" + (i + 1) + "@test.com");
            player.put("phone", "+2010000000" + i);
            player.put("avatar", "");
            player.put("createdAt", System.currentTimeMillis());
            player.put("updatedAt", System.currentTimeMillis());

            // Profile
            Map<String, Object> profile = new HashMap<>();
            profile.put("level", 1 + (int)(Math.random() * 10));
            profile.put("xp", (int)(Math.random() * 500));
            profile.put("xpToNextLevel", 100 + (int)(Math.random() * 400));
            profile.put("title", getTitleForLevel(profile.get("level")));
            profile.put("titleIcon", "⚡");
            profile.put("rating", 2.0 + (Math.random() * 3.0));
            profile.put("preferredPosition", positions[(int)(Math.random() * positions.length)]);
            profile.put("playStyle", "balanced");
            profile.put("strength", 50 + (int)(Math.random() * 50));
            profile.put("speed", 50 + (int)(Math.random() * 50));
            profile.put("stamina", 50 + (int)(Math.random() * 50));
            profile.put("technique", 50 + (int)(Math.random() * 50));
            profile.put("teamwork", 50 + (int)(Math.random() * 50));
            player.put("profile", profile);

            // Stats
            Map<String, Object> stats = new HashMap<>();
            int totalMatches = (int)(Math.random() * 50);
            int wins = (int)(totalMatches * (0.3 + Math.random() * 0.4));
            int losses = (int)(totalMatches * (0.2 + Math.random() * 0.3));
            int draws = totalMatches - wins - losses;

            stats.put("totalMatches", totalMatches);
            stats.put("wins", wins);
            stats.put("losses", losses);
            stats.put("draws", Math.max(0, draws));
            stats.put("goals", (int)(Math.random() * 20));
            stats.put("assists", (int)(Math.random() * 15));
            stats.put("cleanSheets", 0);
            stats.put("yellowCards", (int)(Math.random() * 5));
            stats.put("redCards", 0);
            stats.put("winRate", totalMatches > 0 ? (wins * 100.0 / totalMatches) : 0.0);
            stats.put("averageRating", 2.0 + (Math.random() * 3.0));
            player.put("stats", stats);

            // Empty achievements and favorites
            player.put("achievements", new ArrayList<>());

            Map<String, Object> favorites = new HashMap<>();
            favorites.put("fields", new ArrayList<>());
            favorites.put("positions", Arrays.asList(positions[(int)(Math.random() * positions.length)]));
            player.put("favorites", favorites);

            players.add(player);
        }

        return players;
    }

    private String getTitleForLevel(Object level) {
        int lvl = level instanceof Long ? ((Long) level).intValue() : (Integer) level;
        if (lvl < 3) return "مبتدئ";
        if (lvl < 6) return "لاعب";
        if (lvl < 9) return "محترف";
        return "نجم";
    }

    // ════════════════════════════════════════════════════════════
    // 🗑️ Clear All Data
    // ════════════════════════════════════════════════════════════

    public void clearAllData(Runnable onSuccess, ErrorCallback onError) {
        Log.d(TAG, "Clearing all test data...");

        // Delete test players
        for (int i = 1; i <= 10; i++) {
            db.collection("players").document("test_player_" + i).delete();
        }

        // Delete fields
        for (int i = 1; i <= 5; i++) {
            db.collection("fields").document("field_" + i).delete();
        }

        Log.d(TAG, "✓ All test data cleared");
        onSuccess.run();
    }

    // ════════════════════════════════════════════════════════════
    // 📦 Callbacks
    // ════════════════════════════════════════════════════════════

    public interface SeedCallback {
        void onComplete();
        void onError(String error);
    }

    public interface ErrorCallback {
        void onError(String error);
    }
}