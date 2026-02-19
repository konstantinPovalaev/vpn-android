package com.example.vpnclient

import android.content.Intent
import android.content.SharedPreferences
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var keyInput: EditText
    private lateinit var saveButton: Button
    private lateinit var statusText: TextView
    private lateinit var mainButton: Button
    private lateinit var settingsButton: Button

    private var vpnRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = getSharedPreferences("VPNPrefs", MODE_PRIVATE)

        // Инициализация UI
        keyInput = findViewById(R.id.key_input)
        saveButton = findViewById(R.id.save_button)
        statusText = findViewById(R.id.status_text)
        mainButton = findViewById(R.id.main_button)
        settingsButton = findViewById(R.id.settings_button)

        // Проверяем, есть ли сохранённый ключ
        val savedKey = sharedPref.getString("vpn_key", null)
        if (savedKey != null) {
            showMainScreen()
        } else {
            showKeyScreen()
        }

        // Кнопка сохранения ключа
        saveButton.setOnClickListener {
            val key = keyInput.text.toString().trim()
            if (key.isNotEmpty()) {
                sharedPref.edit().putString("vpn_key", key).apply()
                showMainScreen()
            } else {
                Toast.makeText(this, "Введите ключ", Toast.LENGTH_SHORT).show()
            }
        }

        // Круглая кнопка START/OFF
        mainButton.setOnClickListener {
            if (!vpnRunning) {
                startVpn()
            } else {
                stopVpn()
            }
        }

        // Кнопка настроек (шестерёнка)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showKeyScreen() {
        keyInput.visibility = EditText.VISIBLE
        saveButton.visibility = Button.VISIBLE
        statusText.visibility = TextView.GONE
        mainButton.visibility = Button.GONE
        settingsButton.visibility = Button.GONE
    }

    private fun showMainScreen() {
        keyInput.visibility = EditText.GONE
        saveButton.visibility = Button.GONE
        statusText.visibility = TextView.VISIBLE
        mainButton.visibility = Button.VISIBLE
        settingsButton.visibility = Button.VISIBLE

        statusText.text = "VPN отключён"
        mainButton.text = "START"
        mainButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
        vpnRunning = false
    }

    private fun startVpn() {
        // Запрашиваем разрешение на VPN
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, 0)
        } else {
            onActivityResult(0, RESULT_OK, null)
        }
    }

    private fun stopVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = "STOP"
        startService(intent)

        mainButton.text = "START"
        mainButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
        statusText.text = "VPN отключён"
        vpnRunning = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            // Запускаем VPN-сервис
            val intent = Intent(this, MyVpnService::class.java)
            intent.action = "START"
            intent.putExtra("key", sharedPref.getString("vpn_key", ""))
            startService(intent)

            mainButton.text = "OFF"
            mainButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            statusText.text = "VPN включён"
            vpnRunning = true
        }
    }
}