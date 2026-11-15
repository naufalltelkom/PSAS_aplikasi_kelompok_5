package com.example.psas_aplikasi_kelompok_5

import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.psas_aplikasi_kelompok_5.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase(this)

        // Dropdown Kelas Sesuai Jurusan
        val listKelas = arrayOf("XI PPLG 4", "XI PPLG 1", "XI PPLG 2", "XI PPLG 3", "XI PPLG 5", "XI TJKT 1", "XI TJKT 2")
        binding.spRegKelas.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listKelas)

        binding.btnSubmitRegister.setOnClickListener {
            val nama = binding.etRegNama.text.toString().trim()
            val user = binding.etRegUser.text.toString().trim()
            val pass = binding.etRegPass.text.toString().trim()
            val passConfirm = binding.etRegPassConfirm.text.toString().trim()

            if (nama.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Lengkapi seluruh data pendaftaran!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != passConfirm) {
                Toast.makeText(this, "Konfirmasi kata sandi tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan ke database
            val writableDb = db.writableDatabase
            val cv = ContentValues().apply {
                put("username", user)
                put("password", pass)
                put("nama_lengkap", nama)
                put("id_kelas", 4) // Default XI PPLG 4
                put("role", "Siswa")
            }
            val res = writableDb.insert("users", null, cv)

            if (res != -1L) {
                Toast.makeText(this, "Pendaftaran Berhasil! Silakan Masuk.", Toast.LENGTH_LONG).show()
                finish() // Kembali ke layar login
            } else {
                Toast.makeText(this, "Gagal, username sudah terdaftar!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}