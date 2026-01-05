package katzman.yuval.artdash;

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

public class ForgotPasswordFragment extends Fragment {

    private EditText etForgotEmail;
    private Button btnResetPassword;
    private TextView tvBackToLogin;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        mAuth = FirebaseAuth.getInstance();

        etForgotEmail = view.findViewById(R.id.etForgotEmail);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin);

        btnResetPassword.setOnClickListener(v -> {
            String email = etForgotEmail.getText().toString().trim();

            if (email.isEmpty()) {
                etForgotEmail.setError("Please enter your email");
                etForgotEmail.requestFocus();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Check your email to reset password!", Toast.LENGTH_LONG).show();
                            getParentFragmentManager().popBackStack();
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvBackToLogin.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}