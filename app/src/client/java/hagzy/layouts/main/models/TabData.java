package hagzy.layouts.main.models;

import androidx.fragment.app.Fragment;

/**
 * نموذج بيانات التبويب
 */
public class TabData {

    private int id;
    private String title;
    private Fragment fragment;
    private int iconRes;

    public TabData(int id, String title, Fragment fragment) {
        this.id = id;
        this.title = title;
        this.fragment = fragment;
        this.iconRes = 0;
    }

    public TabData(int id,String title, Fragment fragment, int iconRes) {
        this.id = id;
        this.title = title;
        this.fragment = fragment;
        this.iconRes = iconRes;
    }

    public String getTitle() {
        return title;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public int getIconRes() {
        return iconRes;
    }

    public boolean hasIcon() {
        return iconRes != 0;
    }
}