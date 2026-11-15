package com.example.psas_aplikasi_kelompok_5

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.psas_aplikasi_kelompok_5.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private lateinit var db: AppDatabase
    private lateinit var dayButtons: List<Button>

    private val timetableCardViews = mutableListOf<CardView>()
    private var activeReminderTitle = "Pengingat Pergantian Jam (XI PPLG 4)"
    private var activeReminderMessage = "10 Menit lagi: Mapel Pilihan 1B selesai. Bersiap menuju sesi MK1 (Pak Fendi)!"
    private var timerMinutes = 10
    private var currentTabIndex = 0
    private var dX = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        db = AppDatabase(this)

        // Set Informasi Siswa Sesuai Konsep Desain Baru
        binding.tvStudentName.text = session.getSiswaNama()
        binding.tvStudentClass.text = "PPLG • " + session.getKelasNama()
        binding.tvProfileName.text = session.getSiswaNama()
        binding.tvProfileClass.text = "NIS: 20261104 • " + session.getRole()

        // 1. Background Jernih Murni (Akun Opet -> bg_opet, lainnya -> gedung_telkom)
        setupCustomUserBackground()

        // 2. Foto Profil Opet (Rafasyah)
        val isOpetAccount = session.getSiswaNama().contains("Rafasyah", ignoreCase = true) || session.getUsername().lowercase().contains("opet")
        if (isOpetAccount) {
            val fotoOpetResId = resources.getIdentifier("foto_opet", "drawable", packageName)
            if (fotoOpetResId != 0) {
                binding.ivHeaderProfile.setImageResource(fotoOpetResId)
                binding.ivProfileMain.setImageResource(fotoOpetResId)
            }
        }

        // Pilihan Hari
        dayButtons = listOf(
            binding.btnDaySenin,
            binding.btnDaySelasa,
            binding.btnDayRabu,
            binding.btnDayKamis,
            binding.btnDayJumat
        )

        binding.btnDaySenin.setOnClickListener { selectDay("Senin", binding.btnDaySenin) }
        binding.btnDaySelasa.setOnClickListener { selectDay("Selasa", binding.btnDaySelasa) }
        binding.btnDayRabu.setOnClickListener { selectDay("Rabu", binding.btnDayRabu) }
        binding.btnDayKamis.setOnClickListener { selectDay("Kamis", binding.btnDayKamis) }
        binding.btnDayJumat.setOnClickListener { selectDay("Jumat", binding.btnDayJumat) }

        selectDay("Senin", binding.btnDaySenin)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Cek Jadwal Momentum
        val latestMomentum = db.getLatestMomentum()
        if (latestMomentum != null) {
            val mapelBaru = latestMomentum["mapel_baru"] ?: ""
            val guruBaru = latestMomentum["guru_baru"] ?: ""
            val ruangBaru = latestMomentum["ruang_baru"] ?: ""
            val alasan = latestMomentum["alasan"] ?: ""

            val titleMomentum = "⚡ PERUBAHAN JADWAL: $mapelBaru"
            val descMomentum = "Pindah ke $ruangBaru bersama $guruBaru. Keterangan: $alasan"

            binding.tvMomentumTitle.text = titleMomentum
            showNotification(102, titleMomentum, descMomentum)

            binding.cardNotifMomentum.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle(titleMomentum)
                    .setMessage("Mata Pelajaran: $mapelBaru\nGuru Pengganti: $guruBaru\nRuangan: $ruangBaru\nKeterangan Kurikulum: $alasan\n\nApakah sesi pergantian ini sudah selesai dan ingin mengembalikan jadwal ke semula?")
                    .setPositiveButton("Ya, Saya Mengerti (Kembalikan Normal)") { _, _ ->
                        db.clearMomentum()
                        binding.tvMomentumTitle.text = "Pembaruan Jadwal Momentum"
                        Toast.makeText(this, "Jadwal telah berhasil dikembalikan ke jadwal semula!", Toast.LENGTH_LONG).show()
                        selectDay("Senin", binding.btnDaySenin)
                    }
                    .setNegativeButton("Tetap Gunakan Jadwal Ini", null)
                    .show()
            }
        }

        binding.btnHeaderBell.setOnClickListener {
            showNotification(101, activeReminderTitle, activeReminderMessage)
            Toast.makeText(this, "Notifikasi pengingat jam dikirim!", Toast.LENGTH_SHORT).show()
        }

        binding.btnSetTimer.setOnClickListener {
            val timerOptions = arrayOf("10 Menit Sebelum", "15 Menit Sebelum", "20 Menit Sebelum", "30 Menit Sebelum")
            val minutesArray = arrayOf(10, 15, 20, 30)

            AlertDialog.Builder(this)
                .setTitle("Atur Waktu Pengingat Pergantian Jam")
                .setItems(timerOptions) { _, which ->
                    timerMinutes = minutesArray[which]
                    binding.btnSetTimer.text = "⏱️ ${timerMinutes} Mnt"
                    Toast.makeText(this, "Pengingat diatur: $timerMinutes menit sebelum sesi berganti", Toast.LENGTH_LONG).show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        showNotification(101, activeReminderTitle, "[Pengingat $timerMinutes Menit] $activeReminderMessage")
                    }, 3000)
                }
                .show()
        }

        binding.cardNotifPergantianJam.setOnClickListener {
            switchTab(1)
        }

        setupLiquidTouchGesture()

        binding.tabHome.setOnClickListener { switchTab(0) }
        binding.tabSchedule.setOnClickListener { switchTab(1) }
        binding.tabCalendar.setOnClickListener { switchTab(2) }
        binding.tabNotif.setOnClickListener { switchTab(3) }
        binding.tabProfile.setOnClickListener { switchTab(4) }

        binding.root.post {
            animateLiquidBubbleTo(0, animate = false)
        }

        binding.btnLogout.setOnClickListener {
            session.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupCustomUserBackground() {
        try {
            val username = session.getUsername().lowercase()
            val bgImageView = findViewById<ImageView>(R.id.ivCustomUserBackground) ?: return

            val customBgName = when {
                username.contains("opet") || session.getSiswaNama().contains("Rafasyah", ignoreCase = true) -> "bg_opet"
                username.contains("naufal") -> "bg_naufal"
                username.contains("abel") -> "bg_abel"
                else -> "gedung_telkom"
            }

            val customResId = resources.getIdentifier(customBgName, "drawable", packageName)
            if (customResId != 0) {
                bgImageView.setImageResource(customResId)
            } else {
                bgImageView.setImageResource(R.drawable.gedung_telkom)
            }

            bgImageView.alpha = 1.0f

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupLiquidTouchGesture() {
        binding.layoutBottomNavContainer.setOnTouchListener { view, event ->
            val totalWidth = view.width.toFloat()
            val tabWidth = totalWidth / 5f

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = event.x - binding.viewLiquidBubble.x
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    var newX = event.x - dX
                    val minX = 0f
                    val maxX = totalWidth - binding.viewLiquidBubble.width
                    if (newX < minX) newX = minX
                    if (newX > maxX) newX = maxX
                    binding.viewLiquidBubble.x = newX
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val touchX = event.x
                    val targetTab = (touchX / tabWidth).toInt().coerceIn(0, 4)
                    switchTab(targetTab)
                    true
                }
                else -> false
            }
        }
    }

    private fun switchTab(targetIndex: Int) {
        binding.viewFlipperPages.displayedChild = targetIndex
        animateLiquidBubbleTo(targetIndex, animate = true)
        currentTabIndex = targetIndex

        if (targetIndex == 1) loadSchedulePage()
        if (targetIndex == 2) loadCalendarPage()
        if (targetIndex == 3) loadNotificationPage()
    }

    private fun animateLiquidBubbleTo(tabIndex: Int, animate: Boolean) {
        val totalWidth = binding.layoutBottomNavContainer.width
        if (totalWidth == 0) return

        val tabWidth = totalWidth / 5f
        val targetX = (tabWidth * tabIndex) + (tabWidth / 2f) - (binding.viewLiquidBubble.width / 2f)

        if (animate) {
            binding.viewLiquidBubble.animate()
                .x(targetX)
                .setDuration(320)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
        } else {
            binding.viewLiquidBubble.x = targetX
        }

        val textViews = listOf(binding.textHome, binding.textSchedule, binding.textCalendar, binding.textNotif, binding.textProfile)
        for ((i, tv) in textViews.withIndex()) {
            if (i == tabIndex) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white))
                tv.paint.isFakeBoldText = true
            } else {
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                tv.paint.isFakeBoldText = false
            }
        }
    }

    private fun loadNotificationPage() {
        val container = binding.layoutNotifHistoryContainer
        container.removeAllViews()

        val itemStatus = layoutInflater.inflate(R.layout.item_single_lesson, container, false) as CardView
        itemStatus.findViewById<TextView>(R.id.tvSingleSubject).text = "🟢 Semua Saluran Notifikasi Aktif"
        itemStatus.findViewById<TextView>(R.id.tvSingleTime).text = "Terhubung Otomatis dengan Pengingat Jadwal"
        itemStatus.findViewById<TextView>(R.id.tvSingleRoom).text = "Peringatan akan berbunyi di status bar saat pergantian jam tiba."
        itemStatus.setCardBackgroundColor(Color.WHITE)
        container.addView(itemStatus)

        val itemJam = layoutInflater.inflate(R.layout.item_single_lesson, container, false) as CardView
        itemJam.findViewById<TextView>(R.id.tvSingleSubject).text = "🔔 " + activeReminderTitle
        itemJam.findViewById<TextView>(R.id.tvSingleTime).text = "Waktu: $timerMinutes Menit Sebelum Sesi Berakhir"
        itemJam.findViewById<TextView>(R.id.tvSingleRoom).text = activeReminderMessage
        itemJam.setCardBackgroundColor(Color.parseColor("#FEF2F2"))
        container.addView(itemJam)

        val latestMomentum = db.getLatestMomentum()
        if (latestMomentum != null) {
            val itemMomentum = layoutInflater.inflate(R.layout.item_single_lesson, container, false) as CardView
            itemMomentum.findViewById<TextView>(R.id.tvSingleSubject).text = "⚡ Perubahan Jadwal: " + latestMomentum["mapel_baru"]
            itemMomentum.findViewById<TextView>(R.id.tvSingleTime).text = "Sesi Diperbarui secara Langsung"
            itemMomentum.findViewById<TextView>(R.id.tvSingleRoom).text = "Ruang: " + latestMomentum["ruang_baru"] + " • " + latestMomentum["guru_baru"] + " (" + latestMomentum["alasan"] + ")"
            itemMomentum.findViewById<View>(R.id.tvSingleBadgeMomentum).visibility = View.VISIBLE
            itemMomentum.setCardBackgroundColor(Color.parseColor("#FFF7ED"))
            container.addView(itemMomentum)
        }
    }

    // MEMUAT JADWAL LENGKAP DENGAN PILAR HARI VERTIKAL (SKETSA TEMANMU)
    private fun loadSchedulePage() {
        val container = binding.scheduleContainer
        container.removeAllViews()

        val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")
        val allSchedules = db.getScheduleByClass(session.getKelasId())

        for (dayName in days) {
            val dailyLessons = allSchedules.filter { it["mapel"]?.contains("[$dayName]") == true }
            if (dailyLessons.isEmpty()) continue

            val dayGroupView = layoutInflater.inflate(R.layout.item_day_group, container, false)
            val cardPillar = dayGroupView.findViewById<CardView>(R.id.cardDayPillar)
            val tvVerticalDay = dayGroupView.findViewById<TextView>(R.id.tvVerticalDayName)
            val cardsContainer = dayGroupView.findViewById<LinearLayout>(R.id.layoutDayCardsContainer)

            val verticalLetters = dayName.uppercase().map { it.toString() }.joinToString("\n")
            tvVerticalDay.text = verticalLetters

            when (dayName) {
                "Senin" -> {
                    cardPillar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_senin))
                    tvVerticalDay.setTextColor(Color.WHITE)
                }
                "Selasa" -> {
                    cardPillar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_selasa))
                    tvVerticalDay.setTextColor(Color.BLACK)
                }
                "Rabu" -> {
                    cardPillar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_rabu))
                    tvVerticalDay.setTextColor(Color.BLACK)
                }
                "Kamis" -> {
                    cardPillar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_kamis))
                    tvVerticalDay.setTextColor(Color.WHITE)
                }
                "Jumat" -> {
                    cardPillar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_jumat))
                    tvVerticalDay.setTextColor(ContextCompat.getColor(this, R.color.text_cokelat_tua))
                }
            }

            for (lesson in dailyLessons) {
                val lessonCard = layoutInflater.inflate(R.layout.item_single_lesson, cardsContainer, false) as CardView
                val tvSubject = lessonCard.findViewById<TextView>(R.id.tvSingleSubject)
                val tvTime = lessonCard.findViewById<TextView>(R.id.tvSingleTime)
                val tvRoom = lessonCard.findViewById<TextView>(R.id.tvSingleRoom)
                val badgeMomentum = lessonCard.findViewById<TextView>(R.id.tvSingleBadgeMomentum)

                val cleanMapel = lesson["mapel"]?.replace("[$dayName] ", "") ?: ""
                val waktu = lesson["waktu"] ?: ""
                val guru = lesson["guru"] ?: ""
                val ruang = lesson["ruang"] ?: ""
                val isMomentum = lesson["isMomentum"] == "true"

                tvSubject.text = cleanMapel
                tvTime.text = waktu
                tvRoom.text = "Ruang: $ruang • $guru"

                when (dayName) {
                    "Senin" -> {
                        lessonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_senin))
                        tvSubject.setTextColor(Color.WHITE)
                        tvTime.setTextColor(Color.WHITE)
                        tvRoom.setTextColor(Color.WHITE)
                    }
                    "Selasa" -> {
                        lessonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_selasa))
                        tvSubject.setTextColor(Color.BLACK)
                        tvTime.setTextColor(Color.BLACK)
                        tvRoom.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                    }
                    "Rabu" -> {
                        lessonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_rabu))
                        tvSubject.setTextColor(Color.BLACK)
                        tvTime.setTextColor(Color.BLACK)
                        tvRoom.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                    }
                    "Kamis" -> {
                        lessonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_kamis))
                        tvSubject.setTextColor(Color.WHITE)
                        tvTime.setTextColor(Color.WHITE)
                        tvRoom.setTextColor(Color.WHITE)
                    }
                    "Jumat" -> {
                        lessonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_jumat))
                        tvSubject.setTextColor(ContextCompat.getColor(this, R.color.text_cokelat_tua))
                        tvTime.setTextColor(ContextCompat.getColor(this, R.color.text_cokelat_tua))
                        tvRoom.setTextColor(ContextCompat.getColor(this, R.color.text_cokelat_tua))
                    }
                }

                if (isMomentum) badgeMomentum.visibility = View.VISIBLE else badgeMomentum.visibility = View.GONE

                // POP-UP DIALOG DETAIL JADWAL KETIKA DIKETUK
                lessonCard.setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("Rincian Mata Pelajaran")
                        .setMessage(
                            "Mata Pelajaran: $cleanMapel\n" +
                                    "Hari: $dayName\n" +
                                    "Waktu KBM: $waktu\n" +
                                    "Guru Pengajar: $guru\n" +
                                    "Ruangan Kelas: $ruang\n" +
                                    if (isMomentum) "\n⚡ Status: Terkena Jadwal Momentum Dinamis" else "\nStatus: Jadwal Reguler Normal"
                        )
                        .setPositiveButton("Tutup", null)
                        .show()
                }

                cardsContainer.addView(lessonCard)
            }

            container.addView(dayGroupView)
        }
    }

    private fun loadCalendarPage() {
        val eventsContainer = binding.calendarEventsContainer
        eventsContainer.removeAllViews()

        val events = db.getEvents()
        for (event in events) {
            val tglAngka = event["tanggal_angka"]?.toIntOrNull() ?: 0
            val warnaHex = event["warna_hex"] ?: "#FEE2E2"

            if (tglAngka in 1..30) {
                val resId = resources.getIdentifier("tvDate$tglAngka", "id", packageName)
                if (resId != 0) {
                    val tvDateCell = findViewById<TextView>(resId)
                    tvDateCell?.setBackgroundColor(Color.parseColor(warnaHex))
                }
            }

            val tvEvent = TextView(this).apply {
                text = "📅 ${event["nama"]}\nKategori: ${event["kategori"]} | Waktu: ${event["tanggal"]}"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_primary))
                setPadding(32, 28, 32, 28)
                setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                elevation = 2f

                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 0, 16)
                layoutParams = params

                setOnClickListener {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(event["nama"])
                        .setMessage("Kategori: ${event["kategori"]}\nTanggal: ${event["tanggal"]}\nLokasi: ${event["lokasi"]}")
                        .setPositiveButton("Tutup", null)
                        .show()
                }
            }
            eventsContainer.addView(tvEvent)
        }
    }

    private fun selectDay(hari: String, selectedBtn: Button) {
        for (btn in dayButtons) {
            btn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        }
        selectedBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.telkom_red)
        selectedBtn.setTextColor(ContextCompat.getColor(this, R.color.white))

        val allSchedules = db.getScheduleByClass(session.getKelasId())
        val daily = allSchedules.filter { it["mapel"]?.contains("[$hari]") == true }

        binding.layoutTimetableCards.removeAllViews()
        timetableCardViews.clear()

        for ((index, mapelItem) in daily.withIndex()) {
            val card = CardView(this).apply {
                radius = 32f
                elevation = 4f
                setCardBackgroundColor(if (index == 0) ContextCompat.getColor(this@MainActivity, R.color.telkom_red_light) else ContextCompat.getColor(this@MainActivity, R.color.white))
                val params = LinearLayout.LayoutParams(620, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 24, 0)
                layoutParams = params
                isClickable = true
                isFocusable = true
                foreground = ContextCompat.getDrawable(this@MainActivity, android.R.drawable.list_selector_background)
            }

            timetableCardViews.add(card)
            val currentMapelName = mapelItem["mapel"]?.replace("[$hari] ", "") ?: ""

            card.setOnClickListener {
                for (c in timetableCardViews) {
                    c.setCardBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                }
                card.setCardBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.telkom_red_light))

                if (index + 1 < daily.size) {
                    val nextMapel = daily[index + 1]
                    val nextMapelName = nextMapel["mapel"]?.replace("[$hari] ", "") ?: ""
                    activeReminderTitle = "Pengingat Pergantian Jam: $nextMapelName"
                    activeReminderMessage = "Pergantian sesi ke $nextMapelName (${nextMapel["guru"]}) di ruang ${nextMapel["ruang"]} pukul ${nextMapel["waktu"]}."
                } else {
                    activeReminderTitle = "Sesi Terakhir ($currentMapelName)"
                    activeReminderMessage = "Ini adalah sesi pelajaran terakhir untuk hari $hari. Tidak ada pergantian jam berikutnya."
                }

                binding.tvNotifTitle.text = activeReminderTitle
                binding.tvNotifDesc.text = activeReminderMessage
                Toast.makeText(this@MainActivity, "Pengingat sesi aktif: $currentMapelName", Toast.LENGTH_SHORT).show()
            }

            val cardContent = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(36, 32, 36, 32)

                val tvPeriod = TextView(this@MainActivity).apply {
                    text = if (index == 0) "Sesi Saat Ini" else "Sesi ${index + 1}"
                    textSize = 12f
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.telkom_red))
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                val tvTime = TextView(this@MainActivity).apply {
                    text = mapelItem["waktu"]
                    textSize = 12f
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_secondary))
                }

                val tvSubject = TextView(this@MainActivity).apply {
                    text = currentMapelName
                    textSize = 15f
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_primary))
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    setPadding(0, 10, 0, 4)
                }

                val tvRoomTeacher = TextView(this@MainActivity).apply {
                    text = "Ruang: " + mapelItem["ruang"] + " • " + mapelItem["guru"]
                    textSize = 12f
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_secondary))
                }

                addView(tvPeriod)
                addView(tvTime)
                addView(tvSubject)
                addView(tvRoomTeacher)
            }

            card.addView(cardContent)
            binding.layoutTimetableCards.addView(card)
        }

        if (daily.size > 1) {
            val nextMapel = daily[1]
            val nextMapelName = nextMapel["mapel"]?.replace("[$hari] ", "") ?: ""
            activeReminderTitle = "Pengingat Pergantian Jam: $nextMapelName"
            activeReminderMessage = "10 Menit lagi pergantian ke sesi $nextMapelName (${nextMapel["guru"]}) di ${nextMapel["ruang"]}."
        } else if (daily.isNotEmpty()) {
            activeReminderTitle = "Jadwal Hari $hari"
            activeReminderMessage = "Hanya terdapat 1 sesi pelajaran hari ini."
        }
        binding.tvNotifTitle.text = activeReminderTitle
        binding.tvNotifDesc.text = activeReminderMessage
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
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notifId, notification)
    }
}