package hagzy.layouts.field.sections;

import android.view.View;

import hagzy.layouts.field.utils.FieldDataParser.FieldData;

/**
 * FieldSection - واجهة لجميع أقسام صفحة الملعب
 */
public interface FieldSection {

    /**
     * Get the section view
     */
    View getView();

    /**
     * Set field data to display
     */
    void setData(FieldData data);
}