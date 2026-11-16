package com.example.psas_aplikasi_kelompok_5

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("TelkomAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
        const val KEY_USER_ID = "userId"
        const val KEY_USERNAME = "username"
        const val KEY_ROLE = "role"
        const val KEY_KELAS_ID = "kelasId"
        const val KEY_KELAS_NAMA = "kelasNama"
        const val KEY_SISWA_NAMA = "siswaNama"
    }

    fun saveLoginSession(userId: Int, username: String, role: String, kelasId: Int, kelasNama: String, siswaNama: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_ROLE, role)
            putInt(KEY_KELAS_ID, kelasId)
            putString(KEY_KELAS_NAMA, kelasNama)
            putString(KEY_SISWA_NAMA, siswaNama)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun getRole(): String = prefs.getString(KEY_ROLE, "") ?: ""
    fun getKelasNama(): String = prefs.getString(KEY_KELAS_NAMA, "XI PPLG 4") ?: "XI PPLG 4"
    fun getSiswaNama(): String = prefs.getString(KEY_SISWA_NAMA, "Student") ?: "Student"
    fun getKelasId(): Int = prefs.getInt(KEY_KELAS_ID, 1)
    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("isDarkMode", isDark).apply()
    }

    fun isDarkMode(): Boolean = prefs.getBoolean("isDarkMode", false)
}