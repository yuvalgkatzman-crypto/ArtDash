package katzman.yuval.artdash;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog;

public class PaintFragment extends Fragment {

    private katzman.yuval.artdash.DrawingView drawingView;
    private TextView tvTimer, tvTopic;
    private String roomId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CountDownTimer gameTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paint, container, false);

        drawingView = view.findViewById(R.id.drawingView);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvTopic = view.findViewById(R.id.tvTopic);

        if (getArguments() != null) {
            roomId = getArguments().getString("roomId");
            loadGameData();
        }

        setupActionButtons(view);

        return view;
    }

    private void setupActionButtons(View v) {

        View bottomCard = v.findViewById(R.id.bottomToolsCard);
        if (bottomCard != null) {
            bottomCard.setElevation(20f);
            bottomCard.setTranslationZ(20f);
        }


        v.findViewById(R.id.btnUndo).setOnClickListener(view -> drawingView.undo());
        v.findViewById(R.id.btnRedo).setOnClickListener(view -> drawingView.redo());


        v.findViewById(R.id.btnEraser).setOnClickListener(view -> {
            drawingView.setEraserMode(true);
            Toast.makeText(getContext(), "Eraser Mode", Toast.LENGTH_SHORT).show();
        });


        v.findViewById(R.id.btnClear).setOnClickListener(view -> drawingView.clearCanvas());


        v.findViewById(R.id.btnColorWheel).setOnClickListener(view -> openColorPickerDialog());


        SeekBar sbBrushSize = v.findViewById(R.id.sbBrushSize);
        sbBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                drawingView.setStrokeWidth(progress + 5f);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void openColorPickerDialog() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(getContext(), Color.BLACK, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {}

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                drawingView.setEraserMode(false);
                drawingView.setColor(color);


                ImageButton colorButton = (ImageButton) getView().findViewById(R.id.btnColorWheel);
                if (colorButton != null) {
                    colorButton.setImageTintList(ColorStateList.valueOf(color));
                }
            }
        });
        colorPicker.show();
    }

    private void loadGameData() {
        if (roomId == null) return;
        db.collection("rooms").document(roomId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String topic = doc.getString("topic");
                tvTopic.setText(topic != null ? topic : "Draw Something!");

                com.google.firebase.Timestamp startTime = doc.getTimestamp("startTime");
                if (startTime != null) {
                    calculateAndStartTimer(startTime);
                } else {
                    startMatchTimer(1800);
                }
            }
        });
    }

    private void calculateAndStartTimer(com.google.firebase.Timestamp startTime) {
        long now = System.currentTimeMillis();
        long startMillis = startTime.toDate().getTime();
        long totalGameTime = 180000;

        long elapsed = now - startMillis;
        long remaining = totalGameTime - elapsed;

        if (remaining > 0) {
            startMatchTimer(remaining);
        } else {
            onTimeIsUp();
        }
    }

    private void startMatchTimer(long timeInMillis) {
        if (gameTimer != null) gameTimer.cancel();
        gameTimer = new CountDownTimer(timeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));


                if (millisUntilFinished < 10000) {
                    tvTimer.setTextColor(Color.RED);
                }
            }
            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                onTimeIsUp();
            }
        }.start();
    }

    private void onTimeIsUp() {
        drawingView.setEnabled(false);
        saveDrawingToCloud();
        Toast.makeText(getContext(), "Time's up! Saving...", Toast.LENGTH_LONG).show();
    }

    private void saveDrawingToCloud() {
        try {
            Bitmap bitmap = drawingView.getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // דחיסה ל-50% לצורך חיסכון בנפח ב-Firestore
            String imageString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            updateFirestoreWithImageUrl(imageString);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFirestoreWithImageUrl(String imageEncoded) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "guest_" + System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("imageData", imageEncoded);
        data.put("totalScore", 0);
        data.put("timestamp", FieldValue.serverTimestamp());

        db.collection("rooms").document(roomId)
                .collection("submissions").document(userId)
                .set(data)
                .addOnSuccessListener(aVoid -> navigateToVoting())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Cloud Save Failed", Toast.LENGTH_SHORT).show());
    }

    private void navigateToVoting() {
        if (isAdded()) {
            VotingFragment votingFragment = new VotingFragment();
            Bundle args = new Bundle();
            args.putString("roomId", roomId);
            votingFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.mainFragmentContainer, votingFragment)
                    .commit();
        }
    }

    private void toggleBottomNavigation(boolean show) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(show ? View.VISIBLE : View.GONE);
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
        if (gameTimer != null) gameTimer.cancel();
    }
}