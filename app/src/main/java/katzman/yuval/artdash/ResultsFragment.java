package katzman.yuval.artdash;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Arrays;
import java.util.List;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class ResultsFragment extends Fragment {

    private RecyclerView rvLeaderboard;
    private KonfettiView konfettiView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String roomId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_results, container, false);

        rvLeaderboard = view.findViewById(R.id.rvLeaderboard);
        konfettiView = view.findViewById(R.id.konfettiView);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            roomId = getArguments().getString("roomId");
            loadLeaderboard();
        }

        view.findViewById(R.id.btnPlayAgain).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        return view;
    }

    private void loadLeaderboard() {
        db.collection("rooms").document(roomId)
                .collection("submissions")
                .orderBy("totalScore", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    rvLeaderboard.setAdapter(new LeaderboardAdapter(list));

                    // אם יש תוצאות - נחגוג!
                    if (!list.isEmpty()) {
                        startConfetti();
                    }
                });
    }

    private void startConfetti() {
        EmitterConfig emitterConfig = new Emitter(5L, java.util.concurrent.TimeUnit.SECONDS).perSecond(30);
        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(1f, 5f)
                .colors(Arrays.asList(Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.GREEN))
                .position(new Position.Relative(0.5, -0.1))
                .build();

        konfettiView.start(party);
    }

    private void toggleBottomNavigation(boolean show) {
        if (getActivity() instanceof MainActivity) {
            int visibility = show ? View.VISIBLE : View.GONE;
            ((MainActivity) getActivity()).setBottomNavigationVisibility(visibility);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toggleBottomNavigation(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        toggleBottomNavigation(true);
    }
}