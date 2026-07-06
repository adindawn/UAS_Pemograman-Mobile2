package com.example.memoaese_;import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Tambahkan ini
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adapter diperbarui untuk mendukung fitur pencarian (filtering),
 * dan tampilan ikon PIN untuk catatan yang disematkan.
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Note note);
    }

    public NotesAdapter(List<Note> noteList, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
        this.noteList = noteList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item_layout, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = noteList.get(position);
        holder.bind(currentNote);
    }

    @Override
    public int getItemCount() {
        return noteList != null ? noteList.size() : 0;
    }

    public void updateData(List<Note> newNotes) {
        this.noteList = newNotes;
        notifyDataSetChanged();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentPreviewTextView;
        ImageView pinImageView; // Referensi ikon pin

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.item_note_title);
            contentPreviewTextView = itemView.findViewById(R.id.item_note_content_preview);
            pinImageView = itemView.findViewById(R.id.img_pin); // Inisialisasi ikon pin
        }

        void bind(final Note note) {
            titleTextView.setText(note.getTitle());
            contentPreviewTextView.setText(note.getContent());

            // --- LOGIKA TAMPILAN PIN ---
            if (note.isPinned()) {
                pinImageView.setVisibility(View.VISIBLE); // Muncul jika dipin
            } else {
                pinImageView.setVisibility(View.GONE);    // Hilang jika tidak
            }

            // Klik Biasa
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(note);
                }
            });

            // Tekan Lama (Hold)
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(note);
                }
                return true;
            });
        }
    }
}