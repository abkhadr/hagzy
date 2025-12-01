package hagzy.layouts.settings.cards;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

public class HeroCard {

    private final Context context;
    private final String name;
    private final String email;
    private final String photoUrl;
    private CardView card;

    public HeroCard(Context context, String name, String email, String photoUrl) {
        this.context = context;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        buildCard();
    }

    private void buildCard() {
        card = new CardView(context);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(dp(20), 0, dp(20), 0);
        card.setLayoutParams(cardParams);
        card.setRadius(dp(20));
        card.setCardElevation(0);
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(32), dp(32), dp(32), dp(32));

        // Profile Image
        ImageView profileImg = new ImageView(context);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dp(96), dp(96));
        imgParams.bottomMargin = dp(16);
        profileImg.setLayoutParams(imgParams);

        GradientDrawable imgBg = new GradientDrawable();
        imgBg.setShape(GradientDrawable.OVAL);
        imgBg.setColor(Color.parseColor("#F5F5F5"));
        profileImg.setBackground(imgBg);
        profileImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        profileImg.setClipToOutline(true);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(context).load(photoUrl).into(profileImg);
        } else {
            profileImg.setImageResource(R.drawable.check_badge);
        }

        // Name
        TextView userName = new TextView(context);
        userName.setText(name != null ? name : "—");
        userName.setTextSize(28);
        userName.setTypeface(ThemeManager.fontBold());
        userName.setTextColor(Color.parseColor("#1A1A1A"));
        userName.setTranslationY(-dpf(1f));
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        userName.setLayoutParams(nameParams);

        // Email
        TextView userEmail = new TextView(context);
        userEmail.setText(email != null ? email : "—");
        userEmail.setTextSize(14);
        userEmail.setTypeface(ThemeManager.fontSemiBold());
        userEmail.setTextColor(Color.parseColor("#666666"));
        userEmail.setTranslationY(-dpf(1f));
        LinearLayout.LayoutParams emailParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        emailParams.topMargin = dp(4);
        userEmail.setLayoutParams(emailParams);

        content.addView(profileImg);
        content.addView(userName);
        content.addView(userEmail);
        card.addView(content);
    }

    public CardView getView() {
        return card;
    }

    private int dp(int dpValue) {
        return UiHelper.dp(context, dpValue);
    }

    private float dpf(float value) {
        return UiHelper.dpFloat(context, value);
    }
}