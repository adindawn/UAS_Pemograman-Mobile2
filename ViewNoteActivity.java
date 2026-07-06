// Pastikan nama paket ini sesuai dengan struktur folder proyek Anda
package com.example.memoaese_;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ViewNoteActivity extends AppCompatActivity {

    private TextView textViewJudul, textViewKonten;
    private Button buttonEdit, buttonHapus;
    private ImageButton buttonBack;

    private NoteDatabaseHelper dbHelper;
    private int noteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        dbHelper = new NoteDatabaseHelper(this);

        // Inisialisasi semua elemen View
        textViewJudul = findViewById(R.id.text_view_judul);
        textViewKonten = findViewById(R.id.text_view_konten);
        buttonEdit = findViewById(R.id.button_edit);
        buttonHapus = findViewById(R.id.button_hapus);
        buttonBack = findViewById(R.id.button_back);

        // Ambil ID Catatan dari Intent.
        // Cek hanya dilakukan sekali saat activity dibuat.
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("NOTE_ID")) {
            noteId = intent.getIntExtra("NOTE_ID", -1);
        }

        // Jika ID tidak valid, hentikan activity.
        if (noteId == -1) {
            Toast.makeText(this, "Kesalahan: Gagal memuat catatan.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Atur listener untuk semua tombol
        setupButtonListeners();
    }

    /**
     * PENAMBAHAN: onResume() akan dipanggil setiap kali activity kembali ke foreground.
     * Ini memastikan jika ada perubahan data (misal setelah edit), tampilan akan diperbarui.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Muat ulang data catatan jika noteId valid.
        if (noteId != -1) {
            loadNoteData(noteId);
        }
    }

    /**
     * PERBAIKAN: Menggunakan blok try-finally untuk memastikan database dan cursor selalu ditutup.
     * Ini mencegah kebocoran sumber daya (resource leak).
     */
    private void loadNoteData(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null; // Inisialisasi cursor di luar blok try
        try {
            cursor = db.query(
                    NoteDatabaseHelper.TABLE_NAME,
                    new String[]{NoteDatabaseHelper.COLUMN_TITLE, NoteDatabaseHelper.COLUMN_CONTENT},
                    NoteDatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(NoteDatabaseHelper.COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(NoteDatabaseHelper.COLUMN_CONTENT));

                textViewJudul.setText(title);
                textViewKonten.setText(content);
            } else {
                Toast.makeText(this, "Catatan tidak ditemukan lagi.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } finally {
            // Pastikan cursor ditutup jika tidak null
            if (cursor != null) {
                cursor.close();
            }
            // Selalu tutup koneksi database
            if (db != null) {
                db.close();
            }
        }
    }

    private void setupButtonListeners() {
        buttonBack.setOnClickListener(v -> finish()); // Menggunakan finish() lebih efisien daripada onBackPressed()

        buttonEdit.setOnClickListener(v -> {
            Intent editIntent = new Intent(ViewNoteActivity.this, CreateNoteActivity.class);
            editIntent.putExtra("NOTE_ID", noteId);
            startActivity(editIntent);
            // Tidak perlu finish() di sini, agar pengguna bisa kembali dari mode edit ke view
        });

        buttonHapus.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Catatan")
                .setMessage("Apakah Anda yakin ingin menghapus catatan ini?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteNote())
                .setNegativeButton("Batal", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * PERBAIKAN: Menggunakan try-finally juga di sini untuk keamanan.
     */
    private void deleteNote() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rowsDeleted = db.delete(
                    NoteDatabaseHelper.TABLE_NAME,
                    NoteDatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(noteId)}
            );

            if (rowsDeleted > 0) {
                Toast.makeText(this, "Catatan berhasil dihapus!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Kirim sinyal ke MainActivity untuk refresh
                finish();
            } else {
                Toast.makeText(this, "Gagal menghapus catatan.", Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * PENAMBAHAN: Pastikan untuk menutup database helper saat activity dihancurkan
     * untuk melepas semua sumber daya yang dialokasikan.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
