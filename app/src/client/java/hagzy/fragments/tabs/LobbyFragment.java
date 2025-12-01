package hagzy.fragments.tabs;

import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import hagzy.activities.LiveMatchActivity;
import hagzy.helpers.LobbyManager;

public class LobbyFragment extends Fragment {

    private static final String TAG = "LobbyFragment";

    private LinearLayout contentContainer;
    private ProgressBar progressBar;
    private TextView tvStatus;

    private LobbyManager lobbyManager;
    private String currentLobbyId, userId, userName, currentMatchType = "4v4";
    private boolean isLeader = false;
    private LobbyManager.LobbyData currentLobby;
    private boolean isInLobby = false;

    // Dev Mode
    private boolean devMode = false;
    private int devClickCount = 0;
    private long lastDevClickTime = 0;

    public static LobbyFragment newInstance(String userId, String userName) {
        LobbyFragment fragment = new LobbyFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("userName", userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            userName = getArguments().getString("userName");
        }

        if (userId == null || userName == null) {
            loadUserData();
        }

        return buildUI();
    }

    private void loadUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("players")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            userName = doc.getString("name");
                            if (userName == null || userName.isEmpty()) {
                                userName = auth.getCurrentUser().getDisplayName();
                            }
                            if (userName == null || userName.isEmpty()) {
                                userName = "لاعب";
                            }
                        }
                    })
                    .addOnFailureListener(e -> userName = "لاعب");
        }
    }

    private View buildUI() {
        FrameLayout root = new FrameLayout(requireContext());
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(Color.WHITE);

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setVerticalScrollBarEnabled(false);

        LinearLayout mainContainer = new LinearLayout(requireContext());
        mainContainer.setOrientation(LinearLayout.VERTICAL);

        contentContainer = new LinearLayout(requireContext());
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.addView(contentContainer);

        progressBar = new ProgressBar(requireContext());
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        progressParams.gravity = Gravity.CENTER;
        progressParams.topMargin = dp(100);
        progressBar.setLayoutParams(progressParams);
        progressBar.setVisibility(View.GONE);
        mainContainer.addView(progressBar);

        scrollView.addView(mainContainer);
        root.addView(scrollView);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            scrollView.setPadding(0, 0, 0, bottom);
            return insets;
        });

        displayMainMenu();
        return root;
    }

    private void displayMainMenu() {
        contentContainer.removeAllViews();
        isInLobby = false;

        contentContainer.addView(createHeader());
        contentContainer.addView(createNewLobbyCard());
        contentContainer.addView(createJoinLobbyCard());
    }

    private View createHeader() {
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(24), dp(6), dp(24), dp(6));
        header.setBackgroundColor(Color.WHITE);

//        TextView title = createText("تحدي", 20, "#000000", 3);
//
//        // Enable dev mode with 5 fast clicks
//        title.setOnClickListener(v -> {
//            long currentTime = System.currentTimeMillis();
//            if (currentTime - lastDevClickTime < 500) {
//                devClickCount++;
//                if (devClickCount >= 5) {
//                    devMode = !devMode;
//                    Toast.makeText(requireContext(),
//                            devMode ? "Dev Mode: ON" : "Dev Mode: OFF",
//                            Toast.LENGTH_SHORT).show();
//                    devClickCount = 0;
//                }
//            } else {
//                devClickCount = 1;
//            }
//            lastDevClickTime = currentTime;
//        });
//
//        header.addView(title);

