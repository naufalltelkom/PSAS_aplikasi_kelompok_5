package com.example.psas_aplikasi_kelompok_5

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class ScheduleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        val container = findViewById<LinearLayout>(R.id.scheduleContainer)
        val tvTitle = findViewById<TextView>(R.id.tvTitleSchedule)
        val session = SessionManager(this)
        val db = AppDatabase(this)

        tvTitle.text = "Lesson Schedule - " + session.getKelasNama()

        // Ambil jadwal lengkap berdasarkan kelas yang login (XI PPLG 4)
        val schedules = db.getScheduleByClass(session.getKelasId())
        container.removeAllViews()

        for (item in schedules) {
            val itemView = layoutInflater.inflate(R.layout.item_schedule, container, false)
            val card = itemView.findViewById<CardView>(R.id.cardSchedule)
            val tvSubject = itemView.findViewById<TextView>(R.id.tvSubject)
            val tvBadge = itemView.findViewById<TextView>(R.id.tvBadgeMomentum)
            val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
            val tvRoomTeacher = itemView.findViewById<TextView>(R.id.tvRoomAndTeacher)
            val tvDayBadge = itemView.findViewById<TextView>(R.id.tvDayBadge)

            val fullMapel = item["mapel"] ?: ""
            tvSubject.text = fullMapel
            tvTime.text = item["waktu"]
            tvRoomTeacher.text = "Ruang: " + item["ruang"] + " • " + item["guru"]

            // Logika Warna Badge & Card Sesuai Hari
            when {
                fullMapel.contains("[Senin]") -> {
                    tvDayBadge.text = "Mon"
                    card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_senin))
                    tvSubject.setTextColor(ContextCompat.getColor(this, R.color.white))
                    tvTime.setTextColor(ContextCompat.getColor(this, R.color.white))
                    tvRoomTeacher.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                fullMapel.contains("[Selasa]") -> {
                    tvDayBadge.text = "Tue"
                    card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_selasa))
                    tvSubject.setTextColor(ContextCompat.getColor(this, R.color.black))
                    tvTime.setTextColor(ContextCompat.getColor(this, R.color.black))
                    tvRoomTeacher.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                }
                fullMapel.contains("[Rabu]") -> {
                    tvDayBadge.text = "Wed"
                    card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_rabu))
                    tvSubject.setTextColor(ContextCompat.getColor(this, R.color.black))
                    tvTime.setTextColor(ContextCompat.getColor(this, R.color.black))
                    tvRoomTeacher.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                }
                fullMapel.contains("[Kamis]") -> {
                    tvDayBadge.text = "Thu"
                    card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_kamis))
                    tvSubject.setTextColor(ContextCompat.getColor(this, R.color.white))
                    tvTime.setTextColor(ContextCompat.getColor(this, R.color.white))
                    tvRoomTeacher.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                fullMapel.contains("[Jumat]") -> {
                    tvDayBadge.text = "Fri"
                    card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_jumat))
                    tvSubject.setTextColor(ContextCompat.getColor(this, R.color.text_cokelat_tua))
                    tvTime.setTextColor(ContextCompat.getColor(this, R.color.text_cokelat_tua))
                    tvRoomTeacher.setTextColor(ContextCompat.getColor(this, R.color.text_cokelat_tua))
                }
            }

            // Tampilkan Badge Oranye jika jadwal ini adalah momentum hasil gubahan Admin
            if (item["isMomentum"] == "true") {
                tvBadge.visibility = View.VISIBLE
            } else {
                tvBadge.visibility = View.GONE
            }

            container.addView(itemView)
        }
    }
}