package katzman.yuval.artdash;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public fragmentLogin() {
        // Required empty public constructor
    }

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
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.authFragmentContainer, new RegisterFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

      
        tvForgotPassword.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.authFragmentContainer, new ForgotPasswordFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        btnLogin.setOnClickListener(v -> {
            loginUser();
        });

        return view;
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (isAdded() && getActivity() != null) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Welcome back!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(getContext(), "Login Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}