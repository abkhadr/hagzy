package com.bytepulse.hagzy.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * مدير أكواد التفعيل
 * يتعامل مع إنشاء والتحقق من أكواد التفعيل بين المستخدم ومقدم الخدمة
 */
public class ActivationCodeManager {

    private static final String TAG = "ActivationCodeManager";
    private DatabaseReference realtimeDB;
    private FirebaseFirestore firestore;
    private Context context;

    public ActivationCodeManager(Context context) {
        this.context = context;
        this.realtimeDB = FirebaseDatabase.getInstance().getReference();
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * توليد كود تفعيل فريد
     */
    public String generateActivationCode(String bookingId, String userId, String salt) {
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
            Log.e(TAG, "Error generating activation code", e);
            return UUID.randomUUID().toString();
        }
    }

    /**
     * توليد QR Code من النص
     */
    public Bitmap generateQRCode(String data, int size) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size);

            int width = matrix.getWidth();
            int height = matrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error generating QR code", e);
            return null;
        }
    }

    /**
     * التحقق من كود تفعيل المستخدم
     */
    public void verifyUserActivationCode(String userId, String bookingId, String code,
                                         ActivationCallback callback) {
        realtimeDB.child("bookings").child(userId).child(bookingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onError("الحجز غير موجود");
                            return;
                        }

                        String storedCode = snapshot.child("userActivationCode").getValue(String.class);
                        String activationStatus = snapshot.child("activationStatus").getValue(String.class);

                        if (storedCode == null) {
                            callback.onError("كود التفعيل غير موجود");
                            return;
                        }

                        if (!storedCode.equals(code)) {
                            callback.onError("كود التفعيل غير صحيح");
                            return;
                        }

                        if ("activated".equals(activationStatus)) {
                            callback.onError("تم تفعيل هذا الحجز مسبقاً");
                            return;
                        }

                        // Update activation status
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("activationStatus", "user_confirmed");
                        updates.put("userConfirmationTime", System.currentTimeMillis());

                        realtimeDB.child("bookings").child(userId).child(bookingId)
                                .updateChildren(updates)
                                .addOnSuccessListener(aVoid -> callback.onSuccess("تم تأكيد الحجز من جانب المستخدم"))
                                .addOnFailureListener(e -> callback.onError("فشل التحديث: " + e.getMessage()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError("خطأ في قراءة البيانات: " + error.getMessage());
                    }
                });
    }

    /**
     * التحقق من كود تفعيل مقدم الخدمة
     */
    public void verifyProviderActivationCode(String userId, String bookingId, String code,
                                             ActivationCallback callback) {
        realtimeDB.child("bookings").child(userId).child(bookingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onError("الحجز غير موجود");
                            return;
                        }

                        String storedCode = snapshot.child("providerActivationCode").getValue(String.class);
                        String activationStatus = snapshot.child("activationStatus").getValue(String.class);

                        if (storedCode == null) {
                            callback.onError("كود التفعيل غير موجود");
                            return;
                        }

                        if (!storedCode.equals(code)) {
                            callback.onError("كود التفعيل غير صحيح");
                            return;
                        }

                        if ("activated".equals(activationStatus)) {
                            callback.onError("تم تفعيل هذا الحجز مسبقاً");
                            return;
                        }

                        // Check if user confirmed first
                        if (!"user_confirmed".equals(activationStatus)) {
                            callback.onError("يجب على المستخدم تأكيد الحجز أولاً");
                            return;
                        }

                        // Update activation status - booking is now fully activated
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("activationStatus", "activated");
                        updates.put("providerConfirmationTime", System.currentTimeMillis());
                        updates.put("fullyActivatedTime", System.currentTimeMillis());

                        realtimeDB.child("bookings").child(userId).child(bookingId)
                                .updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    // Update booking status in Firestore
                                    String fieldId = snapshot.child("fieldId").getValue(String.class);
                                    String bookingDocId = snapshot.child("bookingDocId").getValue(String.class);

                                    if (fieldId != null && bookingDocId != null) {
                                        updateBookingActiveStatus(fieldId, bookingDocId, true);
                                    }

                                    callback.onSuccess("✅ تم تفعيل الحجز بنجاح!");
                                })
                                .addOnFailureListener(e -> callback.onError("فشل التحديث: " + e.getMessage()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError("خطأ في قراءة البيانات: " + error.getMessage());
                    }
                });
    }

    /**
     * تحديث حالة الحجز النشط في Firestore
     */
    private void updateBookingActiveStatus(String fieldId, String bookingDocId, boolean isActive) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isActive", isActive);
        updates.put("activatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        firestore.collection("fields")
                .document(fieldId)
                .collection("bookings")
                .document(bookingDocId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Booking activated in Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to activate in Firestore", e));
    }

    /**
     * الحصول على تفاصيل الحجز مع أكواد التفعيل
     */
    public void getBookingWithCodes(String userId, String bookingId, BookingCallback callback) {
        realtimeDB.child("bookings").child(userId).child(bookingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onError("الحجز غير موجود");
                            return;
                        }

                        BookingDetails details = new BookingDetails();
                        details.bookingId = bookingId;
                        details.bookingDocId = snapshot.child("bookingDocId").getValue(String.class);
                        details.fieldId = snapshot.child("fieldId").getValue(String.class);
                        details.bookingName = snapshot.child("bookingName").getValue(String.class);
                        details.category = snapshot.child("category").getValue(String.class);
                        details.price = snapshot.child("price").getValue(Double.class);
                        details.userActivationCode = snapshot.child("userActivationCode").getValue(String.class);
                        details.providerActivationCode = snapshot.child("providerActivationCode").getValue(String.class);
                        details.activationStatus = snapshot.child("activationStatus").getValue(String.class);

                        if (snapshot.hasChild("duration") && snapshot.child("duration").hasChild("startEnd")) {
                            details.startTime = snapshot.child("duration").child("startEnd").child("start").getValue(Long.class);
                            details.endTime = snapshot.child("duration").child("startEnd").child("end").getValue(Long.class);
                        }

                        callback.onSuccess(details);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError("خطأ في قراءة البيانات: " + error.getMessage());
                    }
                });
    }

    /**
     * إلغاء حجز وإعادة تعيين الأكواد
     */
    public void cancelBookingAndResetCodes(String userId, String bookingId, String fieldId,
                                           String bookingDocId, ActivationCallback callback) {
        // Update Firestore
        Map<String, Object> firestoreUpdates = new HashMap<>();
        firestoreUpdates.put("status", "cancelled");
        firestoreUpdates.put("bookedBy", null);
        firestoreUpdates.put("bookedByName", null);
        firestoreUpdates.put("isActive", false);

        firestore.collection("fields")
                .document(fieldId)
                .collection("bookings")
                .document(bookingDocId)
                .update(firestoreUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Delete from Realtime DB
                    realtimeDB.child("bookings")
                            .child(userId)
                            .child(bookingId)
                            .removeValue()
                            .addOnSuccessListener(aVoid1 -> callback.onSuccess("تم إلغاء الحجز بنجاح"))
                            .addOnFailureListener(e -> callback.onError("فشل الإلغاء: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("فشل الإلغاء: " + e.getMessage()));
    }

    /**
     * فحص حالة تفعيل الحجز
     */
    public void checkActivationStatus(String userId, String bookingId, StatusCallback callback) {
        realtimeDB.child("bookings").child(userId).child(bookingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onStatus("not_found", "الحجز غير موجود");
                            return;
                        }

                        String status = snapshot.child("activationStatus").getValue(String.class);
                        Long endTime = null;

                        if (snapshot.hasChild("duration") && snapshot.child("duration").hasChild("startEnd")) {
                            endTime = snapshot.child("duration").child("startEnd").child("end").getValue(Long.class);
                        }

                        // Check if expired
                        if (endTime != null && endTime < System.currentTimeMillis()) {
                            callback.onStatus("expired", "انتهت صلاحية الحجز");
                            return;
                        }

                        if ("activated".equals(status)) {
                            callback.onStatus("activated", "الحجز نشط ومفعّل");
                        } else if ("user_confirmed".equals(status)) {
                            callback.onStatus("user_confirmed", "تم تأكيد المستخدم - بانتظار تأكيد المقدم");
                        } else {
                            callback.onStatus("pending", "بانتظار التفعيل");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onStatus("error", "خطأ: " + error.getMessage());
                    }
                });
    }

    // ============================
    // Callback Interfaces
    // ============================
    public interface ActivationCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface BookingCallback {
        void onSuccess(BookingDetails details);
        void onError(String error);
    }

    public interface StatusCallback {
        void onStatus(String status, String message);
    }

    // ============================
    // BookingDetails Model
    // ============================
    public static class BookingDetails {
        public String bookingId;
        public String bookingDocId;
        public String fieldId;
        public String bookingName;
        public String category;
        public Double price;
        public String userActivationCode;
        public String providerActivationCode;
        public String activationStatus;
        public Long startTime;
        public Long endTime;
    }
}