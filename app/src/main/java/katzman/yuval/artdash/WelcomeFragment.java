package katzman.yuval.artdash;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class WelcomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        TextView title = view.findViewById(R.id.appTitle);
        ImageView logo = view.findViewById(R.id.appLogo);
        TextView tagline = view.findViewById(R.id.appTagline);
        Animation pulse = AnimationUtils.loadAnimation(getContext(), R.anim.pulse);
        title.startAnimation(pulse);
        logo.startAnimation(pulse);
        Button loginButton = view.findViewById(R.id.loginButton);
        Button registerButton = view.findViewById(R.id.registerButton);
        tagline.setAlpha(0f);
        loginButton.setAlpha(0f);
        registerButton.setAlpha(0f);
        new Handler().postDelayed(() -> {
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(800);
            fadeIn.setFillAfter(true);
            tagline.startAnimation(fadeIn);
            tagline.setAlpha(1f);
            loginButton.startAnimation(fadeIn);
            registerButton.startAnimation(fadeIn);

            loginButton.setAlpha(1f);
            registerButton.setAlpha(1f);

        }, 500);
        loginButton.setOnClickListener(v -> {

        });

        registerButton.setOnClickListener(v -> {

        });

        return view;
    }
}
