package katzman.yuval.artdash;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvBio, tvTotalStars, tvDrawingCount;
    private ImageView ivProfilePicture, ivTop1, ivTop2, ivTop3;
    private RecyclerView rvMyGallery;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId;
    private SharedPreferences prefs;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    handlePickedImage(result.getData().getData());
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvBio = view.findViewById(R.id.tvBio);
        tvTotalStars = view.findViewById(R.id.tvTotalStars);
        tvDrawingCount = view.findViewById(R.id.tvDrawingCount);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        ivTop1 = view.findViewById(R.id.ivTop1);
        ivTop2 = view.findViewById(R.id.ivTop2);
        ivTop3 = view.findViewById(R.id.ivTop3);
        rvMyGallery = view.findViewById(R.id.rvMyGallery);

        prefs = getActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        rvMyGallery.setLayoutManager(new GridLayoutManager(getContext(), 3));
        currentUserId = FirebaseAuth.getInstance().getUid();

        loadLocalData();

        if (currentUserId != null) {
            observeUserProfile();
            loadUserGallery();
        }

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditProfileDialog());
        view.findViewById(R.id.btnEditAvatar).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        return view;
    }

    private void loadLocalData() {
        tvUsername.setText(prefs.getString("name", "ARTIST NAME"));
        tvBio.setText(prefs.getString("bio", "BIO HERE"));
        tvTotalStars.setText(String.valueOf(prefs.getInt("totalStars", 0)));
        String imageBase64 = prefs.getString("profileImage", null);
        if (imageBase64 != null) {
            ivProfilePicture.setImageBitmap(decodeBase64ToBitmap(imageBase64));
        }
    }

    private void observeUserProfile() {
        db.collection("User").document(currentUserId).addSnapshotListener((doc, error) -> {
            if (doc != null && doc.exists()) {
                String name = doc.getString("name");
                String bio = doc.getString("bio") != null ? doc.getString("bio") : "";
                String profileImage = doc.getString("profileImage");
                Double stars = doc.getDouble("totalStars");
                int starsInt = (stars != null) ? stars.intValue() : 0;

                tvUsername.setText(name);
                tvBio.setText(bio);
                tvTotalStars.setText(String.valueOf(starsInt));

                if (profileImage != null) {
                    ivProfilePicture.setImageBitmap(decodeBase64ToBitmap(profileImage));
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("name", name);
                editor.putString("bio", bio);
                editor.putInt("totalStars", starsInt);
                editor.putString("profileImage", profileImage);
                editor.apply();
            }
        });
    }

    private void handlePickedImage(Uri imageUri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ivProfilePicture.setImageBitmap(bitmap);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            db.collection("User").document(currentUserId).update("profileImage", base64Image);
            prefs.edit().putString("profileImage", base64Image).apply();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to upload", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile");
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etName = new EditText(getContext());
        etName.setHint("Name");
        etName.setText(tvUsername.getText().toString());
        layout.addView(etName);

        final EditText etBio = new EditText(getContext());
        etBio.setHint("Bio");
        etBio.setText(tvBio.getText().toString());
        layout.addView(etBio);

        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etName.getText().toString();
            String newBio = etBio.getText().toString();

            Map<String, Object> update = new HashMap<>();
            update.put("name", newName);
            update.put("bio", newBio);
            db.collection("User").document(currentUserId).update(update);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadUserGallery() {
        db.collection("User").document(currentUserId).collection("myDrawings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        List<DocumentSnapshot> docs = value.getDocuments();
                        tvDrawingCount.setText(String.valueOf(docs.size()));
                        GalleryAdapter adapter = new GalleryAdapter(docs, this::showImageDetailDialog);
                        rvMyGallery.setAdapter(adapter);
                        updateTopPicks(docs);
                    }
                });
    }

    private void updateTopPicks(List<DocumentSnapshot> docs) {
        ivTop1.setImageDrawable(null); ivTop2.setImageDrawable(null); ivTop3.setImageDrawable(null);
        int count = 1;
        for (DocumentSnapshot doc : docs) {
            Boolean isTop = doc.getBoolean("isTopPick");
            if (isTop != null && isTop) {
                Bitmap bitmap = decodeBase64ToBitmap(doc.getString("imageData"));
                if (count == 1) setupTopImage(ivTop1, bitmap);
                else if (count == 2) setupTopImage(ivTop2, bitmap);
                else if (count == 3) setupTopImage(ivTop3, bitmap);
                count++;
            }
        }
    }

    private void setupTopImage(ImageView iv, Bitmap bitmap) {
        iv.setImageBitmap(bitmap);
        iv.setOnClickListener(v -> showFullScreenImage(bitmap));
    }

    private void showFullScreenImage(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView iv = new ImageView(getContext());
        iv.setImageBitmap(bitmap);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        AlertDialog dialog = builder.setView(iv).create();
        iv.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showImageDetailDialog(DocumentSnapshot doc, Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_image_detail, null);
        ImageView ivLarge = view.findViewById(R.id.ivLargeImage);
        Button btnToggleTop = view.findViewById(R.id.btnToggleTop);
        ivLarge.setImageBitmap(bitmap);
        ivLarge.setOnClickListener(v -> showFullScreenImage(bitmap));
        boolean isTop = doc.getBoolean("isTopPick") != null && doc.getBoolean("isTopPick");
        btnToggleTop.setText(isTop ? "Remove from Hall of Fame" : "Add to Hall of Fame");
        AlertDialog dialog = builder.setView(view).create();
        btnToggleTop.setOnClickListener(v -> {
            doc.getReference().update("isTopPick", !isTop);
            dialog.dismiss();
        });
        dialog.show();
    }

    private Bitmap decodeBase64ToBitmap(String base64Str) {
        if (base64Str == null || base64Str.isEmpty()) return null;
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) { return null; }
    }
}