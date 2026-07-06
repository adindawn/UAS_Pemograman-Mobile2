package com.example.memoaese_;

public class Task {
    private int id;
    private String title;
    private String date;         // Tanggal pembuatan tugas
    private boolean isCompleted;
    private String priority;

    // 1. Constructor LENGKAP (Digunakan oleh DatabaseHelper saat mengambil data dari DB)
    // Hapus parameter deadline agar sesuai dengan tabel database yang baru
    public Task(int id, String title, String date, boolean isCompleted, String priority) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.isCompleted = isCompleted;
        this.priority = priority;
    }

    // 2. Constructor BARU (Digunakan saat membuat tugas baru dari Dialog)
    // Hapus parameter deadline di sini juga
    public Task(String title, String date, boolean isCompleted) {
        this.title = title;
        this.date = date;
        this.isCompleted = isCompleted;
        this.priority = "NORMAL"; // Nilai default
        this.id = 0;              // ID akan diisi otomatis oleh Database
    }

    // Getter dan Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
