package katzman.yuval.artdash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuickMatchFragment extends Fragment {

    private ImageView ivMatchIcon;
    private Button btnStartMatch;
    private boolean isSearching = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId;
    private String currentRoomId = null;
    private ListenerRegistration roomListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quick_match, container, false);
        ivMatchIcon = view.findViewById(R.id.ivMatchIcon);
        btnStartMatch = view.findViewById(R.id.btnStartMatch);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        btnStartMatch.setOnClickListener(v -> {
            if (!isSearching) startSearch();
            else stopSearch();
        });

        return view;
    }

    private void startSearch() {
        isSearching = true;
        btnStartMatch.setText("SEARCHING (1/6)");
        Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_match);
        ivMatchIcon.startAnimation(rotate);

        db.collection("rooms")
                .whereEqualTo("status", "waiting")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        joinRoom(queryDocumentSnapshots.getDocuments().get(0).getId());
                    } else {
                        createNewRoom();
                    }
                })
                .addOnFailureListener(e -> stopSearch());
    }

    private void createNewRoom() {
        List<String> players = new ArrayList<>();
        players.add(currentUserId);
        Map<String, Object> room = new HashMap<>();
        room.put("players", players);
        room.put("status", "waiting");
        room.put("createdAt", FieldValue.serverTimestamp());

        db.collection("rooms").add(room)
                .addOnSuccessListener(docRef -> {
                    currentRoomId = docRef.getId();
                    listenToRoom(currentRoomId);
                });
    }

    private void joinRoom(String roomId) {
        currentRoomId = roomId;
        db.collection("rooms").document(roomId)
                .update("players", FieldValue.arrayUnion(currentUserId))
                .addOnSuccessListener(aVoid -> listenToRoom(roomId));
    }

    private void listenToRoom(String roomId) {
        roomListener = db.collection("rooms").document(roomId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;
                    List<String> players = (List<String>) snapshot.get("players");
                    int count = (players != null) ? players.size() : 0;
                    btnStartMatch.setText("SEARCHING (" + count + "/6)");
                    if (count >= 6) matchFound();
                });
    }

    private void stopSearch() {
        isSearching = false;
        btnStartMatch.setText("FIND A MATCH");
        ivMatchIcon.clearAnimation();
        if (roomListener != null) roomListener.remove();
        if (currentRoomId != null) {
            db.collection("rooms").document(currentRoomId)
                    .update("players", FieldValue.arrayRemove(currentUserId));
        }
    }

    private void matchFound() {
        if (roomListener != null) roomListener.remove();
        isSearching = false;

        PaintFragment paintFragment = new PaintFragment();
        Bundle args = new Bundle();
        args.putString("roomId", currentRoomId);
        paintFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.mainFragmentContainer, paintFragment)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isSearching) stopSearch();
    }
}