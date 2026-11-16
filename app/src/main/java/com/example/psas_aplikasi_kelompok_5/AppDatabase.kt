package com.example.psas_aplikasi_kelompok_5

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase(context: Context) : SQLiteOpenHelper(context, "telkom_school.db", null, 11) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE users (id_user INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, nama_lengkap TEXT, id_kelas INTEGER, role TEXT)")
        db.execSQL("CREATE TABLE jadwal (id_jadwal INTEGER PRIMARY KEY AUTOINCREMENT, id_kelas INTEGER, mapel TEXT, guru TEXT, waktu TEXT, ruang TEXT, hari TEXT)")
        db.execSQL("CREATE TABLE momentum (id_momentum INTEGER PRIMARY KEY AUTOINCREMENT, id_jadwal INTEGER UNIQUE, mapel_baru TEXT, guru_baru TEXT, waktu_baru TEXT, ruang_baru TEXT, alasan TEXT)")

        // Tabel Kalender dilengkapi kolom 'tanggal_angka' (1-30) dan 'warna_hex'
        db.execSQL("CREATE TABLE kalender (id_agenda INTEGER PRIMARY KEY AUTOINCREMENT, nama_agenda TEXT, kategori TEXT, tanggal TEXT, tanggal_angka INTEGER, warna_hex TEXT, lokasi TEXT)")

        // Data Users
        db.execSQL("INSERT INTO users (username, password, nama_lengkap, id_kelas, role) VALUES ('naufal', '123', 'Naufal Azka Ilhami', 4, 'Siswa')")
        db.execSQL("INSERT INTO users (username, password, nama_lengkap, id_kelas, role) VALUES ('opet', '123', 'Rafasyah Javas Nararya', 4, 'Siswa')")
        db.execSQL("INSERT INTO users (username, password, nama_lengkap, id_kelas, role) VALUES ('Abel', '09082010', 'Anabella Earline Andiwa', 4, 'Siswa')")
        db.execSQL("INSERT INTO users (username, password, nama_lengkap, id_kelas, role) VALUES ('admin_kurikulum', 'admin123', 'Admin Kurikulum Stematel', 0, 'Admin Kurikulum')")
        db.execSQL("INSERT INTO users (username, password, nama_lengkap, id_kelas, role) VALUES ('admin_osis', 'osis123', 'Pengurus OSIS Stematel', 0, 'Admin Organisasi')")

        // Data Jadwal XI PPLG 4
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (1, 4, 'Mapel Pilihan 1B', 'Pak Thoriq', '07:40 - 11:40', 'A 2.5', 'Senin')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (2, 4, 'MK1', 'Pak Fendi', '11:40 - 15:20', 'A 2.6', 'Senin')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (3, 4, 'PAI', 'Bu Siti', '07:10 - 09:40', 'C 3.1', 'Selasa')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (4, 4, 'MK 2', 'Bu Firda', '09:40 - 12:20', 'RPS SELATAN', 'Selasa')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (5, 4, 'MK 3', 'Bu Novi', '13:20 - 16:00', 'RPS SELATAN', 'Selasa')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (6, 4, 'Bahasa Jawa', 'Pak Andhang', '08:20 - 09:40', 'A 3.6', 'Rabu')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (7, 4, 'PJOK', 'Pak Fafa', '09:40 - 11:00', 'B 2.4', 'Rabu')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (8, 4, 'Bahasa Indonesia', 'Pak Adi', '11:00 - 14:00', 'C 2.2', 'Rabu')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (9, 4, 'KIK-A', 'Bu Tutut', '14:00 - 16:00', 'RPS SELATAN', 'Rabu')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (10, 4, 'BK', 'Bu Anisa', '07:10 - 08:30', 'B 1.1', 'Kamis')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (11, 4, 'Sejarah', 'Bu Nina', '09:00 - 10:20', 'B 2.3', 'Kamis')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (12, 4, 'KIK-C', 'Pak Reza', '10:20 - 11:40', 'D 2.1', 'Kamis')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (13, 4, 'MK 4', 'Bu Imelda', '11:40 - 15:20', 'D 2.1', 'Kamis')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (14, 4, 'PPC', 'Bu Shinta', '08:10 - 09:30', 'B 2.2', 'Jumat')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (15, 4, 'Matematika', 'Bu Ana', '09:30 - 11:30', 'B 1.4', 'Jumat')")
        db.execSQL("INSERT INTO jadwal (id_jadwal, id_kelas, mapel, guru, waktu, ruang, hari) VALUES (16, 4, 'Bahasa Inggris', 'Pak Oky', '12:50 - 15:30', 'A 3.4', 'Jumat')")

        // Agenda Awal November 2026 Lengkap dengan Angka Tanggal & Warna Kotak
        db.execSQL("INSERT INTO kalender (nama_agenda, kategori, tanggal, tanggal_angka, warna_hex, lokasi) VALUES ('SCC (Stematel Creative Challenge)', 'OSIS Events', '05 - 06 November 2026', 5, '#FEE2E2', 'SMK Telkom Purwokerto')")
        db.execSQL("INSERT INTO kalender (nama_agenda, kategori, tanggal, tanggal_angka, warna_hex, lokasi) VALUES ('Upacara Hari Pahlawan', 'Kegiatan Sekolah', '10 November 2026', 10, '#FEF3C7', 'Lapangan Utama Telkom')")
        db.execSQL("INSERT INTO kalender (nama_agenda, kategori, tanggal, tanggal_angka, warna_hex, lokasi) VALUES ('PSAS Semester Ganjil 2026/2027', 'Exam Dates', '20 - 30 November 2026', 20, '#DBEAFE', 'Ruang Kelas Masing-masing')")
        db.execSQL("INSERT INTO kalender (nama_agenda, kategori, tanggal, tanggal_angka, warna_hex, lokasi) VALUES ('Libur Semester Ganjil', 'School Holiday', '20 Des 2026 - 02 Jan 2027', 0, '#E2E8F0', 'Rumah Siswa')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS jadwal")
        db.execSQL("DROP TABLE IF EXISTS momentum")
        db.execSQL("DROP TABLE IF EXISTS kalender")
        onCreate(db)
    }

    fun checkLogin(u: String, p: String): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT username, nama_lengkap, id_kelas, role FROM users WHERE username = ? AND password = ?", arrayOf(u, p))
        var result: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            result = mapOf(
                "username" to cursor.getString(0),
                "nama_lengkap" to cursor.getString(1),
                "id_kelas" to cursor.getInt(2).toString(),
                "role" to cursor.getString(3)
            )
        }
        cursor.close()
        return result
    }

    fun getScheduleByClass(targetKelasId: Int = 4): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM jadwal WHERE id_kelas = ? ORDER BY id_jadwal ASC", arrayOf(targetKelasId.toString()))
        while (cursor.moveToNext()) {
            val idJadwal = cursor.getInt(cursor.getColumnIndexOrThrow("id_jadwal"))
            var mapel = cursor.getString(cursor.getColumnIndexOrThrow("mapel"))
            var guru = cursor.getString(cursor.getColumnIndexOrThrow("guru"))
            var waktu = cursor.getString(cursor.getColumnIndexOrThrow("waktu"))
            var ruang = cursor.getString(cursor.getColumnIndexOrThrow("ruang"))
            val hari = cursor.getString(cursor.getColumnIndexOrThrow("hari"))
            var isMomentum = "false"

            val mCursor = db.rawQuery("SELECT * FROM momentum WHERE id_jadwal = ? ORDER BY id_momentum DESC LIMIT 1", arrayOf(idJadwal.toString()))
            if (mCursor.moveToFirst()) {
                mapel = mCursor.getString(mCursor.getColumnIndexOrThrow("mapel_baru"))
                guru = mCursor.getString(mCursor.getColumnIndexOrThrow("guru_baru"))
                val waktuBaru = mCursor.getString(mCursor.getColumnIndexOrThrow("waktu_baru"))
                if (!waktuBaru.isNullOrBlank()) waktu = waktuBaru
                ruang = mCursor.getString(mCursor.getColumnIndexOrThrow("ruang_baru")) + " (" + mCursor.getString(mCursor.getColumnIndexOrThrow("alasan")) + ")"
                isMomentum = "true"
            }
            mCursor.close()

            list.add(mapOf(
                "mapel" to "[$hari] $mapel",
                "guru" to guru,
                "waktu" to waktu,
                "ruang" to ruang,
                "isMomentum" to isMomentum
            ))
        }
        cursor.close()
        return list
    }

    fun getLatestMomentum(): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM momentum ORDER BY id_momentum DESC LIMIT 1", null)
        var result: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            result = mapOf(
                "mapel_baru" to cursor.getString(cursor.getColumnIndexOrThrow("mapel_baru")),
                "guru_baru" to cursor.getString(cursor.getColumnIndexOrThrow("guru_baru")),
                "ruang_baru" to cursor.getString(cursor.getColumnIndexOrThrow("ruang_baru")),
                "alasan" to cursor.getString(cursor.getColumnIndexOrThrow("alasan"))
            )
        }
        cursor.close()
        return result
    }

    fun insertMomentum(idJadwal: Int, mapel: String, guru: String, waktu: String, ruang: String, alasan: String) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("id_jadwal", idJadwal)
            put("mapel_baru", mapel)
            put("guru_baru", guru)
            put("waktu_baru", waktu)
            put("ruang_baru", ruang)
            put("alasan", alasan)
        }
        db.insertWithOnConflict("momentum", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun clearMomentum() {
        val db = writableDatabase
        db.execSQL("DELETE FROM momentum")
    }

    fun getEvents(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM kalender", null)
        while (cursor.moveToNext()) {
            list.add(mapOf(
                "id_agenda" to cursor.getInt(cursor.getColumnIndexOrThrow("id_agenda")).toString(),
                "nama" to cursor.getString(cursor.getColumnIndexOrThrow("nama_agenda")),
                "kategori" to cursor.getString(cursor.getColumnIndexOrThrow("kategori")),
                "tanggal" to cursor.getString(cursor.getColumnIndexOrThrow("tanggal")),
                "tanggal_angka" to cursor.getInt(cursor.getColumnIndexOrThrow("tanggal_angka")).toString(),
                "warna_hex" to (cursor.getString(cursor.getColumnIndexOrThrow("warna_hex")) ?: "#FEE2E2"),
                "lokasi" to cursor.getString(cursor.getColumnIndexOrThrow("lokasi"))
            ))
        }
        cursor.close()
        return list
    }

    // FUNGSI UNTUK MENGHAPUS KEGIATAN TERTENTU (FITUR CRUD DELETE)
    fun deleteEvent(idAgenda: Int) {
        val db = writableDatabase
        db.execSQL("DELETE FROM kalender WHERE id_agenda = ?", arrayOf(idAgenda.toString()))
    }
}