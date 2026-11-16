package com.example.psas_aplikasi_kelompok_5

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.psas_aplikasi_kelompok_5.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private lateinit var db: AppDatabase
    private lateinit var dayButtonsDash: List<Button>
    private lateinit var dayButtonsSched: List<Button>

    private var currentCalendarMonth = 11 // 11 = November 2026, 12 = Desember 2026
    private var currentNotifFilter = "Semua"
    private var activeDaySchedule = "Senin"
    private var activeDashDay = "Senin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        db = AppDatabase(this)

        // Set Profil Siswa Sesuai Akun Login
        binding.tvStudentName.text = session.getSiswaNama()
        binding.tvProfileName.text = session.getSiswaNama()
        binding.tvStudentClassBadge.text = session.getKelasNama()
        binding.tvScheduleHeaderClass.text = "Kelas " + session.getKelasNama()

        // Foto Khusus Akun Opet (Rafasyah)
        val isOpetAccount = session.getSiswaNama().contains("Rafasyah", ignoreCase = true) || session.getUsername().lowercase().contains("opet")
        if (isOpetAccount) {
            val fotoOpetResId = resources.getIdentifier("foto_opet", "drawable", packageName)
            if (fotoOpetResId != 0) {
                binding.ivHeaderProfile.setImageResource(fotoOpetResId)
                binding.ivProfileMain.setImageResource(fotoOpetResId)
            }
        }

        // 1. Selector Hari Dashboard
        dayButtonsDash = listOf(binding.btnDaySenin, binding.btnDaySelasa, binding.btnDayRabu, binding.btnDayKamis, binding.btnDayJumat)
        binding.btnDaySenin.setOnClickListener { selectDayDashboard("Senin", binding.btnDaySenin) }
        binding.btnDaySelasa.setOnClickListener { selectDayDashboard("Selasa", binding.btnDaySelasa) }
        binding.btnDayRabu.setOnClickListener { selectDayDashboard("Rabu", binding.btnDayRabu) }
        binding.btnDayKamis.setOnClickListener { selectDayDashboard("Kamis", binding.btnDayKamis) }
        binding.btnDayJumat.setOnClickListener { selectDayDashboard("Jumat", binding.btnDayJumat) }
        selectDayDashboard("Senin", binding.btnDaySenin)

        // 2. Selector Hari Khusus Halaman Jadwal (Menampilkan 1 Hari Saja)
        dayButtonsSched = listOf(binding.btnSchedSenin, binding.btnSchedSelasa, binding.btnSchedRabu, binding.btnSchedKamis, binding.btnSchedJumat)
        binding.btnSchedSenin.setOnClickListener { loadScheduleForDay("Senin", binding.btnSchedSenin) }
        binding.btnSchedSelasa.setOnClickListener { loadScheduleForDay("Selasa", binding.btnSchedSelasa) }
        binding.btnSchedRabu.setOnClickListener { loadScheduleForDay("Rabu", binding.btnSchedRabu) }
        binding.btnSchedKamis.setOnClickListener { loadScheduleForDay("Kamis", binding.btnSchedKamis) }
        binding.btnSchedJumat.setOnClickListener { loadScheduleForDay("Jumat", binding.btnSchedJumat) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Cek Momentum Perubahan Jadwal
        val latestMomentum = db.getLatestMomentum()
        if (latestMomentum != null) {
            val title = "Perubahan Ruangan: " + latestMomentum["mapel_baru"] + "! 🔴"
            binding.tvMomentumTitle.text = title
            showNotification(102, title, "Ruang baru: " + latestMomentum["ruang_baru"] + " (" + latestMomentum["alasan"] + ")")
        }

        // Klik Card Momentum di Dashboard -> tampilkan detail & opsi kembalikan jadwal normal
        binding.cardNotifMomentum.setOnClickListener {
            val momentum = db.getLatestMomentum()
            if (momentum != null) {
                AlertDialog.Builder(this)
                    .setTitle("Perubahan Jadwal: " + momentum["mapel_baru"])
                    .setMessage(
                        "Mata Pelajaran: ${momentum["mapel_baru"]}\n" +
                                "Guru Pengganti: ${momentum["guru_baru"]}\n" +
                                "Ruangan: ${momentum["ruang_baru"]}\n" +
                                "Keterangan: ${momentum["alasan"]}\n\n" +
                                "Kembalikan jadwal ke kondisi reguler?"
                    )
                    .setPositiveButton("Kembalikan Normal") { _, _ ->
                        db.clearMomentum()
                        binding.tvMomentumTitle.text = "Pembaruan Jadwal Momentum"
                        Toast.makeText(this, "Jadwal berhasil dikembalikan ke semula!", Toast.LENGTH_LONG).show()
                        selectDayDashboard(activeDashDay, getDashButtonForDay(activeDashDay))
                        if (binding.viewFlipperPages.displayedChild == 3) loadNotificationPage()
                    }
                    .setNegativeButton("Tutup", null)
                    .show()
            } else {
                switchTab(3)
            }
        }

        // Tombol Pindah Bulan Kalender (November <-> Desember 2026)
        binding.btnPrevMonth.setOnClickListener {
            currentCalendarMonth = 11
            loadCalendarPage()
        }
        binding.btnNextMonth.setOnClickListener {
            currentCalendarMonth = 12
            loadCalendarPage()
        }

        // Filter Halaman Notifikasi (Semua / Akademik / Ruangan)
        binding.btnFilterNotifSemua.setOnClickListener {
            currentNotifFilter = "Semua"
            updateNotifFilterChips(binding.btnFilterNotifSemua)
            loadNotificationPage()
        }
        binding.btnFilterNotifAkademik.setOnClickListener {
            currentNotifFilter = "Akademik"
            updateNotifFilterChips(binding.btnFilterNotifAkademik)
            loadNotificationPage()
        }
        binding.btnFilterNotifRuangan.setOnClickListener {
            currentNotifFilter = "Ruangan"
            updateNotifFilterChips(binding.btnFilterNotifRuangan)
            loadNotificationPage()
        }

        binding.btnOpenSettingsFromProfile.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.btnLihatSemuaKalender.setOnClickListener { switchTab(2) }
        binding.fabAddEventCalendar.setOnClickListener {
            if (session.getRole().contains("Admin", ignoreCase = true)) {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                Toast.makeText(this, "Hanya Akun Admin/OSIS yang berwenang menambah event!", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigasi Bottom Navbar
        binding.tabHome.setOnClickListener { switchTab(0) }
        binding.tabCalendar.setOnClickListener { switchTab(2) }
        binding.tabSchedule.setOnClickListener { switchTab(1) }
        binding.tabNotif.setOnClickListener { switchTab(3) }
        binding.tabProfile.setOnClickListener { switchTab(4) }

        binding.btnLogout.setOnClickListener {
            session.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        applyGlobalAppTheme()
    }

    // =========================================================================
    // LOGIKA TEMA PRESISI (ANTI-SALAH WARNA TEXT HEADER & CARD!)
    // =========================================================================
    private fun applyGlobalAppTheme() {
        val isDark = session.isDarkMode()

        val bgMainColor = if (isDark) Color.parseColor("#111827") else Color.parseColor("#EEF2F6")
        val cardBgColor = if (isDark) Color.parseColor("#1F2937") else Color.WHITE
        val textTitleColor = if (isDark) Color.WHITE else Color.parseColor("#0F172A")
        val textSubColor = if (isDark) Color.parseColor("#CBD5E1") else Color.parseColor("#64748B")

        binding.rootMainLayout.setBackgroundColor(bgMainColor)

        binding.tvCurrentSubjectTitle.setTextColor(textTitleColor)
        binding.tvCurrentTeacher.setTextColor(textSubColor)
        binding.tvMomentumTitle.setTextColor(textTitleColor)

        setCardBackgroundSafe(binding.cardNotifMomentum, if (isDark) Color.parseColor("#3B1F24") else Color.parseColor("#FFF5F5"))
        val parentCardSchedule = binding.tvCurrentSubjectTitle.parent?.parent as? CardView
        parentCardSchedule?.let { setCardBackgroundSafe(it, cardBgColor) }

        val parentCardCal = binding.btnLihatSemuaKalender.parent?.parent?.parent as? CardView
        parentCardCal?.let { setCardBackgroundSafe(it, cardBgColor) }

        // Menggunakan ViewCompat agar aman untuk semua versi Android (bebas error API 21!)
        ViewCompat.setBackgroundTintList(binding.layoutBottomNavContainer, ColorStateList.valueOf(if (isDark) Color.parseColor("#1E293B") else Color.WHITE))

        selectDayDashboard(activeDashDay, getDashButtonForDay(activeDashDay))
        when (binding.viewFlipperPages.displayedChild) {
            1 -> loadScheduleForDay(activeDaySchedule, getButtonForDay(activeDaySchedule))
            2 -> loadCalendarPage()
            3 -> loadNotificationPage()
        }
    }

    private fun setCardBackgroundSafe(card: CardView, color: Int) {
        card.setCardBackgroundColor(color)
        val child = card.getChildAt(0)
        if (child is LinearLayout) {
            child.setBackgroundColor(color)
        }
    }

    private fun getButtonForDay(day: String): Button {
        return when (day) {
            "Selasa" -> binding.btnSchedSelasa
            "Rabu" -> binding.btnSchedRabu
            "Kamis" -> binding.btnSchedKamis
            "Jumat" -> binding.btnSchedJumat
            else -> binding.btnSchedSenin
        }
    }

    private fun getDashButtonForDay(day: String): Button {
        return when (day) {
            "Selasa" -> binding.btnDaySelasa
            "Rabu" -> binding.btnDayRabu
            "Kamis" -> binding.btnDayKamis
            "Jumat" -> binding.btnDayJumat
            else -> binding.btnDaySenin
        }
    }

    private fun switchTab(pageIndex: Int) {
        binding.viewFlipperPages.displayedChild = pageIndex
        binding.fabAddEventCalendar.visibility = if (pageIndex == 2) View.VISIBLE else View.GONE

        val allTabs = listOf(binding.tabHome, binding.tabCalendar, binding.tabSchedule, binding.tabNotif, binding.tabProfile)
        val allTexts = listOf(binding.navTextHome, binding.navTextCalendar, binding.navTextSchedule, binding.navTextNotif, binding.navTextProfile)

        for (i in allTabs.indices) {
            allTabs[i].background = null
            allTexts[i].setTextColor(Color.parseColor("#64748B"))
            allTexts[i].paint.isFakeBoldText = false
        }

        val activeTabIndex = when (pageIndex) {
            0 -> 0
            2 -> 1
            1 -> 2
            3 -> 3
            else -> 4
        }

        allTabs[activeTabIndex].setBackgroundResource(R.drawable.bg_nav_active_pill)
        allTexts[activeTabIndex].setTextColor(Color.parseColor("#DC2626"))
        allTexts[activeTabIndex].paint.isFakeBoldText = true

        if (pageIndex == 1) loadScheduleForDay(activeDaySchedule, getButtonForDay(activeDaySchedule))
        if (pageIndex == 2) loadCalendarPage()
        if (pageIndex == 3) loadNotificationPage()
    }

    private fun selectDayDashboard(hari: String, selectedBtn: Button) {
        activeDashDay = hari
        val isDark = session.isDarkMode()
        for (btn in dayButtonsDash) {
            ViewCompat.setBackgroundTintList(btn, ColorStateList.valueOf(if (isDark) Color.parseColor("#1F2937") else Color.WHITE))
            btn.setTextColor(if (isDark) Color.WHITE else Color.parseColor("#0F172A"))
        }
        ViewCompat.setBackgroundTintList(selectedBtn, ColorStateList.valueOf(if (isDark) Color.parseColor("#7F1D1D") else Color.parseColor("#DC2626")))
        selectedBtn.setTextColor(Color.WHITE)

        val daily = db.getScheduleByClass(session.getKelasId()).filter { it["mapel"]?.contains("[$hari]") == true }
        if (daily.isNotEmpty()) {
            val p1 = daily[0]
            binding.tvCurrentSubjectTitle.text = p1["mapel"]?.replace("[$hari] ", "")
            binding.tvCurrentStatusTime.text = "🟢 " + p1["waktu"] + " WIB"
            binding.tvCurrentTeacher.text = "👤 " + p1["guru"]
            binding.tvCurrentRoomBadge.text = "🏢 " + p1["ruang"]
            binding.tvNextPeriodPreview.text = if (daily.size > 1) "Berikutnya: ${daily[1]["mapel"]?.replace("[$hari] ", "")} (${daily[1]["waktu"]})" else "Sesi terakhir hari ini"
        }
    }

    private fun loadScheduleForDay(hari: String, selectedBtn: Button) {
        activeDaySchedule = hari
        val isDark = session.isDarkMode()

        for (btn in dayButtonsSched) {
            ViewCompat.setBackgroundTintList(btn, ColorStateList.valueOf(Color.parseColor("#40FFFFFF")))
            btn.setTextColor(Color.WHITE)
        }
        ViewCompat.setBackgroundTintList(selectedBtn, ColorStateList.valueOf(Color.WHITE))
        selectedBtn.setTextColor(Color.parseColor(if (isDark) "#7F1D1D" else "#DC2626"))

        binding.tvSelectedDayLabel.text = "$hari Akademik"
        binding.tvSelectedDayLabel.setTextColor(if (isDark) Color.WHITE else Color.parseColor("#0F172A"))

        val container = binding.scheduleContainer
        container.removeAllViews()

        val daily = db.getScheduleByClass(session.getKelasId()).filter { it["mapel"]?.contains("[$hari]") == true }

        for ((index, item) in daily.withIndex()) {
            val itemView = layoutInflater.inflate(R.layout.item_timeline_lesson, container, false)
            val tvSubject = itemView.findViewById<TextView>(R.id.tvTimelineSubject)
            val tvBadgeStatus = itemView.findViewById<TextView>(R.id.tvTimelineStatusBadge)
            val tvTime = itemView.findViewById<TextView>(R.id.tvTimelineTime)
            val tvTeacher = itemView.findViewById<TextView>(R.id.tvTimelineTeacher)
            val tvRoom = itemView.findViewById<TextView>(R.id.tvTimelineRoom)
            val card = itemView.findViewById<CardView>(R.id.cardTimelineLesson)

            card.setCardBackgroundColor(if (isDark) Color.parseColor("#1F2937") else Color.WHITE)
            tvSubject.setTextColor(if (isDark) Color.WHITE else Color.parseColor("#0F172A"))
            tvTime.setTextColor(if (isDark) Color.parseColor("#CBD5E1") else Color.parseColor("#64748B"))
            tvTeacher.setTextColor(if (isDark) Color.parseColor("#CBD5E1") else Color.parseColor("#64748B"))

            val fullMapel = item["mapel"]?.replace("[$hari] ", "") ?: ""
            val isMomentum = item["isMomentum"] == "true"

            tvSubject.text = fullMapel
            tvTime.text = "⏱️ " + item["waktu"] + " WIB"
            tvTeacher.text = "Guru: " + item["guru"]
            tvRoom.text = "RUANG " + item["ruang"]

            if (isMomentum) {
                tvBadgeStatus.text = "MOMENTUM"
                tvBadgeStatus.background = roundedBg(Color.parseColor("#EA580C"), 10f)
                tvBadgeStatus.setTextColor(Color.WHITE)
            } else if (index == 0) {
                tvBadgeStatus.text = "SEDANG BERLANGSUNG"
                tvBadgeStatus.background = roundedBg(Color.parseColor("#DCFCE7"), 10f)
                tvBadgeStatus.setTextColor(Color.parseColor("#059669"))
            } else {
                tvBadgeStatus.text = "AKAN DATANG"
                tvBadgeStatus.background = roundedBg(Color.parseColor(if (isDark) "#334155" else "#F1F5F9"), 10f)
                tvBadgeStatus.setTextColor(Color.parseColor(if (isDark) "#CBD5E1" else "#64748B"))
            }

            card.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Rincian Mata Pelajaran ($hari)")
                    .setMessage("Mata Pelajaran: $fullMapel\nWaktu: ${item["waktu"]} WIB\nGuru: ${item["guru"]}\nRuangan: ${item["ruang"]}")
                    .setPositiveButton("Tutup", null)
                    .show()
            }
            container.addView(itemView)
        }
    }

    private fun loadCalendarPage() {
        val isDark = session.isDarkMode()
        val grid = binding.layoutFullMonthGrid
        grid.removeAllViews()

        val totalDays = if (currentCalendarMonth == 11) 30 else 31
        binding.tvCalendarMonthTitle.text = if (currentCalendarMonth == 11) "November 2026" else "Desember 2026"
        binding.tvCalendarMonthTitle.setTextColor(if (isDark) Color.WHITE else Color.parseColor("#0F172A"))

        var dayCounter = 1
        val startOffset = if (currentCalendarMonth == 11) 6 else 1

        while (dayCounter <= totalDays) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 7f
            }

            for (col in 0..6) {
                val cell = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 96, 1f).apply { setMargins(4, 4, 4, 4) }
                    gravity = Gravity.CENTER
                    textSize = 13f
                }

                if ((grid.childCount == 0 && col < startOffset) || dayCounter > totalDays) {
                    cell.text = ""
                } else {
                    cell.text = dayCounter.toString()
                    cell.setTextColor(if (col == 6) Color.parseColor("#DC2626") else if (isDark) Color.WHITE else Color.parseColor("#0F172A"))

                    if (currentCalendarMonth == 11 && dayCounter == 16) {
                        // Hari ini: lingkaran merah penuh seperti mockup
                        cell.background = roundedBg(Color.parseColor("#DC2626"), 14f)
                        cell.setTextColor(Color.WHITE)
                        cell.paint.isFakeBoldText = true
                    } else if (currentCalendarMonth == 11 && (dayCounter == 5 || dayCounter == 6)) {
                        cell.background = roundedBg(Color.parseColor("#FEE2E2"), 14f)
                        cell.setTextColor(Color.parseColor("#DC2626"))
                        cell.paint.isFakeBoldText = true
                    } else if (currentCalendarMonth == 11 && dayCounter == 10) {
                        cell.background = roundedBg(Color.parseColor("#FEF3C7"), 14f)
                        cell.setTextColor(Color.parseColor("#B45309"))
                        cell.paint.isFakeBoldText = true
                    } else if (currentCalendarMonth == 11 && dayCounter in 20..28) {
                        cell.background = roundedBg(Color.parseColor("#DBEAFE"), 14f)
                        cell.setTextColor(Color.parseColor("#1E40AF"))
                    } else if (currentCalendarMonth == 12 && dayCounter >= 20) {
                        cell.background = roundedBg(Color.parseColor("#DCFCE7"), 14f)
                        cell.setTextColor(Color.parseColor("#059669"))
                    }
                    dayCounter++
                }
                row.addView(cell)
            }
            grid.addView(row)
        }

        val eventsContainer = binding.calendarEventsContainer
        eventsContainer.removeAllViews()
        val accentColors = listOf("#DC2626", "#F59E0B", "#2563EB", "#059669")
        for ((i, event) in db.getEvents().withIndex()) {
            val accent = Color.parseColor(accentColors[i % accentColors.size])
            val tvEvent = TextView(this).apply {
                text = "${event["nama"]}\n🏷️ ${event["kategori"]}   •   🗓️ ${event["tanggal"]}"
                textSize = 13f
                setLineSpacing(6f, 1f)
                setTextColor(if (isDark) Color.WHITE else Color.parseColor("#0F172A"))
                setPadding(40, 32, 32, 32)
                // Kartu membulat dengan garis aksen di sisi kiri (gaya agenda di mockup)
                val card = roundedBg(if (isDark) Color.parseColor("#1F2937") else Color.WHITE, 16f, if (isDark) Color.parseColor("#334155") else Color.parseColor("#E2E8F0"))
                val stripe = roundedBg(accent, 16f)
                background = android.graphics.drawable.LayerDrawable(arrayOf(stripe, card)).apply {
                    setLayerInset(1, (5 * resources.displayMetrics.density).toInt(), 0, 0, 0)
                }
                ViewCompat.setElevation(this, 2f)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 0, 14)
                layoutParams = params
            }
            eventsContainer.addView(tvEvent)
        }
    }

    private fun updateNotifFilterChips(activeBtn: TextView) {
        val chips = listOf(binding.btnFilterNotifSemua, binding.btnFilterNotifAkademik, binding.btnFilterNotifRuangan)
        for (c in chips) {
            c.setBackgroundResource(R.drawable.bg_chip_glass)
            c.setTextColor(Color.WHITE)
        }
        activeBtn.setBackgroundResource(R.drawable.bg_chip_white)
        activeBtn.setTextColor(Color.parseColor("#DC2626"))
    }

    // Helper: latar belakang sudut membulat (gaya kartu/pill seperti mockup)
    private fun roundedBg(fillColor: Int, radiusDp: Float, strokeColor: Int? = null): GradientDrawable {
        val density = resources.displayMetrics.density
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusDp * density
            setColor(fillColor)
            if (strokeColor != null) setStroke((1 * density).toInt(), strokeColor)
        }
    }

    private fun loadNotificationPage() {
        val isDark = session.isDarkMode()
        val container = binding.layoutNotifHistoryContainer
        container.removeAllViews()

        val cardBg = if (isDark) Color.parseColor("#1F2937") else Color.WHITE
        val textPrimary = if (isDark) Color.WHITE else Color.parseColor("#0F172A")
        val textSec = if (isDark) Color.parseColor("#CBD5E1") else Color.parseColor("#64748B")

        if (currentNotifFilter == "Semua" || currentNotifFilter == "Akademik") {
            val itemAkademik = layoutInflater.inflate(R.layout.item_single_lesson, container, false) as CardView
            itemAkademik.setCardBackgroundColor(cardBg)
            itemAkademik.findViewById<TextView>(R.id.tvSingleSubject).apply {
                text = "⏰ [Akademik] Pengingat Pergantian Jam (10 Menit)"
                setTextColor(textPrimary)
            }
            itemAkademik.findViewById<TextView>(R.id.tvSingleTime).apply {
                text = "Hari Ini • Jadwal Belajar & Pergantian Mata Pelajaran"
                setTextColor(textSec)
            }
            itemAkademik.findViewById<TextView>(R.id.tvSingleRoom).apply {
                text = "Sesi Mata Pelajaran berikutnya akan segera dimulai. Harap persiapkan perlengkapan belajar."
                setTextColor(textSec)
            }
            container.addView(itemAkademik)
        }

        if (currentNotifFilter == "Semua" || currentNotifFilter == "Ruangan") {
            val latestMomentum = db.getLatestMomentum()
            val itemRuang = layoutInflater.inflate(R.layout.item_single_lesson, container, false) as CardView
            itemRuang.setCardBackgroundColor(if (isDark) Color.parseColor("#3B1F24") else Color.parseColor("#FFF5F5"))
            itemRuang.findViewById<TextView>(R.id.tvSingleSubject).apply {
                text = "🏢 [Ruangan] Info Perpindahan Ruangan Lab"
                setTextColor(textPrimary)
            }
            itemRuang.findViewById<TextView>(R.id.tvSingleTime).apply {
                text = "Update Real-time Sarpras & Kurikulum"
                setTextColor(textSec)
            }
            itemRuang.findViewById<TextView>(R.id.tvSingleRoom).apply {
                text = if (latestMomentum != null) {
                    "Mapel ${latestMomentum["mapel_baru"]} dialihkan ke ${latestMomentum["ruang_baru"]} (${latestMomentum["alasan"]})"
                } else {
                    "Seluruh laboratorium RPL dan ruang kelas dalam kondisi siap pakai sesuai jadwal reguler."
                }
                setTextColor(textSec)
            }
            container.addView(itemRuang)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(notifId: Int, title: String, message: String) {
        val channelId = "school_schedule_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Jadwal dan Agenda Sekolah", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notifId, notification)
    }
}