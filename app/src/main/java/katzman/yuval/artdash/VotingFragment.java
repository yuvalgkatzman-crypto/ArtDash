package katzman.yuval.artdash;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class VotingFragment extends Fragment {

    private ImageView ivVotedDrawing;
    private TextView tvVotingTimer, tvVotingTopic;
    private RatingBar ratingBar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<DocumentSnapshot> submissions = new ArrayList<>();
    private int currentDrawingIndex = 0;
    private String roomId;
    private CountDownTimer voteTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voting, container, false);

        ivVotedDrawing = view.findViewById(R.id.ivVotedDrawing);
        tvVotingTimer = view.findViewById(R.id.tvVotingTimer);
        tvVotingTopic = view.findViewById(R.id.tvVotingTopic);
        ratingBar = view.findViewById(R.id.ratingBar);

        if (getArguments() != null) {
            roomId = getArguments().getString("roomId");
            loadAllSubmissions();
        }

        return view;
    }

    private void toggleBottomNavigation(boolean show) {
        if (getActivity() instanceof MainActivity) {
            int visibility = show ? View.VISIBLE : View.GONE;
            ((MainActivity) getActivity()).setBottomNavigationVisibility(visibility);
        }
    }

    private void loadAllSubmissions() {
        db.collection("rooms").document(roomId)
                .collection("submissions").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    submissions.clear();
                    submissions.addAll(queryDocumentSnapshots.getDocuments());
                    if (!submissions.isEmpty()) {
                        showNextDrawing();
                    } else {
                        Toast.makeText(getContext(), "No drawings to vote for", Toast.LENGTH_SHORT).show();
                        navigateToResults();
                    }
                });
    }

    private void showNextDrawing() {
        if (currentDrawingIndex < submissions.size()) {
            DocumentSnapshot doc = submissions.get(currentDrawingIndex);
            String base64Image = doc.getString("imageData");

            if (base64Image != null) {
                try {
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivVotedDrawing.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    ivVotedDrawing.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            }

            startVotingTimer();
        } else {
            if (voteTimer != null) voteTimer.cancel();
            navigateToResults();
        }
    }

    private void navigateToResults() {
        ResultsFragment resultsFragment = new ResultsFragment();
        Bundle args = new Bundle();
        args.putString("roomId", roomId);
        resultsFragment.setArguments(args);

        if (isAdded() && getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFragmentContainer, resultsFragment)
                    .commit();
        }
    }

    private void startVotingTimer() {
        ratingBar.setRating(0);
        if (voteTimer != null) voteTimer.cancel();

        voteTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvVotingTimer.setText("" + (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                submitVoteAndContinue();
            }
        }.start();
    }

    private void submitVoteAndContinue() {
        float rating = ratingBar.getRating();
        DocumentSnapshot currentDoc = submissions.get(currentDrawingIndex);
        String playerDocId = currentDoc.getId();

        db.collection("rooms").document(roomId)
                .collection("submissions").document(playerDocId)
                .update("totalScore", FieldValue.increment(rating))
                .addOnCompleteListener(task -> {
                    currentDrawingIndex++;
                    showNextDrawing();
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toggleBottomNavigation(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (voteTimer != null) voteTimer.cancel();
        toggleBottomNavigation(true);
    }
}