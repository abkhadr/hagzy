package hagzy.layouts.settings.cards;

import static com.bytepulse.hagzy.helpers.TranslationManager.t;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import com.bytepulse.hagzy.R;
import com.bytepulse.hagzy.helpers.ThemeManager;
import com.bytepulse.hagzy.helpers.UiHelper;

public class SearchCard {

    private final Context context;
    private CardView card;
    private EditText searchInput;
    private OnSearchListener searchListener;

    public interface OnSearchListener {
        void onSearchTextChanged(String query);
    }

    public SearchCard(Context context) {
        this.context = context;
        buildCard();
    }

    private void buildCard() {
        // Card (without extra container)
        card = new CardView(context);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(dp(20), dp(12), dp(20), dp(20));
        card.setLayoutParams(cardParams);
        card.setRadius(dp(24));
        card.setCardElevation(0);
        card.setCardBackgroundColor(Color.parseColor("#F5F5F5"));

        // Content Layout
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(dp(18), dp(14), dp(18), dp(14));

        // Search Icon
        ImageView searchIcon = new ImageView(context);
        searchIcon.setImageResource(R.drawable.magnifying_glass);
        searchIcon.setColorFilter(Color.parseColor("#999999"));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        iconParams.setMarginEnd(dp(12));
        searchIcon.setLayoutParams(iconParams);

        // Search Input
        searchInput = new EditText(context);
        searchInput.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));
        searchInput.setHint(t("settings.search_hint"));
        searchInput.setTextSize(15);
        searchInput.setTypeface(ThemeManager.fontSemiBold());
        searchInput.setTextColor(Color.parseColor("#1A1A1A"));
        searchInput.setHintTextColor(Color.parseColor("#999999"));
        searchInput.setBackground(null);
        searchInput.setPadding(0, 0, 0, 0);
        searchInput.setSingleLine(true);
        searchInput.setTranslationY(-dpf(1f));

        // Add text change listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchListener != null) {
                    searchListener.onSearchTextChanged(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        content.addView(searchIcon);
        content.addView(searchInput);

        card.addView(content);
    }

    public void setOnSearchListener(OnSearchListener listener) {
        this.searchListener = listener;
    }

    public CardView getView() {
        return card;
    }

    public void clearSearch() {
        searchInput.setText("");
    }

    private int dp(int dpValue) {
        return UiHelper.dp(context, dpValue);
    }

    private float dpf(float value) {
        return UiHelper.dpFloat(context, value);
    }
}