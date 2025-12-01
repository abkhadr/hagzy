package hagzy.layouts.profile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;

import com.bytepulse.hagzy.helpers.CoachMarkHelper;

import hagzy.activities.MainActivity;
import hagzy.layouts.main.models.TabData;
import hagzy.layouts.profile.sections.ProfileHeaderSection;
import hagzy.layouts.profile.sections.PositionSection;
import hagzy.layouts.profile.sections.LevelSection;
import hagzy.layouts.profile.sections.StatsSection;
import hagzy.layouts.profile.sections.SkillsSection;
import hagzy.layouts.profile.sections.AchievementsSection;
import hagzy.layouts.profile.utils.PlayerDataParser.PlayerData;
import hagzy.layouts.profile.components.ProfileSpacing;

/**
 * PlayerProfileManager - مدير محتوى صفحة الملف الشخصي
 */
public class PlayerProfileManager {

    private static final String COACH_KEY_PROFILE = "profile_onboarding_shown_v1";

    private Context context;
    private LinearLayout container;

    // Sections
    private ProfileHeaderSection headerSection;
    private LevelSection levelSection;
    private PositionSection positionSection;
    private StatsSection statsSection;
    private SkillsSection skillsSection;
    private AchievementsSection achievementsSection;
    private int tabIndex;
    public PlayerProfileManager(Context context, int tabIndex) {
        this.context = context;
        this.tabIndex = tabIndex;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        // Initialize sections
        headerSection = new ProfileHeaderSection(context);
        levelSection = new LevelSection(context);
        positionSection = new PositionSection(context);
        statsSection = new StatsSection(context);
        skillsSection = new SkillsSection(context);
        achievementsSection = new AchievementsSection(context);

        // Add sections to container
        container.addView(headerSection.getView());
        container.addView(ProfileSpacing.create(context, 1));
        container.addView(levelSection.getView());
        container.addView(ProfileSpacing.create(context, 1));
        container.addView(positionSection.getView());
        container.addView(ProfileSpacing.create(context, 1));
        container.addView(statsSection.getView());
        container.addView(ProfileSpacing.create(context, 1));
        container.addView(skillsSection.getView());
        container.addView(ProfileSpacing.create(context, 1));
        container.addView(achievementsSection.getView());
    }

    /**
     * Display player profile data
     */
    public void displayProfile(PlayerData data) {
        headerSection.setData(data);
        levelSection.setData(data);
        positionSection.setData(data);
        statsSection.setData(data);
        skillsSection.setData(data);
        achievementsSection.setData(data);
    }

    /**
     * Get the main container view
     */
    public View getView() {
        return container;
    }

    /**
     * Show coach marks if needed
     */
    public void showCoachMarksIfNeeded(Context context, Activity activity) {
        if (activity == null && tabIndex != 0) return;

        SharedPreferences prefs = context.getSharedPreferences("CoachMarkPrefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean(COACH_KEY_PROFILE, false)) {
            return; // Already shown
        }

        startProfileCoachMarks(context, activity, prefs);
    }

    private void startProfileCoachMarks(Context context, Activity activity, SharedPreferences prefs) {
        CoachMarkHelper coach = new CoachMarkHelper(activity);

        // 1. Level Card
        coach.addStep(
                levelSection.getView(),
                "المستوى والتقدم",
                "يعرض مستواك الحالي ونقاط الخبرة (XP). اربح XP من المباريات لترتفع في المستويات!",
                2
        );

        // 2. Stats Card
        coach.addStep(
                positionSection.getView(),
                "مركزك داخل الملعب",
                "تعرّف على مركزك الأساسي داخل الفريق وكيف يؤثر دورك على طريقة اللعب. تابع تطورك للوصول إلى أداء أكثر احترافية.",
                1
        );

        // 3. Stats Card
        coach.addStep(
                statsSection.getView(),
                "إحصائياتك",
                "تابع عدد المباريات، الانتصارات، الأهداف، والتمريرات الحاسمة. كل مباراة تساهم في سجلك!",
                1
        );


        // 4. Skills Card
        coach.addStep(
                skillsSection.getView(),
                "مهاراتك",
                "قيّم نفسك في 5 مهارات أساسية: القوة، السرعة، التحمل، المهارة الفنية، والعمل الجماعي",
                0
        );

        // 5. Achievements Card
        coach.addStep(
                achievementsSection.getView(),
                "الإنجازات",
                "اجمع الإنجازات عبر اللعب والفوز في المباريات. كل إنجاز يعطيك مكافآت خاصة!",
                1
        );

        coach.setOnCompleteListener(() -> {
            prefs.edit().putBoolean(COACH_KEY_PROFILE, true).apply();
        });

        coach.start();
    }
}