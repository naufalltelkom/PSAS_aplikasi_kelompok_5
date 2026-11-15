package com.example.psas_aplikasi_kelompok_5

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class CalendarActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var container: LinearLayout
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        container = findViewById(R.id.calendarEventsContainer)
        db = AppDatabase(this)
        session = SessionManager(this)

        loadCalendarData()
    }

    private fun loadCalendarData() {
        val events = db.getEvents()
        container.removeAllViews()

        // 1. MEWARNAI KOTAK TANGGAL SECARA DINAMIS SESUAI AGENDA YANG DITAMBAHKAN
        for (event in events) {
            val tglAngka = event["tanggal_angka"]?.toIntOrNull() ?: 0
            val warnaHex = event["warna_hex"] ?: "#FEE2E2"

            if (tglAngka in 1..30) {
                // Cari TextView tanggal di layout XML secara dinamis (ID tvDate1 s/d tvDate30 jika ada)
                val resId = resources.getIdentifier("tvDate$tglAngka", "id", packageName)
                if (resId != 0) {
                    val tvDateCell = findViewById<TextView>(resId)
                    tvDateCell?.setBackgroundColor(Color.parseColor(warnaHex))
                }
            }
        }

        // 2. TAMPILKAN DAFTAR AGENDA LENGKAP
        for (event in events) {
            val idAgenda = event["id_agenda"]?.toIntOrNull() ?: 0
            val nama = event["nama"] ?: ""
            val kategori = event["kategori"] ?: ""
            val tgl = event["tanggal"] ?: ""
            val lokasi = event["lokasi"] ?: ""

            val tvEvent = TextView(this).apply {
                text = "📅 $nama\nKategori: $kategori | Waktu: $tgl"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.text_primary))
                setPadding(32, 28, 32, 28)
                setBackgroundColor(ContextCompat.getColor(this@CalendarActivity, R.color.white))
                elevation = 2f

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 16)
                layoutParams = params

                // KLIK AGENDA: TAMPILKAN DETAIL + OPSI HAPUS KEGIATAN (FITUR CRUD DELETE)
                setOnClickListener {
                    val dialog = AlertDialog.Builder(this@CalendarActivity)
                        .setTitle(nama)
                        .setMessage("Kategori: $kategori\nTanggal: $tgl\nLokasi: $lokasi")
                        .setPositiveButton("Tutup", null)

                    // Jika yang membuka adalah Admin, beri tombol HAPUS
                    if (session.getRole().contains("Admin", ignoreCase = true)) {
                        dialog.setNegativeButton("🗑️ Hapus Agenda Ini") { _, _ ->
                            db.deleteEvent(idAgenda)
                            Toast.makeText(this@CalendarActivity, "Agenda '$nama' berhasil dihapus!", Toast.LENGTH_SHORT).show()
                            loadCalendarData() // Muat ulang tampilan
                        }
                    }
                    dialog.show()
                }
            }
            container.addView(tvEvent)
        }
    }
}