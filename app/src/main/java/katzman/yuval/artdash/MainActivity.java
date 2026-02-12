package katzman.yuval.artdash;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // הפעלת הפונקציה שמעלה את המילים ל-Firebase
        // זכרי: אחרי הרצה אחת ולוודא שהמילים עלו, תמחקי את השורה הזו
        uploadAllTopics();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFragmentContainer, new HomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_quick_match) {
                selectedFragment = new QuickMatchFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainFragmentContainer, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    private void uploadAllTopics() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String[] topics = {
                "Lion with a Crown", "Elephant on a Bicycle", "Cat in a Box", "Penguin with a Scarf",
                "Giraffe with a Tie", "Octopus playing Guitar", "Polar Bear eating Ice Cream",
                "Pizza with Extra Cheese", "Giant Hamburger", "Colorful Sushi", "Space Rocket to the Moon",
                "Astronaut Floating", "Small Green Alien", "Fire Breathing Dragon", "The Eiffel Tower",
                "Pirate Ship at Sea", "Old Phone with a Dial", "Electric Guitar", "Cool Sneakers",
                "Alarm Clock", "Colorful Umbrella", "Red Race Car", "Hot Air Balloon",
                "Banana slipping on its Peel", "Sun with Sunglasses", "Cloud crying Rain",
                "Magic Wand", "Treasure Chest", "Dancing Flower", "Moon with a Nightcap"
        };

        for (String topicName : topics) {
            Map<String, Object> topicData = new HashMap<>();
            topicData.put("name", topicName);

            db.collection("topics").add(topicData);
        }
    }
}