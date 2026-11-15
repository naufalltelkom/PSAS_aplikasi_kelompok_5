package com.example.psas_aplikasi_kelompok_5

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id_user: Int = 0,
    val username: String,
    val password_hash: String,
    val role: String // Siswa, Admin Kurikulum, Admin Organisasi, Kepala Sekolah
)

@Entity(tableName = "kelas")
data class KelasEntity(
    @PrimaryKey(autoGenerate = true) val id_kelas: Int = 0,
    val nama_kelas: String
)

@Entity(tableName = "siswa")
data class SiswaEntity(
    @PrimaryKey(autoGenerate = true) val id_siswa: Int = 0,
    val id_user: Int,
    val id_kelas: Int,
    val nisn: String,
    val nama_siswa: String
)

@Entity(tableName = "jadwal_pelajaran")
data class JadwalPelajaranEntity(
    @PrimaryKey(autoGenerate = true) val id_jadwal: Int = 0,
    val id_kelas: Int,
    val hari: String,
    val jam_mulai: String,
    val jam_selesai: String,
    val mata_pelajaran: String,
    val guru_pengajar: String,
    val ruang_kelas: String
)

@Entity(tableName = "jadwal_momentum")
data class JadwalMomentumEntity(
    @PrimaryKey(autoGenerate = true) val id_momentum: Int = 0,
    val id_jadwal: Int,
    val id_kelas: Int,
    val tgl_perubahan: String,
    val jam_mulai_baru: String,
    val jam_selesai_baru: String,
    val mapel_baru: String,
    val guru_baru: String,
    val ruangan_baru: String,
    val alasan: String
)

@Entity(tableName = "kalender_akademik")
data class KalenderAkademikEntity(
    @PrimaryKey(autoGenerate = true) val id_agenda: Int = 0,
    val id_user: Int,
    val nama_agenda: String,
    val kategori: String, // Libur, Ujian, OSIS, PMR
    val tgl_mulai: String,
    val tgl_selesai: String,
    val lokasi: String
)