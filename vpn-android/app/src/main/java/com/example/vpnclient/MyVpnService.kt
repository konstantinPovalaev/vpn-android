package com.example.vpnclient

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramSocket
import java.net.InetSocketAddress

class MyVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var thread: Thread? = null
    private var running = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                val key = intent.getStringExtra("key") ?: return START_NOT_STICKY
                startVpn(key)
            }
            "STOP" -> {
                stopVpn()
            }
        }
        return START_STICKY
    }

    private fun startVpn(key: String) {
        // Создаём уведомление (требуется для Android 8+)
        createNotificationChannel()
        startForeground(1, createNotification())

        // Настраиваем VPN-интерфейс [4]
        val builder = Builder()
        builder.setSession("VPN Client")
        builder.addAddress("10.0.0.2", 32)  // IP клиента
        builder.addRoute("0.0.0.0", 0)      // Весь трафик через VPN
        builder.addDnsServer("8.8.8.8")     // DNS

        // Защищаем сокет от цикличности [4]
        val socket = DatagramSocket()
        protect(socket)

        vpnInterface = builder.establish()

        if (vpnInterface != null) {
            running = true
            thread = Thread {
                val input = FileInputStream(vpnInterface!!.fileDescriptor)
                val output = FileOutputStream(vpnInterface!!.fileDescriptor)

                val buffer = ByteArray(32767)

                while (running) {
                    try {
                        // Читаем пакет из TUN
                        val length = input.read(buffer)
                        if (length > 0) {
                            // Здесь нужно шифровать и отправлять на сервер
                            // Пока просто эхо-тест
                            output.write(buffer, 0, length)
                        }
                    } catch (e: Exception) {
                        break
                    }
                }
            }
            thread?.start()
        }
    }

    private fun stopVpn() {
        running = false
        thread?.interrupt()
        vpnInterface?.close()
        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "vpn_channel",
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "vpn_channel")
            .setContentTitle("VPN Client")
            .setContentText("VPN активен")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onRevoke() {
        stopVpn()
    }
}