package katzman.yuval.artdash;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Locale;
import java.util.Random;

public class PaintFragment extends Fragment {

    private DrawingView drawingView;
    private TextView tvTimer, tvTopic;
    private String roomId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CountDownTimer countDownTimer;


    private String[] topics = {"Apple", "House", "Rocket", "Cat", "Sun", "Car", "Pizza"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paint, container, false);


        drawingView = view.findViewById(R.id.drawingView);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvTopic = view.findViewById(R.id.tvTopic);


        if (getArguments() != null) {
            roomId = getArguments().getString("roomId");
        }

        setupButtons(view);
        startMatchLogic();

        return view;
    }

    private void setupButtons(View v) {
        v.findViewById(R.id.btnUndo).setOnClickListener(view -> drawingView.undo());
        v.findViewById(R.id.btnRedo).setOnClickListener(view -> drawingView.redo());
        v.findViewById(R.id.btnClear).setOnClickListener(view -> drawingView.clearAll());
        v.findViewById(R.id.btnEraser).setOnClickListener(view -> drawingView.setEraser());


        v.findViewById(R.id.colorBlack).setOnClickListener(view -> drawingView.setColor("#000000"));
        v.findViewById(R.id.colorRed).setOnClickListener(view -> drawingView.setColor("#FF0000"));
        v.findViewById(R.id.colorBlue).setOnClickListener(view -> drawingView.setColor("#0000FF"));
        v.findViewById(R.id.colorGreen).setOnClickListener(view -> drawingView.setColor("#4CAF50"));
        v.findViewById(R.id.colorYellow).setOnClickListener(view -> drawingView.setColor("#FFEB3B"));
    }

    private void startMatchLogic() {

        String randomTopic = topics[new Random().nextInt(topics.length)];
        tvTopic.setText("Draw: " + randomTopic);


        countDownTimer = new CountDownTimer(90000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));


                if (millisUntilFinished < 10000) {
                    tvTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                finishDrawing();
            }
        }.start();
    }

    private void finishDrawing() {
        Toast.makeText(getContext(), "Time's up! Submitting...", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}