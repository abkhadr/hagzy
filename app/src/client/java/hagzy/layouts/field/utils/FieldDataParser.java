package hagzy.layouts.field.utils;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FieldDataParser - معالج بيانات الملعب من Firestore
 */
public class FieldDataParser {

    public static FieldData parse(DocumentSnapshot doc) {
        FieldData data = new FieldData();

        // Basic Info
        data.id = doc.getId();
        data.name = doc.getString("name");
        data.description = doc.getString("description");
        data.coverImage = doc.getString("coverImage");
        data.phone = doc.getString("phone");
        data.email = doc.getString("email");
        data.website = doc.getString("website");
        data.isFeatured = doc.getBoolean("isFeatured") != null ?
                doc.getBoolean("isFeatured") : false;
        data.isActive = doc.getBoolean("isActive") != null ?
                doc.getBoolean("isActive") : true;

        // Location
        Map<String, Object> location = (Map<String, Object>) doc.get("location");
        if (location != null) {
            data.address = (String) location.get("address");
            data.city = (String) location.get("city");
            data.district = (String) location.get("district");

            Map<String, Object> coordinates = (Map<String, Object>) location.get("coordinates");
            if (coordinates != null) {
                data.latitude = coordinates.get("latitude") != null ?
                        ((Number) coordinates.get("latitude")).doubleValue() : 0.0;
                data.longitude = coordinates.get("longitude") != null ?
                        ((Number) coordinates.get("longitude")).doubleValue() : 0.0;
            }
        }

        // Pricing
        Map<String, Object> pricing = (Map<String, Object>) doc.get("pricing");
        if (pricing != null) {
            data.hourlyRate = pricing.get("hourlyRate") != null ?
                    ((Long) pricing.get("hourlyRate")).intValue() : 0;
            data.currency = (String) pricing.get("currency");

            Map<String, Object> timeSlots = (Map<String, Object>) pricing.get("timeSlots");
            if (timeSlots != null) {
                data.morningPrice = timeSlots.get("morning") != null ?
                        ((Long) timeSlots.get("morning")).intValue() : data.hourlyRate;
                data.afternoonPrice = timeSlots.get("afternoon") != null ?
                        ((Long) timeSlots.get("afternoon")).intValue() : data.hourlyRate;
                data.eveningPrice = timeSlots.get("evening") != null ?
                        ((Long) timeSlots.get("evening")).intValue() : data.hourlyRate;
            }
        }

        // Rating
        Map<String, Object> rating = (Map<String, Object>) doc.get("rating");
        if (rating != null) {
            data.rating = rating.get("average") != null ?
                    ((Number) rating.get("average")).doubleValue() : 0.0;
            data.totalReviews = rating.get("totalReviews") != null ?
                    ((Long) rating.get("totalReviews")).intValue() : 0;
        }

        // Features
        List<String> features = (List<String>) doc.get("features");
        if (features != null) {
            data.features = new ArrayList<>(features);
        }

        // Images
        List<String> images = (List<String>) doc.get("images");
        if (images != null) {
            data.images = new ArrayList<>(images);
        }

        // Operating Hours
        Map<String, Object> hours = (Map<String, Object>) doc.get("operatingHours");
        if (hours != null) {
            data.openTime = (String) hours.get("open");
            data.closeTime = (String) hours.get("close");
        }

        // Capacity
        Map<String, Object> capacity = (Map<String, Object>) doc.get("capacity");
        if (capacity != null) {
            data.maxPlayers = capacity.get("maxPlayers") != null ?
                    ((Long) capacity.get("maxPlayers")).intValue() : 0;
            data.minPlayers = capacity.get("minPlayers") != null ?
                    ((Long) capacity.get("minPlayers")).intValue() : 0;
        }

        // Field Type
        data.fieldType = doc.getString("fieldType");
        data.surfaceType = doc.getString("surfaceType");

        return data;
    }

    /**
     * FieldData - بيانات الملعب
     */
    public static class FieldData {
        // Basic Info
        public String id;
        public String name;
        public String description;
        public String coverImage;
        public String phone;
        public String email;
        public String website;
        public boolean isFeatured;
        public boolean isActive;

        // Location
        public String address;
        public String city;
        public String district;
        public double latitude;
        public double longitude;

        // Pricing
        public int hourlyRate;
        public String currency;
        public int morningPrice;
        public int afternoonPrice;
        public int eveningPrice;

        // Rating
        public double rating;
        public int totalReviews;

        // Features & Images
        public List<String> features;
        public List<String> images;

        // Operating Hours
        public String openTime;
        public String closeTime;

        // Capacity
        public int maxPlayers;
        public int minPlayers;

        // Field Info
        public String fieldType;
        public String surfaceType;
    }
}