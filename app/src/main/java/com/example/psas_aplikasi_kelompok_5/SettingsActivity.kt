package com.example.psas_aplikasi_kelompok_5

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val session = SessionManager(this)
        val rootScroll = findViewById<ScrollView>(R.id.rootSettingsLayout)
        val btnBack = findViewById<TextView>(R.id.btnBackFromSettings)
        val cardLight = findViewById<LinearLayout>(R.id.cardThemeLight)
        val cardDark = findViewById<LinearLayout>(R.id.cardThemeDark)
        val tvStatusLight = findViewById<TextView>(R.id.tvStatusLight)
        val tvStatusDark = findViewById<TextView>(R.id.tvStatusDark)
        val tvThemeTitle = findViewById<TextView>(R.id.tvThemeTitle)
        val tvNotifTitle = findViewById<TextView>(R.id.tvNotifSettingsTitle)
        val btnSaveSettings = findViewById<Button>(R.id.btnSaveSettings)

        val btnTimer5 = findViewById<Button>(R.id.btnTimer5)
        val btnTimer10 = findViewById<Button>(R.id.btnTimer10)
        val btnTimer15 = findViewById<Button>(R.id.btnTimer15)

        // Fungsi Helper untuk update tampilan Settings
        fun applySettingsTheme(isDark: Boolean) {
            if (isDark) {
                rootScroll.setBackgroundColor(Color.parseColor("#0F172A"))
                tvThemeTitle.setTextColor(Color.WHITE)
                tvNotifTitle.setTextColor(Color.WHITE)
                tvStatusLight.text = "Tersedia"
                tvStatusDark.text = "Aktif (Gelap)"
                cardDark.setBackgroundResource(R.drawable.bg_badge_mode_active)
                cardLight.setBackgroundColor(Color.parseColor("#334155"))
            } else {
                rootScroll.setBackgroundColor(Color.parseColor("#EEF2F6"))
                tvThemeTitle.setTextColor(Color.parseColor("#0F172A"))
                tvNotifTitle.setTextColor(Color.parseColor("#0F172A"))
                tvStatusLight.text = "Aktif"
                tvStatusDark.text = "Tersedia"
                cardLight.setBackgroundResource(R.drawable.bg_badge_mode_active)
                cardDark.setBackgroundColor(Color.parseColor("#E2E8F0"))
            }
        }

        // Terapkan tema saat halaman dibuka
        applySettingsTheme(session.isDarkMode())

        btnBack.setOnClickListener { finish() }

        cardLight.setOnClickListener {
            session.setDarkMode(false)
            applySettingsTheme(false)
            Toast.makeText(this, "Mode Terang Diaktifkan ke Seluruh Aplikasi!", Toast.LENGTH_SHORT).show()
        }

        cardDark.setOnClickListener {
            session.setDarkMode(true)
            applySettingsTheme(true)
            Toast.makeText(this, "Mode Gelap Diaktifkan ke Seluruh Aplikasi!", Toast.LENGTH_SHORT).show()
        }

        btnTimer5.setOnClickListener { Toast.makeText(this, "Pengingat: 5 Menit", Toast.LENGTH_SHORT).show() }
        btnTimer10.setOnClickListener { Toast.makeText(this, "Pengingat: 10 Menit", Toast.LENGTH_SHORT).show() }
        btnTimer15.setOnClickListener { Toast.makeText(this, "Pengingat: 15 Menit", Toast.LENGTH_SHORT).show() }

        btnSaveSettings.setOnClickListener {
            Toast.makeText(this, "Setelan Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}