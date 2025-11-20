package co.edu.unipiloto.myapplication.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import co.edu.unipiloto.myapplication.R

class GuideNotificationService : Service() {

    companion object {
        const val CHANNEL_ID = "GuideNotificationChannel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_MESSAGE = "extra_message"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra(EXTRA_MESSAGE) ?: "Evento ocurrido"

        // Mostrar notificación
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Logistics App")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification) // icono de tu app
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Servicio en primer plano para asegurar ejecución
        startForeground(NOTIFICATION_ID, notification)

        // Detener servicio después de mostrar notificación
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // No es un bound service
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Guía Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
