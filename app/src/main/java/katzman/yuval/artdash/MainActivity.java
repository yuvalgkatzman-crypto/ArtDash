package katzman.yuval.artdash;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFragmentContainer, new MainSwipeFragment())
                    .commit();
        }
    }

    // Empty method to prevent crashes if other fragments call it
    public void setBottomNavigationVisibility(int visibility) {
        // Do nothing as navigation is removed
    }
}