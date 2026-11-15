package com.example.psas_aplikasi_kelompok_5

import androidx.room.*

@Dao
interface SchoolDao {
    // Otentikasi Login (FR-01)
    @Query("SELECT * FROM users WHERE username = :u AND password_hash = :p LIMIT 1")
    suspend fun login(u: String, p: String): UserEntity?

    @Query("SELECT * FROM siswa WHERE id_user = :userId LIMIT 1")
    suspend fun getSiswaByUserId(userId: Int): SiswaEntity?

    @Query("SELECT * FROM kelas WHERE id_kelas = :kelasId LIMIT 1")
    suspend fun getKelasById(kelasId: Int): KelasEntity?

    // Jadwal Pelajaran Terpersonalisasi Kelas XI PPLG 4 (FR-02)
    @Query("SELECT * FROM jadwal_pelajaran WHERE id_kelas = :kelasId AND hari = :hari")
    suspend fun getJadwalByKelasAndHari(kelasId: Int, hari: String): List<JadwalPelajaranEntity>

    // Jadwal Dinamis / Momentum yang Menimpa Jadwal Reguler (FR-04)
    @Query("SELECT * FROM jadwal_momentum WHERE id_kelas = :kelasId AND tgl_perubahan = :tgl")
    suspend fun getMomentumToday(kelasId: Int, tgl: String): List<JadwalMomentumEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMomentum(momentum: JadwalMomentumEntity)

    // Kalender Akademik 2026/2027 (FR-05)
    @Query("SELECT * FROM kalender_akademik")
    suspend fun getAllEvents(): List<KalenderAkademikEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: KalenderAkademikEntity)

    // Master Data Awal Otomatis
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialUsers(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialKelas(kelas: List<KelasEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialSiswa(siswa: List<SiswaEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialJadwal(jadwal: List<JadwalPelajaranEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialEvents(events: List<KalenderAkademikEntity>)
}