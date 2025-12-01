package hagzy.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import hagzy.helpers.LobbyManager;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayerViewHolder> {

    private List<LobbyManager.PlayerData> players;

    public PlayersAdapter(List<LobbyManager.PlayerData> players) {
        this.players = players;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout itemView = new LinearLayout(parent.getContext());
        itemView.setOrientation(LinearLayout.HORIZONTAL);
        itemView.setGravity(Gravity.CENTER_VERTICAL);
        itemView.setPadding(dpToPx(12, parent), dpToPx(12, parent),
                dpToPx(12, parent), dpToPx(12, parent));
        itemView.setElevation(dpToPx(4, parent));

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        itemParams.bottomMargin = dpToPx(8, parent);
        itemView.setLayoutParams(itemParams);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#2d2d44"));
        bg.setCornerRadius(dpToPx(12, parent));
        bg.setStroke(dpToPx(1, parent), Color.parseColor("#404050"));
        itemView.setBackground(bg);

        // Avatar
        ImageView avatar = new ImageView(parent.getContext());
        avatar.setId(View.generateViewId());
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(
                dpToPx(48, parent), dpToPx(48, parent)
        );
        avatar.setLayoutParams(avatarParams);

        // Text Container
        LinearLayout textContainer = new LinearLayout(parent.getContext());
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        textParams.leftMargin = dpToPx(12, parent);
        textContainer.setLayoutParams(textParams);

        // Name
        TextView name = new TextView(parent.getContext());
        name.setId(View.generateViewId());
        name.setTextColor(Color.WHITE);
        name.setTextSize(16);

        // Level
        TextView level = new TextView(parent.getContext());
        level.setId(View.generateViewId());
        level.setTextColor(Color.parseColor("#b0b0b0"));
        level.setTextSize(14);

        textContainer.addView(name);
        textContainer.addView(level);

        itemView.addView(avatar);
        itemView.addView(textContainer);

        return new PlayerViewHolder(itemView, avatar.getId(), name.getId(), level.getId());
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        LobbyManager.PlayerData player = players.get(position);

        holder.tvPlayerName.setText(player.name);
        holder.tvPlayerLevel.setText("المستوى " + player.level);

        if (player.avatar != null && !player.avatar.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(player.avatar)
                    .circleCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public void updatePlayers(List<LobbyManager.PlayerData> newPlayers) {
        this.players = newPlayers;
        notifyDataSetChanged();
    }

    private static int dpToPx(int dp, ViewGroup parent) {
        return (int) (dp * parent.getContext().getResources().getDisplayMetrics().density);
    }

    static class PlayerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvPlayerName;
        TextView tvPlayerLevel;

        public PlayerViewHolder(@NonNull View itemView, int avatarId, int nameId, int levelId) {
            super(itemView);
            ivAvatar = itemView.findViewById(avatarId);
            tvPlayerName = itemView.findViewById(nameId);
            tvPlayerLevel = itemView.findViewById(levelId);
        }
    }
}