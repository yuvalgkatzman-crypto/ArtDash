package katzman.yuval.artdash;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import yuku.ambilwarna.AmbilWarnaDialog;

public class PaintFragment extends Fragment {

    private DrawingView drawingView;
    private TextView tvTimer, tvTopic;
    private String roomId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CountDownTimer gameTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paint, container, false);

        toggleBottomNavigation(false);

        drawingView = view.findViewById(R.id.drawingView);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvTopic = view.findViewById(R.id.tvTopic);

        if (getArguments() != null) {
            roomId = getArguments().getString("roomId");
            loadGameData();
        } else {
            tvTopic.setText("Draw: Something Creative");
        }

        setupActionButtons(view);

        return view;
    }

    private void toggleBottomNavigation(boolean show) {
        if (getActivity() instanceof MainActivity) {
            int visibility = show ? View.VISIBLE : View.GONE;
            ((MainActivity) getActivity()).setBottomNavigationVisibility(visibility);
        }
    }

    private void loadGameData() {
        if (roomId == null) return;

        db.collection("rooms").document(roomId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String topic = doc.getString("topic");
                tvTopic.setText(topic != null ? topic : "Something Creative");

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

    private void setupActionButtons(View v) {
        v.findViewById(R.id.btnUndo).setOnClickListener(view -> drawingView.undo());
        v.findViewById(R.id.btnEraser).setOnClickListener(view -> drawingView.setEraserMode(true));
        v.findViewById(R.id.btnClear).setOnClickListener(view -> drawingView.clear());
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
                drawingView.setColor(color);
                View colorButton = getView().findViewById(R.id.btnColorWheel);
                if (colorButton != null) {
                    colorButton.setBackgroundTintList(ColorStateList.valueOf(color));
                }
            }
        });
        colorPicker.show();
    }

    private void onTimeIsUp() {
        drawingView.setEnabled(false);
        saveDrawingToCloud();
        Toast.makeText(getContext(), "Time's up! Submitting...", Toast.LENGTH_LONG).show();
    }

    private void saveDrawingToCloud() {

        Bitmap bitmap = drawingView.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();


        String imageString = Base64.encodeToString(data, Base64.DEFAULT);


        updateFirestoreWithImageUrl(imageString);
    }

    private void updateFirestoreWithImageUrl(String imageEncoded) {
        if (roomId != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("imageData", imageEncoded);
            data.put("timestamp", FieldValue.serverTimestamp());
            data.put("player", "User_" + System.currentTimeMillis() % 1000);

            db.collection("rooms").document(roomId)
                    .collection("submissions").document("my_user_id")
                    .set(data)
                    .addOnSuccessListener(aVoid -> {

                        navigateToVoting();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void navigateToVoting() {
        VotingFragment votingFragment = new VotingFragment();


        Bundle args = new Bundle();
        args.putString("roomId", roomId);
        votingFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFragmentContainer, votingFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        toggleBottomNavigation(true);
        if (gameTimer != null) {
            gameTimer.cancel();
        }
    }
}