//        TextView subtitle = createText("أنشئ غرفة جديدة أو انضم لغرفة موجودة", 14, "#666666", 1);
//        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        );
//        subtitle.setLayoutParams(subtitleParams);
//        header.addView(subtitle);

        return header;
    }

    private Bitmap loadBitmapFromAssets(Context ctx, String path) {
        try {
            InputStream is = ctx.getAssets().open(path);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private View createNewLobbyCard() {
        LinearLayout layout = new LinearLayout(requireContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layout.setOrientation(LinearLayout.VERTICAL);
        layoutParams.setMargins(dp(24), 0, dp(24), 0);

        layout.setLayoutParams(layoutParams);

        LinearLayout bannerContainer= new LinearLayout(requireContext());
        LinearLayout.LayoutParams bCParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(200)
        );
        bannerContainer.setClipToOutline(true);
        bannerContainer.setLayoutParams(bCParams);

        FrameLayout bannerCard = new FrameLayout(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(200)
        );
        bannerCard.setLayoutParams(cardParams);

        ImageView bannerImage = new ImageView(requireContext());
        bannerImage.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        bannerImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable imgBg = new GradientDrawable();
        imgBg.setColor(Color.TRANSPARENT);
        imgBg.setCornerRadii(
                new float[]{
                        dp(20), dp(20),   // top-left
                        dp(20), dp(20),   // top-right
                        dp(20), dp(20),   // bottom-right
                        dp(20), dp(20)    // bottom-left
                }
        );
        bannerImage.setBackground(imgBg);
        bannerImage.setClipToOutline(true);

        Bitmap bmp = loadBitmapFromAssets(requireContext(), "images/bannerLobby.png");
        bannerImage.setImageBitmap(bmp);
        bannerCard.addView(bannerImage);

        View overlay = new View(requireContext());
        overlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
//        GradientDrawable overlayGradient = new GradientDrawable(
//                GradientDrawable.Orientation.BOTTOM_TOP,
//                new int[]{
//                        Color.parseColor("#90000000"),
//                        Color.parseColor("#60000000")
//                }
//        );
//        overlayGradient.setCornerRadii(
//                new float[]{
//                        dp(20), dp(20),   // top-left
//                        dp(20), dp(20),   // top-right
//                        dp(20), dp(20),   // bottom-right
//                        dp(20), dp(20)    // bottom-left
//                }
//        );
//        overlay.setBackground(overlayGradient);
        bannerCard.addView(overlay);

        LinearLayout contentContainer = new LinearLayout(requireContext());
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setGravity(Gravity.BOTTOM);
        contentContainer.setPadding(dp(6), dp(12), dp(6), dp(24));
        contentContainer.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView title = createText("أنشئ تحدي جديد", 20, "#000000", 3);
        contentContainer.addView(title);

        TextView subtitle = createText("ادعُ أصدقاءك وابدأ مباراة ممتعة الآن", 14, "#666666", 1);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitle.setLayoutParams(subtitleParams);
        contentContainer.addView(subtitle);

        LinearLayout btnCreate = new LinearLayout(requireContext());
        btnCreate.setOrientation(LinearLayout.HORIZONTAL);
        btnCreate.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        btnCreate.setLayoutParams(btnParams);

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#000000"));
        btnBg.setCornerRadius(dp(16));
        btnCreate.setBackground(btnBg);

        TextView btnText = createText("إنشاء تحدي", 16, "#FFFFFF", 3);
        btnCreate.addView(btnText);

        btnCreate.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .setInterpolator(new android.view.animation.OvershootInterpolator())
                            .start();
                    break;
            }
            return false;
        });

        btnCreate.setOnClickListener(v -> createNewLobby());
        bannerContainer.addView(bannerCard);
        layout.addView(bannerContainer);
        layout.addView(contentContainer);
        layout.addView(btnCreate);

        return layout;
    }

    private Button createMatchTypeButton(String type) {
        Button btn = new Button(requireContext());
        btn.setText(type);
        btn.setTextColor(Color.parseColor("#666666"));
        btn.setTextSize(14);
        btn.setAllCaps(false);
        if (ThemeManager.fontBold() != null) {
            btn.setTypeface(ThemeManager.fontBold());
        }
        btn.setTag("match_type:" + type);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, dp(44), 1);
        btnParams.setMargins(dp(4), 0, dp(4), 0);
        btn.setLayoutParams(btnParams);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F0F0F0"));
        bg.setCornerRadius(dp(10));
        btn.setBackground(bg);

        btn.setOnClickListener(v -> {
            currentMatchType = type;
            updateMatchTypeButtons((ViewGroup) btn.getParent(), type);
        });

        if (currentMatchType == null && type.equals("2v2")) {
            currentMatchType = type;
            selectMatchTypeButton(btn);
        }

        return btn;
    }

    private void updateMatchTypeButtons(ViewGroup parent, String selectedType) {
        if (parent == null) return;

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                Object tag = btn.getTag();
                if (tag != null && tag.toString().startsWith("match_type:")) {
                    String type = tag.toString().substring("match_type:".length());
                    if (type.equals(selectedType)) {
                        selectMatchTypeButton(btn);
                    } else {
                        deselectMatchTypeButton(btn);
                    }
                }
            }
        }
    }

    private void selectMatchTypeButton(Button btn) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#000000"));
        bg.setCornerRadius(dp(10));
        btn.setBackground(bg);
        btn.setTextColor(Color.WHITE);
    }

    private void deselectMatchTypeButton(Button btn) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F0F0F0"));
        bg.setCornerRadius(dp(10));
        btn.setBackground(bg);
        btn.setTextColor(Color.parseColor("#666666"));
    }

    private View createJoinLobbyCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(24), dp(32), dp(24), dp(24));
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(dp(24), dp(24), dp(24), 0);
        card.setLayoutParams(cardParams);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setStroke(dp(2), Color.parseColor("#F5F5F5"));
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(dp(24));
        card.setBackground(cardBg);

        LinearLayout containerCard = new LinearLayout(requireContext());
        containerCard.setOrientation(LinearLayout.VERTICAL);
        containerCard.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams containerCParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerCard.setLayoutParams(containerCParams);

        GradientDrawable containerCBg = new GradientDrawable();
        containerCBg.setCornerRadius(dp(20));
        containerCard.setBackground(containerCBg);

        card.addView(containerCard);

