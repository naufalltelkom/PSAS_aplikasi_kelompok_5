package com.example.psas_aplikasi_kelompok_5

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.psas_aplikasi_kelompok_5.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        db = AppDatabase(this)

        // 1. TOMBOL MASUK
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Silakan isi Nama Pengguna dan Kata Sandi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = db.checkLogin(username, password)
            if (user != null) {
                val role = user["role"] ?: "Siswa"
                val namaLengkap = user["nama_lengkap"] ?: username
                val idKelas = user["id_kelas"]?.toIntOrNull() ?: 4
                val remember = binding.switchRemember.isChecked

                if (role.contains("Admin", ignoreCase = true)) {
                    session.saveLoginSession(
                        userId = 99,
                        username = username,
                        role = role,
                        kelasId = 0,
                        kelasNama = "Kurikulum & Organisasi",
                        siswaNama = namaLengkap,
                        remember = remember
                    )
                    Toast.makeText(this, "Selamat datang, $namaLengkap!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, AdminActivity::class.java))
                } else {
                    session.saveLoginSession(
                        userId = 1,
                        username = username,
                        role = "Siswa",
                        kelasId = idKelas,
                        kelasNama = KelasCatalog.name(idKelas),
                        siswaNama = namaLengkap,
                        remember = remember
                    )
                    Toast.makeText(this, "Berhasil masuk! Halo $namaLengkap", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            } else {
                Toast.makeText(this, "Nama Pengguna atau Kata Sandi salah!", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. KLIK "BELUM PUNYA AKUN? DAFTAR DI SINI" -> BUKA FORM PENDAFTARAN LENGKAP
        // Buka Layar 3 (Registrasi Akun Baru)
        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // DIALOG PENDAFTARAN AKUN SISWA BARU
    private fun showRegisterDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val etNewNama = EditText(this).apply { hint = "Nama Lengkap Siswa" }
        val etNewUser = EditText(this).apply { hint = "Nama Pengguna (Username)" }
        val etNewPass = EditText(this).apply { hint = "Kata Sandi" }

        layout.addView(etNewNama)
        layout.addView(etNewUser)
        layout.addView(etNewPass)

        AlertDialog.Builder(this)
            .setTitle("Pendaftaran Akun Siswa Baru")
            .setView(layout)
            .setPositiveButton("Daftar Sekarang") { _, _ ->
                val nama = etNewNama.text.toString().trim()
                val user = etNewUser.text.toString().trim()
                val pass = etNewPass.text.toString().trim()

                if (nama.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this, "Semua data pendaftaran wajib diisi!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Masukkan data siswa baru ke tabel users database lokal
                val writableDb = db.writableDatabase
                val cv = ContentValues().apply {
                    put("username", user)
                    put("password", pass)
                    put("nama_lengkap", nama)
                    put("id_kelas", 4) // Default kelas XI PPLG 4
                    put("role", "Siswa")
                }
                val res = writableDb.insert("users", null, cv)

                if (res != -1L) {
                    Toast.makeText(this, "Pendaftaran berhasil! Silakan masuk dengan akun barumu.", Toast.LENGTH_LONG).show()
                    binding.etUsername.setText(user)
                    binding.etPassword.setText(pass)
                } else {
                    Toast.makeText(this, "Gagal mendaftar, coba nama pengguna lain.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}