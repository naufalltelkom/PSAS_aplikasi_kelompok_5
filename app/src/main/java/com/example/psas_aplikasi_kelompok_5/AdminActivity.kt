package com.example.psas_aplikasi_kelompok_5

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.psas_aplikasi_kelompok_5.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var session: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        db = AppDatabase(this)

        val role = session.getRole()
        binding.tvAdminRoleInfo.text = "Role: $role • ${session.getSiswaNama()}"

        // ==============================================================
        // LOGIKA PEMISAHAN ROLE TAMPILAN ADMIN
        // ==============================================================
        if (role.contains("Kurikulum", ignoreCase = true)) {
            // Jika login sebagai Admin Kurikulum: Tampilkan Kurikulum, Sembunyikan OSIS
            binding.tvAdminTitle.text = "Panel Admin Kurikulum"
            binding.layoutKurikulumSection.visibility = View.VISIBLE
            binding.layoutOsisSection.visibility = View.GONE
        } else {
            // Jika login sebagai Admin Organisasi (OSIS/PMR): Tampilkan OSIS, Sembunyikan Kurikulum
            binding.tvAdminTitle.text = "Panel Admin Organisasi (OSIS)"
            binding.layoutKurikulumSection.visibility = View.GONE
            binding.layoutOsisSection.visibility = View.VISIBLE
        }

        // 1. Data Dropdown Kurikulum
        val listKelas = arrayOf("XI PPLG 4", "XI PPLG 1", "XI PPLG 2", "XI PPLG 3", "X TJKT 1")
        binding.spKelas.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listKelas)

        val listJadwal = arrayOf(
            "Mapel Pilihan 1B (Senin - ID 1)",
            "MK1 (Senin - ID 2)",
            "PAI (Selasa - ID 3)",
            "MK 2 (Selasa - ID 4)",
            "KIK-A (Rabu - ID 9)"
        )
        binding.spJadwalTarget.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listJadwal)

        val listGuru = arrayOf("Pak Thoriq", "Pak Fendi", "Bu Siti", "Bu Firda", "Pak Adi", "Bu Tutut")
        binding.spGuru.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listGuru)

        val listRuang = arrayOf("A 2.5", "A 2.6", "C 3.1", "RPS SELATAN", "C 2.2", "Lab Cloud")
        binding.spRuangan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listRuang)

        // 2. Data Dropdown Organisasi (OSIS)
        val listKategoriOrg = arrayOf("OSIS Events", "Aksaradaya", "Akabara", "Pramuka", "Pemilihan Ketua & Wakil OSIS")
        binding.spKategoriOrganisasi.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listKategoriOrg)

        // Dropdown Pilihan Tanggal 1 - 30 November
        val listTanggalNov = (1..30).map { "$it November 2026" }.toTypedArray()
        binding.spTanggalNovember.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listTanggalNov)

        // Dropdown Pilihan Warna Penanda Kalender
        val listWarnaLabel = arrayOf("Hijau Muda (PMR/Umum)", "Ungu Elegan (OSIS Khusus)", "Oranye Cerah (Peringatan)", "Biru Langit (Ujian/Latihan)", "Merah Muda (Penting)")
        val listWarnaHex = arrayOf("#D1FAE5", "#E9D5FF", "#FFEDD5", "#CFFAFE", "#FCE7F3")
        binding.spWarnaEvent.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listWarnaLabel)

        binding.btnUploadPdf.setOnClickListener {
            Toast.makeText(this, "File PDF Jadwal Reguler Berhasil Disinkronisasi!", Toast.LENGTH_LONG).show()
        }

        // AKSI KURIKULUM: SIMPAN MOMENTUM
        binding.btnSaveMomentum.setOnClickListener {
            val mapelBaru = binding.etMapelBaru.text.toString().trim()
            val guruBaru = binding.spGuru.selectedItem.toString()
            val ruangBaru = binding.spRuangan.selectedItem.toString()
            val alasan = binding.etAlasan.text.toString().trim()

            val targetIdJadwal = when (binding.spJadwalTarget.selectedItemPosition) {
                0 -> 1
                1 -> 2
                2 -> 3
                3 -> 4
                4 -> 9
                else -> 1
            }

            if (mapelBaru.isEmpty()) {
                Toast.makeText(this, "Silakan isi nama mapel pengganti!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.insertMomentum(
                idJadwal = targetIdJadwal,
                mapel = mapelBaru,
                guru = guruBaru,
                waktu = "Sesi Diperbarui",
                ruang = ruangBaru,
                alasan = if (alasan.isEmpty()) "Pergantian Dinamis" else alasan
            )

            Toast.makeText(this, "Jadwal Momentum Berhasil Ditimpa secara Real-Time!", Toast.LENGTH_LONG).show()
            finish()
        }

        // AKSI OSIS: TAMBAH AGENDA DENGAN TANGGAL & WARNA KHUSUS
        binding.btnAddEvent.setOnClickListener {
            val namaAgenda = binding.etNamaAgenda.text.toString().trim()
            val kategori = binding.spKategoriOrganisasi.selectedItem.toString()
            val selectedDateStr = binding.spTanggalNovember.selectedItem.toString()
            val tanggalAngka = binding.spTanggalNovember.selectedItemPosition + 1 // Angka 1 sampai 30
            val warnaHexDipilih = listWarnaHex[binding.spWarnaEvent.selectedItemPosition]
            val lokasi = binding.etLokasiAgenda.text.toString().trim()

            if (namaAgenda.isEmpty()) {
                Toast.makeText(this, "Isi nama kegiatan terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val writableDb = db.writableDatabase
            val cv = android.content.ContentValues().apply {
                put("nama_agenda", namaAgenda)
                put("kategori", kategori)
                put("tanggal", selectedDateStr)
                put("tanggal_angka", tanggalAngka)
                put("warna_hex", warnaHexDipilih)
                put("lokasi", if (lokasi.isEmpty()) "SMK Telkom Purwokerto" else lokasi)
            }
            writableDb.insert("kalender", null, cv)

            Toast.makeText(this, "Agenda Berhasil Ditambahkan ke Tanggal $tanggalAngka November!", Toast.LENGTH_LONG).show()
            binding.etNamaAgenda.text.clear()
            binding.etLokasiAgenda.text.clear()
        }

        // TOMBOL LOGOUT RESMI ADMIN
        binding.btnAdminLogout.setOnClickListener {
            session.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}