package katzman.yuval.artdash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        view.findViewById(R.id.btnFreeDraw).setOnClickListener(v -> {
            navigateToFragment(new FreePlayFragment());
        });

        view.findViewById(R.id.btnLeaderboardBig).setOnClickListener(v -> {
            navigateToFragment(new GlobalLeaderboardFragment());
        });

        return view;
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        transaction.replace(R.id.mainFragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}