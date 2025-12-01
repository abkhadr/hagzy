package hagzy.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

public class JoinChallengeDialog extends DialogFragment {

    private static final String ARG_CHALLENGE_ID = "challengeId";
    private static final String ARG_TYPE = "type";

    private String challengeId;
    private String type;
    private OnJoinListener listener;

    private RadioGroup teamGroup;
    private RadioGroup positionGroup;

    public static JoinChallengeDialog newInstance(String challengeId, String type) {
        JoinChallengeDialog dialog = new JoinChallengeDialog();
        Bundle args = new Bundle();
        args.putString(ARG_CHALLENGE_ID, challengeId);
        args.putString(ARG_TYPE, type);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            challengeId = getArguments().getString(ARG_CHALLENGE_ID);
            type = getArguments().getString(ARG_TYPE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(buildUI());

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }

    private View buildUI() {
        FrameLayout root = new FrameLayout(requireContext());
        root.setPadding(dp(24), dp(24), dp(24), dp(24));

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(24), dp(24), dp(24));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(20));
        container.setBackground(bg);

        // Title
        TextView title = createText("⚽ انضم للتحدي", 22, "#000000", 3);
        title.setGravity(Gravity.CENTER);
        container.addView(title);

        TextView subtitle = createText("اختر الفريق والمركز", 14, "#666666", 1);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subParams.topMargin = dp(4);
        subParams.bottomMargin = dp(20);
        subtitle.setLayoutParams(subParams);
        container.addView(subtitle);

        // Team Selection
        container.addView(createLabel("👥 اختر الفريق"));
        teamGroup = createTeamSelection();
        container.addView(teamGroup);

        // Position Selection
        container.addView(createLabel("🎯 اختر المركز"));
        positionGroup = createPositionSelection();
        container.addView(positionGroup);

        // Buttons
        LinearLayout buttonsRow = new LinearLayout(requireContext());
        buttonsRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonsRow.setWeightSum(2);
        LinearLayout.LayoutParams buttonsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonsParams.topMargin = dp(24);
        buttonsRow.setLayoutParams(buttonsParams);

        // Cancel Button
        TextView cancelButton = createButton("إلغاء", "#E0E0E0", "#000000");
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        cancelParams.setMargins(0, 0, dp(8), 0);
        cancelButton.setLayoutParams(cancelParams);
        cancelButton.setOnClickListener(v -> dismiss());
        buttonsRow.addView(cancelButton);

        // Join Button
        TextView joinButton = createButton("انضم", "#667eea", "#FFFFFF");
        LinearLayout.LayoutParams joinParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        joinParams.setMargins(dp(8), 0, 0, 0);
        joinButton.setLayoutParams(joinParams);
        joinButton.setOnClickListener(v -> handleJoin());
        buttonsRow.addView(joinButton);

        container.addView(buttonsRow);

        root.addView(container);

        return root;
    }

    private TextView createLabel(String text) {
        TextView label = createText(text, 14, "#666666", 3);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(16);
        params.bottomMargin = dp(8);
        label.setLayoutParams(params);
        return label;
    }

    private RadioGroup createTeamSelection() {
        RadioGroup group = new RadioGroup(requireContext());
        group.setOrientation(RadioGroup.HORIZONTAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        group.setLayoutParams(params);

        group.addView(createTeamOption("A", "🔴 الفريق أ"));
        group.addView(createTeamOption("B", "🔵 الفريق ب"));

        return group;
    }

    private RadioButton createTeamOption(String value, String label) {
        RadioButton radio = new RadioButton(requireContext());
        radio.setText(label);
        radio.setTag(value);
        radio.setTypeface(ThemeManager.fontRegular());
        radio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        radio.setPadding(dp(12), dp(8), dp(12), dp(8));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        params.setMargins(dp(4), 0, dp(4), 0);
        radio.setLayoutParams(params);

        return radio;
    }

    private RadioGroup createPositionSelection() {
        RadioGroup group = new RadioGroup(requireContext());
        group.setOrientation(RadioGroup.VERTICAL);

        String[] positions = {
                "goalkeeper:🧤 حارس مرمى",
                "defender:🛡️ مدافع",
                "midfielder:⚡ لاعب وسط",
                "forward:⚽ مهاجم"
        };

        for (String pos : positions) {
            String[] parts = pos.split(":");
            group.addView(createPositionOption(parts[0], parts[1]));
        }

        return group;
    }

    private RadioButton createPositionOption(String value, String label) {
        RadioButton radio = new RadioButton(requireContext());
        radio.setText(label);
        radio.setTag(value);
        radio.setTypeface(ThemeManager.fontRegular());
        radio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        radio.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(4);
        radio.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(12));
        radio.setBackground(bg);

        return radio;
    }

    private void handleJoin() {
        int teamId = teamGroup.getCheckedRadioButtonId();
        int positionId = positionGroup.getCheckedRadioButtonId();

        if (teamId == -1 || positionId == -1) {
            return;
        }

        RadioButton teamRadio = teamGroup.findViewById(teamId);
        RadioButton positionRadio = positionGroup.findViewById(positionId);

        String team = (String) teamRadio.getTag();
        String position = (String) positionRadio.getTag();

        if (listener != null) {
            listener.onJoin(team, position);
        }

        dismiss();
    }

    private TextView createButton(String text, String bgColor, String textColor) {
        TextView button = createText(text, 16, textColor, 3);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(16), dp(14), dp(16), dp(14));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(dp(12));
        button.setBackground(bg);

        button.setClickable(true);
        button.setFocusable(true);

        return button;
    }

    private TextView createText(String text, int size, String color, int weight) {
        return UiHelper.createText(requireContext(), text, size, color, weight);
    }

    private int dp(int value) {
        return UiHelper.dp(requireContext(), value);
    }

    public void setOnJoinListener(OnJoinListener listener) {
        this.listener = listener;
    }

    public interface OnJoinListener {
        void onJoin(String team, String position);
    }
}