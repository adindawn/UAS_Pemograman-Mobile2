// Pastikan nama paket ini sesuai dengan struktur folder proyek Anda
package com.example.memoaese_;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Kelas helper untuk mengelola pembuatan dan pembaruan versi database.
 * Kelas ini mewarisi SQLiteOpenHelper, yang menyederhanakan manajemen database.
 */
public class NoteDatabaseHelper extends SQLiteOpenHelper {

    // Nama file database yang akan disimpan di penyimpanan internal perangkat.
    private static final String DATABASE_NAME = "notes_database.db";

    // Versi database. Jika Anda mengubah struktur tabel (misal: menambah kolom),
    // Anda harus menaikkan nomor versi ini menjadi 2, 3, dan seterusnya.
    private static final int DATABASE_VERSION = 1;

    // Definisikan nama tabel dan kolom-kolomnya sebagai konstanta (public static final).
    // Ini adalah praktik terbaik untuk menghindari kesalahan ketik di seluruh aplikasi.
    public static final String TABLE_NAME = "notes";
    public static final String COLUMN_ID = "_id"; // Nama standar untuk primary key di Android
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";

    // Perintah SQL untuk membuat tabel "notes".
    // Perintah ini akan dieksekusi di dalam metode onCreate().
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_CONTENT + " TEXT);";

    /**
     * Konstruktor untuk NoteDatabaseHelper.
     * @param context Context dari aplikasi (biasanya 'this' dari sebuah Activity).
     */
    public NoteDatabaseHelper(Context context) {
        // Panggil konstruktor superclass untuk menginisialisasi helper.
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Metode ini hanya dipanggil SATU KALI, yaitu saat database dibuat untuk pertama kalinya.
     * Di sinilah kita mengeksekusi perintah SQL untuk membuat skema database awal.
     * @param db Objek database yang baru dibuat.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Eksekusi perintah pembuatan tabel.
        db.execSQL(TABLE_CREATE);
    }

    /**
     * Metode ini dipanggil ketika DATABASE_VERSION dinaikkan.
     * Fungsinya untuk memperbarui skema database tanpa menghapus data pengguna (jika diperlukan).
     * @param db Objek database.
     * @param oldVersion Nomor versi database lama.
     * @param newVersion Nomor versi database baru.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Untuk implementasi sederhana, kita hapus saja tabel lama dan buat yang baru.
        // PERINGATAN: Ini akan menghapus semua data yang ada!
        // Di aplikasi produksi, Anda perlu membuat skrip migrasi untuk menyimpan data pengguna.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Buat kembali tabel dengan skema yang baru.
        onCreate(db);
    }
}
