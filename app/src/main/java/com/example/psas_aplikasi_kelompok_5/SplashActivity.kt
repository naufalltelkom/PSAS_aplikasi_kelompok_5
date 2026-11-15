package com.example.psas_aplikasi_kelompok_5

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.psas_aplikasi_kelompok_5.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        // CEK PERSISTEN 1X LOGIN (DIPERBAIKI SECARA AKURAT)
        if (session.isLoggedIn()) {
            val role = session.getRole()
            // Jika role mengandung kata "Admin" (Kurikulum / Organisasi / OSIS), SELALU BUKA ADMIN PANEL!
            if (role.contains("Admin", ignoreCase = true)) {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
            return
        }

        binding.btnGetStarted.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}