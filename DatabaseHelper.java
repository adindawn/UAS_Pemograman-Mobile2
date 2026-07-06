package com.example.memoaese_;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes_db";
    // Versi tetap 4 tidak apa-apa, tapi struktur di dalamnya kita bersihkan
    private static final int DATABASE_VERSION = 4;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Membuat tabel catatan
        db.execSQL("CREATE TABLE notes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "judul TEXT, " +
                "konten TEXT, " +
                "is_pinned INTEGER DEFAULT 0)");

        // Membuat tabel tugas (DEADLINE DIHAPUS)
        db.execSQL("CREATE TABLE tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "date TEXT, " +
                "is_completed INTEGER DEFAULT 0, " +
                "priority TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Jika Anda ingin membersihkan database lama yang bermasalah,
        // Anda bisa menggunakan DROP TABLE dan memanggil onCreate kembali.
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS tasks");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    // ================= FITUR CATATAN (NOTES) =================

    public long insertNote(String judul, String konten) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("judul", judul);
        values.put("konten", konten);
        values.put("is_pinned", 0);
        return db.insert("notes", null, values);
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM notes ORDER BY is_pinned DESC, id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                notes.add(new Note(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }

    public void updatePinStatus(long id, boolean isPinned) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_pinned", isPinned ? 1 : 0);
        db.update("notes", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void updateNote(long id, String judul, String konten) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("judul", judul);
        values.put("konten", konten);
        db.update("notes", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("notes", "id = ?", new String[]{String.valueOf(id)});
    }

    // ================= FITUR TUGAS (TASKS) =================

    public void addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", task.getTitle());
        values.put("date", task.getDate());
        // deadline dihapus dari sini
        values.put("is_completed", task.isCompleted() ? 1 : 0);
        values.put("priority", task.getPriority());

        db.insert("tasks", null, values);
        db.close();
    }

    /**
     * PENTING: Query ini menjamin Tugas Aktif di Atas dan Selesai di Bawah
     */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Urutan: is_completed (0: Belum, 1: Sudah), lalu ID terbaru
        Cursor cursor = db.rawQuery("SELECT * FROM tasks ORDER BY is_completed ASC, id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                tasks.add(new Task(
                        cursor.getInt(0),      // ID (index 0)
                        cursor.getString(1),   // Title (index 1)
                        cursor.getString(2),   // Date (index 2)
                        cursor.getInt(3) == 1, // is_completed (index 3)
                        cursor.getString(4)    // priority (index 4)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tasks;
    }

    public void updateTaskStatus(int id, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_completed", isCompleted ? 1 : 0);
        db.update("tasks", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tasks", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}