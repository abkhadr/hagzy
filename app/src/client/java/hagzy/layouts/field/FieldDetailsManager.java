package hagzy.layouts.field;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import hagzy.layouts.field.sections.HeroSection;
import hagzy.layouts.field.sections.InfoSection;
import hagzy.layouts.field.sections.PricingSection;
import hagzy.layouts.field.sections.FeaturesSection;
import hagzy.layouts.field.sections.ReviewsSection;
import hagzy.layouts.field.sections.MapSection;
import hagzy.layouts.field.utils.FieldDataParser.FieldData;
import hagzy.layouts.field.components.BookingButton;

/**
 * FieldDetailsManager - مدير محتوى صفحة تفاصيل الملعب
 */
public class FieldDetailsManager {

    private Context context;
    private LinearLayout container;

    // Sections
    private HeroSection heroSection;
    private InfoSection infoSection;
    private PricingSection pricingSection;
    private FeaturesSection featuresSection;
    private ReviewsSection reviewsSection;
    private MapSection mapSection;
    private BookingButton bookingButton;

    public FieldDetailsManager(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        // Initialize sections
        heroSection = new HeroSection(context);
        infoSection = new InfoSection(context);
        pricingSection = new PricingSection(context);
        featuresSection = new FeaturesSection(context);
        reviewsSection = new ReviewsSection(context);
        mapSection = new MapSection(context);
        bookingButton = new BookingButton(context);

        // Add sections to container
        container.addView(heroSection.getView());
        container.addView(infoSection.getView());
        container.addView(pricingSection.getView());
        container.addView(featuresSection.getView());
        container.addView(reviewsSection.getView());
        container.addView(mapSection.getView());
        container.addView(bookingButton.getView());
    }

    /**
     * Display field data in all sections
     */
    public void displayField(FieldData data) {
        heroSection.setData(data);
        infoSection.setData(data);
        pricingSection.setData(data);
        featuresSection.setData(data);
        reviewsSection.setData(data);
        mapSection.setData(data);
        bookingButton.setFieldId(data.id);
    }

    /**
     * Get the main container view
     */
    public View getView() {
        return container;
    }
}