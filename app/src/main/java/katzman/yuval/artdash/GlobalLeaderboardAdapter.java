package katzman.yuval.artdash;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class GlobalLeaderboardAdapter extends RecyclerView.Adapter<GlobalLeaderboardAdapter.ViewHolder> {

    private List<DocumentSnapshot> usersList;


    public GlobalLeaderboardAdapter(List<DocumentSnapshot> usersList) {
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot doc = usersList.get(position);


        holder.tvRank.setText("#" + (position + 1));


        String name = doc.getString("name");
        holder.tvPlayerName.setText(name != null ? name : "Unknown Artist");


        Double stars = doc.getDouble("totalStars");
        int finalStars = (stars != null) ? stars.intValue() : 0;
        holder.tvScoreValue.setText(String.valueOf(finalStars));


        if (holder.tvWinnerBadge != null) {
            if (position == 0) {
                holder.tvWinnerBadge.setVisibility(View.VISIBLE);
            } else {
                holder.tvWinnerBadge.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return usersList != null ? usersList.size() : 0;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvPlayerName, tvScoreValue, tvWinnerBadge;
        ImageView ivSmallDrawing;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvScoreValue = itemView.findViewById(R.id.tvScoreValue);
            ivSmallDrawing = itemView.findViewById(R.id.ivSmallDrawing);
            tvWinnerBadge = itemView.findViewById(R.id.tvWinnerBadge);
        }
    }
}