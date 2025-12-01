package hagzy.layouts.main.menu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bytepulse.hagzy.helpers.UiHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * بروفايل المستخدم في القائمة الجانبية
 */
public class MenuProfile {

    private Context context;
    private LinearLayout container;
    private ImageView profileImage;
    private TextView nameText;
    private TextView emailText;

    public MenuProfile(Context context) {
        this.context = context;
        buildProfile();
        loadUserData();
    }

    private void buildProfile() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(24);
        container.setLayoutParams(params);

        // صورة الملف الشخصي
        profileImage = createProfileImage();
        container.addView(profileImage);

        // اسم المستخدم
        nameText = createNameText();
        container.addView(nameText);

        // البريد الإلكتروني
        emailText = createEmailText();
        container.addView(emailText);
    }

    private ImageView createProfileImage() {
        ImageView image = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(80), dp(80));
        params.bottomMargin = dp(12);
        image.setLayoutParams(params);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // خلفية دائرية
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#F5F5F5"));
        background.setCornerRadius(dp(40));
        image.setBackground(background);

        return image;
    }

    private TextView createNameText() {
        TextView text = UiHelper.createText(context, "", 20, "#000000", 3);
        text.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.bottomMargin = dp(4);
        text.setLayoutParams(params);

        return text;
    }

    private TextView createEmailText() {
        TextView text = UiHelper.createText(context, "", 14, "#666666", 1);
        text.setGravity(Gravity.CENTER);
        return text;
    }

    /**
     * تحميل بيانات المستخدم من Firebase
     */
    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // الاسم
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                nameText.setText(displayName);
            } else {
                nameText.setText("Guest User");
            }

            // البريد
            String email = user.getEmail();
            if (email != null && !email.isEmpty()) {
                emailText.setText(email);
            } else {
                emailText.setText("");
            }

            // الصورة
            Uri photoUri = user.getPhotoUrl();
            if (photoUri != null) {
                Glide.with(context)
                        .load(photoUri)
                        .circleCrop()
                        .into(profileImage);
            }
        } else {
            nameText.setText("Guest User");
            emailText.setText("Not signed in");
        }
    }

    /**
     * تحديث البيانات يدوياً
     */
    public void setUserData(String name, String email, Uri photoUri) {
        if (nameText != null) nameText.setText(name);
        if (emailText != null) emailText.setText(email);

        if (photoUri != null && profileImage != null) {
            Glide.with(context)
                    .load(photoUri)
                    .circleCrop()
                    .into(profileImage);
        }
    }

    /**
     * إعادة تحميل البيانات
     */
    public void refresh() {
        loadUserData();
    }

    public LinearLayout getView() {
        return container;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}