package hagzy.layouts.wallet.models;

/**
 * نموذج عملية المحفظة
 */
public class TransactionItem {

    // ════════════════════════════════════════════════════════════
    // 📊 البيانات الأساسية
    // ════════════════════════════════════════════════════════════

    public final String id;
    public final String type;
    public final String title;
    public final String date;
    public final double amount;
    public final boolean isIncome;
    public final String status;
    public final long timestamp;

    // ════════════════════════════════════════════════════════════
    // 🏗️ البناء
    // ════════════════════════════════════════════════════════════

    public TransactionItem(
            String id,
            String type,
            String title,
            String date,
            double amount,
            boolean isIncome,
            String status,
            long timestamp
    ) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;
        this.status = status;
        this.timestamp = timestamp;
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    /**
     * الحصول على أيقونة العملية حسب النوع
     */
    public String getIcon() {
        switch (type) {
            case "deposit": return "💰";
            case "withdrawal": return "💸";
            case "booking_payment": return "🎫";
            case "refund": return "↩️";
            default: return "💳";
        }
    }

    /**
     * الحصول على عنوان افتراضي حسب النوع
     */
    public String getDefaultTitle() {
        switch (type) {
            case "deposit": return "إيداع في المحفظة";
            case "withdrawal": return "سحب من المحفظة";
            case "booking_payment": return "دفع حجز";
            case "refund": return "استرجاع مبلغ";
            default: return "عملية";
        }
    }

    /**
     * الحصول على لون العملية
     */
    public String getAmountColor() {
        return isIncome ? "#4CAF50" : "#F44336";
    }

    /**
     * الحصول على لون خلفية الأيقونة
     */
    public String getIconBackgroundColor() {
        return isIncome ? "#E8F5E9" : "#FFEBEE";
    }

    /**
     * التحقق من حالة العملية
     */
    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isCompleted() {
        return "completed".equals(status);
    }

    public boolean isFailed() {
        return "failed".equals(status);
    }

    /**
     * الحصول على نص الحالة
     */
    public String getStatusText() {
        switch (status) {
            case "pending": return "⏳ قيد المعالجة";
            case "completed": return "✓ مكتملة";
            case "failed": return "✗ فاشلة";
            default: return "";
        }
    }

    /**
     * الحصول على لون الحالة
     */
    public String getStatusColor() {
        switch (status) {
            case "pending": return "#FF9800";
            case "completed": return "#4CAF50";
            case "failed": return "#F44336";
            default: return "#999999";
        }
    }
}