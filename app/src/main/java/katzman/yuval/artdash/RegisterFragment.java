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

public class RegisterFragment extends Fragment {

    private EditText etEmail, etPassword, etFullName;
    private Button btnRegister;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();

        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmailRegister);
        etPassword = view.findViewById(R.id.etPasswordRegister);
        btnRegister = view.findViewById(R.id.btnRegister);
        TextView tvBackToLogin = view.findViewById(R.id.tvBackToLogin);

        tvBackToLogin.setOnClickListener(v -> {
            if (getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        btnRegister.setOnClickListener(v -> registerUser());

        return view;
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etFullName.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (isAdded() && getActivity() != null) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Failed";
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}