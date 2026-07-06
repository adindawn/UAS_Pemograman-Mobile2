package com.example.memoaese_;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Menjalankan timer
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Berpindah ke MainActivity
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);

                // Tambahkan animasi transisi (Opsional)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                // Tutup activity ini agar tidak bisa kembali ke splash saat tekan tombol back
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}