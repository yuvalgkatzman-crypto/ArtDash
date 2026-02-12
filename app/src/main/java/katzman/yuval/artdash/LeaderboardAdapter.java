package katzman.yuval.artdash;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<DocumentSnapshot> playersList;

    public LeaderboardAdapter(List<DocumentSnapshot> playersList) {
        this.playersList = playersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot doc = playersList.get(position);

        holder.tvRank.setText("#" + (position + 1));
        String playerName = doc.getString("player");
        holder.tvPlayerName.setText(playerName != null ? playerName : "Artist " + (position + 1));

        Double score = doc.getDouble("totalScore");
        if (score == null) score = 0.0;
        holder.tvScoreValue.setText(String.valueOf(score));

        String base64Image = doc.getString("imageData");
        if (base64Image != null) {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.ivSmallDrawing.setImageBitmap(decodedByte);
        }

        if (position == 0) {
            holder.tvWinnerBadge.setVisibility(View.VISIBLE);
            holder.leaderboardItemCard.setCardBackgroundColor(Color.parseColor("#FFD600"));
            holder.tvPlayerName.setTextColor(Color.parseColor("#1A1333"));
            holder.tvScoreValue.setTextColor(Color.parseColor("#1A1333"));
            holder.tvRank.setTextColor(Color.parseColor("#1A1333"));

            ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(holder.tvWinnerBadge,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.2f, 1.0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.2f, 1.0f));
            scaleAnimator.setDuration(1000);
            scaleAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scaleAnimator.setRepeatMode(ObjectAnimator.REVERSE);
            scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleAnimator.start();
        } else {
            holder.tvWinnerBadge.setVisibility(View.GONE);
            holder.tvWinnerBadge.clearAnimation();
            holder.leaderboardItemCard.setCardBackgroundColor(Color.parseColor("#3D2E6E"));
            holder.tvPlayerName.setTextColor(Color.WHITE);
            holder.tvScoreValue.setTextColor(Color.parseColor("#FFD600"));
            holder.tvRank.setTextColor(Color.parseColor("#FFD600"));
        }
    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvPlayerName, tvScoreValue, tvWinnerBadge;
        ImageView ivSmallDrawing;
        CardView leaderboardItemCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvScoreValue = itemView.findViewById(R.id.tvScoreValue);
            ivSmallDrawing = itemView.findViewById(R.id.ivSmallDrawing);
            tvWinnerBadge = itemView.findViewById(R.id.tvWinnerBadge);
            leaderboardItemCard = itemView.findViewById(R.id.leaderboardItemCard);
        }
    }
}