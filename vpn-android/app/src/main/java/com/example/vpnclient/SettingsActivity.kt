package com.example.vpnclient

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPref = getSharedPreferences("VPNPrefs", MODE_PRIVATE)

        val resetButton = findViewById<Button>(R.id.reset_button)
        val telegramButton = findViewById<Button>(R.id.telegram_button)
        val backButton = findViewById<Button>(R.id.back_button)

        resetButton.setOnClickListener {
            sharedPref.edit().remove("vpn_key").apply()
            Toast.makeText(this, "Ключ сброшен", Toast.LENGTH_SHORT).show()
            finish()
        }

        telegramButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/your_channel"))
            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}