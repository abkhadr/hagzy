package hagzy.layouts.field.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bytepulse.hagzy.helpers.UiHelper;

/**
 * BookingButton - زر الحجز الثابت في الأسفل
 */
public class BookingButton {

    private Context context;
    private LinearLayout container;
    private String fieldId;

    public BookingButton(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(Color.WHITE);
        container.setPadding(dp(24), dp(16), dp(24), dp(24));
        container.setElevation(dp(8));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        container.setLayoutParams(params);

        // Booking Button
        LinearLayout button = new LinearLayout(context);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);
        button.setPadding(0, dp(18), 0, dp(18));
        button.setClickable(true);
        button.setFocusable(true);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#000000"));
        bg.setCornerRadius(dp(14));
        button.setBackground(bg);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        button.setLayoutParams(btnParams);

        // Icon
        TextView icon = UiHelper.createText(context, "📅", 20, "#FFFFFF", 1);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        iconParams.setMarginEnd(dp(10));
        icon.setLayoutParams(iconParams);
        button.addView(icon);

        // Text
        TextView text = UiHelper.createText(context, "احجز الآن", 18, "#FFFFFF", 3);
        button.addView(text);

        button.setOnClickListener(v -> handleBooking());

        container.addView(button);
    }

    public View getView() {
        return container;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    private void handleBooking() {
        // TODO: Navigate to booking activity
        Toast.makeText(context,
                "سيتم فتح صفحة الحجز قريباً",
                Toast.LENGTH_SHORT).show();

        // Intent intent = new Intent(context, BookingActivity.class);
        // intent.putExtra("fieldId", fieldId);
        // context.startActivity(intent);
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}