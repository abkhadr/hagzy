package hagzy.layouts.profile.sections;

import android.view.View;

import hagzy.layouts.profile.utils.PlayerDataParser.PlayerData;

/**
 * ProfileSection - واجهة لجميع أقسام الملف الشخصي
 */
public interface ProfileSection {

    /**
     * Get the section view
     */
    View getView();

    /**
     * Set player data to display
     */
    void setData(PlayerData data);
}