package com.example.memoaese_;

public class Note {private long id;
    private String title;
    private String content;
    private boolean isPinned; // Tambahan: Status Pin

    // Konstruktor diperbarui
    public Note(long id, String title, String content, boolean isPinned) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isPinned = isPinned;
    }

    // Getter untuk ID
    public long getId() {
        return id;
    }

    // Getter dan Setter untuk Judul
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter dan Setter untuk Isi Konten
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Getter dan Setter untuk Status Pin
    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }
}