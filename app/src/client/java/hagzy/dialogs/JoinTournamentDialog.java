package hagzy.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinTournamentDialog extends DialogFragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String tournamentId;
    private String tournamentType;
    private int maxPlayersPerTeam;
    private int teamACount;
    private int teamBCount;

    private String selectedTeam = null; // "A" or "B"
    private String selectedPosition = null;

    private LinearLayout teamAButton;
    private LinearLayout teamBButton;
    private LinearLayout positionsContainer;
    private LinearLayout joinButton;
    private ProgressBar progressBar;

    private OnJoinListener onJoinListener;

    public interface OnJoinListener {
        void onJoined();
    }

    public static JoinTournamentDialog newInstance(
            String tournamentId,
            String tournamentType,
            int maxPlayersPerTeam,
            int teamACount,
            int teamBCount
    ) {
        JoinTournamentDialog dialog = new JoinTournamentDialog();
        Bundle args = new Bundle();
        args.putString("tournamentId", tournamentId);
        args.putString("tournamentType", tournamentType);
        args.putInt("maxPlayersPerTeam", maxPlayersPerTeam);
        args.putInt("teamACount", teamACount);
        args.putInt("teamBCount", teamBCount);
        dialog.setArguments(args);
        return dialog;
    }

    
    public void setOnJoinListener(OnJoinListener listener) {
        this.onJoinListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            tournamentId = getArguments().getString("tournamentId");
            tournamentType = getArguments().getString("tournamentType");
            maxPlayersPerTeam = getArguments().getInt("maxPlayersPerTeam");
            teamACount = getArguments().getInt("teamACount");
            teamBCount = getArguments().getInt("teamBCount");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(buildUI());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        return dialog;
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 UI Building
    // ════════════════════════════════════════════════════════════

    private View buildUI() {
        FrameLayout root = new FrameLayout(requireContext());
        root.setPadding(dp(24), dp(24), dp(24), dp(24));

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setVerticalScrollBarEnabled(false);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(24), dp(24), dp(24));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(24));
        container.setBackground(bg);

        // Header
        container.addView(createHeader());

        // Team Selection
        container.addView(createTeamSelection());

        // Positions Container (hidden initially)
        positionsContainer = new LinearLayout(requireContext());
        positionsContainer.setOrientation(LinearLayout.VERTICAL);
        positionsContainer.setVisibility(View.GONE);
        LinearLayout.LayoutParams posParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        posParams.topMargin = dp(24);
        positionsContainer.setLayoutParams(posParams);
        container.addView(positionsContainer);

        // Join Button (hidden initially)
        joinButton = createJoinButton();
        joinButton.setVisibility(View.GONE);
        LinearLayout.LayoutParams joinParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        joinParams.topMargin = dp(24);
        joinButton.setLayoutParams(joinParams);
        container.addView(joinButton);

        // Progress Bar
        progressBar = new ProgressBar(requireContext());
        progressBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                dp(32), dp(32)
        );
        progressParams.gravity = Gravity.CENTER;
        progressParams.topMargin = dp(24);
        progressBar.setLayoutParams(progressParams);
        container.addView(progressBar);

        scrollView.addView(container);
        root.addView(scrollView);

        return root;
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headerParams.bottomMargin = dp(24);
        header.setLayoutParams(headerParams);

        TextView icon = createText("⚔️", 40, "#667eea", 1);
        icon.setGravity(Gravity.CENTER);
        header.addView(icon);

        TextView title = createText("انضم للتورنمنت", 22, "#000000", 3);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(12);
        title.setLayoutParams(titleParams);
        header.addView(title);

        TextView subtitle = createText("اختر الفريق والمركز", 14, "#666666", 1);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subParams.topMargin = dp(4);
        subtitle.setLayoutParams(subParams);
        header.addView(subtitle);

        return header;
    }

    private LinearLayout createTeamSelection() {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);

        TextView label = createText("اختر الفريق", 16, "#000000", 3);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        labelParams.bottomMargin = dp(12);
        label.setLayoutParams(labelParams);
        container.addView(label);

        // Teams Row
        LinearLayout teamsRow = new LinearLayout(requireContext());
        teamsRow.setOrientation(LinearLayout.HORIZONTAL);

        teamAButton = createTeamButton("الفريق A", "🔵", teamACount, maxPlayersPerTeam, "A");
        LinearLayout.LayoutParams teamAParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        teamAParams.setMarginEnd(dp(8));
        teamAButton.setLayoutParams(teamAParams);
        teamsRow.addView(teamAButton);

        teamBButton = createTeamButton("الفريق B", "🔴", teamBCount, maxPlayersPerTeam, "B");
        LinearLayout.LayoutParams teamBParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        teamBParams.setMarginStart(dp(8));
        teamBButton.setLayoutParams(teamBParams);
        teamsRow.addView(teamBButton);

        container.addView(teamsRow);

        return container;
    }

    private LinearLayout createTeamButton(String name, String icon, int count, int max, String team) {
        LinearLayout button = new LinearLayout(requireContext());
        button.setOrientation(LinearLayout.VERTICAL);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(16), dp(20), dp(16), dp(20));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(2), Color.parseColor("#E0E0E0"));
        button.setBackground(bg);

        // Icon
        TextView iconText = createText(icon, 32, "#000000", 1);
        iconText.setGravity(Gravity.CENTER);
        button.addView(iconText);

        // Name
        TextView nameText = createText(name, 16, "#000000", 3);
        nameText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        nameParams.topMargin = dp(8);
        nameText.setLayoutParams(nameParams);
        button.addView(nameText);

        // Count
        TextView countText = createText(count + "/" + max, 14, "#666666", 1);
        countText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        countParams.topMargin = dp(4);
        countText.setLayoutParams(countParams);
        button.addView(countText);

        // Disable if full
        if (count >= max) {
            button.setAlpha(0.5f);
            button.setEnabled(false);
        } else {
            button.setOnClickListener(v -> selectTeam(team, button));
        }

        return button;
    }

    private void selectTeam(String team, LinearLayout selectedButton) {
        selectedTeam = team;
        selectedPosition = null; // Reset position

        // Update team buttons appearance
        updateTeamButton(teamAButton, team.equals("A"));
        updateTeamButton(teamBButton, team.equals("B"));

        // Show positions
        showPositions();
    }

    private void updateTeamButton(LinearLayout button, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));

        if (selected) {
            bg.setColor(Color.parseColor("#667eea"));
            bg.setStroke(dp(2), Color.parseColor("#667eea"));

            // Update text colors to white
            for (int i = 0; i < button.getChildCount(); i++) {
                View child = button.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(Color.WHITE);
                }
            }
        } else {
            bg.setColor(Color.parseColor("#F5F5F5"));
            bg.setStroke(dp(2), Color.parseColor("#E0E0E0"));

            // Reset text colors
            TextView nameText = (TextView) button.getChildAt(1);
            nameText.setTextColor(Color.parseColor("#000000"));
            TextView countText = (TextView) button.getChildAt(2);
            countText.setTextColor(Color.parseColor("#666666"));
        }

        button.setBackground(bg);
    }

    private void showPositions() {
        positionsContainer.removeAllViews();
        positionsContainer.setVisibility(View.VISIBLE);

        TextView label = createText("اختر المركز", 16, "#000000", 3);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        labelParams.bottomMargin = dp(12);
        label.setLayoutParams(labelParams);
        positionsContainer.addView(label);

        List<String> positions = getPositionsForType(tournamentType);

        for (String position : positions) {
            positionsContainer.addView(createPositionButton(position));
        }
    }

    private LinearLayout createPositionButton(String position) {
        LinearLayout button = new LinearLayout(requireContext());
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(dp(20), dp(16), dp(20), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F5F5F5"));
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(2), Color.parseColor("#E0E0E0"));
        button.setBackground(bg);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonParams.bottomMargin = dp(8);
        button.setLayoutParams(buttonParams);

        // Icon
        String icon = getPositionIcon(position);
        TextView iconText = createText(icon + " ", 24, "#667eea", 1);
        button.addView(iconText);

        // Name
        String name = getPositionName(position);
        TextView nameText = createText(name, 16, "#000000", 1);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        nameText.setLayoutParams(nameParams);
        button.addView(nameText);

        button.setOnClickListener(v -> selectPosition(position, button));

        return button;
    }

    private void selectPosition(String position, LinearLayout selectedButton) {
        selectedPosition = position;

        // Update all position buttons
        for (int i = 1; i < positionsContainer.getChildCount(); i++) {
            View child = positionsContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                updatePositionButton((LinearLayout) child, false);
            }
        }

        // Highlight selected
        updatePositionButton(selectedButton, true);

        // Show join button
        joinButton.setVisibility(View.VISIBLE);
    }

    private void updatePositionButton(LinearLayout button, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));

        if (selected) {
            bg.setColor(Color.parseColor("#667eea"));
            bg.setStroke(dp(2), Color.parseColor("#667eea"));

            TextView nameText = (TextView) button.getChildAt(1);
            nameText.setTextColor(Color.WHITE);
        } else {
            bg.setColor(Color.parseColor("#F5F5F5"));
            bg.setStroke(dp(2), Color.parseColor("#E0E0E0"));

            TextView nameText = (TextView) button.getChildAt(1);
            nameText.setTextColor(Color.parseColor("#000000"));
        }

        button.setBackground(bg);
    }

    private LinearLayout createJoinButton() {
        LinearLayout button = new LinearLayout(requireContext());
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(20), dp(16), dp(20), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#4CAF50"));
        bg.setCornerRadius(dp(12));
        button.setBackground(bg);

        TextView buttonText = createText("✅ انضم الآن", 18, "#FFFFFF", 3);
        button.addView(buttonText);

        button.setOnClickListener(v -> joinTournament());

        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1f);
            }
            return false;
        });

        return button;
    }

    // ════════════════════════════════════════════════════════════
    // 🔥 Join Tournament
    // ════════════════════════════════════════════════════════════

    private void joinTournament() {
        if (selectedTeam == null || selectedPosition == null) {
            Toast.makeText(requireContext(), "اختر الفريق والمركز", Toast.LENGTH_SHORT).show();
            return;
        }

        joinButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        String currentUserId = mAuth.getCurrentUser().getUid();

        // Get player data first
        db.collection("players").document(currentUserId).get()
                .addOnSuccessListener(playerDoc -> {
                    if (playerDoc.exists()) {
                        String playerName = playerDoc.getString("name");
                        Map<String, Object> profile = (Map<String, Object>) playerDoc.get("profile");

                        int level = profile != null && profile.get("level") != null ?
                                ((Long) profile.get("level")).intValue() : 1;
                        double rating = profile != null && profile.get("rating") != null ?
                                ((Number) profile.get("rating")).doubleValue() : 0.0;
                        String preferredPosition = profile != null ?
                                (String) profile.get("preferredPosition") : "forward";

                        // Create player data
                        Map<String, Object> playerData = new HashMap<>();
                        playerData.put("userId", currentUserId);
                        playerData.put("name", playerName);
                        playerData.put("avatar", ""); // TODO: Add avatar URL
                        playerData.put("position", selectedPosition);
                        playerData.put("role", "player"); // Will be updated to "captain" if first
                        playerData.put("level", level);
                        playerData.put("rating", rating);
                        playerData.put("preferredPosition", preferredPosition);
                        playerData.put("joinedAt", FieldValue.serverTimestamp());
                        playerData.put("isPaid", false);

                        // Add to tournament
                        String teamField = "team" + selectedTeam + ".players";

                        db.collection("tournaments").document(tournamentId)
                                .update(
                                        teamField, FieldValue.arrayUnion(playerData),
                                        "stats.team" + selectedTeam + "Count", FieldValue.increment(1),
                                        "stats.totalPlayers", FieldValue.increment(1),
                                        "stats.spotsLeft", FieldValue.increment(-1),
                                        "updatedAt", FieldValue.serverTimestamp()
                                )
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(requireContext(), "تم الانضمام بنجاح! ⚽", Toast.LENGTH_SHORT).show();

                                    if (onJoinListener != null) {
                                        onJoinListener.onJoined();
                                    }

                                    dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    joinButton.setVisibility(View.VISIBLE);
                                    Toast.makeText(requireContext(), "فشل الانضمام", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    // ════════════════════════════════════════════════════════════
    // 🔧 Helper Methods
    // ════════════════════════════════════════════════════════════

    private List<String> getPositionsForType(String type) {
        List<String> positions = new ArrayList<>();

        switch (type) {
            case "2v2":
                positions.add("goalkeeper");
                positions.add("forward");
                break;
            case "4v4":
                positions.add("goalkeeper");
                positions.add("defender");
                positions.add("midfielder");
                positions.add("forward");
                break;
            case "5v5":
                positions.add("goalkeeper");
                positions.add("defender");
                positions.add("midfielder");
                positions.add("forward");
                break;
            case "11v11":
                positions.add("goalkeeper");
                positions.add("defender");
                positions.add("midfielder");
                positions.add("forward");
                break;
        }

        return positions;
    }

    private String getPositionIcon(String position) {
        switch (position) {
            case "goalkeeper": return "🧤";
            case "defender": return "🛡️";
            case "midfielder": return "⚡";
            case "forward": return "⚽";
            default: return "👤";
        }
    }

    private String getPositionName(String position) {
        switch (position) {
            case "goalkeeper": return "حارس مرمى";
            case "defender": return "مدافع";
            case "midfielder": return "لاعب وسط";
            case "forward": return "مهاجم";
            default: return "لاعب";
        }
    }

    private TextView createText(String text, int size, String color, int weight) {
        return UiHelper.createText(requireContext(), text, size, color, weight);
    }

    private int dp(int value) {
        return UiHelper.dp(requireContext(), value);
    }
}