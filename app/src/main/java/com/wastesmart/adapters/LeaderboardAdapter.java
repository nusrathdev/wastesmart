package com.wastesmart.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.wastesmart.R;
import com.wastesmart.models.LeaderboardItem;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private Context context;
    private List<LeaderboardItem> leaderboardItems;

    public LeaderboardAdapter(Context context, List<LeaderboardItem> leaderboardItems) {
        this.context = context;
        this.leaderboardItems = leaderboardItems;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardItem item = leaderboardItems.get(position);

        // Set rank
        holder.tvRank.setText(String.valueOf(item.getRank()));

        // Set name
        holder.tvName.setText(item.getName());

        // Set score
        holder.tvScore.setText(String.valueOf(item.getScore()));

        // Set zone/area
        holder.tvZone.setText(item.getZone());

        // Highlight top 3 positions
        if (item.getRank() == 1) {
            holder.tvRank.setBackgroundResource(R.drawable.gold_circle_bg);
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.gold));
        } else if (item.getRank() == 2) {
            holder.tvRank.setBackgroundResource(R.drawable.silver_circle_bg);
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.silver));
        } else if (item.getRank() == 3) {
            holder.tvRank.setBackgroundResource(R.drawable.bronze_circle_bg);
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.bronze));
        } else {
            holder.tvRank.setBackgroundResource(R.drawable.rank_circle_bg);
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.dark_gray));
        }
    }

    @Override
    public int getItemCount() {
        return leaderboardItems.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvScore, tvZone;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvName);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvZone = itemView.findViewById(R.id.tvZone);
        }
    }
}
