package katzman.yuval.artdash;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;

public class fragmentLogin extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister, tvForgotPassword;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvGoToRegister = view.findViewById(R.id.tvGoToRegister);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);

        tvGoToRegister.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.authFragmentContainer, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        tvForgotPassword.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.authFragmentContainer, new ForgotPasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnLogin.setOnClickListener(v -> {
            animateButtonClick(v);
            loginUser();
        });

        return view;
    }

    private void animateButtonClick(View view) {
        ScaleAnimation scale = new ScaleAnimation(1f, 0.95f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(100);
        view.startAnimation(scale);
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (isAdded() && getActivity() != null) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        } else {
                            Toast.makeText(getContext(), "Login Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}