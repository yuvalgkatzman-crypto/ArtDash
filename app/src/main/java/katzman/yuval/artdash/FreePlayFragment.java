package katzman.yuval.artdash;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import yuku.ambilwarna.AmbilWarnaDialog;

public class FreePlayFragment extends Fragment {

    private katzman.yuval.artdash.DrawingView drawingView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_free_play, container, false);

        drawingView = view.findViewById(R.id.drawingView);
        setupActionButtons(view);
        fetchMusicAndPlay();

        return view;
    }

    private void fetchMusicAndPlay() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://itunes.apple.com/search?term=techno&limit=1&entity=musicTrack";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONArray results = jsonObject.getJSONArray("results");

                        if (results.length() > 0) {
                            String audioUrl = results.getJSONObject(0).getString("previewUrl");
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> playMusic(audioUrl));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void playMusic(String url) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                mp.setLooping(true);
                if (isMuted) mp.setVolume(0, 0);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupActionButtons(View v) {
        v.findViewById(R.id.btnUndo).setOnClickListener(view -> drawingView.undo());
        v.findViewById(R.id.btnRedo).setOnClickListener(view -> drawingView.redo());

        v.findViewById(R.id.btnEraser).setOnClickListener(view -> {
            drawingView.setEraserMode(true);
            Toast.makeText(getContext(), "Eraser Mode", Toast.LENGTH_SHORT).show();
        });

        v.findViewById(R.id.btnClear).setOnClickListener(view -> drawingView.clearCanvas());
        v.findViewById(R.id.btnColorWheel).setOnClickListener(view -> openColorPickerDialog());

        v.findViewById(R.id.btnSaveDrawing).setOnClickListener(view -> saveDrawingToCloud());

        ImageButton btnMute = v.findViewById(R.id.btnMute);
        btnMute.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                if (isMuted) {
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    btnMute.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                } else {
                    mediaPlayer.setVolume(0, 0);
                    btnMute.setImageResource(android.R.drawable.ic_lock_silent_mode);
                }
                isMuted = !isMuted;
            }
        });

        SeekBar sbBrushSize = v.findViewById(R.id.sbBrushSize);
        sbBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setStrokeWidth(progress + 5f);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
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
                ImageButton colorButton = getView().findViewById(R.id.btnColorWheel);
                if (colorButton != null) colorButton.setImageTintList(ColorStateList.valueOf(color));
            }
        });
        colorPicker.show();
    }

    private void saveDrawingToCloud() {
        try {
            Bitmap bitmap = drawingView.getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            String imageString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            db.collection("User").document(userId)
                    .update("profileImage", imageString)
                    .addOnSuccessListener(aVoid -> {
                        saveToUserGallery(userId, imageString);
                        Toast.makeText(getContext(), "Masterpiece saved!", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error saving", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToUserGallery(String userId, String imageEncoded) {
        Map<String, Object> personalData = new HashMap<>();
        personalData.put("imageData", imageEncoded);
        personalData.put("timestamp", FieldValue.serverTimestamp());
        personalData.put("isTopPick", false);

        db.collection("User").document(userId)
                .collection("myDrawings").add(personalData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}