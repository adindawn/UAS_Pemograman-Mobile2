package com.example.memoaese_;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText editTextJudul, editTextKonten;
    private Button buttonHapus, buttonSimpanLayout;
    private ImageButton buttonBack;
    private FloatingActionButton buttonVoice;

    // Variabel untuk melacak EditText mana yang sedang difokuskan
    private EditText lastFocusedEditText;

    private boolean isEditMode = false;
    private long noteId = -1;
    private DatabaseHelper dbHelper;

    private static final String PREFS_NAME = "MemoDraft";
    private static final int SPEECH_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        // --- 1. PENGATURAN STATUS BAR ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- 2. INISIALISASI DATABASE & UI ---
        dbHelper = new DatabaseHelper(this);

        editTextJudul = findViewById(R.id.edit_text_judul);
        editTextKonten = findViewById(R.id.edit_text_konten);
        buttonHapus = findViewById(R.id.button_hapus);
        buttonSimpanLayout = findViewById(R.id.button_simpan);
        buttonBack = findViewById(R.id.button_back_create);
        buttonVoice = findViewById(R.id.button_voice);

        if (editTextJudul == null || editTextKonten == null) {
            Toast.makeText(this, "Error: Komponen UI tidak ditemukan!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // --- 3. LOGIKA TRACKING FOKUS (PENTING AGAR VOICE BISA ISI JUDUL) ---
        // Default fokus awal ke konten jika ingin bicara langsung
        lastFocusedEditText = editTextKonten;

        editTextJudul.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) lastFocusedEditText = editTextJudul;
        });

        editTextKonten.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) lastFocusedEditText = editTextKonten;
        });

        // --- 4. LOGIKA KLIK TOMBOL ---
        if (buttonSimpanLayout != null) {
            buttonSimpanLayout.setOnClickListener(v -> saveNotePermanently());
        }

        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> onBackPressed());
        }

        if (buttonVoice != null) {
            buttonVoice.setOnClickListener(v -> startVoiceInput());
        }

        handleIntent();

        if (buttonHapus != null) {
            buttonHapus.setOnClickListener(v -> showDeleteConfirmationDialog());
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Ubah teks petunjuk sesuai kolom yang difokuskan
        String hint = (lastFocusedEditText == editTextJudul) ? "Bicara untuk Judul..." : "Bicara untuk Isi Catatan...";
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, hint);

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Perangkat Anda tidak mendukung fitur suara", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);

                // Gunakan lastFocusedEditText (Bisa Judul atau Konten)
                EditText targetET = lastFocusedEditText;

                String currentText = targetET.getText().toString();
                int cursorPosition = targetET.getSelectionStart();

                // Tambahkan spasi jika sudah ada teks di posisi kursor
                if (!currentText.isEmpty() && cursorPosition > 0) {
                    spokenText = " " + spokenText;
                }

                StringBuilder sb = new StringBuilder(currentText);
                sb.insert(cursorPosition, spokenText);

                targetET.setText(sb.toString());
                targetET.setSelection(cursorPosition + spokenText.length());
            }
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("NOTE_ID")) {
            isEditMode = true;
            noteId = intent.getLongExtra("NOTE_ID", -1);
            if (buttonHapus != null) buttonHapus.setVisibility(View.VISIBLE);
            loadNoteDataFromDatabase();
        } else {
            isEditMode = false;
            if (buttonHapus != null) buttonHapus.setVisibility(View.GONE);
            editTextJudul.setText("");
            editTextKonten.setText("");
            loadDraft();
        }
    }

    private void loadNoteDataFromDatabase() {
        for (Note note : dbHelper.getAllNotes()) {
            if (note.getId() == noteId) {
                editTextJudul.setText(note.getTitle());
                editTextKonten.setText(note.getContent());
                break;
            }
        }
    }

    private void saveNotePermanently() {
        String judul = editTextJudul.getText().toString().trim();
        String konten = editTextKonten.getText().toString().trim();

        if (judul.isEmpty()) {
            Toast.makeText(this, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            dbHelper.updateNote(noteId, judul, konten);
            Toast.makeText(this, "Catatan diperbarui!", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.insertNote(judul, konten);
            Toast.makeText(this, "Catatan disimpan!", Toast.LENGTH_SHORT).show();
            clearDraft();
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isEditMode) {
            saveDraft();
        }
    }

    private void saveDraft() {
        String judul = editTextJudul.getText().toString();
        String konten = editTextKonten.getText().toString();
        if (judul.isEmpty() && konten.isEmpty()) return;

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putString("draft_judul", judul)
                .putString("draft_konten", konten)
                .apply();
    }

    private void loadDraft() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String draftJudul = prefs.getString("draft_judul", "");
        String draftKonten = prefs.getString("draft_konten", "");

        if (!draftJudul.isEmpty() || !draftKonten.isEmpty()) {
            editTextJudul.setText(draftJudul);
            editTextKonten.setText(draftKonten);
        }
    }

    private void clearDraft() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Catatan")
                .setMessage("Yakin ingin menghapus catatan ini?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    if (isEditMode) dbHelper.deleteNote(noteId);
                    clearDraft();
                    Toast.makeText(this, "Catatan dihapus!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}