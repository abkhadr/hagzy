package hagzy.layouts.profile.sections;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

import hagzy.layouts.profile.utils.PlayerDataParser.PlayerData;

/**
 * ProfileHeaderSection - قسم رأس الملف الشخصي
 */
public class ProfileHeaderSection implements ProfileSection {

    private static final String TAG = "ProfileHeaderSection";

    private Context context;
    private LinearLayout container;
    private ImageView avatarImage;
    private TextView nameText;
    private TextView idText;

    public ProfileHeaderSection(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(24), dp(24), dp(24));
        container.setBackgroundColor(Color.WHITE);

        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        // Avatar
        avatarImage = new ImageView(context);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(128), dp(128));
        avatarParams.setMarginEnd(dp(12));
        avatarImage.setLayoutParams(avatarParams);
        avatarImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarImage.setClipToOutline(true);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#F0F0F0"));
        avatarImage.setBackground(avatarBg);

        topRow.addView(avatarImage);

        // Info Container
        LinearLayout infoContainer = new LinearLayout(context);
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        infoParams.leftMargin = dp(16);
        infoContainer.setLayoutParams(infoParams);

        // Name
        nameText = UiHelper.createText(context, "", 26, "#000000", 3);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        nameText.setLayoutParams(nameParams);
        nameText.setGravity(isRTL() ? Gravity.END : Gravity.START);
        infoContainer.addView(nameText);

        // ID Row
        infoContainer.addView(createIdRow());

        topRow.addView(infoContainer);
        container.addView(topRow);
    }

    private View createIdRow() {
        LinearLayout idRow = new LinearLayout(context);
        idRow.setOrientation(LinearLayout.HORIZONTAL);
        idRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams idRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        idRowParams.topMargin = dp(8);
        idRow.setLayoutParams(idRowParams);

        TextView idLabel = UiHelper.createText(context, "ID: ", 12, "#999999", 1);
        idRow.addView(idLabel);

        idText = UiHelper.createText(context, "", 12, "#999999", 1);
        idText.setTypeface(ThemeManager.fontRegular());
        idRow.addView(idText);

        // Copy Button
        FrameLayout copyButton = new FrameLayout(context);
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        copyParams.leftMargin = dp(6);
        copyButton.setLayoutParams(copyParams);
        copyButton.setPadding(dp(4), dp(4), dp(4), dp(4));

        TextView copyIcon = UiHelper.createText(context, "⎘", 12, "#999999", 1);
        copyIcon.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams copyIconParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        copyIcon.setLayoutParams(copyIconParams);
        copyButton.addView(copyIcon);

        idRow.addView(copyButton);

        return idRow;
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void setData(PlayerData data) {
        // Set avatar
        if (data.avatar != null && !data.avatar.isEmpty()) {
            try {
                Glide.with(context).load(data.avatar).into(avatarImage);
            } catch (Exception e) {
                Log.e(TAG, "Error loading avatar", e);
            }
        }

        // Set name
        nameText.setText(data.name);

        // Set ID
        String shortId = data.userId.length() > 12 ?
                data.userId.substring(0, 12) + "..." : data.userId;
        idText.setText(shortId);

        // Setup copy button
        setupCopyButton(data.userId);
    }

    private void setupCopyButton(String userId) {
        View copyButton = ((LinearLayout) container.getChildAt(0))
                .getChildAt(2); // infoContainer

        if (copyButton instanceof LinearLayout) {
            LinearLayout idRow = (LinearLayout) ((LinearLayout) copyButton).getChildAt(2);
            View btn = idRow.getChildAt(2);

            btn.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager)
                        context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("User ID", userId);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "تم نسخ ID", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private boolean isRTL() {
        return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private int dp(int value) {
        return UiHelper.dp(context, value);
    }
}