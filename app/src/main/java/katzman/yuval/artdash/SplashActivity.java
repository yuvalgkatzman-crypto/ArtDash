package katzman.yuval.artdash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView logo = findViewById(R.id.splashLogo);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse_scale);
        logo.startAnimation(pulse);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            finish();
        }, 2500);



    }
}
