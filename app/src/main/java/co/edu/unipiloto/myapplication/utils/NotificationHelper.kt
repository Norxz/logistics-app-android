package co.edu.unipiloto.myapplication.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import co.edu.unipiloto.myapplication.R

object NotificationHelper {

    private const val CHANNEL_ID = "logistics_app_channel"
    private const val CHANNEL_NAME = "Logistics App Notifications"
    private const val CHANNEL_DESC = "Notificaciones de la aplicación Logistics App"

    /**
     * Muestra una notificación con título y contenido.
     * Se asegura de tener el permiso necesario en Android 13+.
     */
    fun showNotification(context: Context, title: String, content: String, notificationId: Int = 1) {
        // Crear canal si es necesario
        createNotificationChannel(context)

        // Revisar permiso en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // No hay permiso, solicitarlo si es una Activity
            if (context is androidx.fragment.app.FragmentActivity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // tu icono
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    /**
     * Crea el canal de notificación (Android 8+)
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
