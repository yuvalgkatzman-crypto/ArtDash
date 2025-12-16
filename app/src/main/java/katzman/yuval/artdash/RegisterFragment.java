package katzman.yuval.artdash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RegisterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        TextView tvBackToLogin = view.findViewById(R.id.tvBackToLogin);


        tvBackToLogin.setOnClickListener(v -> {
            if (getActivity() != null) {

                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }
}