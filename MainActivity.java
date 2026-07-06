package com.example.memoaese_;

import android.content.Intent;import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private FloatingActionButton fabAddNote, fabTaskList;
    private NotesAdapter notesAdapter;
    private List<Note> noteList;
    private List<Note> fullNoteList;
    private DatabaseHelper dbHelper;
    private EditText searchNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        fabAddNote = findViewById(R.id.fab_add_note);
        fabTaskList = findViewById(R.id.fab_task_list);
        searchNote = findViewById(R.id.search_note);

        setupRecyclerView();
        setupFabActions();
        setupSearchAction();
        handleIncomingIntent();
    }

    private void setupRecyclerView() {
        noteList = new ArrayList<>();
        fullNoteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList,
                note -> {
                    Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
                    intent.putExtra("NOTE_ID", note.getId());
                    startActivity(intent);
                },
                note -> showNoteOptionsDialog(note) // Sekarang memanggil Menu Opsi
        );

        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(notesAdapter);
    }

    private void setupSearchAction() {
        searchNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterNotes(String query) {
        List<Note> filteredList = new ArrayList<>();
        for (Note note : fullNoteList) {
            if (note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    note.getContent().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(note);
            }
        }
        notesAdapter.updateData(filteredList);
    }

    // --- MENU OPSI: PIN ATAU HAPUS ---
    private void showNoteOptionsDialog(Note note) {
        String pinText = note.isPinned() ? "Lepas Pin (Unpin)" : "Sematkan Catatan (Pin)";
        String[] options = {pinText, "Hapus Catatan"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opsi Catatan");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Logika Pin/Unpin
                dbHelper.updatePinStatus(note.getId(), !note.isPinned());
                loadNotesFromSource(); // Refresh list agar urutan berubah
                Toast.makeText(this, note.isPinned() ? "Pin dilepas" : "Disematkan ke atas", Toast.LENGTH_SHORT).show();
            } else {
                // Logika Hapus
                showDeleteConfirmationDialog(note);
            }
        });

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_dialog);
        }
        dialog.show();
    }

    private void showDeleteConfirmationDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hapus Catatan");
        builder.setMessage("Yakin ingin menghapus catatan '" + note.getTitle() + "'?");

        builder.setPositiveButton("HAPUS", (d, which) -> {
            dbHelper.deleteNote(note.getId());
            loadNotesFromSource();
            Toast.makeText(this, "Catatan dihapus", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("BATAL", null);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_dialog);
        }
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }

    private void setupFabActions() {
        fabAddNote.setOnClickListener(v -> {
            hideExtraFabs();
            getSharedPreferences("MemoDraft", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
            startActivity(intent);
        });

        fabAddNote.setOnLongClickListener(v -> {
            if (fabTaskList.getVisibility() == View.GONE) {
                showExtraFabs();
            } else {
                hideExtraFabs();
            }
            return true;
        });

        fabTaskList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TaskListActivity.class);
            startActivity(intent);
            hideExtraFabs();
        });
    }

    private void showExtraFabs() {
        fabTaskList.setVisibility(View.VISIBLE);
        fabTaskList.setAlpha(0f);
        fabTaskList.animate().alpha(1f).setDuration(300).start();
    }

    private void hideExtraFabs() {
        fabTaskList.setVisibility(View.GONE);
    }

    private void handleIncomingIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("EXTRA_LATITUDE")) {
            Toast.makeText(this, "Lokasi diterima", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNotesFromSource() {
        fullNoteList = dbHelper.getAllNotes();
        String currentQuery = searchNote.getText().toString();
        if (currentQuery.isEmpty()) {
            notesAdapter.updateData(fullNoteList);
        } else {
            filterNotes(currentQuery);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesFromSource();
        hideExtraFabs();
    }
}