//        TextView sectionTitle = createText("الانضمام لتحدي", 18, "#000000", 3);
//        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        );
//        sectionTitle.setLayoutParams(titleParams);
//        card.addView(sectionTitle);
//
//        TextView sectionSubtitle = createText("أدخل رقم التحدي للانضمام إلى أصدقائك", 14, "#666666", 1);
//        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        );
//        subtitleParams.bottomMargin = dp(20);
//        sectionSubtitle.setLayoutParams(subtitleParams);
//        card.addView(sectionSubtitle);

        EditText etLobbyId = new EditText(requireContext());
        etLobbyId.setHint("أدخل رقم التحدي");
        etLobbyId.setTextColor(Color.parseColor("#000000"));
        etLobbyId.setHintTextColor(Color.parseColor("#999999"));
        etLobbyId.setTextSize(16);
        if (ThemeManager.fontRegular() != null) {
            etLobbyId.setTypeface(ThemeManager.fontSemiBold());
        }
        etLobbyId.setPadding(dp(20), 0, dp(20),0);
        etLobbyId.setSingleLine(true);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
        );
        inputParams.bottomMargin = dp(16);
        etLobbyId.setLayoutParams(inputParams);

        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setStroke(dp(2), Color.parseColor("#F5F5F5"));
        inputBg.setCornerRadius(dp(12));
        etLobbyId.setBackground(inputBg);

        containerCard.addView(etLobbyId);

        LinearLayout btnJoin = new LinearLayout(requireContext());
        btnJoin.setOrientation(LinearLayout.HORIZONTAL);
        btnJoin.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams joinParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        btnJoin.setLayoutParams(joinParams);

        GradientDrawable joinBg = new GradientDrawable();
        joinBg.setColor(Color.parseColor("#F5F5F5"));
        joinBg.setCornerRadius(dp(16));
        btnJoin.setBackground(joinBg);

        TextView btnText = createText("انضمام للتحدي", 16, "#000000", 3);
        btnJoin.addView(btnText);

        btnJoin.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .setInterpolator(new android.view.animation.OvershootInterpolator())
                            .start();
                    break;
            }
            return false;
        });

        btnJoin.setOnClickListener(v -> {
            String lobbyId = etLobbyId.getText().toString().trim();
            if (!lobbyId.isEmpty()) {
                if (userId == null || userName == null) {
                    Toast.makeText(requireContext(), "جاري تحميل بيانات المستخدم...", Toast.LENGTH_SHORT).show();
                    return;
                }
                joinLobby(lobbyId, "4v4");
            } else {
                Toast.makeText(requireContext(), "الرجاء إدخال رقم التحدي", Toast.LENGTH_SHORT).show();
            }
        });

        containerCard.addView(btnJoin);

        return card;
    }

    private void createNewLobby() {
        if (currentMatchType == null) {
            Toast.makeText(requireContext(), "الرجاء اختيار نوع المباراة", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == null || userName == null) {
            Toast.makeText(requireContext(), "جاري تحميل بيانات المستخدم...", Toast.LENGTH_SHORT).show();
            return;
        }

        String newLobbyId = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        joinLobby(newLobbyId, currentMatchType);
    }

    private void joinLobby(String lobbyId, String matchType) {
        if (userId == null || userId.isEmpty() || userName == null || userName.isEmpty()) {
            Toast.makeText(requireContext(), "خطأ في بيانات المستخدم", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        currentLobbyId = lobbyId;

        if (lobbyManager != null && lobbyManager.isConnected()) {
            lobbyManager.disconnect();
        }

        lobbyManager = new LobbyManager(new LobbyManager.LobbyListener() {
            @Override
            public void onConnected() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "✓ Connected to lobby");
                    });
                }
            }

            @Override
            public void onDisconnected() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "انقطع الاتصال", Toast.LENGTH_SHORT).show();
                        displayMainMenu();
                    });
                }
            }

            @Override
            public void onJoinedLobby(LobbyManager.LobbyData lobby, boolean leader) {
                Log.d(TAG, "✓ Joined lobby - Players: " + lobby.players.size() + "/" + lobby.maxPlayers + " | Leader: " + leader);

                isLeader = leader;
                currentLobby = lobby;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        isInLobby = true;
                        displayLobbyView();
                    });
                }
            }

            @Override
            public void onPlayerJoined(LobbyManager.LobbyData lobby) {
                Log.d(TAG, "✓ Player JOINED - Total: " + lobby.players.size() + "/" + lobby.maxPlayers);

                currentLobby = lobby;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Show toast notification
                        if (lobby.players.size() > 0) {
                            String lastPlayer = lobby.players.get(lobby.players.size() - 1).name;
                            Toast.makeText(requireContext(),
                                    "✓ " + lastPlayer + " انضم للغرفة",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // Update UI immediately
                        updateLobbyView();

                        // Check if lobby is full
                        if (lobby.isFull()) {
                            Toast.makeText(requireContext(),
                                    "🎮 التحدي ممتلئة - يمكن بدء المباراة!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onPlayerLeft(LobbyManager.LobbyData lobby) {
                Log.d(TAG, "✗ Player LEFT - Total: " + lobby.players.size() + "/" + lobby.maxPlayers);

                currentLobby = lobby;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                                "✗ لاعب غادر التحدي",
                                Toast.LENGTH_SHORT).show();

                        // Update UI immediately
                        updateLobbyView();
                    });
                }
            }

            @Override
            public void onMatchStarted(String matchId, LobbyManager.LobbyData lobby) {
                Log.d(TAG, "🎮 MATCH STARTED - ID: " + matchId);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                                "🎮 بدء المباراة...",
                                Toast.LENGTH_SHORT).show();

                        // Small delay for better UX
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            Intent intent = new Intent(requireContext(), LiveMatchActivity.class);
                            intent.putExtra("matchId", matchId);
                            intent.putExtra("lobbyId", currentLobbyId);
                            intent.putExtra("userId", userId);
                            intent.putExtra("userName", userName);
                            intent.putExtra("isLeader", isLeader);
                            startActivity(intent);

                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                        }, 500);
                    });
                }
            }

            @Override
            public void onPingUpdate(long ping) {
                if (isInLobby && getActivity() != null) {
                    getActivity().runOnUiThread(() -> updatePing(ping));
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "✗ ERROR: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });

        Log.d(TAG, "→ Connecting to lobby: " + lobbyId);
        lobbyManager.connect(lobbyId, userId, userName, matchType);
    }
    private void displayLobbyView() {
        contentContainer.removeAllViews();

        contentContainer.addView(createLobbyHeader());
        contentContainer.addView(createSpacing(1));
        contentContainer.addView(createLobbyInfoCard());
        contentContainer.addView(createSpacing(1));
        contentContainer.addView(createPlayersCard());
        contentContainer.addView(createSpacing(1));
        contentContainer.addView(createLobbyActions());

        // Add Dev Board if dev mode is enabled
        if (devMode) {
            contentContainer.addView(createSpacing(1));
            contentContainer.addView(createDevBoard());
        }
    }

    private void updateLobbyView() {
        if (!isInLobby || currentLobby == null) {
            Log.w(TAG, "updateLobbyView: Not in lobby or lobby is null");
            return;
        }

        Log.d(TAG, "→ Updating lobby view - Players: " + currentLobby.players.size() + "/" + currentLobby.maxPlayers);

        // Find and update specific cards
        for (int i = 0; i < contentContainer.getChildCount(); i++) {
            View view = contentContainer.getChildAt(i);
            Object tag = view.getTag();

            if ("lobby_info_card".equals(tag)) {
                contentContainer.removeViewAt(i);
                contentContainer.addView(createLobbyInfoCard(), i);
                Log.d(TAG, "  ✓ Updated info card");
            }
            else if ("players_card".equals(tag)) {
                contentContainer.removeViewAt(i);
                contentContainer.addView(createPlayersCard(), i);
                Log.d(TAG, "  ✓ Updated players card");
            }
            else if ("lobby_actions".equals(tag)) {
                contentContainer.removeViewAt(i);
                contentContainer.addView(createLobbyActions(), i);
                Log.d(TAG, "  ✓ Updated actions");
            }
            else if ("dev_board".equals(tag) && devMode) {
                contentContainer.removeViewAt(i);
                contentContainer.addView(createDevBoard(), i);
                Log.d(TAG, "  ✓ Updated dev board");
            }
        }
    }

    private View createLobbyHeader() {
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(dp(20), dp(24), dp(20), dp(20));
        header.setBackgroundColor(Color.WHITE);
        header.setGravity(Gravity.CENTER_VERTICAL);

        // Back Button
        ImageView backBtn = new ImageView(requireContext());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        backParams.setMarginEnd(dp(16));
        backBtn.setLayoutParams(backParams);
        backBtn.setOnClickListener(v -> leaveLobby());
        backBtn.setImageResource(android.R.drawable.ic_menu_revert);
        backBtn.setColorFilter(Color.parseColor("#000000"));

        header.addView(backBtn);

        // Title
        LinearLayout titleContainer = new LinearLayout(requireContext());
        titleContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        titleContainer.setLayoutParams(titleParams);

        TextView title = createText("التحدي", 22, "#000000", 3);
        titleContainer.addView(title);

        tvStatus = createText("متصل", 12, "#4CAF50", 1);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        statusParams.topMargin = dp(2);
        tvStatus.setLayoutParams(statusParams);
        titleContainer.addView(tvStatus);

        header.addView(titleContainer);

        return header;
    }

    private View createLobbyInfoCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(24), dp(20), dp(24), dp(20));
        card.setBackgroundColor(Color.WHITE);
        card.setTag("lobby_info_card");

        // Collapsible Header
        LinearLayout headerRow = new LinearLayout(requireContext());
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(0, 0, 0, dp(0));

        TextView sectionTitle = createText("معلومات التحدي", 16, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        sectionTitle.setLayoutParams(titleParams);
        headerRow.addView(sectionTitle);

        TextView arrowIcon = createText("▼", 14, "#666666", 1);
        arrowIcon.setTag("arrow_icon");
        headerRow.addView(arrowIcon);

        card.addView(headerRow);

        // Content Container
        LinearLayout contentView = new LinearLayout(requireContext());
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setTag("info_content");
        contentView.setVisibility(View.GONE);

        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        contentParams.topMargin = dp(16);
        contentView.setLayoutParams(contentParams);

        if (currentLobby != null) {
            contentView.addView(createInfoRow("رقم التحدي", currentLobby.lobbyId, true));
            contentView.addView(createInfoRow("نوع المباراة", currentLobby.matchType, false));
            contentView.addView(createInfoRow("عدد اللاعبين",
                    currentLobby.players.size() + "/" + currentLobby.maxPlayers, false));
            contentView.addView(createInfoRow("البينج", "-- ms", false));
        }

        card.addView(contentView);

        // Toggle functionality
        headerRow.setOnClickListener(v -> {
            boolean isExpanded = contentView.getVisibility() == View.VISIBLE;
            if (isExpanded) {
                contentView.setVisibility(View.GONE);
                arrowIcon.setText("▼");
            } else {
                contentView.setVisibility(View.VISIBLE);
                arrowIcon.setText("▲");
            }
        });

        return card;
    }

    private LinearLayout createInfoRow(String label, String value, boolean copyable) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(8);
        row.setLayoutParams(rowParams);

        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setColor(Color.parseColor("#FAFAFA"));
        rowBg.setCornerRadius(dp(10));
        row.setBackground(rowBg);

        if (label.equals("البينج")) {
            row.setTag("ping_row");
        }

        TextView labelTv = createText(label, 14, "#666666", 1);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        labelTv.setLayoutParams(labelParams);
        row.addView(labelTv);

        TextView valueTv = createText(value, 14, "#000000", 3);
        valueTv.setTag("value_text");
        row.addView(valueTv);

        if (copyable) {
            TextView copyIcon = createText(" ⎘", 16, "#666666", 1);
            copyIcon.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager)
                        requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Lobby ID", value);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "تم النسخ", Toast.LENGTH_SHORT).show();
            });
            row.addView(copyIcon);
        }

        return row;
    }

    private void updatePing(long ping) {
        View infoCard = contentContainer.findViewWithTag("lobby_info_card");
        if (infoCard instanceof ViewGroup) {
            ViewGroup card = (ViewGroup) infoCard;
            View contentView = card.findViewWithTag("info_content");
            if (contentView instanceof ViewGroup) {
                ViewGroup content = (ViewGroup) contentView;
                for (int i = 0; i < content.getChildCount(); i++) {
                    View child = content.getChildAt(i);
                    if ("ping_row".equals(child.getTag()) && child instanceof ViewGroup) {
                        ViewGroup row = (ViewGroup) child;
                        for (int j = 0; j < row.getChildCount(); j++) {
                            View v = row.getChildAt(j);
                            if ("value_text".equals(v.getTag()) && v instanceof TextView) {
                                ((TextView) v).setText(ping + " ms");
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private View createPlayersCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(24), dp(20), dp(24), dp(20));
        card.setBackgroundColor(Color.WHITE);
        card.setTag("players_card");

        TextView sectionTitle = createText("اللاعبون", 16, "#000000", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(16);
        sectionTitle.setLayoutParams(titleParams);
        card.addView(sectionTitle);

        if (currentLobby != null && currentLobby.players != null) {
            for (int i = 0; i < currentLobby.players.size(); i++) {
                card.addView(createPlayerItem(currentLobby.players.get(i), i == 0));
            }

            int emptySlots = currentLobby.maxPlayers - currentLobby.players.size();
            for (int i = 0; i < emptySlots; i++) {
                card.addView(createEmptySlot());
            }
        }

        return card;
    }

    private View createPlayerItem(LobbyManager.PlayerData player, boolean isLeader) {
        LinearLayout item = new LinearLayout(requireContext());
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(dp(16), dp(14), dp(16), dp(14));
        item.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        itemParams.bottomMargin = dp(8);
        item.setLayoutParams(itemParams);
        GradientDrawable itemBg = new GradientDrawable();
        itemBg.setColor(Color.parseColor("#FAFAFA"));
        itemBg.setCornerRadius(dp(12));
        itemBg.setStroke(dp(2), Color.parseColor("#000000"));
        item.setBackground(itemBg);

        // Avatar
        ImageView avatar = new ImageView(requireContext());
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        avatarParams.setMarginEnd(dp(12));
        avatar.setLayoutParams(avatarParams);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setClipToOutline(true);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#E0E0E0"));
        avatar.setBackground(avatarBg);

        if (player.avatar != null && !player.avatar.isEmpty()) {
            try {
                Glide.with(requireContext()).load(player.avatar).circleCrop().into(avatar);
            } catch (Exception e) {
                // Use default
            }
        }

        item.addView(avatar);

        // Info
        LinearLayout infoContainer = new LinearLayout(requireContext());
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        );
        infoContainer.setLayoutParams(infoParams);

        TextView name = createText(player.name, 15, "#000000", 3);
        infoContainer.addView(name);

        TextView level = createText("المستوى " + player.level, 12, "#666666", 1);
        LinearLayout.LayoutParams levelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        levelParams.topMargin = dp(2);
        level.setLayoutParams(levelParams);
        infoContainer.addView(level);

        item.addView(infoContainer);

        // Leader Badge
        if (isLeader) {
            TextView badge = createText("قائد", 12, "#000000", 3);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            badgeParams.setMarginStart(dp(8));
            badge.setLayoutParams(badgeParams);
            badge.setPadding(dp(8), dp(4), dp(8), dp(4));

            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(Color.parseColor("#FFC107"));
            badgeBg.setCornerRadius(dp(6));
            badge.setBackground(badgeBg);

            item.addView(badge);
        }

        return item;
    }

    private View createEmptySlot() {
        LinearLayout item = new LinearLayout(requireContext());
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(dp(16), dp(14), dp(16), dp(14));
        item.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        itemParams.bottomMargin = dp(8);
        item.setLayoutParams(itemParams);

        GradientDrawable itemBg = new GradientDrawable();
        itemBg.setColor(Color.parseColor("#FAFAFA"));
        itemBg.setCornerRadius(dp(12));
        item.setBackground(itemBg);

        View emptyAvatar = new View(requireContext());
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        avatarParams.setMarginEnd(dp(12));
        emptyAvatar.setLayoutParams(avatarParams);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#E0E0E0"));
        emptyAvatar.setBackground(avatarBg);

        item.addView(emptyAvatar);

        TextView placeholder = createText("في انتظار لاعب...", 14, "#999999", 1);
        item.addView(placeholder);

        return item;
    }

    private View createLobbyActions() {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(20), dp(24), dp(20));
        container.setBackgroundColor(Color.WHITE);
        container.setTag("lobby_actions");

        if (isLeader) {
            LinearLayout btnStart = new LinearLayout(requireContext());
            btnStart.setOrientation(LinearLayout.HORIZONTAL);
            btnStart.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(56)
            );
            startParams.bottomMargin = dp(12);
            btnStart.setLayoutParams(startParams);

            boolean canStart = currentLobby != null && currentLobby.isFull();

            TextView btnText = createText("", 17, "#FFFFFF", 3);
            if (canStart) {
                btnText.setText("🎮 بدء المباراة الآن!");
            } else {
                int needed = currentLobby != null ? (currentLobby.maxPlayers - currentLobby.players.size()) : 0;
                btnText.setText("في انتظار " + needed + " لاعب...");
            }
            btnStart.addView(btnText);

            GradientDrawable startBg = new GradientDrawable();
            if (canStart) {
                startBg.setColor(Color.parseColor("#4CAF50"));
                ValueAnimator anim = ValueAnimator.ofFloat(1.0f, 1.05f, 1.0f);
                anim.setDuration(1000);
                anim.setRepeatCount(ValueAnimator.INFINITE);
                anim.addUpdateListener(animation -> {
                    float scale = (float) animation.getAnimatedValue();
                    btnStart.setScaleX(scale);
                    btnStart.setScaleY(scale);
                });
                anim.start();
            } else {
                startBg.setColor(Color.parseColor("#CCCCCC"));
            }
            startBg.setCornerRadius(dp(16));
            btnStart.setBackground(startBg);
            btnStart.setEnabled(canStart);

            btnStart.setOnTouchListener((v, e) -> {
                if (!canStart) return false;
                switch (e.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.animate()
                                .scaleX(0.95f)
                                .scaleY(0.95f)
                                .setDuration(100)
                                .start();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .setInterpolator(new android.view.animation.OvershootInterpolator())
                                .start();
                        break;
                }
                return false;
            });

            btnStart.setOnClickListener(v -> {
                if (lobbyManager != null && currentLobby != null && currentLobby.isFull()) {
                    v.setEnabled(false);
                    TextView text = (TextView) ((LinearLayout) v).getChildAt(0);
                    text.setText("جاري البدء...");
                    Toast.makeText(requireContext(), "🎮 جاري بدء المباراة...", Toast.LENGTH_SHORT).show();
                    lobbyManager.startMatch(currentLobbyId, userId);
                }
            });

            container.addView(btnStart);
        } else {
            TextView waitingMsg = createText("⏳ في انتظار القائد لبدء المباراة...", 14, "#666666", 1);
            waitingMsg.setGravity(Gravity.CENTER);
            waitingMsg.setPadding(dp(16), dp(12), dp(16), dp(12));

            LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            msgParams.bottomMargin = dp(12);
            waitingMsg.setLayoutParams(msgParams);

            GradientDrawable msgBg = new GradientDrawable();
            msgBg.setColor(Color.parseColor("#FFF3E0"));
            msgBg.setCornerRadius(dp(12));
            waitingMsg.setBackground(msgBg);

            container.addView(waitingMsg);
        }

        LinearLayout btnShare = new LinearLayout(requireContext());
        btnShare.setOrientation(LinearLayout.HORIZONTAL);
        btnShare.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        btnShare.setLayoutParams(shareParams);

        GradientDrawable shareBg = new GradientDrawable();
        shareBg.setColor(Color.parseColor("#F0F0F0"));
        shareBg.setCornerRadius(dp(16));
        btnShare.setBackground(shareBg);

        TextView shareText = createText("📤 مشاركة رقم التحدي", 16, "#000000", 3);
        btnShare.addView(shareText);

        btnShare.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .setInterpolator(new android.view.animation.OvershootInterpolator())
                            .start();
                    break;
            }
            return false;
        });

        btnShare.setOnClickListener(v -> shareLobbyId());

        container.addView(btnShare);

        return container;
    }
    private View createDevBoard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(24), dp(20), dp(24), dp(20));
        card.setTag("dev_board");

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#FFF3E0"));
        cardBg.setCornerRadius(dp(12));
        cardBg.setStroke(dp(2), Color.parseColor("#FF9800"));
        card.setBackground(cardBg);

        TextView title = createText("🛠️ Developer Board", 16, "#E65100", 3);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dp(12);
        title.setLayoutParams(titleParams);
        card.addView(title);

        // Status Info
        if (currentLobby != null) {
            TextView info = createText(
                    "🆔 Lobby: " + currentLobbyId + "\n" +
                            "👥 Players: " + currentLobby.players.size() + "/" + currentLobby.maxPlayers + "\n" +
                            "📊 Status: " + currentLobby.status + "\n" +
                            "👑 Leader: " + (isLeader ? "YES ✓" : "NO") + "\n" +
                            "🤖 Fake: " + fakeManagers.size(),
                    12, "#424242", 1
            );
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            infoParams.bottomMargin = dp(12);
            info.setLayoutParams(infoParams);
            info.setPadding(dp(12), dp(10), dp(12), dp(10));

            GradientDrawable infoBg = new GradientDrawable();
            infoBg.setColor(Color.parseColor("#FFFFFF"));
            infoBg.setCornerRadius(dp(8));
            info.setBackground(infoBg);

            card.addView(info);
        }

        // Quick Add Row
        LinearLayout quickRow = new LinearLayout(requireContext());
        quickRow.setOrientation(LinearLayout.HORIZONTAL);
        quickRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(8);
        quickRow.setLayoutParams(rowParams);

        Button btn1 = createDevButton("+1");
        btn1.setOnClickListener(v -> addFakePlayers(1));
        quickRow.addView(btn1);

        Button btn2 = createDevButton("+2");
        btn2.setOnClickListener(v -> addFakePlayers(2));
        quickRow.addView(btn2);

        Button btn3 = createDevButton("+3");
        btn3.setOnClickListener(v -> addFakePlayers(3));
        quickRow.addView(btn3);

        Button btnFill = createDevButton("Fill");
        btnFill.setOnClickListener(v -> {
            if (currentLobby != null) {
                int needed = currentLobby.maxPlayers - currentLobby.players.size();
                if (needed > 0) {
                    addFakePlayers(needed);
                } else {
                    Toast.makeText(requireContext(), "Lobby Full!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        quickRow.addView(btnFill);

        card.addView(quickRow);

        // Control Row
        LinearLayout controlRow = new LinearLayout(requireContext());
        controlRow.setOrientation(LinearLayout.HORIZONTAL);
        controlRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams controlParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        controlParams.bottomMargin = dp(8);
        controlRow.setLayoutParams(controlParams);

        Button btnRemove = createDevButton("Remove");
        btnRemove.setOnClickListener(v -> removeLastFakePlayer());
        controlRow.addView(btnRemove);

        Button btnClear = createDevButton("Clear All");
        btnClear.setOnClickListener(v -> clearAllFakePlayers());
        controlRow.addView(btnClear);

        Button btnRefresh = createDevButton("↻ Refresh");
        btnRefresh.setOnClickListener(v -> {
            updateLobbyView();
            Toast.makeText(requireContext(), "Refreshed!", Toast.LENGTH_SHORT).show();
        });
        controlRow.addView(btnRefresh);

        card.addView(controlRow);

        // Match Control
        Button btnForceStart = new Button(requireContext());
        btnForceStart.setText("🎮 START MATCH NOW");
        btnForceStart.setTextColor(Color.WHITE);
        btnForceStart.setTextSize(13);
        btnForceStart.setAllCaps(false);
        if (ThemeManager.fontBold() != null) {
            btnForceStart.setTypeface(ThemeManager.fontBold());
        }

        LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
        );
        btnForceStart.setLayoutParams(startParams);

        GradientDrawable startBg = new GradientDrawable();
        startBg.setColor(Color.parseColor("#4CAF50"));
        startBg.setCornerRadius(dp(10));
        btnForceStart.setBackground(startBg);

        btnForceStart.setOnClickListener(v -> forceStartMatch());

        card.addView(btnForceStart);

        return card;
    }

    private Button createDevButton(String text) {
        Button btn = new Button(requireContext());
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#FFFFFF"));
        btn.setTextSize(11);
        btn.setAllCaps(false);
        if (ThemeManager.fontBold() != null) {
            btn.setTypeface(ThemeManager.fontBold());
        }

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0, dp(36), 1
        );
        btnParams.setMargins(dp(4), 0, dp(4), 0);
        btn.setLayoutParams(btnParams);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#FF9800"));
        bg.setCornerRadius(dp(8));
        btn.setBackground(bg);

        return btn;
    }


    // في أول الـ class، ضيف:
    private List<LobbyManager> fakeManagers = new ArrayList<>();
    private boolean autoUpdateEnabled = true;

    // عدّل الـ addFakePlayers method:
    private void addFakePlayers(int count) {
        if (currentLobbyId == null || currentLobby == null) {
            Toast.makeText(requireContext(), "Not in lobby", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = {"أحمد", "محمد", "علي", "حسن", "خالد", "سالم", "فهد", "ناصر", "عبدالله", "يوسف"};
        String[] avatars = {
                "https://i.pravatar.cc/150?img=1",
                "https://i.pravatar.cc/150?img=2",
                "https://i.pravatar.cc/150?img=3",
                "https://i.pravatar.cc/150?img=4",
                "https://i.pravatar.cc/150?img=5"
        };

        for (int i = 0; i < count; i++) {
            String fakeName = names[(int)(Math.random() * names.length)];
            String fakeAvatar = avatars[(int)(Math.random() * avatars.length)];
            String fakeUserId = "fake_" + System.currentTimeMillis() + "_" + (Math.random() * 1000);

            // Fake player listener الي بيعمل update للـ UI الرئيسية
            LobbyManager fakeManager = new LobbyManager(new LobbyManager.LobbyListener() {
                @Override
                public void onConnected() {
                    Log.d("FakePlayer", "Fake player connected: " + fakeName);
                }

                @Override
                public void onDisconnected() {
                    Log.d("FakePlayer", "Fake player disconnected: " + fakeName);
                }

                @Override
                public void onJoinedLobby(LobbyManager.LobbyData lobby, boolean isLeader) {
                    Log.d("FakePlayer", "Fake player joined: " + fakeName);
                    // Update main UI
                    if (getActivity() != null && autoUpdateEnabled) {
                        getActivity().runOnUiThread(() -> {
                            currentLobby = lobby;
                            updateLobbyView();
                        });
                    }
                }

                @Override
                public void onPlayerJoined(LobbyManager.LobbyData lobby) {
                    Log.d("FakePlayer", "Another player joined (from " + fakeName + " perspective)");
                    // Update main UI
                    if (getActivity() != null && autoUpdateEnabled) {
                        getActivity().runOnUiThread(() -> {
                            currentLobby = lobby;
                            updateLobbyView();
                        });
                    }
                }

                @Override
                public void onPlayerLeft(LobbyManager.LobbyData lobby) {
                    Log.d("FakePlayer", "Player left (from " + fakeName + " perspective)");
                    // Update main UI
                    if (getActivity() != null && autoUpdateEnabled) {
                        getActivity().runOnUiThread(() -> {
                            currentLobby = lobby;
                            updateLobbyView();
                        });
                    }
                }

                @Override
                public void onMatchStarted(String matchId, LobbyManager.LobbyData lobby) {
                    Log.d("FakePlayer", "Match started for fake player: " + fakeName);
                }

                @Override
                public void onPingUpdate(long ping) {
                    // Ignore ping for fake players
                }

                @Override
                public void onError(String error) {
                    Log.e("FakePlayer", "Error for " + fakeName + ": " + error);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Fake player error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

            fakeManagers.add(fakeManager);

            // Add delay between connections
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                fakeManager.connect(currentLobbyId, fakeUserId, fakeName, currentLobby.matchType);
            }, i * 300); // 300ms delay between each fake player
        }

        Toast.makeText(requireContext(), "Adding " + count + " player(s)...", Toast.LENGTH_SHORT).show();
    }

    private void removeLastFakePlayer() {
        if (fakeManagers.isEmpty()) {
            Toast.makeText(requireContext(), "No fake players", Toast.LENGTH_SHORT).show();
            return;
        }

        LobbyManager lastManager = fakeManagers.remove(fakeManagers.size() - 1);
        lastManager.disconnect();
        Toast.makeText(requireContext(), "Removed 1 player", Toast.LENGTH_SHORT).show();

        // Force update after 500ms
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isInLobby) updateLobbyView();
        }, 500);
    }

    private void clearAllFakePlayers() {
        if (fakeManagers.isEmpty()) {
            Toast.makeText(requireContext(), "No fake players", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = fakeManagers.size();

        // Disconnect all with delay
        for (int i = 0; i < fakeManagers.size(); i++) {
            final int index = i;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (index < fakeManagers.size()) {
                    fakeManagers.get(index).disconnect();
                }
            }, i * 200); // 200ms delay between each disconnect
        }

        fakeManagers.clear();
        Toast.makeText(requireContext(), "Removing " + count + " player(s)...", Toast.LENGTH_SHORT).show();

        // Force update after all disconnects
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isInLobby) updateLobbyView();
        }, count * 200 + 500);
    }

    private void forceStartMatch() {
        if (currentLobbyId == null || userId == null) {
            Toast.makeText(requireContext(), "Not in lobby", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentLobby != null && currentLobby.players.size() < 2) {
            Toast.makeText(requireContext(), "Need at least 2 players", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lobbyManager != null && lobbyManager.isConnected()) {
            lobbyManager.startMatch(currentLobbyId, userId);
            Toast.makeText(requireContext(), "Starting match...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Not connected", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareLobbyId() {
        if (currentLobbyId != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "انضم لغرفتي: " + currentLobbyId);
            startActivity(Intent.createChooser(shareIntent, "مشاركة رقم التحدي"));
        }
    }

    private void leaveLobby() {
        if (lobbyManager != null && lobbyManager.isConnected()) {
            lobbyManager.disconnect();
        }
        displayMainMenu();
    }

    private TextView createText(String text, int size, String color, int weight) {
        return UiHelper.createText(requireContext(), text, size, color, weight);
    }

    private TextView createTextShadow(String text, int size, String color, int weight, String colorShadow, float r,float dx, float dy) {
        return UiHelper.createTextShadow(requireContext(), text, size, color, weight, colorShadow, r, dx, dy);
    }

    private int dp(int value) {
        return UiHelper.dp(requireContext(), value);
    }

    private View createSpacing(int dpValue) {
        return UiHelper.createSpacing(requireContext(), dpValue, "#F0F0F0");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Disconnect fake players
        for (LobbyManager manager : fakeManagers) {
            manager.disconnect();
        }
        fakeManagers.clear();

        // Disconnect main connection
        if (lobbyManager != null && lobbyManager.isConnected()) {
            lobbyManager.disconnect();
        }
    }
}