package katzman.yuval.artdash;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
        String name = doc.getString("name");
        Double stars = doc.getDouble("totalStars");
        String base64String = doc.getString("profileImage");
        int finalStars = (stars != null) ? stars.intValue() : 0;

        holder.tvPlayerName.setText(name != null ? name : "Unknown Artist");
        holder.tvScoreValue.setText(finalStars + " ⭐");

        if (base64String != null && !base64String.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivSmallDrawing.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.ivSmallDrawing.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            holder.ivSmallDrawing.setImageResource(R.drawable.ic_user_placeholder);
        }

        setupColors(holder, position);
    }

    private void setupColors(ViewHolder holder, int position) {
        int darkColor = Color.parseColor("#1A1A3A");
        if (holder.leaderboardItemCard != null) {
            if (position == 0) {
                holder.leaderboardItemCard.setCardBackgroundColor(Color.parseColor("#FFD700"));
                holder.tvPlayerName.setTextColor(darkColor);
                holder.tvScoreValue.setTextColor(darkColor);
                holder.tvRank.setText("🥇");
                holder.tvRank.setTextColor(darkColor);
            } else if (position == 1) {
                holder.leaderboardItemCard.setCardBackgroundColor(Color.parseColor("#E5E4E2"));
                holder.tvPlayerName.setTextColor(darkColor);
                holder.tvScoreValue.setTextColor(darkColor);
                holder.tvRank.setText("🥈");
                holder.tvRank.setTextColor(darkColor);
            } else if (position == 2) {
                holder.leaderboardItemCard.setCardBackgroundColor(Color.parseColor("#CD7F32"));
                holder.tvPlayerName.setTextColor(Color.WHITE);
                holder.tvScoreValue.setTextColor(Color.WHITE);
                holder.tvRank.setText("🥉");
                holder.tvRank.setTextColor(Color.WHITE);
            } else {
                holder.leaderboardItemCard.setCardBackgroundColor(Color.parseColor("#1F1F3D"));
                holder.tvPlayerName.setTextColor(Color.WHITE);
                holder.tvScoreValue.setTextColor(Color.parseColor("#FFD600"));
                holder.tvRank.setText("#" + (position + 1));
                holder.tvRank.setTextColor(Color.parseColor("#FFD600"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return usersList != null ? usersList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvPlayerName, tvScoreValue;
        ImageView ivSmallDrawing;
        CardView leaderboardItemCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvScoreValue = itemView.findViewById(R.id.tvScoreValue);
            ivSmallDrawing = itemView.findViewById(R.id.ivSmallDrawing);
            leaderboardItemCard = itemView.findViewById(R.id.leaderboardItemCard);
        }
    }
}