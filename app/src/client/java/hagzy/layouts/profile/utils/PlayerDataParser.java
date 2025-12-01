package hagzy.layouts.profile.utils;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PlayerDataParser - معالج بيانات اللاعب من Firestore
 */
public class PlayerDataParser {

    public static PlayerData parse(DocumentSnapshot doc) {
        PlayerData data = new PlayerData();

        // Basic Info
        data.userId = doc.getId();
        data.name = doc.getString("name");
        data.email = doc.getString("email");
        data.avatar = doc.getString("avatar");
        data.hasData = true; // البيانات موجودة

        // Profile
        Map<String, Object> profile = (Map<String, Object>) doc.get("profile");
        if (profile != null) {
            data.preferredPosition = (String) profile.get("preferredPosition");
            data.level = profile.get("level") != null ?
                    ((Long) profile.get("level")).intValue() : 1;
            data.xp = profile.get("xp") != null ?
                    ((Long) profile.get("xp")).intValue() : 0;
            data.xpToNextLevel = profile.get("xpToNextLevel") != null ?
                    ((Long) profile.get("xpToNextLevel")).intValue() : 100;
            data.rating = profile.get("rating") != null ?
                    ((Number) profile.get("rating")).doubleValue() : 0.0;
            data.title = (String) profile.get("title");
            data.strength = profile.get("strength") != null ?
                    ((Long) profile.get("strength")).intValue() : 50;
            data.speed = profile.get("speed") != null ?
                    ((Long) profile.get("speed")).intValue() : 50;
            data.stamina = profile.get("stamina") != null ?
                    ((Long) profile.get("stamina")).intValue() : 50;
            data.technique = profile.get("technique") != null ?
                    ((Long) profile.get("technique")).intValue() : 50;
            data.teamwork = profile.get("teamwork") != null ?
                    ((Long) profile.get("teamwork")).intValue() : 50;
        }

        // Stats
        Map<String, Object> stats = (Map<String, Object>) doc.get("stats");
        if (stats != null) {
            data.totalMatches = stats.get("totalMatches") != null ?
                    ((Long) stats.get("totalMatches")).intValue() : 0;
            data.wins = stats.get("wins") != null ?
                    ((Long) stats.get("wins")).intValue() : 0;
            data.losses = stats.get("losses") != null ?
                    ((Long) stats.get("losses")).intValue() : 0;
            data.draws = stats.get("draws") != null ?
                    ((Long) stats.get("draws")).intValue() : 0;
            data.goals = stats.get("goals") != null ?
                    ((Long) stats.get("goals")).intValue() : 0;
            data.assists = stats.get("assists") != null ?
                    ((Long) stats.get("assists")).intValue() : 0;
            data.winRate = stats.get("winRate") != null ?
                    ((Number) stats.get("winRate")).doubleValue() : 0.0;
        }

        // Achievements
        List<Map<String, Object>> achievementsList = (List<Map<String, Object>>) doc.get("achievements");
        if (achievementsList != null) {
            data.achievements = new ArrayList<>();
            for (Map<String, Object> achievement : achievementsList) {
                Achievement ach = new Achievement();
                ach.id = (String) achievement.get("id");
                ach.name = (String) achievement.get("name");
                ach.description = (String) achievement.get("description");
                ach.unlocked = achievement.get("unlocked") != null ?
                        (Boolean) achievement.get("unlocked") : false;
                data.achievements.add(ach);
            }
        }

        return data;
    }

    /**
     * Create default data for new users
     */
    public static PlayerData createDefaultData(String userId, String name, String email, String avatar) {
        PlayerData data = new PlayerData();

        // Basic Info
        data.userId = userId;
        data.name = name != null ? name : "لاعب جديد";
        data.email = email;
        data.avatar = avatar;
        data.hasData = false; // بيانات افتراضية

        // Profile - Default values
        data.preferredPosition = null; // سيعرض "غير محدد"
        data.level = 1;
        data.xp = 0;
        data.xpToNextLevel = 100;
        data.rating = 0.0;
        data.title = "مبتدئ";
        data.strength = 50;
        data.speed = 50;
        data.stamina = 50;
        data.technique = 50;
        data.teamwork = 50;

        // Stats - All zeros
        data.totalMatches = 0;
        data.wins = 0;
        data.losses = 0;
        data.draws = 0;
        data.goals = 0;
        data.assists = 0;
        data.winRate = 0.0;

        // Achievements - Empty
        data.achievements = new ArrayList<>();

        return data;
    }

    /**
     * PlayerData - بيانات اللاعب
     */
    public static class PlayerData {
        // Basic Info
        public String userId;
        public String name;
        public String email;
        public String avatar;
        public boolean hasData; // هل البيانات موجودة أم افتراضية

        // Profile
        public String preferredPosition;
        public String title;
        public int level;
        public int xp;
        public int xpToNextLevel;
        public double rating;
        public int strength;
        public int speed;
        public int stamina;
        public int technique;
        public int teamwork;

        // Stats
        public int totalMatches;
        public int wins;
        public int losses;
        public int draws;
        public int goals;
        public int assists;
        public double winRate;

        // Achievements
        public List<Achievement> achievements;
    }

    /**
     * Achievement - إنجاز
     */
    public static class Achievement {
        public String id;
        public String name;
        public String description;
        public boolean unlocked;
    }
}