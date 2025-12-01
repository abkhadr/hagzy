package hagzy.fragments.tabs;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bytepulse.hagzy.helpers.UiHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import hagzy.layouts.main.models.TabData;
import hagzy.layouts.profile.PlayerProfileManager;
import hagzy.layouts.profile.utils.PlayerDataParser;
import hagzy.layouts.profile.utils.PlayerDataParser.PlayerData;

public class PlayerProfileFragment extends Fragment {

    private static final String TAG = "PlayerProfileFragment";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    private PlayerProfileManager profileManager;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setupInit();
        setupFirebase();
        return buildUI();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPlayerProfile();
    }

    // ════════════════════════════════════════════════════════════
    // 🎯 Setup Methods
    // ════════════════════════════════════════════════════════════

    private void setupInit() {
        // Initialize will be done here
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
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
        int tabId = getArguments() != null ? getArguments().getInt("tab_id", -1) : -1;

        // Profile Manager
        profileManager = new PlayerProfileManager(requireContext(), tabId);
        profileManager.getView().setVisibility(View.GONE);
        mainContainer.addView(profileManager.getView());

        // Loading State
        progressBar = createProgressBar();
        mainContainer.addView(progressBar);

        scrollView.addView(mainContainer);
        root.addView(scrollView);

        return root;
    }

    // ════════════════════════════════════════════════════════════
    // 📡 Data Loading
    // ════════════════════════════════════════════════════════════

    private void loadPlayerProfile() {
        showLoading();

        db.collection("players")
                .document(userId)
                .get()
                .addOnSuccessListener(this::onDataLoaded)
                .addOnFailureListener(this::onDataError);
    }

    private void onDataLoaded(DocumentSnapshot document) {
        hideLoading();

        PlayerData playerData;

        if (!document.exists()) {
            // مستخدم جديد - إنشاء بيانات افتراضية
            Log.d(TAG, "Player document does not exist - creating default data");
            playerData = PlayerDataParser.createDefaultData(
                    userId,
                    mAuth.getCurrentUser().getDisplayName(),
                    mAuth.getCurrentUser().getEmail(),
                    mAuth.getCurrentUser().getPhotoUrl() != null ?
                            mAuth.getCurrentUser().getPhotoUrl().toString() : null
            );
        } else {
            // بيانات موجودة
            playerData = PlayerDataParser.parse(document);
        }

        displayProfile(playerData);
    }

    private void onDataError(Exception e) {
        hideLoading();
        Log.e(TAG, "Error loading profile", e);

        // عرض بيانات افتراضية عند الخطأ
        PlayerData playerData = PlayerDataParser.createDefaultData(
                userId,
                mAuth.getCurrentUser().getDisplayName(),
                mAuth.getCurrentUser().getEmail(),
                mAuth.getCurrentUser().getPhotoUrl() != null ?
                        mAuth.getCurrentUser().getPhotoUrl().toString() : null
        );
        displayProfile(playerData);
    }

    private void displayProfile(PlayerData data) {
        profileManager.displayProfile(data);
        profileManager.getView().setVisibility(View.VISIBLE);

        // إظهار Coach Marks بعد تأخير بسيط
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            profileManager.showCoachMarksIfNeeded(requireContext(), getActivity());
        }, 600);
    }

    // ════════════════════════════════════════════════════════════
    // 🎨 UI Components
    // ════════════════════════════════════════════════════════════

    private ProgressBar createProgressBar() {
        ProgressBar progress = new ProgressBar(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp(40), dp(40)
        );
        params.gravity = Gravity.CENTER;
        params.topMargin = dp(100);
        progress.setLayoutParams(params);
        return progress;
    }

    // ════════════════════════════════════════════════════════════
    // 📊 State Management
    // ════════════════════════════════════════════════════════════

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        profileManager.getView().setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    // ════════════════════════════════════════════════════════════
    // 🛠️ Helpers
    // ════════════════════════════════════════════════════════════

    private int dp(int value) {
        return UiHelper.dp(requireContext(), value);
    }
}