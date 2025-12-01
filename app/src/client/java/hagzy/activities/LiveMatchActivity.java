package hagzy.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hagzy.adapters.PlayersAdapter;
import hagzy.helpers.LobbyManager;

public class LiveMatchActivity extends AppCompatActivity {

    private TextView tvMatchId, tvTimer, tvTeam1Score, tvTeam2Score, tvStatus, tvPing;
    private RecyclerView rvTeam1, rvTeam2;

    private LobbyManager wsManager;
    private String matchId, lobbyId, userId, userName;
    private boolean isLeader;

    private CountDownTimer matchTimer;
    private long matchDurationMillis = 600000;
    private long timeRemaining = matchDurationMillis;

    private PlayersAdapter team1Adapter, team2Adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        matchId = getIntent().getStringExtra("matchId");
        lobbyId = getIntent().getStringExtra("lobbyId");
        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");
        isLeader = getIntent().getBooleanExtra("isLeader", false);

        setContentView(createUI());
        startMatchTimer();
        connectToMatch();
    }

    private RelativeLayout createUI() {
        RelativeLayout main = new RelativeLayout(this);
        main.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        main.setBackgroundColor(Color.parseColor("#1a1a2e"));

        LinearLayout header = createHeader();
        LinearLayout teamsContainer = createTeamsContainer();

        main.addView(header);
        main.addView(teamsContainer);

        return main;
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setId(android.R.id.content);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));
        header.setElevation(dpToPx(8));

        GradientDrawable headerBg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#2d2d44"), Color.parseColor("#252538")}
        );
        header.setBackground(headerBg);

        RelativeLayout.LayoutParams headerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        headerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        header.setLayoutParams(headerParams);

        // Match ID & Ping Row
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        tvMatchId = new TextView(this);
        tvMatchId.setText("المباراة: " + matchId);
        tvMatchId.setTextColor(Color.WHITE);
        tvMatchId.setTextSize(16);
        LinearLayout.LayoutParams matchIdParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvMatchId.setLayoutParams(matchIdParams);

        tvPing = new TextView(this);
        tvPing.setText("-- ms");
        tvPing.setTextColor(Color.parseColor("#FFC107"));
        tvPing.setTextSize(14);

        topRow.addView(tvMatchId);
        topRow.addView(tvPing);

        // Timer
        tvTimer = new TextView(this);
        tvTimer.setText("10:00");
        tvTimer.setTextColor(Color.parseColor("#4CAF50"));
        tvTimer.setTextSize(48);
        tvTimer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams timerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        timerParams.topMargin = dpToPx(16);
        tvTimer.setLayoutParams(timerParams);

        // Status
        tvStatus = new TextView(this);
        tvStatus.setText("جاري الاتصال...");
        tvStatus.setTextColor(Color.parseColor("#FFC107"));
        tvStatus.setTextSize(16);
        tvStatus.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        statusParams.topMargin = dpToPx(8);
        tvStatus.setLayoutParams(statusParams);

        // Score Row
        LinearLayout scoreRow = new LinearLayout(this);
        scoreRow.setOrientation(LinearLayout.HORIZONTAL);
        scoreRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams scoreRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        scoreRowParams.topMargin = dpToPx(20);
        scoreRow.setLayoutParams(scoreRowParams);

        tvTeam1Score = new TextView(this);
        tvTeam1Score.setText("0");
        tvTeam1Score.setTextColor(Color.parseColor("#2196F3"));
        tvTeam1Score.setTextSize(36);
        tvTeam1Score.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams score1Params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvTeam1Score.setLayoutParams(score1Params);

        TextView colon = new TextView(this);
        colon.setText(":");
        colon.setTextColor(Color.WHITE);
        colon.setTextSize(36);
        colon.setPadding(dpToPx(16), 0, dpToPx(16), 0);

        tvTeam2Score = new TextView(this);
        tvTeam2Score.setText("0");
        tvTeam2Score.setTextColor(Color.parseColor("#E91E63"));
        tvTeam2Score.setTextSize(36);
        tvTeam2Score.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams score2Params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvTeam2Score.setLayoutParams(score2Params);

        scoreRow.addView(tvTeam1Score);
        scoreRow.addView(colon);
        scoreRow.addView(tvTeam2Score);

        header.addView(topRow);
        header.addView(tvTimer);
        header.addView(tvStatus);
        header.addView(scoreRow);

        return header;
    }

    private LinearLayout createTeamsContainer() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setWeightSum(2);

        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        containerParams.addRule(RelativeLayout.BELOW, android.R.id.content);
        containerParams.topMargin = dpToPx(16);
        container.setLayoutParams(containerParams);

        // Team 1
        LinearLayout team1Layout = createTeamLayout("الفريق الأزرق", true);
        rvTeam1 = (RecyclerView) team1Layout.getChildAt(1);

        // Team 2
        LinearLayout team2Layout = createTeamLayout("الفريق الوردي", false);
        rvTeam2 = (RecyclerView) team2Layout.getChildAt(1);

        container.addView(team1Layout);
        container.addView(team2Layout);

        team1Adapter = new PlayersAdapter(new ArrayList<>());
        rvTeam1.setLayoutManager(new LinearLayoutManager(this));
        rvTeam1.setAdapter(team1Adapter);

        team2Adapter = new PlayersAdapter(new ArrayList<>());
        rvTeam2.setLayoutManager(new LinearLayoutManager(this));
        rvTeam2.setAdapter(team2Adapter);

        return container;
    }

    private LinearLayout createTeamLayout(String teamName, boolean isBlue) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        layoutParams.setMargins(isBlue ? 0 : dpToPx(8), 0, isBlue ? dpToPx(8) : 0, 0);
        layout.setLayoutParams(layoutParams);

        // Team Header
        TextView header = new TextView(this);
        header.setText(teamName);
        header.setTextColor(Color.WHITE);
        header.setTextSize(16);
        header.setGravity(Gravity.CENTER);
        header.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        GradientDrawable headerBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                isBlue ? new int[]{Color.parseColor("#2196F3"), Color.parseColor("#1565C0")}
                        : new int[]{Color.parseColor("#E91E63"), Color.parseColor("#AD1457")}
        );
        headerBg.setCornerRadii(new float[]{dpToPx(12),dpToPx(12),dpToPx(12),dpToPx(12),0,0,0,0});
        header.setBackground(headerBg);

        // RecyclerView
        RecyclerView rv = new RecyclerView(this);
        rv.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        rv.setClipToPadding(false);
        LinearLayout.LayoutParams rvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        rvParams.topMargin = dpToPx(8);
        rv.setLayoutParams(rvParams);

        layout.addView(header);
        layout.addView(rv);

        return layout;
    }

    private void startMatchTimer() {
        matchTimer = new CountDownTimer(matchDurationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                int minutes = (int) (timeRemaining / 1000) / 60;
                int seconds = (int) (timeRemaining / 1000) % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                tvStatus.setText("انتهت المباراة");
                Toast.makeText(LiveMatchActivity.this, "انتهى الوقت!", Toast.LENGTH_LONG).show();
            }
        };
        matchTimer.start();
    }

    private void connectToMatch() {
        Log.d("LiveMatch", "==========================================");
        Log.d("LiveMatch", "→ Connecting to match");
        Log.d("LiveMatch", "  Match ID: " + matchId);
        Log.d("LiveMatch", "  Lobby ID: " + lobbyId);
        Log.d("LiveMatch", "  User: " + userName + " (" + userId + ")");
        Log.d("LiveMatch", "  Leader: " + isLeader);

        wsManager = new LobbyManager(new LobbyManager.LobbyListener() {
            @Override
            public void onConnected() {
                Log.d("LiveMatch", "✓ Connected to match");
                runOnUiThread(() -> {
                    tvStatus.setText("متصل - جاري اللعب");
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                    Toast.makeText(LiveMatchActivity.this, "✓ تم الاتصال بالمباراة", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onDisconnected() {
                Log.w("LiveMatch", "✗ Disconnected from match");
                runOnUiThread(() -> {
                    tvStatus.setText("انقطع الاتصال");
                    tvStatus.setTextColor(Color.parseColor("#F44336"));

                    // Show reconnection dialog
                    new AlertDialog.Builder(LiveMatchActivity.this)
                            .setTitle("انقطع الاتصال")
                            .setMessage("هل تريد إعادة الاتصال؟")
                            .setPositiveButton("إعادة الاتصال", (d, w) -> {
                                connectToMatch(); // Reconnect
                            })
                            .setNegativeButton("خروج", (d, w) -> {
                                finish();
                            })
                            .setCancelable(false)
                            .show();
                });
            }

            @Override
            public void onJoinedLobby(LobbyManager.LobbyData lobby, boolean leader) {
                Log.d("LiveMatch", "✓ Joined match lobby");
                Log.d("LiveMatch", "  Players: " + lobby.players.size());
                updateTeams(lobby);
            }

            @Override
            public void onPlayerJoined(LobbyManager.LobbyData lobby) {
                Log.d("LiveMatch", "✓ Player joined during match");
                Log.d("LiveMatch", "  Total players: " + lobby.players.size());
                updateTeams(lobby);

                runOnUiThread(() -> {
                    if (lobby.players.size() > 0) {
                        String lastPlayer = lobby.players.get(lobby.players.size() - 1).name;
                        Toast.makeText(LiveMatchActivity.this,
                                "✓ " + lastPlayer + " انضم للمباراة",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onPlayerLeft(LobbyManager.LobbyData lobby) {
                Log.w("LiveMatch", "✗ Player left during match");
                Log.d("LiveMatch", "  Remaining: " + lobby.players.size());
                updateTeams(lobby);

                runOnUiThread(() -> {
                    Toast.makeText(LiveMatchActivity.this,
                            "✗ لاعب غادر المباراة",
                            Toast.LENGTH_SHORT).show();

                    // If too few players, show warning
                    if (lobby.players.size() < 2) {
                        new AlertDialog.Builder(LiveMatchActivity.this)
                                .setTitle("تحذير")
                                .setMessage("عدد اللاعبين غير كافٍ لاستمرار المباراة")
                                .setPositiveButton("موافق", null)
                                .show();
                    }
                });
            }

            @Override
            public void onMatchStarted(String matchId, LobbyManager.LobbyData lobby) {
                // Already in match, ignore
                Log.d("LiveMatch", "Match started event received (already in match)");
            }

            @Override
            public void onPingUpdate(long ping) {
                runOnUiThread(() -> {
                    tvPing.setText(ping + " ms");

                    // Color code based on ping
                    if (ping < 50) {
                        tvPing.setTextColor(Color.parseColor("#4CAF50")); // Green
                    } else if (ping < 100) {
                        tvPing.setTextColor(Color.parseColor("#FF9800")); // Orange
                    } else if (ping < 200) {
                        tvPing.setTextColor(Color.parseColor("#FF5722")); // Deep Orange
                    } else {
                        tvPing.setTextColor(Color.parseColor("#F44336")); // Red
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e("LiveMatch", "✗ Error: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(LiveMatchActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });

        // Connect using "live" mode to indicate this is an ongoing match
        wsManager.connect(lobbyId, userId, userName, "live");
    }

    private void updateTeams(LobbyManager.LobbyData lobby) {
        if (lobby == null || lobby.players == null || lobby.players.isEmpty()) {
            Log.w("LiveMatch", "Cannot update teams: Invalid lobby data");
            return;
        }

        runOnUiThread(() -> {
            Log.d("LiveMatch", "→ Updating teams");
            Log.d("LiveMatch", "  Total players: " + lobby.players.size());

            // Split players into two teams
            int totalPlayers = lobby.players.size();
            int halfSize = totalPlayers / 2;

            // Team 1: First half of players
            List<LobbyManager.PlayerData> team1 = new ArrayList<>();
            for (int i = 0; i < halfSize && i < totalPlayers; i++) {
                team1.add(lobby.players.get(i));
            }

            // Team 2: Second half of players
            List<LobbyManager.PlayerData> team2 = new ArrayList<>();
            for (int i = halfSize; i < totalPlayers; i++) {
                team2.add(lobby.players.get(i));
            }

            Log.d("LiveMatch", "  Team 1: " + team1.size() + " players");
            Log.d("LiveMatch", "  Team 2: " + team2.size() + " players");

            // Log team members
            for (int i = 0; i < team1.size(); i++) {
                Log.d("LiveMatch", "    Blue Team " + (i+1) + ": " + team1.get(i).name);
            }
            for (int i = 0; i < team2.size(); i++) {
                Log.d("LiveMatch", "    Pink Team " + (i+1) + ": " + team2.get(i).name);
            }

            // Update adapters
            team1Adapter.updatePlayers(team1);
            team2Adapter.updatePlayers(team2);

            Toast.makeText(LiveMatchActivity.this,
                    "تم تحديث الفرق: " + team1.size() + " vs " + team2.size(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void updateScore(int team1Score, int team2Score) {
        runOnUiThread(() -> {
            tvTeam1Score.setText(String.valueOf(team1Score));
            tvTeam2Score.setText(String.valueOf(team2Score));

            // Animate score change
            tvTeam1Score.setScaleX(1.3f);
            tvTeam1Score.setScaleY(1.3f);
            tvTeam1Score.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(300)
                    .start();

            tvTeam2Score.setScaleX(1.3f);
            tvTeam2Score.setScaleY(1.3f);
            tvTeam2Score.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(300)
                    .start();
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("مغادرة المباراة")
                .setMessage("هل أنت متأكد من مغادرة المباراة؟ سيتم احتساب المباراة كخسارة.")
                .setPositiveButton("نعم، غادر", (d, w) -> {
                    if (wsManager != null && wsManager.isConnected()) {
                        wsManager.disconnect();
                    }
                    super.onBackPressed();
                    finish();
                })
                .setNegativeButton("استمر في اللعب", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("LiveMatch", "→ Destroying activity");

        // Cancel timer
        if (matchTimer != null) {
            matchTimer.cancel();
            Log.d("LiveMatch", "  ✓ Timer cancelled");
        }

        // Disconnect WebSocket
        if (wsManager != null && wsManager.isConnected()) {
            wsManager.disconnect();
            Log.d("LiveMatch", "  ✓ WebSocket disconnected");
        }
    }
}