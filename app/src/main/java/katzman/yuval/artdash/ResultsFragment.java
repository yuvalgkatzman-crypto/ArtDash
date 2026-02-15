package katzman.yuval.artdash;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private Bitmap myDrawingBitmap;

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
            fetchMyDrawing();
        }

        View btnShare = view.findViewById(R.id.btnShare);
        if (btnShare != null) btnShare.setOnClickListener(v -> shareDrawing());

        View btnSave = view.findViewById(R.id.btnSaveLocal);
        if (btnSave != null) btnSave.setOnClickListener(v -> saveDrawing());

        view.findViewById(R.id.btnPlayAgain).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        return view;
    }

    private void fetchMyDrawing() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || roomId == null) return;

        db.collection("rooms").document(roomId)
                .collection("submissions").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("imageData")) {
                        String base64 = doc.getString("imageData");
                        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                        myDrawingBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    }
                });
    }

    private void shareDrawing() {
        if (myDrawingBitmap == null) {
            Toast.makeText(getContext(), "Drawing is still loading...", Toast.LENGTH_SHORT).show();
            fetchMyDrawing();
            return;
        }

        try {
            File imagesFolder = new File(requireContext().getExternalFilesDir(null), "SharedImages");
            if (!imagesFolder.exists()) imagesFolder.mkdirs();

            File file = new File(imagesFolder, "art_dash_share.png");
            FileOutputStream stream = new FileOutputStream(file);
            myDrawingBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri uri = FileProvider.getUriForFile(requireContext(),
                    "katzman.yuval.artdash.fileprovider", file);

            if (uri != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/png");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Share your masterpiece via:"));
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDrawing() {
        if (myDrawingBitmap == null) {
            Toast.makeText(getContext(), "Drawing is still loading...", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "ArtDash_" + System.currentTimeMillis() + ".png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ArtDash");

            Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                OutputStream out = requireContext().getContentResolver().openOutputStream(uri);
                myDrawingBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
                Toast.makeText(getContext(), "Saved to Gallery!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLeaderboard() {
        db.collection("rooms").document(roomId)
                .collection("submissions")
                .orderBy("totalScore", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    rvLeaderboard.setAdapter(new LeaderboardAdapter(list));

                    if (!list.isEmpty()) {
                        startConfetti();
                    }
                });
    }

    private void startConfetti() {
        EmitterConfig emitterConfig = new Emitter(5L, TimeUnit.SECONDS).perSecond(30);
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
    public void onResume() {
        super.onResume();
        toggleBottomNavigation(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleBottomNavigation(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        toggleBottomNavigation(true);
    }
}