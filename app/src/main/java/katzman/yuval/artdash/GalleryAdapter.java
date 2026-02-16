package katzman.yuval.artdash;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private List<DocumentSnapshot> drawingDocs;
    private OnItemClickListener listener;

    public GalleryAdapter(List<DocumentSnapshot> documents) {
    }


    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot document, Bitmap bitmap);
    }

    public GalleryAdapter(List<DocumentSnapshot> drawingDocs, OnItemClickListener listener) {
        this.drawingDocs = drawingDocs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());

        int size = parent.getWidth() / 3;
        imageView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(8, 8, 8, 8);

        return new GalleryViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        DocumentSnapshot doc = drawingDocs.get(position);
        String base64Image = doc.getString("imageData");

        if (base64Image != null) {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imageView.setImageBitmap(decodedByte);


            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(doc, decodedByte);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return drawingDocs.size();
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}