package katzman.yuval.artdash;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class QuickMatchFragment extends Fragment {

    private ImageView ivQuickMatchTitle;
    private ImageView ivMatchIcon;
    private Button btnStartMatch;
    private boolean isSearching = false;

    private DatabaseReference dbRef;
    private ValueEventListener matchListener;
    private String userId;
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quick_match, container, false);

        ivQuickMatchTitle = view.findViewById(R.id.ivQuickMatchTitle);
        ivMatchIcon = view.findViewById(R.id.ivMatchIcon);
        btnStartMatch = view.findViewById(R.id.btnStartMatch);

        userId = "user_" + new Random().nextInt(10000);
        dbRef = FirebaseDatabase.getInstance().getReference("waiting_room");

        Animation pulse = AnimationUtils.loadAnimation(getContext(), R.anim.pulse);
        ivQuickMatchTitle.startAnimation(pulse);

        btnStartMatch.setOnClickListener(v -> {
            if (!isSearching) {
                startSearch();
            } else {
                stopSearch();
            }
        });

        return view;
    }

    private void startSearch() {
        isSearching = true;
        btnStartMatch.setText("SEARCHING (1/6)");

        Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_match);
        ivMatchIcon.startAnimation(rotate);

        dbRef.child(userId).setValue(true);

        matchListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long usersCount = snapshot.getChildrenCount();
                btnStartMatch.setText("SEARCHING (" + usersCount + "/6)");

                if (usersCount >= 6 && isSearching) {
                    matchFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        dbRef.addValueEventListener(matchListener);

        timeoutRunnable = () -> {
            if (isSearching) {
                dbRef.get().addOnSuccessListener(snapshot -> {
                    long count = snapshot.getChildrenCount();
                    if (count >= 2) {
                        matchFound();
                    } else {
                        Toast.makeText(getContext(), "Not enough players, keep searching...", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 10000);
    }

    private void stopSearch() {
        isSearching = false;
        btnStartMatch.setText("FIND A PARTNER");
        ivMatchIcon.clearAnimation();

        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }

        if (userId != null) {
            dbRef.child(userId).removeValue();
        }
        if (matchListener != null) {
            dbRef.removeEventListener(matchListener);
        }
    }

    private void matchFound() {
        isSearching = false;
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }

        dbRef.child(userId).removeValue();
        if (matchListener != null) {
            dbRef.removeEventListener(matchListener);
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.mainFragmentContainer, new PaintFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopSearch();
    }
}