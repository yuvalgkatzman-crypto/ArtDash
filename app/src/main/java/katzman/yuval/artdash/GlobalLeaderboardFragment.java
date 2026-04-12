package katzman.yuval.artdash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GlobalLeaderboardFragment extends Fragment {

    private RecyclerView rvGlobal;
    private GlobalLeaderboardAdapter adapter;
    private List<DocumentSnapshot> usersList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressBar pbLoader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_global_leaderboard, container, false);

        rvGlobal = view.findViewById(R.id.rvGlobalLeaderboard);
        pbLoader = view.findViewById(R.id.pbLoader);

        rvGlobal.setLayoutManager(new LinearLayoutManager(getContext()));

        loadData();

        return view;
    }

    private void loadData() {
        if (pbLoader != null) {
            pbLoader.setVisibility(View.VISIBLE);
        }

        db.collection("User")
                .orderBy("totalStars", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded() || getActivity() == null) return;

                    if (pbLoader != null) {
                        pbLoader.setVisibility(View.GONE);
                    }

                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading rankings", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        usersList = value.getDocuments();
                        adapter = new GlobalLeaderboardAdapter(usersList);
                        rvGlobal.setAdapter(adapter);

                        rvGlobal.setAlpha(0f);
                        rvGlobal.animate().alpha(1f).setDuration(500).start();
                    }
                });
    }
}