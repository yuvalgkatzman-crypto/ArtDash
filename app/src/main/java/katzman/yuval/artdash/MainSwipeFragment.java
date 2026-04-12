package katzman.yuval.artdash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class MainSwipeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_swipe, container, false);

        ViewPager2 viewPager = view.findViewById(R.id.mainViewPager);

        viewPager.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return new ProfileFragment();
                    case 1: return new HomeFragment();
                    case 2: return new QuickMatchFragment();
                    default: return new HomeFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        viewPager.setCurrentItem(1, false);
        return view;
    }
}